(function(angular) {

	function userProfileResourceFactory($resource, session) {

		var applicationRoot = '/AuroraTMS/api/userprofiles/'
		var url = applicationRoot + ':id';
		// Pass session token as Authorization header
		var headers = {
			'Authorization' : function(config) {
				// do it via function so logged in user's token is passed
				return 'Bearer ' + session.getAccessToken();
			}
		};

		// Assemble actions with custom headers attached
		var actions = {
			'view' : {
				method : 'GET',
				url : "/AuroraTMS/api/public/userprofiles/:id"
			},
			'get' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + ':id/edit'
			},
			'save' : {
				method : 'POST',
				headers : headers
				//,
				//url : applicationRoot + '?username=:username'
			},
			'create' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + 'create'
			}, // creates empty object
			// 'query' : {method: 'GET', isArray: true, headers: headers, url:
			// applicationRoot + ':firstName'}, // lists all profiles
			// 'remove': {method: 'DELETE', headers: headers},
			'delete' : {
				method : 'DELETE',
				headers : headers
			},
			'update' : {
				method : 'PUT',
				headers : headers
			},
			'editByUsername' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + ':id/edit?username=:username&memberId=:memberId'
			},
			'updateAssociationWithUser' : {
				method : 'PATCH',
				headers : headers,
				url : applicationRoot + ':id?username=:username'
			}
			
		};

		var userProfileResource = $resource(url, {
			id : '@id', 
			username : '@username',
			memberId : '@memberId'
		}, actions);

		return userProfileResource;
	}

	userProfileResourceFactory.$inject = [ '$resource', 'session' ];

	angular.module('auroraTmsApp').factory('userProfileResource',
			userProfileResourceFactory);

})(angular);