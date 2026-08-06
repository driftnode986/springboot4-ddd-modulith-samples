package com.example.shop.order.domain;

import java.util.Optional;

/**
 * 注文の保存と取り出し。ドメイン層はこのインターフェースだけを知っている。
 * どこに保存するかは infrastructure 側の実装が決める。
 */
public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(OrderId id);
}
