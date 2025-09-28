package com.synclife.studyroom.room;

import com.synclife.studyroom.api.RoomsApi;
import com.synclife.studyroom.auth.AuthContext;
import com.synclife.studyroom.auth.AuthFilter;
import com.synclife.studyroom.room.dto.CreateRoomRequest;
import com.synclife.studyroom.room.dto.RoomAvailabilityResponse;
import com.synclife.studyroom.room.dto.RoomResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Rooms", description = "회의실 등록 / 가용성 조회")
@RestController
@RequiredArgsConstructor
public class RoomController implements RoomsApi {

    private final RoomService roomService;
    private final com.synclife.studyroom.reservation.ReservationService reservationService;

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(
            @Valid @RequestBody CreateRoomRequest req,
            HttpServletRequest http
    ) {
        AuthContext ctx = (AuthContext) http.getAttribute(AuthFilter.ATTR);
        Room saved = roomService.create(req.name(), req.location(), req.capacity(), ctx);
        return RoomResponse.from(saved);
    }

    @GetMapping("/rooms")
    public List<RoomAvailabilityResponse> availability(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reservationService.availability(date);
    }
}
