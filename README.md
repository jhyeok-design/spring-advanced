# SPRING ADVANCED

## 1. 프로젝트 개요

Spring 기반 Todo 애플리케이션을 바탕으로 다음 내용을 중심으로 리팩토링과 기능 개선을 진행한 과제입니다.

- AuthUserArgumentResolver를 이용한 로그인 사용자 정보 주입
- Early Return, 불필요한 if-else 제거 등 코드 정리
- DTO + Validation을 활용한 입력값 검증 분리
- EntityGraph를 이용한 N+1 문제 해결
- 단위 테스트 코드 수정 및 보완
- Interceptor + AOP를 활용한 어드민 API 로깅 (도전 과제)

---

## 2. 기술 스택

- Language
    - Java

- Framework / Library
    - Spring Boot
    - Spring Web
    - Spring Data JPA
    - Spring Validation
    - Lombok

- Database
    - MySQL



---

## 3. 필수 과제 요약

- ArgumentResolver
    - AuthUserArgumentResolver를 구현해 컨트롤러에서 AuthUser 파라미터를 바로 받을 수 있도록 구성
    - 사용자 정보 조회 로직을 한 곳으로 모아 컨트롤러의 중복 코드를 줄임

- 코드 개선
    - 회원가입 로직에 Early Return을 적용해, 이미 존재하는 이메일인 경우 불필요한 비밀번호 인코딩과 저장을 막음
    - 날씨 조회 로직에서 복잡한 if-else 구조를 단순하게 정리하고 에러를 먼저 처리하는 방식으로 가독성 개선
    - 비밀번호 형식 검증을 DTO와 Validation으로 이동시켜 서비스 레이어는 비즈니스 로직에 집중하도록 역할 분리

- N+1 문제 해결
    - Todo와 User를 함께 조회할 때 EntityGraph를 사용해 N+1 문제가 발생하지 않도록 개선

- 테스트 코드 정비
    - PasswordEncoder, ManagerService, CommentService 관련 테스트를 실제 예외 타입과 메시지에 맞게 수정
    - Todo의 user가 null인 경우를 명시적으로 예외 처리해, 테스트와 실제 동작을 일치시킴

---

## 4. 도전 과제 – Interceptor + AOP 기반 어드민 API 로깅

이 프로젝트에서는 어드민 전용 API에 대해 Filter, Interceptor, AOP를 함께 활용하여 접근, 요청, 응답을 단계별로 로깅하도록 구성했습니다.

### 4-1. 전체 구조

요청 흐름은 다음과 같은 순서로 동작하도록 설계했습니다.

요청  
→ LoggingFilter  
→ AdminCheckInterceptor  
→ 어드민 컨트롤러 및 AdminService  
→ LoggingCheckAop  
→ 응답

각 계층의 책임은 다음과 같습니다.

- LoggingFilter
    - 요청과 응답을 캐싱 가능한 형태로 감싸서, 이후 단계에서 요청/응답 본문을 안전하게 읽을 수 있도록 지원

- AdminCheckInterceptor
    - 요청에 어드민 권한 정보가 있는지 확인
    - 어드민이 아닌 경우 바로 403 응답을 내려 보내고 진행 중단
    - 어드민이라면 URL과 요청 시각을 간단히 로그로 남김

- LoggingCheckAop
    - AdminService 계층 메서드 실행 전후에 동작
    - 요청 사용자 ID, 요청 시각, URL, 요청 본문, 응답 본문을 상세하게 로깅

### 4-2. 동작 흐름

1. LoggingFilter
    - 모든 요청을 ContentCachingRequestWrapper, ContentCachingResponseWrapper로 한 번 감쌉니다.
    - 이렇게 감싸두면 나중에 요청 본문과 응답 본문을 여러 번 읽어도 문제가 되지 않습니다.
    - 응답 본문은 마지막에 실제 HttpServletResponse로 복사해서 클라이언트로 전달합니다.

2. AdminCheckInterceptor
    - 컨트롤러에 도달하기 전에 Admin이라는 요청 속성을 확인합니다.
    - 값이 없으면 권한 없음으로 판단하고 403 상태 코드와 함께 요청을 바로 종료합니다.
    - 값이 있다면 어드민 요청으로 보고, 요청 URL과 현재 시각을 로그로 남긴 뒤 다음 단계로 넘깁니다.

3. LoggingCheckAop
    - 도메인 서비스 패키지 내 AdminService로 끝나는 서비스 클래스들을 대상으로 동작합니다.
    - 서비스 메서드 실행 전에 다음 정보를 수집합니다.
        - 요청한 사용자 ID (요청 속성에 저장된 userId 기준)
        - 요청 시각
        - 요청 URL
        - 요청 본문(필터에서 감싼 요청 래퍼를 통해 읽어옴, 내용이 없으면 별도 메시지로 처리)
    - 서비스 메서드를 실제로 실행한 뒤, 반환값을 JSON 문자열로 변환해 응답 본문으로 로그에 남깁니다.

### 4-3. 로깅 항목

어드민 API 호출 시 로깅되는 정보는 다음과 같습니다.

- 요청한 사용자 ID
- 요청 시각
- 요청 URL
- 요청 본문 (RequestBody, 주로 JSON 형태)
- 응답 본문 (ResponseBody, 서비스 반환값 기준)

Interceptor 단계에서는 접근 자체에 대한 기본 로그를,  
AOP 단계에서는 요청/응답에 대한 상세 내용을 남기도록 역할을 나누었습니다.

### 4-4. 기대 효과

- 어드민 전용 API 호출 이력을 시간, 사용자, URL 기준으로 쉽게 추적할 수 있습니다.
- 요청과 응답 내용을 함께 로그로 남겨 디버깅과 모니터링에 활용할 수 있습니다.
- 권한 체크, 접근 허용/차단, 상세 로깅을 각각 Filter, Interceptor, AOP로 나누어 구현해  
  유지보수가 쉽고 역할이 명확한 구조를 만들 수 있습니다.
