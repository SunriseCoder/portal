var Ajax = {
	call: function(object, url, success, error) {
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState != 4) {
				return;
			}
			if (this.status == 200) {
				success(object, this);
			} else {
				error(object, this);
			}
		};
		xhttp.open("GET", url, true);
		xhttp.send();
	}
}
