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
<link rel="stylesheet"
	href="//fonts.googleapis.com/css?family=RobotoDraft:400,500,700,400italic" />
<link href="https://fonts.googleapis.com/icon?family=Material+Icons"
      rel="stylesheet"></head>
<body ng-app="auroraTmsApp" layout="column" ng-controller="MenuController" class="ng-cloak">
	<md-toolbar layout="row">
	<div class="md-toolbar-tools">
		<md-button class="md-icon-button" aria-label="Menu" ng-click="openLeftMenu()" hide-gt-md> 
				<ng-md-icon icon="menu" style="fill: white" size="30"></ng-md-icon> 
		</md-button>
		<h2>
			<span class="md-title">Aurora TMS (R)</span>
		</h2>
	</div>
	</md-toolbar>

	<div layout="row" flex md-swipe-left="closeLeftMenu()">
		<md-sidenav ui-view="menu" layout="column" class="md-sidenav-left md-whiteframe-z2"
			md-component-id="left" md-is-locked-open="$mdMedia('gt-md')">
		</md-sidenav>
		<div ui-view="content" layout="row" flex layout-padding></div>
	</div>
	<asset:javascript src="application.js" />
</body>
</html>

