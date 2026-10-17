package com.example.steps.ch12.order.application;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import com.example.shop.shipping.spi.ShipmentArrangement;
import java.time.Clock;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * 第12章までの注文の受け付け（第13章でイベントを足す前の版）。
 *
 * <p>第13章の本文が書き換える前の形を示すために残している。
 * 完成形は {@code com.example.shop.order.application.PlaceOrderService}。
 * 部品スキャンの対象（com.example.shop）の外に置き、Bean にはしない。
 */
public class PlaceOrderService {

    private final OrderRepository orders;
    private final Clock clock;
    private final ShipmentArrangement shipments;

    public PlaceOrderService(
            OrderRepository orders,
            Clock clock,
            ShipmentArrangement shipments) {
        this.orders = orders;
        this.clock = clock;
        this.shipments = shipments;
    }

    // #region place
    @Transactional
    public OrderId place(PlaceOrderCommand command) {
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException(
                    "明細が 1 件もありません");
        }

        Order order = Order.place(
                new OrderId(UUID.randomUUID().toString()),
                new CustomerId(command.customerId()),
                clock.instant());

        for (PlaceOrderCommand.Line line : command.lines()) {
            order.addLine(
                    new ProductId(line.productId()),
                    new Quantity(line.quantity()),
                    Money.yen(line.unitPriceYen()));
        }

        orders.save(order);
        shipments.arrange(order.id().value(), command.address());
        return order.id();
    }
    // #endregion
}
