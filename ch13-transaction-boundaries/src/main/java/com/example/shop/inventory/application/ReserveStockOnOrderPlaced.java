package com.example.shop.inventory.application;

import com.example.shop.order.spi.OrderPlaced;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 注文が成立したことを受けて在庫を引き当てる。
 *
 * <p>注文モジュールはこのクラスを知らない。知っているのは「注文が成立した」という事実だけで、
 * それを誰が受け取るかは注文の関心事ではない。
 */
@Component
public class ReserveStockOnOrderPlaced {

    private static final Logger log = LoggerFactory.getLogger(ReserveStockOnOrderPlaced.class);

    /** 引き当てが終わった注文。第13章では受信の有無を外から確かめるために公開する。 */
    public static final Set<String> reserved = ConcurrentHashMap.newKeySet();

    /** 引き当てに失敗した注文。 */
    public static final Set<String> failed = ConcurrentHashMap.newKeySet();

    /** 次の 1 件を失敗させる。境界の実測でだけ使う。 */
    public static volatile boolean failNext = false;

    @ApplicationModuleListener
    void on(OrderPlaced event) {
        log.info("在庫の引き当てを開始します orderId={}", event.orderId());
        if (failNext) {
            failNext = false;
            failed.add(event.orderId());
            throw new IllegalStateException("在庫の引き当てに失敗しました");
        }
        reserved.add(event.orderId());
        log.info("在庫の引き当てが終わりました orderId={}", event.orderId());
    }
}
