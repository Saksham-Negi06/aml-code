# AML Oracle JET frontend

Oracle JET 16.1 MVVM frontend for the AML services exposed through the Spring Cloud Gateway on port `8080`.

## Prerequisites

- Node.js `20.x` (verified with `20.20.2`)
- npm `10.x`
- Eureka, the transaction service, the ML service, and the gateway running

## Run locally

```powershell
npm install
npm test
npm run serve
```

The frontend calls `http://localhost:8080/api/v1` directly. The gateway allows the Oracle JET development origins `http://localhost:8000` and `http://127.0.0.1:8000` through its CORS configuration.

## Build

```powershell
npm run build
```

The optimized application is written to `web/`.

## JWT (prepared, currently disabled)

JWT is intentionally disabled in `src/js/config/appConfig.js`. While disabled, the API client never reads a token or sends an `Authorization` header, and the current demo login remains local-only.

After the backend authentication endpoint exists:

1. Set `auth.enabled` to `true`.
2. Update `auth.loginPath` if the backend path differs from `/api/v1/auth/login`.
3. Replace the demo login handler in `src/js/appController.js` with `auth.login(credentials)`.
4. Call `auth.clearToken()` during logout.

The auth service accepts either `{ "accessToken": "..." }` or `{ "token": "..." }` in a direct response or inside the backend's standard `data` wrapper.

## Verification

`npm run verify` runs the Node unit tests and optimized JET build. Tests cover API response unwrapping, gateway error propagation, and the disabled-by-default JWT behavior.
