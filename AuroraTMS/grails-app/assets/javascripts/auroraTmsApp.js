(function(){
'use strict';

var app = angular.module('auroraTmsApp', ['ui.router', 'ngMaterial', 'ngMessages', 'ngResource', 'date-range',
                                          'LocalStorageModule', 'ngMaterial.components', 'ngMdIcons', 'mdDateTime', 'md.data.table', 'mdPickers',
                                          'menu', 'login', 'dashboard', "campaigns", 
                                          'registerUser', 'userProfile',
                                          'usattSearch', 'tournament', 'tournamentList', 'event',
                                          'tournamentEntry']);
app.config(['$stateProvider', '$urlRouterProvider', '$mdThemingProvider', '$mdIconProvider', 'localStorageServiceProvider',
            function($stateProvider, $urlRouterProvider, $mdThemingProvider, $mdIconProvider, localStorageServiceProvider) {
    $urlRouterProvider.otherwise('/');
    
    localStorageServiceProvider
    .setStorageType('sessionStorage')
    .setStorageCookie(45, '/')
    .setStorageCookieDomain('window.location')
    .setPrefix('auroraTmsApp');

    $stateProvider
        .state('home',{
        url: '/',
		resolve: {
			session: 'session',
			
			userProfileResource: 'userProfileResource',
			userProfile: function($stateParams, userProfileResource, localStorageService) {
				// fetch user profile without logging in to get the latest ratings
				var userProfileId = localStorageService.get ('userProfileId');
				if (userProfileId != null) {
					console.log ('getting latest user profile info for user with id ' + userProfileId);
					return userProfileResource.view({id: userProfileId}).$promise;
				} else {
					console.log ('No user profile id');
					return {}
				}
			}
		},

        views: {
//            'header': {
//                templateUrl: 'assets/partials/header.html',
//                controller: 'HeaderController'
//            },
	        'menu': {
	          templateUrl: 'assets/partials/menu.html'
	        },
            'content': {
                templateUrl: 'assets/partials/content.html',
                controller: 'ContentController' 
            }
//            'footer': {
//                templateUrl: 'assets/partials/footer.html',
//                controller: 'FooterController'
//            }
        }
    })

    // Configure a dark theme with primary foreground yellow
    $mdThemingProvider.theme('docs-dark')
        .primaryPalette('yellow')
        .dark();   
    
    $mdIconProvider.icon('delete', 
    		'assets/material/actions/delete.svg', 24);
    ;
}]);


	app.controller('HeaderController', [ '$scope', function($scope) {
		$scope.somthing = 5;
	} ]);

	app.controller('ContentController', [ '$scope','userProfile', 
	                                    function($scope, userProfile) {
		$scope.somthing = 6;
		$scope.userProfile = userProfile;
		$scope.hideProfile = (userProfile.usattID == undefined);
	} ]);

	app.controller('FooterController', [ '$scope', function($scope) {
		$scope.somthing = 7;

	} ]);
	
           
})();
