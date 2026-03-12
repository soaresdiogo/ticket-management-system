import { Injectable } from '@angular/core';

/** Supported locale codes. Add new codes here when adding languages. */
export type SupportedLocale = 'pt-BR' | 'en';

const STORAGE_KEY = 'ticket-app-locale';

const SUPPORTED_LOCALES: readonly SupportedLocale[] = ['pt-BR', 'en'] as const;

/** Default locale when no stored/browser preference: en. */
const DEFAULT_LOCALE: SupportedLocale = 'en';

@Injectable({ providedIn: 'root' })
export class LocaleService {
  /**
   * Returns the stored locale from localStorage if valid; otherwise undefined.
   */
  getStoredLocale(): SupportedLocale | undefined {
    if (typeof window === 'undefined' || !window.localStorage) {
      return undefined;
    }
    const stored = window.localStorage.getItem(STORAGE_KEY);
    return this.isSupported(stored) ? (stored as SupportedLocale) : undefined;
  }

  /**
   * Infers preferred locale from browser (region): pt variants → pt-BR, else en.
   */
  getPreferredLocale(): SupportedLocale {
    if (typeof window === 'undefined' || !window.navigator?.language) {
      return DEFAULT_LOCALE;
    }
    const browser = window.navigator.language;
    if (browser.toLowerCase().startsWith('pt')) {
      return 'pt-BR';
    }
    return 'en';
  }

  /**
   * Returns the effective locale: stored > browser preference > default.
   */
  getCurrentLocale(): SupportedLocale {
    return this.getStoredLocale() ?? this.getPreferredLocale();
  }

  /**
   * Persists the chosen locale and returns it.
   */
  setLocale(locale: SupportedLocale): SupportedLocale {
    if (typeof window !== 'undefined' && window.localStorage) {
      window.localStorage.setItem(STORAGE_KEY, locale);
    }
    return locale;
  }

  getSupportedLocales(): readonly SupportedLocale[] {
    return SUPPORTED_LOCALES;
  }

  private isSupported(value: string | null): value is SupportedLocale {
    return value !== null && (SUPPORTED_LOCALES as readonly string[]).includes(value);
  }
}
