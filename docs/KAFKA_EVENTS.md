# Kafka events (TMS)

Event contracts published by TMS services for consumers (e.g. notification-service).

## Topic: `ticket.status.changed`

Published by **ticket-service** when a ticket status is updated (PATCH /tickets/{id}/status).

- **Message key:** `ticketId` (UUID string)
- **Value (JSON):**

| Field         | Type   | Description                          |
|---------------|--------|--------------------------------------|
| eventType     | string | `"TicketStatusChanged"`             |
| eventVersion  | string | `"1.0"`                             |
| ticketId      | string | UUID of the ticket                  |
| userId        | string | UUID of the user who changed status |
| oldStatus     | string | Previous status (e.g. OPEN)         |
| newStatus     | string | New status (e.g. IN_PROGRESS)       |
| timestamp     | string | ISO-8601 UTC (e.g. 2025-03-10T12:00:00.000Z) |

Example:

```json
{
  "eventType": "TicketStatusChanged",
  "eventVersion": "1.0",
  "ticketId": "11111111-1111-1111-1111-111111111111",
  "userId": "22222222-2222-2222-2222-222222222222",
  "oldStatus": "OPEN",
  "newStatus": "IN_PROGRESS",
  "timestamp": "2025-03-10T12:00:00.000Z"
}
```

## Producer configuration (ticket-service)

- **Bootstrap servers:** `spring.kafka.bootstrap-servers` (default: `localhost:9092`)
- **Topic:** `ticket-service.kafka.topic.status-changed` (default: `ticket.status.changed`)
- **Producer:** `acks=all`, `retries=3`, `delivery.timeout.ms=120000`, value serializer: `JacksonJsonSerializer`
