Z8.define('Z8.application.viewport.Login', {
	extend: 'Z8.form.Fieldset',

	plain: true,
	visible: false,

	initComponent: function() {
		var header = { cls: 'header', html: Z8.$('Login.authorization') };
		var login = this.loginField = new Z8.form.field.Text({ label: { text: Z8.$('Login.login.text'), icon: 'fa-user', width: '6.42857143em', align: 'left' }, placeholder: Z8.$('Login.login.placeholder'), value: 'Admin' });
		var password = this.passwordField = new Z8.form.field.Text({ label: { text: Z8.$('Login.password.text'),  icon: 'fa-key', align: 'left', width: '6.42857143em' }, placeholder: Z8.$('Login.password.placeholder'), password: true });
		var loginButton = this.loginButton = new Z8.button.Button({ cls: 'btn-tool', icon: 'fa-check', handler: this.login, scope: this });

		this.controls = [header, login, password, loginButton];

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

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER)
			this.login(this.loginButton);
	}
});

Z8.define('Z8.application.viewport.SSOLogin', {
	extend: 'Z8.application.viewport.Login',

	initComponent: function() {
		this.callParent();
        this.loginButton.text = Z8.$('Login.btn.text');
        var ssoLoginButton = this.ssoLoginButton = new Z8.button.Button({ text: Z8.$('Login.ssoBtn.text'),  cls: 'btn-tool', icon: 'fa-check', handler: this.ssoLogin, scope: this });
		this.controls.add(ssoLoginButton);
	},

	ssoLogin: function(button) {
	    window.location = window.location.origin + '/sso_auth'
	},
});

Z8.define('Z8.application.viewport.ChangePassword.PasswordField', {
	extend: 'Z8.form.field.Text',
	shortClassName: 'ChangePassword.PasswordField',

	validate: function() {
		this.setValid(!this.checkRequirements());
	},

	checkRequirements: function() {
		var newPassword = this.getValue() || '';
		var appPassword = Application.password;
		var hasUpperCase = appPassword.mustHaveUpperCase ? /[A-ZА-Я]/.test(newPassword) : true;
		var hasLowerCase = appPassword.mustHaveLowerCase ? /[a-zа-я]/.test(newPassword) : true;
		var hasSpecialChar = appPassword.mustHaveSpecialСhar ? /[!@#$%^&*()_+{}\[\]:;<>,.?~\\\/'"`]/.test(newPassword) : true;
		var hasDigit = Application.password.mustHaveDigit ? /[0-9]/.test(newPassword) : true;
		return newPassword.length < appPassword.minLength || !hasUpperCase || !hasLowerCase || !hasSpecialChar || !hasDigit;
	}
});

Z8.define('Z8.application.viewport.ChangePassword', {
	extend: 'Z8.window.Window',

	autoClose: false,
	icon: 'fa-key',
	cls: 'change-password',

	initComponent: function() {
		this.header = (this.login || User.login) + Z8.$('ChangePassword.passwordSetting');

		var validation = {
			fn: function(control, valid) {
				if(control == newPassword1)
					newPassword2.validate();
				if(control == newPassword2 && this.okButton != null)
					this.okButton.setEnabled(valid);
			},
			scope: this
		};

		var isEmptyValue = function(value) {
			return (newPassword1.getValue() || '') != (value || '');
		};

		var controls = [];

		if(this.password == null) {
			var password = this.passwordField = new Z8.form.field.Text({ label: Z8.$('ChangePassword.password'), placeholder: Z8.$('ChangePassword.password'), password: true });
			controls.add(password);
		}

		var newPassword1 = this.newPassword1 = new ChangePassword.PasswordField({ label: Z8.$('ChangePassword.newPassword'), placeholder: Z8.$('ChangePassword.newPassword'), password: true, validation: validation, colSpan: 11 });
		var passwordRequirement = this.passwordRequirement = new Button({ icon: 'fa-question', tooltip: this.getRequirements(), enabled: true, cls: 'password-req', colSpan: 1 })
		var newPasswordField = new Z8.form.Fieldset({ plain: true, controls: [newPassword1, passwordRequirement], colCount: 12 });

		var newPassword2 = this.newPassword2 = new Z8.form.field.Text({ label: Z8.$('ChangePassword.newPassword') + ' ' + Z8.$('ChangePassword.repeat'), placeholder: Z8.$('ChangePassword.newPassword'), password: true, required: true, validation: validation, isEmptyValue: isEmptyValue });

		this.controls = controls.add([newPasswordField, newPassword2]);

		this.callParent();
	},

	getRequirements: function() {
		var appPassword = Application.password;
		var mustHaveUpperCase = appPassword.mustHaveUpperCase;
		var mustHaveLowerCase = appPassword.mustHaveLowerCase;
		var mustHaveSpecialСhar = appPassword.mustHaveSpecialСhar;
		var mustHaveDigit = appPassword.mustHaveDigit;
		var minLength = appPassword.minLength;
		if(mustHaveUpperCase || mustHaveLowerCase || mustHaveSpecialСhar || mustHaveDigit || minLength > 0) {
			return Z8.$('ChangePassword.passwordMustContain') + (mustHaveUpperCase ? '\n - ' + Z8.$('ChangePassword.oneCapitalLetter') : '') + (mustHaveLowerCase ? '\n - ' + Z8.$('ChangePassword.oneSmallLetter') : '')
					+ (mustHaveSpecialСhar ? '\n - ' + Z8.$('ChangePassword.oneSpecialChar') : '') + (mustHaveDigit ? '\n - ' + Z8.$('ChangePassword.oneDigit') : '')
					+ (minLength > 0 ? '\n - ' + Z8.$('ChangePassword.length') + minLength : '');
		} else {
			return Z8.$('ChangePassword.noRequirements');
		}
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

		var newPassword1 = this.newPassword1;
		if(newPassword1.checkRequirements()) {
			Application.message({ text: Z8.$('ChangePassword.requirementsError'), type: 'error' });
			this.okButton.setBusy(false);
			return;
		}
		var newPassword = newPassword1.getValue() || '';
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
