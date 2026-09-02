# AI Agent Lab

AI Agent를 활용한 개발 workflow를 실험하기 위한 프로젝트.

## 목표

간단한 Todo 관리 REST API를 구현한다.

## 기능

- Todo 생성
- Todo 목록 조회
- Todo 단건 조회
- Todo 수정
- Todo 삭제

## 요구사항

- Todo의 `title`은 필수다.
- `title`은 최대 100자까지 허용한다.
- 존재하지 않는 Todo를 조회하면 적절한 HTTP 상태 코드로 응답한다.
- 존재하지 않는 Todo를 수정하면 적절한 HTTP 상태 코드로 응답한다.
- 존재하지 않는 Todo를 삭제하면 적절한 HTTP 상태 코드로 응답한다.
- REST API 형태로 제공한다.

## 기술

- Java
- Spring Boot
- PostgreSQL