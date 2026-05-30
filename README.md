# FNY_Server

## Analysis Candidate Filter

최근 30일 메일 전체를 그대로 LLM에 보내지 않고, 서버에서 1차 필터를 수행한 뒤 분석 후보만 `analysis_jobs`로 넘긴다.

### 기본 원칙

- 최근 30일 이내 메일만 분석 후보로 본다.
- 메일함은 전체 메일을 보여주되, 분석은 업무 신호가 있는 메일에만 집중한다.
- 광고성, 자동 알림, 뉴스레터 같은 저가치 메일은 우선 제외한다.
- 회신 요청, 승인 요청, 계약, 정산, 일정, 장애, 긴급 대응 같은 업무 신호는 강하게 가산한다.

### 가산 신호

- 읽지 않음
- 중요 표시
- 중요도 헤더 `high`
- 첨부파일 존재
- 최근 3일 이내 수신
- 직접 수신자 포함
- 액션 키워드 포함
- 회의/일정 키워드 포함
- 장애/긴급 키워드 포함

### 감점 또는 제외 신호

- 30일 초과 수신 메일: `OLD_MAIL`
- 프로모션/소셜/포럼 카테고리
- `no-reply`, `newsletter`, `notification` 계열 발신자
- 뉴스레터, 프로모션, 광고, 인증번호, 로그인 알림 계열 본문
- 본인 발신 메일은 소폭 감점
- 저가치 신호가 강하고 액션 신호가 없으면 `AUTOMATED_OR_PROMOTIONAL`
- 점수가 임계치보다 낮으면 `LOW_SIGNAL`

### 구현 위치

- 후보 판정 로직: `src/main/java/com/mailservice/fny/mailbox/service/AnalysisCandidateEvaluator.java`
- Gmail 동기화 연계: `src/main/java/com/mailservice/fny/mailbox/service/GmailSyncService.java`
- 테스트: `src/test/java/com/mailservice/fny/mailbox/service/AnalysisCandidateEvaluatorTest.java`

## Prompt / Agent Contract

LLM 프롬프트는 `prompt_templates` 테이블에서 관리한다.

- 메일 분석 프롬프트: `prompt_code=EMAIL_ANALYSIS`, `prompt_type=ANALYSIS`
- 보고서 생성 프롬프트: `prompt_code=WEEKLY_REPORT`, `prompt_type=REPORT`

각 프롬프트는 아래 4개 섹션으로 분리한다.

- `role_content`
- `policy_content`
- `guide_content`
- `output_content`

서버는 활성 버전의 프롬프트를 조회해 Agent 요청에 포함한다.

### `/analyze` request

- `email_id`
- `subject`
- `body_text`
- `from_email`
- `received_at`
- `language`
- `prompt`

### `/analyze` response

- `short_summary`
- `detailed_summary`
- `category`
- `priority_level`
- `importance_score`
- `urgency_score`
- `confidence_score`
- `needs_reply`
- `has_deadline`
- `deadline_at`
- `deadline_text`
- `time_sensitivity`
- `requires_action`
- `user_task_summary`
- `priority_reason_codes`
- `suggested_action`
- `reasoning`
- `action_items`
- `model_name`
- `prompt_version`

### Standard analysis codes

`priority_reason_codes`는 아래 값만 사용한다.

- `NEEDS_REPLY`
- `HAS_DEADLINE`
- `URGENT_KEYWORD`
- `DIRECT_TO_ME`
- `IMPORTANT_HEADER`
- `ATTACHMENT`
- `FINANCE_RELATED`
- `MEETING_RELATED`
- `APPROVAL_REQUIRED`
- `CUSTOMER_OR_CONTRACT`
- `NO_STRONG_SIGNAL`

`action_items.action_type`은 아래 값만 사용한다.

- `REPLY`
- `REVIEW`
- `APPROVE`
- `SCHEDULE`
- `PAYMENT`
- `FOLLOW_UP`
- `ARCHIVE`

### `/analyze-weekly` response

- `executive_summary`
- `highlights`
- `risks_blockers`
- `pending_decisions`
- `next_week_suggestions`
- `thread_summaries`
- `model_name`
- `prompt_version`
