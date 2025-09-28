package com.synclife.studyroom.auth;

public record AuthContext(Role role, Long userId) {
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isOwner(Long targetUserId) { return userId != null && userId.equals(targetUserId); }
}
