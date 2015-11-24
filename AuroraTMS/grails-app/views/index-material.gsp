<!doctype html>
<html lang="en" ng-app="starterApp">
  <head>
	<asset:stylesheet href="application.css"/>
  </head>
  <body layout="column" ng-controller="AppCtrl">
    <md-toolbar layout="row">
      <button ng-click="toggleSidenav('left')" hide-gt-sm class="menuBtn">
        <span class="visuallyhidden">Menu</span>
      </button>
      <h1>Hello World</h1>
    </md-toolbar>
    <div layout="row" flex>
        <md-sidenav layout="column" class="md-sidenav-left md-whiteframe-z2" md-component-id="left" md-is-locked-open="$media('gt-sm')">
          
        </md-sidenav>
        <div layout="column" flex md-theme="green" id="content">
            <md-content layout="column" flex class="md-padding">
              <h2>another content 333</h2>
            </md-content>
        </div>
    </div>
    <!-- Angular Material Dependencies -->
	<asset:javascript src="application.js"/>
	<asset:javascript src="avatarService.js"/>
  </body>
  </html>