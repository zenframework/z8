Z8.view.ChangeEmailDialog = Ext.extend(Z8.Window, 
{
	width:400,
	height: 150,
	resizable: false,
	modal: true, 

	title: 'Изменить email',
	
	layout: 'vbox',
	layoutConfig: { align: 'stretch' },

	initComponent: function()
	{
		Z8.view.ChangeEmailDialog.superclass.initComponent.call(this);
		
		this.email = new Ext.form.TextField({ fieldLabel: 'Email', name: 'email', id: 'email', width: 175, value: Z8.user.email });
		
		this.form = new Ext.form.FormPanel({
			labelWidth: 125,
			width: 350,
			border: false,
			frame: false,
			padding: '10 10 10 10',
			defaults: {  },
			defaultType: 'textfield',
			items: this.email
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
		this.destroy();
	},

	onOK: function()
	{
		this.okButton.setDisabled(true);
		this.okButton.setIconClass('silk-loading');
		
		Z8.Ajax.request(Z8.request.desktopId, this.onSuccess, this.onFailure, { email: this.email.getValue() }, this);
	},

	onSuccess: function(result)
	{
		Z8.user.email = this.email.getValue();
		this.destroy();
	},
	
	onFailure: function(info)
	{
		this.okButton.setDisabled(false);
		this.okButton.setIconClass('');
		
		Z8.showMessages("Изменение email", info.messages);
	}
});