(function() {
	'use strict';

	// configure ui.router routes
	angular.module('tournamentEntry', [ 'ui.router', 'ngResource' ])

	.config([ '$stateProvider', '$urlRouterProvider', 
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.tournamentEntry', {
					url : 'api/tournamententry',
					privateUrl : true,
					data: {
						roles: ['ROLE_USER']
				    },
					resolve: {
						session: 'session',
						tournamentEntryResource: 'tournamentEntryResource',
						tournamentEntry: function(tournamentEntryResource, $stateParams, session) {
							return {};
						}
					},
					views : {
						'menu@' : {
							templateUrl : 'assets/partials/menu.html'
						},
						'content@' : {
							templateUrl : 'assets/partials/tournament/entry/entry.html',
							controller : 'tournamentEntryController'
						},
						'membership-tab@home.tournamentEntry' : {
							templateUrl : 'assets/partials/tournament/entry/entry-membership.html',
						},
						
						'events-tab@home.tournamentEntry' : {
							templateUrl : 'assets/partials/tournament/entry/entry-events.html',
						},
						
						'invoice-tab@home.tournamentEntry' : {
							templateUrl : 'assets/partials/tournament/entry/entry-invoice.html',
						},
						
						'payment-tab@home.tournamentEntry' : {
							templateUrl : 'assets/partials/tournament/entry/entry-payment.html',
						}
					}
				})
			} ])
			
	// define controller functions
	.controller('tournamentEntryController', 
			['$scope', '$state', 'session','tournamentEntryResource', 'tournamentEntry',
    function($scope, $state, session, tournamentEntryResource, tournamentEntry) {
		
		// tournament entry
		$scope.tournamentEntry = tournamentEntry;
		
		$scope.enteredEventsList = [
		{eventName:'Open Doubles', entryDateTime: 'Fri 6:00 PM', eventFee: '$28'},
		{eventName:'Open Singles', entryDateTime: 'Sat 9:00 AM', eventFee: '$45'},
		{eventName:'U2500', entryDateTime: 'Sun 9:00 AM', eventFee: '$32'},
		{eventName:'U2400', entryDateTime: 'Sat 11:00 AM', eventFee: '$28'}
        ];
		
		$scope.availableEventsList = [
		                    		{eventName:'U3800 Doubles', entryDateTime: 'Sat 6:00 PM', eventFee: '$28'},
		                    		{eventName:'U2300', entryDateTime: 'Sun 3:00 PM', eventFee: '$32'},
		                    		{eventName:'U2200', entryDateTime: 'Sat 12:30 PM', eventFee: '$28'}
		                            ];

		$scope.unavailableEventsList = [
			                    		{eventName:'Youth Under 18', entryDateTime: 'Sun 9:00 AM', eventFee: '$32', reason: 'Event is full'},
			                    		{eventName:'Youth Under 14', entryDateTime: 'Sun 9:00 AM', eventFee: '$32', reason: 'Age restriction'},
			                    		{eventName:'U2100', entryDateTime: 'Sun 9:00 AM', eventFee: '$32', reason: 'Time conflict'},
			                    		{eventName:'U2000', entryDateTime: 'Sat 3:00 PM', eventFee: '$28', reason: 'Rating too high'},
			                    		{eventName:'Women Singles', entryDateTime: 'Sat 5:00 PM', eventFee: '$28', reason: 'Gender'}
			                            ];
		
		// callback for successful list return
		$scope.success = function (value, responseHeaders) {
			$scope.tournamentEntry = value;
		}
		
		$scope.editTournamentEntry = function (tournamentId, event) {
			console.log ('editTournamentEntry tournament ' + tournamentId);
//			var params = {id: tournamentId};
//			$state.go('home.tournamentEntry', params);
		}
	} 
	])
})();
