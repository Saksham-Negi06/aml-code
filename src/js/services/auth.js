(function (root, factory) {
  if (typeof define === 'function' && define.amd) {
    define(['config/appConfig'], factory);
  } else if (typeof module === 'object' && module.exports) {
    module.exports = factory(require('../config/appConfig'));
  } else {
    root.amlAuth = factory(root.amlAppConfig);
  }
}(typeof self !== 'undefined' ? self : this, function (config) {
  'use strict';
  function storage() {
    if (typeof window === 'undefined') return null;
    return window.sessionStorage || window.localStorage;
  }
  function getToken() {
    var store = storage();
    return config.auth.enabled && store ? store.getItem(config.auth.tokenStorageKey) : null;
  }
  function setToken(token) {
    var store = storage();
    if (!store) return;
    if (token) store.setItem(config.auth.tokenStorageKey, token);
    else store.removeItem(config.auth.tokenStorageKey);
  }
  function clearToken() { setToken(null); }
  function login(credentials) {
    if (!config.auth.enabled) {
      return Promise.reject(new Error('JWT authentication is disabled.'));
    }
    return fetch(config.apiBaseUrl + config.auth.loginPath, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(credentials || {})
    }).then(function (response) {
      var responseBody = typeof response.text === 'function'
        ? response.text()
        : response.json().then(function (payload) { return JSON.stringify(payload); });
      return responseBody.then(function (text) {
        var payload = null;
        if (text) {
          try {
            payload = JSON.parse(text);
          } catch (error) {
            throw new Error(response.ok ? 'The gateway returned an invalid login response.' : 'Invalid username or password.');
          }
        }
        if (!response.ok) throw new Error((payload && payload.message) || 'Invalid username or password.');
        var data = payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
        var token = data && (data.accessToken || data.token);
        if (!token) throw new Error('Authentication response did not include an access token.');
        setToken(token);
        return data;
      });
    });
  }
  function applyAuthorization(headers) {
    var result = Object.assign({}, headers || {});
    var token = getToken();
    if (token) result.Authorization = 'Bearer ' + token;
    return result;
  }
  return Object.freeze({
    isEnabled: function () { return config.auth.enabled; },
    getToken: getToken,
    setToken: setToken,
    clearToken: clearToken,
    login: login,
    applyAuthorization: applyAuthorization
  });
}));
