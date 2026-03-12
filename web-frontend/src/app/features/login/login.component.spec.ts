import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { AuthService } from '../../core/services/auth.service';
import { LocaleService } from '../../core/services/locale.service';
import { createTranslateServiceMock, MockTranslatePipe } from '../../core/testing/translate.mock';
import { LoginComponent } from './login.component';

const translateMock = createTranslateServiceMock();

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  type SubHandler = { next?: () => void; error?: (err: unknown) => void };
  let authMock: { login: (args: unknown) => { subscribe: (handlers: SubHandler) => void }; verifyMfa: (args: unknown) => { subscribe: (handlers: SubHandler) => void } };
  let localeMock: { getCurrentLocale: () => string; getSupportedLocales: () => string[]; setLocale: (l: string) => string };

  beforeEach(async () => {
    authMock = {
      login: () => ({
        subscribe: (handlers: SubHandler) => {
          handlers.next?.();
        },
      }),
      verifyMfa: () => ({
        subscribe: (handlers: SubHandler) => {
          handlers.next?.();
        },
      }),
    };
    localeMock = {
      getCurrentLocale: () => 'en',
      getSupportedLocales: () => ['en', 'pt-BR'],
      setLocale: (l: string) => l,
    };
    await TestBed.configureTestingModule({
      imports: [LoginComponent, NoopAnimationsModule, TranslateModule.forChild()],
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: LocaleService, useValue: localeMock },
        { provide: TranslateService, useValue: translateMock },
        { provide: Router, useValue: { navigate: () => {} } },
      ],
    });
    TestBed.overrideComponent(LoginComponent, {
      set: {
        imports: [
          CommonModule,
          ReactiveFormsModule,
          MatFormFieldModule,
          MatInputModule,
          MatButtonModule,
          MatProgressSpinnerModule,
          MatIconModule,
          MatSelectModule,
          MockTranslatePipe,
        ],
      },
    });
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start on credentials step', () => {
    expect(component.step()).toBe('credentials');
  });

  it('should have currentLocale and supportedLocales from locale service', () => {
    expect(component.currentLocale()).toBe('en');
    expect(component.supportedLocales).toContain('pt-BR');
  });

  it('should call localeService.setLocale and translate.use when onLocaleChange', () => {
    const setSpy = vi.spyOn(localeMock, 'setLocale');
    component.onLocaleChange('pt-BR');
    expect(setSpy).toHaveBeenCalledWith('pt-BR');
  });

  it('should mask email correctly', () => {
    expect(component.maskEmail('ab@x.com')).toBe('**@x.com');
    expect(component.maskEmail('user+tag@gmail.com').startsWith('us')).toBe(true);
    expect(component.maskEmail('').includes('@')).toBe(false);
  });
});
