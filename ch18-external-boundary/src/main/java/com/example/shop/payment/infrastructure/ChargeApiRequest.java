package com.example.shop.payment.infrastructure;

/** 決済代行会社が受け取る形。項目名も単位も相手の都合で決まっている。 */
record ChargeApiRequest(String reference, long amount, String currency) {}
