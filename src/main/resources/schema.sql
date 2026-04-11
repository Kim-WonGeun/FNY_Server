DROP TABLE IF EXISTS email_labels;
DROP TABLE IF EXISTS labels;
DROP TABLE IF EXISTS analysis_jobs;
DROP TABLE IF EXISTS email_action_items;
DROP TABLE IF EXISTS email_analysis;
DROP TABLE IF EXISTS email_recipients;
DROP TABLE IF EXISTS emails;
DROP TABLE IF EXISTS mail_accounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS mail_integrations;

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
    access_token_encrypted CLOB,
    refresh_token_encrypted CLOB,
    token_expires_at TIMESTAMP,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_mail_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE emails (
    id VARCHAR(20) NOT NULL,
    mail_account_id VARCHAR(20) NOT NULL,
    external_message_id VARCHAR(255) NOT NULL,
    external_thread_id VARCHAR(255),
    internet_message_id VARCHAR(500),
    subject VARCHAR(500),
    body_text CLOB,
    body_html CLOB,
    message_snippet VARCHAR(1000),
    from_name VARCHAR(255),
    from_email VARCHAR(255) NOT NULL,
    received_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_starred BOOLEAN NOT NULL DEFAULT FALSE,
    has_attachment BOOLEAN NOT NULL DEFAULT FALSE,
    importance_header VARCHAR(50),
    raw_payload CLOB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_emails_mail_account FOREIGN KEY (mail_account_id) REFERENCES mail_accounts (id)
);

CREATE TABLE email_recipients (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    recipient_type VARCHAR(10) NOT NULL,
    recipient_name VARCHAR(255),
    recipient_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_email_recipients_email FOREIGN KEY (email_id) REFERENCES emails (id)
);

CREATE TABLE email_analysis (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    analysis_version INT NOT NULL DEFAULT 1,
    is_latest BOOLEAN NOT NULL DEFAULT TRUE,
    model_name VARCHAR(100),
    prompt_version VARCHAR(50),
    short_summary VARCHAR(1000),
    detailed_summary CLOB,
    category VARCHAR(50),
    priority_level VARCHAR(10),
    importance_score DECIMAL(5,2),
    urgency_score DECIMAL(5,2),
    confidence_score DECIMAL(5,2),
    needs_reply BOOLEAN,
    has_deadline BOOLEAN,
    deadline_at TIMESTAMP,
    suggested_action VARCHAR(255),
    reasoning CLOB,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_email_analysis_email FOREIGN KEY (email_id) REFERENCES emails (id)
);

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
    PRIMARY KEY (id),
    CONSTRAINT fk_email_action_items_analysis FOREIGN KEY (analysis_id) REFERENCES email_analysis (id)
);

CREATE TABLE analysis_jobs (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    job_type VARCHAR(50) NOT NULL DEFAULT 'EMAIL_ANALYSIS',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INT NOT NULL DEFAULT 5,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    worker_id VARCHAR(100),
    error_message CLOB,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_analysis_jobs_email FOREIGN KEY (email_id) REFERENCES emails (id)
);

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

CREATE TABLE email_labels (
    id VARCHAR(20) NOT NULL,
    email_id VARCHAR(20) NOT NULL,
    label_id VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_email_labels_email FOREIGN KEY (email_id) REFERENCES emails (id),
    CONSTRAINT fk_email_labels_label FOREIGN KEY (label_id) REFERENCES labels (id)
);

CREATE TABLE mail_integrations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    provider_type VARCHAR(30) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    email_address VARCHAR(150) NOT NULL,
    smtp_host VARCHAR(150) NOT NULL,
    smtp_port INTEGER NOT NULL,
    smtp_username VARCHAR(150) NOT NULL,
    smtp_password VARCHAR(255) NOT NULL,
    protocol VARCHAR(20) NOT NULL,
    tls_enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
