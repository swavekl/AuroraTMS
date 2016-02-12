function showError ($mdDialog, httpResponse, title) {
	var message = httpResponse.statusText;
	if (httpResponse.data.length > 0 && httpResponse.data[0] == 'errors') {
		message = "";
		for (var i = 1; i < httpResponse.data.length; i++) {
			message += httpResponse.data[i];
			message += "\n";
		}
	}
	var alert = $mdDialog
	.alert()
	.title(title)
	.textContent('Error code: ' + httpResponse.status + ", Message: " + message)
	.ariaLabel(title)
	.ok('Close');

    $mdDialog
      .show( alert )
      .finally(function() {
        alert = undefined;
      });
}
