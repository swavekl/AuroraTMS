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
						tournamentEntryList: function(tournamentEntryResource, $stateParams, session) {
							console.log ('getting entry list owned by user');
							var params = {tournamentId: $stateParams.tournamentId};
							return tournamentEntryResource.list (params).$promise;
							
						},
						tournamentEntry: function(tournamentEntryResource, $stateParams, session, tournamentEntryList, userProfile) {
							console.log ('getting entry or creating a new one');
							if (tournamentEntryList != null && tournamentEntryList.length == 0) {
								var params = {tournamentId: $stateParams.tournamentId, userProfileId: userProfile.id};
								return tournamentEntryResource.create (params).$promise;
							} else {
								var params = {tournamentId: $stateParams.tournamentId, id: tournamentEntryList[0].id};
								return tournamentEntryResource.get(params).$promise;
							}
						},
						
						// entered events
						eventEntryResource: 'eventEntryResource',
						eventEntries: function(eventEntryResource, tournamentEntry, $stateParams, session) {
							var tournamentEntryId = (tournamentEntry.id != null) ? tournamentEntry.id : 0;
							var params = {tournamentEntryId: tournamentEntryId, tournamentId: $stateParams.tournamentId};
							return eventEntryResource.list (params).$promise;
						},
						
						// charges and refunds
						financialTransactionResource: 'financialTransactionResource',
						financialTransactions: function (financialTransactionResource, tournamentEntry, $stateParams, session) {
							var tournamentEntryId = (tournamentEntry.id != null) ? tournamentEntry.id : 0;
							var params = {tournamentEntryId: tournamentEntryId};	
							return financialTransactionResource.list (params).$promise
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
		        // url will be /form/invoice
		        .state('home.tournamentEntry.invoice', {
		        	url: '/invoice',
		        	templateUrl : 'assets/partials/tournament/entry/entry-invoice.html',
		        })
		        .state('home.tournamentEntry.payment', {
		        	url: '/payment',
		        	templateUrl : 'assets/partials/tournament/entry/entry-payment.html',
		        })
		        .state('home.tournamentEntry.completed', {
		            url: '/confirmation',
					templateUrl : 'assets/partials/tournament/entry/entry-completed.html',
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
			 'financialTransactionResource', 'financialTransactions',
    function($scope, $state, session, $mdDialog,
    		tournamentResource, tournament, 
			eventResource, events, 
    		tournamentEntryResource, tournamentEntry, 
    		userProfileResource, userProfile, 
    		eventEntryResource, eventEntries,
    		financialTransactionResource, financialTransactions) {
		// console.log ('in tournamentEntryController state = ' +
		// $state.current.name);
		// tournament entry
		$scope.tournament = tournament;
		$scope.tournamentEntry = tournamentEntry;
		if ($scope.tournamentEntry.tournament == null) {
			$scope.tournamentEntry.tournament = tournament;
		}
		
		$scope.userProfile = userProfile;
		
		$scope.isNewMember = false;
		$scope.membershipExpired = false;
		$scope.membershipWillExpire = false;
		
		// check if new USATT member
		if ($scope.userProfile.usattID == 0 || $scope.userProfile.usattID > 900000) {
			$scope.isNewMember = true;
		} else {
			// check if expires before tournament
			var today = new Date();
			var tournamentDate = (tournament != null && tournament.endDate != null) ? new Date (tournament.endDate) : new Date();
			var membershipExpirationDate = ($scope.userProfile.expirationDate) ? new Date ($scope.userProfile.expirationDate) : new Date();
			var isExpirationDateBeforeTournamentDate = moment(membershipExpirationDate).isBefore(tournamentDate, 'day') || 
			   										   moment(membershipExpirationDate).isSame(tournamentDate, 'day'); 
			var isExpirationDateBeforeToday = moment(membershipExpirationDate).isBefore(today, 'day'); 

			$scope.userProfile.expirationDate = membershipExpirationDate;
			$scope.membershipExpired = isExpirationDateBeforeTournamentDate && isExpirationDateBeforeToday; 
			$scope.membershipWillExpire = isExpirationDateBeforeTournamentDate && !isExpirationDateBeforeToday; 
		}
		
		$scope.needToPayMembership = ($scope.membershipExpired || $scope.membershipWillExpire || $scope.isNewMember);
		
		var isTournamentDirector = session.isInRole ('TOURNAMENT_DIRECTOR');
		var isAdmin = session.isInRole ('ADMIN');
		var hasGroup = session.hasGroup ();
		if (isTournamentDirector || isAdmin) {
			// go to page where you find user to enter
		} else if (hasGroup) {
			// enter yourself or family/group member
		} else {
			// enter yourself only
		}

		// here is the list of steps. Membership may not be required if it is up
		// to date
		$scope.steps = []
		$scope.steps.push ('home.tournamentEntry.events');
// if ($scope.needToPayMembership)
			$scope.steps.push ('home.tournamentEntry.membership');
		$scope.steps.push ('home.tournamentEntry.invoice');
		$scope.steps.push ('home.tournamentEntry.payment');
// $scope.steps.push ('home.tournamentEntry.completed');
		
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
		// status of event entries
		var STATUS_NOT_SELECTED = 'NOT_SELECTED';
		var STATUS_PENDING = 'PENDING';
		var STATUS_CONFIRMED = 'CONFIRMED';
		
		$scope.events = events;
		$scope.eventEntryInfos = eventEntries;
		
		//
		// converts eventEntry to eventEntryInfo
		//
		$scope.makeEventInfo = function (eventEntry) {
			var eventEntryInfo = {};
			var tournamentStartDate = new Date($scope.tournament.startDate);
			for (var i = 0; i < $scope.events.length; i++) {
				var event = $scope.events[i];
				if (event.id == eventEntry.eventEntry.event.id) {
					eventEntryInfo.id = event.id;
					eventEntryInfo.eventEntry = eventEntry.eventEntry;
					eventEntryInfo.eventName = event.name;
					eventEntryInfo.entryDateTime = formatEventDateTime(event.day, event.startTime, tournamentStartDate);
					eventEntryInfo.fee = eventEntry.fee;
					var availabilityStatus = eventEntry.availabilityStatus;
					eventEntryInfo.availabilityStatus = availabilityStatus;
					if (availabilityStatus != 'ENTERED' && availabilityStatus != 'AVAILABLE') {
						if (availabilityStatus == 'RATING') {
							eventEntryInfo.reason = 'Rating is too high/low';
						} else if (availabilityStatus == 'WRONG_AGE') {
							eventEntryInfo.reason = 'Either too old/young';
						} else if (availabilityStatus == 'WRONG_GENDER') {
							eventEntryInfo.reason = 'Wrong gender';
						} else if (availabilityStatus == 'TIME_CONFLICT') {
							eventEntryInfo.reason = 'Time confict';
						} else if (availabilityStatus == 'FULL') {
							eventEntryInfo.reason = 'Event is full';
						} else if (availabilityStatus == 'WAITING_LIST') {
							eventEntryInfo.reason = 'You may enter waiting list';
						}
					}
					break;
				}
			}
			return eventEntryInfo;
		}

		//
		// divides them into 3 groups: entered, available and not available
		//
		$scope.divideEntries = function () {
			// create eventInfoObjects
			$scope.enteredEventsList = [];
			$scope.availableEventsList = [];
			$scope.unavailableEventsList = [];

			for (var i = 0; i < $scope.eventEntryInfos.length; i++) {
				var eventEntryInfo = $scope.eventEntryInfos[i];
				eventEntryInfo = $scope.makeEventInfo(eventEntryInfo);
				var availabilityStatus = eventEntryInfo.availabilityStatus;
// console.log ('availability status for ' + eventEntryInfo.eventName + " is " +
// availabilityStatus);
				if (availabilityStatus == 'ENTERED') {
					$scope.enteredEventsList.push (eventEntryInfo);
				} else if (availabilityStatus == 'AVAILABLE') {
					$scope.availableEventsList.push(eventEntryInfo);
				} else {
					$scope.unavailableEventsList.push(eventEntryInfo);
				}
			}
		}
		
		$scope.divideEntries();


		// callback for successful list return
		$scope.success = function (value, responseHeaders) {
			$scope.tournamentEntry = value;
		}
		
		$scope.editTournamentEntry = function (tournamentId, event) {
			console.log ('editTournamentEntry tournament ' + tournamentId);
// var params = {id: tournamentId};
// $state.go('home.tournamentEntry', params);
		}
		
		$scope.eventEntrySuccess = function(value, responseHeaders) {
			console.log ('event entry save success ' + value.id);
			$scope.refreshEventEntries();
		}
		
		$scope.eventEntryFailure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to save event entry');
		}
		
		$scope.refreshListEventEntriesSuccess = function(value, responseHeaders) {
			console.log ('refreshListEventEntriesSuccess # of entries = ' + value.length);
			$scope.eventEntryInfos = value;
			$scope.divideEntries();
			// update invoice
			$scope.updateSummary();
		}
		
		$scope.refreshListEventEntriesFailure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to refresh event entries');
		}
		
		$scope.refreshEventEntries = function () {
			console.log ('refershing event entries');
			var tournamentEntryId = (tournamentEntry.id != null) ? tournamentEntry.id : 0;
			var params = {tournamentEntryId: $scope.tournamentEntry.id, tournamentId: $scope.tournament.id};
			eventEntryResource.list (params, $scope.refreshListEventEntriesSuccess, $scope.refreshListEventEntriesFailure);
		}
		
		$scope.enteringEventId = -1;
		
		$scope.tournamentEntrySuccess = function(value, responseHeaders) {
			$scope.tournamentEntry = value;
			console.log ('tournament entry save success with id = ' + $scope.tournamentEntry.id);
			$scope.enterEventInternal ();
		}
		
		$scope.tournamentEntryFailure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to save tournament entry');
		}

		$scope.enterEventInternal = function () {
			var event = null;
			for (var i = 0; i < $scope.events.length; i++) {
				if ($scope.events[i].id == $scope.enteringEventId) {
					event = $scope.events[i];
					break;
				}
			}
			if (event != null) {
				var eventEntry = {
						status: STATUS_PENDING,
						dateEntered: new Date(),
						event: event,
						tournamentEntry: $scope.tournamentEntry,
						tournamentEntryId: $scope.tournamentEntry.id
				};
				console.log ('eventEntry.tournamentEntryId = ' + eventEntry.tournamentEntryId);
				eventEntryResource.save (eventEntry, $scope.eventEntrySuccess, $scope.eventEntryFailure);
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
					// need this for resource parameter mapping not for GORM
					// persistence
					$scope.tournamentEntry.tournamentId = $scope.tournament.id;
					tournamentEntryResource.save ($scope.tournamentEntry, $scope.tournamentEntrySuccess, $scope.tournamentEntryFailure);
				} else {
					$scope.enterEventInternal ();
				}
			}
		}
		
		//
		// Withdrawing from event
		//
		$scope.eventEntryDeleteSuccess = function(value, responseHeaders) {
			$scope.refreshEventEntries();
		}
		
		$scope.eventEntryDeleteFailure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to delete tournament entry');
		}

		$scope.withdrawFromEvent = function (entry, browserEvent) {
			console.log ('withdrawing from event ' + entry.eventName);
			var eventEntry = null;
			for (var i = 0; i < $scope.enteredEventsList.length; i++) {
				var enteredEventInfo = $scope.enteredEventsList[i];
				if (enteredEventInfo.eventName == entry.eventName) {
					eventEntry = enteredEventInfo.eventEntry;
				}
			}
			
			if (eventEntry != null) {
				eventEntry.tournamentEntryId = $scope.tournamentEntry.id
				console.log ('deleting eventEntry.id = ' + eventEntry.id);
				eventEntryResource.delete (eventEntry, $scope.eventEntryDeleteSuccess, $scope.eventEntryDeleteFailure);

				// remove the deleted event entry from tournament entry list of
				// entries, so the two are in sync without refreshing from
				// backend
				for (var k = 0; k < $scope.tournamentEntry.eventEntries.length; k++) {
					if ($scope.tournamentEntry.eventEntries[k].id == eventEntry.id) {
						$scope.tournamentEntry.eventEntries.splice(k, 1);
					}
				}
			}
		}

		// -------------------------------------------------------------------------------------------------------------------
		// membership options
		// -------------------------------------------------------------------------------------------------------------------

		$scope.membershipOptions = [
		                          {membershipName: 'Membership is current', fee: 0, availableToMembers: 1, availableToAdults: 1, membershipType: 0, forDisplay: 0 },
		                          {membershipName: 'Adult 1-year (G)', fee: 75, availableToMembers: 1, availableToAdults: 1, membershipType: 1, forDisplay: 1 },
		                          {membershipName: 'Adult 3-year (G)', fee: 210, availableToMembers: 1, availableToAdults: 1, membershipType: 2, forDisplay: 1 },
		                          {membershipName: 'Adult 5-year (G)', fee: 325, availableToMembers: 1, availableToAdults: 1, membershipType: 3, forDisplay: 1 },
		                          {membershipName: 'Junior 1-year (G)', fee: 45, availableToMembers: 1, availableToAdults: 0, membershipType: 4, forDisplay: 1 },
		                          {membershipName: 'Junior 3-year (G)', fee: 125, availableToMembers: 1, availableToAdults: 0, membershipType: 5, forDisplay: 1 },
		                          {membershipName: 'Collegiate 1-Year (G)', fee: 45, availableToMembers: 1, availableToAdults: 1, membershipType: 6, forDisplay: 1 },
		                          {membershipName: 'Household 1-Year (G)', fee: 150, availableToMembers: 1, availableToAdults: 1, membershipType: 7, forDisplay: 1 },
		                          {membershipName: 'Lifetime (G)', fee: 1300, availableToMembers: 1, availableToAdults: 1, membershipType: 8, forDisplay: 1 },
		                          // {membershipName: 'Contributor (G)', fee: 45, availableToMembers: 1, availableToAdults: 1, membershipType: 9, forDisplay: 1 },
		                          {membershipName: 'Tournament Pass (per tournament) (A)', fee: 20, availableToMembers: 0, availableToAdults: 1, membershipType: 10, forDisplay: 1 },
		                          ];

		// default to the first option in case the member has a current membership
		$scope.selectedMembershipOption = $scope.membershipOptions[0];
		
		// select an option if the user needs to pay for the membership
		if ($scope.needToPayMembership == true) {
			// if you need to pay for the membership, the second option must be defaulted to
			$scope.selectedMembershipOption = $scope.membershipOptions[1];
			
			// if the user already selected an option during the current session, reselect it now
			if ($scope.tournamentEntry.membershipOption >=0 && $scope.tournamentEntry.membershipOption <= $scope.membershipOptions.length) {
				for (var i = 0; i < $scope.membershipOptions.length; i++) {
					if ($scope.tournamentEntry.membershipOption == $scope.membershipOptions[i].membershipType) {
						
						if ($scope.tournamentEntry.membershipOption != 0)
							$scope.selectedMembershipOption = $scope.membershipOptions[i];
						else
							$scope.selectedMembershipOption = $scope.membershipOptions[1];
						break;
					}
				}
			}
		}

		//
		// figure out if the current user is an adult
		//
		$scope.isAdultUser = function (){
			var tournamentDate = (tournament != null && tournament.endDate != null) ? new Date (tournament.endDate) : new Date();
			var birthdayDate = new Date($scope.userProfile.dateOfBirth);
			var years = tournamentDate.getFullYear() - birthdayDate.getFullYear();

			// reset birthday to the current year.
			birthdayDate.setFullYear(tournamentDate.getFullYear());

			// if the user's birthday has not occurred yet this year, subtract
			// 1.
			if (tournamentDate < birthdayDate)
			    years--;
			
			return (years >= 18);		// used to determine whether to display
										// junior membership options or not
		}

		//
		// store the selected membership option
		//
		$scope.selectMembership = function (option) {
			console.log ('selecting membership option ' + option.membershipName);
			$scope.selectedMembershipOption = option;
			$scope.tournamentEntry.membershipOption = option.membershipType;
			$scope.updateSummary();
		}
		
		// ----------------------------------------------------------------------------------------------------------------------------------
		// Summary step
		// ----------------------------------------------------------------------------------------------------------------------------------
		//
		// calculate events total and other fees
		//
		$scope.invoice = {
				currentItems: [], 
				grandTotal: 0, 
				previousTransactionsItems: [], 
				balanceDue: 0,
				actionLabel: 'Balance Due'};
// $scope.previousTransactions = [{paymentDate: "12/23/2015", amount: 91.00},
// {paymentDate: "12/25/2015", amount: -20.00}];
		$scope.previousTransactions = financialTransactions;
		
		$scope.updateSummary = function () {
			var currentItems = [];
			var grandTotal = 0;
			// invoice
			var eventsTotal = 0;
			var eventItems = [];
			for (var i = 0; i < $scope.enteredEventsList.length; i++) {
				var enteredEventInfo = $scope.enteredEventsList[i];
				eventsTotal += enteredEventInfo.fee;
				eventItems.push({name: enteredEventInfo.eventName, price: enteredEventInfo.fee});
			}
			eventItems.push ({name: 'Events Total', price: eventsTotal});
			currentItems.push({group: 'Events', items: eventItems});
			grandTotal = eventsTotal;

			// membership (only update this if we are on the membership options
			// page and membership needs to be paid)
// if ($scope.needToPayMembership && $scope.getCurrentStepIndex() == 1) {
			if ($scope.needToPayMembership && eventsTotal > 0) {
				var membershipItems = [];
				var membershipName = $scope.selectedMembershipOption.membershipName.substr(0, $scope.selectedMembershipOption.membershipName.length - 3);
				membershipItems.push ({name: membershipName, price: $scope.selectedMembershipOption.fee});
				currentItems.push({group: 'USATT Membership', items: membershipItems});
				grandTotal += $scope.selectedMembershipOption.fee;
			}
			
			// other fees
			if ($scope.tournament.adminFee != 0 || $scope.tournament.lateFee != 0) {
				var otherFeesItems = [];
				if ($scope.tournament.adminFee != 0 && eventsTotal > 0) {
					otherFeesItems.push ({name: 'Administrative fee', price: $scope.tournament.adminFee});
					grandTotal += $scope.tournament.adminFee;
				}
				if ($scope.tournament.lateEntryFee != 0 && $scope.isLateEntry() && eventsTotal > 0) {
					otherFeesItems.push ({name: 'Late fee', price: $scope.tournament.lateEntryFee});
					grandTotal += $scope.tournament.lateEntryFee; 
				}
				if (otherFeesItems.length > 0) {
					currentItems.push({group: 'Other fees', items: otherFeesItems});
				}
			}
			
			// all items
			$scope.invoice.currentItems = currentItems;
			// grand total line
			$scope.invoice.grandTotal = grandTotal;
			
			// balance due (maybe less by payments)
			var previousTransactionsTotal = 0;
			
			// previous payments if any
			var previousTransactionItems = [];
			if ($scope.previousTransactions != null) {
				for (var i = 0; i < $scope.previousTransactions.length; i++) {
					var transaction = $scope.previousTransactions[i];
					var amount = transaction.amount / 100;
					// in Zulu time (i.e. UTC)
					var dateFormat = "YYYY-DD-MM'T'HH:mm:ss.SSSZ"
					var transTime = moment.utc(transaction.createdDate);
					var local = transTime.local();
					var name = transaction.type.name + " on " + local.format('lll');
					// console.log ('transaction type ' +
					// transaction.type.name);
					if (transaction.type.name == 'Refund') {
						amount = -amount;
					}
					previousTransactionItems.push ({name: name, price: amount});
					previousTransactionsTotal += amount;
				}
				previousTransactionItems.push ({name: 'Total', price: previousTransactionsTotal});
			}
			
			$scope.invoice.previousTransactionsItems = previousTransactionItems;
			$scope.invoice.balanceDue = grandTotal - previousTransactionsTotal;
			$scope.invoice.actionLabel = ($scope.invoice.balanceDue >= 0) ? "Balance Due" : 'Refund Due';
			
		}
		
		
		//
		// determine if entry is late
		//
		$scope.isLateEntry = function () {
			var isLate = false;
			var today = new Date();
			var lateEntryStartDate = $scope.tournament.lateEntryStartDate;
			if (lateEntryStartDate != null) {
				isLate = moment(today).isAfter(lateEntryStartDate, 'day');
			}
			return isLate;
		}
		
		// call the above method to refresh the entries
		$scope.updateSummary();

		// ---------------------------------------------------------------------------------------------------------------------------------
		// payments/refunds
		// ---------------------------------------------------------------------------------------------------------------------------------
		// see FinancialTransaction.groovy Type enum
		var CHARGE = 'Charge';
		var REFUND = 'Refund';
		
		// PaymentMethod
		var CREDITCARD = 'CreditCard';
		var CHECK = 'Check';
		var CASH = 'Cash';
		var PAYPAL = 'PayPal';
		
		$scope.card = {
				number: "4242424242424242",
				cvc: "123",
				expiration_month: 1,
				expiration_year: 2018
		}
		
		// error message from token creation
		$scope.error = null;
		
		// button pressed to pay or get refund which was disabled
		$scope.paymentRefundButton = null;
		
		$scope.transactionInProgress = false;
		
		//
		// successful confirmation of entries after payment
		//
		$scope.confirmEntriesSuccess = function (value, responseHeaders) {
			$scope.transactionInProgress = false;
			$state.go ('home.tournamentEntry.completed');
		}
		
		//
		// failure confirmation of entries after payment
		//
		$scope.confirmEntriesFailure  = function (httpResponse) {
			$scope.transactionInProgress = false;
			showError ($mdDialog, httpResponse, "Confirming your event entries failed!");
		}
		
		//
		// confirms the temporarily reserved entries
		//
		$scope.confirmEntries = function (){
			$scope.tournamentEntry.tournamentId = $scope.tournament.id;
			tournamentEntryResource.confirmEntries ($scope.tournamentEntry, $scope.confirmEntriesSuccess, $scope.confirmEntriesFailure);
		}
		
		//
		// called when successfully conducted charge transaction
		//
		$scope.successFinacialTransactionSave = function (value, responseHeaders) {
			$scope.confirmEntries();
		}
		
		//
		// called when save fails
		//
		$scope.errorFinancialTransactionSave = function (httpResponse) {
			$scope.transactionInProgress = false;
			var operation = ($scope.invoice.balanceDue >= 0) ? "Charge" : "Refund";
			showError ($mdDialog, httpResponse, operation + " failed!");
		}

		//
		// handler for token creation
		//
		$scope.stripeResponseHandler = function (status, response) {
// console.log ('in stripeResponseHandler');
			
			$scope.paymentRefundButton.disabled = false; 
			if (response.error) {
				$scope.transactionInProgress = false;
			    // Show the errors on the form
				$scope.error = response.error.message;
				console.log ('error = ' + $scope.error);
			    // $form.find('button').prop('disabled', false);
			  } else {
			    // response contains id and card, which contains additional card
				// details
			    var token = response.id;
// console.log ('token = ' + token);
			    var amount = $scope.invoice.balanceDue * 100;
			    var financialTransaction = {
			    		createdDate: new Date(),
			    		createdBy: session.getUser(),
			    		amount: amount,
			    		type: CHARGE,
			    		paymentMethod: CREDITCARD,
			    		paymentMethodIdentifier: token,
			    		stripeChargeIdentifier: "ch_temporary_not_null",  // will
																			// be
																			// assigned
																			// after
																			// transaction
																			// completes
			    		stripeRefundIdentifier: "ref_temp_not_null",
			    		// required by resource for parameter mapping
			    		tournamentEntryId: $scope.tournamentEntry.id,
			    		account: {id: 1},
			    		tournamentEntry: {class: $scope.tournamentEntry.class, 
			    			id: $scope.tournamentEntry.id, 
			    			tournament: {
			    				class: $scope.tournamentEntry.tournament.class,
			    				id: $scope.tournamentEntry.tournament.id
			    			}
			    		}
			    };
			    // initiate the transaction
			    financialTransactionResource.save (financialTransaction, $scope.successFinacialTransactionSave, $scope.errorFinancialTransactionSave);
			  }			
		}
		
		$scope.performPayment = function (browserEvent) {
			browserEvent.preventDefault();
			
			// disable buttons to prevent double submittal
			browserEvent.target.disabled = true;
			$scope.paymentRefundButton = browserEvent.target; 
			
			// disable the button to prevent multiple submission $scope.error =
			// null;
			// validate data
			var validNumber = Stripe.card.validateCardNumber($scope.card.number); 
			var validCVC = Stripe.card.validateCVC($scope.card.cvc); 
			var validExpiration = Stripe.card.validateExpiry($scope.card.expiration_month, $scope.card.expiration_year); 
			if (validNumber && validCVC && validExpiration) { 
			  // This identifies your website in the createToken call below
			  var stripePublishableKey = $scope.tournament.stripeKey; 
			  if (stripePublishableKey != null) { 
				  $scope.transactionInProgress = true;
				  Stripe.setPublishableKey(stripePublishableKey);
			
				  // create token
			  Stripe.card.createToken({ 
				  number: $scope.card.number, 
				  cvc:$scope.card.cvc, 
				  exp_month: $scope.card.expiration_month, 
				  exp_year: $scope.card.expiration_year 
				  }, 
				  $scope.stripeResponseHandler); 
			  } else {
				  $scope.error = "missing Stripe gateway key"; 
				  } 
			  } else { 
				  $scope.error = ""; 
				  if (!validNumber) 
					  $scope.error += "Invalid number"
			  
				  if (!validCVC) { 
					$scope.error += ($scope.error != "") ? ", " : "";
					$scope.error += "Invalid CVC" 
				  }
			  
				  if (!validExpiration) { 
					  $scope.error += ($scope.error != "") ? ", " : "";
					  $scope.error += "Invalid expiration date"; 
				  } 
			}
		}
		
		//
		// issue refund
		//
		$scope.performRefund = function (browserEvent) {
			browserEvent.preventDefault();
			
			// disable buttons to prevent double submittal
			browserEvent.target.disabled = true;
			$scope.paymentRefundButton = browserEvent.target; 
			
			// disable the button to prevent multiple submission
			$scope.error = null;
			
			console.log ('performing refund of ' + $scope.invoice.balanceDue);
		    var amount = $scope.invoice.balanceDue * 100;
		    var financialTransaction = {
		    		createdDate: new Date(),
		    		createdBy: session.getUser(),
		    		amount: amount,
		    		type: REFUND,
		    		paymentMethod: CREDITCARD,
		    		paymentMethodIdentifier: "token_not_null",
		    		stripeChargeIdentifier: "ch_temporary_not_null",
		    		stripeRefundIdentifier: "ref_temp_not_null",
		    		// required by resource for parameter mapping
		    		tournamentEntryId: $scope.tournamentEntry.id,
		    		account: {id: 1},
		    		tournamentEntry: {class: $scope.tournamentEntry.class, 
		    			id: $scope.tournamentEntry.id, 
		    			tournament: {
		    				class: $scope.tournamentEntry.tournament.class,
		    				id: $scope.tournamentEntry.tournament.id
		    			}
		    		}
		    };
		    // initiate the transaction
		    financialTransactionResource.save (financialTransaction, $scope.successFinacialTransactionSave, $scope.errorFinancialTransactionSave);
		}
		
		$scope.viewTournament = function (browserEvent) {
			browserEvent.preventDefault();
			var params = {id: $scope.tournament.id};
			$state.go('home.tournamentView', params);
		}
	} 
	])
})();
