Z8.define('Z8.form.field.SearchCheckbox', {
	extend: 'TripleCheckbox',
	shortClassName: 'SearchCheckbox',

	statics: {
		BusyIconCls: 'fa fa-spin fa-circle-o-notch disabled busy',
	},

	initComponent: function() {
		this.callParent();

		this.busyCls = DOM.parseCls(SearchCheckbox.BusyIconCls).pushIf('control');
	},

	getIconCls: function() {
		return this.busy ? this.busyCls : this.callParent();
	},

	setValue: function(value) {
		if (this.isBusy())
			return;

		this.callParent(value);
		this.fireEvent('search', this, value);
	},

	getFilter: function() {
		if(this.field == null)
			return null;

		var value = this.getValue();
		return !Z8.isEmpty(value) ? { property: this.field.name, value: value } : null;
	},

	setBusy: function(busy) {
		this.busy = busy;
		DOM.setCls(this.icon, this.getIconCls());
	},

	isBusy: function() {
		return this.busy;
	},

	onClick: function(event, target) {
		if (this.isBusy())
			return event.stopEvent();

		this.callParent(event, target);
	},

	onContextMenu: function(event, target) {
		if (this.isBusy())
			return event.stopEvent();

		this.callParent(event, target);
	},
});