var Z8 = {
	classes: {},

	apply: function(object, config) {
		object = object || {};

		for(var name in config)
			object[name] = config[name];

		return object;
	},

	addMembers: function(prototype, config) {
		for(var name in config) {
			var member = config[name];
			Z8.addMember(prototype, member, name);
		}
	},

	addMember: function(prototype, member, name) {
		if(member === undefined)
			return;

		if(typeof member == 'function' && member.$owner == null) {
			var previous = prototype[name];
			if(previous != null)
				member.$previous = previous;

			member.$owner = prototype;
		}

		prototype[name] = member;
	},

	addCallParent: function(prototype) {
		var callParent = function() {
			return this.callParent.caller.$previous.apply(this, arguments);
		};

		Z8.addMember(prototype, callParent, 'callParent');
	},

	newConstructor: function() {
		return function constructor() {
			return this.constructor.apply(this, arguments);
		};
	},

	define: function(className, config) {
		var cls = Z8.newConstructor();

		var single = config.single;
		delete config.single;

		var extend = config.extend;
		delete config.extend;

		var mixins = config.mixins;
		delete config.mixins;

		var statics = config.statics;
		delete config.statics;

		var shortName = config.shortClassName;
		delete config.shortClassName;

		var baseCls = null;

		if(extend != null) {
			baseCls = String.isString(extend) ? Z8.classes[extend] : extend;
			if(baseCls == null)
				throw 'Superclass not found: "' + extend + '"'; 
		} else
			baseCls = function(param) { return Z8.addMembers(this, param); };

		var basePrototype = baseCls.prototype;
		var prototype = cls.prototype = Object.create(basePrototype);
		prototype.self = prototype;
		prototype.$className = className;
		if(shortName != null)
			prototype.$shortClassName = shortName;
		cls.superclass = prototype.superclass = basePrototype;

		Z8.addMembers(prototype, config);
		Z8.addCallParent(prototype);
		Z8.mixin(prototype, mixins);
		Z8.apply(cls, statics);

		if(!single) {
			Z8.namespace(className, cls);
			Z8.namespace(shortName, cls);
		}

		return cls;
	},

	mixin: function(prototype, mixins) {
		if(mixins == null)
			return;

		mixins = Array.isArray(mixins) ? mixins : [mixins];

		for(var i = 0, length = mixins.length; i < length; i++) {
			var mixin = Z8.classes[mixins[i]];

			if(mixin == null)
				throw 'Mixin not found: "' + mixins[i] + '"';

			mixin = mixin.prototype;
			var mixinId = mixin.mixinId || mixin.$shortClassName;

			if(mixinId == null)
				throw 'Mixed in class must have mixinId: "' + mixin.$className + '"';

			for(var name in mixin) {
				if(name != 'mixinId' && name != '$shortClassName' && prototype[name] === undefined)
					prototype[name] = mixin[name];
			}

			if(prototype.mixins == null)
				prototype.mixins = {};

			prototype.mixins[mixinId] = mixin;
		}
	},

	namespace: function(name, cls) {
		if(name == null)
			return;

		var root = window;
		var parts = name.split('.');
		var length = parts.length - 1;
		var last = parts[length];

		for(var i = 0; i < length; i++) {
			var part = parts[i];
			var newRoot = root[part];
			if(newRoot == null)
				newRoot = root[part] = {};
			root = newRoot;
		}

		root[last] = cls;
		Z8.classes[name] = cls;

		return cls;
	},

	create: function(cls, config) {
		if(String.isString(cls)) {
			var name = cls;
			cls = Z8.classes[name];
			if(cls == null)
				throw 'Class not found: "' + name + '"';
		}

		var object = Object.create(cls.prototype);
		cls.call(object, config);
		return object;
	},

	callback: function(callback/*, scope, args */) {
		if(callback == null)
			return;

		var args = [];
		var scope = null;

		if(typeof callback == 'object') {
			scope = callback.scope;
			callback = callback.fn || callback.callback;
			args = Array.prototype.slice.call(arguments, 1);
		} else {
			scope = arguments[1];
			args = Array.prototype.slice.call(arguments, 2);
		}

		if(callback == null || scope != null && scope.disposed)
			return;

		callback.apply(scope, args);
	},

	isEmpty: function(value) {
		if(value === undefined || value === null || value === '')
			return true;
		if(Array.isArray(value))
			return value.length == 0;
		return false;
	}
};

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

})();