package com.example.shop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 内部コレクションの返し方による違いを固定する確認用のテスト。
 * 本文の説明が実際の挙動と食い違わないようにするために置いている。
 */
class CollectionLeakProbeTest {

    @Test
    @DisplayName("そのまま返すと、呼び出し側の追加が内部に通る")
    void rawListLeaks() {
        List<String> inside = new ArrayList<>();
        inside.add("A");

        inside.add("B"); // 呼び出し側が同じ参照を持っている状態

        assertThat(inside).hasSize(2);
    }

    @Test
    @DisplayName("unmodifiableList は書けないが、あとからの内部変更が見える")
    void unmodifiableListIsALiveView() {
        List<String> inside = new ArrayList<>();
        inside.add("A");
        List<String> view = Collections.unmodifiableList(inside);

        assertThatThrownBy(() -> view.add("B"))
                .isInstanceOf(UnsupportedOperationException.class);

        inside.add("C");

        assertThat(view).hasSize(2); // 内部の追加がそのまま見えている
    }

    @Test
    @DisplayName("List.copyOf はその時点の複製なので、あとからの内部変更が見えない")
    void copyOfIsASnapshot() {
        List<String> inside = new ArrayList<>();
        inside.add("A");
        List<String> copy = List.copyOf(inside);

        assertThatThrownBy(() -> copy.add("B"))
                .isInstanceOf(UnsupportedOperationException.class);

        inside.add("C");

        assertThat(copy).hasSize(1); // 複製した時点のまま
        assertThat(inside).hasSize(2);
    }
}
