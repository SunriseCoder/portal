var HttpUtils = {
    get: function(caller, url, success, error) {
        var xhttp = new XMLHttpRequest();
        xhttp.onreadystatechange = function() {
            if (this.readyState != 4) {
                return;
            }
            if (this.status == 200) {
                success(caller, this);
            } else {
                error(caller, this);
            }
        };
        xhttp.open("GET", url, true);
        xhttp.send();
    },

    post: function(url, params, async, successHandler, errorHandler, that) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', url, async);
        if (async) {
            xhr.onload = function () {
                if (successHandler !== undefined && xhr.status === 200) {
                    successHandler(that);
                } else if (errorHandler !== undefined) {
                    errorHandler(that);
                }
            };
        }
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhr.send(params);
        return xhr;
    },

    upload: function(url, formData, progressBar, success, error) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', url, true);
        xhr.onload = function () {
            if (xhr.status === 200) {
                success(progressBar);
            } else {
                error(progressBar);
            }
        };
        xhr.upload.addEventListener("progress", function(e) {
            var percent = parseInt(e.loaded / e.total * 100);
            //TODO Refactor this. Make Uploader as single HTML Component and call method like setProgress()
            progressBar.getElementsByClassName("bar")[0].style = "width: " + percent + "%;";
            progressBar.getElementsByClassName("label")[0].innerText = percent + "%";
        });
        xhr.send(formData);
    }
}
