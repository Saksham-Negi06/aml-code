define(['ojs/ojarraydataprovider','models/data','viewModels/base','ojs/ojtable'], function(ArrayDataProvider,data,BaseViewModel){
  'use strict';
  function RulesViewModel(){var self=this;BaseViewModel.call(self);self.rows=data.rules.map(function(r){return{rule:r[0],name:r[1],condition:r[2],threshold:r[3],severity:r[4],status:'ACTIVE',hits:r[5]};});self.dataProvider=new ArrayDataProvider(self.rows,{keyAttributes:'rule'});self.columns=[{headerText:'RULE',field:'rule'},{headerText:'NAME',field:'name'},{headerText:'CONDITION',field:'condition'},{headerText:'THRESHOLD',field:'threshold'},{headerText:'SEVERITY',field:'severity'},{headerText:'STATUS',field:'status'},{headerText:'HITS (30D)',field:'hits'}];self.create=function(){window.alert('Create rule form ready.');};}
  return RulesViewModel;
});
