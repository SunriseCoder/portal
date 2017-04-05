var Locales = {
    _keys: undefined,

    _load: function() {
        var response = this._loadData('/i18n/default.json');
        _keys = JSON.parse(response);
    },

    _loadData: function(url) {
        var xhrObject = new XMLHttpRequest();
        xhrObject.overrideMimeType("application/json");
        // synchronous AJAX is deprecated, but important to load resources
        xhrObject.open('GET', url, false);
        xhrObject.send();
        var response = xhrObject.responseText;
        return response;
    },

    i18n: function(key) {
        var value = _keys[key];

        if (!value) {
            console.log('Cannot find i18n message for ' + key);
            return key;
        }

        return value;
    },

    writeTitle: function(key) {
        var title = document.getElementsByTagName("title")[0];
        var value = this.i18n(key);
        title.text = value;
    },

    write: function(id, key) {
        var element = document.getElementById(id);
        var value = this.i18n(key);
        element.innerHTML = value;
    }
}

Locales._load();
