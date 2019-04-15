Z8.define('Z8.form.field.Group', {
	extend: 'Z8.form.field.Control',

	constructor: function(config) {
		config = config || {};
		config.controls = [];
		this.callParent(config);
	},

	subcomponents: function() {
		return [this.section];
	},

	add: function(action) {
		this.controls.push(action);
	},

	controlMarkup: function() {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(!control.isListbox)
				control.label = false;
			control.on('change', this.onChange, this);
		}

		var section = this.section = new Z8.form.Fieldset({ cls: 'field-group', controls: controls, colCount: this.colCount, plain: true, readOnly: this.isReadOnly(), enabled: this.isEnabled() });
		return section.htmlMarkup();
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly);
		if(this.section != null)
			this.section.setReadOnly(readOnly);
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);
		if(this.section != null)
			this.section.setEnabled(enabled);
	},

	setActive: function(active) {
		this.callParent(active);

		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++)
			controls[i].setActive(active);
	},

	focus: function(select) {
		return this.isEnabled() ? this.section.focus() : false;
	},

	isValid: function() {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			if(!controls[i].isValid())
				return false;
		}
		return true;
	},

	validate: function() {
		this.setValid(this.isValid());
	},

	onChange: function() {
		this.validate();
	}
});