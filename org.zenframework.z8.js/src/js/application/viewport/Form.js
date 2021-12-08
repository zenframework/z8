Z8.define('Z8.application.viewport.Form', {
	extend: 'Z8.Container',
	shortClassName: 'ViewportForm',

	setTitle: function(title) {
		Z8.Container.prototype.setTitle(this, title);
		this.fireEvent('title', this, title);
	},

	getIcon: function() {
		return this.icon;
	},

	setIcon: function(icon) {
		this.icon = icon;
	},

	completeRender: function() {
		Z8.Container.prototype.completeRender.call(this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		Z8.Container.prototype.onDestroy.call(this);
	},

	onKeyDown: function(event, target) {
		switch(event.getKey()) {
		case Event.ESC:
			Viewport.closeForm(this);
			return event.stopEvent();
		}
	}
});

