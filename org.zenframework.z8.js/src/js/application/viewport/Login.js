Z8.define('Z8.application.viewport.Login', {
	extend: 'Z8.form.Fieldset',

	plain: true,
	visible: false,

	initComponent: function() {
		var header = { cls: 'header', html: 'Авторизация' };
		var login = this.loginField = new Z8.form.field.Text({ label: { text: Z8.$('Login.login.text'), icon: 'fa-user', width: '6.42857143em', align: 'left' }, placeholder: Z8.$('Login.login.placeholder'), value: 'Admin' });
		var password = this.passwordField = new Z8.form.field.Text({ label: { text: Z8.$('Login.password.text'),  icon: 'fa-key', align: 'left', width: '6.42857143em' }, placeholder: Z8.$('Login.password.placeholder'), password: true });
		var loginButton = this.loginButton = new Z8.button.Button({ text: Z8.$('Login.btn.text'), cls: 'btn-tool', icon: 'fa-check', handler: this.login, scope: this });
        var ssoLoginButton = this.ssoLoginButton = new Z8.button.Button({ text: Z8.$('Login.ssoBtn.text'),  cls: 'btn-tool', icon: 'fa-check', handler: this.ssoLogin, scope: this });

		this.controls = [header, login, password, loginButton, ssoLoginButton];

		this.callParent();
	},

	htmlMarkup: function() {
		var markup = Z8.form.Fieldset.prototype.htmlMarkup.call(this);
		markup.tag = 'form';
		return markup;
	},

	getCls: function() {
		return Z8.form.Fieldset.prototype.getCls.call(this).pushIf('login');
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

	show: function(show) {
		if(show !== false && this.isVisible()) {
			this.focus();
			return;
		}

		this.mask = DOM.append(Viewport.getBody(), { cls: 'window-mask login' }); 

		this.callParent(show);
		DOM.addCls(this, 'open', 100);

		this.focus();

		this.fireEvent('show', this);
	},

	hide: function(hide) {
		if(hide !== false && !this.isVisible())
			return;

		DOM.remove(this.mask);
		this.mask = null;

		DOM.removeCls(this, 'open');
		this.callParent(hide);

		this.passwordField.setValue('');
		this.fireEvent('hide', this);
	},

	login: function(button) {
		button.setBusy(true);

		var callback = function(response, success) {
			button.setBusy(false);

			if(success) {
				this.hide();
				if(response.user.changePassword) {
					var handler = function(window, success) {
						if(success)
							Z8.callback(this.handler, this.scope, response);
						else
							this.show();
					};
					new Z8.application.viewport.ChangePassword({ handler: handler, scope: this, login: login, password: password }).open();
				} else
					Z8.callback(this.handler, this.scope, response);
			} else
				this.loginField.focus();
		};

		var login = this.loginField.getValue();
		var password = this.passwordField.getValue() || '';
		password = Application.hashPassword ? MD5.hex(password) : password;

		var parameters = {
			request: 'login',
			login: login,
			password: password,
			experimental: true
		};

		HttpRequest.send(parameters, { fn: callback, scope: this });
	},

	ssoLogin: function(button) {
	    window.location = window.location.origin + '/sso_auth'
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER)
			this.login(this.loginButton);
	}
});

Z8.define('Z8.application.viewport.ChangePassword', {
	extend: 'Z8.window.Window',

	autoClose: false,
	icon: 'fa-key',
	cls: 'change-password',

	initComponent: function() {
		this.header = (this.login || User.login) + ' - установка пароля';

		var validation = {
			fn: function(control, valid) {
				if(control == newPassword1)
					newPassword2.validate();
				if(this.okButton != null)
					this.okButton.setEnabled(valid);
			},
			scope: this
		};

		var isEmptyValue = function(value) {
			return (newPassword1.getValue() || '') != (value || '');
		};

		var controls = [];

		if(this.password == null) {
			var password = this.passwordField = new Z8.form.field.Text({ label: 'Пароль', placeholder: 'Пароль', password: true });
			controls.add(password);
		}

		var newPassword1 = this.newPassword1 = new Z8.form.field.Text({ label: 'Новый пароль', placeholder: 'Новый пароль', password: true, validation: validation });
		var newPassword2 = this.newPassword2 = new Z8.form.field.Text({ label: 'Новый пароль (повтор)', placeholder: 'Новый пароль', password: true, required: true, validation: validation, isEmptyValue: isEmptyValue });

		this.controls = controls.add([newPassword1, newPassword2]);

		this.callParent();
	},

	ok: function() {
		this.okButton.setBusy(true);

		var callback = function(response, success) {
			this.okButton.setBusy(false);
			Z8.callback(this.handler, this.scope, window, true);
			this.close();
		};

		var login = this.login || User.login;
		var password = this.password;
		if(password == null) {
			password = this.passwordField.getValue() || '';
			password = Application.hashPassword ? MD5.hex(password) : password;
		}
		var newPassword = this.newPassword1.getValue() || '';
		newPassword = Application.hashPassword ? MD5.hex(newPassword) : newPassword;

		var parameters = {
			request: 'login',
			login: login,
			password: password,
			newPassword: newPassword
		};

		HttpRequest.send(parameters, { fn: callback, scope: this });
	},

	cancel: function() {
		Z8.callback(this.handler, this.scope, window, false);
		this.close();
	}
});