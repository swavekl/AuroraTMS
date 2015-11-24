(function() {
	'use strict';

	var menuModule = angular.module('menu', [ 'ui.router', 'ngMaterial' ]);

	menuModule.controller('MenuController', [ '$scope', '$state', '$mdSidenav', 'auth',
			function($scope, $state, $mdSidenav, auth) {
				// some variable
				$scope.somthing = 8;

				$scope.navigateTo = function(nextState) {
					$mdSidenav('left').close().then(function () {
						$state.go(nextState);
			          });
				}

				$scope.openLeftMenu = function() {
					$mdSidenav('left').toggle();
				}

				$scope.closeLeftMenu = function() {
					$mdSidenav('left').close();
				}

				$scope.logout = function(nextState) {
					$mdSidenav('left').close().then(function () {
						auth.logOut();
						$state.go(nextState);
			          });
				}
			}
	]);

})();
