# Querydsl 쿼리 패턴

## 목적

동적 조회 조건을 작성할 때 `where()`의 null 처리, `BooleanExpression` 체이닝, `BooleanBuilder`, `Expressions.TRUE/FALSE` 항등 조건을 언제 쓰는지 정리한다.

이 문서의 목표는 Querydsl 기능 목록을 설명하는 것이 아니라, 검색/필터 쿼리에서 조용히 잘못된 결과가 나오는 패턴을 피하는 것이다.

## 기본 선택

검색 폼처럼 optional 조건들이 AND로 결합되는 목록 조회는 아래 패턴을 기본값으로 둔다.

- 조건 메서드는 `BooleanExpression`을 반환한다.
- 조건이 없으면 `null`을 반환한다.
- 조건 조립은 `where(...)` 가변인자에 맡긴다.

```java
queryFactory.selectFrom(newsletter)
        .where(
                categoryEq(category),
                keywordContains(keyword)
        )
        .fetch();
```

```java
private BooleanExpression categoryEq(NewsletterCategory category) {
    return category == null ? null : newsletter.category.eq(category);
}
```

Querydsl의 `where(...)`는 null 조건을 무시한다. 그래서 각 조건 메서드가 "조건 없음"을 `null`로 표현해도 전체 AND 조건을 안전하게 조립할 수 있다.

이 방식은 Querydsl 코드에서 가장 흔하게 읽히는 형태다. 별도의 팀 컨벤션을 몰라도 `BooleanExpression` 조건을 만들고, 없으면 null을 반환해 `where(...)`에 넘긴다는 흐름을 바로 이해할 수 있다.

## 체이닝 주의

`where(...)`의 null 무시는 where 인자 레벨에서만 안전하게 기대한다.

아래처럼 `BooleanExpression`을 직접 체이닝할 때 좌측이 null이면 메서드 호출 자체가 불가능하다.

```java
categoryEq(category).and(keywordContains(keyword));
```

`categoryEq(category)`가 null이면 NPE가 발생한다.

Querydsl 5.1.0 기준으로 `.and(null)`과 `.or(null)`은 자기 자신을 반환한다. 즉 우측 null보다 좌측 null이 더 직접적인 NPE 위험이다.

```java
expr.and(null); // expr
expr.or(null);  // expr
```

하지만 우측 null을 허용한다는 이유로 null 반환 helper를 자유롭게 체이닝해도 된다고 해석하지 않는다. 재사용 helper가 어느 위치에 놓일지 코드만 보고 보장하기 어렵기 때문이다.

## OR 조건 그룹

키워드 검색처럼 여러 필드 중 하나라도 매칭되면 되는 조건은 메서드 내부에서 하나의 OR 그룹으로 완성한다.

```java
private BooleanExpression keywordContains(String keyword) {
    if (!hasText(keyword)) {
        return null;
    }

    return newsletter.title.containsIgnoreCase(keyword)
            .or(newsletter.description.containsIgnoreCase(keyword));
}
```

바깥에서는 OR 그룹을 조건 하나로 취급한다.

```java
queryFactory.selectFrom(newsletter)
        .where(
                categoryEq(category),
                keywordContains(keyword)
        )
        .fetch();
```

OR 조건을 바깥에서 조금씩 이어 붙이면 null 위치와 항등 조건을 계속 의식해야 한다. 검색어 하나에 대한 OR 그룹은 가능한 한 한 메서드 안에서 닫는다.

## TRUE/FALSE 항등 조건

조건이 없을 때도 항상 non-null `BooleanExpression`을 반환해야 한다면 항등 조건을 사용할 수 있다.

AND 조립의 항등원은 TRUE다.

```text
TRUE and A = A
```

OR 조립의 항등원은 FALSE다.

```text
FALSE or A = A
```

예를 들어 AND 체이닝 전용 helper는 조건이 없을 때 `Expressions.TRUE`를 반환할 수 있다.

```java
private BooleanExpression categoryEqForAnd(NewsletterCategory category) {
    return category == null ? Expressions.TRUE : newsletter.category.eq(category);
}
```

그러나 이 패턴은 기본 구현 방식으로 채택하지 않는다.

메서드 시그니처만 보면 해당 helper가 AND 전용인지 OR 전용인지 알 수 없다. AND 항등원인 TRUE를 OR 문맥에 넣으면 전체 조건이 항상 참이 된다.

```java
expr.or(Expressions.TRUE); // 전체 TRUE
```

OR 항등원인 FALSE를 AND 문맥에 넣으면 전체 조건이 항상 거짓이 된다.

```java
expr.and(Expressions.FALSE); // 전체 FALSE
```

이 문제는 예외 없이 조용히 잘못된 결과를 반환하므로 NPE보다 발견하기 어렵다.

## Predicate 반환 컨벤션

일부 코드에서는 nullable where 조건 helper의 반환 타입을 `Predicate`로 둔다.

```java
private Predicate categoryEq(NewsletterCategory category) {
    return category == null ? null : newsletter.category.eq(category);
}
```

이 컨벤션의 의도는 "이 메서드는 `where(...)`에 넣는 용도이며 체이닝하지 말라"는 메시지를 타입으로 드러내는 것이다.

`Predicate`에는 `.and()`와 `.or()`가 없으므로 아래 같은 실수는 컴파일 단계에서 막힌다.

```java
categoryEq(category).and(keywordContains(keyword));
```

하지만 이 프로젝트의 기본 규칙으로는 채택하지 않는다.

이유:

- `Predicate` 반환이 null 가능성을 타입으로 보장하지는 않는다.
- `Predicate`와 `BooleanExpression` 반환 기준을 팀원이 추가로 외워야 한다.
- Querydsl에 익숙하지 않은 사람이 읽을 때 "왜 어떤 helper는 Predicate고 어떤 helper는 BooleanExpression인가"가 새 혼란이 된다.
- private repository helper에서는 `where(...)` 가변인자만 사용한다는 코드 리뷰 규칙으로 충분히 통제할 수 있다.

따라서 letterPick의 기본값은 `BooleanExpression + null 반환 + where(...)`다.

외부 참고 코드나 기존 코드에서 `Predicate` 반환 helper를 만나면, "where 전용 nullable 조건 helper일 수 있다"는 신호로 해석한다. 새 코드에서 이 컨벤션을 도입하려면 해당 repository 안에서 반환 타입 기준을 먼저 통일한다.

## BooleanBuilder

`BooleanBuilder`는 조건을 명령형으로 누적할 때 사용한다.

```java
BooleanBuilder builder = new BooleanBuilder();

if (category != null) {
    builder.and(newsletter.category.eq(category));
}

if (hasText(keyword)) {
    builder.and(
            newsletter.title.containsIgnoreCase(keyword)
                    .or(newsletter.description.containsIgnoreCase(keyword))
    );
}
```

단순 AND 필터에서는 `where(...)` 가변인자와 null 반환 helper가 더 읽기 쉽다.

다음 상황에서는 `BooleanBuilder`를 검토한다.

- 조건을 반복문으로 누적한다.
- OR 조건 개수가 동적으로 바뀐다.
- 조건 그룹을 단계적으로 조립해야 한다.
- null 반환 helper를 체이닝해야 해서 사용 위치가 불안정하다.

## Spring Data method와 Querydsl 선택

Spring Data derived query가 충분한 경우:

- 조건이 고정되어 있다.
- 파라미터 null이 "조건 없음"을 의미하지 않는다.
- 단순 equality 조회다.

```java
Slice<Newsletter> findByCategory(NewsletterCategory category, Pageable pageable);
```

이 메서드는 `category`가 항상 있어야 하는 조회에는 적합하다.

하지만 `category == null`일 때 전체 조회를 의미해야 하면 같은 메서드로 표현하지 않는다. Spring Data derived query의 null 파라미터는 "조건 제외"가 아니라 해당 필드의 null 값 조회로 해석될 수 있다.

이런 경우는 서비스에서 분기하거나 Querydsl custom query를 사용한다.

```java
private BooleanExpression categoryEq(NewsletterCategory category) {
    return category == null ? null : newsletter.category.eq(category);
}
```

Querydsl custom query가 맞는 경우:

- null이면 조건을 제외해야 한다.
- optional filter가 늘어날 가능성이 높다.
- OR 검색 그룹이 있다.
- fetch join, 정렬, Slice 조회를 직접 제어해야 한다.

## 조회용 DTO projection

조회 전용 목록 API는 repository가 엔티티 대신 조회용 DTO를 반환할 수 있다.

```text
Repository -> Slice<조회용 DTO>
Service    -> Slice<조회용 DTO>
Controller -> collection response envelope
```

참고: Spring Data JPA는 repository method가 entity 전체가 아니라 interface/class 기반 projection을 반환하는 방식을 공식 지원한다.

https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html

이 패턴은 command/update 경로의 repository 사용 방식과 다르다.

### 엔티티 반환이 맞는 경우

엔티티 반환은 도메인 행위가 필요한 경로에 맞다.

- 상태 변경, 수정, 삭제가 필요하다.
- 도메인 메서드를 호출해야 한다.
- 불변식 검증이나 상태 전이가 필요하다.
- 영속성 컨텍스트의 dirty checking을 활용해야 한다.
- 하나의 애그리거트를 로딩해 도메인 규칙을 적용해야 한다.

이 경우 repository는 엔티티를 반환하고 application service가 도메인 행위를 수행한다.

```text
Repository -> Entity
Service    -> domain method 호출 / 상태 변경
```

### DTO projection이 맞는 경우

DTO projection은 read-only 조회에 맞다.

- 목록, 검색, 대시보드처럼 화면에 필요한 값을 읽기만 한다.
- 페이징, 필터, 정렬이 붙는다.
- 여러 테이블 값을 한 줄의 read model로 평탄화해야 한다.
- 전체 엔티티 컬럼을 읽을 필요가 없다.
- 연관관계 접근으로 인한 N+1을 query 단계에서 통제하고 싶다.
- 응답이 도메인 모델 구조보다 화면/조회 목적에 가깝다.

이 경우 Querydsl select 단계에서 필요한 값을 DTO로 만든다.

```java
List<NewsletterListItem> results = queryFactory
        .select(Projections.constructor(
                NewsletterListItem.class,
                newsletter.id,
                newsletter.title,
                newsletter.category
        ))
        .from(newsletter)
        .where(categoryEq(condition.category()))
        .orderBy(newsletter.id.asc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1L)
        .fetch();
```

위 예시는 projection 흐름을 설명하기 위한 것이다. 실제 DTO 이름과 필드는 해당 유스케이스의 API/조회 계약에서 정한다.

### 조회용 DTO와 Web Response DTO

repository가 반환하는 DTO는 web response DTO가 아니라 조회용 DTO다.

```text
좋음:
adapter.persistence -> application query/read model

피함:
adapter.persistence -> adapter.webapi.dto.NewslettersResponse
```

조회용 DTO는 "이 쿼리가 반환하는 의미 있는 데이터 묶음"이다. Web Response DTO는 "그 데이터를 HTTP/JSON으로 어떤 모양으로 노출할지"를 표현한다.

두 DTO를 항상 처음부터 분리할 필요는 없다. 다음처럼 item DTO를 response envelope 안에 그대로 넣는 완화된 구조도 가능하다.

```text
Repository -> Slice<NewsletterListItem>
Controller -> { items: List<NewsletterListItem>, page: ... }
```

이 구조는 `Slice`/`Page` 직접 노출과 top-level 배열 응답을 피한다. 다만 `items[]` 내부 JSON shape는 `NewsletterListItem`과 같아진다.

item 내부 shape가 달라지기 시작하면 response item DTO를 분리한다.

```text
Repository -> Slice<NewsletterListItem>
Controller -> { items: List<NewsletterItemResponse>, page: ... }
```

분리 신호:

- 외부 JSON 필드명만 바꾸고 query DTO 필드는 유지하고 싶다.
- flat query result를 nested JSON으로 내려줘야 한다.
- 날짜, 상태, label 같은 표시 형식을 response에서 가공해야 한다.
- 사용자별 권한, 구독 상태, 읽음 여부처럼 repository 단독으로 만들 수 없는 필드가 item에 추가된다.
- 같은 조회용 DTO를 서로 다른 API 응답 shape로 재사용해야 한다.

### projection 방식 선택

Querydsl DTO projection 방식은 여러 가지가 있다.

- `Projections.constructor`: DTO 생성자 기준. DTO가 Querydsl을 몰라도 된다. 타입/순서 변경을 리뷰에서 확인해야 한다.
- `Projections.fields` / `Projections.bean`: 필드명 또는 setter 기준. alias가 중요하다.
- `@QueryProjection`: 컴파일 타임 안정성이 좋지만 DTO가 Querydsl annotation과 generated Q 타입에 의존한다.

참고: Querydsl API는 constructor, bean, fields 기반 projection factory와 `@QueryProjection` annotation을 제공한다.

https://querydsl.com/static/querydsl/5.0.0/apidocs/com/querydsl/core/types/Projections.html

https://querydsl.com/static/querydsl/5.0.0/apidocs/com/querydsl/core/annotations/QueryProjection.html

기본값은 DTO를 Querydsl에 묶지 않는 방식부터 검토한다. projection 필드가 많아져 순서 오류가 실제 부담이 되면 `@QueryProjection` 도입을 repository 단위로 검토한다.

### trade-off

DTO projection으로 얻는 것:

- 필요한 컬럼만 조회한다.
- read-only 목록에서 엔티티 hydration 비용을 줄인다.
- join 결과를 목록 item으로 바로 평탄화할 수 있다.
- LAZY 연관관계 접근으로 인한 N+1 위험을 query 단계에서 줄인다.
- 단순 조회 서비스가 불필요한 entity-to-dto 매핑으로 두꺼워지는 것을 막는다.

DTO projection으로 잃는 것:

- repository query가 조회용 DTO shape에 묶인다.
- DTO가 늘어난다.
- projection DTO에는 도메인 행위를 호출할 수 없다.
- 비슷한 목록이 늘어나면 유사한 projection DTO가 여러 개 생긴다.
- query와 DTO 생성자가 함께 바뀌므로 리팩토링 시 리뷰 포인트가 늘어난다.

따라서 기본 규칙은 "읽기는 무조건 DTO, 쓰기는 무조건 엔티티"가 아니다. 도메인 행위가 필요한 command 경로는 엔티티를 반환하고, 화면/검색/목록 중심의 read-only query는 DTO projection을 우선 검토한다.

## Slice 조회 패턴

Spring Data가 생성한 repository method는 반환 타입이 `Slice`이면 Slice 객체 생성을 프레임워크가 처리한다.

하지만 Querydsl custom 구현은 일반 Java 코드다. Querydsl의 `fetch()`는 `List`를 반환하므로 직접 `SliceImpl`로 감싸야 한다.

```java
List<NewsletterListItem> results = queryFactory
        .select(Projections.constructor(
                NewsletterListItem.class,
                newsletter.id,
                newsletter.title,
                newsletter.category
        ))
        .from(newsletter)
        .where(categoryEq(condition.category()))
        .orderBy(newsletter.id.asc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1L)
        .fetch();

boolean hasNext = results.size() > pageable.getPageSize();
List<NewsletterListItem> content = hasNext
        ? results.subList(0, pageable.getPageSize())
        : results;

return new SliceImpl<>(content, pageable, hasNext);
```

`Slice`는 전체 count가 아니라 다음 조각 존재 여부만 필요하다는 뜻이다. 그래서 `pageSize + 1`개를 조회하고 초과분이 있으면 `hasNext = true`로 판단한다.

목록 조회에는 안정적인 정렬 기준도 필요하다. offset 기반 조회에서 `orderBy`가 없으면 페이지 사이의 순서가 흔들릴 수 있다.

```java
.orderBy(newsletter.id.asc())
```

정렬 기준은 도메인/API 요구에 맞춰 정한다. 요구가 없으면 MVP에서는 단순하고 안정적인 식별자 정렬부터 시작한다.

## 코드 리뷰 체크리스트

- optional AND 조건이 `where(...)` 가변인자와 null 반환 helper로 조립되는가?
- nullable 조건 helper가 프로젝트 기본값인 `BooleanExpression` 반환을 따르는가?
- `Predicate` 반환 helper를 도입했다면 해당 repository 안에서 반환 타입 기준이 일관적인가?
- null 반환 helper가 `.and()` 또는 `.or()` 체이닝의 좌측에 오지 않는가?
- OR 검색 그룹이 메서드 내부에서 하나의 조건으로 닫혀 있는가?
- `Expressions.TRUE/FALSE`가 반대 연산자 문맥에 들어가 결과를 폭발시키지 않는가?
- 단순 equality 조회를 Querydsl로 과하게 작성하고 있지 않은가?
- null이면 전체 조회인 요구를 Spring Data derived query 하나로 억지 표현하고 있지 않은가?
- read-only 목록 조회에서 엔티티 반환과 DTO projection 중 어느 쪽이 맞는지 검토했는가?
- DTO projection을 쓴다면 repository가 web response DTO가 아니라 조회용 DTO를 반환하는가?
- Querydsl custom `Slice` 조회가 `pageSize + 1`개를 가져오는가?
- offset 기반 목록 조회에 안정적인 `orderBy`가 있는가?
