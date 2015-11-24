/**
 * Filter for use with ng-repeat to repeat a fixed or variable number of times
 * 
 * Use: <span ng-repeat="n in tournament.starLevel | range">
 * or   <span ng-repeat="n in 5 | range"> 
 */
angular.module('auroraTmsApp')
  .filter('range', function(){
    return function(n) {
      var res = [];
      for (var i = 0; i < n; i++) {
        res.push(i);
      }
      return res;
    };
  });