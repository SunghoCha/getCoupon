# 회원 API

## 문서 메타
- 상태: DRAFT
- 마지막 수정 단계: 2단계 (API 초안 도출)
- 프론트 공유 상태: 공유 전
- 관련 문서:
  - [회원.md](회원.md)
  - [회원_산출물.md](회원_산출물.md)
  - 공통 규약 원본: [../참고자료/컨트롤러_구현_가이드.md](../참고자료/컨트롤러_구현_가이드.md)

---

**[전역 외부 계약]**

## 0. 범위

- **포함**: 회원 정보 수정, 회원 정지, 탈퇴, 관리자 탈퇴 처리
- **제외**:
  - **회원 가입** — OAuth2 콜백 기반이라 일반 REST API 계약과 성격이 다르다. 인증 어댑터 영역에서 다루며, 별도 문서(예: `회원_oauth.md`)로 추후 작성 예정.

---

## 1. 전역 계약

> 공통 규약의 원본은 [`참고자료/컨트롤러_구현_가이드.md`](../참고자료/컨트롤러_구현_가이드.md)다.
> 이 섹션에는 회원 도메인에서 필요한 **override만** 기록한다.

(현재까지 override 없음)

---

**[유스케이스별 계약]**

## 2. 유스케이스별 계약

각 유스케이스는 **A. 외부 계약** + **B. 백엔드 확인 사항** 두 블록을 가진다.
- A는 2단계(현재)에서 채운다.
- B는 4~8단계에 걸쳐 점진적으로 채운다.

---

### 컨트롤러 TDD 진행 대시보드

> 8단계 진행 현황. 도메인_개발_가이드 §8 "Phase 단위 진행" 기반.

**Phase 0. 기초 인프라 (공통)**
- [x] 의존성 추가 (`springdoc-openapi-starter-webmvc-ui`)
- [x] `ErrorResponse` / `ErrorCode` 정의
- [x] `@RestControllerAdvice` 기본 틀
- [x] `@CurrentUser` 애노테이션 + ArgumentResolver
- [x] `@WebMvcTest` 세팅 확인

**Phase 순서**: 탈퇴 → 회원 정보 수정 → 회원 정지 → 관리자 탈퇴 처리

| Phase | UC | 상태 |
|-------|----|------|
| 1 | 탈퇴 | ✅ 완료 (2026-04-24) |
| 2 | 회원 정보 수정 | ✅ 완료 (2026-04-24) |
| 3 | 회원 정지 | ✅ 완료 (2026-04-24) |
| 4 | 관리자 탈퇴 처리 | ✅ 완료 (2026-04-24) |
| 5 | 통합 스모크 + OpenAPI 검증 | ✅ 완료 (2026-04-25) |

각 UC의 Red/Green 세부 진행은 해당 UC B 블록에 기록한다.

**Phase 5. 통합 스모크 + OpenAPI 검증** (유스케이스 횡단)
- [x] 로컬 개발용 MySQL 환경 구성 (docker-compose + local profile) — 앱을 실제 DB로 기동해 수동 확인 가능
- [x] 통합 테스트 작성 — 대표 성공 경로 4건 (`@SpringBootTest` + Testcontainers + 실제 DB 왕복 + 상태 변화 검증)
- [x] OpenAPI 스펙 자동 검증 — 4개 엔드포인트 path 존재 확인 (`MemberControllerIntegrationTest#openapi_spec_includes_member_endpoints`)
- [x] 수동 확인 — `/v3/api-docs` 응답과 회원_api.md 외부 계약 일치 (HTTP 메서드·응답 코드 전체)
- [ ] 9단계 FINAL 승격 및 `#5 OpenAPI 동기화` 섹션 확정 (별도 단계)

---

### 회원 정보 수정

#### A. 외부 계약

**목적**: 회원이 자신의 정보를 수정한다 (현재 범위: nickname)

**호출 주체**: 로그인한 회원 (본인)

**엔드포인트**
- URL: `/api/v1/members/me`
- HTTP Method: PATCH

**요청**
- path: 없음 (`me`는 principal 기반 자기 참조)
- query: 없음
- body 초안:
  ```json
  { "nickname": "..." }
  ```
  현재 수정 가능 필드는 `nickname`. 향후 필드 추가 시 optional로 붙음.

**성공 응답**
- 상태 코드: `204 No Content`
- 응답 바디 초안: 없음

**실패 응답 (가설)** — 이 유스케이스 고유의 비즈니스 실패만
- 대상 회원 없음 → `404 Not Found` (MemberNotFoundException)
- 닉네임 중복 → `409 Conflict` (DuplicateNicknameException)
- 상태 전이 위반 (ACTIVE가 아닌 상태에서 수정 시도) → `409 Conflict` (MemberStatusException)

**인증/인가 경계**
- 호출 허용 대상: 로그인한 회원 본인
- 경계 규칙 메모: controller가 인증 principal에서 memberId를 추출. path에 id를 넣지 않으므로 impersonation 표면 자체가 없음 (산출물 §2.2 "본인 확인 인증 계층 전담"과 정합).

**멱등성 / no-op**
- 반복 호출 시 계약: 동일 닉네임 재요청도 성공 처리 (멱등 보장). 구현상 중복 검증은 스킵하되 도메인 호출은 수행하여 상태 검증은 항상 거친다.

**예시 요청/응답**
```http
PATCH /api/v1/members/me
Content-Type: application/json

{ "nickname": "새닉네임" }
```
```http
204 No Content
```

**미결 사항**
- nickname 필드 bean validation(길이 2~20, 허용 문자 패턴)을 컨트롤러 DTO에 추가할지 → 8단계 컨트롤러 TDD에서 확정

#### B. 백엔드 확인 사항

**상위 참조**: [회원_산출물.md](회원_산출물.md) / 회원 정보 수정

**컨트롤러 TDD Phase 체크리스트** (Phase 2)
- [x] `MemberControllerApi.changeNickname` 인터페이스 선언 (Swagger 애노테이션 포함)
- [x] Red: URL 매핑 / body 바인딩 테스트
- [x] Green: 컨트롤러 구현 (`@CurrentUser` + `@RequestBody`)
- [x] Red/Green: 회원 없음 → 404 예외 매핑
- [x] Red/Green: 닉네임 중복 → 409 예외 매핑
- [x] Red/Green: 상태 위반 → 409 예외 매핑
- [x] Red/Green: `@Valid` 검증 실패 → 400
- [ ] Refactor

**관련 도메인 행위** — 4단계 도메인 TDD 후 확정
- 애그리거트 메서드: `Member.changeNickname(Nickname)` / `Member.ensureCanChangeNickname()`

**서비스 호출 계약** — 6단계 서비스 TDD 후 확정
- 포트: `MemberModifier.changeNickname(MemberNicknameChangeRequest)`
- DTO: `MemberNicknameChangeRequest(memberId, nickname)`

**validation 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- 검증 대상과 실패 시 응답 모양: (미정)

**예외 번역 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**principal 매핑** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**컨트롤러 테스트 포인트** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**OpenAPI path ref** — 9단계 최종 확정 시 채움
- (미정)

---

### 회원 정지

#### A. 외부 계약

**목적**: 관리자가 ACTIVE 상태의 회원을 정지한다

**호출 주체**: 관리자

**엔드포인트**
- URL: `/api/v1/admin/members/suspension`
- HTTP Method: POST

**요청**
- path: 없음
- query: 없음
- body 초안:
  ```json
  { "memberId": 42 }
  ```
  대상 회원 식별자는 body로 전달 (URL path에 memberId 노출 방지 — §3 판단 근거 참조).

**성공 응답**
- 상태 코드: `204 No Content`
- 응답 바디 초안: 없음

**실패 응답 (가설)** — 이 유스케이스 고유의 비즈니스 실패만
- 대상 회원 없음 → `404 Not Found` (MemberNotFoundException)
- 상태 전이 위반 (ACTIVE가 아닌 상태에서 정지 시도) → `409 Conflict` (MemberStatusException)

**인증/인가 경계**
- 호출 허용 대상: 관리자
- 경계 규칙 메모: 관리자 권한 확인은 **인가 계층 전담**. 서비스는 권한 검증하지 않음 (산출물 §2.4 정합).

**멱등성 / no-op**
- 반복 호출 시 계약: 멱등성 없음. 이미 SUSPENDED 상태에서 재요청 시 `MemberStatusException` → `409 Conflict` 발생.

**예시 요청/응답**
```http
POST /api/v1/admin/members/suspension
Content-Type: application/json

{ "memberId": 42 }
```
```http
204 No Content
```

**미결 사항**
- 정지 사유(`reason`) 필드는 향후 도메인 확장 시 body에 optional로 추가 예정

#### B. 백엔드 확인 사항

**상위 참조**: [회원_산출물.md](회원_산출물.md) / 회원 정지

**컨트롤러 TDD Phase 체크리스트** (Phase 3)
- [x] `AdminMemberControllerApi.suspend` 인터페이스 선언 (일반 회원 API와 분리)
- [x] Red/Green: URL 매핑 + body DTO 바인딩 (`MemberSuspendRequest`)
- [x] Red/Green: 회원 없음 → 404
- [x] Red/Green: 상태 위반 → 409
- [x] 관리자 인가 표현 결정 — [ADR-002](../결정기록/ADR-002_관리자_인가_방식.md) (메서드 보안 채택) + [인증_인가_가이드.md §3-2](../참고자료/인증_인가_가이드.md) (`hasAuthority('ROLE_ADMIN')` 표현 채택): `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`
- [x] Red/Green: 권한 없음(일반 회원 호출) → 403
- [x] Red/Green: `@Valid` 검증 실패(memberId null 등) → 400
- [ ] Refactor

**관련 도메인 행위** — 4단계 도메인 TDD 후 확정
- 애그리거트 메서드: `Member.suspend()`

**서비스 호출 계약** — 6단계 서비스 TDD 후 확정
- 포트: `MemberModifier.suspend(MemberSuspendRequest)`
- DTO: `MemberSuspendRequest(memberId)`

**validation 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**예외 번역 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**principal 매핑** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**컨트롤러 테스트 포인트** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**OpenAPI path ref** — 9단계 최종 확정 시 채움
- (미정)

---

### 탈퇴

#### A. 외부 계약

**목적**: 회원이 자신의 계정을 탈퇴한다 (상태 DEACTIVATED로 전이)

**호출 주체**: 로그인한 회원 (본인)

**엔드포인트**
- URL: `/api/v1/members/me`
- HTTP Method: DELETE

**요청**
- path: 없음 (`me`는 principal 기반 자기 참조)
- query: 없음
- body 초안: 없음

**성공 응답**
- 상태 코드: `204 No Content`
- 응답 바디 초안: 없음

**실패 응답 (가설)** — 이 유스케이스 고유의 비즈니스 실패만
- 대상 회원 없음 → `404 Not Found` (MemberNotFoundException — principal이 가리키는 회원이 존재하지 않는 경우)
- 상태 전이 위반 (ACTIVE가 아닌 상태에서 탈퇴 시도) → `409 Conflict` (MemberStatusException)

**인증/인가 경계**
- 호출 허용 대상: 로그인한 회원 본인
- 경계 규칙 메모: controller가 인증 principal에서 memberId를 추출 (산출물 §2.3 "본인 확인 인증 계층 전담"과 정합).

**멱등성 / no-op**
- 반복 호출 시 계약: 멱등성 없음. 이미 DEACTIVATED 상태면 `MemberStatusException` → `409 Conflict` 발생.

**예시 요청/응답**
```http
DELETE /api/v1/members/me
```
```http
204 No Content
```

**미결 사항**
- 없음

#### B. 백엔드 확인 사항

**상위 참조**: [회원_산출물.md](회원_산출물.md) / 탈퇴

**컨트롤러 TDD Phase 체크리스트** (Phase 1 — 기본 패턴 체득)
- [x] `MemberControllerApi.withdraw` 인터페이스 선언
- [x] Red: URL 매핑 실패 테스트
- [x] Green: 컨트롤러 구현 (`@CurrentUser Long memberId`)
- [x] Red/Green: 회원 없음 → 404 매핑
- [x] Red/Green: 상태 위반 → 409 매핑
- [ ] Refactor

**관련 도메인 행위** — 4단계 도메인 TDD 후 확정
- 애그리거트 메서드: `Member.withdraw()`

**서비스 호출 계약** — 6단계 서비스 TDD 후 확정
- 포트: `MemberModifier.withdraw(Long requesterId)` (DTO 없이 principal ID 직접 전달)

**validation 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**예외 번역 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**principal 매핑** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**컨트롤러 테스트 포인트** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**OpenAPI path ref** — 9단계 최종 확정 시 채움
- (미정)

---

### 관리자 탈퇴 처리

#### A. 외부 계약

**목적**: 관리자가 SUSPENDED 상태의 회원을 탈퇴 처리한다 (상태 DEACTIVATED로 전이)

**호출 주체**: 관리자

**엔드포인트**
- URL: `/api/v1/admin/members/withdrawal`
- HTTP Method: POST

**요청**
- path: 없음
- query: 없음
- body 초안:
  ```json
  { "memberId": 42 }
  ```
  대상 회원 식별자는 body로 전달 (§3 판단 근거 참조).

**성공 응답**
- 상태 코드: `204 No Content`
- 응답 바디 초안: 없음

**실패 응답 (가설)** — 이 유스케이스 고유의 비즈니스 실패만
- 대상 회원 없음 → `404 Not Found` (MemberNotFoundException)
- 상태 전이 위반 (SUSPENDED가 아닌 상태에서 탈퇴 처리 시도) → `409 Conflict` (MemberStatusException)

**인증/인가 경계**
- 호출 허용 대상: 관리자
- 경계 규칙 메모: 관리자 권한 확인은 **인가 계층 전담**. 서비스는 권한 검증하지 않음 (산출물 §2.5 정합).

**멱등성 / no-op**
- 반복 호출 시 계약: 멱등성 없음. 이미 DEACTIVATED 상태면 `MemberStatusException` → `409 Conflict` 발생.

**예시 요청/응답**
```http
POST /api/v1/admin/members/withdrawal
Content-Type: application/json

{ "memberId": 42 }
```
```http
204 No Content
```

**미결 사항**
- 없음

#### B. 백엔드 확인 사항

**상위 참조**: [회원_산출물.md](회원_산출물.md) / 관리자 탈퇴 처리

**컨트롤러 TDD Phase 체크리스트** (Phase 4)
- [x] `AdminMemberControllerApi.withdrawByAdmin` 인터페이스 선언
- [x] Red/Green: URL 매핑 + body DTO 바인딩 (`MemberWithdrawByAdminRequest`)
- [x] Red/Green: 회원 없음 → 404
- [x] Red/Green: 상태 위반 (SUSPENDED 아님) → 409
- [x] 관리자 인가 표현 결정 — [ADR-002](../결정기록/ADR-002_관리자_인가_방식.md) + [인증_인가_가이드.md §3-2](../참고자료/인증_인가_가이드.md): `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` 채택 (Phase 3에서 기 결정)
- [x] Red/Green: 권한 없음(일반 회원 호출) → 403
- [x] Red/Green: `@Valid` 검증 실패(memberId null 등) → 400
- [ ] Refactor

**관련 도메인 행위** — 4단계 도메인 TDD 후 확정
- 애그리거트 메서드: `Member.withdrawByAdmin()`

**서비스 호출 계약** — 6단계 서비스 TDD 후 확정
- 포트: `MemberModifier.withdrawByAdmin(MemberWithdrawByAdminRequest)`
- DTO: `MemberWithdrawByAdminRequest(memberId)`

**validation 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**예외 번역 확정 사항** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**principal 매핑** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**컨트롤러 테스트 포인트** — 8단계 컨트롤러 TDD 후 확정
- (미정)

**OpenAPI path ref** — 9단계 최종 확정 시 채움
- (미정)

---

**[전역 부록]**

## 3. 판단 근거

> 관례 밖 결정과 그 근거. 2-4 판단 지점에서 도출되는 것 위주로 기록한다.

### 3-1. URL 정책: 본인은 `/me`, 관리자는 body DTO

**결정**
- 본인 조작(회원 정보 수정, 탈퇴): `/api/v1/members/me` 패턴
- 관리자 조작(회원 정지, 관리자 탈퇴 처리): URL에 대상 id를 넣지 않고 body DTO로 전달
  - `POST /api/v1/admin/members/suspension` + `{ "memberId": 42 }`
  - `POST /api/v1/admin/members/withdrawal` + `{ "memberId": 42 }`

**이유**

1. **본인 조작에서 path-id의 실익이 없음**
   - 대상이 principal 자신이므로 path id 값은 항상 principal.memberId와 같아야 함 → 값 자체가 중복
   - 산출물 §2.2, §2.3의 "본인 확인 책임은 인증 계층 전담" 결정과 정합: path에 id가 없으면 impersonation 표면 자체가 사라짐
   - 업계 관행과도 일치 (GitHub `/user`, Google `/me` 등)

2. **관리자 조작에서 memberId를 path에 두면 개인정보가 기본값으로 외부에 새어나감**
   - path는 access log / 브라우저 히스토리 / Referer / 3rd-party analytics / APM / 에러 리포트에 **기본적으로** 기록됨
   - body는 위 채널들에서 **기본적으로 기록되지 않음** (payload logging은 opt-in)
   - memberId가 순차 정수 PK이므로 노출 시 enumeration 힌트가 됨
   - 관리자 API가 보안 맥락(SSO gated 등)에 있어도 표면을 줄이는 게 방어 심도에 유리

**트레이드오프 (감수한 비용)**
- 관리자 API가 RPC-style로 바뀌어 REST 순수성 약해짐 — action endpoint(`/suspension`, `/withdrawal`)이 리소스 아닌 행위를 가리킴
- URL만으로 "어느 대상에 대한 조작인지" 구분 불가 → 감사 로그는 **앱 레벨**에서 body 포함한 로깅 필요

**후순위 검토**
- Member PK를 UUID/opaque로 전환하면 path 노출 우려 자체가 줄어듦. 그때 관리자 API를 `/admin/members/{id}/suspension` 같은 resource-style로 환원하는 것 재검토 가능.

---

## 4. 변경 이력

| 날짜 | 단계 | 유스케이스 ID | 변경 내용 | breaking 여부 | 프론트 공유 여부 |
|------|------|---------------|-----------|---------------|-------------------|
| 2026-04-21 | 2단계 | — | 문서 골격 생성 (DRAFT) | N | 공유 전 |
| 2026-04-24 | 8단계 | 1·2·3·4 | Phase 1~4 컨트롤러 구현 완료. Phase 3·4는 `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` 적용 | N | 공유 전 |
| 2026-04-25 | 8단계 | — | Phase 5 완료 — 통합 스모크 4건 + OpenAPI 자동 검증 1건. 로컬 개발용 MySQL 환경 추가 | N | 공유 전 |

---

## 5. OpenAPI 동기화

- 동기화 대상: (없음 — 9단계 도달 시 결정)
- 마지막 동기화 시점: —
- 누락/불일치 메모: —
