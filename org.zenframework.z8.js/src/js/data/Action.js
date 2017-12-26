Z8.define('Z8.data.Action', {
	extend: 'Z8.Object',
/*
	text: '', 
	id: guid.Null, 
	target: null,
	global: false,
*/
	statics: {
		actionsQueue: []
	},

	target: 'ru.ivk.postfactor.desktops.Actions',

	execute: function(options) {
		this.options = options;
		this.getParameters(options);
	},

	getParameters: function(options) {
		var okCallback = function(parameters) {
			var records = options.records || [];
			this.fireEvent('beforeaction', this, options);
			this.executeAction(records, parameters);
		};

		var cancelCallback = function() {
			Z8.callback(this.options, this, null, false);
		};

		if(this.form != null) {
			var records = options.records || [];
			var form = {};
			var type = this.form;

			if(typeof this.form == 'object') {
				type = form.type;
				delete form.type;
				form = Z8.apply({}, this.form);
			}

			form.records = records;
			var form = Z8.create(type, form);
			form.on('ok', okCallback, this);
			form.on('cancel', cancelCallback, this);
			form.show();
		} else
			okCallback.call(this, null);
	},

	executeAction: function(records, parameters) {
		parameters = Z8.apply(this.options.parameters || {}, parameters);

		var params = {
			xaction: 'command',
			request: this.target,
			command: this.command || this.id, 
			parameters: parameters, 
			data: this.global ? [] :  this.getRecordIds(records)
		};

		var callback = function(response, success) {
			Z8.data.Action.actionsQueue.shift();
			Z8.callback(this.options, this, response, success);
			this.fireEvent('action', this, success);
		};

		Z8.data.Action.actionsQueue.push(this);

		this.delayedRequest(params, callback);
	},

	delayedRequest: function(params, callback) {
		if(Z8.data.Model.activeSaves != 0 || Z8.data.Action.actionsQueue[0] != this) {
			this.task = this.task || new Z8.util.DelayedTask();
			this.task.delay(100, this.delayedRequest, this, params, callback);
		} else
			HttpRequest.send(params, { fn: callback, scope: this });
	},

	getRecordIds: function(records) {
		var result = [];

		for(var i = 0; i < records.length; i++)
			result.push(records[i].id);

		return result;
	}
});