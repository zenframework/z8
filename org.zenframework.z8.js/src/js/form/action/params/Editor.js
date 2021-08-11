Z8.define('Z8.form.action.params.Editor', {
	extend: 'Z8.form.Fieldset',

	plain: true,
	parameters: null,

	htmlMarkup: function() {
		var controls = [];
		for (var i = 0; i < this.parameters.length; ++i) {
			var parameter = this.parameters[i];
			var control = this.createFieldControl(parameter);
			controls.add(control);
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

	getParameters: function() {
		var controls = this.controls;
		
		var values = [];
		for (var i = 0; i < controls.length; ++i) {
			var control = controls[i];
			var value;
			var fields = control.param.field.query.fields;
			if (control instanceof Z8.form.field.Combobox && Array.isArray(fields)) {
				var rec = control.getSelectedRecord();
				value = (rec != null && fields.length >= 2) ? rec.get(fields[1].name) : guid.Null;
			} else
				value = control.getValue();
			
			values.add({ id: control.param.id, value: value });
		}
		return values;
	},

	statics: {
		getParametersEditor: function(action) {
			if(!Z8.isEmpty(action.parameters)) {
				var editorCfg = { flex: 1, parameters: action.parameters };
				return new Z8.form.action.params.Editor(editorCfg);
			} else {
				return null;
			}
		},

		getParametersWindow: function(action, handler, scope) {
			var editor = this.getParametersEditor(action);

			if(editor == null)
				return null;

			var winHandler = function(window, success) {
				if (success) {
					var params = editor.getParameters();
					handler.call(scope, params);
				}
			}

			var windowCfg = { header: action.text, icon: 'fa-list-alt', controls: [editor],
								handler: winHandler, scope: null };
			return new Z8.window.Window(windowCfg);
		}
	}
});