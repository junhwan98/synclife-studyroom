package com.synclife.studyroom.reservation;

import com.synclife.studyroom.auth.AuthContext;
import com.synclife.studyroom.auth.AuthFilter;
import com.synclife.studyroom.reservation.dto.CreateReservationRequest;
import com.synclife.studyroom.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservations", description = "예약 생성/취소")
@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "예약 생성 (USER)",
            description = "사용자 토큰: `user-token-<id>` (예: `user-token-7`)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ReservationResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "id": 1, "roomId": 2, "userId": 7,
                  "startAt": "2025-09-26T09:30:00Z",
                  "endAt":   "2025-09-26T10:00:00Z" }
                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 시간 범위",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"BAD_REQUEST", "message":"startAt < endAt (UTC)" }
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
            { "code":"FORBIDDEN", "message":"예약 생성은 사용자만 가능합니다." }
            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "room not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"NOT_FOUND", "message":"room not found" }
                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "OVERLAP (겹치는 시간대)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"OVERLAP", "message":"요청 시간대가 기존 예약과 겹칩니다." }
                """
                            )
                    )
            )
    })
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateReservationRequest.class),
                            examples = @ExampleObject(
                                    name = "기본 예시",
                                    value = """
                        { "roomId": 1,
                          "startAt": "2025-09-26T09:00:00Z",
                          "endAt":   "2025-09-26T10:00:00Z" }
                        """
                            )
                    )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateReservationRequest req,
            HttpServletRequest http
    ) {
        AuthContext ctx = (AuthContext) http.getAttribute(AuthFilter.ATTR);
        return reservationService.create(req, ctx);
    }

    @Operation(
            summary = "예약 취소 (OWNER or ADMIN)",
            description = "관리자: `admin-token`, 또는 예약자 본인 토큰 필요",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 완료",
                    content = @Content(schema = @Schema(hidden = true)) // 👈 본문 없음 표시
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
            ),
            @ApiResponse(responseCode = "404", description = "reservation not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.synclife.studyroom.common.ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                { "code":"NOT_FOUND", "message":"reservation not found" }
                """
                            )
                    )
            )
    })
    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @Parameter(description = "예약 ID", example = "123")
            @PathVariable Long id,
            HttpServletRequest http
    ) {
        AuthContext ctx = (AuthContext) http.getAttribute(AuthFilter.ATTR);
        reservationService.cancel(id, ctx);
    }
}
