package com.synclife.studyroom.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrencyReservationTest {

    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void reset() {
        jdbc.update("DELETE FROM reservations");
        jdbc.update("DELETE FROM rooms");
        jdbc.update("INSERT INTO rooms(name, location, capacity) VALUES ('A','1F',4)");
    }

    @Test
    void only_one_success_on_overlap() throws Exception {
        Long roomId = jdbc.queryForObject("SELECT id FROM rooms LIMIT 1", Long.class);
        Instant s = Instant.parse("2025-09-26T09:30:00Z");
        Instant e = Instant.parse("2025-09-26T10:00:00Z");

        int N = 10;
        ExecutorService pool = Executors.newFixedThreadPool(N);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(N);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < N; i++) {
            final long uid = i + 1;
            pool.submit(() -> {
                try {
                    start.await();
                    jdbc.update("""
                        INSERT INTO reservations(room_id, user_id, start_at, end_at)
                        VALUES (?, ?, ?, ?)
                    """, roomId, uid, Timestamp.from(s), Timestamp.from(e));
                    success.incrementAndGet();
                } catch (DataAccessException ignore) {
                    // EXCLUDE 충돌 (SQLSTATE 23P01)
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM reservations WHERE room_id = ?", Integer.class, roomId);

        assertThat(count).isEqualTo(1);
        assertThat(success.get()).isEqualTo(1);
    }
}
