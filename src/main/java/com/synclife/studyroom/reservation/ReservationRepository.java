package com.synclife.studyroom.reservation;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

   // 특정 구간과 겹치는 모든 예약 (모든 방 대상), 방-시작시간 정렬
    @Query("""
        select r from Reservation r
        where r.startAt < :end and r.endAt > :start
        order by r.room.id asc, r.startAt asc
    """)
    List<Reservation> findAllOverlapping(@Param("start") Instant start, @Param("end") Instant end);
}
