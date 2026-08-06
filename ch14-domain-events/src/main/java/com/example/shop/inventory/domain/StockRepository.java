package com.example.shop.inventory.domain;

import java.util.Optional;

/** 在庫の出し入れ口。実装は infrastructure に置く。 */
public interface StockRepository {

    Optional<Stock> findBySku(String sku);

    void save(Stock stock);
}
