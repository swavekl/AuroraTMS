(function (angular) {

  function UserService($http, $q){

  /**
	 * check if username is unique
	 * 
	 * @param username
	 * @returns {*|Promise}
	 */
    this.checkUnique = function(username){
    	var deferred = $q.defer();
        $http({method: 'GET',
        	url: '/AuroraTMS/api/user',
        	params: {'username': username}
        })
        .success(function(data, status, headers, config) {
        	var unique = false; 
        	if(data.hasOwnProperty('unique')){
        		unique = data.unique;
   		    }
            (unique) ? deferred.resolve() : deferred.reject();
        })
        .error(function (data, status, headers, config) {
        	deferred.reject();
        });
        
        return deferred.promise; 
    };

    // Inject dependencies
    UserService.$inject = ['$http', '$q'];

  // Export
  angular
    .module('auroraTmsApp')
    .service('userService', UserService);
  }
})(angular);