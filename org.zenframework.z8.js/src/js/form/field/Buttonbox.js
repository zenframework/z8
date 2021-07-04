Z8.define('Z8.form.field.ButtonBox', {
	extend: 'Control',
	shortClassName: 'ButtonBox',

	getCls: function() {
		return Control.prototype.getCls.call(this).pushIf('buttonbox');
	},

	initComponent: function() {
		Control.prototype.initComponent.call(this);
		this.buttons = new ButtonGroup({ items: this.items });
	},

	getButtonGroup: function() {
		return this.buttons;
	},

	getItems: function() {
		return this.buttons.getItems();
	},

	setItems: function(items) {
		return this.buttons.setItems(items);
	},

	controlMarkup: function() {
		return this.buttons.htmlMarkup();
	},

	subcomponents: function() {
		return Control.prototype.subcomponents.call(this).add(this.buttons);
	}
});
