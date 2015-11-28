(function() {
	'use strict';

	// configure ui.router routes
	angular.module('tournamentList', [ 'ui.router', 'ngResource', 'date-range' ])

	.config([ '$stateProvider', '$urlRouterProvider', 
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.tournamentList', {
					url : 'api/tournaments',
					privateUrl : false,
					views : {
						'content@' : {
							templateUrl : 'assets/partials/tournament/tournament-list.html',
							resolve: {
								session: 'session',

								// get a list of tournaments in the region				
								tournamentResource: 'tournamentResource',
								tournamentList: function(tournamentResource, $stateParams, session) {
									// figure out the region based on state of the currently logged in user if there is one
									// otherwise all tournaments should be returned
									return tournamentResource.query({offset: 0, max: 10, username: session.getUser()}).$promise;
								}
							},
							controller : 'tournamentListController'
						}
					}
				})
				.state('home.tournamentManageList', {
					url : 'api/tournaments',
					privateUrl : true,
					data: {
						roles: ['ROLE_TOURNAMENT_DIRECTOR', 'ROLE_ADMIN']
				    },
					views : {
						'menu@' : {
							templateUrl : 'assets/partials/tournament/tournament-menu.html'
						},
						'content@' : {
							templateUrl : 'assets/partials/tournament/tournament-manage-list.html',
							resolve: {
								session: 'session',
								tournamentResource: 'tournamentResource',
								tournamentList: function(tournamentResource, $stateParams, session) {
									if (session.getUser() != null) {
										console.log ('getting tournaments for tournament director ' + session.getUser());
										return tournamentResource.list({offset: 0, max: 10}).$promise;
									} else {
										return [];
									}
								}
							},
							controller : 'tournamentListController'
						}
					}
				})
			} ])
			
	// define controller functions
	.controller('tournamentListController', 
			['$scope', '$state', 'session','tournamentResource', 'tournamentList',
    function($scope, $state, session, tournamentResource, tournamentList) {
		
		// list criteria
		$scope.allRegions = ['All regions', 'East', 'Midwest', 'Mountain', 'North', 'Northwest', 'Pacific', 'South Central', 'Southeast'];
		$scope.search = {
				region: $scope.allRegions[0],
				offset: 0,
				max: 10
		}
		// tournament data
		$scope.tournamentList = tournamentList;
		
		// callback for successful list return
		$scope.success = function (value, responseHeaders) {
			$scope.tournamentList = value;
		}
		
		// filter tournaments by region
		$scope.filterByRegion = function (region) {
			tournamentResource.query({offset: 0, max: 10, region: region, username: ""}, $scope.success);
		}
		
		$scope.showTournamentDetails = function (tournamentId, event) {
			console.log ('showing tournament details ' + tournamentId);
			var params = {id: tournamentId};
			$state.go('home.tournamentView', params);
		}

		$scope.editTournament = function (tournamentId, event) {
			console.log ('editTournament tournament ' + tournamentId);
			var params = {id: tournamentId};
			$state.go('home.tournamentEdit', params);
		}
		
		$scope.addTournament = function (event) {
			console.log ('add tournament');
			$state.go('home.tournamentCreate');
		}
	} 
	])
})();
