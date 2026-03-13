import { APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { DatePipe } from '@angular/common';
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
    translate.setDefaultLang('en');
    return firstValueFrom(translate.use(locale));
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    DatePipe,
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
