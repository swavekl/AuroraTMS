(function() {
	'use strict';

	angular.module('registerUser', [ 'ui.router' ])

	.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider.state('home.registerUser', {
					url : 'registerUser',
					views : {
						'content@' : {
							templateUrl : 'assets/partials/register/registerContent.html',
							controller : 'RegisterCtrl'
						}
					}

				})

				$stateProvider.state('home.registerSuccess', {
					url : 'registerSuccess',
					views : {
						'content@' : {
							templateUrl : 'assets/partials/register/registerSuccess.html',
							controller : 'RegisterCtrl'
						}
					}

				})
			} ])

	.controller('RegisterCtrl', [ '$scope', '$q', '$http', '$state', 
	                           function($scope, $q, $http, $state ) {
	
		$scope.registrationInfo = {
			username: 'slorenc',
			email: "swaveklorenc@yahoo.com",
			password: '',
			password2: '',
			errors: []
		};
		
		$scope.registering = false;
	
		$scope.doRegisterUser = function () {
			$scope.registering = true;
			$scope.errorMessage = "";
			var deferred = $q.defer();
	        $http({method: 'POST',
	        	url: 'register/ajaxRegister',
	        	params: {
	        		'username': $scope.registrationInfo.username,
	        		'email': $scope.registrationInfo.email, 
	        		'password': $scope.registrationInfo.password,
	        		'password2': $scope.registrationInfo.password2,
	        		 'ajax': true
	        		     }
	        })
	        .success(function(data, status, headers, config) {
				$scope.registering = false;
	        	console.log ('success ' + data);
	        	 if(data.hasOwnProperty('errors')){
	        		 $scope.registrationInfo.errors = data.errors;
	        		 } else if (data.hasOwnProperty('emailSent')) {
	        			 console.log ('emailed registration info in OK');
	        			 $state.go('home.registerSuccess');
	        		 }
	            deferred.resolve(data);
	        })
	        .error(function (data, status, headers, config) {
				$scope.registering = false;
	        	console.log ('Got error ' + data + " status " + status);
	            deferred.reject('Registration failed');
	        });
			
			return deferred.promise;
		};

} ])
})();
