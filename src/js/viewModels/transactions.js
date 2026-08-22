define(['knockout', 'ojs/ojarraydataprovider', 'services/api', 'viewModels/base', 'ojs/ojtable'], function (ko, ArrayDataProvider, api, BaseViewModel) {
  'use strict';

  function TransactionsViewModel(params) {
    var self = this;
    BaseViewModel.call(self, params && params.rootRouter);
    self.rows = ko.observableArray([]);
    self.error = ko.observable('');
    self.loading = ko.observable(true);
    self.search = params && params.globalSearch || ko.observable('');
    self.selectedTransaction = ko.observable(null);
    self.filtered = ko.computed(function () {
      var query = String(self.search() || '').toLowerCase();
      return self.rows().filter(function (transaction) {
        return !query || [transaction.transactionId, transaction.senderAccountId, transaction.receiverAccountId, transaction.paymentType, transaction.status, transaction.riskCategory].join(' ').toLowerCase().indexOf(query) >= 0;
      });
    });
    self.dataProvider = ko.pureComputed(function () { return new ArrayDataProvider(self.filtered(), { keyAttributes: 'id' }); });
    self.openTransaction = function (transaction) { self.selectedTransaction(transaction || null); };
    self.closeTransaction = function () { self.selectedTransaction(null); };

    Promise.all([api.get('/transactions'), api.get('/cases')])
      .then(function (result) {
        var cases = result[1] || [];
        return Promise.all((result[0] || []).map(function (transaction) {
          return api.get('/transactions/' + encodeURIComponent(transaction.transactionId) + '/risk')
            .catch(function () { return null; })
            .then(function (assessment) {
              transaction.displayDate = transaction.transactionDatetime ? new Date(transaction.transactionDatetime).toLocaleString() : '-';
              transaction.displayAmount = new Intl.NumberFormat('en-US', { style: 'currency', currency: transaction.paymentCurrency || 'USD' }).format(Number(transaction.amount || 0));
              transaction.riskScore = self.riskPercent(assessment && assessment.riskScore);
              transaction.riskCategory = String(assessment && assessment.riskCategory || self.riskCategory(transaction.riskScore)).toUpperCase();
              transaction.flagged = Boolean(assessment && assessment.shouldFlag);
              transaction.explanation = assessment && assessment.oneLineExplanation || 'No unusual behavior detected.';
              transaction.recommendation = assessment && assessment.recommendation || 'Continue routine monitoring.';
              transaction.caseRecord = cases.find(function (caseRecord) { return caseRecord.transactionId === transaction.transactionId; });
              return transaction;
            });
        }));
      })
      .then(function (transactions) { self.rows(transactions); })
      .catch(function (error) { self.error(error.message); })
      .finally(function () { self.loading(false); });
  }
  return TransactionsViewModel;
});
