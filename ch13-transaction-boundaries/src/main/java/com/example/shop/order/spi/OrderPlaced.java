package com.example.shop.order.spi;

/**
 * 注文が成立したという事実。
 *
 * <p>過去に起きたことなので、あとから書き換えられない値として持つ。
 * 受け取る側が必要とする分だけを載せ、注文集約そのものは渡さない。
 */
public record OrderPlaced(String orderId, String customerId) {
}
