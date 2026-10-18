package com.example.shop.payment.spi;

import java.util.List;

/**
 * 支払いが成立しなかったという事実。
 *
 * <p>すでに進んでしまった処理を戻すために発表する。戻す相手が何を戻せばよいかを
 * 判断できるよう、引き当ての対象を載せている。支払いの側は、誰が戻すのかを知らない。
 *
 * <p>eventId は {@link com.example.shop.order.spi.OrderPlaced} と同じく、この発表そのものの
 * 識別子で、注文の識別子とは別物。受け取る側が二度戻していないかを判断するために使う。
 */
public record PaymentFailed(String eventId, String orderId, List<Item> items, String reason) {

    public PaymentFailed {
        items = List.copyOf(items);
    }

    /** 戻す対象。商品と個数だけを持つ。 */
    public record Item(String productId, int quantity) {
    }
}
