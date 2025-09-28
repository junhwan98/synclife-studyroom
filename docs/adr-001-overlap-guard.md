# ADR-001: 예약 겹침 방지 전략

## Context
동일 방에서 겹치는 시간대 예약은 절대 허용되면 안 됨. 동시성 상황에서도 무결성 보장 필요.

## Decision
PostgreSQL `tstzrange + EXCLUDE USING gist` 채택. 제약명 `reservations_no_overlap`.
서비스는 제약 위반(제약명 우선, SQLSTATE 23P01 보조)을 409(CONFLICT, code=OVERLAP)로 매핑.

## Alternatives
- 앱 레벨 검사 + 비관/낙관락: 레이스 윈도우 존재, 복잡도↑
- 고유 인덱스(별도 버킷화): 시간 구간 특성상 부적합
- 큐/직렬화: 지연/복잡도↑

## Consequences
- 장점: DB가 1차 방어선(정확), 코드 단순, 테스트 용이
- 단점: PG 의존성, GIST 인덱스 유지 비용
- 테스트: 병렬 10건 중 1건 성공 테스트로 검증