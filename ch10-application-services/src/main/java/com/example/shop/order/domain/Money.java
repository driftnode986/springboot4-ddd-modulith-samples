package com.example.shop.order.domain;

// #region money
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException(
                    "金額と通貨は必須です");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(
                    "金額は 0 以上です: " + amount);
        }
        // 桁数をその通貨の単位に揃える。
        // DB から戻った 1500.00 とコードで作った 1500 を
        // 同じものとして扱うため。
        amount = amount.setScale(
                currency.getDefaultFractionDigits(),
                RoundingMode.UNNECESSARY);
    }
    // #endregion

    // #region money-rest
    public static Money yen(long amount) {
        return new Money(
                BigDecimal.valueOf(amount),
                Currency.getInstance("JPY"));
    }

    public Money plus(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "通貨が違う金額は足せません: " + currency
                            + " と " + other.currency);
        }
        return new Money(amount.add(other.amount), currency);
    }
}
// #endregion
