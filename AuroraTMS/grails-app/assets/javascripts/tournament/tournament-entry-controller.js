//
// Controller for entering tournaments
//
(function() {
	'use strict';

	// configure ui.router routes
	angular.module('tournamentEntry', [ 'ui.router', 'ngResource' ])

	.config([ '$stateProvider', '$urlRouterProvider', 
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.tournamentEntry', {
					url : 'api/tournamententry/:tournamentId',
					data: {
						privateUrl : true,
						roles: ['ROLE_USER']
				    },
					resolve: {
						session: 'session',
						
						tournamentResource: 'tournamentResource',
						tournament: function(tournamentResource, $stateParams, session) {
							return tournamentResource.view({id: $stateParams.tournamentId}).$promise;
						},

						// all tournament events
						eventResource: 'eventResource',
						events: function(eventResource, $stateParams, session) {
							return eventResource.list({tournamentId: $stateParams.tournamentId}).$promise;
						},

						// user profile
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

						// the entry
						tournamentEntryResource: 'tournamentEntryResource',
						tournamentEntry: function(tournamentEntryResource, $stateParams, session) {
							var params = {tournamentId: $stateParams.tournamentId};
							return tournamentEntryResource.create (params).$promise;
						},
						
						// entered events
						eventEntryResource: 'eventEntryResource',
						eventEntries: function(eventEntryResource, tournamentEntry, $stateParams, session) {
							var params = {tournamentEntryId: tournamentEntry.id};
							return eventEntryResource.list (params).$promise;
						},

					},
					views : {
						'menu@' : {
							templateUrl : 'assets/partials/menu.html'
						},
						'content@' : {
							templateUrl : 'assets/partials/tournament/entry/entry.html',
							controller : 'tournamentEntryController'
						},
					}
				})
		        .state('home.tournamentEntry.events', {
		            url: '/events',
					templateUrl : 'assets/partials/tournament/entry/entry-events.html',
		        })
		        // url will be 'api/tournamententry/:tournamentId
		        .state('home.tournamentEntry.membership', {
		            url: '/membership',
					templateUrl : 'assets/partials/tournament/entry/entry-membership.html',
		        })
		        // url will be /form/payment
		        // url will be /form/interests
		        .state('home.tournamentEntry.invoice', {
		        	url: '/invoice',
		        	templateUrl : 'assets/partials/tournament/entry/entry-invoice.html',
		        })
		        // url will be /form/interests
		        .state('home.tournamentEntry.payment', {
		            url: '/payment',
					templateUrl : 'assets/partials/tournament/entry/entry-payment.html',
		        });
			} ])
			
	// define controller functions
	.controller('tournamentEntryController', 
			['$scope', '$state', 'session', '$mdDialog',
			 'tournamentResource', 'tournament', 
			 'eventResource', 'events', 
			 'tournamentEntryResource', 'tournamentEntry',
			 'userProfileResource', 'userProfile',
			 'eventEntryResource', 'eventEntries',
    function($scope, $state, session, $mdDialog,
    		tournamentResource, tournament, 
			eventResource, events, 
    		tournamentEntryResource, tournamentEntry, 
    		userProfileResource, userProfile, 
    		eventEntryResource, eventEntries) {
		//console.log ('in tournamentEntryController state = ' + $state.current.name);
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
		
		// here is the list of steps.  Membership may not be required if it is up to date
		$scope.steps = []
		$scope.steps.push ('home.tournamentEntry.events');
//		if ($scope.membershipExpired)
			$scope.steps.push ('home.tournamentEntry.membership');
		$scope.steps.push ('home.tournamentEntry.invoice');
		$scope.steps.push ('home.tournamentEntry.payment');
		
		$scope.getCurrentStepIndex = function () {
			var curState = $state.current.name;
			var index = 0;
			for (var i = 0; i < $scope.steps.length; i++) {
				if ($scope.steps[i] == curState) {
					index = i;
				}
			}
			return index;
		}
		
		//
		// Moves to the next step
		//
		$scope.nextStep = function () {
			var index = $scope.getCurrentStepIndex ();
			if (index < $scope.steps.length - 1) {
				index++;
			}
			
			var nextState = $scope.steps[index];
			$state.go (nextState, $state.params);
		}
		
		// 
		// moves to the previous step
		//
		$scope.prevStep = function () {
			var index = $scope.getCurrentStepIndex ();
			if (index > 0) {
				index--;
			}
			
			var prevState = $scope.steps[index];
			$state.go (prevState, $state.params);
		}
		
		//
		// checks if this is the last step
		//
		$scope.isFirstStep = function () {
			var index = $scope.getCurrentStepIndex ();
			return (index == 0);
		}
		
		//
		// checks if this is the last step
		//
		$scope.isLastStep = function () {
			var index = $scope.getCurrentStepIndex ();
			return (index == ($scope.steps.length - 1));
		}
		
		// -------------------------------------------------------------------------------------------------------------------
		// event entries
		// -------------------------------------------------------------------------------------------------------------------
		
		$scope.events = events;
		$scope.eventEntries = eventEntries;
		
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

		/*
//		var adultDate = new Date($scope.userProfile.dateOfBirth);
//		adultDate.setYear($scope.userProfile.dateOfBirth.getFullYear() + 18);
//		$isJunior = moment(adultDate).isSame(tournamentDate, 'day') || moment(adultDate).isAfter(tournamentDate, 'day');
*/
		$scope.isAdultUser = false;

		$scope.membershipTypes = [
		                          {membershipName: 'Adult 1-year (G)', fee: 75, availableToMembers: 1, availableToAdults: 1, membershipType: 1},
		                          {membershipName: 'Adult 3-year (G)', fee: 210, availableToMembers: 1, availableToAdults: 1, membershipType: 2},
		                          {membershipName: 'Adult 5-year (G)', fee: 325, availableToMembers: 1, availableToAdults: 1, membershipType: 3},
		                          {membershipName: 'Junior 1-year (G)', fee: 45, availableToMembers: 1, availableToAdults: 0, membershipType: 4},
		                          {membershipName: 'Junior 3-year (G)', fee: 125, availableToMembers: 1, availableToAdults: 0, membershipType: 5},
		                          {membershipName: 'Collegiate 1-Year (G)', fee: 45, availableToMembers: 1, availableToAdults: 1, membershipType: 6},
		                          {membershipName: 'Household 1-Year (G)', fee: 150, availableToMembers: 1, availableToAdults: 1, membershipType: 7},
		                          {membershipName: 'Lifetime (G)', fee: 1300, availableToMembers: 1, availableToAdults: 1, membershipType: 8},
//		                          {membershipName: 'Contributor (G)', fee: 45, availableToMembers: 1, availableToAdults: 1, membershipType: 9},
		                          {membershipName: 'Tournament Pass (per tournament) (A)', fee: 20, availableToMembers: 0, availableToAdults: 1, membershipType: 10},
		                          ];

		$scope.unavailableEventsList = [
			                    		{eventName:'Youth Under 18', entryDateTime: 'Sun 9:00 AM', eventFee: '$32', reason: 'Event is full'},
			                    		{eventName:'Youth Under 14', entryDateTime: 'Sun 9:00 AM', eventFee: '$32', reason: 'Age restriction'},
			                    		{eventName:'U2100', entryDateTime: 'Sun 9:00 AM', eventFee: '$32', reason: 'Time conflict'},
			                    		{eventName:'U2000', entryDateTime: 'Sat 3:00 PM', eventFee: '$28', reason: 'Rating too high'},
			                    		{eventName:'Women Singles', entryDateTime: 'Sat 5:00 PM', eventFee: '$28', reason: 'Gender'}
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
		
		$scope.eventEntrySuccess = function(value, responseHeaders) {
			console.log ('event entry save success ' + value.id);
		}
		
		$scope.eventEntryFailure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to save event entry');
		}
		
		$scope.enteringEventId = -1;
		
		$scope.tournamentEntrySuccess = function(value, responseHeaders) {
			console.log ('tournament entry save success');
			$scope.tournamentEntry = value;
			$scope.enterEventInternal ();
		}
		
		$scope.tournamentEntryFailure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to save tournament entry');
		}

		$scope.enterEventInternal = function (tournamentEntryId) {
			var event = null;
			for (var i = 0; i < $scope.events.length; i++) {
				if ($scope.events[i].id == $scope.enteringEventId) {
					event = $scope.events[i];
					break;
				}
			}
			if (event != null) {
				var eventEntry = {
						status: 'PENDING',
						dateEntered: new Date(),
						event: event,
						eventId: event.id  // for $resource
				};
				eventEntryResource.create (eventEntry, $scope.eventEntrySuccess, $scope.eventEntryFailure);
			}
		}

		$scope.enterEvent = function (entry, browserEvent) {
			browserEvent.preventDefault();
			
			console.log ('entering event ' + entry.eventName);
			$scope.enteringEventId = -1;
			for (var i = 0; i < $scope.events.length; i++) {
				if ($scope.events[i].name == entry.eventName) {
					$scope.enteringEventId = $scope.events[i].id;
					break;
				}
			}
			
			if ($scope.enteringEventId  != -1) {
				// if no tournament entry create it first
				if ($scope.tournamentEntry.id == undefined) {
					// need this for resource parameter mapping not for GORM persistence
					$scope.tournamentEntry.tournamentId = $scope.tournament.id;
					tournamentEntryResource.save ($scope.tournamentEntry, $scope.tournamentEntrySuccess, $scope.tournamentEntryFailure);
				} else {
					$scope.enterEventInternal ();
				}
			}
		}
		
		$scope.withdrawFromEvent = function (entry, browserEvent) {
			console.log ('withdrawing from event ' + entry.eventName);
			for (var i = 0; i < $scope.enteredEventsList.length; i++) {
				if ($scope.enteredEventsList[i].eventName == entry.eventName) {
					var entries = $scope.enteredEventsList.splice(i, 1);
					$scope.availableEventsList.push (entries[0]);
				}
			}
		}

		
		// -------------------------------------------------------------------------------------------------------------------
		// membership options
		// -------------------------------------------------------------------------------------------------------------------

		$scope.membershipOptions = [
		                          {membershipName: 'Adult 1-year (G)', fee: 75, availableToMembers: 1, availableToAdults: 1, membershipType: 1},
		                          {membershipName: 'Adult 3-year (G)', fee: 210, availableToMembers: 1, availableToAdults: 1, membershipType: 2},
		                          {membershipName: 'Adult 5-year (G)', fee: 325, availableToMembers: 1, availableToAdults: 1, membershipType: 3},
		                          {membershipName: 'Junior 1-year (G)', fee: 45, availableToMembers: 1, availableToAdults: 0, membershipType: 4},
		                          {membershipName: 'Junior 3-year (G)', fee: 125, availableToMembers: 1, availableToAdults: 0, membershipType: 5},
		                          {membershipName: 'Collegiate 1-Year (G)', fee: 45, availableToMembers: 1, availableToAdults: 1, membershipType: 6},
		                          {membershipName: 'Household 1-Year (G)', fee: 150, availableToMembers: 1, availableToAdults: 1, membershipType: 7},
		                          {membershipName: 'Lifetime (G)', fee: 1300, availableToMembers: 1, availableToAdults: 1, membershipType: 8},
//		                          {membershipName: 'Contributor (G)', fee: 45, availableToMembers: 1, availableToAdults: 1, membershipType: 9},
		                          {membershipName: 'Tournament Pass (per tournament) (A)', fee: 20, availableToMembers: 0, availableToAdults: 1, membershipType: 10},
		                          ];

		$scope.selectedMembershipOption = $scope.membershipOptions[0];		// membership option selected by the user or defaulted to

		//
		// figure out if the current user is an adult
		//
		$scope.isAdultUser = function (){
			var birthdayDate = new Date($scope.userProfile.dateOfBirth);
			var years = tournamentDate.getFullYear() - birthdayDate.getFullYear();

			// reset birthday to the current year.
			birthdayDate.setFullYear(tournamentDate.getFullYear());

			// if the user's birthday has not occurred yet this year, subtract 1.
			if (tournamentDate < birthdayDate)
			    years--;
			
			return (years >= 18);		// used to determine whether to display junior membership options or not
		}

		//
		// store the selected membership option
		//
		$scope.selectMembership = function (option) {
			console.log ('selecting membership option ' + option.membershipName);
			$scope.selectedMembershipOption = option;
		}
	} 
	])
})();
