import { Component, signal, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AuthService } from '../../core/services/auth.service';
import { LocaleService, SupportedLocale } from '../../core/services/locale.service';

type Step = 'credentials' | 'mfa';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSelectModule,
    TranslateModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly translate = inject(TranslateService);
  readonly localeService = inject(LocaleService);

  readonly currentLocale = signal<SupportedLocale>(this.localeService.getCurrentLocale());
  readonly supportedLocales = this.localeService.getSupportedLocales();

  readonly step = signal<Step>('credentials');
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly credentialsForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  readonly mfaForm = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(10)]],
  });

  readonly email = computed(() => this.credentialsForm.get('email')?.value ?? '');

  /** Masks the local part of the email, e.g. "user+tag@gmail.com" → "us*****ag@gmail.com" */
  maskEmail(email: string): string {
    if (!email || !email.includes('@')) return email ?? '';
    const [local, ...domainParts] = email.split('@');
    const domain = domainParts.join('@');
    if (local.length <= 2) return '*'.repeat(local.length) + '@' + domain;
    const visibleStart = 2;
    const visibleEnd = 2;
    const maskLength = Math.max(0, local.length - visibleStart - visibleEnd);
    const start = local.slice(0, visibleStart);
    const end = local.slice(-visibleEnd);
    return start + '*'.repeat(maskLength) + end + '@' + domain;
  }

  onSubmitCredentials(): void {
    this.error.set(null);
    if (this.credentialsForm.invalid) {
      this.credentialsForm.markAllAsTouched();
      return;
    }
    const { email, password } = this.credentialsForm.getRawValue();
    this.loading.set(true);
    this.auth.login({ email, password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.step.set('mfa');
        this.mfaForm.patchValue({ code: '' });
      },
      error: (err) => {
        this.loading.set(false);
        const msg =
          err?.error?.error ??
          err?.message ??
          'Invalid credentials. Please try again.';
        this.error.set(msg);
      },
    });
  }

  onSubmitMfa(): void {
    this.error.set(null);
    if (this.mfaForm.invalid) {
      this.mfaForm.markAllAsTouched();
      return;
    }
    const code = this.mfaForm.getRawValue().code;
    const email = this.credentialsForm.getRawValue().email;
    this.loading.set(true);
    this.auth.verifyMfa({ email, code, includeRefreshToken: true }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        const msg =
          err?.error?.error ??
          err?.message ??
          'Invalid or expired code. Please try again.';
        this.error.set(msg);
      },
    });
  }

  backToCredentials(): void {
    this.step.set('credentials');
    this.error.set(null);
    this.mfaForm.reset();
  }

  /** Flag emoji for locale: Brazil for pt-BR, UK for en. */
  localeFlag(locale: SupportedLocale): string {
    return locale === 'pt-BR' ? '🇧🇷' : '🇬🇧';
  }

  /** Display label for locale in the dropdown. */
  localeLabel(locale: SupportedLocale): string {
    return locale === 'pt-BR' ? 'Português' : 'English';
  }

  onLocaleChange(locale: SupportedLocale): void {
    this.localeService.setLocale(locale);
    this.translate.use(locale).subscribe(() => this.currentLocale.set(locale));
  }
}
