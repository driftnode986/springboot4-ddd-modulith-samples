package com.example.shop.inventory.domain;

/**
 * 受け取り済みのイベントの記録。
 *
 * <p>「もう処理したか」を尋ねるのではなく「記録できたか」を尋ねる。
 * 尋ねてから書くまでの隙間に同じイベントが割り込むと、どちらも
 * 「まだ処理していない」と判断してしまうため。
 */
public interface HandledEvents {

    /**
     * 受け取ったことを記録する。
     *
     * @return 記録できたら true。すでに記録済みなら false
     */
    boolean recordIfFirst(String eventId, String handler);
}
