## API 규약

- Base URL: http://localhost:8080

- Content-Type: application/json

- (데모 인증) X-USER-ID: <int> — 작성/수정/삭제/좋아요 시 필요

- 날짜 포맷: ISO-8601
  
<br>

## 성공 응답
{ "message": "<result_code>", "data": { ... } }

<br>

## 에러 응답 (전역 통일)
{ "status": <http_status_int>, "message": "<error_message>", "data": null }

<br>

## 엔드포인트 요약

  ### 사용자

- POST /users/signup — 회원가입

### 게시글

- GET /posts — 목록/검색/정렬/페이징
  query, authorId, hasImage, from, to, page, size, sort(LATEST|POPULAR|VIEW)

- GET /posts/{postId} — 상세 (increaseView=true 기본)

- POST /posts — 작성 (X-USER-ID 필요)

- PATCH /posts/{postId} — 수정 (X-USER-ID 필요)

- PUT /posts/{postId} — 소프트 삭제 (X-USER-ID 필요)

- POST /posts/{postId}/like — 좋아요 (X-USER-ID 필요)

- DELETE /posts/{postId}/like — 좋아요 취소 (X-USER-ID 필요)

- GET /posts/me/likes — 내가 좋아요한 글 (X-USER-ID 필요)

### 댓글

- GET /posts/{postId}/comments — 목록

- POST /posts/{postId}/comments — 작성 (X-USER-ID 필요)

- PATCH /posts/{postId}/comments/{commentId} — 수정 (X-USER-ID 필요, 작성자 본인)

- PUT /posts/{postId}/comments/{commentId} — 소프트 삭제 (X-USER-ID 필요, 작성자 본인)

<br>

## 디렉토리 구조

src/main/java/com/ktb/community

├─ config/QuerydslConfig.java

├─ domain/           # 엔티티 (Integer ID, INT UNSIGNED 매핑)

├─ dto/              # 요청/응답 DTO

├─ exception/        # ErrorCode, ApiException, GlobalExceptionHandler

├─ repository/       # JpaRepository + custom + QueryDSL impl

├─ service/          # 도메인 서비스

└─ controller/       # REST API
