var CookieUtils = {
    get: function(key) {
        var cookieMap = this._getCookieMap();
        var value = this._findCookieValue(cookieMap, key);
        return value;
    },

    put: function(key, value, expires) { // expires in seconds
        var expirationValue = this._buildExpirationValue();
        document.cookie = key + "=" + value + "; " + expirationValue;
    },

    _getCookieMap: function() {
        var cookieMap = [];
        var cookies = document.cookie;
        var splittedCookie = cookies.split(";");
        for (var i = 0; i < splittedCookie.length; i++) {
            var pair = splittedCookie[i].trim();
            var splittedPair = pair.split("=");
            if (splittedPair.length == 2) {
                cookieMap.push({"key" : splittedPair[0], "value" : splittedPair[1]});
            }
        }
        return cookieMap;
    },

    _findCookieValue: function(cookieMap, key) {
        for (var i = 0; i < cookieMap.length; i++) {
            var pair = cookieMap[i];
            if (pair.key == key) {
                return pair.value;
            }
        }
        return undefined;
    },

    _buildExpirationValue: function(expires) {
        if (expires) {
            var date = new Date();
            date.setTime(date.getTime() + expires * 1000);
        } else {
            var date = new Date(Date.UTC(9999, 0, 1));
        }
        var value = date.toUTCString();
        return value;
    }
}
