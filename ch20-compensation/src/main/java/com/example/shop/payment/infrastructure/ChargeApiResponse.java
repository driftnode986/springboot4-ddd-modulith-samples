package com.example.shop.payment.infrastructure;

/**
 * 決済代行会社が返す形。
 *
 * <p>{@code status} は相手が決めた文字列で、支払いの語彙ではない。 この型を支払いの内側へ渡してはいけない。
 */
record ChargeApiResponse(String id, String status) {}
