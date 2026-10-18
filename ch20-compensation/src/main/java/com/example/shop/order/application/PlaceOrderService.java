package com.example.shop.order.application;

import com.example.shop.order.domain.CustomerId;
import com.example.shop.order.domain.Money;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.order.domain.ProductId;
import com.example.shop.order.domain.Quantity;
import com.example.shop.order.spi.OrderPlaced;
import com.example.shop.payment.spi.PaymentRequest;
import com.example.shop.shipping.spi.ShipmentArrangement;
import java.time.Clock;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ShipmentArrangement shipments;
    private final PaymentRequest payments;
    private final ApplicationEventPublisher events;

    public PlaceOrderService(
            OrderRepository orders,
            Clock clock,
            ShipmentArrangement shipments,
            PaymentRequest payments,
            ApplicationEventPublisher events) {
        this.orders = orders;
        this.clock = clock;
        this.shipments = shipments;
        this.payments = payments;
        this.events = events;
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

        // 出荷は直接呼ぶ。注文と同じトランザクションで動く。
        shipments.arrange(order.id().value(), command.address());

        // 在庫はイベントで繋ぐ。受け取る相手をここでは知らない。
        events.publishEvent(
                new OrderPlaced(
                        UUID.randomUUID().toString(),
                        order.id().value(),
                        command.customerId(),
                        command.lines().stream()
                                .map(line -> new OrderPlaced.Item(line.productId(), line.quantity()))
                                .toList()));

        return order.id();
    }

    /**
     * 請求を頼む。成立しなかったときに戻す対象を一緒に渡す。
     *
     * <p>引き当てはイベントで別のトランザクションに渡してあるので、ここで例外が起きても
     * データベースは在庫を戻さない。戻す処理は支払いの側から改めて頼むことになる。
     */
    @Transactional
    public void requestPayment(OrderId orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("注文がありません: " + orderId.value()));

        payments.request(
                UUID.fromString(order.id().value()),
                order.total().amount().longValue(),
                order.lines().stream()
                        .map(line -> new PaymentRequest.Reserved(
                                line.productId().value(), line.quantity().value()))
                        .toList());
    }
}
