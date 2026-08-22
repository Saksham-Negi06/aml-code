define(['knockout', 'services/api', 'viewModels/base'], function (ko, api, BaseViewModel) {
  'use strict';

  var THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000;

  function RulesViewModel(params) {
    var self = this;
    BaseViewModel.call(self, params && params.rootRouter);

    self.rows = ko.observableArray([]);
    self.error = ko.observable('');
    self.loading = ko.observable(true);
    self.busy = ko.observable(false);

    self.severityOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    self.showCreateForm = ko.observable(false);
    self.formError = ko.observable('');
    self.formName = ko.observable('');
    self.formThreshold = ko.observable('');
    self.formSeverity = ko.observable('HIGH');

    function loadRules() {
      self.loading(true);
      return Promise.all([api.get('/rules'), api.get('/transactions')])
        .then(function (result) {
          var rules = result[0] || [];
          var transactions = result[1] || [];
          var now = Date.now();
          self.rows(rules.map(function (rule) {
            var threshold = Number(rule.amountThreshold || 0);
            var hits = transactions.filter(function (transaction) {
              var amount = Number(transaction.amount || 0);
              var time = new Date(transaction.transactionDatetime || transaction.createdAt || 0).getTime();
              return amount > threshold && !isNaN(time) && (now - time) <= THIRTY_DAYS_MS && (now - time) >= 0;
            }).length;
            return {
              id: rule.id,
              name: rule.name,
              condition: 'Transaction amount exceeds threshold',
              threshold: self.money(threshold),
              severity: rule.severity,
              status: rule.status,
              hits: hits
            };
          }));
        })
        .catch(function (error) { self.error(error.message); })
        .finally(function () { self.loading(false); });
    }

    self.create = function () {
      self.formError('');
      self.formName('');
      self.formThreshold('');
      self.formSeverity('HIGH');
      self.showCreateForm(true);
    };

    self.closeCreateForm = function () {
      self.showCreateForm(false);
    };

    self.submitRule = function () {
      var name = self.formName().trim();
      var threshold = Number(self.formThreshold());

      if (!name) { self.formError('Give the rule a name.'); return; }
      if (!threshold || threshold <= 0) { self.formError('Enter an amount threshold greater than zero.'); return; }

      self.formError('');
      self.busy(true);
      api.post('/rules', { name: name, amountThreshold: threshold, severity: self.formSeverity() })
        .then(function () {
          self.showCreateForm(false);
          return loadRules();
        })
        .catch(function (error) { self.formError(error.message); })
        .finally(function () { self.busy(false); });
    };

    self.toggleStatus = function (rule) {
      var nextStatus = rule.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
      self.busy(true);
      api.put('/rules/' + encodeURIComponent(rule.id), { status: nextStatus })
        .then(loadRules)
        .catch(function (error) { self.error(error.message); })
        .finally(function () { self.busy(false); });
    };

    self.removeRule = function (rule) {
      if (!window.confirm('Delete the rule "' + rule.name + '"? Alerts it already triggered are not affected.')) return;
      self.busy(true);
      api.remove('/rules/' + encodeURIComponent(rule.id))
        .then(loadRules)
        .catch(function (error) { self.error(error.message); })
        .finally(function () { self.busy(false); });
    };

    loadRules();
  }

  return RulesViewModel;
});
