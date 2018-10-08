
Z8.define('Z8.application.viewport.Login', {
	extend: 'Z8.form.Fieldset',

	plain: true,
	visible: false,

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('login', 'air', 'display-none');

		var header = { cls: 'header', html: 'Авторизация' };
		var login = this.loginField = new Z8.form.field.Text({ label: { text: 'Логин', icon: 'fa-user', width: '6.42857143em', align: 'left' }, placeholder: 'Логин', value: 'Admin' });
		var password = this.passwordField = new Z8.form.field.Text({ label: { text: 'Пароль',  icon: 'fa-key', align: 'left', width: '6.42857143em' }, placeholder: 'Пароль', password: true });
		var loginButton = this.loginButton = new Z8.button.Button({ cls: 'btn-tool', icon: 'fa-check', handler: this.login, scope: this });

		this.controls = [header, login, password, loginButton];

		this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keydown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keydown', this.onKeyDown, this);
		this.callParent();
	},

	focus: function() {
		this.loginField.focus();
	},

	show: function() {
		if(this.isVisible()) {
			this.focus();
			return;
		}

		this.mask = DOM.append(Viewport.getBody(), { cls: 'window-mask login' }); 

		this.callParent();
		DOM.addCls(this, 'open', 100);

		this.focus();

		this.fireEvent('show', this);
	},

	hide: function() {
		if(!this.isVisible())
			return;

		DOM.remove(this.mask);
		this.mask = null;

		DOM.removeCls(this, 'open');
		this.callParent();

		this.passwordField.setValue('');
		this.fireEvent('hide', this);
	},

	login: function(button) {
		button.setBusy(true);

		var callback = function(response, success) {
			button.setBusy(false);

			if(success) {
				Z8.callback(this.handler, this.scope, response);
				this.hide();
			} else
				this.loginField.focus();
		}

		var password = this.passwordField.getValue() || '';
		var parameters = {
			request: 'login',
			login: this.loginField.getValue(), 
			password: Application.hashPassword ? MD5.hex(password) : password,
			experimental: true
		};

		HttpRequest.send(parameters, { fn: callback, scope: this });
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER)
			this.login(this.loginButton);
	}
});