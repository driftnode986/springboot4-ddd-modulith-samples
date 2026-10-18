-- Spring Modulith のイベント公開レジストリが使う表。
-- 自動では作られないため、業務の表と同じように移行スクリプトで管理する。
-- 索引は、未完了のイベントを起動時と監視で引くためのもの。
-- #region event-publication
CREATE TABLE event_publication (
    id                     UUID         NOT NULL,
    listener_id            VARCHAR(512) NOT NULL,
    event_type             VARCHAR(512) NOT NULL,
    serialized_event       TEXT         NOT NULL,
    publication_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date        TIMESTAMP WITH TIME ZONE,
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    completion_attempts    INTEGER      NOT NULL DEFAULT 0,
    status                 VARCHAR(16)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_event_publication_incomplete
    ON event_publication (completion_date, publication_date);
-- #endregion
