package com.synclife.studyroom.reservation.dto;

import java.time.Instant;

public record ReservationResponse(Long id, Long roomId, Long userId, Instant startAt, Instant endAt) {}
