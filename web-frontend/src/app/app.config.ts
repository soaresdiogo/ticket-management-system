import { APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { firstValueFrom } from 'rxjs';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { LocaleService } from './core/services/locale.service';

function initializeLocale(
  translate: TranslateService,
  localeService: LocaleService
): () => Promise<unknown> {
  return () => {
    const locale = localeService.getCurrentLocale();
    // #region agent log
    fetch('http://127.0.0.1:7584/ingest/3ae02373-567d-4b7d-be79-e71134a965c8', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Debug-Session-Id': 'c23077' },
      body: JSON.stringify({
        sessionId: 'c23077',
        location: 'app.config.ts:initializeLocale',
        message: 'APP_INITIALIZER locale',
        data: { locale },
        timestamp: Date.now(),
        hypothesisId: 'H1',
      }),
    }).catch(() => {});
    // #endregion
    translate.setDefaultLang('en');
    return firstValueFrom(translate.use(locale)).then(() => {
      // #region agent log
      const instant = translate.instant('login.title');
      fetch('http://127.0.0.1:7584/ingest/3ae02373-567d-4b7d-be79-e71134a965c8', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-Debug-Session-Id': 'c23077' },
        body: JSON.stringify({
          sessionId: 'c23077',
          location: 'app.config.ts:after use()',
          message: 'After translate.use()',
          data: { instant, isKey: instant === 'login.title' },
          timestamp: Date.now(),
          hypothesisId: 'H2',
        }),
      }).catch(() => {});
      // #endregion
    });
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    provideAnimationsAsync(),
    provideTranslateService({
      fallbackLang: 'en',
      loader: provideTranslateHttpLoader({ prefix: '/assets/i18n/', suffix: '.json' }),
    }),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeLocale,
      deps: [TranslateService, LocaleService],
      multi: true,
    },
  ],
};
