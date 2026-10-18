package com.example.shop.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.payment.domain.ChargeResult;
import com.example.shop.payment.domain.StubPaymentGateway;
import com.example.shop.payment.spi.PaymentFailed;
import com.example.shop.payment.spi.PaymentRequest.Reserved;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 請求の結果ごとに、発表するかどうかが分かれることを確かめる。 */
class RequestPaymentServiceTest {

    private final List<Object> events = new ArrayList<>();
    private final List<Reserved> reserved =
            List.of(new Reserved("P-1", 3),
                    new Reserved("P-2", 1));

    private RequestPaymentService serviceReturning(
            ChargeResult result) {
        return new RequestPaymentService(
                new StubPaymentGateway(result), events::add);
    }

    @Test
    @DisplayName("請求が成立したら、何も発表しない")
    void publishesNothingWhenSettled() {
        serviceReturning(ChargeResult.SETTLED)
                .request(UUID.randomUUID(), 12_000, reserved);

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("断られたら、戻す対象を載せて PaymentFailed を発表する")
    void publishesPaymentFailedWhenDeclined() {
        UUID orderId = UUID.randomUUID();

        serviceReturning(ChargeResult.DECLINED)
                .request(orderId, 12_000, reserved);

        assertThat(events).hasSize(1);
        PaymentFailed failed = (PaymentFailed) events.get(0);
        assertThat(failed.eventId()).isNotBlank();
        assertThat(failed.orderId())
                .isEqualTo(orderId.toString());
        assertThat(failed.reason()).isEqualTo("DECLINED");
        assertThat(failed.items()).containsExactly(
                new PaymentFailed.Item("P-1", 3),
                new PaymentFailed.Item("P-2", 1));
    }

    @Test
    @DisplayName("結果が確定しないときは、戻さずに例外で知らせる")
    void doesNotPublishWhenUnknown() {
        RequestPaymentService service =
                serviceReturning(ChargeResult.UNKNOWN);

        assertThatThrownBy(() -> service.request(
                        UUID.randomUUID(), 12_000, reserved))
                .isInstanceOf(
                        PaymentUndeterminedException.class);
        assertThat(events).isEmpty();
    }
}
