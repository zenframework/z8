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
		var tabTag = this.getTabTag();
		if (tabTag != null)
			tabTag.setIcon(icon);
		return this;
	},

	setTitle: function(title) {
		var tabTag = this.getTabTag();
		if (tabTag != null)
			tabTag.setText(title);
		return this;
	},

	setExtra: function(text, title) {
		var tabTag = this.getTabTag();
		if (tabTag != null && text != null){
			tabTag.setCounter(text);
		}
		if (tabTag != null && title != null)
			tabTag.setTooltip(title);
		return this;
	},

	setBusy: function(busy) {
		var tabTag = this.getTabTag();
		if (tabTag != null)
			return tabTag.setBusy(busy);
	},

	show: function(show) {
		var tabTag = this.getTabTag();
		if (tabTag)
			tabTag.show(show);

		this.callParent(show);

		var tabs = this.getTabs();
		if(show !== undefined && !show && (tabs != null && this == tabs.getActiveTab()))
			this.deactivate();

		return this;
	},

	hide: function(hide) {
		var tabTag = this.getTabTag();
		if (tabTag)
			tabTag.hide(hide);

		this.callParent(hide);

		var tabs = this.getTabs();
		if((hide === undefined || !hide) && (tabs != null && this == tabs.getActiveTab()))
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
		var tabTag = this.getTabTag();
		if (tabTag)
			tabTag.destroy();
		this.callParent();
	},

	setText: function(text) {
		this.setTitle(text);
	},
});