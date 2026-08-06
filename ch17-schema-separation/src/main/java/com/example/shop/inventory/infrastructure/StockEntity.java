package com.example.shop.inventory.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** 表の形をそのまま写した入れ物。ドメインの Stock とは別物。 */
@Entity
@Table(name = "stocks", schema = "inventory")
class StockEntity {

    @Id
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Version
    private long version;

    protected StockEntity() {
        // JPA 用
    }

    StockEntity(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    String getSku() {
        return sku;
    }

    int getQuantity() {
        return quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
