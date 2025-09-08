Z8.define('Z8.form.Tab', {
	extend: 'Z8.form.Fieldset',
	shortClassName: 'Tab',

	plain: true,

	getTabs: function() {
		return this.tabs; 
	},

	getTabTag: function() {
		return this.tabTag;
	},

	setIcon: function(icon) {
		this.getTabTag().setIcon(icon);
		return this;
	},

	setTitle: function(title) {
		this.getTabTag().setText(title);
		return this;
	},

	setExtra: function(text, title) {
		if (text != null){
			this.getTabTag().setCounter(text);
		}
		if (title != null)
			this.getTabTag().setTooltip(title);
		return this;
	},

	setBusy: function(busy) {
		return this.getTabTag().setBusy(busy);
	},

	show: function(show) {
		this.getTabTag().show(show);
		this.callParent(show);

		if(show !== undefined && !show && this == this.getTabs().getActiveTab())
			this.deactivate();

		return this;
	},

	hide: function(hide) {
		this.getTabTag().hide(hide);
		this.callParent(hide);

		if((hide === undefined || !hide) && this == this.getTabs().getActiveTab())
			this.deactivate();

		return this;
	},

	activate: function() {
		var tabs = this.getTabs();
		var activeTab = tabs.getActiveTab();

		if(activeTab == this)
			return this;

		if(activeTab != null)
			activeTab.deactivate();

		DOM.removeCls(this, 'inactive');
		tabs.setActiveTab(this);

		this.show();
		this.getTabTag().setToggled(true, true);

		tabs.onActivateTab(this);
		return this;
	},

	deactivate: function() {
		this.getTabs().onDeactivateTab(this);
		DOM.addCls(this, 'inactive');
		return this;
	},

	onDestroy: function() {
		this.getTabTag().destroy();
		this.callParent();
	},

	setText: function(text) {
		var tag = this.tag;
		if(tag != null)
			tag.setText(text);
	},
});