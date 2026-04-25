-- =========================================================
-- RESET (DROP TABLES)
-- =========================================================
DROP TABLE IF EXISTS email_labels;
DROP TABLE IF EXISTS labels;
DROP TABLE IF EXISTS analysis_jobs;
DROP TABLE IF EXISTS prompt_templates;
DROP TABLE IF EXISTS weekly_mail_reports;
DROP TABLE IF EXISTS email_action_items;
DROP TABLE IF EXISTS email_analysis;
DROP TABLE IF EXISTS email_recipients;
DROP TABLE IF EXISTS emails;
DROP TABLE IF EXISTS mail_accounts;
DROP TABLE IF EXISTS users;


-- =========================================================
-- TABLES
-- =========================================================

-- USERS
CREATE TABLE users (
    id VARCHAR(20) NOT NULL,
    display_name VARCHAR(100),
    primary_email VARCHAR(255),
    status VARCHAR(20),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE users IS '서비스 사용자';
COMMENT ON COLUMN users.id IS '사용자 식별 ID';
COMMENT ON COLUMN users.display_name IS '사용자 표시명';
COMMENT ON COLUMN users.primary_email IS '대표 이메일 주소';
COMMENT ON COLUMN users.status IS '사용자 상태 코드';
COMMENT ON COLUMN users.last_login_at IS '마지막 로그인 시간';
COMMENT ON COLUMN users.created_at IS '생성일시';
COMMENT ON COLUMN users.updated_at IS '수정일시';


-- MAIL_ACCOUNTS
CREATE TABLE mail_accounts (
    id VARCHAR(20) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_account_id VARCHAR(255) NOT NULL,
    account_email VARCHAR(255) NOT NULL,
    account_name VARCHAR(100),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sync_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sync_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    access_token_encrypted TEXT,
    refresh_token_encrypted TEXT,
    token_expires_at TIMESTAMP,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_mail_accounts_provider_provider_account UNIQUE (provider, provider_account_id),
    CONSTRAINT uk_mail_accounts_provider_account_email UNIQUE (provider, account_email)
);

COMMENT ON TABLE mail_accounts IS '메일 계정';
COMMENT ON COLUMN mail_accounts.id IS '메일 계정 식별 ID';
COMMENT ON COLUMN mail_accounts.user_id IS '사용자 ID';
COMMENT ON COLUMN mail_accounts.provider IS '메일 제공자 구분';
COMMENT ON COLUMN mail_accounts.provider_account_id IS '제공자 계정 식별값';
COMMENT ON COLUMN mail_accounts.account_email IS '메일 계정 주소';
COMMENT ON COLUMN mail_accounts.account_name IS '메일 계정 이름';
COMMENT ON COLUMN mail_accounts.is_primary IS '대표 계정 여부';
COMMENT ON COLUMN mail_accounts.sync_enabled IS '동기화 사용 여부';
COMMENT ON COLUMN mail_accounts.sync_status IS '동기화 상태';
COMMENT ON COLUMN mail_accounts.access_token_encrypted IS '암호화된 접근 토큰';
COMMENT ON COLUMN mail_accounts.refresh_token_encrypted IS '암호화된 갱신 토큰';
COMMENT ON COLUMN mail_accounts.token_expires_at IS '토큰 만료 일시';
COMMENT ON COLUMN mail_accounts.last_synced_at IS '마지막 동기화 일시';
COMMENT ON COLUMN mail_accounts.created_at IS '생성일시';
COMMENT ON COLUMN mail_accounts.updated_at IS '수정일시';


-- EMAILS
CREATE TABLE emails (
    id VARCHAR(20) NOT NULL,
    mail_account_id VARCHAR(20) NOT NULL,
    external_message_id VARCHAR(255) NOT NULL,
    external_thread_id VARCHAR(255),
    internet_message_id VARCHAR(500),
    subject VARCHAR(500),
    body_text TEXT,
    body_html TEXT,
    message_snippet VARCHAR(1000),
    from_name VARCHAR(255),
    from_email VARCHAR(255) NOT NULL,
    received_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_starred BOOLEAN NOT NULL DEFAULT FALSE,
    has_attachment BOOLEAN NOT NULL DEFAULT FALSE,
    importance_header VARCHAR(50),
    raw_payload TEXT,
    analysis_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    analysis_candidate_score INT,
    analysis_candidate_reasons VARCHAR(1000),
    analysis_skipped_reason VARCHAR(100),
    analysis_candidate_evaluated_at TIMESTAMP,
    attention_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    attention_resolved_at TIMESTAMP,
    attention_status VARCHAR(30) NOT NULL DEFAULT 'NEEDS_ATTENTION',
    attention_status_updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_emails_mail_account_external_message UNIQUE (mail_account_id, external_message_id)
);

COMMENT ON TABLE emails IS '메일';
COMMENT ON COLUMN emails.id IS '메일 식별 ID';
COMMENT ON COLUMN emails.mail_account_id IS '메일 계정 ID';
COMMENT ON COLUMN emails.external_message_id IS '외부 메시지 식별값';
COMMENT ON COLUMN emails.external_thread_id IS '외부 스레드 식별값';
COMMENT ON COLUMN emails.internet_message_id IS '인터넷 메시지 ID';
COMMENT ON COLUMN emails.subject IS '메일 제목';
COMMENT ON COLUMN emails.body_text IS '메일 본문 텍스트';
COMMENT ON COLUMN emails.body_html IS '메일 본문 HTML';
COMMENT ON COLUMN emails.message_snippet IS '메일 미리보기';
COMMENT ON COLUMN emails.from_name IS '발신자 이름';
COMMENT ON COLUMN emails.from_email IS '발신자 이메일';
COMMENT ON COLUMN emails.received_at IS '수신 일시';
COMMENT ON COLUMN emails.sent_at IS '발송 일시';
COMMENT ON COLUMN emails.is_read IS '읽음 여부';
COMMENT ON COLUMN emails.is_starred IS '중요 표시 여부';
COMMENT ON COLUMN emails.has_attachment IS '첨부파일 여부';
COMMENT ON COLUMN emails.importance_header IS '중요도 헤더';
COMMENT ON COLUMN emails.raw_payload IS '원본 메일 데이터';
COMMENT ON COLUMN emails.analysis_eligible IS '자동 분석 대상 여부';
COMMENT ON COLUMN emails.analysis_candidate_score IS '분석 후보 점수';
COMMENT ON COLUMN emails.analysis_candidate_reasons IS '분석 후보 판단 사유 코드';
COMMENT ON COLUMN emails.analysis_skipped_reason IS '자동 분석 제외 사유';
COMMENT ON COLUMN emails.analysis_candidate_evaluated_at IS '분석 후보 판단 일시';
COMMENT ON COLUMN emails.attention_resolved IS '사용자 확인/처리 완료 여부';
COMMENT ON COLUMN emails.attention_resolved_at IS '사용자 확인/처리 완료 일시';
COMMENT ON COLUMN emails.attention_status IS '사용자 처리 상태';
COMMENT ON COLUMN emails.attention_status_updated_at IS '사용자 처리 상태 변경 일시';
COMMENT ON COLUMN emails.created_at IS '생성일시';
COMMENT ON COLUMN emails.updated_at IS '수정일시';


-- EMAIL_RECIPIENTS
CREATE TABLE email_recipients (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    recipient_type VARCHAR(10) NOT NULL,
    recipient_name VARCHAR(255),
    recipient_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE email_recipients IS '메일 수신자';
COMMENT ON COLUMN email_recipients.id IS '수신자 식별 ID';
COMMENT ON COLUMN email_recipients.email_id IS '메일 ID';
COMMENT ON COLUMN email_recipients.recipient_type IS '수신자 유형 (TO, CC, BCC)';
COMMENT ON COLUMN email_recipients.recipient_name IS '수신자 이름';
COMMENT ON COLUMN email_recipients.recipient_email IS '수신자 이메일';
COMMENT ON COLUMN email_recipients.created_at IS '생성일시';
COMMENT ON COLUMN email_recipients.updated_at IS '수정일시';


-- EMAIL_ANALYSIS
CREATE TABLE email_analysis (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    analysis_version INT NOT NULL DEFAULT 1,
    is_latest BOOLEAN NOT NULL DEFAULT TRUE,
    model_name VARCHAR(100),
    prompt_version VARCHAR(50),
    short_summary VARCHAR(1000),
    detailed_summary TEXT,
    category VARCHAR(50),
    priority_level VARCHAR(10),
    importance_score DECIMAL(5,2),
    urgency_score DECIMAL(5,2),
    confidence_score DECIMAL(5,2),
    needs_reply BOOLEAN,
    has_deadline BOOLEAN,
    deadline_at TIMESTAMP,
    deadline_text VARCHAR(255),
    time_sensitivity VARCHAR(30),
    requires_action BOOLEAN,
    user_task_summary VARCHAR(1000),
    priority_reason_codes VARCHAR(1000),
    suggested_action VARCHAR(255),
    reasoning TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE email_analysis IS '메일 분석';
COMMENT ON COLUMN email_analysis.id IS '메일 분석 ID';
COMMENT ON COLUMN email_analysis.email_id IS '메일 ID';
COMMENT ON COLUMN email_analysis.analysis_version IS '분석 버전';
COMMENT ON COLUMN email_analysis.is_latest IS '최신 분석 여부';
COMMENT ON COLUMN email_analysis.model_name IS '모델명';
COMMENT ON COLUMN email_analysis.prompt_version IS '프롬프트 버전';
COMMENT ON COLUMN email_analysis.short_summary IS '한 줄 요약';
COMMENT ON COLUMN email_analysis.detailed_summary IS '상세 요약';
COMMENT ON COLUMN email_analysis.category IS '메일 카테고리';
COMMENT ON COLUMN email_analysis.priority_level IS '우선순위';
COMMENT ON COLUMN email_analysis.importance_score IS '중요도 점수';
COMMENT ON COLUMN email_analysis.urgency_score IS '긴급도 점수';
COMMENT ON COLUMN email_analysis.confidence_score IS '신뢰도 점수';
COMMENT ON COLUMN email_analysis.needs_reply IS '회신 필요 여부';
COMMENT ON COLUMN email_analysis.has_deadline IS '마감 존재 여부';
COMMENT ON COLUMN email_analysis.deadline_at IS '마감 일시';
COMMENT ON COLUMN email_analysis.deadline_text IS '메일 원문에 표현된 마감 문구';
COMMENT ON COLUMN email_analysis.time_sensitivity IS '시간 민감도';
COMMENT ON COLUMN email_analysis.requires_action IS '사용자 액션 필요 여부';
COMMENT ON COLUMN email_analysis.user_task_summary IS '사용자가 해야 할 일 요약';
COMMENT ON COLUMN email_analysis.priority_reason_codes IS '우선순위 판단 사유 코드';
COMMENT ON COLUMN email_analysis.suggested_action IS '추천 액션';
COMMENT ON COLUMN email_analysis.reasoning IS '판단 사유';
COMMENT ON COLUMN email_analysis.status IS '분석 상태';
COMMENT ON COLUMN email_analysis.analyzed_at IS '분석 일시';
COMMENT ON COLUMN email_analysis.created_at IS '생성일시';
COMMENT ON COLUMN email_analysis.updated_at IS '수정일시';


-- EMAIL_ACTION_ITEMS
CREATE TABLE email_action_items (
    id VARCHAR(20) NOT NULL,
    analysis_id VARCHAR(20) NOT NULL,
    action_text VARCHAR(500) NOT NULL,
    action_type VARCHAR(50),
    priority_level VARCHAR(10),
    due_at TIMESTAMP,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE email_action_items IS '메일 액션 항목';
COMMENT ON COLUMN email_action_items.id IS '액션 항목 ID';
COMMENT ON COLUMN email_action_items.analysis_id IS '분석 ID';
COMMENT ON COLUMN email_action_items.action_text IS '액션 내용';
COMMENT ON COLUMN email_action_items.action_type IS '액션 유형';
COMMENT ON COLUMN email_action_items.priority_level IS '우선순위';
COMMENT ON COLUMN email_action_items.due_at IS '마감 일시';
COMMENT ON COLUMN email_action_items.is_completed IS '완료 여부';
COMMENT ON COLUMN email_action_items.created_at IS '생성일시';
COMMENT ON COLUMN email_action_items.updated_at IS '수정일시';


-- ANALYSIS_JOBS
CREATE TABLE analysis_jobs (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    job_type VARCHAR(50) NOT NULL DEFAULT 'EMAIL_ANALYSIS',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INT NOT NULL DEFAULT 5,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    worker_id VARCHAR(100),
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE analysis_jobs IS '메일 분석 작업';
COMMENT ON COLUMN analysis_jobs.id IS '분석 작업 ID';
COMMENT ON COLUMN analysis_jobs.email_id IS '메일 ID';
COMMENT ON COLUMN analysis_jobs.job_type IS '작업 유형';
COMMENT ON COLUMN analysis_jobs.status IS '작업 상태';
COMMENT ON COLUMN analysis_jobs.priority IS '작업 우선순위';
COMMENT ON COLUMN analysis_jobs.retry_count IS '재시도 횟수';
COMMENT ON COLUMN analysis_jobs.max_retries IS '최대 재시도 횟수';
COMMENT ON COLUMN analysis_jobs.worker_id IS '처리 worker';
COMMENT ON COLUMN analysis_jobs.error_message IS '오류 메시지';
COMMENT ON COLUMN analysis_jobs.started_at IS '작업 시작 시각';
COMMENT ON COLUMN analysis_jobs.completed_at IS '작업 완료 시각';
COMMENT ON COLUMN analysis_jobs.created_at IS '생성일시';
COMMENT ON COLUMN analysis_jobs.updated_at IS '수정일시';


-- PROMPT_TEMPLATES
CREATE TABLE prompt_templates (
    id VARCHAR(20) NOT NULL,
    prompt_code VARCHAR(80) NOT NULL,
    prompt_name VARCHAR(100) NOT NULL,
    prompt_type VARCHAR(50) NOT NULL,
    version INT NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    role_content TEXT NOT NULL,
    policy_content TEXT NOT NULL,
    guide_content TEXT NOT NULL,
    output_content TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE prompt_templates IS 'LLM 프롬프트 템플릿';
COMMENT ON COLUMN prompt_templates.id IS '프롬프트 템플릿 ID';
COMMENT ON COLUMN prompt_templates.prompt_code IS '프롬프트 코드';
COMMENT ON COLUMN prompt_templates.prompt_name IS '프롬프트명';
COMMENT ON COLUMN prompt_templates.prompt_type IS '프롬프트 유형';
COMMENT ON COLUMN prompt_templates.version IS '프롬프트 버전';
COMMENT ON COLUMN prompt_templates.model_name IS '사용 모델명';
COMMENT ON COLUMN prompt_templates.role_content IS 'Role 섹션';
COMMENT ON COLUMN prompt_templates.policy_content IS 'Policy 섹션';
COMMENT ON COLUMN prompt_templates.guide_content IS 'Guide 섹션';
COMMENT ON COLUMN prompt_templates.output_content IS 'Output 섹션';
COMMENT ON COLUMN prompt_templates.active IS '사용 여부';
COMMENT ON COLUMN prompt_templates.created_at IS '생성일시';
COMMENT ON COLUMN prompt_templates.updated_at IS '수정일시';


-- WEEKLY_MAIL_REPORTS
CREATE TABLE weekly_mail_reports (
    id VARCHAR(20) NOT NULL,
    mail_account_id VARCHAR(20) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    email_count INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    report_type VARCHAR(30) NOT NULL DEFAULT 'WEEKLY',
    executive_summary VARCHAR(2000),
    report_payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE weekly_mail_reports IS '주간 메일 요약(주간보고용)';
COMMENT ON COLUMN weekly_mail_reports.id IS '주간 요약 ID';
COMMENT ON COLUMN weekly_mail_reports.mail_account_id IS '메일 계정 ID';
COMMENT ON COLUMN weekly_mail_reports.period_start IS '집계 시작 시각';
COMMENT ON COLUMN weekly_mail_reports.period_end IS '집계 종료 시각(배타)';
COMMENT ON COLUMN weekly_mail_reports.email_count IS '포함된 메일 수';
COMMENT ON COLUMN weekly_mail_reports.status IS '생성 상태';
COMMENT ON COLUMN weekly_mail_reports.report_type IS '보고서 양식 구분';
COMMENT ON COLUMN weekly_mail_reports.executive_summary IS '한 줄 요약';
COMMENT ON COLUMN weekly_mail_reports.report_payload IS '상세 요약 JSON';
COMMENT ON COLUMN weekly_mail_reports.created_at IS '생성일시';
COMMENT ON COLUMN weekly_mail_reports.updated_at IS '수정일시';


-- LABELS
CREATE TABLE labels (
    id VARCHAR(20) NOT NULL,
    label_name VARCHAR(100) NOT NULL,
    label_code VARCHAR(50) NOT NULL,
    label_type VARCHAR(50),
    color VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE labels IS '메일 라벨';
COMMENT ON COLUMN labels.id IS '라벨 ID';
COMMENT ON COLUMN labels.label_name IS '라벨명';
COMMENT ON COLUMN labels.label_code IS '라벨 코드';
COMMENT ON COLUMN labels.label_type IS '라벨 유형';
COMMENT ON COLUMN labels.color IS '라벨 색상';
COMMENT ON COLUMN labels.created_at IS '생성일시';
COMMENT ON COLUMN labels.updated_at IS '수정일시';


-- EMAIL_LABELS
CREATE TABLE email_labels (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    label_id VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

COMMENT ON TABLE email_labels IS '메일 라벨 매핑';
COMMENT ON COLUMN email_labels.id IS '메일 라벨 ID';
COMMENT ON COLUMN email_labels.email_id IS '메일 ID';
COMMENT ON COLUMN email_labels.label_id IS '라벨 ID';
COMMENT ON COLUMN email_labels.confidence_score IS '신뢰도 점수';
COMMENT ON COLUMN email_labels.created_at IS '생성일시';
COMMENT ON COLUMN email_labels.updated_at IS '수정일시';


-- =========================================================
-- DUMMY DATA
-- ID FORMAT: PREFIX_YYMMDD_RANDOM6
-- =========================================================

-- USERS (30)
INSERT INTO users (id, display_name, primary_email, status, last_login_at)
SELECT
    CONCAT(
        'USR_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(gs::text || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    CONCAT('홍길동', gs) AS display_name,
    CONCAT('user', gs, '@test.com') AS primary_email,
    'ACTIVE' AS status,
    NOW() AS last_login_at
FROM generate_series(1, 30) AS gs;


-- MAIL_ACCOUNTS (users 1:1)
INSERT INTO mail_accounts (
    id, user_id, provider, provider_account_id, account_email, account_name, is_primary, sync_enabled, sync_status
)
SELECT
    CONCAT(
        'MAC_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(u.id || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    u.id AS user_id,
    'GOOGLE' AS provider,
    CONCAT('google_', u.id) AS provider_account_id,
    u.primary_email AS account_email,
    u.display_name AS account_name,
    TRUE AS is_primary,
    TRUE AS sync_enabled,
    'ACTIVE' AS sync_status
FROM users u;


-- PROMPT_TEMPLATES
INSERT INTO prompt_templates (
    id, prompt_code, prompt_name, prompt_type, version, model_name,
    role_content, policy_content, guide_content, output_content, active
) VALUES (
    CONCAT('PRM_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_A00001'),
    'WEEKLY_REPORT',
    '주간보고 생성 프롬프트',
    'REPORT',
    1,
    'gpt-5.4-mini',
    '너는 업무 메일을 근거로 주간보고 초안을 작성하는 전문 비서다. 사용자가 선택한 기간의 메일 제목과 본문을 읽고, 보고자가 바로 다듬어 제출할 수 있는 실무형 주간보고를 한국어로 작성한다.',
    '메일 원문에 없는 사실, 수치, 일정, 담당자를 만들지 않는다. 근거가 약한 내용은 단정하지 말고 확인 필요 항목으로 분류한다. 개인 정보, 토큰, 비밀번호, 인증 코드, 민감한 계정 정보는 보고서에 노출하지 않는다. 광고성 메일, 자동 알림, 중복 알림은 실제 업무 진행과 관련된 경우에만 반영한다. 출력은 반드시 지정된 JSON 형식만 반환한다.',
    '금주실적에는 선택 기간 안에 완료되었거나 의미 있는 진척이 확인되는 업무를 쓴다. 진행사항에는 아직 완료되지 않았지만 추적해야 하는 업무 흐름을 쓴다. 특이사항에는 장애, 일정 지연, 계약, 정산, 보안, 고객 이슈처럼 보고자가 별도로 인지해야 하는 내용을 쓴다. 확인 필요에는 회신, 승인, 검토, 의사결정이 필요한 항목을 쓴다. 차주계획에는 메일 흐름상 다음 주에 이어서 처리해야 할 업무를 쓴다. 각 항목은 짧은 명사 나열이 아니라 보고 문장으로 작성하고, 가능하면 메일 근거가 드러나도록 구체적으로 쓴다.',
    '다음 JSON 객체만 반환한다. {"executive_summary":"선택 기간 전체 흐름을 2문장 이내로 요약","highlights":["금주실적: ...","진행사항: ..."],"risks_blockers":["특이사항: ..."],"pending_decisions":["확인필요: ..."],"next_week_suggestions":["차주계획: ..."],"thread_summaries":[{"email_id":"메일 ID","subject":"메일 제목","one_liner":"보고서에 반영된 핵심 근거"}]}',
    TRUE
);


-- EMAILS (각 계정당 100개)
INSERT INTO emails (
    id, mail_account_id, external_message_id, subject, body_text, from_name, from_email, received_at, is_read
)
SELECT
    CONCAT(
        'EML_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(m.id || tpl.seq::text || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    m.id AS mail_account_id,
    CONCAT('MSG_', m.id, '_', tpl.seq) AS external_message_id,
    tpl.subject,
    CONCAT(
        '안녕하세요. ', tpl.body_text,
        ' 관련하여 세부 내용을 함께 공유드립니다. 이번 메일은 단순 알림이 아니라 담당자 확인과 후속 조치가 필요한 업무 내용을 포함하고 있습니다. ',
        '현재 확인해야 할 항목은 요청 배경, 처리 기한, 관련 담당자, 외부 공유 가능 여부입니다. ',
        '검토 중 추가 이슈가 발견되면 회신에 함께 남겨 주시고, 바로 처리 가능한 항목과 차주로 넘겨야 하는 항목을 구분해 주세요. ',
        '회신 내용을 바탕으로 금주실적, 차주계획, 특이사항에 반영할 예정입니다. 감사합니다.'
    ),
    tpl.from_name,
    tpl.from_email,
    NOW() - (tpl.seq * INTERVAL '17 minutes') AS received_at,
    tpl.is_read
FROM mail_accounts m
CROSS JOIN (
    VALUES
        (1, '업무 요청 메일', '금일 중 확인 부탁드립니다.', '김부장', 'boss@test.com', FALSE),
        (2, '회의 일정 안내', '내일 오전 10시 회의 예정입니다.', '김과장', 'manager@test.com', TRUE),
        (3, '보고 요청', '주간 보고서 제출 바랍니다.', '이팀장', 'leader@test.com', FALSE),
        (4, '긴급 승인 요청', '오늘 중 승인 여부 확인 부탁드립니다.', '대표이사', 'ceo@test.com', FALSE),
        (5, '고객 미팅 후속 정리', '고객 미팅 요청사항을 확인해 주세요.', '고객성공팀', 'cs@test.com', FALSE),
        (6, '정산 자료 확인', '정산 자료와 세금계산서 내역 확인 요청드립니다.', '재무팀', 'finance@test.com', TRUE),
        (7, '개발 일정 공유', '배포 일정과 QA 진행 상황을 공유드립니다.', '개발리드', 'devlead@test.com', FALSE),
        (8, '계약 변경 검토', '계약 변경 조항 검토가 필요합니다.', '법무팀', 'legal@test.com', FALSE),
        (9, '주간 업무 업데이트', '이번 주 진행 상황과 다음 주 예정 업무를 공유드립니다.', 'PM', 'pm@test.com', TRUE),
        (10, '장애 대응 결과 공유', '장애 대응 결과와 재발 방지 항목을 공유드립니다.', '운영팀', 'ops@test.com', FALSE),
        (11, '채용 면접 일정 조율', '면접 가능 시간을 회신 부탁드립니다.', '인사팀', 'hr@test.com', FALSE),
        (12, '디자인 시안 검토', '신규 화면 디자인 시안 검토 부탁드립니다.', '디자인팀', 'design@test.com', TRUE),
        (13, '보안 점검 안내', '정기 보안 점검 일정과 준비 사항을 안내드립니다.', '보안팀', 'security@test.com', FALSE),
        (14, '고객 문의 답변 요청', '고객 문의에 대한 답변 초안 확인 부탁드립니다.', 'CS팀', 'support@test.com', FALSE),
        (15, '예산 사용 내역 공유', '이번 달 예산 사용 내역을 공유드립니다.', '재무팀', 'finance@test.com', TRUE),
        (16, '릴리즈 노트 확인', '릴리즈 노트 초안을 확인해 주세요.', '개발리드', 'devlead@test.com', FALSE),
        (17, '계약서 서명 요청', '계약서 최종본 서명을 요청드립니다.', '법무팀', 'legal@test.com', FALSE),
        (18, '파트너 제휴 제안', '파트너사 제휴 제안서를 검토 부탁드립니다.', '제휴팀', 'partner@test.com', TRUE),
        (19, '마케팅 캠페인 보고', '캠페인 성과 보고서를 공유드립니다.', '마케팅팀', 'marketing@test.com', FALSE),
        (20, '데이터 추출 요청', '분석용 데이터 추출 가능 여부 확인 부탁드립니다.', '데이터팀', 'data@test.com', FALSE),
        (21, '월간 회고 일정', '월간 회고 일정을 안내드립니다.', 'PM', 'pm@test.com', TRUE),
        (22, '긴급 고객 이슈', '고객 이슈가 접수되어 빠른 확인이 필요합니다.', '고객성공팀', 'cs@test.com', FALSE),
        (23, 'API 변경 공지', '외부 API 변경 사항을 공유드립니다.', '플랫폼팀', 'platform@test.com', FALSE),
        (24, '인프라 비용 알림', '인프라 비용 증가 항목 확인 부탁드립니다.', '인프라팀', 'infra@test.com', TRUE),
        (25, '교육 일정 안내', '신규 교육 일정과 참석자를 확인해 주세요.', '교육팀', 'edu@test.com', FALSE),
        (26, '기획 문서 피드백', '기획 문서 피드백을 전달드립니다.', '기획팀', 'planning@test.com', FALSE),
        (27, '결제 실패 알림', '정기 결제 실패 건 확인이 필요합니다.', '결제팀', 'billing@test.com', TRUE),
        (28, '운영 정책 변경', '운영 정책 변경 사항을 검토 부탁드립니다.', '운영팀', 'ops@test.com', FALSE),
        (29, '성과 지표 공유', '이번 주 주요 성과 지표를 공유드립니다.', '데이터팀', 'data@test.com', FALSE),
        (30, '회의록 공유', '오늘 회의록과 후속 과제를 공유드립니다.', '김과장', 'manager@test.com', TRUE)
) AS tpl(seq, subject, body_text, from_name, from_email, is_read);

INSERT INTO emails (
    id, mail_account_id, external_message_id, subject, body_text, from_name, from_email, received_at, is_read
)
SELECT
    CONCAT(
        'EML_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(m.id || gs::text || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    m.id AS mail_account_id,
    CONCAT('MSG_', m.id, '_', gs) AS external_message_id,
    CASE MOD(gs, 6)
        WHEN 0 THEN '긴급 승인 요청'
        WHEN 1 THEN '고객 미팅 후속 정리'
        WHEN 2 THEN '정산 자료 확인'
        WHEN 3 THEN '개발 일정 공유'
        WHEN 4 THEN '계약 변경 검토'
        ELSE '주간 업무 업데이트'
    END AS subject,
    CASE MOD(gs, 6)
        WHEN 0 THEN '안녕하세요. 오늘 중 승인 여부 확인 부탁드립니다. 고객사 전달 일정이 오후에 잡혀 있어 지연 시 별도 안내가 필요합니다. 현재 검토가 필요한 항목은 예산 범위, 담당자 배정, 외부 공유 가능 여부입니다. 승인 대상에는 신규 분석 리포트 샘플과 고객사 전용 안내 문구가 포함되어 있으며, 보류가 필요한 경우 사유를 명확히 남겨야 합니다. 승인 가능 여부, 보완 필요 항목, 고객사에 바로 전달 가능한 범위를 구분해 회신해 주세요. 회신을 받으면 최종 문안을 정리하고 오후 미팅 전에 공유하겠습니다.'
        WHEN 1 THEN '안녕하세요. 고객 미팅에서 나온 요청사항을 정리했습니다. 우선순위가 높은 항목은 사용량 리포트 개선, 알림 문구 조정, 관리자 권한 범위 확인입니다. 고객사는 특히 메일 분석 결과가 어떤 근거로 긴급하다고 판단되는지 설명 가능한 형태를 원하고 있습니다. 담당자별 후속 액션을 확인해 주시고, 이번 주 안에 처리 가능한 범위와 다음 주로 넘겨야 하는 항목을 구분해 주세요. 회신 내용을 바탕으로 고객사에 진행 계획과 예상 일정을 공유하겠습니다.'
        WHEN 2 THEN '안녕하세요. 이번 달 정산 자료와 세금계산서 발행 내역을 확인 부탁드립니다. 일부 항목의 비용 코드가 이전 달과 다르게 표시되어 있어 재무팀 확인이 필요합니다. 첨부된 집계 기준에는 인프라 사용량, 외부 API 호출 비용, 테스트 계정 사용분이 포함되어 있습니다. 누락된 항목이나 중복 청구 가능성이 있는 항목이 있는지 봐 주세요. 확인 완료 후 증빙 보관 여부와 재발행 필요 여부도 함께 알려주시면 감사하겠습니다.'
        WHEN 3 THEN '안녕하세요. 다음 배포 일정과 QA 진행 상황을 공유드립니다. 현재 핵심 기능 개발은 대부분 완료되었고, 남은 작업은 권한 예외 처리와 모바일 화면 확인입니다. QA에서 발견된 위험 요소는 메일 원문 펼침 영역의 긴 본문 표시, 보고서 생성 후 이전 결과 선택, 기간 필터 초기화 동작입니다. 배포 전까지 우선 처리해야 하는 항목과 차주로 넘겨도 되는 항목을 구분해 주세요. 각 담당자는 오늘 오후까지 상태를 업데이트해 주세요.'
        WHEN 4 THEN '안녕하세요. 계약 변경 조항 검토가 필요합니다. 변경된 내용은 서비스 제공 범위, 장애 고지 기준, 데이터 보관 기간과 관련되어 있습니다. 분석 결과가 자동 생성되는 특성상 고객에게 제공되는 요약 정보의 책임 범위를 어디까지 볼 것인지 확인해야 합니다. 책임 범위가 넓어지는 문구, 보안 검토가 필요한 표현, 비용 정산에 영향을 주는 조항을 중점적으로 봐 주세요.'
        ELSE '안녕하세요. 이번 주 진행 상황과 다음 주 예정 업무를 공유드립니다. 주요 완료 항목은 메일 목록 화면 개선, 주간보고 초안 생성, 분석 결과 저장 흐름 점검입니다. 전체 메일함에서 원문과 요약을 함께 확인할 수 있게 되었고, 보고서 생성 메뉴에서는 선택한 기간의 메일을 바탕으로 금주실적과 차주계획을 정리할 수 있게 되었습니다. 다음 주에는 실제 메일 연동 범위, 에이전트 분석 품질, 대량 메일 페이징 성능을 함께 확인할 예정입니다.'
    END AS body_text,
    CASE MOD(gs, 6)
        WHEN 0 THEN '대표이사'
        WHEN 1 THEN '고객성공팀'
        WHEN 2 THEN '재무팀'
        WHEN 3 THEN '개발리드'
        WHEN 4 THEN '법무팀'
        ELSE m.account_name
    END AS from_name,
    CASE MOD(gs, 6)
        WHEN 0 THEN 'ceo@test.com'
        WHEN 1 THEN 'cs@test.com'
        WHEN 2 THEN 'finance@test.com'
        WHEN 3 THEN 'devlead@test.com'
        WHEN 4 THEN 'legal@test.com'
        ELSE m.account_email
    END AS from_email,
    NOW() - (gs * INTERVAL '17 minutes') AS received_at,
    CASE WHEN MOD(gs, 3) = 0 THEN TRUE ELSE FALSE END AS is_read
FROM mail_accounts m
CROSS JOIN generate_series(31, 100) AS gs;


-- EMAIL_RECIPIENTS
INSERT INTO email_recipients (id, email_id, recipient_type, recipient_email)
SELECT
    CONCAT(
        'ERC_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(e.id || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    e.id AS email_id,
    'TO' AS recipient_type,
    'me@test.com' AS recipient_email
FROM emails e;


-- EMAIL_ANALYSIS
INSERT INTO email_analysis (
    id, email_id, analysis_version, is_latest, short_summary, category, priority_level, needs_reply, status
)
SELECT
    CONCAT(
        'ANL_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(e.id || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    e.id AS email_id,
    1 AS analysis_version,
    TRUE AS is_latest,
    '업무 관련 요청 메일' AS short_summary,
    'REQUEST' AS category,
    'P2' AS priority_level,
    TRUE AS needs_reply,
    'COMPLETED' AS status
FROM emails e;


-- EMAIL_ACTION_ITEMS
INSERT INTO email_action_items (id, analysis_id, action_text, action_type)
SELECT
    CONCAT(
        'ACT_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(a.id || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    a.id AS analysis_id,
    '메일 내용 확인 및 회신' AS action_text,
    'REPLY' AS action_type
FROM email_analysis a;


-- LABELS
INSERT INTO labels (id, label_name, label_code)
SELECT
    CONCAT(
        'LBL_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(v.label_code || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    v.label_name,
    v.label_code
FROM (
    VALUES
        ('긴급', 'URGENT'),
        ('요청', 'REQUEST'),
        ('보고', 'REPORT'),
        ('회의', 'MEETING')
) AS v(label_name, label_code);


-- EMAIL_LABELS (REQUEST 라벨 매핑)
INSERT INTO email_labels (id, email_id, label_id)
SELECT
    CONCAT(
        'ELB_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(e.id || l.id || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    e.id AS email_id,
    l.id AS label_id
FROM emails e
JOIN labels l ON l.label_code = 'REQUEST';


-- ANALYSIS_JOBS
INSERT INTO analysis_jobs (id, email_id, job_type, status)
SELECT
    CONCAT(
        'JOB_', TO_CHAR(CURRENT_DATE, 'YYMMDD'), '_',
        UPPER(SUBSTRING(MD5(e.id || CLOCK_TIMESTAMP()::text || RANDOM()::text) FROM 1 FOR 6))
    ) AS id,
    e.id AS email_id,
    'EMAIL_ANALYSIS' AS job_type,
    'COMPLETED' AS status
FROM emails e;
