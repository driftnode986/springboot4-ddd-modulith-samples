// コンパイルできないことを確かめるためのファイル。
// javac に直接渡して使う（Gradle のビルド対象には入れない）。
package com.example.shop.order.domain;

import java.time.Instant;

class DirectConstruction {
    void attempt() {
        Order order = new Order(new OrderId("O-1"), new CustomerId("C-1"), Instant.now());
    }
}
