package com.example.shop.before;

public final class OrderWithEnumDemo {

    public static void main(String[] args) {
        // #region demo
        OrderWithEnum order = new OrderWithEnum();
        System.out.println("status    = " + order.status());
        System.out.println("paymentId = " + order.paymentId());
        System.out.println("length    = "
                + order.paymentId().length());
        // #endregion
    }
}
