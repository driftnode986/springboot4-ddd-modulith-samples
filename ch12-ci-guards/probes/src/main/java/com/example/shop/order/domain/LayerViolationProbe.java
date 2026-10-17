package com.example.shop.order.domain;

import com.example.shop.order.infrastructure.JpaOrderRepository;

public class LayerViolationProbe {

    private final JpaOrderRepository repository;

    public LayerViolationProbe(JpaOrderRepository repository) {
        this.repository = repository;
    }
}
