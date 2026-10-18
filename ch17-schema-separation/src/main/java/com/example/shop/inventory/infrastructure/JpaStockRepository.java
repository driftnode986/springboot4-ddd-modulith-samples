package com.example.shop.inventory.infrastructure;

import com.example.shop.inventory.domain.Stock;
import com.example.shop.inventory.domain.StockRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaStockRepository implements StockRepository {

    private final StockJpaRepository jpa;

    JpaStockRepository(StockJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Stock> findBySku(String sku) {
        return jpa.findById(sku).map(entity -> new Stock(entity.getSku(), entity.getQuantity()));
    }

    @Override
    public void save(Stock stock) {
        StockEntity entity =
                jpa.findById(stock.sku()).orElseGet(() -> new StockEntity(stock.sku(), 0));
        entity.setQuantity(stock.quantity());
        jpa.save(entity);
    }
}
