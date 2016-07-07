Z8.view.ParametersWindow = Ext.extend(Z8.Window, {
	parameters: null,
	
	width:400,
	resizable: false,
	modal: true, 

	layout: 'vbox',
	layoutConfig: { align: 'stretch' },

	constructor: function(config) {
		Ext.apply(this, config);

		this.parameters = this.parameters || [];
		
		this.height = this.minHeight = Math.max(3, this.parameters.length + 1) * 26 + 90;
		
		Z8.view.ParametersWindow.superclass.constructor.call(this);
	},
		
	initComponent: function() {
		Z8.view.ParametersWindow.superclass.initComponent.call(this);
		
		this.addEvents('ok', 'cancel', 'error');
		
		this.files = [];
		this.source = {};

		var parameters = this.parameters;
		var propertyNames = {};
		
		for(var i = 0; i < parameters.length; i++) {
			var parameter = parameters[i];
			
			if(parameter.serverType != Z8.ServerTypes.File) {
				propertyNames[parameter.text] = parameter.text;
				
				var value = null;
				
				if(parameter.queryId != null)
					value = { queryId: parameter.queryId, fieldId: parameter.fieldId, recordId: parameter.recordId };
				else if(parameter.fieldId != null && this.record != null)
					value = this.record.data[parameter.fieldId];
				
				if(value == null)
					value = Z8.decode(parameter.value, parameter.serverType);
				
				this.source[parameter.text] = value;
			} else
				this.files.push(parameter);
		}
		
		if(!Z8.isEmpty(propertyNames)) {
			this.propsGrid = new Z8.grid.PropertyGrid({
				flex: 1,
				propertyNames: propertyNames,
				source: this.source,
				viewConfig: { forceFit: true, scrollOffset: 2 }
			});
			
			this.add(this.propsGrid);
		}
		
		var items = [];
		for(var i = 0; i < this.files.length; i++) {
			var file = this.files[i];
			var fileField = new Z8.form.FileUploadField({id: file.id, width: 250, fieldLabel: file.text });
			items.push(fileField);
		}

		this.form = new Ext.form.FormPanel({
			border: false,
			frame: false,
			padding: '10 10 10 10',
			fileUpload: true, 
			url: Z8.request.url,
			method: Z8.request.method,
			baseParams: this.params || {},
			items: items
		});

		this.add(this.form);

		this.okButton = new Ext.Button({ text: 'OK', iconCls: '', handler: this.onOK, scope: this });
		this.addButton(this.okButton);
		
		this.cancelButton = new Ext.Button({ text: 'Oтмена', handler: this.onCancel, scope: this });
		this.addButton(this.cancelButton);

		this.on('close', this.onCancel, this);
	},
	
	onCancel: function() {
		if(this.action != null && this.action.transactionId != null)
			Ext.removeNode(this.action.transactionId);
		
		this.destroy();
	},

	processSource: function(source) {
		var result = {};
		
		for(var name in source) {
			var value = source[name];
			result[name] = Ext.isObject(value) ?  value.recordId : value;
		}
		
		var files = this.files;
		
		for(var i = 0, length = files.length; i < length; i++) {
			var file = files[i];
			result[file.name] = i;
		}
		
		return result;
	},

	show: function() {
		if(Ext.isEmpty(this.parameters))
			this.onOK();
		else
			Z8.view.ParametersWindow.superclass.show.call(this);
	},
	
	onOK: function() {
		if(this.propsGrid != null)
			this.propsGrid.stopEditing();
		
		var source = this.processSource(this.source);

		var success = function(form, action) {
			var result = form.requestId != null ? form : action.result;

			if(result != null && result.success) {
				this.destroy();
				this.success.call(this.scope, result);
			} else
				failure(form, action);
		};

		var failure = function(form, action) {
			this.okButton.setDisabled(false);
			this.okButton.setIconClass('');

			var result = form.requestId != null ? form : action.result;

			if(result != null && this.relogin(result.status))
				return;

			var	info = result != null ? result.info : { messages: ['Transaction failure.'] };

			if(this.failure != null)
				this.failure.call(this.scope, info);
			else
				Z8.showMessages(this.title || 'Z8', Z8.Format.nl2br(info.messages));
		}			

		var files = this.files;
		
		this.okButton.setDisabled(true);
		this.okButton.setIconClass('silk-loading');

		if(files.length != 0) {
			var form = this.form.getForm();

			Ext.apply(form.baseParams, this.baseParams);
			form.baseParams.sessionId = Z8.sessionId;
			form.baseParams.parameters = Ext.encode(source);
			
			form.on('beforeaction', this.onBeforeAction, this);
			
			form.submit({ success: success, failure: failure, scope: this });
		} else {
			var params = Ext.apply({}, this.baseParams);
			params.parameters = Ext.encode(source);
			Z8.Ajax.request(this.baseParams.requestId, success, failure, params, this);
		}
	},

	onBeforeAction: function(form, action) {
		this.action = action;
	},
	
	relogin: function(status) {
		if(status == Z8.Status.AccessDenied) {
			Z8.LoginManager.login(this.onRelogin, this, true);
			return true;
		}
		return false;
	},

	onRelogin: function(result) {
		this.onOK();
	}
});