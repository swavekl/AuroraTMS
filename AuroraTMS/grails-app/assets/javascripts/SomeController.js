(function (angular) {

  function SomeController(auth){

    // Do something if logged in
    if(auth.isLoggedIn()){
      // ...
    }
  }

  // Inject dependencies
  SomeController.$inject = ['auth'];

  // Export
  angular
    .module('app')
    .controller('SomeController', SomeController);

})(angular);