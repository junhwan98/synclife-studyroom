package com.synclife.studyroom.room.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateRoomRequest(
        @NotBlank String name,
        @NotBlank String location,
        @Min(1) int capacity
) {}
