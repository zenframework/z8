(function () {
	var DEFAULT_LOCALE = 'ru';

	Z8.locales = {};

	var i18n = (function (defaultLocale) {
		var locale = window._LOCALE_ || defaultLocale;

		function getLocale() {
			return locale;
		}

		function toString(variable) {
			return typeof variable === 'string' ? variable
				: (typeof variable === 'number' ? '' + variable
					: JSON.stringify(variable));
		}

		function load(locale, messages) {
			if (messages != null)
				Z8.locales[locale] = Z8.apply(Z8.locales[locale], messages);
		}

		function formatMessage(message, data) {
			// Replace {variables}'s
			message = message.replace(/{[0-9a-zA-Z-_. ]+}/g, function (name) {
				name = name.replace(/^{([^}]*)}$/, '$1');
				return toString(data[name]);
			});
			return message;
		}

		function getMessages(){
			return Z8.locales[getLocale()];
		}

		// TODO: return unknown keys?
		function getMessage(key, data) {
			var message = getMessages()[key];
			return message == null ? '${' + key + '}' : (data != null ? formatMessage(message, data) : message);
		}

		return {
			getMessage: getMessage,
			load: load,
			getLocale: getLocale
		}
	})(DEFAULT_LOCALE)

	/**
	 * Get locale message by key.
	 *
	 * Usage examples:
	 * 		Messages:
	 * 			{
	 * 			 	'key1': 'Message 1',
	 * 			 	'key2': 'Message with array parameter {0} and {1}',
	 * 			 	'key3': 'Message with object parameter {prop1} and {prop2}',
	 * 			}
	 *		Call:
	 *			Z8.$('key1') 												-> 'Message 1'
	 *			Z8.$('key2', ['value_1', 'value_2']) 						-> 'Message with array parameter value_1 and value_2'
	 *			Z8.$('key3', {prop1: 'prop_value_1', prop2: 'prop_value_2'})-> 'Message with object parameter prop_value_1 and prop_value_2'
	 *
	 * @param key message key
	 * @param data message variables (Array or Object)
	 * @returns {string}
	 */
	Z8.$ = function (key, data) {
		return i18n.getMessage(key, data);
	};

	Z8.loadMessages = function (locale, messages) {
		i18n.load(locale, messages);
	};

	Z8.loadNlsMessages = function() {
		new HttpRequest({ 
			defaultUrl: 'nls.json'
		}).send({
			session: Application.session,
			languages: navigator.languages,
		}, {
			fn: function(response, success) {
				if (!success)
					return;

				this.loadMessages(response.language, response.data);

				Application.NlsActual = true;
				Viewport.onLogin();
			}, scope: Z8
		});
	};
})();