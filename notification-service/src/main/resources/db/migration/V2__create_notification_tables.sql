-- Notification service schema (aligned with docker/init-dbs.sql for tms_notifications).
-- When DB is created by init-dbs.sql, baseline-on-migrate=1 skips this; when DB is empty, this runs after baseline.

CREATE TABLE IF NOT EXISTS notifications (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id       UUID NOT NULL,
  user_id         UUID NOT NULL,
  type            VARCHAR(100) NOT NULL,
  title           VARCHAR(255) NOT NULL,
  message         TEXT NOT NULL,
  reference_id    UUID,
  "read"          BOOLEAN DEFAULT false,
  read_at         TIMESTAMP,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS email_log (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  notification_id UUID NOT NULL REFERENCES notifications(id),
  recipient_email VARCHAR(255) NOT NULL,
  subject         VARCHAR(255) NOT NULL,
  status          VARCHAR(50) NOT NULL,
  resend_id       VARCHAR(255),
  error_message   TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_tenant_id ON notifications(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_email_log_notification_id ON email_log(notification_id);
