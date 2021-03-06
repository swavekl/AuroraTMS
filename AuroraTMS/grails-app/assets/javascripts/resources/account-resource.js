(function(angular) {
	
	function accountResourceFactory($resource, session) {
		var applicationRoot = '/AuroraTMS/api/accounts'
		var url = applicationRoot + '/:id';
		// Pass session token as Authorization header
		var headers = {
			'Authorization': function (config) {
				// do it via function so logged in user's token is passed
				return 'Bearer ' + session.getAccessToken();
			}
		};

		// Assemble actions with custom headers attached
		var actions = {
			'view' : {
				method : 'GET',
//				headers : headers,
				url : applicationRoot + '/:id'
			},
			'get' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + '/:id/edit'
			},
			'save' : {
				method : 'POST',
				headers : headers
			},
			'create' : {
				method : 'GET',
				headers : headers,
				url : applicationRoot + '/create'
			}, // creates empty object
			'query' : {
				method : 'GET',
				isArray : true,
//				headers : headers,
				url : applicationRoot + '?tournamentId=:tournamentId&offset=:offset&max=:max'
			},
			'list' : {
				method : 'GET',
				isArray : true,
				headers : headers,
				url : applicationRoot + '?owned=true&offset=:offset&max=:max'
			},
			'delete' : {
				method : 'DELETE',
				headers : headers
			},
			'update' : {
				method : 'PUT',
				headers : headers
			}
		};

		var accountResource = $resource(url, {
			id : '@id',
			offset : '@offset',
			max : '@max',
			tournamentId: '@tournamentId'
		}, actions);

		return accountResource;
	}

	accountResourceFactory.$inject = [ '$resource', 'session' ];

	angular.module('auroraTmsApp').factory('accountResource',
			accountResourceFactory);

})(angular);
