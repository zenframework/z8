Z8.define('z8.app.Viewport', {
	extend: 'Z8.application.viewport.Viewport',

	completeRender: function() {
		this.callParent();
		DOM.on(window, 'resize', this.onResize, this);
		this.onResize();
	},

	onDestroy: function() {
		DOM.un(window, 'resize', this.onResize, this);
		this.callParent();
	},

	createHeaderButtons: function() {
		var buttons = this.callParent();

		var login = User.login;
		var user = this.userButton = new Z8.button.Button({ text: login, tooltip: login, cls: 'btn-tool', icon: 'fa-user', enabled: true, handler: this.onSettings, scope: this });

		return buttons.insert(user, 0);
	},
	
	onSettings: function() {
		// Viewport.open({ request: <personal-settings-class> }, false);
	},
	
	onResize: function(event, target) {
		var width = document.body.clientWidth;
		Ems.setBase(width < 768 ? 11 : width < 900 ? 12 : 13, true);
	}
});