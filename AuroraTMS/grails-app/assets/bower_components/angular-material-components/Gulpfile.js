'use strict';

var gulp = require('gulp');

var $ = require('gulp-load-plugins')();
var lazypipe = require('lazypipe');

gulp.task('default', ['build']);

gulp.task('build', ['build:js', 'build:js-min', 'build:css', 'build:css-min']);

var rubySassOptions = {'sourcemap=none': true};

gulp.task('templates', function() {
  return gulp.src('src/**/*.html')
    .pipe($.angularTemplatecache({
      standalone: true,
      module: 'ngMaterial.components.templates'
    }))
    .pipe($.ngAnnotate())
    .pipe(gulp.dest('.tmp/templates'));
});

var buildJs = lazypipe()
  .pipe($.angularFilesort)
  .pipe($.sourcemaps.init)
  .pipe($.wrap, '(function(){<%= contents %>})();')
  .pipe($.concat, 'angular-material-components.js');

gulp.task('build:js', ['templates'], function() {
  return gulp.src(['./src/**/*.js', '.tmp/**/*.js'], {base: '.'})
    .pipe(buildJs())
    .pipe($.sourcemaps.write())
    .pipe(gulp.dest('./dist/'));
});

gulp.task('build:js-min', ['templates'], function() {
  return gulp.src(['./src/**/*.js', '.tmp/**/*.js'], {base: '.'})
    .pipe(buildJs())
    .pipe($.uglify())
    .pipe($.rename('angular-material-components.min.js'))
    .pipe($.sourcemaps.write())
    .pipe(gulp.dest('./dist/'));
});

gulp.task('build:css', function() {
  return gulp.src('./src/**/*.scss')
    .pipe($.rubySass(rubySassOptions))
    .pipe($.concat('angular-material-components.css'))
    .pipe(gulp.dest('./dist/'));
});

gulp.task('build:css-min', function() {
  return gulp.src('./src/**/*.scss')
    .pipe($.rubySass(rubySassOptions))
    .pipe($.concat('angular-material-components.min.css'))
    .pipe($.csso())
    .pipe(gulp.dest('./dist/'));
});

gulp.task('watch', ['build:js', 'build:css'], function () {
  gulp.watch([
    './src/**/*.js',
    '.tmp/**/*.js',
    './src/**/*.html'
  ], ['build:js']);
  gulp.watch([
      './src/**/*.scss'
    ], ['build:css']);
});
