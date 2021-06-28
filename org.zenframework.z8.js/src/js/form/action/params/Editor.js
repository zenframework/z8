Z8.define('Z8.form.action.params.Editor', {
	extend: 'Z8.form.Fieldset',

	plain: true,
	parameters: null,
	
	htmlMarkup: function() {
		var controls = [];
		for (var i = 0; i < this.parameters.length; ++i) {
			var parameter = this.parameters[i];
			var control = this.createFieldControl(field);
			controls.add(control);
		}

		this.colCount = 1;
		this.controls = controls;
		return this.callParent();
	},
	
	createFieldControl: function(parameter) {
		var control = Z8.form.Helper.createControl(parameter);

		control.setReadOnly(false);
		control.label.align = 'left';
		control.placeholder = 'Не задано';
		if(!Z8.isEmpty(parameter.value))
			control.initValue(parameter.value);

		return control;
	},
	
	getParameters: function() {
		var controls = this.controls;
		var parameters = []
		for (var i = 0; i < controls.length; ++i) {
			var control = controls[i];
			var parameter;
		}
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
				
			var windowCfg = { header: action.text, icon: 'fa-list-alt', controls: [editor],
								handler: handler, scope: scope };
			return new Z8.window.Window(windowCfg);
		}
	}
});