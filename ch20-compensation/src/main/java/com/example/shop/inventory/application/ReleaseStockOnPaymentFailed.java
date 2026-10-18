package com.example.shop.inventory.application;

import com.example.shop.inventory.domain.HandledEvents;
import com.example.shop.inventory.domain.Stock;
import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.payment.spi.PaymentFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 支払いが成立しなかった事実を受けて、引き当てた在庫を戻す。
 *
 * <p>引き当ては別のトランザクションですでに確定しているため、データベースの機能では戻せない。
 * 戻す処理をここに書く。これが補償の実体で、引き当てとは別の、対になる処理として存在する。
 *
 * <p>引き当てと同じく、同じ発表が 2 回届くことを前提にしている。戻しは打ち消しではなく
 * 加算なので、二度実行すると在庫が実際より増える。記録を見て 1 回目だけを通す。
 */
@Component
public class ReleaseStockOnPaymentFailed {

    private static final Logger log = LoggerFactory.getLogger(ReleaseStockOnPaymentFailed.class);
    private static final String HANDLER = "inventory.ReleaseStockOnPaymentFailed";

    private final StockRepository stocks;
    private final HandledEvents handledEvents;

    ReleaseStockOnPaymentFailed(StockRepository stocks, HandledEvents handledEvents) {
        this.stocks = stocks;
        this.handledEvents = handledEvents;
    }

    @ApplicationModuleListener
    void on(PaymentFailed event) {
        if (!handledEvents.recordIfFirst(event.eventId(), HANDLER)) {
            log.info("戻し済みのイベントなので読み飛ばします eventId={}", event.eventId());
            return;
        }

        for (PaymentFailed.Item item : event.items()) {
            Stock stock =
                    stocks.findBySku(item.productId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "在庫がありません: " + item.productId()));
            stock.release(item.quantity());
            stocks.save(stock);
            log.info(
                    "引き当てを戻しました sku={} 残り={} 理由={}",
                    stock.sku(),
                    stock.quantity(),
                    event.reason());
        }
    }
}
