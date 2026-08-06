package com.example.shop.order.application;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 注文を受け付けるユースケース。
 * 集約とリポジトリを呼ぶ順序を決めるだけで、業務規則そのものは持たない。
 */
@Service
public class PlaceOrderService {

    private final OrderRepository orders;
    private final Clock clock;

    public PlaceOrderService(OrderRepository orders, Clock clock) {
        this.orders = orders;
        this.clock = clock;
    }

    @Transactional
    public OrderId place(PlaceOrderCommand command) {
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("明細が 1 件もありません");
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
        return order.id();
    }
}
