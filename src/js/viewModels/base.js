define(['knockout'], function (ko) {
  'use strict';

  function BaseViewModel(rootRouter) {
    var self = this;
    self.router = rootRouter;
    self.pageSearch = ko.observable('');

    self.go = function (path) {
      rootRouter.go({ path: path });
    };

    self.money = function (number, currency) {
      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency || 'USD',
        maximumFractionDigits: 0
      }).format(number);
    };

    self.riskPercent = function (value) {
      var score = Number(value || 0);
      if (!isFinite(score)) score = 0;
      if (score > 0 && score <= 1) score *= 100;
      return Math.round(Math.min(100, Math.max(0, score)));
    };

    self.riskClass = function (score) {
      score = self.riskPercent(score);
      return score >= 85 ? 'critical' : score >= 70 ? 'high' : score >= 50 ? 'medium' : 'low';
    };

    self.riskCategory = function (score) {
      return self.riskClass(score).toUpperCase();
    };

    self.statusClass = function (value) {
      return String(value || '').toLowerCase().replace(/_/g, '-').replace(/ /g, '-');
    };

    // Transaction risk scores from the API are inconsistently scaled (some 0-1, some 0-100),
    // so category comparisons must rank the backend's own riskCategory label, never the raw number.
    self.categoryRank = function (category) {
      return { UNASSESSED: 0, LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }[String(category || '').toUpperCase()] || 0;
    };

    self.initials = function (name) {
      return String(name || '').split(' ').map(function (x) { return x.charAt(0); }).join('');
    };
  }

  return BaseViewModel;
});
