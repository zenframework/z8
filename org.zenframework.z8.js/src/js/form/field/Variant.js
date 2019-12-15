Z8.define('Z8.form.field.Variant', {
	extend: 'Z8.form.field.Control',

	tabIndex: -1,

	constructor: function(config) {
		var config = config || {};
		config.controls = {};
		this.callParent(config);
	},

	getControls: function() {
		return Object.values(this.controls);
	},

	subcomponents: function() {
		return this.getControls();
	},

	createControl: function(type, cls) {
		var control = this.controls[type] = Z8.create(cls, { cls: 'display-none', label: this.label });
		control.on('change', this.onControlChange, this);
		return control;
	},

	controlMarkup: function() {
		var markup = [
			this.createControl(Type.String, 'Z8.form.field.Text').htmlMarkup(),
			this.createControl(Type.Date, 'Z8.form.field.Date').htmlMarkup(),
			this.createControl(Type.Datetime, 'Z8.form.field.Datetime').htmlMarkup(),
			this.createControl(Type.Integer, 'Z8.form.field.Integer').htmlMarkup(),
			this.createControl(Type.Float, 'Z8.form.field.Float').htmlMarkup()
		];

		this.label = false;
		return markup;
	},

	setAutoSave: function(autoSave) {
		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++)
			controls[i].setAutoSave(autoSave);
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly);

		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++)
			controls[i].setReadOnly(readOnly);
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++)
			controls[i].setEnabled(readOnly);
	},

	setType: function(type) {
		if(this.type == type)
			return;

		this.type = type;

		if(this.variant != null)
			this.variant.hide();

		this.variant = this.controls[type];

		if(this.variant != null) {
			this.variant.initValue(this.getValue(), this.getDisplayValue());
			this.variant.show();
		}
	},

	setValue: function(value, displayValue) {
		this.callParent(value, displayValue);

		if(!this.isControlChange && this.variant != null)
			this.variant.initValue(value, displayValue);
	},

	onControlChange: function(control, newValue, oldValue) {
		this.isControlChange = true;
		this.setValue(newValue, control.getDisplayValue());
		this.isControlChange = false;
	}
});