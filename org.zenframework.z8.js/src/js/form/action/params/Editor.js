Z8.define('Z8.form.action.params.Editor', {
	extend: 'Z8.form.Fieldset',

	plain: true,
	parameters: null,

	htmlMarkup: function() {
		var controls = [];
		for (var i = 0; i < this.parameters.length; ++i) {
			var parameter = this.parameters[i];
			if(parameter.visible) {
				var control = this.createFieldControl(parameter);
				controls.add(control);
			}
		}

		this.colCount = 1;
		this.controls = controls;
		return this.callParent();
	},

	createFieldControl: function(parameter) {
		var control = Z8.form.Helper.createControl(parameter.field);

		control.setReadOnly(false);
		control.label.align = 'left';
		control.placeholder = 'Не задано';
		control.param = parameter;
		if(!Z8.isEmpty(parameter.value))
			control.initValue(parameter.value);

		return control;
	},

	setValues: function() {
		var controls = this.controls;
		
		var values = [];
		for (var i = 0; i < controls.length; ++i) {
			var control = controls[i];
			var value;
			var fields;
			if(control.param.field.query)
				fields = control.param.field.query.fields;
			if (control instanceof Z8.form.field.Combobox && Array.isArray(fields)) {
				var rec = control.getSelectedRecord();
				value = (rec != null && fields.length >= 2) ? rec.get(fields[1].name) : guid.Null;
			} else
				value = control.getValue();
			
			control.param.value = value;
		}
	}
});