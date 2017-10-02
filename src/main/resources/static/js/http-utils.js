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

    postAjax: function(url, params, async, successHandler, errorHandler, that) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', url, async);
        if (async) {
            xhr.onload = function () {
                var response = JSON.parse(xhr.response);
                if (successHandler !== undefined && xhr.status === 200 && response.status === 'Ok') {
                    successHandler(that, response);
                } else if (errorHandler !== undefined) {
                    errorHandler(that, response);
                }
            };
        }
        xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        xhr.send(params);
        return xhr;
    }
}
