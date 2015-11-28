/**
 * Controller for editing event properties 
 */
(function() {
	'use strict';

	// configure ui.router routes
	angular.module('event', [ 'ui.router', 'ngResource'])

	.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.event', {
					// abstract state
					url : 'api/tournament/:tournamentId',
					privateUrl : true,
					data: {
						roles: ['ROLE_TOURNAMENT_DIRECTOR', 'ROLE_ADMIN']
				    },
					resolve: {
						session: 'session',

						tournamentResource: 'tournamentResource',
						tournament: function(tournamentResource, $stateParams, session) {
							console.log ('in home.event abstract state - getting tournament with id ' + $stateParams.tournamentId);
							return tournamentResource.view({id: $stateParams.tournamentId}).$promise;
						},
					}
				})
				.state ('home.event.create', {
					// create new event state
					url : '/event/create?ordinalNumber&eventName',
					resolve: {
						eventResource: 'eventResource',
						event: function (eventResource, $stateParams, session) {
							var queryOptions = {tournamentId: $stateParams.tournamentId, username: session.getUser()};
							console.log ('Creating event for tournament = ' + queryOptions.tournamentId + ", username "+ queryOptions.username);
							return eventResource.create(queryOptions).$promise;
						}
					},
					views : {
						'content@' : {
							templateUrl : 'assets/partials/event/event-edit.html',
							controller : 'eventController'
						},
						
						'general-tab@home.event.create' : {
							templateUrl: 'assets/partials/event/event-edit-general-tab.html'
						},
						
						'options-tab@home.event.create' : {
							templateUrl: 'assets/partials/event/event-edit-options-tab.html'
						},
						
						'playoff-tab@home.event.create' : {
							templateUrl: 'assets/partials/event/event-edit-playoff-tab.html'
						},
						
						'prizes-tab@home.event.create' : {
							templateUrl: 'assets/partials/event/event-edit-prizes-tab.html'
						}
					}					
				})
				.state ('home.event.edit', {
					// edit existing event state
					url : '/event/:id/',
					resolve: {
						eventResource: 'eventResource',
						event: function (eventResource, $stateParams, session) {
							var queryOptions = {tournamentId: $stateParams.tournamentId, id: $stateParams.id, username: session.getUser()};
							console.log ('Getting event for tournament = ' + queryOptions.tournamentId + ", eventid " + queryOptions.id + ", username "+ queryOptions.username);
							return eventResource.view(queryOptions).$promise;
						}
					},
					views : {
						'content@' : {
							templateUrl : 'assets/partials/event/event-edit.html',
							controller : 'eventController'
						},
						
						'general-tab@home.event.edit' : {
							templateUrl: 'assets/partials/event/event-edit-general-tab.html'
						},
						
						'options-tab@home.event.edit' : {
							templateUrl: 'assets/partials/event/event-edit-options-tab.html'
						},
						
						'playoff-tab@home.event.edit' : {
							templateUrl: 'assets/partials/event/event-edit-playoff-tab.html'
						},
						
						'prizes-tab@home.event.edit' : {
							templateUrl: 'assets/partials/event/event-edit-prizes-tab.html'
						}
					}					
				})
			} ])
			
	// define controller functions
	.controller('eventController', 
			['$scope', '$state', 'session','$mdDialog', 'tournamentResource', 'tournament', 'eventResource', 'event', 
    function($scope, $state, session, $mdDialog, tournamentResource, tournament, eventResource, event) {
		// tournament data (start and end date)
		$scope.tournament = tournament;

		// currently edited event
		$scope.editedEvent = event;
		if ($state.current.name == 'home.event.create') {
			$scope.editedEvent.ordinalNumber = $state.params.ordinalNumber;
			$scope.editedEvent.name = ($state.params.eventName != 'Other') ? $state.params.eventName : '';
			// initialize min & max player rating
			for (var i = 0; i < eventDefaults.length; i++) {
				var eventDefault = eventDefaults[i];
				if (eventDefault.name == $state.params.eventName) {
					$scope.editedEvent.minPlayerRating = eventDefault.minPlayerRating;
					$scope.editedEvent.maxPlayerRating = eventDefault.maxPlayerRating;
					$scope.editedEvent.doubles = eventDefault.doubles;
					break;
				}
			}
			// initialize some sensible defaults
			$scope.editedEvent.day = 1;
			$scope.editedEvent.startTime = 9.0;
			$scope.editedEvent.tournament = $scope.tournament;
			$scope.editedEvent.singleElimniation = false;
			$scope.editedEvent.maxEntries = 0;
		}
		
		// create dates array for event day drop down
		$scope.tournament.startDate = (tournament.startDate) ? new Date(tournament.startDate) : new Date();
		$scope.tournament.endDate = (tournament.endDate) ? new Date (tournament.endDate) : new Date();
		$scope.eventDatesArray = createStartDatesArray($scope.tournament.startDate, $scope.tournament.endDate);
		
		// create start times array and their text equivalents
		$scope.eventTimesArray = createStartTimesArray();
		
		//
		// event start dates array
		//
		function createStartDatesArray (startDate, endDate) {
			var tempStartDate = moment([startDate.getFullYear(), startDate.getMonth(), startDate.getDate()]);
			var tempEndDate = moment([endDate.getFullYear(), endDate.getMonth(), endDate.getDate()]);
			// add one day so we loop twice for two day tournament, 3 for 3 day  etc.
			tempEndDate = tempEndDate.add(1, 'days');
			var datesArray = [];
			var day = 1;
			do {
				var dateText = tempStartDate.format('LL');
				datesArray.push ({day: day, dayText: dateText});
				tempStartDate = tempStartDate.add(1, 'days');
				day++;
			} while (tempStartDate.isBefore(tempEndDate))
			return datesArray;
		}
		
		//
		// event start times array
		//
		function createStartTimesArray () {
			var FIRST_START_TIME = 7;
			var LAST_START_TIME = 23;
			var startTimes = [];
			for (var hour = FIRST_START_TIME; hour < LAST_START_TIME; hour++) {
				var timeText = moment().hour(hour).minutes(0).seconds(0).format('LT');
				startTimes.push ({fractionalHour: hour * 1.0, timeText: timeText});
				timeText = moment().hour(hour).minutes(30).seconds(0).format('LT');
				startTimes.push ({fractionalHour: hour + 0.5, timeText: timeText});
			}
			return startTimes;
		}
		
		//
		// called when successfully saved event
		//
		$scope.successEventSave = function (value, responseHeaders) {
			$state.go ('home.tournamentEdit', {id: $scope.tournament.id, selectedTab: 1});
		}
		
		//
		// called when save fails
		//
		$scope.errorEventSave = function (httpResponse) {
			var alert = $mdDialog
			.alert()
			.title('Failed to save event')
			.textContent('Error code: ' + httpResponse.status + ", Message " + httpResponse.statusText)
			.ariaLabel('Save Error')
			.ok('Close');

	        $mdDialog
	          .show( alert )
	          .finally(function() {
	            alert = undefined;
	          });
		}
		
		//
		// called when save is clicked
		//
		$scope.saveEvent = function (browserEvent) {
			browserEvent.preventDefault();

			// save current event date & time
			if ($scope.editedEvent != null) {
				$scope.editedEvent.tournamentId = $scope.tournament.id;
				if ($scope.editedEvent.id == null) {
					eventResource.save ($scope.editedEvent, $scope.successEventSave, $scope.errorEventSave)
				} else {
					eventResource.update ($scope.editedEvent, $scope.successEventSave, $scope.errorEventSave);
				}
			}
		}

		//
		// called when Cancel is clicked
		//
		$scope.cancelEvent = function (browserEvent) {
			browserEvent.preventDefault();
			$state.go ('home.tournamentEdit', {id: $scope.tournament.id, selectedTab: 1});
		}
		
	} 
	])
})();
   