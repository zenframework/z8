Z8.LoginWindow = Ext.extend(Ext.Window, 
{
	frame: true,
	shadow: false,
	border:false,
	resizable:false,
	closable:false,
	baseCls:'loginform',
	width: 448,
	height: 159,
	waitMsgTarget: true,
	defaultButton: 'login',
	layout: 'absolute',

	initComponent: function()
	{
		Z8.LoginWindow.superclass.initComponent.call(this);

		var login = Z8.user != null ? Z8.user.login : '';
		var password = '';
		
		var saasMode = getRequestDomain() == 'organization.org';
	
		this.login = new Ext.form.TextField({
			id:'login',
			disabled: this.modal,
			name: 'login',
			fieldLabel: 'Логин',
			value: login,
			width: 240
		});
		
		this.password = new Ext.form.TextField({
			name: 'password',
			fieldLabel: 'Пароль',
			inputType: 'password',
			value: password,
			width: 240
		});
		
		var fieldSet = new Ext.form.FieldSet({
			border: false,
			baseCls:'fieldlogin',
			autoHeight: true,
			x: 0, y: 10, width: 300,
			items: [ this.login, this.password ]
		});
		
		var loginBtn = new Ext.Button({
			text: 'Вход в систему',
			cls:'loginbut',
			handler: this.onSubmit,
			scope: this,
			x: 205, y: 80
		});
		
		var recoverBtn = new Ext.Button({
			text: 'Забыли пароль?',
			cls:'loginbut',
			handler: this.onRecovery,
			scope: this,
			x: 100, y: 80
		});
		
		var items = [fieldSet, loginBtn];
		
		if(saasMode)
		{
			items.push(recoverBtn);
		}
		
		this.add(new Ext.Container({
			layout: 'absolute',
			items: items,
			x: 120,
			y: (Ext.isIE && !Ext.isIE9) ? 10 : 30
		}));
		
		//this.add({xtype:'button', text: 'Вход в систему', cls:'loginbut', height:22, width:70, handler: this.onSubmit, scope: this });
	},

	initEvents: function()
	{
		Z8.LoginWindow.superclass.initEvents.call(this);

		var enter = { key: 13, handler: this.onSubmit, scope: this, stopEvent: true };
		this.keyMap = new Ext.KeyMap(this.el, [enter]);
	},

	onShow: function()
	{
		Z8.LoginWindow.superclass.onShow.call(this);
		
		var focus = this.modal ? this.password : this.login;
		focus.focus();
	},
	
	onDestoroy: function()
	{
		this.keyMap.disable();
		
		Z8.LoginWindow.superclass.onDestroy.call(this);
	},
	
	onSubmit: function()
	{
		var parameters = 
		{
			requestId: Z8.request.desktopId,
			login: this.login.getValue(),
			password: this.password.getValue()
		};

		Z8.Ajax.login(this.onLogin, parameters, this);
	},
	
	onRecovery: function()
	{
		window.location.href="http://organization.org/index.php?do=recovery&login="+this.login.value;
	},
	
	onLogin: function(result)
	{
		this.destroy();
		
		if(this.handler != null)
		{
			this.handler.call(this.scope, result);
		}
	}
});