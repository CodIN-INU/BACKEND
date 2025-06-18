# 데이터베이스 분리 전략

## 1. 개요

현재 단일 MongoDB 데이터베이스를 각 마이크로서비스별로 독립적인 데이터베이스로 분리하는 전략을 수립합니다. 각 서비스는 자체 데이터를 소유하고 관리하며, 서비스 간 데이터 접근은 API를 통해서만 이루어집니다.

## 2. 현재 데이터베이스 구조 분석

### 2.1 현재 MongoDB 컬렉션 목록
```
codin (단일 데이터베이스)
├── users                    # 사용자 정보
├── posts                    # 게시물
├── comments                 # 댓글
├── replyComments           # 대댓글
├── likes                   # 좋아요
├── scraps                  # 스크랩
├── chatRooms               # 채팅방
├── chattings               # 채팅 메시지
├── lectures                # 강의 정보
├── reviews                 # 강의 리뷰
├── infos                   # 학과 정보 (교수, 연구실, 사무실)
├── reports                 # 신고
├── notifications           # 알림
├── blocks                  # 차단
└── emails                  # 이메일 발송 이력
```

## 3. 서비스별 데이터베이스 분리 계획

### 3.1 인증 서비스 데이터베이스 (codin-auth-db)
```
컬렉션:
├── users                   # 인증 관련 사용자 정보만
├── refresh_tokens          # 리프레시 토큰
├── oauth2_states          # OAuth2 상태 정보
└── login_attempts         # 로그인 시도 이력
```

**users 스키마 (인증 서비스용)**
```json
{
  "_id": "ObjectId",
  "email": "string",
  "password": "string (optional)",
  "role": "USER|ADMIN|MANAGER",
  "status": "ACTIVE|DISABLED|SUSPENDED",
  "provider": "GOOGLE|APPLE|LOCAL",
  "providerId": "string",
  "lastLoginAt": "Date",
  "createdAt": "Date",
  "updatedAt": "Date"
}
```

### 3.2 사용자 서비스 데이터베이스 (codin-user-db)
```
컬렉션:
├── user_profiles          # 사용자 프로필 정보
├── user_settings          # 사용자 설정
├── blocks                 # 차단 관계
└── user_activities        # 사용자 활동 로그
```

**user_profiles 스키마**
```json
{
  "_id": "ObjectId",
  "userId": "ObjectId (인증서비스의 user ID)",
  "nickname": "string",
  "profileImageUrl": "string",
  "department": "Department enum",
  "bio": "string",
  "isPublic": "boolean",
  "createdAt": "Date",
  "updatedAt": "Date"
}
```

### 3.3 콘텐츠 서비스 데이터베이스 (codin-content-db)
```
컬렉션:
├── posts                  # 게시물
├── comments               # 댓글
├── reply_comments         # 대댓글
├── likes                  # 좋아요
├── scraps                 # 스크랩
├── lectures               # 강의 정보
├── reviews               # 강의 리뷰
├── infos                 # 학과 정보
├── reports               # 신고
└── content_tags          # 콘텐츠 태그
```

### 3.4 알림 서비스 데이터베이스 (codin-notification-db)
```
컬렉션:
├── notifications          # 알림
├── notification_settings  # 알림 설정
├── fcm_tokens            # FCM 토큰
├── email_templates       # 이메일 템플릿
└── email_logs            # 이메일 발송 로그
```

### 3.5 채팅 서비스 데이터베이스 (codin-chat-db)
```
컬렉션:
├── chat_rooms            # 채팅방
├── chat_messages         # 채팅 메시지
├── chat_participants     # 채팅 참가자
└── chat_files           # 채팅 파일
```

## 4. 데이터 마이그레이션 전략

### 4.1 단계별 마이그레이션 계획

#### Phase 1: 데이터베이스 스키마 준비 (1주)
```bash
# 새로운 데이터베이스 생성
mongosh --eval "
use codin-auth-db;
db.createCollection('users');
db.createCollection('refresh_tokens');

use codin-user-db;
db.createCollection('user_profiles');
db.createCollection('blocks');

use codin-content-db;
db.createCollection('posts');
db.createCollection('comments');

use codin-notification-db;
db.createCollection('notifications');

use codin-chat-db;
db.createCollection('chat_rooms');
db.createCollection('chat_messages');
"
```

#### Phase 2: 데이터 이주 스크립트 작성 (1주)
```javascript
// 사용자 데이터 분리 스크립트
// 인증 서비스용 사용자 데이터
db.users.aggregate([
  {
    $project: {
      email: 1,
      password: 1,
      role: 1,
      status: 1,
      provider: "$oauthProvider",
      providerId: "$oauthId",
      createdAt: 1,
      updatedAt: 1
    }
  },
  {
    $out: {
      db: "codin-auth-db",
      coll: "users"
    }
  }
]);

// 사용자 프로필 데이터
db.users.aggregate([
  {
    $project: {
      userId: "$_id",
      nickname: 1,
      profileImageUrl: 1,
      department: 1,
      bio: 1,
      isPublic: 1,
      createdAt: 1,
      updatedAt: 1
    }
  },
  {
    $out: {
      db: "codin-user-db",
      coll: "user_profiles"
    }
  }
]);
```

#### Phase 3: 점진적 마이그레이션 (2주)
1. **읽기 전용 복제**: 기존 데이터를 새 데이터베이스로 복제
2. **이중 쓰기**: 새로운 데이터는 양쪽 데이터베이스에 동시 저장
3. **검증**: 데이터 일관성 검증
4. **전환**: 새 데이터베이스로 완전 전환

### 4.2 마이그레이션 스크립트 예시

```bash
#!/bin/bash
# migrate-data.sh

echo "Starting database migration..."

# 1. 인증 서비스 데이터 마이그레이션
echo "Migrating auth service data..."
mongosh codin --eval "
load('migration-scripts/migrate-auth-data.js');
migrateAuthData();
"

# 2. 사용자 서비스 데이터 마이그레이션
echo "Migrating user service data..."
mongosh codin --eval "
load('migration-scripts/migrate-user-data.js');
migrateUserData();
"

# 3. 콘텐츠 서비스 데이터 마이그레이션
echo "Migrating content service data..."
mongosh codin --eval "
load('migration-scripts/migrate-content-data.js');
migrateContentData();
"

# 4. 데이터 검증
echo "Validating migrated data..."
mongosh --eval "
load('migration-scripts/validate-migration.js');
validateMigration();
"

echo "Migration completed successfully!"
```

## 5. 데이터 일관성 보장 전략

### 5.1 분산 트랜잭션 패턴

#### Saga 패턴 구현
```java
@Service
public class PostCreationSaga {
    
    @SagaOrchestrationStart
    public void createPost(CreatePostRequest request) {
        // 1. 콘텐츠 서비스에서 게시물 생성
        // 2. 사용자 서비스에서 활동 점수 업데이트
        // 3. 알림 서비스에서 팔로워들에게 알림 발송
    }
    
    @SagaOrchestrationEnd
    public void compensatePostCreation(CreatePostRequest request) {
        // 실패 시 롤백 로직
    }
}
```

### 5.2 이벤트 소싱
```java
@EventHandler
public class UserEventHandler {
    
    @EventSourcing
    public void handleUserCreated(UserCreatedEvent event) {
        // 사용자 생성 시 다른 서비스에 필요한 데이터 동기화
    }
    
    @EventSourcing
    public void handleUserUpdated(UserUpdatedEvent event) {
        // 사용자 정보 업데이트 시 관련 서비스 데이터 동기화
    }
}
```

## 6. 서비스 간 데이터 참조 전략

### 6.1 서비스 간 ID 매핑
```json
// 콘텐츠 서비스의 게시물 데이터
{
  "_id": "post_id",
  "authorId": "user_id_from_auth_service",
  "title": "게시물 제목",
  "content": "게시물 내용",
  // 작성자 정보는 별도 API 호출로 조회
}
```

### 6.2 데이터 비정규화 전략
```json
// 성능을 위한 필수 정보 비정규화
{
  "_id": "post_id",
  "authorId": "user_id",
  "authorName": "작성자명",    // 비정규화된 데이터
  "authorDepartment": "학과", // 비정규화된 데이터
  "title": "게시물 제목",
  "lastSyncAt": "2024-01-01T00:00:00Z"
}
```

## 7. 데이터 동기화 메커니즘

### 7.1 CDC (Change Data Capture) 사용
```yaml
# MongoDB Change Streams 설정
spring:
  data:
    mongodb:
      change-stream:
        enabled: true
        collections:
          - users
          - posts
```

### 7.2 메시지 큐를 통한 이벤트 전파
```java
@EventListener
public void handleUserProfileUpdate(UserProfileUpdatedEvent event) {
    // 사용자 프로필 업데이트 시 관련 서비스들에 이벤트 발송
    messagingService.publish("user.profile.updated", event);
}
```

## 8. 백업 및 복구 전략

### 8.1 서비스별 백업 정책
```bash
# 인증 서비스 백업 (높은 우선순위)
mongodump --db codin-auth-db --out /backup/auth/$(date +%Y%m%d)

# 콘텐츠 서비스 백업 (중간 우선순위)
mongodump --db codin-content-db --out /backup/content/$(date +%Y%m%d)

# 채팅 서비스 백업 (낮은 우선순위)
mongodump --db codin-chat-db --out /backup/chat/$(date +%Y%m%d)
```

### 8.2 교차 서비스 참조 무결성 검증
```javascript
// 데이터 무결성 검증 스크립트
function validateCrossServiceReferences() {
    // 게시물의 작성자 ID가 사용자 서비스에 존재하는지 확인
    const orphanedPosts = db.posts.find({
        authorId: { $nin: getUserIdsFromAuthService() }
    });
    
    if (orphanedPosts.count() > 0) {
        print("WARNING: Found orphaned posts without valid authors");
    }
}
```

## 9. 성능 최적화

### 9.1 인덱스 전략
```javascript
// 각 서비스별 필수 인덱스 생성
// 인증 서비스
db.users.createIndex({ "email": 1 }, { unique: true });
db.users.createIndex({ "provider": 1, "providerId": 1 });

// 콘텐츠 서비스
db.posts.createIndex({ "authorId": 1, "createdAt": -1 });
db.comments.createIndex({ "postId": 1, "createdAt": 1 });

// 채팅 서비스
db.chat_messages.createIndex({ "chatRoomId": 1, "createdAt": 1 });
```

### 9.2 샤딩 전략
```javascript
// 채팅 메시지 샤딩 (채팅방 ID 기준)
sh.shardCollection("codin-chat-db.chat_messages", { "chatRoomId": 1 });

// 게시물 샤딩 (작성일 기준)
sh.shardCollection("codin-content-db.posts", { "createdAt": 1 });
```

## 10. 모니터링 및 알림

### 10.1 데이터베이스 메트릭 수집
```yaml
# Prometheus를 통한 MongoDB 메트릭 수집
mongodb_exporter:
  uri: mongodb://localhost:27017
  databases:
    - codin-auth-db
    - codin-user-db
    - codin-content-db
    - codin-notification-db
    - codin-chat-db
```

### 10.2 데이터 일관성 모니터링
```java
@Component
public class DataConsistencyMonitor {
    
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void checkDataConsistency() {
        // 서비스 간 데이터 일관성 검증
        // 불일치 발견 시 알림 발송
    }
}
```

## 11. 장애 대응 계획

### 11.1 서비스별 장애 격리
- 각 서비스의 데이터베이스 장애가 다른 서비스에 영향을 주지 않도록 격리
- Circuit Breaker 패턴으로 장애 전파 방지

### 11.2 데이터 복구 절차
1. 백업에서 데이터 복원
2. 증분 로그를 통한 최신 상태 복구
3. 서비스 간 데이터 동기화 재실행
4. 무결성 검증 후 서비스 재개
