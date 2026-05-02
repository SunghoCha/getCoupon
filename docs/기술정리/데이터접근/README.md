# 데이터 접근 기술정리

## 목적

Repository, JPA, Querydsl, 페이징 조회, 인덱스 튜닝처럼 데이터 조회/저장 경계에서 반복되는 판단을 기록한다.

이 문서는 API 계약 문서가 아니다. 실제 endpoint의 요청/응답 shape는 각 도메인의 API 문서를 우선한다.

내부 구현은 코드와 테스트를 단일 출처로 둔다. 이 문서는 코드만 보면 놓치기 쉬운 선택 기준, 오용 위험, 리뷰 관점을 남긴다.

## 문서 목록

- [Querydsl 쿼리 패턴](쿼리패턴/README.md)

## Repository 패키지 기준

Spring Data repository는 도메인별 `adapter.persistence` 패키지에 둔다.

application service는 해당 Spring Data repository를 직접 주입한다. `application.required` 아래에 repository port를 따로 두지 않는다.

이 기준은 Spring Data repository와 custom implementation의 패키지·이름 규칙을 우선하기 위한 선택이다. 외부 시스템 교체, 복수 persistence adapter, 도메인별 adapter 분리가 실제 요구가 되면 application port와 adapter wrapper 도입을 다시 검토한다.

## 포함 범위

- Spring Data repository method와 custom query 선택 기준
- Querydsl 동적 조건 작성 패턴
- `Page`, `Slice`, offset, cursor 같은 조회 단위 구현 기준
- fetch join, N+1, 정렬 기준처럼 조회 코드에 직접 영향을 주는 판단
- 실제 병목이나 조회 요구가 생긴 뒤의 인덱스 설계와 튜닝 기록

## 비범위

- 외부 API 요청/응답 계약
- 도메인 규칙
- 코드와 같은 내용을 반복하는 구현 설명
- 실제 사례 없이 미리 적는 인덱스 목록

## 추가 기준

새 문서는 같은 실수를 반복할 위험이 있거나, 코드 리뷰 때 반복해서 판단해야 하는 기준이 생겼을 때 추가한다.

인덱스 튜닝 문서는 실제 조회 쿼리, 정렬 기준, 예상 데이터 규모, 실행 계획, 선택한 인덱스와 보류한 인덱스가 함께 있을 때 작성한다.
