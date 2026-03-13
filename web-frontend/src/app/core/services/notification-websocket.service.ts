import { Injectable, inject, NgZone, OnDestroy } from '@angular/core';
import { Observable, Subject, share, takeUntil } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import type { NotificationPayload } from '../models/notification.model';
import { AuthService } from './auth.service';

const WS_ENDPOINT = '/ws';
const TOPIC_NOTIFICATIONS = '/topic/notifications';

/**
 * Connects to the notification WebSocket (STOMP over SockJS), subscribes to /topic/notifications,
 * and exposes notifications as an Observable. Single responsibility: connection and subscription only.
 * Sends the JWT as a query param so the gateway can authenticate the handshake (browser cannot set headers on WS).
 */
@Injectable({ providedIn: 'root' })
export class NotificationWebSocketService implements OnDestroy {
  private readonly ngZone = inject(NgZone);
  private readonly auth = inject(AuthService);
  private readonly destroy$ = new Subject<void>();

  private readonly notificationsSubject = new Subject<NotificationPayload>();
  /** Stream of notifications from the server. Completes when service is destroyed. */
  readonly notifications$: Observable<NotificationPayload> = this.notificationsSubject
    .asObservable()
    .pipe(share(), takeUntil(this.destroy$));

  private client: Client | null = null;
  private connected = false;
  private retryCount = 0;
  private static readonly MAX_RETRIES = 5;
  private static readonly RETRY_DELAY_MS = 10_000;

  /**
   * Connects to /ws with SockJS and STOMP, then subscribes to /topic/notifications.
   * Safe to call multiple times; subsequent calls are no-ops if already connected.
   * If the notification service is down (e.g. connection refused on 8084), retries with backoff.
   */
  connect(): void {
    if (this.connected && this.client?.active) {
      return;
    }

    this.ngZone.runOutsideAngular(() => {
      const socket = new SockJS(this.wsUrl());
      const stompClient = new Client({
        webSocketFactory: () => socket as unknown as WebSocket,
        onConnect: () => {
          this.ngZone.run(() => {
            this.connected = true;
            this.retryCount = 0;
            this.subscribeToNotifications(stompClient);
          });
        },
        onStompError: () => {
          this.ngZone.run(() => {
            this.connected = false;
            this.scheduleRetry();
          });
        },
        onWebSocketError: () => {
          this.ngZone.run(() => {
            this.connected = false;
            this.scheduleRetry();
          });
        },
      });
      stompClient.activate();
      this.client = stompClient;
    });
  }

  private scheduleRetry(): void {
    if (this.retryCount >= NotificationWebSocketService.MAX_RETRIES) return;
    this.retryCount += 1;
    this.ngZone.runOutsideAngular(() => {
      setTimeout(() => {
        if (this.connected || this.destroy$.closed) return;
        this.disconnect();
        this.connect();
      }, NotificationWebSocketService.RETRY_DELAY_MS);
    });
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connected = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.disconnect();
  }

  private wsUrl(): string {
    if (globalThis.window === undefined) return '';
    const base = globalThis.location.origin;
    const token = this.auth.getAccessToken();
    const path = token ? `${WS_ENDPOINT}?access_token=${encodeURIComponent(token)}` : WS_ENDPOINT;
    return `${base}${path}`;
  }

  private subscribeToNotifications(stompClient: Client): void {
    stompClient.subscribe(TOPIC_NOTIFICATIONS, (message) => {
      try {
        const body = JSON.parse(message.body) as NotificationPayload;
        this.ngZone.run(() => this.notificationsSubject.next(body));
      } catch {
        // ignore malformed messages
      }
    });
  }
}
