define([
  'knockout',
  'ojs/ojarraydataprovider',
  'services/api',
  'viewModels/base',
  'ojs/ojtable'
], function (ko, ArrayDataProvider, api, BaseViewModel) {
  'use strict';

  function DashboardViewModel() {
    var self = this;
    BaseViewModel.call(self);

    self.metrics = ko.observableArray([]);
    self.trend = ko.observableArray([]);
    self.alerts = ko.observableArray([]);
    self.error = ko.observable('');

    self.alertColumns = [
      { headerText: 'ALERT', field: 'id' },
      { headerText: 'CUSTOMER', field: 'customer' },
      { headerText: 'TRANSACTION', field: 'txn' },
      { headerText: 'TYPE', field: 'type' },
      { headerText: 'SEVERITY', field: 'severity' },
      { headerText: 'SCORE', field: 'score' },
      { headerText: 'STATUS', field: 'status' },
      { headerText: 'ASSIGNED', field: 'analyst' },
      { headerText: 'CREATED', field: 'created' }
    ];

    self.openAlert = function (event) {
      var key = event.detail && event.detail.context && event.detail.context.key;
      if (key) window.alert('Open alert ' + key);
    };

    self.screenTransaction = function () {
      window.alert('Transaction screening form ready for API submission.');
    };

    self.exportSummary = function () {
      window.alert('Export is not provided by the backend.');
    };
    Promise.all([api.get('/customers'), api.get('/accounts'), api.get('/transactions')]).then(function (result) {
      var customers = result[0] || [], accounts = result[1] || [], transactions = result[2] || [];
      self.metrics([['Total customers', String(customers.length), 'From customer service', 'danger-none'], ['Active accounts', String(accounts.length), 'From account service', 'danger-none'], ['Transactions monitored', String(transactions.length), 'From transaction service', 'danger-none']]);
      self.trend(transactions.slice(-14).map(function () { return 100; }));
      self.alerts(transactions.slice(-4));
    }).catch(function (error) { self.error(error.message); });
  }

  return DashboardViewModel;
});
