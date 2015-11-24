(function() {
	'use strict';

	// configure ui.router routes
	angular.module('tournament', [ 'ui.router', 'ngResource', 'date-range' ])

	.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.tournamentView', {
					url : 'api/tournaments/:id',
					privateUrl : false,
					views : {
						'content@' : {
							templateUrl : 'assets/partials/tournament/tournament-view.html',
							resolve: {
								session: 'session',

								tournamentResource: 'tournamentResource',
								tournament: function(tournamentResource, $stateParams, session) {
									return tournamentResource.view({id: $stateParams.id}).$promise;
								},
							    
								eventResource: 'eventResource',
								events: function (eventResource, $stateParams, session, tournament) {
									var queryOptions = {tournamentId: tournament.id, offset: 0, max: 0, username: session.getUser()};
									var events = eventResource.query(queryOptions).$promise;
									events = events || [];
									return events;
								}
							},
							controller : 'tournamentController'
						}
					}
				})
				.state('home.tournamentEdit', {
					url : 'api/tournament/edit/:id',
					privateUrl : true,
					data: {
						roles: ['ROLE_TOURNAMENT_DIRECTOR', 'ROLE_ADMIN']
				    },
					views : {
						'content@' : {
							templateUrl : 'assets/partials/tournament/tournament-edit.html',
							resolve: {
								session: 'session',

								tournamentResource: 'tournamentResource',
								tournament: function(tournamentResource, $stateParams, session) {
									return tournamentResource.view({id: $stateParams.id}).$promise;
								},
				    
								eventResource: 'eventResource',
								events: function (eventResource, $stateParams, session, tournament) {
									var queryOptions = {tournamentId: tournament.id, offset: 0, max: 30, username: session.getUser()};
									console.log ('tournament = ' + queryOptions.tournamentId + " username "+ queryOptions.username);
									var events = eventResource.query(queryOptions).$promise;
									events = events || [];
									return events;
								}
							},
							controller : 'tournamentController'
						}
					}
				})
				.state('home.tournamentCreate', {
					url : 'api/tournament/create',
					privateUrl : true,
					data: {
						roles: ['ROLE_TOURNAMENT_DIRECTOR', 'ROLE_ADMIN']
				    },
					views : {
						'content@' : {
							templateUrl : 'assets/partials/tournament/tournament-edit.html',
							resolve: {
								session: 'session',

								tournamentResource: 'tournamentResource',
								tournament: function(tournamentResource, $stateParams, session) {
									// get current user so ownership can be assigned
									return tournamentResource.create({}).$promise;
								},
							    
								eventResource: 'eventResource',
								events: function (eventResource, $stateParams, session) {
									// new tournament has no events
									return [];
								}
							},
							controller : 'tournamentController'
						}
					}
				})
			} ])
			
	// define controller functions
	.controller('tournamentController', 
			['$scope', '$state', 'session','tournamentResource', 'tournament','eventResource', 'events', '$mdDialog',
    function($scope, $state, session, tournamentResource, tournament, eventResource, events, $mdDialog) {
		// tournament data
		$scope.tournament = tournament;

//		console.log ('got tournament ' + tournament.id + " name: " + tournament.name);
		$scope.tournament.startDate = (tournament.startDate) ? new Date(tournament.startDate) : new Date();
		$scope.startDateText = moment(tournament.startDate).format('LL');
		$scope.startDateTextSaved = $scope.startDateText;

		$scope.tournament.endDate = (tournament.endDate) ? new Date (tournament.endDate) : new Date();
		$scope.endDateText = moment(tournament.endDate).format('LL');
		$scope.endDateTextSaved = $scope.endDateText;

		$scope.tournament.contactName = 'Swavek Lorenc';
		
        // sates for venue location
		$scope.statesArray = [
                  			  'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA', 
                  			  'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
                			  'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
                			  'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
                			  'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY'
                        ];

		
		$scope.enterTournament = function (tournamentId, event) {
			console.log ('entering tournament ' + tournamentId);
//			var params = {id: tournamentId};
//			$state.go('home.tournamentView', params);
		}

		$scope.successSave = function(value, responseHeaders) {
			$state.go('home.tournamentManageList');
		}
		
		//
		// Save tournament
		//
		$scope.save = function () {
			// start date
			if ($scope.startDateText != $scope.startDateTextSaved) {
				$scope.tournament.startDate = moment($scope.startDateText, "MMMM-D-YYYY").toDate();
			} else {
				$scope.startDateText = moment($scope.tournament.startDate).format('LL');
			} 
			$scope.startDateTextSaved = $scope.startDateText;
			
			// end date
			if ($scope.endDateText != $scope.endDateTextSaved) {
				$scope.tournament.endDate = moment($scope.endDateText, "MMMM-D-YYYY").toDate();
			} else {
				$scope.endDateText = moment($scope.tournament.endDate).format('LL');
			} 
			$scope.endDateTextSaved = $scope.endDateText;
			
			if ($scope.tournament.id == null) {
				tournamentResource.save ($scope.tournament, $scope.successSave)
			} else {
				tournamentResource.update ($scope.tournament, $scope.successSave);
			}
		}
		
		// ============================================================================================================================
		// EVENT related functions
		// ============================================================================================================================
		// save original events
		$scope.originalEvents = events;
		
		// currently edited event
		$scope.editedEvent = null; 

		
		//        
		// format the event start date and time (for the list of events)
		//
		$scope.formatEventDateTime = function (eventDay, startTime, tournamentStartDate) {
			var mDate = moment([tournamentStartDate.getFullYear(), tournamentStartDate.getMonth(), tournamentStartDate.getDate()]);
			if (eventDay > 1) {
				mDate = mDate.add(eventDay - 1, 'days');
			}
			var hours = 0;
			var minutes = 0;
			// test if they chose 30 minutes past hour start time
			if (startTime == Math.floor(startTime)) {
				hours = startTime;
				minutes = 0;
			} else {
				// he chose 8:30 or 9:30 etc.
				hours = Math.floor(startTime);
				minutes = 30;
			}
//			console.log ('just date' + mDate.format());
//			console.log ('hours ' + hours + " minutes " + minutes);
			mDate.zone (0);
			mDate.utc();
			mDate.hours(hours);
			mDate.minutes(minutes);
			mDate.seconds(0);
			return mDate.format('ddd h:mm A');  // Sun 1:30 PM
		}
		
		//
		// Converts event into editable eventInfo
		//
		$scope.makeEventInfo = function (event) {
//			console.log ('event.startDateTime = ' + event.startDateTime);
			var eventDateTime = $scope.formatEventDateTime (event.day, event.startTime, $scope.tournament.startDate);
//			console.log ('eventDateTime = ' + eventDateTime);
			var eventInfo = {
					id: event.id,
					ordinalNumber: event.ordinalNumber,
					name: event.name,
					eventDateTime: eventDateTime,
					eventDate: event.day, //eventDate, day 1, 2 or 3
					eventTime: event.startTime, // eventTime,  9.0, 10.5 etc.
					singleElimination: event.singleElimination,
					doubles: event.doubles,
					maxEntries: event.maxEntries,
					players: 1, 
					maxPlayers: 32
					};
			return eventInfo;
		}
		
		//
		// nested events only contain id field and class so use these eventInfos for listing/editing
		//
		$scope.eventInfoList = []; 
		for (var i = 0; i < $scope.originalEvents.length; i++) {
			var event = $scope.originalEvents[i];
			var eventInfo = $scope.makeEventInfo (event);
			// date and time are encoded together in one date object
			$scope.eventInfoList.push(eventInfo);
		}

		//
		// event start dates array
		//
		$scope.createStartDatesArray = function (startDate, endDate) {
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
		var FIRST_START_TIME = 7;
		var LAST_START_TIME = 23;
		$scope.createStartTimesArray = function () {
			var startTimes = [];
			for (var hour = FIRST_START_TIME; hour < LAST_START_TIME; hour++) {
				var timeText = moment().hour(hour).minutes(0).seconds(0).format('LT');
				startTimes.push ({fractionalHour: hour * 1.0, timeText: timeText});
				timeText = moment().hour(hour).minutes(30).seconds(0).format('LT');
				startTimes.push ({fractionalHour: hour + 0.5, timeText: timeText});
			}
			
			
			return startTimes;
		}

		$scope.eventDatesArray = $scope.createStartDatesArray($scope.tournament.startDate, $scope.tournament.endDate);
		
		// create start times array and their text equivalents
		$scope.eventTimesArray = $scope.createStartTimesArray();
		
		//
		// called when successfully saved event
		//
		$scope.successEventSave = function (value, responseHeaders) {
			console.log ('saved event successfully');
			// set event id now that it is known
			var newEventCreated = false;
			var savedEvent = value;
			for (var i = 0; i < $scope.originalEvents.length; i++) {
				var event = $scope.originalEvents[i];
				if (event.ordinalNumber == savedEvent.ordinalNumber &&
					event.name == savedEvent.name) {
					newEventCreated = (event.id == null);
					event.id = savedEvent.id;
					break;
				}
			}
			
			// now update the info list so the list is updated via data binding
			if (newEventCreated) {
				var eventInfo = $scope.makeEventInfo (savedEvent);
				$scope.eventInfoList.push(eventInfo);
			}
		}
		
		$scope.saveEvent = function (browserEvent) {
			browserEvent.preventDefault();
//			console.log ('saving current event');
			// save current event date & time
			if ($scope.editedEvent != null) {
				// find original event
				var saveToEvent = null;
				for (var i = 0; i < $scope.originalEvents.length; i++) {
					var event = $scope.originalEvents[i];
					if (event.id == $scope.editedEvent.id) {
						saveToEvent = event;
						break;
					}
				}
				if (saveToEvent == null) {
					saveToEvent = {};
					$scope.originalEvents.push (saveToEvent);
				}

				// transfer from copy to original
				saveToEvent.name = $scope.editedEvent.name;
				saveToEvent.ordinalNumber = $scope.editedEvent.ordinalNumber;
				saveToEvent.day = $scope.editedEvent.eventDate;
				saveToEvent.startTime = $scope.editedEvent.eventTime;
				saveToEvent.tournamentId = $scope.tournament.id;
				saveToEvent.singleElimination = $scope.editedEvent.singleElimination;
				saveToEvent.doubles = $scope.editedEvent.doubles;
				saveToEvent.maxEntries = $scope.editedEvent.maxEntries;
				if (saveToEvent.id == null) {
					eventResource.save (saveToEvent, $scope.successEventSave)
				} else {
					eventResource.update (saveToEvent, $scope.successEventSave);
				}
				
				// update date & time used in the list
				$scope.editedEvent.eventDateTime = $scope.formatEventDateTime(saveToEvent.day, saveToEvent.startTime, $scope.tournament.startDate)
//				console.log ('$scope.editedEvent.eventDateTime = ' + $scope.editedEvent.eventDateTime);
				
			}
		}
		
		//
		// show details of event to be edited
		//
		$scope.editEvent = function (event, browserEvent) {
			// now set the new event as current
			$scope.editedEvent = event;
			browserEvent.preventDefault();
		
		}
		
		//
		// Callback after adding new event
		//
		$scope.successEventCreate = function (value, responseHeaders) {
			var newEvent = value;
			console.log ('success done creating newEvent in backend');
			// now create event info with sensible defaults
			var nextOrdinalNumber = events.length + 1;
			do {
				var isUsed = false;
				for (var i = 0; i < $scope.eventInfoList.length; i++) {
					if ($scope.eventInfoList[i].ordinalNumber == nextOrdinalNumber) {
						nextOrdinalNumber++;
						isUsed = true;
						break;
					}
				}			
			} while (isUsed);
			newEvent.ordinalNumber = nextOrdinalNumber;
			
			// set some sensible start time
			newEvent.day = 1;
			newEvent.startTime = 9.0;
			newEvent.tournament = $scope.tournament;
			newEvent.doubles = false;
			newEvent.singleElimniation = false;
			newEvent.maxEntries = 0;
			
			// save in original list
			$scope.originalEvents.push (newEvent);
			
			// convert to eventInfor for editing	
			var eventInfo = $scope.makeEventInfo (newEvent);
			
			// make it show up on the right
			$scope.editedEvent = eventInfo;
			
		}
		
		//
		// Add new event
		//
		$scope.addEvent = function (browserEvent) {
			browserEvent.preventDefault();
			// create a new event in the backend with all fields and save in
			var queryOptions = {tournamentId: $scope.tournament.id};
			eventResource.create(queryOptions, $scope.successEventCreate);
		}
		
		//
		// Delete event functionality
		//
		$scope.deletedEventName = null;
		$scope.successEventDelete = function (value, responseHeaders) {
			
			for (var i = 0; i < $scope.eventInfoList.length; i++) {
				if ($scope.eventInfoList[i].name == $scope.deletedEventName) {
					$scope.eventInfoList.splice(i, 1);
					break;
				}
			}
			
			for (var i = 0; i < $scope.originalEvents.length; i++) {
				if ($scope.originalEvents[i].name == $scope.deletedEventName) {
					$scope.originalEvents.splice(i, 1);
					break;
				}
			}
		}

		//
		// deletes event after confirmation
		//
		$scope.deleteEvent = function(event, browserEvent) {

			browserEvent.preventDefault();
			$scope.deletedEventName = null;
			// Appending dialog to document.body to
			// cover sidenav in docs app
			var confirm = $mdDialog
				.confirm()
				.title('Delete Event')
				.content('Delete event ' + event.name + "?")
				.ariaLabel('Delete Event')
				.targetEvent(browserEvent)
				.ok('OK')
				.cancel('Cancel');
		
		$mdDialog.show(confirm)
				.then(function() {
					// if not a new event
					if (event.id != null) {
						var queryOptions = {tournamentId: $scope.tournament.id, id: event.id};
						eventResource.delete(queryOptions, $scope.successEventDelete);
					}
					// save since we don't get it back on successul completion
					$scope.deletedEventName = event.name;
					// clear right hand panel
					$scope.editedEvent = null;
				},
				function() {
					// do nothing on cancel
				});
		};
		
		$scope.clickIcon = [];
		for (var i = 0; i < $scope.eventInfoList.length; i++) {
			$scope.clickIcon.push('expand_more');
		}
		
        $scope.clickIconMorph = function(eventIndex) {
            if ($scope.clickIcon[eventIndex] == 'expand_less')
                $scope.clickIcon[eventIndex] = 'expand_more';
            else
                $scope.clickIcon[eventIndex] = 'expand_less';
        }
        
        $scope.getIcon = function (eventIndex) {
        	return $scope.clickIcon[eventIndex];
        }
        
	} 
	])
})();
   