Z8.define('Z8.application.viewport.Form', {
	extend: 'Z8.Container',
	shortClassName: 'viewport.Form',

	setTitle: function(title) {
		this.callParent(title);
		this.fireEvent('title', this, title);
	},

	getIcon: function() {
		return this.icon;
	},

	setIcon: function(icon) {
		this.icon = icon;
	}
});

