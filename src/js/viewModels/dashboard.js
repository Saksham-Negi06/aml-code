define(['knockout', 'services/api', 'viewModels/base'], function (ko, api, BaseViewModel) {
  'use strict';

  function DashboardViewModel(params) {
    var self = this;
    BaseViewModel.call(self, params && params.rootRouter);

    self.metrics = ko.observableArray([]);
    self.activity = ko.observableArray([]);
    self.activityRange = ko.observable('No transactions available');
    self.recentTransactions = ko.observableArray([]);
    self.riskDistribution = ko.observableArray([]);
    self.riskTotal = ko.observable(0);
    self.donutStyle = ko.observable({ background: '#e7eeeb' });
    self.error = ko.observable('');

    self.screenTransaction = function () {
      window.alert('Transaction screening form ready for API submission.');
    };

    function transactionTime(transaction) {
      return new Date(transaction.transactionDatetime || transaction.createdAt || 0);
    }

    function formatDate(date) {
      return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric' }).format(date);
    }

    function categoryRank(category) {
      return { UNASSESSED: 0, LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }[category] || 0;
    }

    function makeActivity(transactions) {
      var today = new Date();
      today.setHours(0, 0, 0, 0);
      var dayCounts = [];
      var counts = {};
      transactions.forEach(function (transaction) {
        var date = transactionTime(transaction);
        if (!isNaN(date.getTime())) {
          date.setHours(0, 0, 0, 0);
          counts[date.toISOString().slice(0, 10)] = (counts[date.toISOString().slice(0, 10)] || 0) + 1;
        }
      });
      for (var offset = 13; offset >= 0; offset -= 1) {
        var day = new Date(today);
        day.setDate(today.getDate() - offset);
        dayCounts.push({ label: formatDate(day), count: counts[day.toISOString().slice(0, 10)] || 0 });
      }
      var maximum = Math.max.apply(null, dayCounts.map(function (day) { return day.count; }).concat([1]));
      self.activity(dayCounts.map(function (day) {
        return { label: day.label, count: day.count, height: day.count ? Math.max(10, (day.count / maximum) * 100) : 0 };
      }));
      self.activityRange(dayCounts[0].label + ' – ' + dayCounts[dayCounts.length - 1].label);
    }

    function makeDistribution(customers, accounts, transactions, assessments) {
      var customerByAccount = {};
      var customerCategory = {};
      customers.forEach(function (customer) { customerCategory[customer.id] = 'UNASSESSED'; });
      accounts.forEach(function (account) { customerByAccount[account.id] = account.customerId; });
      transactions.forEach(function (transaction, index) {
        var assessment = assessments[index];
        var category = assessment && assessment.riskCategory;
        if (!category) return;
        [transaction.senderAccountId, transaction.receiverAccountId].forEach(function (accountId) {
          var customerId = customerByAccount[accountId];
          if (customerId && categoryRank(category) > categoryRank(customerCategory[customerId])) {
            customerCategory[customerId] = category;
          }
        });
      });

      var categories = [
        { label: 'Low', key: 'LOW', color: '#0f7657', className: 'legend-low' },
        { label: 'Medium', key: 'MEDIUM', color: '#e0a642', className: 'legend-medium' },
        { label: 'High', key: 'HIGH', color: '#e0645a', className: 'legend-high' },
        { label: 'Critical', key: 'CRITICAL', color: '#7f1d1d', className: 'legend-critical' },
        { label: 'Unassessed', key: 'UNASSESSED', color: '#8aa19a', className: 'legend-unassessed' }
      ];
      categories.forEach(function (category) {
        category.count = Object.keys(customerCategory).filter(function (customerId) {
          return customerCategory[customerId] === category.key;
        }).length;
      });
      var total = customers.length;
      self.riskTotal(total);
      self.riskDistribution(categories);
      if (!total) {
        self.donutStyle({ background: '#e7eeeb' });
        return;
      }
      var position = 0;
      var stops = categories.filter(function (category) { return category.count; }).map(function (category) {
        var nextPosition = position + (category.count / total) * 100;
        var stop = category.color + ' ' + position + '% ' + nextPosition + '%';
        position = nextPosition;
        return stop;
      });
      self.donutStyle({ background: 'conic-gradient(' + stops.join(', ') + ')' });
    }

    Promise.all([api.get('/customers'), api.get('/accounts'), api.get('/transactions')])
      .then(function (result) {
        var customers = result[0] || [];
        var accounts = result[1] || [];
        var transactions = (result[2] || []).slice().sort(function (left, right) {
          return transactionTime(right) - transactionTime(left);
        });
        self.metrics([
          ['Total customers', String(customers.length), 'From customer service', 'danger-none'],
          ['Active accounts', String(accounts.length), 'From account service', 'danger-none'],
          ['Transactions monitored', String(transactions.length), 'From transaction service', 'danger-none']
        ]);
        self.recentTransactions(transactions.slice(0, 8).map(function (transaction) {
          transaction.displayDate = isNaN(transactionTime(transaction).getTime()) ? '' : transactionTime(transaction).toLocaleString();
          return transaction;
        }));
        makeActivity(transactions);
        return Promise.all(transactions.map(function (transaction) {
          return api.get('/transactions/' + encodeURIComponent(transaction.transactionId) + '/risk')
            .catch(function () { return null; });
        })).then(function (assessments) {
          makeDistribution(customers, accounts, transactions, assessments);
        });
      })
      .catch(function (error) { self.error(error.message); });
  }

  return DashboardViewModel;
});
