(function (root, factory) {
  if (typeof define === 'function' && define.amd) {
    define(['config/appConfig', 'services/auth'], factory);
  } else if (typeof module === 'object' && module.exports) {
    module.exports = factory(require('../config/appConfig'), require('./auth'));
  } else {
    root.amlApi = factory(root.amlAppConfig, root.amlAuth);
  }
}(typeof self !== 'undefined' ? self : this, function (config, auth) {
  'use strict';

  function ApiError(message, status, details) {
    this.name = 'ApiError';
    this.message = message;
    this.status = status;
    this.details = details;
    if (Error.captureStackTrace) Error.captureStackTrace(this, ApiError);
  }
  ApiError.prototype = Object.create(Error.prototype);
  ApiError.prototype.constructor = ApiError;

  function parseResponse(response) {
    if (response.status === 204) return Promise.resolve(null);
    return response.text().then(function (text) {
      if (!text) return null;
      try { return JSON.parse(text); } catch (error) {
        throw new ApiError('The gateway returned an invalid JSON response.', response.status, text);
      }
    });
  }

  function request(path, options) {
    options = options || {};
    var headers = auth.applyAuthorization(options.headers);
    if (options.body !== undefined) headers['Content-Type'] = 'application/json';
    return fetch(config.apiBaseUrl + path, Object.assign({}, options, { headers: headers }))
      .then(function (response) {
        return parseResponse(response).then(function (payload) {
          if (!response.ok || (payload && payload.success === false)) {
            throw new ApiError(
              (payload && payload.message) || ('Gateway request failed (' + response.status + ').'),
              response.status,
              payload
            );
          }
          return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload;
        });
      })
      .catch(function (error) {
        if (error instanceof ApiError) throw error;
        throw new ApiError('Unable to reach the API gateway at ' + config.apiBaseUrl + '.', 0, error);
      });
  }

  return Object.freeze({
    ApiError: ApiError,
    get: function (path) { return request(path, { method: 'GET' }); },
    post: function (path, body) { return request(path, { method: 'POST', body: JSON.stringify(body || {}) }); },
    put: function (path, body) { return request(path, { method: 'PUT', body: JSON.stringify(body || {}) }); },
    remove: function (path) { return request(path, { method: 'DELETE' }); }
  });
}));
