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

    self.riskClass = function (score) {
      return score >= 85 ? 'critical' : score >= 70 ? 'high' : score >= 50 ? 'medium' : 'low';
    };

    self.statusClass = function (value) {
      return String(value || '').toLowerCase().replace(/_/g, '-').replace(/ /g, '-');
    };

    self.initials = function (name) {
      return String(name || '').split(' ').map(function (x) { return x.charAt(0); }).join('');
    };
  }

  return BaseViewModel;
});
