package com.example.shop.catalog.application;

import com.example.shop.inventory.spi.StockReservation;
import org.springframework.stereotype.Service;

/**
 * 既存の違反を再現するために置いてある。
 * catalog は依存先を宣言していないのに inventory を参照している。
 * 第12章で「既知の違反」として許容リストに載せ、あとで直す対象にする。
 */
@Service
public class CatalogAvailabilityService {

    private final StockReservation reservations;

    public CatalogAvailabilityService(StockReservation reservations) {
        this.reservations = reservations;
    }
}
