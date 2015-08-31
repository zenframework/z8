Ext.apply(Ext.form.VTypes,
{
    password: function(val, field)
    {
        if(field.initialPassField)
        {
			var pwd = Ext.getCmp(field.initialPassField);
			return (val == pwd.getValue());
		}
		return true;
	},

	passwordText : 'Пароли не совпадают'
});


Z8.view.ChangePasswordDialog = Ext.extend(Z8.Window, 
{
	width:400,
	height: 150,
	resizable: false,
	modal: true, 

	title: 'Изменить пароль',
	
	layout: 'vbox',
	layoutConfig: { align: 'stretch' },

	initComponent: function()
	{
		Z8.view.ChangePasswordDialog.superclass.initComponent.call(this);
		
		this.oldPassword = new Ext.form.TextField({ fieldLabel: 'Пароль', name: 'oldPass', id: 'oldPassword', width: 175, inputType: 'password' });
		this.newPassword = new Ext.form.TextField({ fieldLabel: 'Новый пароль', name: 'pass', id: 'password', width: 175, inputType: 'password' });
		this.confirmedPassword = new Ext.form.TextField({ fieldLabel: 'Подтвердите', name: 'passwordConfirm', vtype: 'password', initialPassField: 'password', width: 175, inputType: 'password' });
		
		this.form = new Ext.form.FormPanel({
			labelWidth: 125,
			width: 350,
			border: false,
			frame: false,
			padding: '10 10 10 10',
			defaults: {  },
			defaultType: 'textfield',
			items: [ this.oldPassword, this.newPassword, this.confirmedPassword ]
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
		
		Z8.Ajax.request(Z8.request.desktopId, this.onSuccess, this.onFailure, { password: this.oldPassword.getValue(), newPassword: this.newPassword.getValue() }, this);
	},

	onSuccess: function(result)
	{
		this.destroy();
	},
	
	onFailure: function(info)
	{
		this.okButton.setDisabled(false);
		this.okButton.setIconClass('');
		
		Z8.showMessages("Изменение пароля", info.messages);
	}
});