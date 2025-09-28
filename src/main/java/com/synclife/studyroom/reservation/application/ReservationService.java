package com.synclife.studyroom.reservation.application;

import com.synclife.studyroom.auth.AuthContext;
import com.synclife.studyroom.reservation.domain.Reservation;
import com.synclife.studyroom.reservation.domain.ReservationRepository;
import com.synclife.studyroom.room.domain.Room;
import com.synclife.studyroom.room.domain.RoomRepository;
import com.synclife.studyroom.reservation.dto.CreateReservationRequest;
import com.synclife.studyroom.reservation.dto.ReservationResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.synclife.studyroom.room.dto.RoomAvailabilityResponse;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class ReservationService {
    private final ReservationRepository reservations;
    private final RoomRepository rooms;

    public ReservationService(ReservationRepository reservations, RoomRepository rooms) {
        this.reservations = reservations; this.rooms = rooms;
    }

    @Transactional
    public ReservationResponse create(CreateReservationRequest req, AuthContext ctx) {
        if (ctx == null) throw new SecurityException("UNAUTHORIZED");
        if (ctx.userId() == null) throw new SecurityException("FORBIDDEN_RESERVATION_REQUIRES_USER");

        validateRange(req.startAt(), req.endAt());

        Room room = rooms.findById(req.roomId())
                .orElseThrow(() -> new NoSuchElementException("room not found"));

        try {
            var saved = reservations.save(new Reservation(room, ctx.userId(), req.startAt(), req.endAt()));
            return new ReservationResponse(saved.getId(), room.getId(), ctx.userId(), saved.getStartAt(), saved.getEndAt());
        } catch (DataIntegrityViolationException e) {
            if (isExclusionViolation(e)) throw new IllegalStateException("CONFLICT_OVERLAP");
            throw e;
        }
    }

    @Transactional
    public void cancel(Long reservationId, AuthContext ctx) {
        if (ctx == null) throw new SecurityException("UNAUTHORIZED");
        var resv = reservations.findById(reservationId).orElseThrow(() -> new NoSuchElementException("reservation not found"));
        if (!(ctx.isAdmin() || ctx.isOwner(resv.getUserId()))) throw new SecurityException("FORBIDDEN");
        reservations.delete(resv);
    }

    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponse> availability(LocalDate dateUtc) {
        // 1) 날짜 구간을 [00:00Z, +1일 00:00Z)로 잡기
        Instant dayStart = dateUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd   = dateUtc.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // 2) 방 전체와, 해당 구간과 겹치는 예약 전체 조회
        var allRooms = rooms.findAll();
        var allResvs = reservations.findAllOverlapping(dayStart, dayEnd);

        // 3) 방별로 예약 묶기
        Map<Long, List<Reservation>> byRoom = allResvs.stream()
                .collect(Collectors.groupingBy(r -> r.getRoom().getId(), LinkedHashMap::new, Collectors.toList()));

        // 4) 방별 free slot 계산
        List<RoomAvailabilityResponse> result = new ArrayList<>();
        for (Room room : allRooms) {
            List<Reservation> list = byRoom.getOrDefault(room.getId(), List.of());

            // 빈 구간 계산: 커서를 dayStart로 두고 예약 사이의 간격을 수집
            Instant cursor = dayStart;
            List<RoomAvailabilityResponse.ReservationWindow> resvDtos = new ArrayList<>();
            List<RoomAvailabilityResponse.FreeSlot> free = new ArrayList<>();

            for (Reservation r : list) {
                Instant s = r.getStartAt();
                Instant e = r.getEndAt();
                if (s.isAfter(cursor)) {
                    free.add(new RoomAvailabilityResponse.FreeSlot(cursor, s));
                }
                cursor = e.isAfter(cursor) ? e : cursor; // 반개구간이므로 같은 시각이면 이동 없음
                resvDtos.add(new RoomAvailabilityResponse.ReservationWindow(
                        r.getId(), r.getUserId(), r.getStartAt(), r.getEndAt()
                ));
            }
            if (cursor.isBefore(dayEnd)) {
                free.add(new RoomAvailabilityResponse.FreeSlot(cursor, dayEnd));
            }

            result.add(new RoomAvailabilityResponse(
                    room.getId(), room.getName(), room.getLocation(), room.getCapacity(),
                    resvDtos, free
            ));
        }
        // 방 id 오름차순 리턴
        result.sort(Comparator.comparing(RoomAvailabilityResponse::roomId));
        return result;
    }

    private void validateRange(Instant start, Instant end) {
        if (start == null || end == null || !start.isBefore(end))
            throw new IllegalArgumentException("startAt < endAt");
    }

    private boolean isExclusionViolation(DataIntegrityViolationException e) {
        Throwable root = e.getMostSpecificCause();

        return root != null && root.getMessage() != null && root.getMessage().contains("exclusion constraint")
                || (root instanceof org.postgresql.util.PSQLException p && "23P01".equals(p.getSQLState()));
    }
}
