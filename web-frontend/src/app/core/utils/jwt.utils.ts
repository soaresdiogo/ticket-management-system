/**
 * Decodes a JWT payload without verification (client-side only).
 * Verification is done by the API gateway. Use only for reading claims (e.g. role, userId).
 */
export interface JwtPayload {
  sub?: string;
  email?: string;
  role?: string;
  tenantId?: string;
  exp?: number;
  iat?: number;
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded) as JwtPayload;
  } catch {
    return null;
  }
}
