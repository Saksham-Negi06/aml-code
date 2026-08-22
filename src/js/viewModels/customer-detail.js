define(['knockout', 'services/api', 'viewModels/base'], function (ko, api, BaseViewModel) {
  'use strict';

  function CustomerDetailViewModel(params) {
    var self = this;
    var id = window.sessionStorage.getItem('aegis_selected_customer_id');
    self.router = params && params.rootRouter;
    BaseViewModel.call(self, self.router);
    self.customer = ko.observable(null);
    self.accounts = ko.observableArray([]);
    self.transactions = ko.observableArray([]);
    self.error = ko.observable('');
    self.loading = ko.observable(true);

    self.riskPercent = function (value) {
      var score = Number(value || 0);
      if (!isFinite(score)) score = 0;
      if (score > 0 && score <= 1) score *= 100;
      return Math.round(Math.min(100, Math.max(0, score)));
    };
    self.back = function () { if (self.router) self.router.go('customers'); };
    self.runAssessment = function () { window.alert('Risk is calculated from the customer accounts and their latest transaction assessments.'); };

    if (!id) {
      self.error('No customer selected.');
      self.loading(false);
      return;
    }

    Promise.all([api.get('/customers/' + encodeURIComponent(id)), api.get('/accounts'), api.get('/transactions')])
      .then(function (result) {
        var customer = result[0];
        var accounts = (result[1] || []).filter(function (account) { return account.customerId === customer.id; });
        var accountIds = accounts.map(function (account) { return account.id; });
        var transactions = (result[2] || []).filter(function (transaction) {
          return accountIds.indexOf(transaction.senderAccountId) >= 0 || accountIds.indexOf(transaction.receiverAccountId) >= 0;
        });
        return Promise.all(transactions.map(function (transaction) {
          return api.get('/transactions/' + encodeURIComponent(transaction.transactionId) + '/risk')
            .catch(function () { return null; })
            .then(function (assessment) {
              transaction.riskScore = self.riskPercent(assessment && assessment.riskScore);
              transaction.riskCategory = assessment && assessment.riskCategory || self.riskClass(transaction.riskScore).toUpperCase();
              transaction.explanation = assessment && assessment.oneLineExplanation || 'No risk explanation is available.';
              transaction.recommendation = assessment && assessment.recommendation || 'Continue routine monitoring.';
              transaction.displayDate = transaction.transactionDatetime ? new Date(transaction.transactionDatetime).toLocaleString() : '-';
              return transaction;
            });
        })).then(function (enrichedTransactions) {
          var accountAverage = accounts.reduce(function (sum, account) {
            return sum + self.riskPercent(account.accountRiskScore);
          }, 0) / (accounts.length || 1);
          var highest = enrichedTransactions.length ? enrichedTransactions.reduce(function (best, transaction) {
            return transaction.riskScore > best.riskScore ? transaction : best;
          }) : null;
          var score = Math.max(Math.round(accountAverage), highest ? highest.riskScore : 0);
          self.accounts(accounts);
          self.transactions(enrichedTransactions);
          self.customer({
            id: customer.id,
            name: 'Customer ' + customer.id.slice(0, 8),
            type: customer.accountHolderType || 'Unknown',
            kyc: customer.kycVerificationStatus || 'Unknown',
            riskCountry: customer.riskCountryFlag ? 'Yes' : 'No',
            onboarded: customer.createdAt ? new Date(customer.createdAt).toLocaleDateString() : '-',
            score: score,
            risk: highest ? highest.riskCategory : self.riskClass(score).toUpperCase(),
            accountCount: accounts.length,
            transactionCount: enrichedTransactions.length,
            explanation: highest ? highest.explanation : (accounts.length ? 'No flagged behavior was found across this customer\'s transactions.' : 'No accounts are linked to this customer yet.'),
            recommendation: highest ? highest.recommendation : 'Continue routine monitoring.'
          });
        });
      })
      .catch(function (error) { self.error(error.message); })
      .finally(function () { self.loading(false); });
  }

  return CustomerDetailViewModel;
});
