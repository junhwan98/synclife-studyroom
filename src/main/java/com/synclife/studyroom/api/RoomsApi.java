package com.synclife.studyroom.api;

import com.synclife.studyroom.room.dto.CreateRoomRequest;
import com.synclife.studyroom.room.dto.RoomAvailabilityResponse;
import com.synclife.studyroom.room.dto.RoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

public interface RoomsApi {

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
    RoomResponse create(
            @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateRoomRequest.class),
                            examples = @ExampleObject(
                                    value = """
                  { "name":"A", "location":"1F", "capacity":4 }
                  """
                            )
                    )
            )
            CreateRoomRequest req,
            HttpServletRequest http
    );

    @Operation(
            summary = "가용성 조회",
            description = "`date`는 **UTC 날짜**입니다. 해당 일자 `[00:00Z, 24:00Z)`의 예약/빈 슬롯을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RoomAvailabilityResponse.class)),
                            examples = @ExampleObject(
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
    List<RoomAvailabilityResponse> availability(
            @Parameter(example = "2025-09-26", description = "UTC 기준 날짜 (YYYY-MM-DD)")
            LocalDate date
    );
}
