# ADR-002: RBAC 간소화 방식

## Context
요건: ADMIN만 방 생성, USER는 자기 예약만 취소. 과제 범위 내 간단 인증/인가 필요.

## Decision
커스텀 필터가 `Authorization: Bearer ...` 해석
- `admin-token` → ADMIN
- `user-token-<id>` → USER, userId 부여
  Swagger 문서 경로는 필터 우회.

## Alternatives
- Spring Security + JWT: 과제 범위 대비 과도
- 세션/쿠키: 상태 관리 필요

## Consequences
- 장점: 단순/명확, 데모 빠름
- 단점: 실서비스 부적합(보안/만료/회수 없음)
- 마이그레이션: 운영 전환 시 Security/JWT로 대체 가능