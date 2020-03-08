Z8.define('Z8.form.Tabs', {
	extend: 'Z8.Container',
	shortClassName: 'Tabs',

	isTabControl: true,

	mixins: ['Z8.form.field.Field'],

	getCls: function() {
		return Z8.Container.prototype.getCls.call(this).pushIf('tabs', this.flex ? 'flex' : '');
	},

	htmlMarkup: function() {
		this.setReadOnly(this.isReadOnly());

		var controls = this.controls || [];

		this.headerCls = DOM.parseCls(this.headerCls).pushIf('header');
		this.bodyCls = DOM.parseCls(this.bodyCls).pushIf('body');

		var callback = function(tagButton, toggled) {
			this.activateTab(tagButton.tab);
			if(!this.activateLock) {
				this.lastClickedTab = tagButton.tab;
				tagButton.tab.focus();
			}
		};

		var tagButtons = [];
		var items = [];

		for(var i = 0, length = controls.length; i < length; i++) {
			var tab = controls[i];
			tab.cls = DOM.parseCls(tab.cls).pushIf('tab', 'inactive').join(' ');

			var tagButton = tab.tagButton = new Z8.button.Button({ cls: 'tag', toggle: true, text: tab.title, icon: tab.icon, tab: tab });
			tab.icon = null;

			tagButton.on('toggle', callback, this);
			tagButtons.push(tagButton);
		}

		var header = this.header = new Z8.button.Group({ radio: true, cls: this.headerCls.join(' '), items: tagButtons });
		var body = new Z8.Container({ cls: this.bodyCls.join(' '), items: controls });

		this.items = [header, body];

		return this.callParent();
	},

	getActiveTab: function() {
		return this.activeTab;
	},

	showTab: function(tab, show) {
		if(tab == null)
			return;
		tab.tagButton.show(show);
		if(!show && tab == this.getActiveTab())
			DOM.addCls(tab, 'inactive');
	},

	activateTab: function(activeTab) {
		activeTab = activeTab || null;

		if(this.activateLock || this.activeTab === activeTab)
			return;

		if(this.activeTab != null) {
			this.fireEvent('deactivateTab', this, this.activeTab);
			this.activeTab.setActive(false);
		}

		DOM.addCls(this.activeTab, 'inactive');
		this.activeTab = activeTab;

		if(activeTab == null)
			return;

		DOM.removeCls(activeTab, 'inactive');

		this.activateLock = true;
		activeTab.tagButton.setToggled(true);
		this.activateLock = false;

		this.onActivateTab(activeTab);

		this.fireEvent('activateTab', this, this.activeTab);
	},

	onActivateTab: function(activeTab) {
		var tabs = this.controls;
		var isActive = this.isActive();
		for(var i = 0, length = tabs.length; i < length; i++) {
			var tab = tabs[i];
			tab.setActive(isActive && tab == activeTab);
		}
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];

			if(!control.readOnlyLock && control.setReadOnly != null)
				control.setReadOnly(readOnly);
		}

		this.readOnly = readOnly;
		DOM.swapCls(this, readOnly, 'readonly');
	},

	setActive: function(active) {
		if(this.isActive() == active)
			return;

		this.inactive = !active;

		var activeTab = this.activeTab;

		if(active && this.activeTab === undefined)
			this.activateTab(this.controls[this.defaultTabIndex || 0]);

		var activeTab = this.activeTab;
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var tab = controls[i];
			tab.setActive(active && tab == activeTab);
		}
	},
});