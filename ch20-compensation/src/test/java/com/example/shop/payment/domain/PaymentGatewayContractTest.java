package com.example.shop.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 差し替えられることそのものを確かめる。
 *
 * <p>ここには HTTP も決済代行会社の名前も出てこない。 支払いの内側が知っているのは「請求すると結果が返る」ところまでで、 それが本物の通信かどうかを気にしないでいられる。
 */
class PaymentGatewayContractTest {

    @Test
    @DisplayName("成立を返す相手なら、支払いは成立として受け取れる")
    void readsSettled() {
        PaymentGateway gateway = new StubPaymentGateway(ChargeResult.SETTLED);

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.SETTLED);
    }

    @Test
    @DisplayName("結果が確定しない相手なら、そのまま確定しないものとして受け取れる")
    void readsUnknown() {
        PaymentGateway gateway = new StubPaymentGateway(ChargeResult.UNKNOWN);

        assertThat(gateway.charge(UUID.randomUUID(), 12_000)).isEqualTo(ChargeResult.UNKNOWN);
    }
}
