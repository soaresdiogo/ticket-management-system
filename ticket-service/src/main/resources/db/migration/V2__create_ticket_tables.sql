-- Ticket service schema (aligned with docker/init-dbs.sql for tms_tickets).
-- When DB is created by init-dbs.sql, baseline-on-migrate=1 skips this; when DB is empty, this runs after baseline.

CREATE TABLE IF NOT EXISTS tickets (
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

CREATE TABLE IF NOT EXISTS ticket_status_history (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id       UUID NOT NULL REFERENCES tickets(id),
  changed_by      UUID NOT NULL,
  old_status      VARCHAR(50) NOT NULL,
  new_status      VARCHAR(50) NOT NULL,
  comment         TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ticket_comments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ticket_id       UUID NOT NULL REFERENCES tickets(id),
  author_id       UUID NOT NULL,
  author_role     VARCHAR(50) NOT NULL,
  content         TEXT NOT NULL,
  internal        BOOLEAN DEFAULT false,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tickets_tenant_id ON tickets(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tickets_client_id ON tickets(client_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
CREATE INDEX IF NOT EXISTS idx_ticket_status_history_ticket_id ON ticket_status_history(ticket_id);
CREATE INDEX IF NOT EXISTS idx_ticket_comments_ticket_id ON ticket_comments(ticket_id);
