define([], function () {
  'use strict';
  // No client-side records are retained. Views fetch data from services/api.
  return { customers: [], transactions: [], alerts: [], investigations: [], rules: [], trend: [] };
});
