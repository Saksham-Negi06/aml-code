define([
  'knockout',
  'ojs/ojarraydataprovider',
  'services/api',
  'viewModels/base',
  'ojs/ojtable'
], function (ko, ArrayDataProvider, api, BaseViewModel) {
  'use strict';

  function CustomersViewModel(params) {
    var self = this;
    self.router = params && params.rootRouter;
    BaseViewModel.call(self, self.router);
    self.search = ko.observable('');
    self.globalSearch = params && params.globalSearch || ko.observable('');
    self.customers = ko.observableArray([]);
    self.error = ko.observable('');
    self.loading = ko.observable(true);
    self.filteredCustomers = ko.computed(function () {
      var q = (self.search() || self.globalSearch() || '').toLowerCase();
      return self.customers().filter(function (c) {
        return (c.id + ' ' + c.accountHolderType + ' ' + c.kycVerificationStatus + ' ' + c.riskLevel).toLowerCase().indexOf(q) >= 0;
      });
    });
    self.dataProvider = ko.pureComputed(function () {
      return new ArrayDataProvider(self.filteredCustomers(), { keyAttributes: 'id' });
    });
    self.columns = [
      { headerText: 'CUSTOMER ID', field: 'id' },
      { headerText: 'HOLDER TYPE', field: 'accountHolderType' },
      { headerText: 'KYC STATUS', field: 'kycVerificationStatus' },
      { headerText: 'RISK COUNTRY', field: 'riskCountryFlag' },
      { headerText: 'ACCOUNTS', field: 'accountCount' },
      { headerText: 'CREATED', field: 'createdAt' }
    ];
    self.addCustomer = function () { window.alert('Customer creation is not available in this view.'); };
    self.openCustomer = function (customer) {
      if (!customer || !self.router) return;
      window.sessionStorage.setItem('aegis_selected_customer_id', customer.id);
      self.router.go('customer-detail');
    };
    Promise.all([api.get('/customers'), api.get('/accounts')]).then(function (result) {
      var accounts = result[1] || [];
      self.customers((result[0] || []).map(function (customer) {
        var linked = accounts.filter(function (account) { return account.customerId === customer.id; });
        customer.riskScore = Math.round(linked.reduce(function (sum, account) { return sum + Number(account.accountRiskScore || 0); }, 0) / (linked.length || 1));
        customer.riskLevel = self.riskClass(customer.riskScore).toUpperCase();
        customer.accountCount = (customer.accountIds || []).length;
        customer.riskCountryFlag = customer.riskCountryFlag ? 'Yes' : 'No';
        customer.createdAt = customer.createdAt ? new Date(customer.createdAt).toLocaleString() : '';
        return customer;
      }));
    }).catch(function (error) { self.error(error.message); }).finally(function () { self.loading(false); });
  }
  return CustomersViewModel;
});
