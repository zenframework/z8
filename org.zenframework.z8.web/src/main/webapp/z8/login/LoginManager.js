Z8.login.LoginManager = Ext.extend(Ext.util.Observable,
{
	loginWindow: null,
	handlers: [],

	constructor: function(config)
	{
		Ext.apply(this, config);
		Z8.login.LoginManager.superclass.constructor.call(this);
		
		this.addEvents('login', 'logout');
	},
	
	login: function(handler, scope, modal)
	{
		if(handler != null)
		{
			this.handlers.push({ fn: handler, scope: scope });
		}
		
		this.showLoginWindow(modal);
	},
	
	logout: function()
	{
		this.fireEvent('logout');
	},

	showLoginWindow: function(modal)
	{
		if(this.loginWindow == null)
		{
			this.loginWindow = new Z8.LoginWindow({ modal: modal, handler: this.onLogin, scope: this });
			this.loginWindow.show();
		}
	},

	onLogin: function(result)
	{
		this.loginWindow = null;

		this.fireEvent('login', result);

		for(var i = 0; i < this.handlers.length; i++)
		{
			this.handlers[i].fn.call(this.handlers[i].scope, result);
		}
		
		this.handlers = [];
	}
});

Ext.apply(Z8.LoginManager, new Z8.login.LoginManager());
