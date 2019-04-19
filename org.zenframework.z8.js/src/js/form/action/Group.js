Z8.define('Z8.form.action.Group', {
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
		return [this.group];
	},

	controlMarkup: function() {
		var group = this.group = new Z8.Container({ cls: 'action-group', items: this.actions });
		return group.htmlMarkup();
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly);
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);
		if(this.group != null)
			this.group.setEnabled(enabled);
	},

	setActive: function(active) {
		this.callParent(active);
		this.group.setActive(active);
	}
});