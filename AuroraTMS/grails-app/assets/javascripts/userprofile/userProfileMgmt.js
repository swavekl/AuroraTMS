(function() {
	'use strict';

	angular.module('userProfile', [ 'ui.router', 'ngResource' ])

	.config([ '$stateProvider', '$urlRouterProvider', 
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.createProfile', {
					url : 'userprofile/createProfile/:memberId',
					data: {
						privateUrl : true
					},
					views : {
						'content@' : {
							templateUrl : 'assets/partials/userProfile/userProfileEdit.html',
							resolve: {
								session: 'session',

								usattProfileResource: 'usattProfileResource',
								usattProfile: function(usattProfileResource, $stateParams, session) {
									var memberId = $stateParams.memberId;
									console.log ('memberId = ' + memberId);
									if (memberId != undefined && memberId != 0) {
										console.log ('gettting USATT profile by memberId ' + memberId);
										return usattProfileResource.query({memberId: memberId}).$promise;
									} else {
										return [];
									}
								},
								
								// empty to satisfy controller dependencies
								userProfileResource: 'userProfileResource',
								userProfile : function (userProfileResource, $stateParams, session, usattProfile) { 
									var userId = session.getUser();
									var memberId = $stateParams.memberId;
									var id = 123;
									console.log ('creating user profile by userid ' + userId + ' and memberId ' + memberId);
									
									return userProfileResource.editByUsername({id: id, username: userId, memberId: memberId}).$promise;
								}
							},
							controller : 'userProfileCtrl'
						}
					}
				})

				.state('home.editProfile', {
					url : 'userprofile/editProfile',
					data: {
						privateUrl : true
					},
					views : {
						'menu@' : {
							templateUrl : 'assets/partials/menu.html'
						},
				
						'content@' : {
							templateUrl : 'assets/partials/userProfile/userProfileEdit.html',
							resolve: {
								session: 'session',
								
								// empty to satisfy the interface
								usattProfileResource: 'usattProfileResource',
								usattProfile : function (usattProfileResource, $stateParams, session) {
									return []
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
								}
							},
							controller : 'userProfileCtrl'
						}
					}
				})
			} ])

	.controller('userProfileCtrl', 
			['$scope', '$state', '$mdDialog', 'userProfileResource', 'userProfile', 'session', 'usattProfileResource', 'usattProfile',
		        function($scope, $state, $mdDialog, userProfileResource, userProfile, session, usattProfileResource, usattProfile) {
		
		if (userProfile.dateOfBirth != undefined) {
			userProfile.dateOfBirth = new Date (userProfile.dateOfBirth);
		} else {
			userProfile.dateOfBirth = new Date ();
		}
			
		// user profile info retrieved
		$scope.profile = userProfile;
		if (userProfile != undefined && userProfile.id != null) {
			session.setUserProfileId(userProfile.id);
		}
		$scope.dateOfBirthText = moment(userProfile.dateOfBirth).format('LL');
		$scope.dateOfBirthTextSaved = $scope.dateOfBirthText;
		
		$scope.profile.gender = ($scope.profile.gender != null) ? $scope.profile.gender : 'M';
		
		// if we are creating a new user and have his USATT record
		// transfer this data into user profile to minimize need to type
		if (usattProfile.length == 1) {
			// USATT member info is retrieved
			var up = usattProfile[0];
			var streetAddress = up.address1 ? up.address1 : "";
			streetAddress += up.address2 ? (" " + up.address2) : "";
			var dob = new Date (up.dateOfBirth);
			var expirationDate = new Date (up.expirationDate);
			$scope.profile = {
				firstName : up.firstName,
				lastName: up.lastName,
				usattID: up.memberId,
				streetAddress: streetAddress,
				city: up.city,
				state: up.state,
				zipCode: up.zipCode,
				country: up.country,
				gender: up.gender,
				dateOfBirth: dob,
				expirationDate: expirationDate 
			};
			$scope.dateOfBirthText = moment(dob).format('LL');
			$scope.dateOfBirthTextSaved = $scope.dateOfBirthText;
		}
		
		$scope.expirationDateText = moment($scope.profile.expirationDate).format('LL');
		
		$scope.successUpdate = function(value, responseHeaders) {
			console.log ('successfully updated association');
			$state.go('home');
		}
		
		$scope.failureUpdate = function(value, responseHeaders) {
			showError ($mdDialog, httpResponse, 'Failed to Associate User Profile with Login');
		}
		
		$scope.successAndUpdateAssociation = function(value, responseHeaders) {
			$scope.profile.id = value.id;
			session.setUserProfileId (value.id);
			console.log ('set user profile id to ' + session.getUserProfileId());
			var username = session.getUser();
			console.log ('updating association between profile ' + value.id + ' and user ' + username);
			userProfileResource.updateAssociationWithUser ({id: value.id, username: username}, $scope.successUpdate, $scope.failureUpdate);
		}

		$scope.success = function(value, responseHeaders) {
			$scope.profile.id = value.id;
			session.setUserProfileId (value.id);
			console.log ('set user profile id to ' + session.getUserProfileId());
			$state.go('home');
		}
		
		$scope.failure = function (httpResponse) {
			showError ($mdDialog, httpResponse, 'Failed to Save User Profile');
		}
		
		$scope.save = function () {
// console.log ('saving $scope.dateOfBirthText = ' + $scope.dateOfBirthText);
// console.log ('saving $scope.dateOfBirthTextSaved = ' +
// $scope.dateOfBirthTextSaved);
// console.log ('saving $scope.profile.dateOfBirth = ' +
// $scope.profile.dateOfBirth);
			if ($scope.dateOfBirthText != $scope.dateOfBirthTextSaved) {
				// October 18, 1964
				$scope.profile.dateOfBirth = moment($scope.dateOfBirthText, "MMMM-D-YYYY").toDate();
// console.log ('converted $scope.profile.dateOfBirth to ' +
// $scope.profile.dateOfBirth);
			} else {
				$scope.dateOfBirthText = moment($scope.profile.dateOfBirth).format('LL');
			} 
			$scope.dateOfBirthTextSaved = $scope.dateOfBirthText;
			
			if ($scope.profile.id == null) {
				var username = session.getUser();  // pass username so we can
													// link them together
				console.log ('saving new user profile ' + $scope.profile + " for user " + username);
				userProfileResource.save ($scope.profile, $scope.successAndUpdateAssociation, $scope.failure);
			} else {
				userProfileResource.update ($scope.profile, $scope.success, $scope.failure);
			}
		}
} ])
})();
