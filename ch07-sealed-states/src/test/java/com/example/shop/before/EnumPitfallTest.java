package com.example.shop.before;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 第7章の冒頭で示す、状態を enum で持った場合の挙動を確認するためのテスト。
 * 本文で説明する「受付済でも決済の識別子を読めてしまう」ことを固定する。
 */
class EnumPitfallTest {

    @Test
    @DisplayName("受付済でも決済の識別子を読めてしまう")
    void 受付済でも決済の識別子を読めてしまう() {
        OrderWithEnum order = new OrderWithEnum();

        System.out.println("status    = " + order.status());
        System.out.println("paymentId = " + order.paymentId());

        assertThat(order.status()).isEqualTo(OrderWithEnum.Status.ACCEPTED);
        assertThat(order.paymentId()).isNull();
    }

    @Test
    @DisplayName("読んだ値を使うと離れた場所で落ちる")
    void 読んだ値を使うと離れた場所で落ちる() {
        OrderWithEnum order = new OrderWithEnum();

        assertThatThrownBy(() -> order.paymentId().length())
                .isInstanceOf(NullPointerException.class);
    }
}
