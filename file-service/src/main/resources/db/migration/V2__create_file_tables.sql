-- File service schema (aligned with docker/init-dbs.sql for tms_files).
-- When DB is created by init-dbs.sql, baseline-on-migrate=1 skips this; when DB is empty, this runs after baseline.

CREATE TABLE IF NOT EXISTS attachments (
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

CREATE TABLE IF NOT EXISTS download_log (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  attachment_id   UUID NOT NULL REFERENCES attachments(id),
  downloaded_by   UUID NOT NULL,
  ip_address      VARCHAR(45),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_attachments_tenant_id ON attachments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_attachments_ticket_id ON attachments(ticket_id);
CREATE INDEX IF NOT EXISTS idx_attachments_created_at ON attachments(created_at);
CREATE INDEX IF NOT EXISTS idx_download_log_attachment_id ON download_log(attachment_id);
