(function(angular) {

	/**
	 * directive which checks if user has access to this state
	 * If not the element containing this link will be removed from DOM
	 * use:
	 * <md-list-item ng-click="navigateTo('tournament.list')" authorized>
	 * or on button:
	 * <md-button class="md-raised" ui-sref="home" authorized>
	 * @returns
	 */
	function authorized(session, $state, removeElement) {
		return {
			restrict : 'A',
			link : function(scope, element, attributes) {
				// check ng-click first
				var stateName = null;
				var navigateTo = attributes.ngClick;
				if (navigateTo && navigateTo.length > 0) {
					stateName = navigateTo.substr("navigateTo('".length);
					stateName = stateName.substr(0, stateName.length - 2);
				}
				
				// if not ng-clic then perhaps 'ui-sref' on a button
				if (stateName == null) {
					stateName = attributes.uiSref;
//					console.log ('ui-sref stateName ' + stateName);
				}

				if (stateName != null) {
					var state = $state.get(stateName);
					if (state) {
						if (state.data && state.data.roles) {
							var hasAccess = false;
							var stateRequiredRoles = state.data.roles;
							//console.log('stateRequiredRoles ' + stateRequiredRoles);
							var currentUserRoles = session.getRoles();
							//console.log('currentUserRoles = ' + currentUserRoles);
							if (currentUserRoles) {
								for (var i = 0; i < currentUserRoles.length
										&& !hasAccess; i++) {
									var userRole = currentUserRoles[i];
									for (var k = 0; k < stateRequiredRoles.length; k++) {
										var stateRole = stateRequiredRoles[k];
										if (stateRole == userRole) {
											hasAccess = true;
											break;
										}
									}
								}
							}
							
							// hide element that you don't have access to
							//console.log ('User ' + session.getUser() + ((hasAccess) ? " has" : " doesn't have") + ' access to state ' + stateName);
							
							// remove element and its children
							if (!hasAccess) {
				                angular.forEach(element.children(), function (child) {
				                    removeElement(child);
				                });
				                removeElement(element);
				            }
						}
					}
				}

			}
		};
	}
	;

	authorized.$inject = [ 'session', '$state', 'removeElement' ];

	angular.module('auroraTmsApp')
	.directive("authorized", authorized)
	.constant('removeElement', function(element){
	    element && element.remove && element.remove();
	});

})(angular);