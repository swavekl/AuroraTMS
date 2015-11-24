(function (angular) {

  function AuthService($http, $q, $state, session, $mdDialog){

//	  $scope.login = {
//		  errors: []
//	  };
	  
	/**
    * Check whether the user is logged in
    * @returns boolean
    */
    this.isLoggedIn = function isLoggedIn(){
      return session.getUser() !== null;
    };

    /**
    * Log in
    *
    * @param credentials
    * @returns {*|Promise}
    */
    this.logIn = function(credentials){
    	var deferred = $q.defer();
        $http({method: 'POST',
        	url: '/AuroraTMS/api/login',
        	params: {'username': credentials.username, 
        		     'password': credentials.password
        		     }
        })
        .success(function(data, status, headers, config) {
        	console.log ('success ' + data);
        	 if(data.hasOwnProperty('error')){
        		 console.log ('error ' + status);
        		 } else if (data.hasOwnProperty('username')) {
        	          session.setUser(data.username);
        	          session.setRoles(data.roles);
        	          session.setAccessToken(data.access_token);
        	          
        	          var nextState = session.getNextState ();
        	          var nextStateParams = session.getNextStateParams();
        	          if (nextState != null) {
//             			 console.log ('logged in OK - going to state ' + nextState);
            	          $state.go (nextState, nextStateParams);
        	          } else {
        	        	  $state.go ('home');
        	          }
        		 }
            deferred.resolve(data);
        })
        .error(function (data, status, headers, config) {
        	console.log ('error during login ' + status + ", data " + data);
        	var alert = $mdDialog.alert({
                title: 'Error',
                content: 'Error during login (' + status + ').  Please check username and password are correct',
                ok: 'Close'
              });
              $mdDialog
                .show( alert )
                .finally(function() {
                  alert = undefined;
                });        	
        });
        
        return deferred.promise; 
    };

    /**
    * Log out
    *
    * @returns {*|Promise}
    */
    this.logOut = function(){
    	// since default spring security rest storage for tokens is JWT the token can't be deleted 
    	// (it will expire), so it sends back 404
        session.destroy();
        $state.go ('home');

//    	return $http({method: 'GET',
//        	url: '/AuroraTMS/api/logout',
//           	headers: {'Authorization': 'Bearer ' + session.getAccessToken()}
//        })
//        .success(function(data, status, headers, config) {
//        	// in case we switch to another storage for tokens method keep this code.
//            session.destroy();
//            $state.go ('home');
//        })
//        .error(function (data, status, headers, config) {
//        	// since default spring security rest storage for tokens is JWT the token can't be deleted 
//        	// (it will expire), so it sends back 404
//            session.destroy();
//            $state.go ('home');
//        });
    };

  }

  // Inject dependencies
  AuthService.$inject = ['$http', '$q', '$state', 'session', '$mdDialog'];

  // Export
  angular
    .module('auroraTmsApp')
    .service('auth', AuthService);

})(angular);
