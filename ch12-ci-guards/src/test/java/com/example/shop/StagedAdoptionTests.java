package com.example.shop;

// #region staged-head
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;

class StagedAdoptionTests {

    /** 既知の違反。直したらこの一覧から消す。 */
    static final List<String> KNOWN = List.of(
            "Module 'catalog' depends on named interface(s) "
                    + "'inventory :: spi'");

    @Test
    @DisplayName("既知の違反を除いて、新しい違反がない")
    void hasNoNewViolations() {
        Violations violations = ApplicationModules
                .of(ShopApplication.class)
                .detectViolations()
                .filter(violation -> KNOWN.stream().noneMatch(
                        violation::hasMessageContaining));

        violations.throwIfPresent();
    }
// #endregion

    // #region no-stale-entries
    @Test
    @DisplayName("許容リストは実在する違反だけを持つ")
    void allowListHasNoStaleEntries() {
        Violations all = ApplicationModules
                .of(ShopApplication.class)
                .detectViolations();

        assertThat(KNOWN).allSatisfy(known ->
                assertThat(all.getMessages()).anySatisfy(
                        message -> assertThat(message)
                                .contains(known)));
    }
    // #endregion
// #region staged-end
}
// #endregion
