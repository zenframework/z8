var User = null;

Z8.define('Z8.application.User', {
	extend: 'Z8.Object',

	constructor: function(config) {
		this.callParent(config);

		try {
			var settings = this.settings;
			this.settings = Z8.isEmpty(settings) ? {} : JSON.decode(settings);
		} catch(e) {
			this.settings = {};
		}
	},

	getProperty: function(key, subkey) {
		var application = this.settings[Application.id];
		var section = application ? application[key] : null;
		return section != null ? (subkey != null ? section[subkey] : section) : null;
	},

	setProperty: function(key, subkey, value) {
		var settings = this.settings;
		var applicationId = Application.id;
		var application = settings[applicationId];

		if(application == null)
			settings[applicationId] = application = {};

		if(value != undefined) {
			var section = application[key];
			if(section == null)
				application[key] = section = {};
			section[subkey] = value;
		} else
			application[key] = subkey;
	},

	getFilter: function(key) {
		return new Z8.data.Filter(this.getProperty(key, 'filter'));
	},

	setFilter: function(key, filter) {
		this.setProperty(key, 'filter', filter != null ? filter.toJson() : undefined);
	},

	getPeriod: function(key) {
		return new Z8.data.Period(this.getProperty(key, 'period'));
	},

	setPeriod: function(key, period) {
		this.setProperty(key, 'period', period != null ? period.toJson() : undefined);
	},

	saveSettings: function() {
		var params = { request: 'settings', data: this.settings };
		HttpRequest.send(params);
	}
});