Z8.define('Z8.form.Tabs', {
	extend: 'Container',
	shortClassName: 'Tabs',

	isTabControl: true,

	mixins: ['Z8.form.field.Field'],

	getCls: function() {
		return Container.prototype.getCls.call(this).pushIf('tabs', this.flex ? 'flex' : '');
	},

	constructor: function (config) {
		config = config || {};
		this.controls = config.controls || [];

		Container.prototype.constructor.call(this, config);
	},

	initComponent: function() {
		Container.prototype.initComponent.call(this);

		this.setReadOnly(this.isReadOnly());

		this.headerCls = DOM.parseCls(this.headerCls).pushIf('header');
		this.bodyCls = DOM.parseCls(this.bodyCls).pushIf('body');

		var tags = [];
		for(var tab of this.getTabs())
			tags.push(this.initializeTab(tab).getTabTag());

		this.header = new ButtonGroup({ radio: true, cls: this.headerCls.join(' '), items: tags });
		this.body = new Container({ cls: this.bodyCls.join(' '), items: this.getTabs() });

		this.setItems([this.header, this.body]);
	},

	getTabs: function() {
		return this.controls;
	},

	getTab: function(index) {
		return this.controls[index];
	},

	getActiveTab: function() {
		return this.activeTab;
	},

	setActiveTab: function(tab) {
		this.activeTab = tab;
		this.updateTabsActiveState();
		return this;
	},

	initializeTab: function(tab) {
		tab.getTabs = function() {
			return this.tabs; 
		};

		tab.getTabTag = function() {
			return this.tabTag;
		};

		tab.setIcon = function(icon) {
			this.getTabTag().setIcon(icon);
			return this;
		};

		tab.setTitle = function(title) {
			this.getTabTag().setText(title);
			return this;
		};

		tab.setBusy = function(busy) {
			return this.getTabTag().setBusy(busy);
		};

		tab.protoShow = tab.show;
		tab.show = function(show) {
			this.getTabTag().show(show);
			this.protoShow(show);

			if(show !== undefined && !show && tab == this.getTabs().getActiveTab())
				tab.deactivate();

			return this;
		};

		tab.protoHide = tab.hide;
		tab.hide = function(hide) {
			this.getTabTag().hide(hide);
			this.protoHide(hide);

			if((hide === undefined || !hide) && tab == this.getTabs().getActiveTab())
				tab.deactivate();

			return this;
		};

		tab.activate = function() {
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
		};

		tab.deactivate = function() {
			this.getTabs().onDeactivateTab(this);
			DOM.addCls(this, 'inactive');
			return this;
		};

		tab.protoOnDestroy = tab.onDestroy;
		tab.onDestroy = function() {
			this.getTabTag().destroy();
			this.protoOnDestroy();
		};

		tab.tabTag = new Z8.button.Button({ cls: DOM.parseCls(tab.cls).pushIf('tag'), visible: tab.visible, toggle: true, text: tab.getTitle(), icon: tab.icon, tab: tab, toggleHandler: this.onTabToggle, scope: this });
		tab.cls = DOM.parseCls(tab.cls).pushIf('tab', 'inactive');
		tab.icon = null;
		tab.tabs = this;

		return tab;
	},

	onTabToggle: function(tabTag, toggled) {
		if(!toggled)
			return;

		var tab = tabTag.tab;
		this.activateTab(tab);
		tab.focus();
	},

	onActivateTab: function(tab) {
		this.fireEvent('activateTab', this, tab);
	},

	onDeactivateTab: function(tab) {
		this.fireEvent('deactivateTab', this, tab);
	},

	showTab: function(tab, show) {
		if(tab != null)
			tab.show(show);
	},

	activateTab: function(tab) {
		if(tab != null)
			tab.activate();
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		for(var tab of this.getTabs()) {
			if(!tab.readOnlyLock && tab.setReadOnly != null)
				tab.setReadOnly(readOnly);
		}

		this.readOnly = readOnly;
		DOM.swapCls(this, readOnly, 'readonly');
	},

	setActive: function(active) {
		if((this.isActive() ^ active) == 0)
			return;

		Component.prototype.setActive.call(this, active); /* Component.prototype not Container.prototype */ 

		var activeTab = this.getActiveTab();

		if(active && activeTab === undefined) {
			var tab = this.getFirstVisible(this.defaultTabIndex);
			if(tab != null)
				tab.activate();
		}

		this.updateTabsActiveState();
	},

	add: function(tab, index) {
		var tag = this.initializeTab(tab).getTabTag();
		this.header.add(tag, index);
		this.body.add(tab, index);
	},

	remove: function (tab) {
		this.controls.remove(tab);

		var tag = tab.getTabTag();
		this.header.remove(tag);
		this.body.remove(tab);
	},

	clearTabs: function () {
		this.header.removeAll();
		this.body.removeAll();
	},

	updateTabsActiveState: function() {
		var isActive = this.isActive();
		var activeTab = this.getActiveTab();

		for(var tab of this.getTabs())
			tab.setActive(isActive && tab == activeTab);
	},

	getFirstVisible: function(index) {
		for(var i = index || 0, tabs = this.getTabs(), length = tabs.length; i < length; i++) {
			var tab = tabs[i];
			if(tab.isVisible())
				return tab;
		}
		return null;
	}
});