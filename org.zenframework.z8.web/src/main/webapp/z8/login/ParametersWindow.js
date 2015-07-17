Z8.view.ParametersWindow = Ext.extend(Z8.Window, 
{
	parameters: null,
	
	width:400,
	resizable: false,
	modal: true, 

	layout: 'vbox',
	layoutConfig: { align: 'stretch' },

	constructor: function(config)
	{
		Ext.apply(this, config);

		this.height = this.minHeight = Math.max(3, this.parameters.length + 1) * 26 + 90;
		
		Z8.view.ParametersWindow.superclass.constructor.call(this);
	},
		
	initComponent: function()
	{
		Z8.view.ParametersWindow.superclass.initComponent.call(this);
		
		this.addEvents('ok', 'cancel', 'error');

		var parameters = this.parameters;
		
		var propertyNames = {};
		
		this.files = [];
		this.source = {};
		
		for(var i = 0; i < parameters.length; i++)
		{
			var parameter = parameters[i];
			
			if(parameter.serverType == Z8.ServerTypes.File)
			{
				this.files.push(parameter);
			}
			else
			{
				propertyNames[parameter.text] = parameter.text;
				
				var value = null;
				
				if(parameter.queryId != null)
				{
					value = { queryId: parameter.queryId, fieldId: parameter.fieldId, recordId: parameter.recordId };
				}
				else if(parameter.fieldId != null && this.record != null)
				{
					value = this.record.data[parameter.fieldId];
				}
				
				if(value == null)
				{
					value = Z8.decode(parameter.value, parameter.serverType);
				}
				
				this.source[parameter.text] = value;
			}
		}
		
		if(!Z8.isEmpty(propertyNames))
		{
			this.propsGrid = new Z8.grid.PropertyGrid({
				flex: 1,
				propertyNames: propertyNames,
				source: this.source,
				viewConfig: { forceFit: true, scrollOffset: 2 }
			});
			
			this.add(this.propsGrid);
		}
		
		var items = [];
		for(var i = 0; i < this.files.length; i++)
		{
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
	
	onCancel: function()
	{
		if(this.action != null && this.action.transactionId != null)
		{
			Ext.removeNode(this.action.transactionId);
		}

		this.fireEvent('cancel');
		
		this.destroy();
	},

	processSource: function(source)
	{
		var result = {};
		
		for(var name in source)
		{
			var value = source[name];
			
			if(Ext.isObject(value))
			{
				result[name] = value.recordId;
			}
			else
			{
				result[name] = value;
			}
		}
		
		return result;
	},
	
	onOK: function()
	{
		if(this.propsGrid != null)
		{
			this.propsGrid.stopEditing();
		}
		
		if(this.files.length != 0)
		{
			var form = this.form.getForm();

			Ext.apply(form.baseParams, this.baseParams);
			form.baseParams.sessionId = Z8.sessionId;
			
			form.on('beforeaction', this.onBeforeAction, this);
			
			this.okButton.setDisabled(true);
			this.okButton.setIconClass('silk-loading');

			form.submit({ success: this.onSubmitSuccess, failure: this.onSubmitFailure, scope: this });
		}
		else
		{
			var source = this.processSource(this.source);
			this.fireEvent('OK', source, null);
			this.destroy();
		}
	},

	onBeforeAction: function(form, action)
	{
		this.action = action;
	},
	
	onSubmitSuccess: function(form, action)
	{
		var result = action.result;
		
		if(result != null && result.success)
		{
			var source = this.processSource(this.source);
			
			for(var i = 0; i < result.data.length; i++)
			{
				var file = result.data[i];
				source[file.id] = file;
			}
			
			this.fireEvent('OK', source, result.serverId);
			this.destroy();
		}
		else
		{
			this.onSubmitFailure(form, action);
		}
	},
	
	onSubmitFailure: function(form, action)
	{
		this.okButton.setDisabled(false);
		this.okButton.setIconClass('');

		var result = action.result;

		if(result != null && this.relogin(result.status))
		{
			return;
		}
			
		var	info = result != null ? result.info : { messages: ['Transaction failure.'] };
		
		this.fireEvent('error', info);

		var event = this.events['error'];
		
		if(event == null || event.listeners == null || event.listeners.length == 0)
		{
			Z8.showMessages(this.title || 'Z8', Z8.Format.nl2br(info.messages));
		}
	},

	relogin: function(status)
	{
		if(status == Z8.Status.AccessDenied)
		{
			Z8.LoginManager.login(this.onRelogin, this, true);
			return true;
		}
		return false;
	},

	onRelogin: function(result)
	{
		this.onOK();
	}
});