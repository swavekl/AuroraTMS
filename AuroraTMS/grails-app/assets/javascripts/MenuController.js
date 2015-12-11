(function() {
	'use strict';

	var menuModule = angular.module('menu', [ 'ui.router', 'ngMaterial', "ng.deviceDetector" ]);

	menuModule.controller('MenuController', [ '$scope', '$state', '$mdSidenav', 'auth', 'deviceDetector',
			function($scope, $state, $mdSidenav, auth, deviceDetector) {
				// some variable
				$scope.somthing = 8;
				
				$scope.vm = deviceDetector;
				// difference of overflow handling
				$scope.myOverlfow = ($scope.vm.browser == 'firefox' && !$scope.vm.isMobile()) ? 'hidden' : 'auto';
				
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
