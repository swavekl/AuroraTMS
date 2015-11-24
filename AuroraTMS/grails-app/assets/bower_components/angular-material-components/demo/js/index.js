'use strict';

angular.module('demoApp', ['ngMaterial.components'])
.config(function($mdThemingProvider) {
  $mdThemingProvider.theme('datePickerTheme').primaryPalette('teal');
})
.controller('datePickerCtrl', function() {

  });
