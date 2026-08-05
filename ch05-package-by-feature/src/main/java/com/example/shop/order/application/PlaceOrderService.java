package com.example.shop.order.application;

import com.example.shop.inventory.spi.StockReservation;
import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderRepository;
import com.example.shop.payment.spi.PaymentRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PlaceOrderService {

    private final OrderRepository orders;
    private final StockReservation stocks;
    private final PaymentRequest payments;

    PlaceOrderService(OrderRepository orders, StockReservation stocks, PaymentRequest payments) {
        this.orders = orders;
        this.stocks = stocks;
        this.payments = payments;
    }

    public UUID place(String customerId, String sku, int quantity) {
        var order = new Order(UUID.randomUUID(), customerId);
        orders.save(order);
        stocks.reserve(sku, quantity);
        payments.request(order.id());
        return order.id();
    }
}
