(function (root, factory) {
  if (typeof define === 'function' && define.amd) {
    define([], factory);
  } else if (typeof module === 'object' && module.exports) {
    module.exports = factory();
  } else {
    root.amlAppConfig = factory();
  }
}(typeof self !== 'undefined' ? self : this, function () {
  'use strict';
  return Object.freeze({
    apiBaseUrl: 'http://localhost:8080/api/v1',
    auth: Object.freeze({
      enabled: true,
      loginPath: '/auth/login',
      tokenStorageKey: 'aml_access_token'
    })
  });
}));
