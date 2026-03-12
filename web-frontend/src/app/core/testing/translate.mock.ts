import { Pipe, PipeTransform } from '@angular/core';
import { of } from 'rxjs';

/**
 * Minimal mock for TranslateService for unit tests.
 */
export function createTranslateServiceMock() {
  return {
    instant: (key: string, params?: Record<string, unknown>) =>
      params ? `${key}:${JSON.stringify(params)}` : key,
    get: (key: string) => of(key),
    getParsedResult: (key: string) => of(key),
    stream: (key: string) => of(key),
    use: () => of({}),
    setDefaultLang: () => of({}),
    getCurrentLang: () => 'en',
    getBrowserLang: () => 'en',
    getLangs: () => ['en', 'pt-BR'],
    addLangs: () => {},
    onLangChange: of({ lang: 'en', translations: {} }),
    onTranslationChange: of({ translations: {}, lang: 'en' }),
    onDefaultLangChange: of({ lang: 'en', translations: {} }),
  };
}

/** Pipe that returns the key as-is for tests (avoids TranslatePipe's subscribe logic). */
@Pipe({ name: 'translate', standalone: true })
export class MockTranslatePipe implements PipeTransform {
  transform(key: string, params?: Record<string, unknown>): string {
    return params ? `${key}:${JSON.stringify(params)}` : key;
  }
}
