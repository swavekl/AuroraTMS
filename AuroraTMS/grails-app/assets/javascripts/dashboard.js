(function() {
	'use strict';

	var dashboardModule = angular.module('dashboard', [ 'ui.router' ]);

	dashboardModule.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider.state('home.dashboard', {
					url : 'dashboard',
					privateUrl : true,
					views : {
						'content@' : {
							templateUrl : 'assets/partials/dashboard.html',
							controller : 'DashboardController'
						}
					}

				})
			} ]);

	dashboardModule.controller('DashboardController', [ '$scope',
			function($scope) {
				$scope.somthing = 8;

			} ]);

})();
