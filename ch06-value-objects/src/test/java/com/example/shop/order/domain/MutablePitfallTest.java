package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MutablePitfallTest {

    record Leaky(List<String> tags) {}

    record Safe(List<String> tags) {
        Safe {
            tags = List.copyOf(tags);
        }
    }

    @Test
    void 実測() {
        List<String> source = new ArrayList<>(List.of("gift"));

        Leaky leaky = new Leaky(source);
        source.add("express");
        System.out.println("[probe] leaky 経由で見える値 = " + leaky.tags());

        List<String> got = leaky.tags();
        got.add("fragile");
        System.out.println("[probe] 取り出した側から追加後 = " + leaky.tags());

        List<String> source2 = new ArrayList<>(List.of("gift"));
        Safe safe = new Safe(source2);
        source2.add("express");
        System.out.println("[probe] safe 経由で見える値  = " + safe.tags());
        try {
            safe.tags().add("fragile");
        } catch (UnsupportedOperationException e) {
            System.out.println("[probe] safe への追加      = UnsupportedOperationException");
        }

        System.out.println("[probe] Leaky equals = "
                + new Leaky(new ArrayList<>(List.of("a"))).equals(new Leaky(new ArrayList<>(List.of("a")))));
    }

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
}
