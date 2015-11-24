(function() {
	'use strict';

	var campaignsModule = angular.module('campaigns', [ 'ui.router' ]);

	campaignsModule.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider.state('home.campaigns', {
				    url: 'campaigns',
				    views: {
				        'menu@': {
					          templateUrl: 'assets/partials/menu.html'
					        },
				        'content@': {
				            templateUrl: 'assets/partials/campaigns.html',
				            controller: 'CampaignController'
				        }
				    }

				})
			} ]);

	campaignsModule.controller('CampaignController', [ '$scope', function($scope) {
		$scope.something = 9;

	} ]);
	

})();
