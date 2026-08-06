package com.example.shop.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.example.shop.inventory.domain.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 記録による守りが無い場合に、戻しが何を起こすかを確かめる確認用のテスト。
 *
 * <p>引き当ては手元の数を超えられないため、二度実行すれば足りなくなって拒まれる。戻しにはその
 * 上限がない。二度実行すると、預かっていない在庫が手元にあることになる。
 */
class UnguardedReleaseProbeTest {

    @Test
    @DisplayName("守りが無ければ、戻しを 2 回実行すると在庫が実際より増える")
    void doubleReleaseInflatesQuantity() {
        Stock stock = new Stock("P-1", 10);
        stock.reserve(3);

        stock.release(3);
        stock.release(3);

        assertThat(stock.quantity()).isEqualTo(13);
    }

    @Test
    @DisplayName("引き当ては 2 回実行しても、手元の数を超えられないので拒まれる")
    void doubleReserveIsRejectedByTheUpperBound() {
        Stock stock = new Stock("P-1", 5);
        stock.reserve(3);

        assertThat(catchThrowable(() -> stock.reserve(3)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(stock.quantity()).isEqualTo(2);
    }
}
