package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MutablePitfallTest {

    // #region leaky
    record Leaky(List<String> tags) {}
    // #endregion

    // #region safe
    record Safe(List<String> tags) {
        Safe {
            tags = List.copyOf(tags);
        }
    }
    // #endregion

    @Test
    @DisplayName("リストをそのまま持つと、外から中身が変わる（確認用）")
    void 実測() {
        // #region leaky-source
        List<String> source = new ArrayList<>(List.of("gift"));
        Leaky leaky = new Leaky(source);

        source.add("express");
        // #endregion
        System.out.println("[probe] 渡した側から追加後 = " + leaky.tags());
        assertThat(leaky.tags()).containsExactly("gift", "express");

        // #region leaky-got
        List<String> got = leaky.tags();
        got.add("fragile");
        // #endregion
        System.out.println("[probe] 取り出した側から追加後 = " + leaky.tags());
        assertThat(leaky.tags())
                .containsExactly("gift", "express", "fragile");

        Leaky a = new Leaky(new ArrayList<>(List.of("a")));
        Leaky b = new Leaky(new ArrayList<>(List.of("a")));
        System.out.println("[probe] Leaky equals = " + a.equals(b));
        assertThat(a).isEqualTo(b);
    }

    // #region safe-tests
    @Test
    @DisplayName("渡したリストを変更しても影響しない")
    void 渡したリストを変更しても影響しない() {
        List<String> source = new ArrayList<>(List.of("gift"));
        Safe safe = new Safe(source);

        source.add("express");

        assertThat(safe.tags()).containsExactly("gift");
    }

    @Test
    @DisplayName("取り出したリストは変更できない")
    void 取り出したリストは変更できない() {
        Safe safe = new Safe(new ArrayList<>(List.of("gift")));

        assertThatThrownBy(() -> safe.tags().add("fragile"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
    // #endregion
}
