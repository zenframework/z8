Z8.define('Z8.form.action.Action', {
	extend: 'Z8.button.Button',

	mixins: ['Z8.form.field.Field'],

	isAction: true,
	push: true,

	constructor: function(config) {
		this.callParent(config);

		var action = this.action;

		if(action != null) {
			this.handler = action.handler || this.handler || this.onAction;
			this.scope = action.scope || this.scope || this;
		}
	},

	isValid: function() {
		return true;
	},

	validate: function() {},

	isReadOnly: function() {
		return false;
	},

	setReadOnly: function() {
	},

	onAction: function(button) {
		var action = this.action;

		if(!Z8.isEmpty(action.parameters))
			this.requestActionParameters();
		else
			this.runAction();
	},

	requestActionParameters: function() {
		this.runAction();
	},

	runAction: function(callback) {
		if(!this.isBusy())
			this.setBusy(true);

		var action = this.action;

		var record = this.getRecord();

		var params = {
			request: action.request,
			action: 'action',
			name: action.name,
			records: (record != null && !record.phantom) ? [record.id] : null,
			parameters: action.parameters
		};

		var sendCallback = function(response, success) {
			this.setBusy(false);
			this.onActionComplete(record, response, success);
			Z8.callback(callback, response, success);
		};

		HttpRequest.send(params, { fn: sendCallback, scope: this });
	},

	onActionComplete: function(record, response, success) {
		if(success && record != null && this.getRecord() == record) {
			var reloadCallback = function(record, success) {
				this.setBusy(false);
				if(this.form != null)
					this.form.loadRecord(record);
				this.fireEvent('complete', this, record);
			};
			record.reload({ fn: reloadCallback, scope: this });
		} else
			this.setBusy(false);
	}
});