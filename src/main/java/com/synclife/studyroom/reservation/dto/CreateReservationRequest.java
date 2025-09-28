package com.synclife.studyroom.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record CreateReservationRequest(
        @NotNull Long roomId,
        @NotNull Instant startAt,
        @NotNull Instant endAt
) {}
