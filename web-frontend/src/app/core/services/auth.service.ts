import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';

import { decodeJwtPayload } from '../utils/jwt.utils';

const AUTH_API = '/auth';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
}

export interface VerifyMfaRequest {
  email: string;
  code: string;
  includeRefreshToken?: boolean;
}

export interface VerifyMfaResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  /** True when refresh token was set in HttpOnly cookie by the server. */
  refreshTokenSet?: boolean;
}

export interface RefreshResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshTokenSet?: boolean;
}

/** User claims from JWT (role drives dashboard: CLIENT vs USER/office). */
export interface UserProfile {
  userId: string;
  email: string;
  role: string;
  tenantId: string | null;
}

/** In-memory only. Expiry is checked when reading token. */
const DEFAULT_EXPIRES_AT = 0;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly accessToken = signal<string | null>(null);
  private readonly expiresAt = signal<number>(DEFAULT_EXPIRES_AT);
  private readonly userProfile = signal<UserProfile | null>(null);

  readonly isAuthenticated = computed(() => this.hasValidAccessToken());
  readonly profile = computed(() => this.userProfile());

  /** True if user has office role (USER). */
  readonly isOfficeUser = computed(() => {
    return this.userProfile()?.role?.toUpperCase() === 'USER';
  });
  /** True if user has client role (CLIENT). */
  readonly isClientUser = computed(() => this.userProfile()?.role?.toUpperCase() === 'CLIENT');

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${AUTH_API}/login`, body);
  }

  verifyMfa(body: VerifyMfaRequest): Observable<VerifyMfaResponse> {
    return this.http
      .post<VerifyMfaResponse>(`${AUTH_API}/verify-mfa`, body, { withCredentials: true })
      .pipe(tap((res) => this.setAccessToken(res.accessToken, res.expiresIn)));
  }

  /**
   * Refresh access token using the HttpOnly refresh-token cookie.
   * Call with withCredentials so the cookie is sent.
   */
  refreshSession(): Observable<RefreshResponse | null> {
    return this.http
      .post<RefreshResponse>(`${AUTH_API}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((res) => this.setAccessToken(res.accessToken, res.expiresIn)),
        catchError(() => of(null))
      );
  }

  logout(): void {
    this.http.post(`${AUTH_API}/logout`, {}, { withCredentials: true }).subscribe({
      next: () => this.clearAndNavigate(),
      error: () => this.clearAndNavigate(),
    });
  }

  getAccessToken(): string | null {
    if (!this.hasValidAccessToken()) return null;
    return this.accessToken();
  }

  private setAccessToken(access: string, expiresInSeconds: number): void {
    const expiresAt = Date.now() + expiresInSeconds * 1000;
    this.accessToken.set(access);
    this.expiresAt.set(expiresAt);
    const payload = decodeJwtPayload(access);
    if (payload?.sub && payload.role) {
      this.userProfile.set({
        userId: payload.sub,
        email: payload.email ?? '',
        role: payload.role,
        tenantId: payload.tenantId ?? null,
      });
    } else {
      this.userProfile.set(null);
    }
  }

  private clearTokens(): void {
    this.accessToken.set(null);
    this.expiresAt.set(DEFAULT_EXPIRES_AT);
    this.userProfile.set(null);
  }

  private clearAndNavigate(): void {
    this.clearTokens();
    this.router.navigate(['/login']);
  }

  private hasValidAccessToken(): boolean {
    const token = this.accessToken();
    const expiresAt = this.expiresAt();
    if (!token || !expiresAt) return false;
    return Date.now() < expiresAt;
  }
}
