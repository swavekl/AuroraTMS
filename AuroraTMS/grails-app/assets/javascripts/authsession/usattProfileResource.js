(function(angular) {

	function usattProfileResourceFactory($resource, session) {

		var applicationRoot = '/AuroraTMS/api/usattprofiles'
		var url = applicationRoot + ':id';
		// Pass session token as Authorization header
		var headers = {
			'Authorization': function (config) {
				// do it via function so logged in user's token is passed
				return 'Bearer ' + session.getAccessToken();
			}
		};

		// Assemble actions with custom headers attached
		var actions = {
			'get' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + ':id/edit'
			},
			'save' : {
				method : 'POST',
				headers : headers,
				url : applicationRoot + ':id'
			},
			'create' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + '/create?memberId=:memberId'
			}, // creates empty object
			'delete' : {
				method : 'DELETE',
				headers : headers
			},
			'update' : {
				method : 'PUT',
				headers : headers
			},
			'query' : {
				method : 'GET',
				isArray : true,
				headers : headers,
				url : applicationRoot
						+ '?offset=:offset&max=:max&firstName=:firstName&lastName=:lastName&memberId=:memberId'
			},
			'queryByMemberId' : {
				method : 'GET',
				isArray : true,
				headers : headers,
				url : applicationRoot + '?memberId=:memberId'
			},
			'newUSATTMember' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + '/newmember'
			}
		};

		var usattProfileResource = $resource(url, {
			offset : '@offset',
			max : '@max',
			memberId : '@memberId',
			firstname : '@firstname',
			lastname : '@lastname'
		}, actions);

		return usattProfileResource;
	}

	usattProfileResourceFactory.$inject = [ '$resource', 'session' ];

	angular.module('auroraTmsApp').factory('usattProfileResource',
			usattProfileResourceFactory);

})(angular);