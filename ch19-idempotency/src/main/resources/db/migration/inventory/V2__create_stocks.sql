CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.stocks (
    sku      VARCHAR(64) NOT NULL,
    quantity INTEGER     NOT NULL,
    version  BIGINT      NOT NULL,
    CONSTRAINT pk_stocks PRIMARY KEY (sku)
);

-- 受け取り済みのイベントを記録する。主キーがそのまま二重処理の防波堤になる。
CREATE TABLE inventory.handled_events (
    event_id   VARCHAR(64) NOT NULL,
    handler    VARCHAR(120) NOT NULL,
    handled_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_handled_events PRIMARY KEY (event_id, handler)
);
