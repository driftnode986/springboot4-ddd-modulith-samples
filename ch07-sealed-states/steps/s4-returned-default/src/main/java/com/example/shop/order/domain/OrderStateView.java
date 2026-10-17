package com.example.shop.order.domain;

import com.example.shop.order.domain.OrderState.Accepted;
import com.example.shop.order.domain.OrderState.Cancelled;
import com.example.shop.order.domain.OrderState.Confirmed;
import com.example.shop.order.domain.OrderState.Paid;
import com.example.shop.order.domain.OrderState.Reserved;

public final class OrderStateView {

    public static String label(OrderState state) {
        // #region switch
        return switch (state) {
            case Accepted a -> "ご注文を承りました";
            case Reserved r -> "在庫を確保しました";
            case Paid p -> "お支払いを確認しました ("
                    + p.paymentId().value() + ")";
            case Confirmed c -> "出荷が確定しました";
            case Cancelled c -> "キャンセル済み: " + c.reason();
            default -> "処理中";
        };
        // #endregion
    }
}
