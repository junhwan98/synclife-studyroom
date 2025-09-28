package com.synclife.studyroom.room;

import com.synclife.studyroom.auth.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {
    private final RoomRepository rooms;
    public RoomService(RoomRepository rooms) { this.rooms = rooms; }

    @Transactional
    public Room create(String name, String location, int capacity, AuthContext ctx) {
        if (ctx == null) throw new SecurityException("UNAUTHORIZED");
        if (!ctx.isAdmin()) throw new SecurityException("FORBIDDEN");
        return rooms.save(new Room(name, location, capacity));
    }
}
