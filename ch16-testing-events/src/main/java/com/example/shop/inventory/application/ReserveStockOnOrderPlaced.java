package com.example.shop.inventory.application;

import com.example.shop.inventory.domain.HandledEvents;
import com.example.shop.inventory.domain.Stock;
import com.example.shop.inventory.domain.StockRepository;
import com.example.shop.order.spi.OrderPlaced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 注文が成立した事実を受けて在庫を引き当てる。
 *
 * <p>同じイベントが 2 回届くことを前提にしている。1 回目だけが引き当てを行い、
 * 2 回目以降は記録を見て何もしない。
 */
@Component
public class ReserveStockOnOrderPlaced {

    private static final Logger log = LoggerFactory.getLogger(ReserveStockOnOrderPlaced.class);
    private static final String HANDLER = "inventory.ReserveStockOnOrderPlaced";

    private final StockRepository stocks;
    private final HandledEvents handledEvents;

    ReserveStockOnOrderPlaced(StockRepository stocks, HandledEvents handledEvents) {
        this.stocks = stocks;
        this.handledEvents = handledEvents;
    }

    @ApplicationModuleListener
    void on(OrderPlaced event) {
        if (!handledEvents.recordIfFirst(event.eventId(), HANDLER)) {
            log.info("処理済みのイベントなので読み飛ばします eventId={}", event.eventId());
            return;
        }

        for (OrderPlaced.Item item : event.items()) {
            Stock stock =
                    stocks.findBySku(item.productId())
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "在庫がありません: " + item.productId()));
            stock.reserve(item.quantity());
            stocks.save(stock);
            log.info("在庫を引き当てました sku={} 残り={}", stock.sku(), stock.quantity());
        }
    }
}
