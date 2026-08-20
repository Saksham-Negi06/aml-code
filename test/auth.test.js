'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const config = require('../src/js/config/appConfig');
const auth = require('../src/js/services/auth');

test('JWT integration remains disabled by default', async () => {
  assert.equal(config.auth.enabled, false);
  assert.equal(auth.isEnabled(), false);
  assert.deepEqual(auth.applyAuthorization({ Accept: 'application/json' }), { Accept: 'application/json' });
  await assert.rejects(auth.login({ username: 'admin' }), /JWT authentication is disabled/);
});
