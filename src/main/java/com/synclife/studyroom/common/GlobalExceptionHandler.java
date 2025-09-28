package com.synclife.studyroom.common;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> sec(SecurityException e) {
        return switch (e.getMessage()) {
            case "UNAUTHORIZED" -> ResponseEntity.status(401)
                    .body(new ErrorResponse("UNAUTHORIZED","로그인이 필요합니다."));
            case "FORBIDDEN" -> ResponseEntity.status(403)
                    .body(new ErrorResponse("FORBIDDEN","권한이 없습니다."));
            case "FORBIDDEN_RESERVATION_REQUIRES_USER" -> ResponseEntity.status(403)
                    .body(new ErrorResponse("FORBIDDEN","예약 생성은 사용자만 가능합니다."));
            default -> ResponseEntity.status(403)
                    .body(new ErrorResponse("FORBIDDEN","권한이 없습니다."));
        };
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> bad(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> conflict(IllegalStateException e) {
        if ("CONFLICT_OVERLAP".equals(e.getMessage()))
            return ResponseEntity.status(409).body(new ErrorResponse("OVERLAP","요청 시간대가 기존 예약과 겹칩니다."));
        return ResponseEntity.status(409).body(new ErrorResponse("CONFLICT", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }
}
