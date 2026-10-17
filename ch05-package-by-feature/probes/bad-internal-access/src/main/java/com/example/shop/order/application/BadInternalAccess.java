package com.example.shop.order.application;

import com.example.shop.inventory.domain.Stock;

class BadInternalAccess {
    void touch(Stock stock) {
    }
}
