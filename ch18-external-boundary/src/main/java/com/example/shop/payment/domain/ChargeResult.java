package com.example.shop.payment.domain;

/**
 * 支払いの語彙で表した請求の結果。 外部サービスが返す数値コードは、この列挙のいずれかに翻訳してから内側へ渡す。 翻訳を行う腐敗防止層の実装は第18章で作る。
 */
public enum ChargeResult {
    /** 請求が成立した。 */
    SETTLED,
    /** 与信が通らず請求が成立しなかった。 */
    DECLINED,
    /** 相手側の障害により結果が確定していない。 */
    UNKNOWN
}
