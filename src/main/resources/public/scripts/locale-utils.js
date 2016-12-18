var Locales = {
	_keys: undefined,

	_load: function() {
		var xhrObject = new XMLHttpRequest();
        xhrObject.overrideMimeType("application/json");
        // synchrones AJAX deprecated, hier aber notwendig um sicherzustellen,
        // dass Texte da bevor Komponenten geladen werden
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
	}
}
Locales._load();
