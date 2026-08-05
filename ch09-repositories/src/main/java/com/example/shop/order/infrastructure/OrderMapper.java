package com.example.shop.order.infrastructure;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderLine;
import com.example.shop.order.domain.OrderState;
import com.example.shop.order.domain.PaymentId;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import com.example.shop.order.domain.ReservationId;
import java.util.Currency;

/**
 * ドメインの Order と表の形の OrderEntity を相互に変換する。
 * この 1 クラスだけが両方を知っている。
 */
final class OrderMapper {

    private OrderMapper() {}

    static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity(order.id().value());
        entity.setCustomerId(order.customerId().value());
        applyState(entity, order.state());
        for (OrderLine line : order.lines()) {
            entity.getLines()
                    .add(new OrderLineEntity(
                            line.productId().value(),
                            line.quantity().value(),
                            line.unitPrice().amount(),
                            line.unitPrice().currency().getCurrencyCode()));
        }
        return entity;
    }

    /** 状態ごとに持つ項目が違うので、判別のための名前と一緒に平らに置く。 */
    private static void applyState(OrderEntity entity, OrderState state) {
        switch (state) {
            case OrderState.Accepted s -> {
                entity.setState("ACCEPTED");
                entity.setAcceptedAt(s.acceptedAt());
            }
            case OrderState.Reserved s -> {
                entity.setState("RESERVED");
                entity.setAcceptedAt(s.acceptedAt());
                entity.setReservationId(s.reservationId().value());
            }
            case OrderState.Paid s -> {
                entity.setState("PAID");
                entity.setAcceptedAt(s.acceptedAt());
                entity.setReservationId(s.reservationId().value());
                entity.setPaymentId(s.paymentId().value());
            }
            case OrderState.Confirmed s -> {
                entity.setState("CONFIRMED");
                entity.setAcceptedAt(s.acceptedAt());
                entity.setReservationId(s.reservationId().value());
                entity.setPaymentId(s.paymentId().value());
                entity.setConfirmedAt(s.confirmedAt());
            }
            case OrderState.Cancelled s -> {
                entity.setState("CANCELLED");
                entity.setCancelledAt(s.cancelledAt());
                entity.setCancelReason(s.reason());
            }
        }
    }

    static Order toDomain(OrderEntity entity) {
        Order order = Order.restore(
                new OrderId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                toState(entity));
        for (OrderLineEntity line : entity.getLines()) {
            order.addLine(
                    new ProductId(line.getProductId()),
                    new Quantity(line.getQuantity()),
                    new Money(line.getUnitAmount(), Currency.getInstance(line.getUnitCurrency())));
        }
        return order;
    }

    /** 判別のための名前から、もとの状態を組み立て直す。 */
    private static OrderState toState(OrderEntity entity) {
        return switch (entity.getState()) {
            case "ACCEPTED" -> new OrderState.Accepted(entity.getAcceptedAt());
            case "RESERVED" -> new OrderState.Reserved(
                    entity.getAcceptedAt(), new ReservationId(entity.getReservationId()));
            case "PAID" -> new OrderState.Paid(
                    entity.getAcceptedAt(),
                    new ReservationId(entity.getReservationId()),
                    new PaymentId(entity.getPaymentId()));
            case "CONFIRMED" -> new OrderState.Confirmed(
                    entity.getAcceptedAt(),
                    new ReservationId(entity.getReservationId()),
                    new PaymentId(entity.getPaymentId()),
                    entity.getConfirmedAt());
            case "CANCELLED" -> new OrderState.Cancelled(
                    entity.getCancelledAt(), entity.getCancelReason());
            default -> throw new IllegalStateException("未知の状態です: " + entity.getState());
        };
    }
}
