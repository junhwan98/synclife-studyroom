package com.synclife.studyroom.room.dto;

import java.time.Instant;
import java.util.List;

public record RoomAvailabilityResponse(
        Long roomId,
        String name,
        String location,
        int capacity,
        List<ReservationWindow> reservations,
        List<FreeSlot> freeSlots
) {
    public record ReservationWindow(Long id, Long userId, Instant startAt, Instant endAt) {}
    public record FreeSlot(Instant startAt, Instant endAt) {}
}
