import { describe, it, expect } from 'vitest';
import { decodeJwtPayload } from './jwt.utils';

function base64UrlEncode(str: string): string {
  return btoa(str).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

describe('decodeJwtPayload', () => {
  it('should decode a valid JWT payload', () => {
    const payload = { sub: 'user-123', email: 'u@example.com', role: 'CLIENT', tenantId: 'tenant-456' };
    const token = `header.${base64UrlEncode(JSON.stringify(payload))}.signature`;
    const result = decodeJwtPayload(token);
    expect(result).toEqual(payload);
  });

  it('should return null for token with less than 3 parts', () => {
    expect(decodeJwtPayload('one.two')).toBeNull();
    expect(decodeJwtPayload('single')).toBeNull();
  });

  it('should return null for invalid base64 payload', () => {
    const token = 'a.!!!invalid!!!.c';
    expect(decodeJwtPayload(token)).toBeNull();
  });

  it('should return null for invalid JSON in payload', () => {
    const token = `a.${base64UrlEncode('not json')}.c`;
    expect(decodeJwtPayload(token)).toBeNull();
  });

  it('should decode payload with only required claims', () => {
    const payload = { sub: 'id', role: 'USER' };
    const token = `x.${base64UrlEncode(JSON.stringify(payload))}.y`;
    const result = decodeJwtPayload(token);
    expect(result?.sub).toBe('id');
    expect(result?.role).toBe('USER');
  });
});
