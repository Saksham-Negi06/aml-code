define([
  'ojs/ojcore',
  'knockout',
  'ojs/ojrouter',
  'ojs/ojknockout',
  'ojs/ojnavigationlist',
  'ojs/ojbutton',
  'ojs/ojinputtext',
  'ojs/ojknockouttemplateutils',
  'ojs/ojmodule-element-utils',
  'ojs/ojcheckboxset',
  'ojs/ojarraydataprovider',
  'ojs/ojresponsiveutils',
  'ojs/ojresponsiveknockoututils',
  'ojs/ojdrawerpopup',
  'ojs/ojmenu',
  'ojs/ojoption'
], function (oj, ko, Router, koBinding, NavigationList, Button, InputText, KnockoutTemplateUtils, ModuleElementUtils, Checkboxset, ArrayDataProvider, ResponsiveUtils, ResponsiveKnockoutUtils) {
  'use strict';

  function ControllerViewModel() {
    var self = this;

    self.router = Router.rootInstance;
    self.router.configure({
      dashboard: { label: 'Dashboard', isDefault: true },
      customers: { label: 'Customer risk' },
      'customer-detail': { label: 'Customer detail' },
      transactions: { label: 'Transactions' },
      alerts: { label: 'AML alerts' },
      investigations: { label: 'Investigations' },
      reports: { label: 'Reports' },
      rules: { label: 'Rules' }
    });
    Router.defaults['urlAdapter'] = new Router.urlParamAdapter();

    Router.sync();

    // oj-module needs a view/viewModel configuration, not the legacy ojRouter
    // name-only configuration.  ModuleElementUtils creates it for each route.
    self.activeView = ko.observable('dashboard');
    self.searchText = ko.observable('');
    self.router.stateId.subscribe(function (route) {
      if (route) self.activeView(route);
    });
    // oj-module binds before createConfig's Promise resolves. An empty view is
    // a valid initial config and prevents the binding from receiving undefined.
    self.moduleConfig = ko.observable({ view: [], viewModel: null });
    self.moduleAdapter = { koObservableConfig: self.moduleConfig };
    var navigationSequence = 0;
    function loadModule(name) {
      var sequence = ++navigationSequence;
      return ModuleElementUtils.createConfig({
        name: name,
        params: { rootRouter: self.router, globalSearch: self.searchText }
      }).then(function (config) {
        if (sequence === navigationSequence) self.moduleConfig(config);
      });
    }
    loadModule(self.activeView());
    self.activeView.subscribe(loadModule);
    self.selection = { path: self.router.stateId };
    self.sideDrawerOn = ko.observable(false);
    self.smScreen = ResponsiveKnockoutUtils.createMediaQueryObservable(
      ResponsiveUtils.getFrameworkQuery(ResponsiveUtils.FRAMEWORK_QUERY_KEY.SM_ONLY)
    );
    self.appName = ko.observable('AML command center');
    self.userLogin = ko.observable('jeel.doshi@aegis.example');
    self.footerLinks = [
      { name: 'About', linkId: 'about', linkTarget: '#about' },
      { name: 'Privacy', linkId: 'privacy', linkTarget: '#privacy' },
      { name: 'Terms of use', linkId: 'terms', linkTarget: '#terms' }
    ];

    self.toggleDrawer = function () {
      self.sideDrawerOn(!self.sideDrawerOn());
    };

    // Authentication is intentionally demo-only: each new app load begins at secure access.
    window.localStorage.removeItem('aegis_demo_token');
    self.isAuthenticated = ko.observable(false);
    self.authMode = ko.observable('login');
    self.authMessage = ko.observable('');
    self.message = ko.observable('');
    self.manner = ko.observable('polite');
    self.KnockoutTemplateUtils = KnockoutTemplateUtils;
    self.username = ko.observable('admin');
    self.password = ko.observable('password123');
    self.passwordVisible = ko.observable(false);
    self.signupName = ko.observable('');
    self.signupEmail = ko.observable('');
    self.signupPassword = ko.observable('');
    self.mobileMenuOpen = ko.observable(false);
    self.toastMessage = ko.observable('');
    self.alertCount = ko.observable(9);
    self.currentUser = ko.observable({ name: 'Jeel Doshi', role: 'Senior compliance', initials: 'JD' });

    self.wireBrandHome = function () {
      var brandHome = document.querySelector('.sidebar-brand');
      if (!brandHome || brandHome.dataset.homeLinkBound) return;
      brandHome.dataset.homeLinkBound = 'true';
      brandHome.classList.add('brand-home');
      brandHome.setAttribute('role', 'button');
      brandHome.setAttribute('tabindex', '0');
      brandHome.setAttribute('aria-label', 'Go to dashboard');
      brandHome.addEventListener('click', function () { self.goHome(); });
      brandHome.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); self.goHome(); }
      });
    };

    // The compact shell uses a native input, so connect it to the observable
    // shared with each routed view.
    window.setTimeout(function () {
      var input = document.querySelector('.topbar .search input');
      if (input) input.addEventListener('input', function () { self.searchText(input.value); });
      var topbar = document.querySelector('.topbar');
      var sidebar = document.querySelector('.sidebar');
      if (topbar && sidebar) {
        var menu = document.createElement('button'); menu.className = 'menu-button'; menu.type = 'button'; menu.setAttribute('aria-label', 'Open navigation'); menu.textContent = '☰';
        menu.addEventListener('click', function () { self.openMenu(); }); topbar.insertBefore(menu, topbar.firstChild);
        self.mobileMenuOpen.subscribe(function (open) { sidebar.classList.toggle('open', open); });
      }
      self.wireBrandHome();
    }, 0);

    self.announce = function (data, event) {
      self.message(event.detail.message);
      self.manner(event.detail.manner);
    };

    var navData = [
      { path: 'dashboard', detail: { label: 'Dashboard', iconClass: 'oj-ux-ico-home' } },
      { path: 'customers', detail: { label: 'Customer risk', iconClass: 'oj-ux-ico-contact' } },
      { path: 'transactions', detail: { label: 'Transactions', iconClass: 'oj-ux-ico-arrow-switch' } },
      { path: 'alerts', detail: { label: 'AML alerts', iconClass: 'oj-ux-ico-warning', badge: 9 } },
      { path: 'investigations', detail: { label: 'Investigations', iconClass: 'oj-ux-ico-task' } },
      { path: 'reports', detail: { label: 'Reports', iconClass: 'oj-ux-ico-report' } }
    ];

    self.navItems = navData;
    self.navDataProvider = new ArrayDataProvider(navData, { keyAttributes: 'path' });

    self.goToRoute = function (item) {
      if (!item || !item.path) return;
      self.activeView(item.path);
      self.router.go(item.path);
      self.mobileMenuOpen(false);
    };

    self.goHome = function () {
      self.activeView('dashboard');
      self.router.go('dashboard');
      self.mobileMenuOpen(false);
    };

    self.login = function () {
      self.isAuthenticated(true);
      window.setTimeout(self.wireBrandHome, 0);
      self.activeView('dashboard');
      self.router.go('dashboard');
      self.showToast('Secure session started');
    };

    self.showSignup = function () {
      self.authMessage('');
      self.authMode('signup');
    };

    self.showLogin = function () {
      self.authMessage('');
      self.authMode('login');
    };

    self.signup = function () {
      if (!self.signupName().trim() || !self.signupEmail().trim() || !self.signupPassword()) {
        self.authMessage('Complete all fields to create your workspace access.');
        return;
      }
      self.userLogin(self.signupEmail().trim());
      self.currentUser({ name: self.signupName().trim(), role: 'Compliance analyst', initials: self.signupName().trim().slice(0, 2).toUpperCase() });
      self.isAuthenticated(true);
      window.setTimeout(self.wireBrandHome, 0);
      self.activeView('dashboard');
      self.router.go('dashboard');
      self.showToast('Your secure workspace is ready');
    };

    self.logout = function () {
      self.isAuthenticated(false);
      self.authMode('login');
      self.mobileMenuOpen(false);
      self.showToast('Signed out successfully');
    };

    self.togglePassword = function () {
      self.passwordVisible(!self.passwordVisible());
    };

    self.showNotifications = function () {
      self.showToast('3 new critical alert notifications');
    };

    self.showHelp = function () {
      self.showToast('Compliance workspace help is available.');
    };

    self.forgotPassword = function () {
      self.showToast('Password recovery is available through your administrator.');
    };

    self.openMenu = function () {
      self.mobileMenuOpen(!self.mobileMenuOpen());
    };

    self.closeMenu = function () {
      self.mobileMenuOpen(false);
    };

    self.showToast = function (message) {
      self.toastMessage(message);
      window.setTimeout(function () {
        if (self.toastMessage() === message) self.toastMessage('');
      }, 2800);
    };

    self.goToCustomers = function () {
      self.router.go('customers');
    };
  }

  return new ControllerViewModel();
});
