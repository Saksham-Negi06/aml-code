define(['knockout','ojs/ojarraydataprovider','services/api','viewModels/base','ojs/ojtable'], function (ko, ArrayDataProvider, api, BaseViewModel) {
  'use strict';
  function AlertsViewModel(params){
    var self=this; BaseViewModel.call(self, params && params.rootRouter); self.severity=ko.observable('All severities'); self.status=ko.observable('All statuses');
    self.rows=ko.observableArray([]); self.error=ko.observable(''); self.loading=ko.observable(true); self.busy=ko.observable(false);
    self.search=params&&params.globalSearch||ko.observable('');
    self.filtered=ko.computed(function(){var q=String(self.search()||'').toLowerCase();return self.rows().filter(function(a){return (self.severity()==='All severities'||a.severity===self.severity())&&(self.status()==='All statuses'||a.status===self.status())&&(!q||[a.id,a.customer,a.txn,a.type,a.severity,a.status,a.analyst].join(' ').toLowerCase().indexOf(q)>=0);});});
    self.dataProvider=ko.pureComputed(function(){return new ArrayDataProvider(self.filtered(),{keyAttributes:'id'});}); self.selectedAlert=ko.observable(null); self.selectedCustomer=ko.observable(null); self.assignTo=ko.observable('');
    self.openAlert=function(alert){self.assignTo('');self.selectedAlert(alert||null);}; self.closeAlert=function(){self.selectedAlert(null);};
    self.goToRules=function(){if(self.router)self.router.go('rules');};
    function createCase(owner){var alert=self.selectedAlert();if(!alert)return Promise.resolve();self.busy(true);self.error('');return api.post('/cases',{transactionId:alert.txn,owner:owner||null,priority:alert.severity}).then(function(item){alert.status='ESCALATED';alert.analyst=item.owner||'Unassigned';alert.caseId=item.id;self.rows.valueHasMutated();return item;}).catch(function(e){self.error(e.message);throw e;}).finally(function(){self.busy(false);});}
    self.createInvestigation=function(){createCase(self.assignTo().trim()||null).then(function(){self.closeAlert();}).catch(function(){});};

    // An alert can come from the AML risk model (automatic) and/or a manually defined
    // detection rule (Rules page) matching on transaction amount. Both sources are merged
    // into one alert feed, tagged so analysts can see why each alert exists.
    function bestSeverity(candidates) {
      return candidates.reduce(function (best, category) {
        return self.categoryRank(category) > self.categoryRank(best) ? category : best;
      }, candidates[0]);
    }

    function buildAlert(transaction, risk, matchedRules, cases) {
      var mlFlagged = Boolean(risk && risk.shouldFlag);
      if (!mlFlagged && !matchedRules.length) return null;
      var caseItem = cases.find(function (c) { return c.transactionId === transaction.transactionId; });
      var severities = matchedRules.map(function (rule) { return rule.severity; });
      if (mlFlagged) severities.push(String(risk.riskCategory || 'HIGH').toUpperCase());
      var sources = matchedRules.map(function (rule) { return 'Rule: ' + rule.name; });
      if (mlFlagged) sources.unshift('ML model');
      var ruleNames = matchedRules.map(function (rule) { return rule.name; }).join(', ');
      return {
        id: (risk && risk.requestId) || transaction.id,
        customer: transaction.senderAccountId,
        txn: transaction.transactionId,
        type: mlFlagged ? (risk.oneLineExplanation || risk.riskCategory || 'AML risk model flag') : ('Matched rule: ' + ruleNames),
        severity: bestSeverity(severities),
        source: sources.join(' + '),
        confidence: risk && risk.modelConfidence != null ? Math.round(Number(risk.modelConfidence)) : null,
        status: caseItem ? caseItem.status : 'NEW',
        analyst: (caseItem && caseItem.owner) || 'Unassigned',
        created: risk && risk.createdAt ? new Date(risk.createdAt).toLocaleString() : (transaction.transactionDatetime ? new Date(transaction.transactionDatetime).toLocaleString() : ''),
        caseId: caseItem && caseItem.id
      };
    }

    Promise.all([api.get('/transactions'), api.get('/cases'), api.get('/rules')])
      .then(function (results) {
        var cases = results[1] || [];
        var activeRules = (results[2] || []).filter(function (rule) { return rule.status === 'ACTIVE'; });
        return Promise.all((results[0] || []).map(function (transaction) {
          var matchedRules = activeRules.filter(function (rule) {
            return Number(transaction.amount || 0) > Number(rule.amountThreshold || 0);
          });
          return api.get('/transactions/' + encodeURIComponent(transaction.transactionId) + '/risk')
            .then(function (risk) { return buildAlert(transaction, risk, matchedRules, cases); })
            .catch(function () { return buildAlert(transaction, null, matchedRules, cases); });
        }));
      })
      .then(function (items) { self.rows(items.filter(Boolean)); })
      .catch(function (e) { self.error(e.message); })
      .finally(function () { self.loading(false); });
  } return AlertsViewModel;
});
