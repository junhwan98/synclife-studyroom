package com.synclife.studyroom.room;

import com.synclife.studyroom.auth.AuthContext;
import com.synclife.studyroom.auth.AuthFilter;
import com.synclife.studyroom.room.dto.RoomAvailabilityResponse;
import com.synclife.studyroom.room.dto.RoomResponse;
import com.synclife.studyroom.room.dto.CreateRoomRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class RoomController {

    private final RoomService roomService;
    private final com.synclife.studyroom.reservation.ReservationService reservationService;

    @Operation(
            summary = "회의실 등록 (ADMIN)",
            description = "관리자 토큰 필요: `admin-token`",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RoomResponse.class),
                            examples = @ExampleObject(
                                    name = "Created",
                                    value = """
                { "id": 3, "name": "Z", "location": "B2", "capacity": 4 }
                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"BAD_REQUEST", "message":"capacity > 0" }
                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "UNAUTHORIZED",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"UNAUTHORIZED", "message":"로그인이 필요합니다." }
                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "FORBIDDEN",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"FORBIDDEN", "message":"권한이 없습니다." }
                """
                            )
                    )
            )
    })
    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(
            @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateRoomRequest.class),
                            examples = @ExampleObject(
                                    name = "예시",
                                    value = """
                        { "name":"A", "location":"1F", "capacity":4 }
                        """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateRoomRequest req,
            HttpServletRequest http
    ) {
        AuthContext ctx = (AuthContext) http.getAttribute(AuthFilter.ATTR);
        Room saved = roomService.create(req.name(), req.location(), req.capacity(), ctx);
        return RoomResponse.from(saved);
    }

    @Operation(
            summary = "가용성 조회",
            description = "`date`는 **UTC 날짜**입니다. 해당 일자 `[00:00Z, 24:00Z)`의 예약/빈 슬롯을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RoomAvailabilityResponse.class)),
                            examples = @ExampleObject(
                                    name = "Sample",
                                    value = """
                [
                  {
                    "roomId": 1,
                    "name": "A",
                    "location": "1F",
                    "capacity": 4,
                    "reservations": [
                      { "id": 10, "userId": 7, "startAt": "2025-09-26T09:00:00Z", "endAt": "2025-09-26T10:00:00Z" }
                    ],
                    "freeSlots": [
                      { "startAt": "2025-09-26T00:00:00Z", "endAt": "2025-09-26T09:00:00Z" },
                      { "startAt": "2025-09-26T10:00:00Z", "endAt": "2025-09-27T00:00:00Z" }
                    ]
                  }
                ]
                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"BAD_REQUEST", "message":"Failed to convert value of type 'String' to required type 'LocalDate'" }
                """
                            )
                    )
            )
    })
    @GetMapping("/rooms")
    public List<RoomAvailabilityResponse> availability(
            @Parameter(example = "2025-09-26", description = "UTC 기준 날짜 (YYYY-MM-DD)")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return reservationService.availability(date);
    }
}
