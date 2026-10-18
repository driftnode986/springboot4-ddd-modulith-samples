package com.example.shop.catalog.application;

import com.example.shop.inventory.spi.StockReservation;
import org.springframework.stereotype.Service;

@Service
public class CatalogAvailabilityService {

    private final StockReservation reservations;

    public CatalogAvailabilityService(
            StockReservation reservations) {
        this.reservations = reservations;
    }
}
