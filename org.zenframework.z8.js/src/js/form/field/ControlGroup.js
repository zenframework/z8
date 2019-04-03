Z8.define('Z8.form.field.ControlGroup', {
	extend: 'Z8.form.field.Control',

	constructor: function(config) {
		config = config || {};
		config.controls = [];
		config.cls = DOM.parseCls(this.cls).pushIf('group');

		this.callParent(config);
	},

	subcomponents: function() {
		return [this.section];
	},

	controlMarkup: function() {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			control.label = false;
			control.on('change', this.onChange, this);
		}

		var section = this.section = new Z8.form.Fieldset({ controls: controls, colCount: this.colCount, plain: true, readOnly: this.isReadOnly(), enabled: this.isEnabled() });
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