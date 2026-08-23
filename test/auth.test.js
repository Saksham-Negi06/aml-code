'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const config = require('../src/js/config/appConfig');
const auth = require('../src/js/services/auth');

test('JWT integration is enabled for the Spring auth endpoint', async () => {
  assert.equal(config.auth.enabled, true);
  assert.equal(auth.isEnabled(), true);
  assert.deepEqual(auth.applyAuthorization({ Accept: 'application/json' }), { Accept: 'application/json' });
});

test('login stores the access token returned by the backend', async () => {
  const originalFetch = global.fetch;
  const originalWindow = global.window;
  const values = {};
  global.window = {
    localStorage: {
      getItem: (key) => values[key] || null,
      setItem: (key, value) => { values[key] = value; },
      removeItem: (key) => { delete values[key]; }
    }
  };
  global.fetch = async () => ({
    ok: true,
    text: async () => JSON.stringify({ token: 'test-token', username: 'admin', role: 'USER' })
  });
  try {
    const response = await auth.login({ username: 'admin', password: 'password123' });
    assert.equal(response.token, 'test-token');
    assert.equal(auth.getToken(), 'test-token');
  } finally {
    auth.clearToken();
    global.fetch = originalFetch;
    global.window = originalWindow;
  }
});

test('login exposes the backend error for invalid credentials', async () => {
  const originalFetch = global.fetch;
  global.fetch = async () => ({
    ok: false,
    text: async () => JSON.stringify({ message: 'Invalid username or password' })
  });
  try {
    await assert.rejects(auth.login({ username: 'admin', password: 'wrong' }), /Invalid username or password/);
  } finally {
    global.fetch = originalFetch;
  }
});

test('login handles an empty gateway error response', async () => {
  const originalFetch = global.fetch;
  global.fetch = async () => ({ ok: false, text: async () => '' });
  try {
    await assert.rejects(auth.login({ username: 'admin', password: 'wrong' }), /Invalid username or password/);
  } finally {
    global.fetch = originalFetch;
  }
});
