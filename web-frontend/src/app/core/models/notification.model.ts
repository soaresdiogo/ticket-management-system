/**
 * Payload pushed over WebSocket from /topic/notifications.
 * Aligned with backend NotificationPayload.
 */
export interface NotificationPayload {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  referenceId: string | null;
  read: boolean;
  createdAt: string;
}
