(function() {
	'use strict';

	// configure ui.router routes
	angular.module('tournament', [ 'ui.router', 'ngResource', 'date-range'])

	.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.tournamentView', {
					url : 'api/tournaments/:id',
					data: {
						privateUrl : false
					},
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
									var queryOptions = {tournamentId: tournament.id, offset: 0, max: 50, username: session.getUser()};
									return eventResource.query(queryOptions).$promise;
								},
								
								accountResource: 'accountResource',
								accounts: function (accountResource, $stateParams, session, tournament) {
									var queryOptions = {tournamentId: tournament.id, offset: 0, max: 50};
									return accountResource.query(queryOptions).$promise;
								}
							},
							controller : 'tournamentController'
						}
					}
				})
				.state('home.tournamentEdit', {
					url : 'api/tournament/edit/:id?selectedTab',
					data: {
						privateUrl : true,
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
									var queryOptions = {tournamentId: tournament.id, offset: 0, max: 50, username: session.getUser()};
									//console.log ('tournament = ' + queryOptions.tournamentId + " username "+ queryOptions.username);
									return eventResource.query(queryOptions).$promise;
								},
								
								accountResource: 'accountResource',
								accounts: function (accountResource, $stateParams, session, tournament) {
									var queryOptions = {tournamentId: tournament.id, offset: 0, max: 50};
									return accountResource.query(queryOptions).$promise;
								}
							},
							controller : 'tournamentController', 
						},
						
						'general-tab@home.tournamentEdit' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-general-tab.html'
						},
						
						'events-tab@home.tournamentEdit' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-events-tab.html'
						},
						
						'contact-tab@home.tournamentEdit' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-contact-tab.html'
						},
						
						'payment-info-tab@home.tournamentEdit' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-payment-info-tab.html'
						}
					}
				})
				.state('home.tournamentCreate', {
					url : 'api/tournament/create',
					data: {
						privateUrl : true,
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
								},
								
								accountResource: 'accountResource',
								accounts: function (accountResource, $stateParams, session) {
									return accountResource.create({}).$promise;
								}
							},
							controller : 'tournamentController'
						},
						
						'general-tab@home.tournamentCreate' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-general-tab.html'
						},
						
						'events-tab@home.tournamentCreate' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-events-tab.html'
						},
						
						'contact-tab@home.tournamentCreate' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-contact-tab.html'
						},
						
						'payment-info-tab@home.tournamentCreate' : {
							templateUrl: 'assets/partials/tournament/tournament-edit-payment-info-tab.html'
						}
					}
				})
			} ])
			
	// define controller functions
	.controller('tournamentController', 
			['$scope', '$state', 'session','$mdDialog', 'tournamentResource', 'tournament', 'eventResource', 'events', 'accountResource', 'accounts',
    function($scope, $state, session, $mdDialog, tournamentResource, tournament, eventResource, events, accountResource, accounts) {
		
		// when coming back from editing events switch back to the 'Events' tab
		$scope.selectedTab = ($state.params.selectedTab == undefined) ? 0 : ($state.params.selectedTab);
		
		// tournament data
		$scope.tournament = tournament;

//		console.log ('got tournament ' + tournament.id + " name: " + tournament.name);
		// make new tournaments start 3 months from now
		var defaultStartDate = moment().add(90, 'days').toDate();
		
		$scope.tournament.startDate = (tournament.startDate) ? new Date(tournament.startDate) : defaultStartDate;
		$scope.startDateText = moment(tournament.startDate).format('LL');
		$scope.startDateTextSaved = $scope.startDateText;

		$scope.tournament.endDate = (tournament.endDate) ? new Date (tournament.endDate) : $scope.tournament.startDate;
		$scope.endDateText = moment(tournament.endDate).format('LL');
		$scope.endDateTextSaved = $scope.endDateText;

		var defaultRatingCutoffDate = moment([defaultStartDate.getFullYear(), defaultStartDate.getMonth(), defaultStartDate.getDate()]).subtract(30, 'days').toDate();
		var defaultLateEntryStartDate = moment([defaultStartDate.getFullYear(), defaultStartDate.getMonth(), defaultStartDate.getDate()]).subtract(14, 'days').toDate();
		var defaultEntryCutoffDate = moment([defaultStartDate.getFullYear(), defaultStartDate.getMonth(), defaultStartDate.getDate()]).subtract(7, 'days').toDate();

		$scope.tournament.ratingCutoffDate = (tournament.ratingCutoffDate) ? new Date (tournament.ratingCutoffDate) : defaultRatingCutoffDate;
		$scope.tournament.lateEntryStartDate = (tournament.lateEntryStartDate) ? new Date (tournament.lateEntryStartDate) : defaultLateEntryStartDate;
		$scope.tournament.entryCutoffDate = (tournament.entryCutoffDate) ? new Date (tournament.entryCutoffDate) : defaultEntryCutoffDate;

        // states for venue location
		$scope.statesArray = [
                  			  'AL', 'AK', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'FL', 'GA', 
                  			  'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MD',
                			  'MA', 'MI', 'MN', 'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ',
                			  'NM', 'NY', 'NC', 'ND', 'OH', 'OK', 'OR', 'PA', 'RI', 'SC',
                			  'SD', 'TN', 'TX', 'UT', 'VT', 'VA', 'WA', 'WV', 'WI', 'WY'
                        ];
		
		$scope.enterTournament = function (tournamentId, browserEvent) {
			console.log ('entering tournament ' + tournamentId);
			var params = {tournamentId: tournamentId};
			$state.go('home.tournamentEntry.events', params);
		}
		
		$scope.successSave = function(value, responseHeaders) {
			$state.go('home.tournamentManageList');
		}
		
		$scope.errorSave = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to Save Tournament');
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
				tournamentResource.save ($scope.tournament, $scope.successSave, $scope.errorSave);
			} else {
				tournamentResource.update ($scope.tournament, $scope.successSave, $scope.errorSave);
			}
		}
		

		
		// ============================================================================================================================
		// EVENT related functions
		// ============================================================================================================================
		// save original events
		$scope.originalEvents = events;
		
		//
		// Converts event into editable eventInfo
		//
		$scope.makeEventInfo = function (event) {
//			console.log ('event.startDateTime = ' + event.startDateTime);
			var eventDateTime = formatEventDateTime (event.day, event.startTime, $scope.tournament.startDate);
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
					players: (event.confirmedEntriesCount != undefined) ? event.confirmedEntriesCount : 0 
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
		
		// calculate total number of players
		$scope.totalEventEntries = 0;
		for (var i = 0; i < $scope.eventInfoList.length; i++) {
			$scope.totalEventEntries += $scope.eventInfoList[i].players;
		}
		
		// calculate total number of available event spots for the tournament
		$scope.totalAvailableEventSpots = 0;
		for (var i = 0; i < $scope.eventInfoList.length; i++) {
			$scope.totalAvailableEventSpots += $scope.eventInfoList[i].maxPlayers;
		}

		// indicates if the number of sign-up players is equal to number of available tournament spots
		$scope.maxPlayersReached = ($scope.totalEventEntries == $scope.totalAvailableEventSpots);

		//
		// is the tournament closed for registration (either the number of entries reached max or sign-up cutoff date is in the past)
		//
		$scope.tournamentRegistrationClosed = function () {
			var tournamentCutOffDate = ($scope.tournament.entryCutoffDate != null ? $scope.tournament.entryCutoffDate : new Date());
			var today = new Date();
			var isCuttOffDateReached = moment(tournamentCutOffDate).isBefore(today, 'day');
			var isMaxNumberOfEntriesReached = $scope.maxPlayersReached;
			
			return (isMaxNumberOfEntriesReached || isCuttOffDateReached);
		}
		
		//
		// show details of event to be edited
		//
		$scope.editEvent = function (event, browserEvent) {
			// now set the new event as current
			browserEvent.preventDefault();
		
			var params = {tournamentId: $scope.tournament.id, id: event.id};
			$state.go ('home.event.edit', params);
		}
		
		//
		// Add new event
		//
		$scope.addEvent = function (browserEvent) {
			browserEvent.preventDefault();
			var tournamentId = $scope.tournament.id;
			
			// controller for dialog prompt
			function DialogController($scope, $mdDialog, eventDefaultsLocal) {
				// create array of arrays - each array represents one row in the popup
				$scope.eventDefaults = [];
				for (var i = 0; i < eventDefaultsLocal.length; i++ ) {
					if (i % 5 == 0) 
						$scope.eventDefaults.push([]);
					$scope.eventDefaults[$scope.eventDefaults.length-1].push(eventDefaultsLocal[i]);
				}
				
				$scope.answer = function (eventName, $browserEvent) {
					$mdDialog.hide();
				
					// now transition to creating and editing this new event
					var nextOrdinalNumber = getNextOrdinalNumber();
					var params = {tournamentId: tournamentId, ordinalNumber: nextOrdinalNumber, eventName: eventName};
					$state.go ('home.event.create', params);
				}
				
				$scope.closeDialog = function() {
				      $mdDialog.hide();
				}
			}
		
			// prompt user for type of event so we can preset some sensible
			// defaults like max player rating doubles/singles etc.
			$mdDialog.show({
			  controller: DialogController,
			  templateUrl: 'assets/partials/tournament/addEventDialog.tmpl.html',
			  parent: angular.element(document.body),
			  clickOutsideToClose:true,
			  locals: {eventDefaultsLocal: eventDefaults}
			});
		}
		
		
		//
		// finds the next ordinal number for event
		//
		function getNextOrdinalNumber () {
			var nextOrdinalNumber = 0;
			// find the max ordinal number
			for (var i = 0; i < $scope.originalEvents.length; i++) {
				var event = $scope.originalEvents[i];
				if (event.ordinalNumber > nextOrdinalNumber) {
					nextOrdinalNumber = event.ordinalNumber;
				}
			}
			nextOrdinalNumber++;
			return nextOrdinalNumber;
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
				.textContent('Delete event ' + event.name + " ?")
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
		
		//=================================================================================================
		// event list table sorting and pagination
		//=================================================================================================
		  $scope.selected = [];
		  $scope.query = {
		    filter: '',
		    order: 'ordinaNumber',
		    limit: (screen.availHeight > 1000) ? 15 : 10,
		    page: 1
		  };

		 $scope.success = function (desserts) {
//		    $scope.desserts = desserts;
		  }

		  // in the future we may see a few built in alternate headers but in the mean time
		  // you can implement your own search header and do something like
		  $scope.search = function (predicate) {
//		    $scope.filter = predicate;
//		    $scope.deferred = $nutrition.desserts.get($scope.query, $scope.success).$promise;
		  };

		  $scope.onOrderChange = function (order) {
//		    return $nutrition.desserts.get($scope.query, $scope.success).$promise; 
		  };

		  $scope.onPaginationChange = function (page, limit) {
//		    return $nutrition.desserts.get($scope.query, $scope.success).$promise; 
		  };
		
		//=================================================================================================
		// expanding/collapsing chevron icon
		//=================================================================================================
		
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
        
        //===================================================================================================
        // Payment Info
        //===================================================================================================
        
// I don't know where it gets created. Are we creating accounts here or somewhere else?
// If here, then we need to check first if one exists and if not, create one
        
        // Gateway Types
		$scope.gatewayTypesArray = [ 'Stripe', 'PayPal' ];        
        
        // Default to some payment type
        if (accounts.length == 0)
        {
        	// No account exists so create one now
        	$scope.account = createAccount();
        }
        else
        {
        	// Select the first existing account by default
        	$scope.account = accounts[0];
        }

		// Switch to the selected account
		$scope.gatewayTypeSelected = function (gatewayType) {
			console.log ('Gateway Type ' + gatewayType + ' was Selected');
			$scope.account = getAccount(gatewayType);
		}

		// Create a new account
		function createAccount()
		{
			console.log ('Creating a new account');
			// Needs implementation
		}
		
		// Return the account with the gateway type
        function getAccount(gatewayType) {
        	for (var i = 0; i < accounts.length; i++)
        	{
        		if (accounts[i].gatewayType.name == gatewayType)
        			return accounts[i];
        	}
			console.log ('Payment Type ' + gatewayType + ' not found!');
        	return null;
        }

		$scope.testInProgress = false;
		
        // Run a transaction test (payment and refund) using the entered payment info for the selected gateway type
		$scope.testTransaction = function(browserEvent) {
			browserEvent.preventDefault();

			if ($scope.testInProgress == true)
				return;
			
			$scope.testInProgress = true;

			if ($scope.account.gatewayType.name == 'Stripe')
			{
				if ($scope.account.stripePublicKey == null || $scope.account.stripeSecretKey == null)
				{
					alert('Please make sure both public and secret keys are entered');
				}
				else
				{
					console.log ('Testing Payment with ' + $scope.account.gatewayType.name + ' gateway');
					
					console.log ('Testing Refund with ' + $scope.account.gatewayType.name + ' gateway');
				}
			}
			else if ($scope.account.gatewayType.name == 'PayPal')
			{
				console.log ('PayPal Testing is not implemented');
				alert('PayPal Testing is not implemented');
//				if ($scope.account.payPalKey == null)
//				{
//					alert('Please make sure both public and secret keys are entered');
//				}
//				else
//				{
//					console.log ('Testing Payment with ' + $scope.account.gatewayType.name + ' gateway');
//					
//					console.log ('Testing Refund with ' + $scope.account.gatewayType.name + ' gateway');
//				}
			}
			
			// Move this to a success handler
			$scope.testInProgress = false;
		}


		$scope.saveTournamentPaymentInfo = function () {
			
			// Implement the save function
			console.log ('Saving Payment Info');
		}
	} 
	])
})();
      
