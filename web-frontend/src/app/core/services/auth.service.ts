import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

const AUTH_API = '/auth';
const ACCESS_TOKEN_KEY = 'tms_access_token';
const REFRESH_TOKEN_KEY = 'tms_refresh_token';
const EXPIRES_AT_KEY = 'tms_expires_at';

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
  refreshToken?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly accessToken = signal<string | null>(this.getStoredAccessToken());
  private readonly refreshToken = signal<string | null>(this.getStoredRefreshToken());

  readonly isAuthenticated = computed(() => !!this.accessToken());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(body: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${AUTH_API}/login`, body);
  }

  verifyMfa(body: VerifyMfaRequest): Observable<VerifyMfaResponse> {
    return this.http.post<VerifyMfaResponse>(`${AUTH_API}/verify-mfa`, body).pipe(
      tap((res) => {
        this.setTokens(res.accessToken, res.refreshToken ?? null, res.expiresIn);
      })
    );
  }

  logout(): void {
    this.clearTokens();
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return this.accessToken();
  }

  private setTokens(access: string, refresh: string | null, expiresIn: number): void {
    const expiresAt = Date.now() + expiresIn * 1000;
    this.accessToken.set(access);
    this.refreshToken.set(refresh);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(ACCESS_TOKEN_KEY, access);
      localStorage.setItem(EXPIRES_AT_KEY, String(expiresAt));
      if (refresh) localStorage.setItem(REFRESH_TOKEN_KEY, refresh);
    }
  }

  private clearTokens(): void {
    this.accessToken.set(null);
    this.refreshToken.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(ACCESS_TOKEN_KEY);
      localStorage.removeItem(REFRESH_TOKEN_KEY);
      localStorage.removeItem(EXPIRES_AT_KEY);
    }
  }

  private getStoredAccessToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    const token = localStorage.getItem(ACCESS_TOKEN_KEY);
    const expiresAt = localStorage.getItem(EXPIRES_AT_KEY);
    if (!token || !expiresAt) return null;
    if (Date.now() >= Number(expiresAt)) {
      this.clearTokens();
      return null;
    }
    return token;
  }

  private getStoredRefreshToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }
}
