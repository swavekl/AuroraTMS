/**
 * Search for USATT member, create new member etc. 
 */
(function() {
	'use strict';

	angular.module('usattSearch', [ 'ui.router', 'ngResource' ])

	.config([ '$stateProvider', '$urlRouterProvider', 
			function($stateProvider, $urlRouterProvider) {
				$stateProvider
				.state('home.searchMember', {
					url : 'usatt/searchMember',
					privateUrl : true,
					roles: ['ROLE_USER'],
					views : {
						'content@' : {
							templateUrl : 'assets/partials/usatt/usattSearchContent.html',
							resolve: {
								usattProfileResource: 'usattProfileResource'
							},
							controller : 'usattSearchCtrl'
						}
					}
				})
			} ])

	.controller('usattSearchCtrl', 
			['$scope', '$state', 'usattProfileResource', 'session',
		        function($scope, $state, usattProfileResource, session) {

		// search criteria
		$scope.search = {
				firstName: "",
				lastName: "",
				memberId: "",
				// paging controls
				max: 20,
				offset: 0,
				listEnd: false,
				searched: false,
				inProgress: false
		}
		
		// search results
		$scope.usattMembers = [];
		
		$scope.getNextPageOffset = function () {
			return $scope.search.offset + $scope.search.max;
		}
		
		$scope.getPrevPageOffset = function () {
			var previousOffset = $scope.search.offset - $scope.search.max;
			return (previousOffset >= 0) ? previousOffset : 0;
		}
		
		$scope.findUSATTMember = function (firstName, lastName, memberId) {
			$scope.search.offset = 0;
			$scope.findUSATTMemberInternal ($scope.search.offset, $scope.search.max, firstName, lastName, memberId);
		}

		$scope.nextPage = function () {
			$scope.search.offset = $scope.getNextPageOffset();
			$scope.findUSATTMemberInternal ($scope.search.offset, $scope.search.max, $scope.search.firstName, $scope.search.lastName, $scope.search.memberId);
		}

		$scope.prevPage = function () {
			$scope.search.offset = $scope.getPrevPageOffset();
			$scope.findUSATTMemberInternal ($scope.search.offset, $scope.search.max, $scope.search.firstName, $scope.search.lastName, $scope.search.memberId);
		}
		
		$scope.reachedEnd = function () {
			return $scope.search.listEnd;
		}
		
		$scope.reachedStart = function () {
			return ($scope.search.offset == 0);
		}

		$scope.findUSATTMemberInternal = function (offset, max, firstName, lastName, memberId) {
		$scope.search.listEnd = false;
		$scope.search.searched = true;
		$scope.usattMembers = [];
		$scope.search.inProgress = true;
//		console.log ('searching for USATT member: name ' + firstName + ", " +  lastName + ", " + memberId + ', offset '  + offset);
		usattProfileResource.query({offset: offset, max: max, firstName: firstName, lastName: lastName, memberId: memberId}).$promise
        .then(function(data) {
        	$scope.search.inProgress = false;
            // promise fulfilled
//    		console.log ('found ' + data.length + ' players');
            if (data.length > 0) {
            	if (data.length == 1) {
                	var player = data[0];
//    				console.log ('found 1 player ' + player.firstName + ' ' + player.lastName + ' memberId ' + player.memberId);
    				var params = {memberId: player.memberId};
    				$state.go ('home.createProfile', params);
    				$scope.search.listEnd = true;
            	} else {
            		$scope.usattMembers = data;
            		$scope.search.listEnd = data.length < $scope.search.max;
            	}
            } else {
//            	console.log ('no players found');
            	$scope.search.listEnd = true;
            }
        }, function(error) {
        	$scope.search.inProgress = false;
            // promise rejected, could log the error with:
        	console.log('error', error);
        });
	}
		
		$scope.newUSATTMember = function () {
			$state.go ('home.createProfile', {memberId: 0});
		}
		
		$scope.editMember = function (memberId, event) {
			$state.go ('home.createProfile', {memberId: memberId});
		}
} ])
})();
