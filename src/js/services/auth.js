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
  function storage() { return typeof window !== 'undefined' ? window.localStorage : null; }
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
      return response.json().then(function (payload) {
        if (!response.ok) throw new Error(payload.message || 'Authentication failed.');
        var data = Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
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
