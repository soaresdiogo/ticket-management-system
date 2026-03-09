-- ========== Create databases (one per service) ==========
CREATE DATABASE tms_auth;
CREATE DATABASE tms_tickets;
CREATE DATABASE tms_files;
CREATE DATABASE tms_notifications;

-- ========== tms_auth ==========
\connect tms_auth

CREATE TABLE tenants (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(255) NOT NULL,
  email         VARCHAR(255) UNIQUE NOT NULL,
  logo_url      VARCHAR(500),
  active        BOOLEAN DEFAULT true,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     UUID NOT NULL REFERENCES tenants(id),
  email         VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name     VARCHAR(255) NOT NULL,
  role          VARCHAR(50) NOT NULL,
  active        BOOLEAN DEFAULT true,
  first_access  BOOLEAN DEFAULT true,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mfa_codes (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID NOT NULL REFERENCES users(id),
  code          VARCHAR(6) NOT NULL,
  used          BOOLEAN DEFAULT false,
  expires_at    TIMESTAMP NOT NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID NOT NULL REFERENCES users(id),
  token_hash    VARCHAR(255) UNIQUE NOT NULL,
  expires_at    TIMESTAMP NOT NULL,
  revoked       BOOLEAN DEFAULT false,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE auth_audit_log (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID NOT NULL REFERENCES users(id),
  action        VARCHAR(100) NOT NULL,
  ip_address    VARCHAR(45),
  user_agent    VARCHAR(500),
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== tms_tickets ==========
\connect tms_tickets

CREATE TABLE tickets (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  client_id       UUID NOT NULL,
  accountant_id   UUID,
  title           VARCHAR(255) NOT NULL,
  description     TEXT NOT NULL,
  status          VARCHAR(50) NOT NULL,
  priority        VARCHAR(50) DEFAULT 'NORMAL',
  category        VARCHAR(100),
  resolved_at     TIMESTAMP,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_status_history (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id       UUID NOT NULL REFERENCES tickets(id),
  changed_by      UUID NOT NULL,
  old_status      VARCHAR(50) NOT NULL,
  new_status      VARCHAR(50) NOT NULL,
  comment         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ticket_comments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id       UUID NOT NULL REFERENCES tickets(id),
  author_id       UUID NOT NULL,
  author_role     VARCHAR(50) NOT NULL,
  content         TEXT NOT NULL,
  internal        BOOLEAN DEFAULT false,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== tms_files ==========
\connect tms_files

CREATE TABLE attachments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  ticket_id       UUID NOT NULL,
  uploaded_by     UUID NOT NULL,
  uploader_role   VARCHAR(50) NOT NULL,
  file_name       VARCHAR(255) NOT NULL,
  minio_key       VARCHAR(500) NOT NULL,
  mime_type       VARCHAR(100) NOT NULL,
  file_size       BIGINT NOT NULL,
  visible_to      VARCHAR(50) NOT NULL,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at      TIMESTAMP
);

CREATE TABLE download_log (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  attachment_id   UUID NOT NULL REFERENCES attachments(id),
  downloaded_by   UUID NOT NULL,
  ip_address      VARCHAR(45),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========== tms_notifications ==========
\connect tms_notifications

CREATE TABLE notifications (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  user_id         UUID NOT NULL,
  type            VARCHAR(100) NOT NULL,
  title           VARCHAR(255) NOT NULL,
  message         TEXT NOT NULL,
  reference_id    UUID,
  read            BOOLEAN DEFAULT false,
  read_at         TIMESTAMP,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_log (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  notification_id UUID NOT NULL REFERENCES notifications(id),
  recipient_email VARCHAR(255) NOT NULL,
  subject         VARCHAR(255) NOT NULL,
  status          VARCHAR(50) NOT NULL,
  resend_id       VARCHAR(255),
  error_message   TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
