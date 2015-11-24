(function() {
	'use strict';

	angular.module('login', [ 'ui.router' ])

	.config([ '$stateProvider', '$urlRouterProvider',
			function($stateProvider, $urlRouterProvider) {
				$stateProvider.state('home.login', {
					url : 'login',
					views : {
						'content@' : {
							templateUrl : 'assets/partials/login/loginContent.html',
							controller : 'LoginCtrl'
						}
					}

				})
			} ])

	.controller('LoginCtrl', [ '$scope', '$q', '$http', '$location', '$state', 'auth',
	                           function($scope, $q, $http, $location, $state, auth ) {
	
		$scope.login = {
			username: "swavek",
			password: "swavek",
			errors: ""
		};
	
		$scope.doLogin = function () {
			var credentials = new Object();
			credentials.username = $scope.login.username;
			credentials.password = $scope.login.password;
			auth.logIn (credentials);
		};
		
		$scope.doLogout = function(){
			auth.logOut();
		};
		
		$scope.isLoggedIn = function(){
			return auth.isLoggedIn();
		};
} ])
})();




