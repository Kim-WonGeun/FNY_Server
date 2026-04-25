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

INSERT INTO prompt_templates (
    id, prompt_code, prompt_name, prompt_type, version, model_name,
    role_content, policy_content, guide_content, output_content, active
) VALUES (
    'PRM_260413_A00001',
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

INSERT INTO emails (
    id, mail_account_id, external_message_id, external_thread_id, internet_message_id, subject, body_text,
    body_html, message_snippet, from_name, from_email, received_at, sent_at, is_read, is_starred,
    has_attachment, importance_header, raw_payload
) VALUES
    ('EML_260409_A00001', 'MAC_260409_A00001', 'MSG_MAC_1_1', 'THR_MAC_1', '<msg1@test.com>', '업무 요청 메일', '안녕하세요. 금일 중 확인이 필요한 업무 요청 사항을 전달드립니다. 고객사에서 오후 회의 전에 진행 가능 여부를 확인하고 싶어 하며, 내부 검토가 늦어질 경우 일정 조정 안내가 필요합니다. 확인 대상은 요청 범위, 담당자 배정, 금주 처리 가능 여부입니다. 특히 외부 공유 자료에 포함될 수 있는 문구가 있어 법무 검토 필요 여부도 함께 판단해 주세요. 확인 후 가능하면 오늘 오전 중 1차 의견을 회신 부탁드립니다. 회신 내용에 따라 제가 고객사 전달 문안을 정리하겠습니다.', '<p>안녕하세요. 금일 중 확인이 필요한 업무 요청 사항을 전달드립니다. 고객사에서 오후 회의 전에 진행 가능 여부를 확인하고 싶어 하며, 내부 검토가 늦어질 경우 일정 조정 안내가 필요합니다. 확인 대상은 요청 범위, 담당자 배정, 금주 처리 가능 여부입니다. 특히 외부 공유 자료에 포함될 수 있는 문구가 있어 법무 검토 필요 여부도 함께 판단해 주세요. 확인 후 가능하면 오늘 오전 중 1차 의견을 회신 부탁드립니다. 회신 내용에 따라 제가 고객사 전달 문안을 정리하겠습니다.</p>', '금일 중 확인 부탁드립니다.', '김부장', 'boss@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, TRUE, FALSE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00002', 'MAC_260409_A00001', 'MSG_MAC_1_2', 'THR_MAC_1', '<msg2@test.com>', '회의 일정 안내', '안녕하세요. 내일 오전 10시에 진행할 정기 회의 일정을 안내드립니다. 이번 회의에서는 메일함 화면 개선 현황, 분석 결과 노출 방식, 보고서 생성 기능의 남은 이슈를 순서대로 확인할 예정입니다. 각 담당자는 본인 업무의 완료 항목과 지연 항목을 간단히 정리해 참석해 주세요. 회의 전까지 공유 문서에 논의가 필요한 안건을 추가해 주시면 우선순위에 맞춰 진행하겠습니다. 참석이 어려운 경우 오늘 중 대리 참석자 또는 사전 의견을 남겨 주세요.', '<p>안녕하세요. 내일 오전 10시에 진행할 정기 회의 일정을 안내드립니다. 이번 회의에서는 메일함 화면 개선 현황, 분석 결과 노출 방식, 보고서 생성 기능의 남은 이슈를 순서대로 확인할 예정입니다. 각 담당자는 본인 업무의 완료 항목과 지연 항목을 간단히 정리해 참석해 주세요. 회의 전까지 공유 문서에 논의가 필요한 안건을 추가해 주시면 우선순위에 맞춰 진행하겠습니다. 참석이 어려운 경우 오늘 중 대리 참석자 또는 사전 의견을 남겨 주세요.</p>', '내일 오전 10시 회의 예정입니다.', '김과장', 'manager@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, FALSE, FALSE, 'normal', '{"provider":"gmail"}'),
    ('EML_260409_A00003', 'MAC_260409_A00001', 'MSG_MAC_1_3', 'THR_MAC_2', '<msg3@test.com>', '보고 요청', '안녕하세요. 이번 주 업무 진행 내용을 바탕으로 주간 보고서 초안을 제출 부탁드립니다. 보고서에는 금주 완료한 항목, 진행 중인 항목, 다음 주 계획, 특이사항을 구분해서 작성해 주세요. 특히 메일 분석 결과에서 실제로 업무 결정에 도움이 되었던 내용과 아직 보완이 필요한 부분을 함께 정리하면 좋겠습니다. 고객 대응이나 일정 지연 가능성이 있는 항목은 별도 표시해 주세요. 오늘 퇴근 전까지 초안을 공유해 주시면 제가 최종 보고 자료에 반영하겠습니다.', '<p>안녕하세요. 이번 주 업무 진행 내용을 바탕으로 주간 보고서 초안을 제출 부탁드립니다. 보고서에는 금주 완료한 항목, 진행 중인 항목, 다음 주 계획, 특이사항을 구분해서 작성해 주세요. 특히 메일 분석 결과에서 실제로 업무 결정에 도움이 되었던 내용과 아직 보완이 필요한 부분을 함께 정리하면 좋겠습니다. 고객 대응이나 일정 지연 가능성이 있는 항목은 별도 표시해 주세요. 오늘 퇴근 전까지 초안을 공유해 주시면 제가 최종 보고 자료에 반영하겠습니다.</p>', '주간 보고서 제출 바랍니다.', '이팀장', 'leader@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, FALSE, TRUE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00004', 'MAC_260409_A00002', 'MSG_MAC_2_1', 'THR_MAC_3', '<msg4@test.com>', '계약 검토 요청', '안녕하세요. 첨부된 계약서의 수정 조항에 대한 검토를 부탁드립니다. 이번 변경안에는 서비스 제공 범위, 장애 발생 시 고지 기준, 데이터 보관 기간, 비용 정산 방식이 포함되어 있습니다. 이전 버전과 비교했을 때 책임 범위가 넓어지는 표현이 있는지 확인이 필요합니다. 고객사 회신 기한이 가까워서 법무 검토 의견을 받은 뒤 영업팀과 최종 문안을 조율하려고 합니다. 위험 문구가 있다면 대체 문안까지 함께 제안해 주시면 감사하겠습니다.', '<p>안녕하세요. 첨부된 계약서의 수정 조항에 대한 검토를 부탁드립니다. 이번 변경안에는 서비스 제공 범위, 장애 발생 시 고지 기준, 데이터 보관 기간, 비용 정산 방식이 포함되어 있습니다. 이전 버전과 비교했을 때 책임 범위가 넓어지는 표현이 있는지 확인이 필요합니다. 고객사 회신 기한이 가까워서 법무 검토 의견을 받은 뒤 영업팀과 최종 문안을 조율하려고 합니다. 위험 문구가 있다면 대체 문안까지 함께 제안해 주시면 감사하겠습니다.</p>', '첨부된 계약서 확인 부탁드립니다.', '법무팀', 'legal@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, TRUE, TRUE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00005', 'MAC_260409_A00002', 'MSG_MAC_2_2', 'THR_MAC_4', '<msg5@test.com>', '주간 회고 공유', '안녕하세요. 이번 주 회고 문서를 공유드립니다. 전체적으로 메일 목록과 상세 본문 확인 흐름은 안정화되었고, 보고서 생성 화면의 사용성도 개선되었습니다. 다만 대량 메일을 조회할 때 사용자가 어떤 메일이 분석 근거로 쓰였는지 더 명확히 볼 수 있어야 한다는 의견이 있었습니다. 다음 주에는 기간 필터, 페이징 상태 유지, 분석 키워드 표현 방식을 추가로 점검하면 좋겠습니다. 문서에 의견을 남겨 주시면 다음 회고 때 함께 확인하겠습니다.', '<p>안녕하세요. 이번 주 회고 문서를 공유드립니다. 전체적으로 메일 목록과 상세 본문 확인 흐름은 안정화되었고, 보고서 생성 화면의 사용성도 개선되었습니다. 다만 대량 메일을 조회할 때 사용자가 어떤 메일이 분석 근거로 쓰였는지 더 명확히 볼 수 있어야 한다는 의견이 있었습니다. 다음 주에는 기간 필터, 페이징 상태 유지, 분석 키워드 표현 방식을 추가로 점검하면 좋겠습니다. 문서에 의견을 남겨 주시면 다음 회고 때 함께 확인하겠습니다.</p>', '이번 주 회고 문서를 공유드립니다.', 'PM', 'pm@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, FALSE, FALSE, 'normal', '{"provider":"gmail"}'),
    ('EML_260409_A00006', 'MAC_260409_A00002', 'MSG_MAC_2_3', 'THR_MAC_5', '<msg6@test.com>', '긴급 장애 공지', '안녕하세요. 금일 오전 일부 사용자에게 메일 분석 결과가 지연 노출되는 문제가 확인되었습니다. 현재 원인은 에이전트 응답 대기 시간이 길어진 상황과 서버 재시도 설정이 겹친 것으로 보고 있습니다. 우선 임시 조치로 분석 요청 큐를 분리하고, 실패한 작업은 재처리 대상으로 표시했습니다. 영향 범위와 재발 방지 대책을 정리해 오후 3시까지 공유 부탁드립니다. 고객 문의가 들어올 수 있으니 CS팀에 전달할 안내 문구도 함께 준비해 주세요.', '<p>안녕하세요. 금일 오전 일부 사용자에게 메일 분석 결과가 지연 노출되는 문제가 확인되었습니다. 현재 원인은 에이전트 응답 대기 시간이 길어진 상황과 서버 재시도 설정이 겹친 것으로 보고 있습니다. 우선 임시 조치로 분석 요청 큐를 분리하고, 실패한 작업은 재처리 대상으로 표시했습니다. 영향 범위와 재발 방지 대책을 정리해 오후 3시까지 공유 부탁드립니다. 고객 문의가 들어올 수 있으니 CS팀에 전달할 안내 문구도 함께 준비해 주세요.</p>', '서비스 장애 대응이 필요합니다.', '운영팀', 'ops@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, TRUE, FALSE, 'high', '{"provider":"gmail"}'),
    ('EML_260409_A00007', 'MAC_260409_A00003', 'MSG_MAC_3_1', 'THR_MAC_6', '<msg7@test.com>', '제안서 피드백', '안녕하세요. 전달주신 제안서 초안을 검토했고 수정 요청사항을 정리해 공유드립니다. 기능 설명은 전반적으로 이해하기 쉽지만, 메일 연동 후 분석 결과가 어떤 기준으로 우선순위화되는지 사례가 조금 더 필요해 보입니다. 보고서 생성 기능은 금주실적, 차주계획, 특이사항을 자동으로 뽑아주는 흐름을 예시로 넣으면 설득력이 높아질 것 같습니다. 예상 도입 일정과 보안 검토 절차도 별도 페이지로 분리해 주세요. 수정본은 이번 주 금요일까지 다시 확인하겠습니다.', '<p>안녕하세요. 전달주신 제안서 초안을 검토했고 수정 요청사항을 정리해 공유드립니다. 기능 설명은 전반적으로 이해하기 쉽지만, 메일 연동 후 분석 결과가 어떤 기준으로 우선순위화되는지 사례가 조금 더 필요해 보입니다. 보고서 생성 기능은 금주실적, 차주계획, 특이사항을 자동으로 뽑아주는 흐름을 예시로 넣으면 설득력이 높아질 것 같습니다. 예상 도입 일정과 보안 검토 절차도 별도 페이지로 분리해 주세요. 수정본은 이번 주 금요일까지 다시 확인하겠습니다.</p>', '제안서 수정 요청사항 전달드립니다.', '고객사', 'client@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, FALSE, TRUE, 'normal', '{"provider":"outlook"}'),
    ('EML_260409_A00008', 'MAC_260409_A00003', 'MSG_MAC_3_2', 'THR_MAC_7', '<msg8@test.com>', '세금계산서 발행 안내', '안녕하세요. 요청하신 세금계산서 발행이 완료되어 안내드립니다. 이번 발행 건에는 3월 사용료, 추가 분석 작업 비용, 인프라 사용량 증가분이 포함되어 있습니다. 비용 코드와 프로젝트 코드가 내부 정산 기준과 일치하는지 확인해 주세요. 만약 발행 정보나 담당자 정보에 수정이 필요한 경우 오늘 중 알려주시면 재발행 절차를 진행하겠습니다. 증빙 파일은 회계 시스템에도 업로드해 두었으니 필요 시 내려받아 보관 부탁드립니다.', '<p>안녕하세요. 요청하신 세금계산서 발행이 완료되어 안내드립니다. 이번 발행 건에는 3월 사용료, 추가 분석 작업 비용, 인프라 사용량 증가분이 포함되어 있습니다. 비용 코드와 프로젝트 코드가 내부 정산 기준과 일치하는지 확인해 주세요. 만약 발행 정보나 담당자 정보에 수정이 필요한 경우 오늘 중 알려주시면 재발행 절차를 진행하겠습니다. 증빙 파일은 회계 시스템에도 업로드해 두었으니 필요 시 내려받아 보관 부탁드립니다.</p>', '세금계산서 발행이 완료되었습니다.', '재무팀', 'finance@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE, FALSE, TRUE, 'low', '{"provider":"outlook"}'),
    ('EML_260409_A00009', 'MAC_260409_A00003', 'MSG_MAC_3_3', 'THR_MAC_8', '<msg9@test.com>', '면접 일정 조율', '안녕하세요. 다음 주 진행 예정인 백엔드 개발자 면접 일정 조율을 부탁드립니다. 후보자는 화요일 오후와 목요일 오전 참석이 가능하다고 회신했습니다. 면접에서는 Spring Boot 기반 API 설계 경험, 메일 연동 처리 방식, 비동기 분석 작업 운영 경험을 중점적으로 확인하려고 합니다. 참석 가능한 시간대를 오늘 중 알려주시면 캘린더 초대와 사전 과제 안내를 발송하겠습니다. 면접 질문에 추가하고 싶은 내용이 있으면 함께 전달해 주세요.', '<p>안녕하세요. 다음 주 진행 예정인 백엔드 개발자 면접 일정 조율을 부탁드립니다. 후보자는 화요일 오후와 목요일 오전 참석이 가능하다고 회신했습니다. 면접에서는 Spring Boot 기반 API 설계 경험, 메일 연동 처리 방식, 비동기 분석 작업 운영 경험을 중점적으로 확인하려고 합니다. 참석 가능한 시간대를 오늘 중 알려주시면 캘린더 초대와 사전 과제 안내를 발송하겠습니다. 면접 질문에 추가하고 싶은 내용이 있으면 함께 전달해 주세요.</p>', '면접 가능 시간을 회신 부탁드립니다.', '인사팀', 'hr@test.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE, FALSE, FALSE, 'high', '{"provider":"outlook"}');

INSERT INTO emails (
    id, mail_account_id, external_message_id, external_thread_id, internet_message_id, subject, body_text,
    body_html, message_snippet, from_name, from_email, received_at, sent_at, is_read, is_starred,
    has_attachment, importance_header, raw_payload
)
SELECT
    'EML_260409_A' || LPAD(CAST(X AS VARCHAR), 5, '0'),
    'MAC_260409_A00001',
    'MSG_MAC_1_' || X,
    'THR_MAC_EXTRA_' || MOD(X, 8),
    '<msg' || X || '@test.com>',
    CASE MOD(X, 6)
        WHEN 0 THEN '긴급 승인 요청'
        WHEN 1 THEN '고객 미팅 후속 정리'
        WHEN 2 THEN '정산 자료 확인'
        WHEN 3 THEN '개발 일정 공유'
        WHEN 4 THEN '계약 변경 검토'
        ELSE '주간 업무 업데이트'
    END,
    CASE MOD(X, 6)
        WHEN 0 THEN '안녕하세요. 오늘 중 승인 여부 확인 부탁드립니다. 고객사 전달 일정이 오후에 잡혀 있어 지연 시 별도 안내가 필요합니다. 현재 검토가 필요한 항목은 예산 범위, 담당자 배정, 외부 공유 가능 여부입니다. 승인 대상에는 신규 분석 리포트 샘플과 고객사 전용 안내 문구가 포함되어 있으며, 보류가 필요한 경우 사유를 명확히 남겨야 합니다. 급한 건이라 가능하면 점심 전까지 1차 의견을 부탁드립니다. 승인 가능 여부, 보완 필요 항목, 고객사에 바로 전달 가능한 범위를 구분해 회신해 주세요. 회신을 받으면 제가 최종 문안을 정리하고 오후 미팅 전에 공유하겠습니다.'
        WHEN 1 THEN '안녕하세요. 고객 미팅에서 나온 요청사항을 정리했습니다. 우선순위가 높은 항목은 사용량 리포트 개선, 알림 문구 조정, 관리자 권한 범위 확인입니다. 고객사는 특히 메일 분석 결과가 어떤 근거로 긴급하다고 판단되는지 설명 가능한 형태를 원하고 있습니다. 담당자별 후속 액션을 확인해 주시고, 이번 주 안에 처리 가능한 범위와 다음 주로 넘겨야 하는 항목을 구분해 주세요. 추가로 데모 환경에서 재현 가능한 시나리오가 있으면 함께 적어 주시면 좋겠습니다. 회신 내용을 바탕으로 고객사에 진행 계획과 예상 일정을 공유하겠습니다.'
        WHEN 2 THEN '안녕하세요. 이번 달 정산 자료와 세금계산서 발행 내역을 확인 부탁드립니다. 일부 항목의 비용 코드가 이전 달과 다르게 표시되어 있어 재무팀 확인이 필요합니다. 첨부된 집계 기준에는 인프라 사용량, 외부 API 호출 비용, 테스트 계정 사용분이 포함되어 있습니다. 누락된 항목이나 중복 청구 가능성이 있는 항목이 있는지 봐 주세요. 확인 완료 후 증빙 보관 여부와 재발행 필요 여부도 함께 알려주시면 감사하겠습니다. 만약 정산 기준 변경이 필요하다면 다음 월 마감 전에 반영할 수 있도록 별도 코멘트를 남겨 주세요.'
        WHEN 3 THEN '안녕하세요. 다음 배포 일정과 QA 진행 상황을 공유드립니다. 현재 핵심 기능 개발은 대부분 완료되었고, 남은 작업은 권한 예외 처리와 모바일 화면 확인입니다. QA에서 발견된 위험 요소는 메일 원문 펼침 영역의 긴 본문 표시, 보고서 생성 후 이전 결과 선택, 기간 필터 초기화 동작입니다. 배포 전까지 우선 처리해야 하는 항목과 차주로 넘겨도 되는 항목을 구분해 주세요. 각 담당자는 오늘 오후까지 상태를 업데이트해 주시고, 차단 이슈가 있으면 원인과 필요한 지원을 함께 남겨 주세요. 최종 배포 여부는 내일 오전 회의에서 결정하겠습니다.'
        WHEN 4 THEN '안녕하세요. 계약 변경 조항 검토가 필요합니다. 변경된 내용은 서비스 제공 범위, 장애 고지 기준, 데이터 보관 기간과 관련되어 있습니다. 특히 분석 결과가 자동 생성되는 특성상 고객에게 제공되는 요약 정보의 책임 범위를 어디까지 볼 것인지 확인해야 합니다. 법무 검토 이후 영업팀과 고객사에 전달할 예정이니 의견 있으시면 회신 부탁드립니다. 책임 범위가 넓어지는 문구, 보안 검토가 필요한 표현, 비용 정산에 영향을 주는 조항을 중점적으로 봐 주세요. 수정 의견은 조항 번호와 함께 남겨 주시면 최종본 반영이 빠를 것 같습니다.'
        ELSE '안녕하세요. 이번 주 진행 상황과 다음 주 예정 업무를 공유드립니다. 주요 완료 항목은 메일 목록 화면 개선, 주간보고 초안 생성, 분석 결과 저장 흐름 점검입니다. 특히 전체 메일함에서 원문과 요약을 함께 확인할 수 있게 되었고, 보고서 생성 메뉴에서는 선택한 기간의 메일을 바탕으로 금주실적과 차주계획을 정리할 수 있게 되었습니다. 다음 주에는 실제 메일 연동 범위, 에이전트 분석 품질, 대량 메일 페이징 성능을 함께 확인할 예정입니다. 누락된 내용이나 추가해야 할 업무가 있으면 회신 부탁드립니다. 회신 내용은 다음 주 계획 항목에 반영하겠습니다.'
    END,
    '<p>' ||
    CASE MOD(X, 6)
        WHEN 0 THEN '안녕하세요. 오늘 중 승인 여부 확인 부탁드립니다. 고객사 전달 일정이 오후에 잡혀 있어 지연 시 별도 안내가 필요합니다. 현재 검토가 필요한 항목은 예산 범위, 담당자 배정, 외부 공유 가능 여부입니다. 승인 대상에는 신규 분석 리포트 샘플과 고객사 전용 안내 문구가 포함되어 있으며, 보류가 필요한 경우 사유를 명확히 남겨야 합니다. 급한 건이라 가능하면 점심 전까지 1차 의견을 부탁드립니다. 승인 가능 여부, 보완 필요 항목, 고객사에 바로 전달 가능한 범위를 구분해 회신해 주세요. 회신을 받으면 제가 최종 문안을 정리하고 오후 미팅 전에 공유하겠습니다.'
        WHEN 1 THEN '안녕하세요. 고객 미팅에서 나온 요청사항을 정리했습니다. 우선순위가 높은 항목은 사용량 리포트 개선, 알림 문구 조정, 관리자 권한 범위 확인입니다. 고객사는 특히 메일 분석 결과가 어떤 근거로 긴급하다고 판단되는지 설명 가능한 형태를 원하고 있습니다. 담당자별 후속 액션을 확인해 주시고, 이번 주 안에 처리 가능한 범위와 다음 주로 넘겨야 하는 항목을 구분해 주세요. 추가로 데모 환경에서 재현 가능한 시나리오가 있으면 함께 적어 주시면 좋겠습니다. 회신 내용을 바탕으로 고객사에 진행 계획과 예상 일정을 공유하겠습니다.'
        WHEN 2 THEN '안녕하세요. 이번 달 정산 자료와 세금계산서 발행 내역을 확인 부탁드립니다. 일부 항목의 비용 코드가 이전 달과 다르게 표시되어 있어 재무팀 확인이 필요합니다. 첨부된 집계 기준에는 인프라 사용량, 외부 API 호출 비용, 테스트 계정 사용분이 포함되어 있습니다. 누락된 항목이나 중복 청구 가능성이 있는 항목이 있는지 봐 주세요. 확인 완료 후 증빙 보관 여부와 재발행 필요 여부도 함께 알려주시면 감사하겠습니다. 만약 정산 기준 변경이 필요하다면 다음 월 마감 전에 반영할 수 있도록 별도 코멘트를 남겨 주세요.'
        WHEN 3 THEN '안녕하세요. 다음 배포 일정과 QA 진행 상황을 공유드립니다. 현재 핵심 기능 개발은 대부분 완료되었고, 남은 작업은 권한 예외 처리와 모바일 화면 확인입니다. QA에서 발견된 위험 요소는 메일 원문 펼침 영역의 긴 본문 표시, 보고서 생성 후 이전 결과 선택, 기간 필터 초기화 동작입니다. 배포 전까지 우선 처리해야 하는 항목과 차주로 넘겨도 되는 항목을 구분해 주세요. 각 담당자는 오늘 오후까지 상태를 업데이트해 주시고, 차단 이슈가 있으면 원인과 필요한 지원을 함께 남겨 주세요. 최종 배포 여부는 내일 오전 회의에서 결정하겠습니다.'
        WHEN 4 THEN '안녕하세요. 계약 변경 조항 검토가 필요합니다. 변경된 내용은 서비스 제공 범위, 장애 고지 기준, 데이터 보관 기간과 관련되어 있습니다. 특히 분석 결과가 자동 생성되는 특성상 고객에게 제공되는 요약 정보의 책임 범위를 어디까지 볼 것인지 확인해야 합니다. 법무 검토 이후 영업팀과 고객사에 전달할 예정이니 의견 있으시면 회신 부탁드립니다. 책임 범위가 넓어지는 문구, 보안 검토가 필요한 표현, 비용 정산에 영향을 주는 조항을 중점적으로 봐 주세요. 수정 의견은 조항 번호와 함께 남겨 주시면 최종본 반영이 빠를 것 같습니다.'
        ELSE '안녕하세요. 이번 주 진행 상황과 다음 주 예정 업무를 공유드립니다. 주요 완료 항목은 메일 목록 화면 개선, 주간보고 초안 생성, 분석 결과 저장 흐름 점검입니다. 특히 전체 메일함에서 원문과 요약을 함께 확인할 수 있게 되었고, 보고서 생성 메뉴에서는 선택한 기간의 메일을 바탕으로 금주실적과 차주계획을 정리할 수 있게 되었습니다. 다음 주에는 실제 메일 연동 범위, 에이전트 분석 품질, 대량 메일 페이징 성능을 함께 확인할 예정입니다. 누락된 내용이나 추가해야 할 업무가 있으면 회신 부탁드립니다. 회신 내용은 다음 주 계획 항목에 반영하겠습니다.'
    END || '</p>',
    CASE MOD(X, 6)
        WHEN 0 THEN '오늘 중 승인 여부 확인 부탁드립니다.'
        WHEN 1 THEN '고객 미팅 후속 액션을 확인해 주세요.'
        WHEN 2 THEN '정산 자료와 세금계산서 내역 확인 요청'
        WHEN 3 THEN '배포 일정과 QA 진행 상황 공유'
        WHEN 4 THEN '계약 변경 조항 검토 요청'
        ELSE '주간 진행 상황과 예정 업무 공유'
    END,
    CASE MOD(X, 6)
        WHEN 0 THEN '대표이사'
        WHEN 1 THEN '고객성공팀'
        WHEN 2 THEN '재무팀'
        WHEN 3 THEN '개발리드'
        WHEN 4 THEN '법무팀'
        ELSE '홍길동1'
    END,
    CASE MOD(X, 6)
        WHEN 0 THEN 'ceo@test.com'
        WHEN 1 THEN 'cs@test.com'
        WHEN 2 THEN 'finance@test.com'
        WHEN 3 THEN 'devlead@test.com'
        WHEN 4 THEN 'legal@test.com'
        ELSE 'user1@test.com'
    END,
    DATEADD('MINUTE', -X * 17, CURRENT_TIMESTAMP),
    DATEADD('MINUTE', -X * 18, CURRENT_TIMESTAMP),
    CASE WHEN MOD(X, 3) = 0 THEN TRUE ELSE FALSE END,
    CASE WHEN MOD(X, 10) = 0 THEN TRUE ELSE FALSE END,
    CASE WHEN MOD(X, 4) = 0 THEN TRUE ELSE FALSE END,
    CASE WHEN MOD(X, 6) IN (0, 4) THEN 'high' WHEN MOD(X, 6) = 2 THEN 'low' ELSE 'normal' END,
    '{"provider":"gmail","seed":"bulk"}'
FROM SYSTEM_RANGE(10, 106);

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

INSERT INTO email_recipients (id, email_id, recipient_type, recipient_name, recipient_email)
SELECT
    'ERC_260409_A' || LPAD(CAST(X AS VARCHAR), 5, '0'),
    'EML_260409_A' || LPAD(CAST(X AS VARCHAR), 5, '0'),
    'TO',
    '나',
    'user1@test.com'
FROM SYSTEM_RANGE(10, 106);

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
