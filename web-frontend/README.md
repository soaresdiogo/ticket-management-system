# TMS Web Frontend

Angular 21 frontend for the Ticket Management System. Uses **Angular Material** and **Tailwind CSS**, with global styles aligned to the ContaFlow/dashboard-cli design (Sora font, navy/amber palette).

## Features

- **Login + MFA**: Credentials step then one-time code verification (calls `POST /auth/login` and `POST /auth/verify-mfa` via API gateway).
- **Auth guard**: Unauthenticated users are redirected to `/login`; after MFA success they are sent to `/dashboard`.

## Development server

Ensure the API gateway (and auth service) are running (e.g. gateway on port 8080). The dev server proxies `/auth` to the gateway.

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
