package com.example.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(ShopApplication.class);

    @Test
    @DisplayName("モジュール構造が規約を満たしている")
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    @DisplayName("検出されたモジュールを出力する")
    void printsModuleStructure() {
        modules.forEach(System.out::println);
    }
}
