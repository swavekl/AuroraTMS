function showError ($mdDialog, httpResponse, title) {
	var alert = $mdDialog
	.alert()
	.title(title)
	.textContent('Error code: ' + httpResponse.status + ", Message " + httpResponse.statusText)
	.ariaLabel(title)
	.ok('Close');

    $mdDialog
      .show( alert )
      .finally(function() {
        alert = undefined;
      });
}
