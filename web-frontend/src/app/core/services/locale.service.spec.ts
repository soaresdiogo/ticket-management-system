import { TestBed } from '@angular/core/testing';
import { LocaleService } from './locale.service';

const STORAGE_KEY = 'ticket-app-locale';

describe('LocaleService', () => {
  let service: LocaleService;

  beforeEach(() => {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(STORAGE_KEY);
    }
    TestBed.configureTestingModule({});
    service = TestBed.inject(LocaleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return preferred locale from browser when navigator.language is pt', () => {
    Object.defineProperty(window.navigator, 'language', {
      value: 'pt-BR',
      configurable: true,
    });
    expect(service.getPreferredLocale()).toBe('pt-BR');
  });

  it('should return preferred locale from browser when navigator.language is en', () => {
    Object.defineProperty(window.navigator, 'language', {
      value: 'en-US',
      configurable: true,
    });
    expect(service.getPreferredLocale()).toBe('en');
  });

  it('should return pt-BR for pt variant', () => {
    Object.defineProperty(window.navigator, 'language', {
      value: 'pt',
      configurable: true,
    });
    expect(service.getPreferredLocale()).toBe('pt-BR');
  });

  it('should persist and return stored locale', () => {
    expect(service.setLocale('pt-BR')).toBe('pt-BR');
    expect(service.getStoredLocale()).toBe('pt-BR');
  });

  it('should return undefined for invalid stored locale', () => {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(STORAGE_KEY, 'xx');
      expect(service.getStoredLocale()).toBeUndefined();
    }
  });

  it('should return current locale preferring stored over preferred', () => {
    service.setLocale('pt-BR');
    Object.defineProperty(window.navigator, 'language', {
      value: 'en',
      configurable: true,
    });
    expect(service.getCurrentLocale()).toBe('pt-BR');
  });

  it('should return supported locales list', () => {
    const locales = service.getSupportedLocales();
    expect(locales).toContain('pt-BR');
    expect(locales).toContain('en');
    expect(locales.length).toBe(2);
  });
});
