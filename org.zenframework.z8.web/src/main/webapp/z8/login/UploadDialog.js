Z8.view.UploadDialog = Ext.extend(Z8.Window, 
{
	width:400,
	height: 100,
	resizable: false,
	modal: true, 

	layout: 'vbox',
	layoutConfig: { align: 'stretch' },

	initComponent: function()
	{
		Z8.view.UploadDialog.superclass.initComponent.call(this);
		
		var items = [];
		
		this.uploadField = new Z8.form.FileUploadField({ width: 250, fieldLabel: 'Файл' });
		items.push(this.uploadField);
		
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

		this.addEvents('ok', 'error');
	},
	
	onCancel: function()
	{
		if(this.action != null && this.action.transactionId != null)
		{
			Ext.removeNode(this.action.transactionId);
		}
		
		this.destroy();
	},

	onOK: function()
	{
		var form = this.form.getForm();
		form.baseParams.sessionId = Z8.sessionId;
		
		form.on('beforeaction', this.onBeforeAction, this);
		
		this.okButton.setDisabled(true);
		this.okButton.setIconClass('silk-loading');
		
		form.submit({ success: this.onSubmitSuccess, failure: this.onSubmitFailure, scope: this });
	},

	onBeforeAction: function(form, action)
	{
		this.action = action;
		return true;
	},
	
	onSubmitSuccess: function(form, action)
	{
		var result = action.result;
		
		if(result != null && result.success)
		{
			this.fireEvent('ok', result.data || result);
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