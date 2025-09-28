package com.synclife.studyroom.room.dto;

import com.synclife.studyroom.room.domain.Room;

public record RoomResponse(Long id, String name, String location, int capacity) {
    public static RoomResponse from(Room r) {
        return new RoomResponse(r.getId(), r.getName(), r.getLocation(), r.getCapacity());
    }
}
