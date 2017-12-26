
Z8.define('Z8.application.viewport.Login', {
	extend: 'Z8.form.Fieldset',

	plain: true,

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('login', 'air', 'display-none');

		var header = { cls: 'header', html: 'Авторизация' };
		var login = this.loginField = new Z8.form.field.Text({ label: { text: 'Логин', icon: 'fa-user', width: 80, align: 'left' }, placeholder: 'Логин', value: 'Admin' });
		var password = this.passwordField = new Z8.form.field.Text({ label: { text: 'Пароль',  icon: 'fa-key', align: 'left', width: 80 }, placeholder: 'Пароль', password: true });
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
		if(this.visible) {
			this.focus();
			return;
		}

		this.visible = true;

		this.mask = DOM.append(Viewport.getBody(), { cls: 'window-mask login' }); 

		DOM.removeCls(this, 'display-none');
		DOM.addCls(this, 'open', 100);

		this.focus();

		this.fireEvent('show', this);
	},

	hide: function() {
		if(!this.visible)
			return;

		this.visible = false;

		DOM.remove(this.mask);
		this.mask = null;

		DOM.removeCls(this, 'open');
		DOM.addCls(this, 'display-none', 200);

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

		var parameters = {
			login: this.loginField.getValue(), 
			password: MD5.hex(this.passwordField.getValue() || ''),
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