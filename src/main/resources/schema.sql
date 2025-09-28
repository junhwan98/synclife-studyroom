DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS rooms;

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE rooms (
  id         BIGSERIAL PRIMARY KEY,
  name       TEXT        NOT NULL,
  location   TEXT        NOT NULL,
  capacity   INT         NOT NULL CHECK (capacity > 0),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_rooms_name ON rooms (name);

CREATE TABLE reservations (
  id         BIGSERIAL PRIMARY KEY,
  room_id    BIGINT      NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
  user_id    BIGINT      NOT NULL,
  start_at   TIMESTAMPTZ NOT NULL,
  end_at     TIMESTAMPTZ NOT NULL,
  period     tstzrange   GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_time_range CHECK (start_at < end_at),

  EXCLUDE USING gist (
    room_id WITH =,
    period  WITH &&
  )
);