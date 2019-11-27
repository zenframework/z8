var Viewport = null;

Z8.define('Z8.application.viewport.Viewport', {
	extend: 'Z8.Container',

	cls: 'viewport air',

	handlers: [],
	forms: [],

	initComponent: function() {
		this.callParent();

		this.setTitle(Application.name);
	},

	htmlMarkup: function() {
		if (Application.startupForm == null) {
			var items = [ this.createBreadcrumb() ];
			this.forms = [ null ];
		}
		var breadcrumbs = this.breadcrumbs = new Z8.button.Group({ cls: 'breadcrumbs flex', items: items });

		var logout = this.logout = new Z8.button.Button({ tooltip: 'Выход', cls: 'btn-tool', icon: 'fa-power-off', handler: this.logout, scope: this });

		var header = this.header = new Z8.Container({ cls: 'header flex-row flex-center', items: [breadcrumbs, logout] });
		var body = this.body = new Z8.Container({ cls: 'body' });
		var popupMessages = this.popupMessages = new Z8.viewport.PopupMessages();

		var loginForm = this.loginForm = new Z8.application.viewport.Login({ handler: this.loginCallback, scope: this });
		loginForm.on('show', this.onLoginFormShow, this);
		loginForm.on('hide', this.onLoginFormHide, this);

		this.items = [header, body, loginForm, popupMessages];

		return this.callParent();
	},

	login: function(options) {
		this.handlers.push(options);
		var login = this.loginForm;
		this.isLoggingIn ? login.focus() : login.show();
	},

	logout: function() {
		if(!this.isLoggingIn) {
			this.onLogout();
			Application.login();
		} else
			this.loginForm.focus();
	},

	createHeaderButtons: function() {
		//var settings = this.settingsButton = new Z8.button.Button({ tooltip: 'Настройки', cls: 'btn-tool', icon: 'fa-cog', enabled: false, handler: this.onSettings, scope: this });
		var jobMonitor = this.jobMonitorButton = new Z8.button.Button({ tooltip: 'Монитор задач', cls: 'btn-tool', icon: 'fa-tv', handler: this.openJobMonitor, scope: this });
		//var logger = this.loggerButton = new Z8.button.Button({ tooltip: 'Сообщения', cls: 'btn-tool', icon: 'fa-comment-o', enabled: false, handler: this.openLogger, scope: this });
		return [jobMonitor]; //, logger, settings];
	},

	onLogin: function() {
		if(this.isLoggedIn) {
			this.focus();
			return;
		}

		this.isLoggedIn = true;

		var header = this.header; 
		var menuToggle = this.menuToggle = new Z8.button.Button({ tooltip: 'Развернуть/скрыть меню', cls: 'btn-tool', icon: 'fa-bars', handler: this.toggleMenu, scope: this });
		header.add(menuToggle, 0);

		var buttons = this.buttons = this.createHeaderButtons();
		header.add(buttons, header.getCount() - 1);

		var menu = this.menu = new Z8.application.sidebar.Sidebar({ owner: menuToggle, handler: this.onMenuItem, scope: this });
		menu.on('open', this.onMenuOpen, this);
		menu.on('close', this.onMenuClose, this);
		this.add(menu);

		if(User.administrator) {
			var sourceCode = this.sourceCode = new Z8.application.viewport.SourceCode();
			this.add(sourceCode);
		}

		if(Application.startupForm == null) {
			this.openMenu();
		} else {
			var startupForm = Application.startupForm;
			if (typeof startupForm === 'function')
				startupForm = startupForm();
			if (startupForm != null)
				this.open(startupForm);
		}

		DOM.on(menuToggle, 'mouseDown', this.onMenuToggleMouseDown, this);
		DOM.on(document.body, 'keyDown', this.onKeyDown, this);
		DOM.on(window, 'popState', this.onPopState, this);
	},

	onLogout: function() {
		if(!this.isLoggedIn)
			return;

		this.isLoggedIn = false;

		DOM.un(this.menuToggle, 'mouseDown', this.onMenuToggleMouseDown, this);
		DOM.un(document.body, 'keyDown', this.onKeyDown, this);
		DOM.un(this, 'popState', this.onPopState, this);

		this.closeAllForms();

		var header = this.header;
		header.remove(this.menuToggle);
		header.remove(this.buttons);

		if(this.jobMonitor != null)
			this.jobMonitor.destroy();

		var menu = this.menu;
		menu.un('open', this.onMenuOpen, this);
		menu.un('close', this.onMenuClose, this);
		this.remove(menu);

		this.remove(this.sourceCode);

		this.body.removeAll();

		this.menuToggle = this.buttons = this.menu = this.jobMonitor = this.sourceCode = null;
	},

	loginCallback: function(loginData) {
		var handlers = this.handlers;

		for(var i = 0, length = handlers.length; i < length; i++)
			Z8.callback(handlers[i], loginData);

		this.handlers = [];

		this.onLogin();
	},

	message: function(message) {
		this.popupMessages.show(message);
	},

	getBody: function() {
		return this.body;
	},

	focus: function() {
		var forms = this.forms;
		var length = forms.length;
		return length > 1 ? forms[length - 1].focus() : DOM.focus(this);
	},

	openMenu: function(button) {
		this.menu.open();
	},

	showSourceCode: function(show) {
		if(this.sourceCode != null)
			show ? this.sourceCode.open() : this.sourceCode.close();
	},

	initSourceCode: function(source) {
		if(this.sourceCode != null)
			this.sourceCode.load(source);
	},

	onMenuToggleMouseDown: function(event, target) {
		if(this.menuToggle.isEnabled())
			this.menu.closeOnBlur = false;
	},

	toggleMenu: function(button) {
		this.menu.closeOnBlur = true;
		this.menu.toggle();
	},

	enableMenu: function(enabled) {
		if(this.menu != null) {
			this.menuToggle.setEnabled(enabled);
			this.menu.setEnabled(enabled);
		}
	},

	onMenuOpen: function(menu) {
		this.menuToggle.rotateIcon(90);
	},

	onMenuClose: function(menu) {
		this.menuToggle.rotateIcon(0);
		this.focus();
	},

	onLoginFormShow: function(form) {
		this.enableMenu(false);
		this.isLoggingIn = true;
	},

	onLoginFormHide: function(form) {
		this.enableMenu(true);
		this.isLoggingIn = false;
	},

	onMenuItem: function(item) {
		this.open({ request: item.request, id: item.id }, true);
	},

	setTitle: function(title) {
		this.callParent(title);
		DOM.setInnerHtml(this.text, title);
	},

	onBreadcrumbClick: function(button) {
		this.openForm(button.form);
	},

	createBreadcrumb: function(form, depth) {
		var text = form != null ? form.getTitle() : Application.name;
		var icon = form != null ? form.getIcon() : null;
		return new Z8.button.Button({ cls: 'n' + (depth || 1), text: text.ellipsis(27), tooltip: Format.htmlEncode(text), icon: icon, form: form, handler: this.onBreadcrumbClick, scope: this });
	},

	closeForm: function(form) {
		var forms = this.forms;
		var index = forms.indexOf(form);

		if(index == -1)
			return;

		form = forms[Math.max(index - 1, 0)];
		this.openForm(form);
	},

	getFormById: function(id) {
		var forms = this.forms;

		for(var i = 0, length = forms.length; i < length; i++) {
			var form = forms[i];
			if(form != null && form.formId == id)
				return form;
		}
		return null;
	},

	closeAllForms: function() {
		var forms = this.forms;
		var breadcrumbs = this.breadcrumbs;
		var body = this.body;
		var buttons = breadcrumbs.items;
		for(var i = buttons.length - 1; i >= 0; i--) {
			var button = buttons[i];
			var form = button.form;
			if (form != null) {
				body.remove(button.form);
				breadcrumbs.remove(button);
				forms.removeAt(i);
			}
		}
	},

	openForm: function(form, closeOthers) {
		var store = form != null ? form.store : null;

		this.showSourceCode(false);
		this.initSourceCode(store != null ? (store.isStore ? store.getSourceCodeLocation() : store.sourceCode) : null);

		var forms = this.forms;
		var index = forms.indexOf(form || null);

		if(index == -1 && closeOthers)
			index = 0;

		var breadcrumbs = this.breadcrumbs;
		var buttons = this.breadcrumbs.items;
		var body = this.body;

		if(index != -1) {
			for(var i = buttons.length - 1; i > index; i--) {
				var button = buttons[i];
				body.remove(button.form);
				breadcrumbs.remove(button);
				forms.removeAt(i);
			}

			button = buttons[i];

			if(forms[i] != null) {
				DOM.removeCls(forms[i], 'display-none');
				forms[i].setActive(true);
			}
		} 

		if(form != null && forms.indexOf(form) == -1) {
			button = this.createBreadcrumb(form, forms.length + 1);
			if(forms[forms.length - 1] != null) {
				DOM.addCls(forms[forms.length - 1], 'display-none');
				forms[forms.length - 1].setActive(false);
			}
			breadcrumbs.add(button);
			body.add(form);
			forms.push(form);
			form.setActive(true);
		}

		this.focus();

		Application.setTitle(form != null ? form.getTitle() : null);
	},

	getJobMonitor: function() {
		if(this.jobMonitor == null)
			this.jobMonitor = new Z8.application.job.JobMonitor({ autoDestroy: false });
		return this.jobMonitor;
	},

	startJob: function(job) {
		var jobMonitor = this.getJobMonitor();
		jobMonitor.addJob(job);
	},

	open: function(params, closeOthers, config, fromHistory) {
		if(this.isOpeningForm)
			return;

		this.isOpeningForm = true;

		var callback = function(response, success) {
			this.menuToggle.setBusy(false);
			this.isOpeningForm = false;

			if(!success)
				return;

			if(!response.isJob) {
				response.id = params.id;
				response.where = params.where;
				response.filter = params.filter;
				response.period = params.period;
				response.sourceFilter = params.sourceFilter;

				var formId = Component.nextId();
				var formConfig = Z8.apply({ store: response, formId: formId }, config);
				var formCls = Application.getSubclass(response.ui);
				var form = formCls != null ? Z8.create(formCls, formConfig) : new Z8.application.form.Navigator(formConfig);

				var state = { z8: true, params : params, closeOthers: closeOthers, config: config, formId: formId };
				var history = window.history;
				fromHistory ? history.replaceState(state, "", "") : history.pushState(state, "", "");

				this.openForm(form, closeOthers);
			} else {
				var job = new Z8.application.job.Job(response);
				this.startJob(job);
			}
		};

		if(String.isString(params))
			params = { request: params };
		if(params.filter == null)
			params.filter = User.getFilter(params.request).getActive();
		if(params.period == null)
			params.period = User.getPeriod(params.request);

		this.menuToggle.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });
	},

	openJobMonitor: function() {
		this.getJobMonitor().open();
	},

	onSettings: function() {
/*
		var view1 = new forms.View1();
		this.openForm(view1, true);
*/
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.openMenu();
			event.stopEvent();
		} else if((key == Event.MINUS || key == Event.NUM_MINUS) && event.shiftKey && event.altKey && (!DOM.isInput(target) || DOM.isReadOnly(target))) {
			Ems.enlarge(-1);
			event.stopEvent();
		} else if((key == Event.PLUS || key == Event.NUM_PLUS) && event.shiftKey && event.altKey && (!DOM.isInput(target) || DOM.isReadOnly(target))) {
			Ems.enlarge(1);
			event.stopEvent();
		} else if((key == Event.ZERO || key == Event.NUM_ZERO) && event.shiftKey && event.altKey && (!DOM.isInput(target) || DOM.isReadOnly(target))) {
			Ems.reset();
			event.stopEvent();
		}
	},

	onPopState: function(event, target) {
		var state = event.state;
		if(state != null && state.z8) {
			var form = this.getFormById(state.formId);
			if(form != null)
				this.openForm(form, state.closeOthers);
			else
				this.open(state.params, state.closeOthers, state.config, true);
		} else
			this.openForm(null);
		event.stopEvent();
	}
});