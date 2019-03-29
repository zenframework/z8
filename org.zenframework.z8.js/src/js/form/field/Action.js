Z8.define('Z8.form.field.Action', {
	extend: 'Z8.button.Button',

	mixins: ['Z8.form.field.Field'],

	isAction: true,

	constructor: function(config) {
		var action = config;
		var type = action.type;
		config = { name: action.name, text: action.header, tooltip: action.description, icon: action.icon, primary: type == 'primary', success: type == 'success', danger: type == 'danger', action: action, handler: this.onAction, scope: this };
		this.callParent(config);
	},

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('action');
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
		this.setBusy(true);

		var action = this.action;

		if(!Z8.isEmpty(action.parameters))
			this.requestActionParameters();
		else
			this.runAction();
	},

	requestActionParameters: function() {
		this.runAction();
	},

	runAction: function() {
		var action = this.action;

		var record = this.getRecord();

		var params = {
			request: action.request,
			action: 'action',
			name: action.name,
			records: (record != null && !record.phantom) ? [record.id] : null,
			parameters: action.parameters
		};

		var callback = function(response, success) {
			this.setBusy(false);
			this.onActionComplete(record, response, success);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onActionComplete: function(record, response, success) {
		if(success && this.getRecord() == record) {
			var reloadCallback = function(record, success) {
				this.setBusy(false);
				this.form.loadRecord(record);
			};
			record.reload({ fn: reloadCallback, scope: this });
		}
	}
});