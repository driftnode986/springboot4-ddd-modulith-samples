package com.example.shop.inventory.infrastructure;

import com.example.shop.inventory.domain.HandledEvents;
import java.sql.Timestamp;
import java.time.Clock;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 受け取り済みのイベントを表に記録する。
 *
 * <p>「すでにあるか」を先に読まない。読んでから書くまでの隙間に同じイベントが
 * 割り込むと、どちらの処理も「まだ無い」と判断してしまう。挿入を試みて、
 * 主キーの重複で拒まれたかどうかで判断する。
 */
@Repository
class JdbcHandledEvents implements HandledEvents {

    private final JdbcTemplate jdbc;
    private final Clock clock;

    JdbcHandledEvents(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public boolean recordIfFirst(String eventId, String handler) {
        try {
            jdbc.update(
                    "INSERT INTO inventory.handled_events (event_id, handler, handled_at) VALUES (?, ?, ?)",
                    eventId,
                    handler,
                    Timestamp.from(clock.instant()));
            return true;
        } catch (DuplicateKeyException alreadyHandled) {
            return false;
        }
    }
}
