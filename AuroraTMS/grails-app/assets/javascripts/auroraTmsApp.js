(function(){
'use strict';

var app = angular.module('auroraTmsApp', ['ui.router', 'ngMaterial', 'ngMessages', 'ngResource', 'date-range',
                                          'LocalStorageModule', 'ngMaterial.components', 'ngMdIcons', 'mdDateTime', 'md.data.table', 'mdPickers',
                                          'menu', 'login', 'dashboard', "campaigns", 'registerUser', 'userProfile',
                                          'usattSearch', 'tournament', 'tournamentList', 'event']);
app.config(['$stateProvider', '$urlRouterProvider', '$mdThemingProvider', '$mdIconProvider', 
            function($stateProvider, $urlRouterProvider, $mdThemingProvider, $mdIconProvider) {
    $urlRouterProvider.otherwise('/');

    $stateProvider
        .state('home',{
        url: '/',
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

	app.controller('ContentController', [ '$scope', function($scope) {
		$scope.somthing = 6;

	} ]);

	app.controller('FooterController', [ '$scope', function($scope) {
		$scope.somthing = 7;

	} ]);
	
           
})();
