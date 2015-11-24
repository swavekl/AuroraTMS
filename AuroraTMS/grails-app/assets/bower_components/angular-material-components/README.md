# Angular Material Components

## Additional components for [Angular Material](https://material.angularjs.org/)

[Angular Material](https://material.angularjs.org/) still lacks some components, like date picker and select dropdown. 
This project aims to bring those missing components.

Some components are inspired by [Lumx](http://ui.lumapps.com/), using 
[Angular Material](https://material.angularjs.org/) features and dropping JQuery dependency.

## Live demo

Live demo is [available on rawgit](https://rawgit.com/Toilal/angular-material-components/master/demo/index.html).

## Get ready

1. Install with bower

    ```shell
    bower install angular-material-components --save
    ```

2. Register module dependency

    ```js
    angular.module('app', ['ngMaterial.components']);
    ```

## Components

### Date Picker
A date picker inspired by [Lumx](http://ui.lumapps.com/).

```html
<mdc-date-picker model="date" label="Pick a date" dialog-md-theme="pickerTheme"/>
```
