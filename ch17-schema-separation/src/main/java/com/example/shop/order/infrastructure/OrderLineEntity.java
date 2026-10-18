package com.example.shop.order.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_lines", schema = "ordering")
class OrderLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_amount", nullable = false)
    private BigDecimal unitAmount;

    @Column(name = "unit_currency", nullable = false)
    private String unitCurrency;

    protected OrderLineEntity() {
        // JPA が使う
    }

    OrderLineEntity(String productId, int quantity, BigDecimal unitAmount, String unitCurrency) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
        this.unitCurrency = unitCurrency;
    }

    String getProductId() {
        return productId;
    }

    int getQuantity() {
        return quantity;
    }

    BigDecimal getUnitAmount() {
        return unitAmount;
    }

    String getUnitCurrency() {
        return unitCurrency;
    }
}
