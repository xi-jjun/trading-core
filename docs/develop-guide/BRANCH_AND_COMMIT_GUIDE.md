# 브랜치 및 커밋 가이드

## 🌿 브랜치 전략

### 브랜치 네이밍 규칙

개발 시 브랜치를 생성할 때는 다음 형식을 따라야 합니다:

```
#{ISSUE_NUM}_{FUNCTION_DESC}
```

**형식 설명**:
- `#{ISSUE_NUM}`: 이슈 번호 (예: #3)
- `{FUNCTION_DESC}`: 기능 설명 (영문, 소문자, 언더스코어 구분)

**예시**:
```bash
# 올바른 예시
feature/#3_add_architecture_docs
feature/#5_implement_data_collection
bugfix/#12_fix_order_execution_error
hotfix/#15_critical_security_patch

# 잘못된 예시
feature/add-architecture-docs        # 이슈 번호 없음
feature/#3AddArchitectureDocs        # 언더스코어 미사용
feature/3_add_architecture_docs     # # 기호 누락
```

**브랜치 타입**:
- `feature/`: 새로운 기능 개발
- `bugfix/`: 버그 수정
- `hotfix/`: 긴급 수정
- `refactor/`: 리팩토링

**브랜치 생성 예시**:
```bash
# 이슈 #10: 데이터 수집 모듈 구현
git checkout -b feature/#10_implement_data_collection

# 이슈 #25: 주문 실행 버그 수정
git checkout -b bugfix/#25_fix_order_execution_error

# 이슈 #30: 긴급 보안 패치
git checkout -b hotfix/#30_critical_security_patch
```

---

## 📝 커밋 메시지 규칙

### 커밋 메시지 형식

커밋 메시지는 다음 형식을 따라야 합니다:

```
티켓 번호 : 설명
```

**형식 설명**:
- `티켓 번호`: 이슈 번호 (예: #3, #10)
- `:`: 콜론과 공백으로 구분
- `설명`: 변경 사항에 대한 간결한 설명 (한글 또는 영문)

**예시**:
```bash
# 올바른 예시
#3 : 아키텍처 문서 추가
#10 : 데이터 수집 모듈 구현
#12 : 주문 실행 버그 수정
#15 : 긴급 보안 패치 적용

# 잘못된 예시
아키텍처 문서 추가                    # 티켓 번호 없음
#3아키텍처 문서 추가                  # 콜론과 공백 누락
#3: 아키텍처 문서 추가                # 공백 없음 (허용 가능하지만 일관성 유지)
Add architecture docs                # 티켓 번호 없음
```

### 커밋 메시지 작성 가이드

#### 1. 간결하고 명확하게
- ✅ 좋은 예: `#10 : 데이터 수집 모듈 구현`
- ❌ 나쁜 예: `#10 : 데이터 수집 모듈을 구현했습니다. 바이낸스 API를 연동하고 캔들스틱 데이터를 수집하는 기능을 추가했습니다.`

#### 2. 동사형으로 시작 (선택사항)
- ✅ 좋은 예: `#10 : 데이터 수집 모듈 구현`
- ✅ 좋은 예: `#10 : 데이터 수집 모듈 추가`
- ❌ 나쁜 예: `#10 : 데이터 수집 모듈`

#### 3. 한글이나 영문 일관성 유지
- 프로젝트에서 한글을 사용한다면 한글로 통일
- 영문을 사용한다면 영문으로 통일

#### 4. 여러 변경사항이 있는 경우
```bash
# 여러 파일 수정이지만 하나의 이슈인 경우
#10 : 데이터 수집 모듈 구현

# 여러 이슈를 한 번에 처리한 경우 (권장하지 않음)
#10 : 데이터 수집 모듈 구현
#11 : 데이터 가공 모듈 구현
```

**권장**: 하나의 커밋은 하나의 이슈에 대응하는 것이 좋습니다.

### 커밋 예시

```bash
# 기능 추가
git commit -m "#10 : 데이터 수집 모듈 구현"

# 버그 수정
git commit -m "#25 : 주문 실행 시 null pointer 예외 수정"

# 문서 추가
git commit -m "#3 : 아키텍처 문서 추가"

# 리팩토링
git commit -m "#30 : 리스크 관리 모듈 코드 리팩토링"

# 설정 변경
git commit -m "#15 : 데이터베이스 연결 설정 수정"
```

---

## 🔄 브랜치 및 커밋 워크플로우

### 일반적인 개발 워크플로우

1. **이슈 생성**
   - GitHub/GitLab에서 이슈 생성
   - 이슈 번호 확인 (예: #10)

2. **브랜치 생성**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/#10_implement_data_collection
   ```

3. **개발 및 커밋**
   ```bash
   # 작업 진행
   git add .
   git commit -m "#10 : 데이터 수집 모듈 구현"
   
   # 추가 작업
   git add .
   git commit -m "#10 : 바이낸스 API 클라이언트 추가"
   ```

4. **푸시 및 PR 생성**
   ```bash
   git push origin feature/#10_implement_data_collection
   # GitHub/GitLab에서 Pull Request 생성
   ```

5. **PR 머지 후 브랜치 삭제**
   ```bash
   git checkout develop
   git pull origin develop
   git branch -d feature/#10_implement_data_collection
   ```

---

## ✅ 체크리스트

브랜치 생성 전:
- [ ] 이슈가 생성되어 있고 번호를 확인했는가?
- [ ] 브랜치 이름이 `#{ISSUE_NUM}_{FUNCTION_DESC}` 형식을 따르는가?
- [ ] 브랜치 타입(feature/bugfix/hotfix/refactor)이 적절한가?

커밋 전:
- [ ] 커밋 메시지가 `티켓 번호 : 설명` 형식을 따르는가?
- [ ] 커밋 메시지가 간결하고 명확한가?
- [ ] 하나의 커밋에 하나의 논리적 변경만 포함되어 있는가?

---

## 📚 참고사항

- 브랜치와 커밋 메시지는 프로젝트의 히스토리를 추적하는 데 중요한 역할을 합니다
- 일관된 네이밍 규칙을 따르면 코드 리뷰와 버그 추적이 쉬워집니다
- 이슈 번호를 포함하면 GitHub/GitLab에서 자동으로 이슈와 연결됩니다

