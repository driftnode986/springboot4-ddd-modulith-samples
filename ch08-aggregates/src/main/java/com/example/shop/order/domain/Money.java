package com.example.shop.order.domain;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("金額と通貨は必須です");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("金額は 0 以上です: " + amount);
        }
    }

    public static Money yen(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("JPY"));
    }

    public Money plus(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "通貨が違う金額は足せません: " + currency + " と " + other.currency);
        }
        return new Money(amount.add(other.amount), currency);
    }
}
