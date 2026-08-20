define(['knockout','services/api','viewModels/base'], function(ko,api,BaseViewModel){
  'use strict';
  function CustomerDetailViewModel(params){
    var self=this;self.router=params&&params.rootRouter;BaseViewModel.call(self,self.router);var id=window.sessionStorage.getItem('aegis_selected_customer_id');
    self.customer=ko.observable(null);self.accounts=ko.observableArray([]);self.transactions=ko.observableArray([]);self.error=ko.observable('');self.loading=ko.observable(true);
    self.back=function(){if(self.router)self.router.go('customers');};self.runAssessment=function(){window.alert('Risk is calculated from the customer accounts and their latest transaction assessments.');};
    if(!id){self.error('No customer selected.');self.loading(false);return;}
    Promise.all([api.get('/customers/'+encodeURIComponent(id)),api.get('/accounts'),api.get('/transactions')]).then(function(result){
      var raw=result[0],accounts=(result[1]||[]).filter(function(a){return a.customerId===raw.id;}),accountIds=accounts.map(function(a){return a.id;}),tx=(result[2]||[]).filter(function(t){return accountIds.indexOf(t.senderAccountId)>=0||accountIds.indexOf(t.receiverAccountId)>=0;});
      return Promise.all(tx.map(function(t){return api.get('/transactions/'+encodeURIComponent(t.transactionId)+'/risk').catch(function(){return null;}).then(function(r){t.riskScore=r?Math.round(Number(r.riskScore||0)):0;t.riskCategory=r&&r.riskCategory||'LOW';t.explanation=r&&r.oneLineExplanation||'No risk explanation is available.';t.recommendation=r&&r.recommendation||'Continue routine monitoring.';t.displayDate=t.transactionDatetime?new Date(t.transactionDatetime).toLocaleString():'—';return t;});})).then(function(enriched){
        var score=Math.round(accounts.reduce(function(sum,a){return sum+Number(a.accountRiskScore||0);},0)/(accounts.length||1));var highest=enriched.reduce(function(best,t){return t.riskScore>best.riskScore?t:best;},{riskScore:score,riskCategory:self.riskClass(score).toUpperCase()});
        self.accounts(accounts);self.transactions(enriched);self.customer({id:raw.id,name:'Customer '+raw.id.slice(0,8),type:raw.accountHolderType||'Unknown',kyc:raw.kycVerificationStatus||'Unknown',riskCountry:raw.riskCountryFlag?'Yes':'No',onboarded:raw.createdAt?new Date(raw.createdAt).toLocaleDateString():'—',score:Math.max(score,highest.riskScore||0),risk:highest.riskCategory||self.riskClass(score).toUpperCase(),accountCount:accounts.length,transactionCount:enriched.length,explanation:highest.explanation||'No flagged behavior was found.',recommendation:highest.recommendation||'Continue routine monitoring.'});
      });
    }).catch(function(e){self.error(e.message);}).finally(function(){self.loading(false);});
  } return CustomerDetailViewModel;
});
