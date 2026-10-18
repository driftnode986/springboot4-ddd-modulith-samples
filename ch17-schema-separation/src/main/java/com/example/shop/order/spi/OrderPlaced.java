package com.example.shop.order.spi;

import java.util.List;

/**
 * 注文が成立したという事実。過去に起きたことなので書き換えられない。
 *
 * <p>eventId は「この発表そのもの」の識別子で、注文の識別子とは別物。
 * 受け取る側が、同じ発表を二度処理していないかを判断するために使う。
 *
 * <p>明細は、受け取る側が必要とする分だけを載せる。注文集約そのものは渡さない。
 */
public record OrderPlaced(String eventId, String orderId, String customerId, List<Item> items) {

    public OrderPlaced {
        items = List.copyOf(items);
    }

    /** 引き当てる対象。商品と個数だけを持つ。 */
    public record Item(String productId, int quantity) {
    }
}
