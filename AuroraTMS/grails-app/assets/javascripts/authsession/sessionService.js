(function (angular) {

  function sessionService($log, localStorageService){

    // Instantiate data when service
    // is loaded
    this._user = null; // JSON.parse(localStorageService.get('session.user'));
    this._accessToken = null;  //JSON.parse(localStorageService.get('session.accessToken'));
    this._roles = null;
    this._userProfileId = null;
    this._nextState = null;
    this._nextStateParams = null;

    this.getUser = function(){
      return this._user;
    };

    this.setUser = function(user){
      this._user = user;
      //localStorageService.set('session.user', JSON.stringify(user));
      return this;
    };

    this.getUserProfileId = function(){
        return this._userProfileId;
      };

    this.setUserProfileId = function(userProfileId){
        this._userProfileId = userProfileId;
        console.log ('setting userprofile id to ' + userProfileId);
        //localStorageService.set('session.userProfileId', JSON.stringify(userProfileId));
        return this;
    };
    
    this.getRoles = function () {
    	return this._roles;
    };
    
    this.setRoles = function (roles) {
    	this._roles = roles;
    };
    
    this.isInRole = function (role) {
    	var hasRole = false;
    	if (this._roles != null) {
    		for (var i = 0; i < this._roles.length; i++) {
    			if (this._roles[i] == role) {
    				hasRole = true;
    				break;
    			}
    		}
    	}
    	
    	return hasRole;
    };
    
    this.hasGroup = function () {
    	// to do
    	return false;
    }

    this.getAccessToken = function(){
      return this._accessToken;
    };

    this.setAccessToken = function(token){
      this._accessToken = token;
      //localStorageService.set('session.accessToken', token);
      return this;
    };
    
    this.setNextState = function (nextState) {
    	this._nextState = nextState;
    }

    this.getNextState = function (nextState) {
    	var retValue = this._nextState;
    	this._nextState = null;
    	return retValue;
    }
    
    this.setNextStateParams = function (nextStateParams) {
    	this._nextStateParams = nextStateParams;
    }
    
    this.getNextStateParams = function () {
    	var retValue = this._nextStateParams;
    	this._nextStateParams = null;
    	return retValue;
    } 

    /**
     * Destroy session
     */
    this.destroy = function destroy(){
      this.setUser(null);
      this.setRoles(null);
      this.setAccessToken(null);
      this.setUserProfileId(null);
    };

  }

  // Inject dependencies
  sessionService.$inject = ['$log', 'localStorageService'];

  // Export
  angular
    .module('auroraTmsApp')
    .service('session', sessionService);

})(angular);