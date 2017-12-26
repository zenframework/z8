Z8.define('Z8.data.Batch', {
	extend: 'Z8.Object',

	model: null,
	store: null,
	singleMode: false,

	getModel: function() {
		if(this.store != null)
			this.model = this.store.getModel();

		var model = this.model;

		if(String.isString(model))
			model = this.model = Z8.classes[this.model];

		if(typeof model == 'function')
			model = this.model = model.prototype;

		return model;
	},

	getServerData: function(records, action) {
		var data = [];
		for(var i = 0, length = records.length; i < length; i++)
			data.push(records[i].getServerData(action));
		return data;
	},

	create: function(records, callback, options) {
		this.execute('create', records, callback, options);
	},

	update: function(records, callback, options) {
		this.execute('update', records, callback, options);
	},

	destroy: function(records, callback, options) {
		this.execute('destroy', records, callback, options);
	},

	execute: function(action, records, callback, options) {
		var singleMode = this.singleMode;

		if(singleMode && Array.isArray(records) && records.length != 1)
			throw 'Multiple records can not be saved in single mode';

		records = Array.isArray(records) ? records : [records];

		var model = this.getModel();
		var data = this.getServerData(records, action);

		var params = {
			request: model.getName(),
			action: action,
			data: data,
			fields: Model.getFieldNames(model.getRequestFields()),
			link: model.getLink(),
			query: model.getQuery()
		};

		Z8.apply(params, options);

		this.beginTransaction();

		var requestCallback = function(response, success) {
			if(success) {
				var data = response.data || [];

				for(var i = 0, length = records.length; i < length; i++)
					records[i].onAction(action, data[i]);

				this.commit();

				Z8.callback(callback, singleMode ? records[0] : records, true);

				for(var i = 0, length = records.length; i < length; i++)
					records[i].afterAction(action);
			} else {
				this.rollback();
				Z8.callback(callback, singleMode ? records[0] : records, false);
			}
		};

		HttpRequest.send(params, { fn: requestCallback, scope: this });
	},

	beginTransaction: function() {
		if(this.store != null)
			this.store.beginTransaction();
	},

	commit: function() {
		if(this.store != null)
			this.store.commit();
	},

	rollback: function() {
		if(this.store != null)
			this.store.rollback();
	}
});