(function(angular) {

	function userProfilePublicResourceFactory($resource, session) {

		var applicationRoot = '/AuroraTMS/api/public/userprofiles'
		var url = applicationRoot;
		// Pass session token as Authorization header
//		var headers = {
//			'Authorization' : function(config) {
//				// do it via function so logged in user's token is passed
//				return 'Bearer ' + session.getAccessToken();
//			}
//		};

		// Assemble actions with custom headers attached
		var actions = {
			'view' : {
				method : 'GET',
				url : applicationRoot + "/:id"
			},
			'query' : {
				method: 'GET', 
				isArray: true, 
				url: applicationRoot
			}
		};

		var userProfileResource = $resource(url, {
			id : '@id',
			eventId : '@eventId'
		}, actions);

		return userProfileResource;
	}

	userProfilePublicResourceFactory.$inject = [ '$resource', 'session' ];

	angular.module('auroraTmsApp').factory('userProfilePublicResource',
			userProfilePublicResourceFactory);

})(angular);