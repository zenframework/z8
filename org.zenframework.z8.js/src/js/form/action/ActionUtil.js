Z8.define('Z8.form.action.Util', {
	shortClassName: 'ActionUtil',
	
	statics: {
		setParameterValue: function(action, name, value) {
			var parameters = action.parameters;
			for (var i = 0; i < parameters.length; ++i) {
				var parameter = parameters[i];
				if(parameter.id == name) {
					parameter.value = value;
					return;
				}
			}
		},
		
		hasVisibleParameters: function(action) {
			var parameters = action.parameters;
			for (var i = 0; i < parameters.length; ++i) {
				if(parameters[i].visible)
					return true;
			}
			
			return false;
		},
			
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
					editor.setValues();
					handler.call(scope, action);
				}
			}

			var windowCfg = { header: action.text, icon: 'fa-list-alt', controls: [editor],
								handler: winHandler, scope: null };
			return new Z8.window.Window(windowCfg);
		},

		getParameters: function(action) {
			var parameters = action.parameters;
			
			var values = [];
			for (var i = 0; i < parameters.length; ++i) {
				var parameter = parameters[i];
				values.add({ id: parameter.id, value: parameter.value });
			}
			return values;
		},

		getExecutableParameters: function(action) {
			var parameters = action.parameters;
			
			var values = {};
			for (var i = 0; i < parameters.length; ++i) {
				var parameter = parameters[i];
				values[parameter.id] = parameter.value;
			}
			return values;
		}
	}
});