package com.example.shop.inventory.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

interface StockJpaRepository extends JpaRepository<StockEntity, String> {
}
