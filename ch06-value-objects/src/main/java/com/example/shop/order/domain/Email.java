package com.example.shop.order.domain;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("メールアドレスの形式ではありません: " + value);
        }
    }
}
