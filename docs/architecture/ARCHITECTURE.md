# Trading Core System Architecture

## 전체 시스템 아키텍처

이 문서는 Trading Core System의 전체 아키텍처를 다이어그램과 설명으로 제공합니다.

---

## 시스템 개요

Trading Core System은 6개의 주요 모듈로 구성된 마이크로서비스 아키텍처를 따릅니다:

1. **데이터 수집 모듈** (Data Collection Module)
2. **데이터 가공 모듈** (Data Processing Module)
3. **코인 매매 모듈** (Trading Module)
4. **AI 모듈** (AI Module)
5. **API Logic 모듈** (API Module)
6. **리스크 관리 모듈** (Risk Management Module) ⭐ 신규

---

## 전체 시스템 아키텍처 다이어그램 (추상화 버전)

```mermaid
graph TB
    subgraph "외부 시스템"
        EXT[외부 데이터 소스<br/>바이낸스, 뉴스, 소셜미디어]
        USER[사용자<br/>프론트엔드]
    end

    subgraph "Trading Core System"
        COLLECT[데이터 수집 모듈]
        PROCESS[데이터 가공 모듈]
        AI[AI 모듈]
        TRADE[코인 매매 모듈]
        RISK[리스크 관리 모듈]
        API[API Logic 모듈]
        DB[(데이터 저장소)]
        CACHE[(캐시 레이어<br/>Redis)]
        TSDB[(시계열 DB<br/>TimescaleDB)]
    end

    %% 외부 시스템과의 상호작용
    EXT -->|데이터 수집| COLLECT
    TRADE -->|주문 실행| EXT
    USER <-->|API 요청/응답| API

    %% 내부 데이터 흐름
    COLLECT -->|원천 데이터 저장| DB
    COLLECT -->|시계열 데이터 저장| TSDB
    DB -->|데이터 조회| PROCESS
    TSDB -->|시계열 데이터 조회| PROCESS
    PROCESS -->|가공된 데이터| AI
    PROCESS -->|가공된 데이터| API
    PROCESS -->|캐시 저장| CACHE
    CACHE -->|캐시 조회| API
    AI -->|매매 신호| TRADE
    TRADE -->|리스크 검증 요청| RISK
    RISK -->|승인/거부| TRADE
    TRADE -->|거래 이력 저장| DB
    API -->|설정 저장/조회| DB
    DB -->|설정 조회| TRADE

    style COLLECT fill:#e1f5ff
    style PROCESS fill:#fff4e1
    style AI fill:#e1ffe1
    style TRADE fill:#ffe1f5
    style RISK fill:#ffcccc
    style API fill:#f5e1ff
    style DB fill:#ffe1e1
    style CACHE fill:#ffffcc
    style TSDB fill:#ccffcc
```

---

## 전체 시스템 아키텍처 다이어그램 (상세 버전)

```mermaid
graph TB
    subgraph "External Systems"
        BINANCE[바이낸스 API]
        NEWS[뉴스 소스<br/>RSS, 크롤링]
        SOCIAL[소셜 미디어<br/>트위터, 레딧]
        ONCHAIN[온체인 데이터<br/>체인 분석]
        FE[프론트엔드<br/>대시보드]
    end

    subgraph "Trading Core System"
        subgraph "데이터 수집 모듈"
            DC[Data Collector<br/>스케줄러]
            DC -->|수집| BINANCE
            DC -->|수집| NEWS
            DC -->|수집| SOCIAL
            DC -->|수집| ONCHAIN
        end

        subgraph "데이터 저장소 계층"
            DB[(PostgreSQL<br/>원천 데이터)]
            TSDB[(TimescaleDB<br/>시계열 데이터)]
            CACHE[(Redis<br/>캐시)]
        end

        subgraph "데이터 가공 모듈"
            DP_AI[AI용 데이터 가공<br/>기술적 지표 계산]
            DP_DASH[대시보드용 데이터 가공<br/>통계 집계]
        end

        subgraph "코인 매매 모듈"
            TM[Trade Manager<br/>포지션 관리]
            TM -->|주문 실행| BINANCE
        end

        subgraph "리스크 관리 모듈"
            RM[Risk Manager<br/>독립 모듈]
            RM -->|리스크 검증| TM
        end

        subgraph "AI 모듈"
            AI_JUDGE[AI 판단 엔진<br/>포지션 진입 판단]
            AI_CHAT[AI 챗봇<br/>질의응답]
        end

        subgraph "API Logic 모듈"
            API[REST API<br/>인증/권한]
            WS[WebSocket<br/>실시간 업데이트]
        end
    end

    %% 데이터 흐름
    DC -->|원천 데이터 저장| DB
    DC -->|시계열 데이터 저장| TSDB
    DB -->|조회| DP_AI
    TSDB -->|시계열 조회| DP_AI
    DB -->|조회| DP_DASH
    TSDB -->|시계열 조회| DP_DASH
    DP_DASH -->|캐시 저장| CACHE
    CACHE -->|캐시 조회| API

    %% AI 판단 흐름
    DP_AI -->|가공된 데이터| AI_JUDGE
    AI_JUDGE -->|매수/매도 신호| TM
    TM -->|리스크 검증 요청| RM
    RM -->|승인/거부| TM

    %% 대시보드 흐름
    DP_DASH -->|가공된 데이터| API
    API -->|HTTP/WebSocket| FE
    WS -->|실시간 업데이트| FE

    %% 챗봇 흐름
    FE -->|질문| API
    API -->|질문 전달| AI_CHAT
    DP_AI -->|데이터 조회| AI_CHAT
    AI_CHAT -->|답변| API
    API -->|답변| FE

    %% 사용자 설정
    FE -->|설정 변경| API
    API -->|설정 저장| DB
    DB -->|설정 조회| TM

    style DC fill:#e1f5ff
    style DP_AI fill:#fff4e1
    style DP_DASH fill:#fff4e1
    style TM fill:#ffe1f5
    style RM fill:#ffcccc
    style AI_JUDGE fill:#e1ffe1
    style AI_CHAT fill:#e1ffe1
    style API fill:#f5e1ff
    style WS fill:#f5e1ff
    style DB fill:#ffe1e1
    style TSDB fill:#ccffcc
    style CACHE fill:#ffffcc
```

---

## 모듈별 상세 아키텍처

### 1. 데이터 수집 모듈

```mermaid
graph LR
    subgraph "데이터 수집 모듈"
        SCHEDULER[스케줄러<br/>Cron/Quartz]
        BINANCE_COLLECTOR[바이낸스 수집기<br/>REST/WebSocket]
        NEWS_COLLECTOR[뉴스 수집기<br/>RSS/Crawler]
        SOCIAL_COLLECTOR[소셜 미디어 수집기<br/>API/Crawler]
        ONCHAIN_COLLECTOR[온체인 수집기<br/>API]
        VALIDATOR[데이터 검증기]
        STORAGE[데이터 저장]
    end

    SCHEDULER --> BINANCE_COLLECTOR
    SCHEDULER --> NEWS_COLLECTOR
    SCHEDULER --> SOCIAL_COLLECTOR
    SCHEDULER --> ONCHAIN_COLLECTOR

    BINANCE_COLLECTOR --> VALIDATOR
    NEWS_COLLECTOR --> VALIDATOR
    SOCIAL_COLLECTOR --> VALIDATOR
    ONCHAIN_COLLECTOR --> VALIDATOR

    VALIDATOR --> STORAGE
```

**주요 컴포넌트**:
- **스케줄러**: 주기적 데이터 수집 트리거
- **수집기들**: 각 외부 소스별 전용 수집기
- **검증기**: 데이터 품질 검증
- **저장소**: PostgreSQL에 원천 데이터 저장

---

### 2. 데이터 가공 모듈

```mermaid
graph TB
    subgraph "데이터 가공 모듈"
        INPUT[원천 데이터 입력]
        
        subgraph "AI용 가공 파이프라인"
            TECH_IND[기술적 지표 계산<br/>RSI, MACD, 볼린저 밴드]
            NORMALIZE[데이터 정규화]
            VECTORIZE[벡터화<br/>뉴스/감정 데이터]
            AI_FORMAT[AI 입력 형식 변환]
        end

        subgraph "대시보드용 가공 파이프라인"
            AGGREGATE[데이터 집계]
            STATS[통계 계산<br/>수익률, 승률]
            CHART_DATA[차트 데이터 생성]
            CACHE[캐싱]
        end

        OUTPUT_AI[AI 모듈 출력]
        OUTPUT_DASH[대시보드 출력]
    end

    INPUT --> TECH_IND
    INPUT --> AGGREGATE

    TECH_IND --> NORMALIZE
    NORMALIZE --> VECTORIZE
    VECTORIZE --> AI_FORMAT
    AI_FORMAT --> OUTPUT_AI

    AGGREGATE --> STATS
    STATS --> CHART_DATA
    CHART_DATA --> CACHE
    CACHE --> OUTPUT_DASH
```

**주요 컴포넌트**:
- **AI용 파이프라인**: 기술적 지표 계산 및 AI 입력 형식 변환
- **대시보드용 파이프라인**: 사용자 친화적 데이터 집계 및 캐싱

---

### 3. 코인 매매 모듈

```mermaid
graph TB
    subgraph "코인 매매 모듈"
        SIGNAL[AI 신호 수신<br/>매수/매도/보류]
        ORDER_MANAGER[주문 관리자]
        POSITION_TRACKER[포지션 추적기]
        RETRY[재시도 로직<br/>Exponential Backoff]
        CIRCUIT[서킷 브레이커<br/>Resilience4j]
        
        BINANCE_API[바이낸스 API<br/>주문 실행]
    end

    subgraph "리스크 관리 모듈"
        RISK_VALIDATOR[리스크 검증기]
        POSITION_SIZE[포지션 크기 계산]
        STOP_LOSS[손절가 계산]
        TAKE_PROFIT[익절가 계산]
        RISK_CHECK[리스크 한도 검사]
    end

    SIGNAL --> ORDER_MANAGER
    ORDER_MANAGER -->|리스크 검증 요청| RISK_VALIDATOR
    RISK_VALIDATOR --> POSITION_SIZE
    POSITION_SIZE --> STOP_LOSS
    STOP_LOSS --> TAKE_PROFIT
    TAKE_PROFIT --> RISK_CHECK
    RISK_CHECK -->|통과| ORDER_MANAGER
    RISK_CHECK -->|실패| REJECT[주문 거부]
    
    ORDER_MANAGER --> CIRCUIT
    CIRCUIT --> RETRY
    RETRY --> BINANCE_API
    BINANCE_API --> POSITION_TRACKER
    POSITION_TRACKER -->|상태 업데이트| DB[(데이터베이스)]
```

**주요 컴포넌트**:
- **주문 관리자**: 바이낸스 API를 통한 주문 실행
- **포지션 추적기**: 활성 포지션 모니터링 및 청산 관리
- **재시도 로직**: Exponential Backoff를 통한 실패 시 재시도
- **서킷 브레이커**: 외부 API 장애 시 자동 차단

**리스크 관리 모듈** (독립 모듈):
- **리스크 검증기**: 매매 전 리스크 검증
- **포지션 크기 계산**: 리스크 기반 포지션 크기 결정
- **손절/익절 계산**: 동적 손절/익절가 계산
- **리스크 한도 검사**: 일일/전체 손실 한도 검증

---

### 4. 리스크 관리 모듈

```mermaid
graph TB
    subgraph "리스크 관리 모듈"
        REQUEST[리스크 검증 요청]
        
        subgraph "검증 파이프라인"
            POSITION_CALC[포지션 크기 계산<br/>잔고 기반]
            STOP_LOSS_CALC[손절가 계산<br/>변동성 기반]
            TAKE_PROFIT_CALC[익절가 계산<br/>리스크/보상 비율]
            DAILY_LIMIT[일일 손실 한도 검사]
            TOTAL_LIMIT[전체 손실 한도 검사]
            POSITION_LIMIT[최대 포지션 수 검사]
        end
        
        AUDIT[감사 로그 기록]
        RESPONSE[승인/거부 응답]
    end

    REQUEST --> POSITION_CALC
    POSITION_CALC --> STOP_LOSS_CALC
    STOP_LOSS_CALC --> TAKE_PROFIT_CALC
    TAKE_PROFIT_CALC --> DAILY_LIMIT
    DAILY_LIMIT --> TOTAL_LIMIT
    TOTAL_LIMIT --> POSITION_LIMIT
    POSITION_LIMIT --> AUDIT
    AUDIT --> RESPONSE
```

**주요 컴포넌트**:
- **포지션 크기 계산**: 잔고, 리스크 파라미터 기반 포지션 크기 결정
- **손절/익절 계산**: 변동성, 리스크/보상 비율 기반 동적 계산
- **손실 한도 검사**: 일일/전체 손실 한도 초과 여부 검증
- **포지션 수 제한**: 최대 동시 포지션 수 제한
- **감사 로그**: 모든 리스크 검증 결과 기록

**장점**:
- 리스크 정책의 독립적 관리 및 변경
- 여러 매매 전략에서 재사용 가능
- 리스크 관리 로직 테스트 용이

---

### 5. AI 모듈

```mermaid
graph TB
    subgraph "AI 모듈"
        subgraph "포지션 판단 엔진"
            JUDGE_INPUT[가공된 데이터 입력]
            AI_MODEL[AI 모델<br/>판단 로직]
            SIGNAL_GEN[신호 생성<br/>매수/매도/보류]
            CONFIDENCE[신뢰도 점수]
            REASONING[판단 근거 생성]
        end

        subgraph "챗봇 엔진"
            CHAT_INPUT[사용자 질문]
            NLP[자연어 처리]
            CONTEXT[컨텍스트 관리]
            DATA_LOOKUP[데이터 조회]
            RESPONSE_GEN[답변 생성]
        end

        MODEL_MANAGER[모델 관리자<br/>로딩/캐싱/버전]
    end

    JUDGE_INPUT --> AI_MODEL
    AI_MODEL --> SIGNAL_GEN
    AI_MODEL --> CONFIDENCE
    AI_MODEL --> REASONING

    CHAT_INPUT --> NLP
    NLP --> CONTEXT
    CONTEXT --> DATA_LOOKUP
    DATA_LOOKUP --> RESPONSE_GEN

    MODEL_MANAGER --> AI_MODEL
    MODEL_MANAGER --> RESPONSE_GEN
```

**주요 컴포넌트**:
- **포지션 판단 엔진**: AI 기반 매매 신호 생성
- **챗봇 엔진**: 사용자 질의응답 처리
- **모델 관리자**: AI 모델 라이프사이클 관리

---

### 6. API Logic 모듈

```mermaid
graph TB
    subgraph "API Logic 모듈"
        AUTH[인증/권한 관리<br/>JWT/OAuth]
        
        subgraph "REST API"
            DASHBOARD_API[대시보드 API<br/>GET /api/dashboard/*]
            CHAT_API[챗봇 API<br/>POST /api/chat]
            CONFIG_API[설정 API<br/>POST /api/config]
            TRADE_API[거래 이력 API<br/>GET /api/trades]
        end

        subgraph "WebSocket"
            WS_SERVER[WebSocket 서버]
            WS_HANDLER[실시간 업데이트 핸들러]
        end

        VALIDATOR_API[요청 검증]
        ERROR_HANDLER[에러 처리]
    end

    FE[프론트엔드] --> AUTH
    AUTH --> VALIDATOR_API
    VALIDATOR_API --> DASHBOARD_API
    VALIDATOR_API --> CHAT_API
    VALIDATOR_API --> CONFIG_API
    VALIDATOR_API --> TRADE_API
    
    FE --> WS_SERVER
    WS_SERVER --> WS_HANDLER

    DASHBOARD_API --> ERROR_HANDLER
    CHAT_API --> ERROR_HANDLER
    CONFIG_API --> ERROR_HANDLER
    TRADE_API --> ERROR_HANDLER
```

**주요 컴포넌트**:
- **인증/권한**: 사용자 인증 및 권한 관리
- **REST API**: HTTP 기반 API 엔드포인트
- **WebSocket**: 실시간 데이터 스트리밍
- **검증 및 에러 처리**: 요청 검증 및 에러 핸들링

---

## 데이터 흐름도

### 자동 매매 실행 흐름

```mermaid
sequenceDiagram
    participant Scheduler
    participant DataCollector
    participant DataProcessor
    participant AIModule
    participant TradingModule
    participant BinanceAPI

    Scheduler->>DataCollector: 트리거 (1분마다)
    DataCollector->>BinanceAPI: 차트 데이터 요청
    BinanceAPI-->>DataCollector: 차트 데이터 반환
    DataCollector->>DataCollector: 데이터 검증 및 저장

    DataCollector->>DataProcessor: 새 데이터 알림
    DataProcessor->>DataProcessor: AI용 데이터 가공
    DataProcessor->>AIModule: 가공된 데이터 전달

    AIModule->>AIModule: AI 모델 분석
    AIModule-->>TradingModule: 매수/매도 신호 반환

    alt 신호가 매수/매도인 경우
        TradingModule->>TradingModule: 리스크 계산
        TradingModule->>BinanceAPI: 주문 실행
        BinanceAPI-->>TradingModule: 주문 결과
        TradingModule->>TradingModule: 포지션 업데이트
    else 신호가 보류인 경우
        TradingModule->>TradingModule: 대기
    end
```

### 대시보드 조회 흐름

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant APIModule
    participant DataProcessor
    participant Database

    User->>Frontend: 대시보드 접속
    Frontend->>APIModule: GET /api/dashboard/data
    APIModule->>APIModule: 인증 확인
    
    APIModule->>DataProcessor: 대시보드용 데이터 요청
    DataProcessor->>Database: 데이터 조회
    Database-->>DataProcessor: 원천 데이터 반환
    DataProcessor->>DataProcessor: 데이터 가공 (집계, 통계)
    DataProcessor-->>APIModule: 가공된 데이터 반환
    
    APIModule-->>Frontend: JSON 응답
    Frontend-->>User: 대시보드 렌더링

    loop 실시간 업데이트
        DataProcessor->>APIModule: WebSocket 이벤트
        APIModule->>Frontend: 실시간 데이터 푸시
        Frontend->>User: UI 업데이트
    end
```

### AI 챗봇 질의응답 흐름

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant APIModule
    participant AIModule
    participant DataProcessor

    User->>Frontend: 질문 입력 ("비트코인 추세는?")
    Frontend->>APIModule: POST /api/chat
    APIModule->>APIModule: 인증 확인
    
    APIModule->>AIModule: 질문 전달
    AIModule->>AIModule: 자연어 처리
    
    AIModule->>DataProcessor: 관련 데이터 요청
    DataProcessor-->>AIModule: 가공된 데이터 반환
    
    AIModule->>AIModule: AI 모델로 답변 생성
    AIModule-->>APIModule: 답변 반환
    
    APIModule->>APIModule: 대화 히스토리 저장
    APIModule-->>Frontend: 답변 JSON
    Frontend-->>User: 답변 표시
```

---

## 모듈 간 의존성

```mermaid
graph TD
    API[API Logic 모듈]
    AI[AI 모듈]
    TRADE[코인 매매 모듈]
    RISK[리스크 관리 모듈]
    PROCESS[데이터 가공 모듈]
    COLLECT[데이터 수집 모듈]
    DB[(데이터베이스)]
    TSDB[(시계열 DB)]
    CACHE[(캐시)]

    API --> AI
    API --> PROCESS
    API --> TRADE
    
    AI --> PROCESS
    TRADE --> AI
    TRADE --> RISK
    TRADE --> DB
    
    PROCESS --> DB
    PROCESS --> TSDB
    PROCESS --> CACHE
    COLLECT --> DB
    COLLECT --> TSDB

    style API fill:#f9f,stroke:#333,stroke-width:2px
    style AI fill:#9f9,stroke:#333,stroke-width:2px
    style TRADE fill:#ff9,stroke:#333,stroke-width:2px
    style RISK fill:#fcc,stroke:#333,stroke-width:2px
    style PROCESS fill:#9ff,stroke:#333,stroke-width:2px
    style COLLECT fill:#99f,stroke:#333,stroke-width:2px
```

**의존성 규칙**:
- API Logic 모듈은 다른 모든 모듈에 의존 가능
- AI 모듈은 데이터 가공 모듈에만 의존
- 코인 매매 모듈은 AI 모듈, 리스크 관리 모듈, 데이터베이스에 의존
- 리스크 관리 모듈은 독립적 (다른 모듈에 의존하지 않음)
- 데이터 가공 모듈은 데이터베이스, 시계열 DB, 캐시에 의존
- 데이터 수집 모듈은 데이터베이스, 시계열 DB에만 의존

---

## 기술 스택 매핑

| 모듈 | 주요 기술 | 설명 |
|------|----------|------|
| 데이터 수집 모듈 | Kotlin, Spring Scheduler, HTTP Client, Resilience4j | 스케줄링 및 외부 API 호출, 재시도 로직 |
| 데이터 가공 모듈 | Kotlin, Spring Batch (선택), 계산 라이브러리 | 데이터 변환 및 집계 |
| 코인 매매 모듈 | Kotlin, HTTP Client, 상태 머신, Resilience4j | 주문 실행 및 포지션 관리, 서킷 브레이커 |
| 리스크 관리 모듈 | Kotlin, Spring Modulith | 리스크 검증 및 정책 관리 |
| AI 모듈 | Kotlin, AI 모델 라이브러리 (구현 시 결정) | AI 추론 및 자연어 처리 |
| API Logic 모듈 | Kotlin, Spring Web MVC, WebSocket, Spring Security | REST API 및 실시간 통신, 인증/권한 |
| 데이터 저장소 | PostgreSQL, TimescaleDB, Redis | 원천 데이터, 시계열 데이터, 캐시 |
| 설정 관리 | Spring Cloud Config (선택) | 중앙화된 설정 관리 |
| 모니터링 | Prometheus, Grafana, OpenTelemetry | 메트릭 수집 및 시각화, 분산 추적 |
| 로깅 | Logback, JSON 로깅 | 구조화된 로깅 |
| 보안 | Spring Security, Vault (선택) | 인증/권한, 비밀 관리 |

---

## 운영 인프라

### 모니터링 및 로깅

```mermaid
graph TB
    subgraph "애플리케이션"
        APP[모든 모듈]
    end

    subgraph "모니터링 스택"
        PROMETHEUS[Prometheus<br/>메트릭 수집]
        GRAFANA[Grafana<br/>시각화]
        OTEL[OpenTelemetry<br/>분산 추적]
    end

    subgraph "로깅 스택"
        LOG[구조화된 로깅<br/>JSON 형식]
        LOG_AGG[로그 수집기<br/>ELK/Loki]
    end

    subgraph "알림"
        ALERT[PagerDuty/Slack<br/>알림]
    end

    APP -->|메트릭| PROMETHEUS
    APP -->|트레이스| OTEL
    APP -->|로그| LOG
    PROMETHEUS --> GRAFANA
    PROMETHEUS --> ALERT
    LOG --> LOG_AGG
```

**주요 메트릭**:
- 시스템 메트릭: CPU, 메모리, 디스크
- 애플리케이션 메트릭: API 응답 시간, 에러율, 처리량
- 비즈니스 메트릭: 거래 수, 수익률, 포지션 수

**로깅 전략**:
- 구조화된 JSON 로깅
- 로그 레벨: ERROR, WARN, INFO, DEBUG
- 민감 정보 마스킹
- 모든 거래 및 리스크 검증 로깅

---

### 설정 관리

```mermaid
graph LR
    subgraph "설정 저장소"
        CONFIG_REPO[Git 저장소<br/>또는 환경 변수]
        CONFIG_SERVER[Spring Cloud Config<br/>선택 사항]
    end

    subgraph "애플리케이션"
        APP1[데이터 수집]
        APP2[코인 매매]
        APP3[API Logic]
    end

    CONFIG_REPO --> CONFIG_SERVER
    CONFIG_SERVER --> APP1
    CONFIG_SERVER --> APP2
    CONFIG_SERVER --> APP3
```

**설정 관리 전략**:
- 환경별 설정 분리: local, sandbox, production
- 동적 설정 변경: `/actuator/refresh` 엔드포인트
- 민감 정보 암호화: Spring Cloud Config 암호화 기능
- 설정 버전 관리: Git을 통한 설정 변경 이력

---

### 에러 처리 및 재시도 전략

**재시도 전략**:
- Exponential Backoff: 1초, 2초, 4초, 8초...
- 최대 재시도 횟수: 3회
- 재시도 가능한 에러: 네트워크 타임아웃, 5xx 에러
- 재시도 불가능한 에러: 4xx 에러, 인증 실패

**서킷 브레이커**:
- Resilience4j 사용
- 실패율 임계값: 50%
- 서킷 열림 시간: 60초
- 반개방 상태에서 점진적 복구

**데드 레터 큐 (DLQ)**:
- 실패한 작업 저장
- 수동 재처리 가능
- 실패 원인 분석

---

### 보안 강화

```mermaid
graph TB
    subgraph "보안 계층"
        AUTH[인증/권한<br/>Spring Security]
        SECRET[비밀 관리<br/>Vault/환경 변수]
        ENCRYPT[암호화<br/>저장/전송]
        AUDIT[감사 로그<br/>모든 중요 작업]
    end

    subgraph "애플리케이션"
        APP[모든 모듈]
    end

    AUTH --> APP
    SECRET --> APP
    ENCRYPT --> APP
    APP --> AUDIT
```

**보안 전략**:
- **API 키 관리**: HashiCorp Vault 또는 환경 변수
- **데이터 암호화**: 
  - 저장 시 암호화 (encryption at rest)
  - 전송 시 암호화 (TLS)
- **감사 로그**: 모든 중요한 작업 기록
  - 매매 실행
  - 설정 변경
  - 리스크 검증 결과
  - 사용자 인증/권한 변경

---

### 데이터 저장소 계층화

```mermaid
graph TB
    subgraph "데이터 저장소 계층"
        APP[애플리케이션]
        CACHE[Redis<br/>캐시 레이어<br/>대시보드 데이터]
        TSDB[TimescaleDB<br/>시계열 데이터<br/>차트, 가격 틱]
        PG[PostgreSQL<br/>원천 데이터<br/>거래 이력, 설정]
    end

    APP --> CACHE
    APP --> TSDB
    APP --> PG
```

**데이터 저장 전략**:
- **원천 데이터 (PostgreSQL)**: 거래 이력, 설정, 사용자 정보
- **시계열 데이터 (TimescaleDB)**: 차트 데이터, 가격 틱, 기술적 지표
- **캐시 (Redis)**: 대시보드 데이터, 실시간 지표, 세션 데이터

**장점**:
- 각 저장소의 특성에 맞는 최적화
- 실시간 조회 성능 향상
- 데이터베이스 부하 분산

---

## 확장성 고려사항

### 수평 확장
- 각 모듈은 독립적으로 확장 가능
- 로드 밸런서를 통한 다중 인스턴스 운영
- 상태 없는(stateless) 설계 권장

### 데이터베이스 확장
- 읽기 전용 복제본 활용
- 샤딩 전략 (코인별, 시간별)
- 캐싱 레이어 활용 (Redis)

### 메시징 큐 도입 (향후)
- 모듈 간 비동기 통신
- 이벤트 기반 아키텍처로 전환 가능
- RabbitMQ, Kafka 등 고려

---

## 테스트 전략

### 단위 테스트
- 각 모듈의 핵심 로직 테스트
- Mock을 활용한 의존성 격리
- 코드 커버리지 목표: 80% 이상

### 통합 테스트
- 모듈 간 상호작용 테스트
- Testcontainers를 활용한 데이터베이스 테스트
- 외부 API Mock 서버 활용

### E2E 테스트
- 전체 시나리오 테스트
- 실제 바이낸스 샌드박스 환경 활용
- 페이퍼 트레이딩 모드

### 백테스팅
- 과거 데이터로 전략 검증
- 성능 지표 계산 (수익률, 샤프 비율 등)

