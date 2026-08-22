define(['knockout', 'services/api', 'viewModels/base'], function (ko, api, BaseViewModel) {
  'use strict';

  // Values the AML risk model was trained on (see ml/ml_req/output/split_info.json and
  // ml/ml_req/fastapi_app.py's category_code() alias table). Sending anything outside these
  // vocabularies still works, but the model treats it as an unrecognized ("unknown") category.
  var CURRENCIES = [
    { value: 'USD', label: 'USD — US dollar' },
    { value: 'EUR', label: 'EUR — Euro' },
    { value: 'GBP', label: 'GBP — UK pounds' },
    { value: 'INR', label: 'INR — Indian rupee' },
    { value: 'JPY', label: 'JPY — Yen' },
    { value: 'CHF', label: 'CHF — Swiss franc' },
    { value: 'AED', label: 'AED — Dirham' },
    { value: 'PKR', label: 'PKR — Pakistani rupee' },
    { value: 'NGN', label: 'NGN — Naira' },
    { value: 'TRY', label: 'TRY — Turkish lira' },
    { value: 'MAD', label: 'MAD — Moroccan dirham' },
    { value: 'MXN', label: 'MXN — Mexican peso' },
    { value: 'ALL', label: 'ALL — Albanian lek' }
  ];

  var BANK_LOCATIONS = [
    { value: 'US', label: 'United States' },
    { value: 'GB', label: 'United Kingdom' },
    { value: 'IN', label: 'India' },
    { value: 'DE', label: 'Germany' },
    { value: 'FR', label: 'France' },
    { value: 'AT', label: 'Austria' },
    { value: 'IT', label: 'Italy' },
    { value: 'JP', label: 'Japan' },
    { value: 'MX', label: 'Mexico' },
    { value: 'MA', label: 'Morocco' },
    { value: 'NL', label: 'Netherlands' },
    { value: 'NG', label: 'Nigeria' },
    { value: 'PK', label: 'Pakistan' },
    { value: 'ES', label: 'Spain' },
    { value: 'CH', label: 'Switzerland' },
    { value: 'TR', label: 'Turkey' },
    { value: 'AE', label: 'UAE' },
    { value: 'AL', label: 'Albania' }
  ];

  var PAYMENT_TYPES = [
    { value: 'ACH', label: 'ACH transfer' },
    { value: 'CASH_DEPOSIT', label: 'Cash deposit' },
    { value: 'CASH_WITHDRAWAL', label: 'Cash withdrawal' },
    { value: 'CHEQUE', label: 'Cheque' },
    { value: 'CREDIT_CARD', label: 'Credit card' },
    { value: 'DEBIT_CARD', label: 'Debit card' },
    { value: 'CROSS_BORDER', label: 'Cross-border transfer' }
  ];

  function toLocalDatetimeInputValue(date) {
    var pad = function (n) { return String(n).padStart(2, '0'); };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) +
      'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  }

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
    self.accountOptions = ko.observableArray([]);

    // Screen-transaction form
    self.currencyOptions = CURRENCIES;
    self.bankLocationOptions = BANK_LOCATIONS;
    self.paymentTypeOptions = PAYMENT_TYPES;
    self.showScreenForm = ko.observable(false);
    self.formSubmitting = ko.observable(false);
    self.formError = ko.observable('');
    self.formResult = ko.observable(null);
    self.formSender = ko.observable('');
    self.formReceiver = ko.observable('');
    self.formAmount = ko.observable('');
    self.formPaymentCurrency = ko.observable('USD');
    self.formReceivedCurrency = ko.observable('USD');
    self.formSenderLocation = ko.observable('US');
    self.formReceiverLocation = ko.observable('US');
    self.formPaymentType = ko.observable('ACH');
    self.formDatetime = ko.observable('');
    self.formUseGemini = ko.observable(false);

    self.screenTransaction = function () {
      self.formError('');
      self.formResult(null);
      self.formSender(self.accountOptions()[0] ? self.accountOptions()[0].id : '');
      self.formReceiver(self.accountOptions()[1] ? self.accountOptions()[1].id : '');
      self.formAmount('');
      self.formDatetime(toLocalDatetimeInputValue(new Date()));
      self.showScreenForm(true);
    };

    self.closeScreenForm = function () {
      self.showScreenForm(false);
    };

    self.screenAnother = function () {
      self.formResult(null);
      self.formError('');
      self.formAmount('');
      self.formDatetime(toLocalDatetimeInputValue(new Date()));
    };

    self.submitScreenTransaction = function () {
      var sender = self.formSender();
      var receiver = self.formReceiver();
      var amount = Number(self.formAmount());

      if (!sender || !receiver) { self.formError('Choose both a sender and a receiver account.'); return; }
      if (sender === receiver) { self.formError('Sender and receiver accounts must be different.'); return; }
      if (!amount || amount <= 0) { self.formError('Enter an amount greater than zero.'); return; }
      if (!self.formDatetime()) { self.formError('Choose a transaction date and time.'); return; }

      var isoDatetime;
      try {
        isoDatetime = new Date(self.formDatetime()).toISOString();
        if (isNaN(new Date(self.formDatetime()).getTime())) throw new Error('invalid');
      } catch (dateError) {
        self.formError('The transaction date and time is invalid.');
        return;
      }

      self.formError('');
      self.formSubmitting(true);

      var payload = {
        transaction_id: 'TXN-' + Date.now().toString(36).toUpperCase() + '-' + Math.random().toString(36).slice(2, 6).toUpperCase(),
        sender_account: sender,
        receiver_account: receiver,
        amount: amount,
        payment_currency: self.formPaymentCurrency(),
        received_currency: self.formReceivedCurrency(),
        sender_bank_location: self.formSenderLocation(),
        receiver_bank_location: self.formReceiverLocation(),
        payment_type: self.formPaymentType(),
        transaction_datetime: isoDatetime,
        use_gemini: self.formUseGemini()
      };

      api.post('/transactions', payload)
        .then(function () {
          return api.get('/transactions/' + encodeURIComponent(payload.transaction_id) + '/risk');
        })
        .then(function (risk) {
          self.formResult({
            transactionId: payload.transaction_id,
            riskCategory: (risk && risk.riskCategory) || 'UNKNOWN',
            explanation: (risk && risk.oneLineExplanation) || 'No explanation returned.',
            recommendation: (risk && risk.recommendation) || 'Continue routine monitoring.',
            shouldFlag: Boolean(risk && risk.shouldFlag)
          });
          return loadDashboard();
        })
        .catch(function (error) { self.formError(error.message); })
        .finally(function () { self.formSubmitting(false); });
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
      var counts = {};
      transactions.forEach(function (transaction) {
        var date = transactionTime(transaction);
        if (!isNaN(date.getTime())) {
          date.setHours(0, 0, 0, 0);
          counts[date.toISOString().slice(0, 10)] = (counts[date.toISOString().slice(0, 10)] || 0) + 1;
        }
      });
      var dayCounts = [];
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
        { label: 'Low', key: 'LOW', color: 'var(--risk-low)', className: 'legend-low' },
        { label: 'Medium', key: 'MEDIUM', color: 'var(--risk-medium)', className: 'legend-medium' },
        { label: 'High', key: 'HIGH', color: 'var(--risk-high)', className: 'legend-high' },
        { label: 'Critical', key: 'CRITICAL', color: 'var(--risk-critical)', className: 'legend-critical' },
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

    function loadDashboard() {
      return Promise.all([api.get('/customers'), api.get('/accounts'), api.get('/transactions')])
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
          self.accountOptions(accounts.map(function (account) {
            return {
              id: account.id,
              label: (account.accountType || 'Account') + ' · ' + (account.bankLocation || 'Unknown') + ' · ' + String(account.id).slice(0, 8)
            };
          }));
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

    loadDashboard();
  }

  return DashboardViewModel;
});
