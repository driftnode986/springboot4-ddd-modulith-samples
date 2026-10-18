@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"inventory :: spi", "payment :: spi", "shipping :: spi"})
package com.example.shop.order;
