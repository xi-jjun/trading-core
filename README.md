# Trading Core System

AI 기반 자동화 암호화폐 트레이딩 플랫폼

## 📋 프로젝트 개요

Trading System은 거래소(Binance) API를 활용하여 실시간 데이터 수집, 분석, 자동 매매를 수행하는 백엔드 시스템입니다.

### 주요 기능

- 실시간 암호화폐 시장 데이터 수집 및 분석
- AI 기반 매매 의사결정 지원
- 자동화된 포지션 진입/청산 실행
- 사용자 대시보드를 통한 투명한 트레이딩 모니터링

## 🛠️ 기술 스택

### 백엔드

- **언어**: Kotlin 2.3.0
- **프레임워크**: Spring Boot 4.0.1
- **모듈화**: Spring Modulith 2.0.1
- **데이터베이스**: PostgreSQL
- **마이그레이션**: Flyway
- **Java**: 25

### 주요 의존성

- Spring Web MVC
- Spring Data JDBC / JPA
- Spring Modulith
- Jackson (Kotlin 지원)
- PostgreSQL Driver

## 📁 프로젝트 구조

- [ARCHITECTURE Desc](docs/architecture/ARCHITECTURE.md) : 전체 아키텍처 설명

## 📚 문서

프로젝트의 상세한 아키텍처, 개발 가이드, 요구사항 등은 `docs/` 디렉토리에 정리되어 있습니다.

### 📖 필수 문서

#### 요구사항 및 계획

- **[PRD.md](docs/PRD.md)**: Product Requirements Document
    - 시스템 개요 및 목적
    - 모듈별 상세 설명
    - Use Cases
    - 비기능 요구사항

- **[MVP_SCOPE.md](docs/MVP_SCOPE.md)**: MVP 범위 정의
    - 모듈별 MVP 포함/제외 기능
    - MVP 아키텍처 다이어그램
    - 개발 우선순위 및 성공 기준

- **[MVP_TASKS.md](docs/MVP_TASKS.md)**: MVP 개발 태스크 목록
    - 46개 최소 단위 태스크
    - 10개 Phase로 구성
    - 우선순위별 분류 및 예상 일정

#### 아키텍처 문서

- **[ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md)**: 전체 시스템 아키텍처
    - 추상화/상세 버전 다이어그램
    - 모듈별 상세 아키텍처
    - 데이터 흐름도
    - 모듈 간 의존성

- **[DESIGN_REVIEW.md](docs/architecture/DESIGN_REVIEW.md)**: 설계 적절성 검토
    - 설계 강점 분석
    - 개선 제안 및 로드맵
    - 전체 평가

- **[DATABASE_STRATEGY.md](docs/architecture/DATABASE_STRATEGY.md)**: 데이터베이스 전략
    - TimescaleDB와 PostgreSQL 관계
    - 아키텍처 옵션
    - MVP → Phase 2 마이그레이션 가이드

#### 개발 가이드

- **[DB_MIGRATION_GUIDE.md](docs/develop-guide/db-migration/DB_MIGRATION_GUIDE.md)**: DB 마이그레이션 가이드
    - Flyway 사용 방법
    - 마이그레이션 파일 작성 규칙

- **[BRANCH_AND_COMMIT_GUIDE.md](docs/develop-guide/git-collaboration/BRANCH_AND_COMMIT_GUIDE.md)**: 브랜치 및 커밋 가이드
    - 브랜치 네이밍 규칙 (`#{ISSUE_NUM}_{FUNCTION_DESC}`)
    - 커밋 메시지 규칙 (`티켓 번호 : 설명`)
    - 개발 워크플로우

- **[timescaledb-guide.md](docs/basic-knowledge/timescaledb-guide.md)**: TimescaleDB 가이드
    - TimescaleDB 개념 및 활용
    - 트레이딩 시스템에서의 활용 예시

### 📂 문서 디렉토리 구조

```
docs/
├── PRD.md                              # 요구사항 정의 문서
├── MVP_SCOPE.md                        # MVP 범위 정의
├── MVP_TASKS.md                        # MVP 개발 태스크
├── architecture/                        # 아키텍처 문서
│   ├── ARCHITECTURE.md                 # 전체 아키텍처
│   ├── DESIGN_REVIEW.md                # 설계 검토
│   └── DATABASE_STRATEGY.md            # DB 전략
├── develop-guide/                      # 개발 가이드
│   ├── DB_MIGRATION_GUIDE.md           # DB 마이그레이션 가이드
│   └── BRANCH_AND_COMMIT_GUIDE.md      # 브랜치 및 커밋 가이드
└── basic-knowledge/                    # 기본 지식
    └── timescaledb-guide.md            # TimescaleDB 가이드
```

## 🌿 브랜치 및 커밋 규칙

브랜치 네이밍 및 커밋 메시지 규칙은 [브랜치 및 커밋 가이드](docs/develop-guide/git-collaboration/BRANCH_AND_COMMIT_GUIDE.md)를 참고하세요.

**요약**:

- **브랜치 네이밍**: `#{ISSUE_NUM}_{FUNCTION_DESC}` 형식 (예: `feature/#10_implement_data_collection`)
- **커밋 메시지**: `티켓 번호 : 설명` 형식 (예: `#10 : 데이터 수집 모듈 구현`)

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- PostgreSQL
- Gradle (또는 Gradle Wrapper 사용)

### 환경 설정

1. **환경 변수 설정**
   ```bash
   cp env.sample .env
   # .env 파일을 수정하여 필요한 환경 변수 설정
   ```

2. **데이터베이스 설정**
    - `src/main/resources/application-local.properties` 또는
    - `src/main/resources/application-sandbox.properties`에서
    - 데이터베이스 연결 정보 설정

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

### 프로파일 선택

- `local`: 로컬 개발 환경
- `sandbox`: 샌드박스 환경

프로파일은 환경 변수 `SPRING_PROFILES_ACTIVE`로 설정하거나 `application.properties`에서 기본값 설정 가능합니다.

## 🏗️ 시스템 아키텍처

자세한 아키텍처는 [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md)를 참고하세요.

---

**⚠️ 중요**: 프로젝트 시작 전 반드시 `docs/` 디렉토리의 문서들을 확인하세요. 특히 다음 문서를 우선적으로 읽어보시기 바랍니다:

1. [PRD.md](docs/PRD.md) - 시스템 전체 요구사항
2. [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) - 시스템 아키텍처
3. [MVP_SCOPE.md](docs/MVP_SCOPE.md) - MVP 범위 및 계획

