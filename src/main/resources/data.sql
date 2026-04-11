INSERT INTO users (id, display_name, primary_email, status, last_login_at) VALUES
    ('USR_260409_A00001', '홍길동1', 'user1@test.com', 'ACTIVE', CURRENT_TIMESTAMP),
    ('USR_260409_A00002', '홍길동2', 'user2@test.com', 'ACTIVE', CURRENT_TIMESTAMP),
    ('USR_260409_A00003', '홍길동3', 'user3@test.com', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO mail_accounts (
    id, user_id, provider, provider_account_id, account_email, account_name,
    is_primary, sync_enabled, sync_status, last_synced_at
) VALUES
    ('MAC_260409_A00001', 'USR_260409_A00001', 'GOOGLE', 'google_USR_260409_A00001', 'user1@test.com', '홍길동1', TRUE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP),
    ('MAC_260409_A00002', 'USR_260409_A00002', 'GOOGLE', 'google_USR_260409_A00002', 'user2@test.com', '홍길동2', TRUE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP),
    ('MAC_260409_A00003', 'USR_260409_A00003', 'MICROSOFT', 'microsoft_USR_260409_A00003', 'user3@test.com', '홍길동3', TRUE, TRUE, 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO emails (
    id, mail_account_id, external_message_id, external_thread_id, internet_message_id, subject, body_text,
    body_html, message_snippet, from_name, from_email, received_at, sent_at, is_read, is_starred,
    has_attachment, importance_header, raw_payload
) VALUES
    ('EML_260409_A00001', 'MAC_260409_A00001', 'MSG_MAC_1_1', 'THR_MAC_1', '<msg1@test.com>', '업무 요청 메일', '금일 중 확인 부탁드립니다.', '<p>금일 중 확인 부탁드립니다.</p>', '금일 중 확인 부탁드립니다.', '김부장', 'boss@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, TRUE, FALSE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00002', 'MAC_260409_A00001', 'MSG_MAC_1_2', 'THR_MAC_1', '<msg2@test.com>', '회의 일정 안내', '내일 오전 10시 회의 예정입니다.', '<p>내일 오전 10시 회의 예정입니다.</p>', '내일 오전 10시 회의 예정입니다.', '김과장', 'manager@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, FALSE, FALSE, 'normal', '{"provider":"gmail"}'),
    ('EML_260409_A00003', 'MAC_260409_A00001', 'MSG_MAC_1_3', 'THR_MAC_2', '<msg3@test.com>', '보고 요청', '주간 보고서 제출 바랍니다.', '<p>주간 보고서 제출 바랍니다.</p>', '주간 보고서 제출 바랍니다.', '이팀장', 'leader@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, FALSE, TRUE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00004', 'MAC_260409_A00002', 'MSG_MAC_2_1', 'THR_MAC_3', '<msg4@test.com>', '계약 검토 요청', '첨부된 계약서 확인 부탁드립니다.', '<p>첨부된 계약서 확인 부탁드립니다.</p>', '첨부된 계약서 확인 부탁드립니다.', '법무팀', 'legal@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, TRUE, TRUE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00005', 'MAC_260409_A00002', 'MSG_MAC_2_2', 'THR_MAC_4', '<msg5@test.com>', '주간 회고 공유', '이번 주 회고 문서를 공유드립니다.', '<p>이번 주 회고 문서를 공유드립니다.</p>', '이번 주 회고 문서를 공유드립니다.', 'PM', 'pm@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, FALSE, FALSE, 'normal', '{"provider":"gmail"}'),
    ('EML_260409_A00006', 'MAC_260409_A00002', 'MSG_MAC_2_3', 'THR_MAC_5', '<msg6@test.com>', '긴급 장애 공지', '서비스 장애 대응이 필요합니다.', '<p>서비스 장애 대응이 필요합니다.</p>', '서비스 장애 대응이 필요합니다.', '운영팀', 'ops@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, TRUE, FALSE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00007', 'MAC_260409_A00003', 'MSG_MAC_3_1', 'THR_MAC_6', '<msg7@test.com>', '제안서 피드백', '제안서 수정 요청사항 전달드립니다.', '<p>제안서 수정 요청사항 전달드립니다.</p>', '제안서 수정 요청사항 전달드립니다.', '고객사', 'client@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, FALSE, TRUE, 'normal', '{"provider":"outlook"}'),
    ('EML_260409_A00008', 'MAC_260409_A00003', 'MSG_MAC_3_2', 'THR_MAC_7', '<msg8@test.com>', '세금계산서 발행 안내', '세금계산서 발행이 완료되었습니다.', '<p>세금계산서 발행이 완료되었습니다.</p>', '세금계산서 발행이 완료되었습니다.', '재무팀', 'finance@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, FALSE, TRUE, 'low', '{"provider":"outlook"}'),
    ('EML_260409_A00009', 'MAC_260409_A00003', 'MSG_MAC_3_3', 'THR_MAC_8', '<msg9@test.com>', '면접 일정 조율', '면접 가능 시간을 회신 부탁드립니다.', '<p>면접 가능 시간을 회신 부탁드립니다.</p>', '면접 가능 시간을 회신 부탁드립니다.', '인사팀', 'hr@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, FALSE, FALSE, 'high', '{"provider":"outlook"}');

INSERT INTO email_recipients (id, email_id, recipient_type, recipient_name, recipient_email) VALUES
    ('ERC_260409_A00001', 'EML_260409_A00001', 'TO', '나', 'user1@test.com'),
    ('ERC_260409_A00002', 'EML_260409_A00002', 'TO', '나', 'user1@test.com'),
    ('ERC_260409_A00003', 'EML_260409_A00003', 'TO', '나', 'user1@test.com'),
    ('ERC_260409_A00004', 'EML_260409_A00004', 'TO', '나', 'user2@test.com'),
    ('ERC_260409_A00005', 'EML_260409_A00005', 'TO', '나', 'user2@test.com'),
    ('ERC_260409_A00006', 'EML_260409_A00006', 'TO', '나', 'user2@test.com'),
    ('ERC_260409_A00007', 'EML_260409_A00007', 'TO', '나', 'user3@test.com'),
    ('ERC_260409_A00008', 'EML_260409_A00008', 'TO', '나', 'user3@test.com'),
    ('ERC_260409_A00009', 'EML_260409_A00009', 'TO', '나', 'user3@test.com');

INSERT INTO email_analysis (
    id, email_id, analysis_version, is_latest, model_name, prompt_version, short_summary,
    detailed_summary, category, priority_level, importance_score, urgency_score,
    confidence_score, needs_reply, has_deadline, suggested_action, reasoning, status
) VALUES
    ('ANL_260409_A00001', 'EML_260409_A00001', 1, TRUE, 'gpt-5.4-mini', 'v1', '오늘 안에 확인이 필요한 업무 요청 메일', '상사가 금일 내 확인을 요청한 업무 메일입니다.', 'REQUEST', 'P1', 92.00, 88.00, 95.00, TRUE, TRUE, '내용 확인 후 회신', '명시적인 확인 요청과 짧은 마감 시점이 있습니다.', 'COMPLETED'),
    ('ANL_260409_A00002', 'EML_260409_A00002', 1, TRUE, 'gpt-5.4-mini', 'v1', '내일 오전 회의 일정 안내', '캘린더 등록이 필요한 회의 공지 메일입니다.', 'MEETING', 'P2', 72.00, 60.00, 93.00, FALSE, TRUE, '회의 일정 캘린더 반영', '시간 정보가 분명하고 액션이 단순합니다.', 'COMPLETED'),
    ('ANL_260409_A00003', 'EML_260409_A00003', 1, TRUE, 'gpt-5.4-mini', 'v1', '주간 보고서 제출 요청', '정기 보고 제출 요청 메일입니다.', 'REPORT', 'P2', 84.00, 70.00, 91.00, TRUE, TRUE, '보고서 작성 및 제출', '업무 제출 요청이며 회신 또는 제출 액션이 필요합니다.', 'COMPLETED'),
    ('ANL_260409_A00004', 'EML_260409_A00004', 1, TRUE, 'gpt-5.4-mini', 'v1', '계약서 검토 요청', '첨부파일 검토가 필요한 법무 관련 메일입니다.', 'REQUEST', 'P1', 95.00, 82.00, 94.00, TRUE, FALSE, '첨부 계약서 검토', '법적 리스크가 있을 수 있어 중요도가 높습니다.', 'COMPLETED'),
    ('ANL_260409_A00005', 'EML_260409_A00005', 1, TRUE, 'gpt-5.4-mini', 'v1', '주간 회고 문서 공유', '참고용 문서 공유 메일입니다.', 'REPORT', 'P3', 50.00, 35.00, 90.00, FALSE, FALSE, '필요 시 문서 확인', '즉시 액션 필요성은 낮습니다.', 'COMPLETED'),
    ('ANL_260409_A00006', 'EML_260409_A00006', 1, TRUE, 'gpt-5.4-mini', 'v1', '긴급 장애 대응 요청', '서비스 장애 대응이 필요한 긴급 알림 메일입니다.', 'URGENT', 'P1', 98.00, 99.00, 97.00, TRUE, FALSE, '장애 대응 시작', '서비스 영향이 명시된 고긴급 메일입니다.', 'COMPLETED'),
    ('ANL_260409_A00007', 'EML_260409_A00007', 1, TRUE, 'gpt-5.4-mini', 'v1', '제안서 수정 피드백', '고객 요청사항이 포함된 피드백 메일입니다.', 'REQUEST', 'P2', 80.00, 68.00, 92.00, TRUE, FALSE, '제안서 수정안 반영', '수정 요청이 구체적으로 담긴 메일입니다.', 'COMPLETED'),
    ('ANL_260409_A00008', 'EML_260409_A00008', 1, TRUE, 'gpt-5.4-mini', 'v1', '세금계산서 발행 완료 안내', '재무 처리 확인용 메일입니다.', 'FINANCE', 'P3', 58.00, 30.00, 89.00, FALSE, FALSE, '회계 증빙 보관', '참조 목적이 강한 메일입니다.', 'COMPLETED'),
    ('ANL_260409_A00009', 'EML_260409_A00009', 1, TRUE, 'gpt-5.4-mini', 'v1', '면접 일정 회신 요청', '후보자 면접 가능 시간을 회신해야 하는 메일입니다.', 'REQUEST', 'P2', 78.00, 76.00, 94.00, TRUE, TRUE, '가능 시간 회신', '상대방이 회신을 기다리는 일정 조율 메일입니다.', 'COMPLETED');

INSERT INTO email_action_items (
    id, analysis_id, action_text, action_type, priority_level, is_completed
) VALUES
    ('ACT_260409_A00001', 'ANL_260409_A00001', '메일 내용 확인 및 회신', 'REPLY', 'P1', FALSE),
    ('ACT_260409_A00002', 'ANL_260409_A00002', '회의 일정 캘린더 등록', 'SCHEDULE', 'P2', FALSE),
    ('ACT_260409_A00003', 'ANL_260409_A00003', '주간 보고서 작성 및 제출', 'SUBMIT', 'P2', FALSE),
    ('ACT_260409_A00004', 'ANL_260409_A00004', '첨부 계약서 검토', 'REVIEW', 'P1', FALSE),
    ('ACT_260409_A00005', 'ANL_260409_A00005', '공유 문서 읽기', 'READ', 'P3', FALSE),
    ('ACT_260409_A00006', 'ANL_260409_A00006', '장애 대응 우선 수행', 'ESCALATE', 'P1', FALSE),
    ('ACT_260409_A00007', 'ANL_260409_A00007', '제안서 수정 포인트 반영', 'EDIT', 'P2', FALSE),
    ('ACT_260409_A00008', 'ANL_260409_A00008', '세금계산서 증빙 보관', 'ARCHIVE', 'P3', FALSE),
    ('ACT_260409_A00009', 'ANL_260409_A00009', '면접 가능 시간 회신', 'REPLY', 'P2', FALSE);

INSERT INTO labels (id, label_name, label_code, label_type, color) VALUES
    ('LBL_260409_A00001', '긴급', 'URGENT', 'SYSTEM', '#E74C3C'),
    ('LBL_260409_A00002', '요청', 'REQUEST', 'SYSTEM', '#F39C12'),
    ('LBL_260409_A00003', '보고', 'REPORT', 'SYSTEM', '#3498DB'),
    ('LBL_260409_A00004', '회의', 'MEETING', 'SYSTEM', '#2ECC71'),
    ('LBL_260409_A00005', '재무', 'FINANCE', 'SYSTEM', '#8E44AD');

INSERT INTO email_labels (id, email_id, label_id, confidence_score) VALUES
    ('ELB_260409_A00001', 'EML_260409_A00001', 'LBL_260409_A00002', 95.00),
    ('ELB_260409_A00002', 'EML_260409_A00002', 'LBL_260409_A00004', 93.00),
    ('ELB_260409_A00003', 'EML_260409_A00003', 'LBL_260409_A00003', 91.00),
    ('ELB_260409_A00004', 'EML_260409_A00004', 'LBL_260409_A00002', 94.00),
    ('ELB_260409_A00005', 'EML_260409_A00005', 'LBL_260409_A00003', 90.00),
    ('ELB_260409_A00006', 'EML_260409_A00006', 'LBL_260409_A00001', 97.00),
    ('ELB_260409_A00007', 'EML_260409_A00007', 'LBL_260409_A00002', 92.00),
    ('ELB_260409_A00008', 'EML_260409_A00008', 'LBL_260409_A00005', 89.00),
    ('ELB_260409_A00009', 'EML_260409_A00009', 'LBL_260409_A00002', 94.00);

INSERT INTO analysis_jobs (
    id, email_id, job_type, status, priority, retry_count, max_retries, worker_id,
    started_at, completed_at
) VALUES
    ('JOB_260409_A00001', 'EML_260409_A00001', 'EMAIL_ANALYSIS', 'COMPLETED', 1, 0, 3, 'worker-analysis-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00002', 'EML_260409_A00002', 'EMAIL_ANALYSIS', 'COMPLETED', 3, 0, 3, 'worker-analysis-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00003', 'EML_260409_A00003', 'EMAIL_ANALYSIS', 'COMPLETED', 2, 0, 3, 'worker-analysis-1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00004', 'EML_260409_A00004', 'EMAIL_ANALYSIS', 'COMPLETED', 1, 0, 3, 'worker-analysis-2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00005', 'EML_260409_A00005', 'EMAIL_ANALYSIS', 'COMPLETED', 4, 0, 3, 'worker-analysis-2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00006', 'EML_260409_A00006', 'EMAIL_ANALYSIS', 'COMPLETED', 1, 0, 3, 'worker-analysis-2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00007', 'EML_260409_A00007', 'EMAIL_ANALYSIS', 'COMPLETED', 2, 0, 3, 'worker-analysis-3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00008', 'EML_260409_A00008', 'EMAIL_ANALYSIS', 'COMPLETED', 5, 0, 3, 'worker-analysis-3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('JOB_260409_A00009', 'EML_260409_A00009', 'EMAIL_ANALYSIS', 'COMPLETED', 2, 0, 3, 'worker-analysis-3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
