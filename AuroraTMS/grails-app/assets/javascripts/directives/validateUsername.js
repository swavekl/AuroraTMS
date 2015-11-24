(function(angular) {
//
//	angular.directive('validateUsername',
//			function($q, userService) {
//
//				return {
//					restrict : "A",
//					require : 'ngModel',
//					link : function(scope, element, attrs, ctrl) {
//
//						ctrl.$asyncValidators.username = function(modelValue,
//								viewValue) {
//							return $q(function(resolve, reject) {
//								userService.checkUnique(viewValue).then(
//										function() {
//											resolve();
//										}, function() {
//											reject();
//										});
//							});
//						};
//					}
//				};
//			});

})(angular);