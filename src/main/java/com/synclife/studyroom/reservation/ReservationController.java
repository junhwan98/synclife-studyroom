package com.synclife.studyroom.reservation;

import com.synclife.studyroom.api.ReservationsApi;
import com.synclife.studyroom.auth.AuthContext;
import com.synclife.studyroom.auth.AuthFilter;
import com.synclife.studyroom.reservation.dto.CreateReservationRequest;
import com.synclife.studyroom.reservation.dto.ReservationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservations", description = "예약 생성/취소")
@RestController
@RequiredArgsConstructor
public class ReservationController implements ReservationsApi {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest req,
            HttpServletRequest http
    ) {
        AuthContext ctx = (AuthContext) http.getAttribute(AuthFilter.ATTR);
        return reservationService.create(req, ctx);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable Long id,
            HttpServletRequest http
    ) {
        AuthContext ctx = (AuthContext) http.getAttribute(AuthFilter.ATTR);
        reservationService.cancel(id, ctx);
    }
}
