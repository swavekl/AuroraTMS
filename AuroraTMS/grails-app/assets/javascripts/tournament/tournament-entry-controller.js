(function() {
	'use strict';

	// configure ui.router routes
	angular.module('tournamentEntry', [ 'ui.router', 'ngResource' ])

	.config([ '$stateProvider', '$urlRouterProvider', 
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.tournamentEntry', {
					url : 'api/tournamententry/:tournamentId',
					privateUrl : true,
					data: {
						roles: ['ROLE_USER']
				    },
					resolve: {
						session: 'session',
						
						tournamentResource: 'tournamentResource',
						tournament: function(tournamentResource, $stateParams, session) {
							return tournamentResource.view({id: $stateParams.tournamentId}).$promise;
						},

						tournamentEntryResource: 'tournamentEntryResource',
						tournamentEntry: function(tournamentEntryResource, $stateParams, session) {
							var params = {tournamentId: $stateParams.tournamentId};
							return tournamentEntryResource.create (params).$promise;
						},

						userProfileResource: 'userProfileResource',
						userProfile: function(userProfileResource, $stateParams, session) {
							var userId = session.getUser();
							var userProfileId = session.getUserProfileId();
							console.log ('session user id ' + userId + ", user profile id " + userProfileId);
							if (userProfileId != undefined) {
								console.log ('getting user profile with id ' + userProfileId);
								return userProfileResource.get({id: userProfileId}).$promise;
							} else {
								console.log ('gettting profile by userid ' + userId);
								return userProfileResource.editByUsername({username: userId}).$promise;
							}
						},
						
//						usattProfileResource: 'usattProfileResource',
//						usattProfile: function(usattProfileResource, userProfile, session) {
//							var memberId = userProfile.usattID;
//							console.log ('memberId = ' + memberId);
//							if (memberId != undefined && memberId != 0) {
//								console.log ('gettting USATT profile by memberId ' + memberId);
//								return usattProfileResource.queryByMemberId({memberId: memberId}).$promise;
//							} else {
//								return {};
//							}
//						},

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
			['$scope', '$state', 'session','tournamentResource', 'tournament', 'tournamentEntryResource', 'tournamentEntry','userProfileResource', 'userProfile',
    function($scope, $state, session, tournamentResource, tournament, tournamentEntryResource, tournamentEntry, userProfileResource, userProfile) {
		
		// tournament entry
		$scope.tournament = tournament;
		$scope.tournamentEntry = tournamentEntry;
		if ($scope.tournamentEntry.tournament == null) {
			$scope.tournamentEntry.tournament = tournament;
		}
		$scope.userProfile = userProfile;
		$scope.membershipExpired = false;
		// check if new USATT member
		if ($scope.userProfile.usattID == 0 || $scope.userProfile.usattID > 90000) {
			$scope.membershipExpired = true;
		} else {
			// check if expires before tournament
			var tournamentDate = (tournament != null && tournament.endDate != null) ? new Date (tournament.endDate) : new Date();
			var membershipExpirationDate = ($scope.userProfile.expirationDate) ? new Date ($scope.userProfile.expirationDate) : new Date();
			$scope.userProfile.expirationDate = membershipExpirationDate;
			$scope.membershipExpired = moment(membershipExpirationDate).isBefore(tournamentDate, 'day') || 
			                           moment(membershipExpirationDate).isSame(tournamentDate, 'day'); 
		}
		
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
