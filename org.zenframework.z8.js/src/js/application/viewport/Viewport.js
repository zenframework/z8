var Viewport = null;

Z8.define('Z8.application.viewport.Viewport', {
	extend: 'Z8.Container',

	cls: 'air',

	handlers: [],
	forms: [],
	loginType: 'Z8.application.viewport.Login',

	initComponent: function() {
		this.callParent();
		this.setTitle(Application.name);

		if(Application.startupForm == null)
			this.forms = [null];

		var header = this.header = new Z8.Container({ cls: 'header', items: [] });
		var body = this.body = new Z8.Container({ cls: 'body' });
		var popupMessages = this.popupMessages = new Z8.viewport.PopupMessages();

		this.loginForm = Z8.create(this.loginType, { handler: this.loginCallback, scope: this });

		this.items = [header, body, popupMessages];
	},

	getCls: function() {
		return Z8.Container.prototype.getCls.call(this).pushIf('viewport');
	},

	logout: function() {
		if(!this.isLoggingIn) {
			this.onLogout();
			Application.login();
		} else
			this.loginForm.focus();
	},

	createHeaderButtons: function() {
		var jobMonitor = this.jobMonitorButton = new Z8.button.Button({ tooltip: Z8.$('Z8.Viewport.taskMonitor'), cls: 'btn-tool', icon: 'fa-tv', handler: this.openJobMonitor, scope: this });
		return [jobMonitor];
	},

	onLogin: function() {
		if(this.isLoggedIn) {
			this.focus();
			return;
		}

		this.isLoggedIn = true;

		var header = this.header;

		var buttons = this.buttons = this.createHeaderButtons();
		header.add(buttons, header.getCount() - 1);

		if(Application.startupForm != null) {
			var startupForm = Application.startupForm;
			if(typeof startupForm === 'function')
				startupForm = startupForm();
			if(startupForm != null)
				this.open(startupForm);
		}
	},

	onLogout: function() {
		if(!this.isLoggedIn)
			return;

		this.isLoggedIn = false;

		this.closeAllForms();

		var header = this.header;
		header.remove(this.buttons);

		if(this.jobMonitor != null)
			this.jobMonitor.destroy();

		this.body.removeAll();

		this.buttons = this.jobMonitor = null;
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

	onMenuItem: function(item) {
		this.open({ request: item.request, id: item.id }, true);
	},

	setTitle: function(title) {
		this.callParent(title);
		DOM.setInnerHtml(this.text, title);
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
		this.body.removeAll();
		this.forms = [];
	},

	openForm: function(form, closeOthers) {
		var store = form != null ? form.store : null;

		var forms = this.forms;
		var index = forms.indexOf(form || null);

		if(index == -1 && closeOthers)
			index = 0;

		var body = this.body;

		if(index != -1) {
			for(var i = forms.length - 1; i > index; i--) {
				body.remove(forms[i]);
				forms.removeAt(i);
			}

			if(forms[i] != null) {
				DOM.removeCls(forms[i], 'display-none');
				forms[i].setActive(true);
			}
		}

		if(form != null && forms.indexOf(form) == -1) {
			if(forms[forms.length - 1] != null) {
				DOM.addCls(forms[forms.length - 1], 'display-none');
				forms[forms.length - 1].setActive(false);
			}
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

	open: function(params, closeOthers, config) {
		if(this.isOpeningForm)
			return;

		this.isOpeningForm = true;

		var callback = function(response, success) {
			this.isOpeningForm = false;

			if(!success)
				return;

			if(!response.isJob) {
				response.id = params.id;
				response.where = params.where;
				response.filter = params.filter;
				response.period = params.period;

				var formId = Component.nextId();
				var formConfig = Z8.apply({ store: response, formId: formId }, config);
				var formCls = Application.getSubclass(response.ui);
				var form = formCls != null ? Z8.create(formCls, formConfig) : new Z8.application.form.Navigator(formConfig);

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

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	openJobMonitor: function() {
		this.getJobMonitor().open();
	}
});
