package com.synclife.studyroom.api;

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

import jakarta.servlet.http.HttpServletRequest;

public interface ReservationsApi {

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
                                    { "code":"BAD_REQUEST", "message":"startAt < endAt" }
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
    ReservationResponse create(
            @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateReservationRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                    { "roomId": 1,
                                      "startAt": "2025-09-26T09:00:00Z",
                                      "endAt":   "2025-09-26T10:00:00Z" }
                                    """
                            )
                    )
            )
            CreateReservationRequest req,
            HttpServletRequest http
    );

    @Operation(
            summary = "예약 취소 (OWNER or ADMIN)",
            description = "관리자: `admin-token`, 또는 예약자 본인 토큰 필요",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 완료",
                    content = @Content(schema = @Schema(hidden = true))
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
    void cancel(
            @Parameter(description = "예약 ID", example = "123")
            Long id,
            HttpServletRequest http
    );
}
