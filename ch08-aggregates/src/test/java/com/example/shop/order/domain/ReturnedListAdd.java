package com.example.shop.order.domain;

/**
 * 本文の「次の呼び出しはコンパイルが通ります」の 1 行を、そのままコンパイルにかける。
 * テストではない（呼ばない）。実行したときの挙動は ImmutableAddProbeTest で確かめている。
 */
final class ReturnedListAdd {

    private ReturnedListAdd() {
    }

    static void addToReturnedList(Order order) {
        // #region add-to-lines
        order.lines().add(new OrderLine(new ProductId("P-999"),
                new Quantity(1), Money.yen(1)));
        // #endregion
    }
}
