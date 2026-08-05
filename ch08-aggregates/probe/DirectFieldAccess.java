// コンパイルできないことを確かめるためのファイル。
package com.example.shop.order.domain;

import java.time.Instant;

class DirectFieldAccess {
    void attempt() {
        Order order = Order.place(new OrderId("O-1"), new CustomerId("C-1"), Instant.now());
        order.lines.clear();
    }
}
