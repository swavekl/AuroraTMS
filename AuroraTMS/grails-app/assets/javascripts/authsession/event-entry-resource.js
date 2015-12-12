(function(angular) {
	
	function eventEntryResourceFactory($resource, session) {
		var applicationRoot = '/AuroraTMS/api/tournamententries/:tournamentEntryId';
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
			}, 
			// public method for getting entries of a tournament
			'query' : {   
				method : 'GET',
				isArray : true,
				url : applicationRoot
			},
			// private (see headers with Bearer) being passed
			'list' : {
				method : 'GET',
				isArray : true,
				headers : headers,
				url : applicationRoot
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

		var tournamentEntryResource = $resource(url, {
			tournamentEntryId : '@tournamentEntryId',
			id : '@id',
			offset : '@offset',
			max : '@max',
			username: '@username'
		}, actions);

		return tournamentEntryResource;
	}

	eventEntryResourceFactory.$inject = [ '$resource', 'session' ];

	angular.module('auroraTmsApp').factory('eventEntryResource',
			eventEntryResourceFactory);

})(angular);