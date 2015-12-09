(function (angular) {

  function checkAccessOnStateChange($rootScope, $state, auth, session){
	  
    // Listen for state changes when using ui-router
    $rootScope.$on('$stateChangeStart', 
    		function(event, toState, toParams, fromState, fromParams) {

      var protectedState = ((toState.data != undefined && toState.data.privateUrl != undefined) ? toState.data.privateUrl : false);

// console.log ('fromState ' + ((fromState) ? fromState.name : "unknown"));
// console.log ('fromParams ' + angular.toJson(fromParams, true));
// console.log ('toState ' + toState.name);
// console.log ('toParams ' + angular.toJson(toParams, true));
// console.log ('State is protected = ' + protectedState);
      // Here we simply check if logged in but you can
      // implement more complex logic that inspects the
      // state to see if access is allowed or not
// console.log ('logged in user is ' + session.getUser());
      if (protectedState && !auth.isLoggedIn()) {
          session.setNextState (toState.name);
          session.setNextStateParams (toParams);

          // console.log ('redirecting to login state because ' + toState.name + " is protected");
 	      // Redirect to login
    	  $state.go ("home.login");

          // Prevent state change
          event.preventDefault();
    	  }
      });
  };

  // Inject dependencies
  checkAccessOnStateChange.$inject = ['$rootScope', '$state', 'auth', 'session'];

  // Export
  angular
    .module('auroraTmsApp')
    .run(checkAccessOnStateChange);

})(angular);

// // Listen for location changes
// // This happens before route or state changes
// $rootScope.$on('$locationChangeStart', function(event, newUrl, oldUrl){
// if(!auth.isLoggedIn()){
//
//     
// // Redirect to login
// $state.go ("home.login");
// console.log ('redirecting to login page');
// 	
// // Prevent location change
// event.preventDefault();
// }
// });

// // Listen for route changes when using ngRoute
// $rootScope.$on('$routeChangeStart', function(event, nextRoute, currentRoute){
//
// // Here we simply check if logged in but you can
// // implement more complex logic that inspects the
// // route to see if access is allowed or not
// if(!auth.isLoggedIn()){
//
// // Redirect to login
//
// // Prevent state change
// event.preventDefault();
// }
// });

