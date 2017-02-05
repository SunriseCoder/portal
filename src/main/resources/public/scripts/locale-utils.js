var Locales = {
    _keys: undefined,

    _load: function() {
        var xhrObject = new XMLHttpRequest();
        xhrObject.overrideMimeType("application/json");
        // synchronous AJAX is deprecated, but important to load resources
        xhrObject.open('GET', '/i18n/default.json', false);
        xhrObject.send();
        _keys = JSON.parse(xhrObject.responseText);
    },

    i18n: function(key) {
        var value = _keys[key];

        if (!value) {
            console.log('Cannot find i18n message for ' + key);
            return key;
        }

        return value;
    },

    writeTitle(key) {
        var title = document.getElementsByTagName("title")[0];
        var value = this.i18n(key);
        title.text = value;
    },

    write(id, key) {
        var element = document.getElementById(id);
        var value = this.i18n(key);
        element.innerHTML = value;
    }
}

Locales._load();
