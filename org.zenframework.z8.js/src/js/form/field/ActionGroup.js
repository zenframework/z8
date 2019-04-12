Z8.define('Z8.form.field.ActionGroup', {
	extend: 'Z8.form.field.Control',

	constructor: function(config) {
		config = config || {};
		config.actions = [];
		config.label = false;

		this.callParent(config);
	},

	add: function(action) {
		this.actions.push(action);
	},

	subcomponents: function() {
		return [this.actionGroup];
	},

	controlMarkup: function() {
		var actionGroup = this.actionGroup = new Z8.Container({ cls: 'action-group', items: this.actions });
		return actionGroup.htmlMarkup();
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly);
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);
		if(this.actionGroup != null)
			this.actionGroup.setEnabled(enabled);
	},

	setActive: function(active) {
		this.callParent(active);
		this.actionGroup.setActive(active);
	}
});