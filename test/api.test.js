'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const api = require('../src/js/services/api');

test('unwraps the Spring ApiResponse data field', async (context) => {
  context.after(() => { delete global.fetch; });
  global.fetch = async (url, options) => {
    assert.equal(url, 'http://localhost:8080/api/v1/customers');
    assert.equal(options.method, 'GET');
    assert.equal(options.headers.Authorization, undefined);
    return new Response(JSON.stringify({ success: true, data: [{ id: 'C-1' }] }), { status: 200 });
  };
  assert.deepEqual(await api.get('/customers'), [{ id: 'C-1' }]);
});

test('surfaces gateway errors with status and message', async (context) => {
  context.after(() => { delete global.fetch; });
  global.fetch = async () => new Response(JSON.stringify({ success: false, message: 'Invalid account' }), { status: 400 });
  await assert.rejects(api.post('/transactions', {}), (error) => {
    assert.equal(error.name, 'ApiError');
    assert.equal(error.status, 400);
    assert.equal(error.message, 'Invalid account');
    return true;
  });
});
