package com.synclife.studyroom.room.dto;

public record RoomResponse(Long id, String name, String location, int capacity) {
    public static RoomResponse from(com.synclife.studyroom.room.Room r) {
        return new RoomResponse(r.getId(), r.getName(), r.getLocation(), r.getCapacity());
    }
}
