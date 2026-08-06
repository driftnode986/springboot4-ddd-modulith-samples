package com.example.shop;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * 第11章までの形。verify() は違反が1件でもあれば落ちる。
 * この章では既存の違反を1件持つ状態を作るため、段階導入の StagedAdoptionTests に置き換えた。
 */
class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(ShopApplication.class);

    @Test
    @Disabled("既知の違反を1件持っているため。第12章「既存のプロジェクトに後から入れる」を参照")
    @DisplayName("モジュール構造が規約を満たしている")
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    @DisplayName("検出されたモジュールと公開面を出力する")
    void printsModuleStructure() {
        modules.forEach(System.out::println);
    }
}
