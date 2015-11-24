<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="description" content="">
<meta name="viewport"
	content="width=device-width initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>Aurora TMS</title>

<asset:stylesheet href="application.css" />
</head>
<body ng-app="auroraTmsApp" ><%--
       <div ui-view="header"></div>
       <div ui-view="content"></div>
       <div ui-view="footer"></div>

	--%>

<div ng-controller="AppCtrl" layout="column" layout-fill>
  <section layout="row" flex>
    <md-sidenav md-swipe-left="onSwipeLeft()" class="md-sidenav-left md-whiteframe-z2" md-component-id="left" md-is-locked-open="$media('gt-md')">
      <md-toolbar class="md-theme-indigo">
        <h1 class="md-toolbar-tools">Menu</h1>
      </md-toolbar>
      <md-content class="layout-fill" ng-controller="LeftCtrl" layout="column" layout-align="start start">
       <md-button ng-repeat="item in items" ui-sref="{{item.location}}"> 
        <span>{{item.label}}</span>
       </md-button>
        <md-button ng-click="close()" class="md-primary" hide-gt-md>
          Close Sidenav Left
        </md-button>
        <p hide-md show-gt-md>
        New Content in paragraph
        </p>
      </md-content>
    </md-sidenav>
    <md-content flex class="md-padding">
      <div layout="column" layout-fill layout-align="center center">
        <p>
        The left sidenav will 'lock open' on a medium (>=960px wide) device.
        </p>
        <div>
          <md-button ng-click="toggleLeft()"
            class="md-primary" hide-gt-md>
            Toggle left
          </md-button>
        </div>
        <div>
          <md-button ng-click="toggleRight()"
            class="md-primary">
            Toggle right
          </md-button>
        </div>
      </div>
    </md-content>
    <md-sidenav class="md-sidenav-right md-whiteframe-z2" md-component-id="right">
      <md-toolbar class="md-theme-light">
        <h1 class="md-toolbar-tools">Sidenav Right</h1>
      </md-toolbar>
      <md-content ng-controller="RightCtrl" class="md-padding">
        <md-button ng-click="close()" class="md-primary">
          Close Sidenav Right
        </md-button>
      </md-content>
    </md-sidenav>
  </section>
</div>

<asset:javascript src="application.js" />
</body>


</html>

