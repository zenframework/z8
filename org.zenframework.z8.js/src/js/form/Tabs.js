Z8.define('Z8.form.Tabs', {
	extend: 'Z8.Container',
	shortClassName: 'Tabs',

	initComponent: function() {
		this.minHeight = this.height = this.height || Ems.unitsToEms(6);
		this.callParent();
	},

	htmlMarkup: function() {
		this.setReadOnly(this.isReadOnly());

		var controls = this.controls || [];

		this.cls = DOM.parseCls(this.cls).pushIf('tabs', this.flex ? 'flexed' : '');
		this.headerCls = DOM.parseCls(this.headerCls).pushIf('header');
		this.bodyCls = DOM.parseCls(this.bodyCls).pushIf('body');

		var callback = function(tag, toggled) {
			this.activateTab(tag.tab);
		};

		var tags = [];
		var items = [];

		for(var i = 0, length = controls.length; i < length; i++) {
			var tab = controls[i];
			tab.cls = DOM.parseCls(tab.cls).pushIf('tab', i != 0 ? 'inactive' : '').join(' ');

			var tag = tab.tag = new Z8.button.Button({ cls: 'tag', toggle: true, text: tab.title, icon: tab.icon, tab: tab });
			tab.icon = null;

			tag.on('toggle', callback, this);
			tags.push(tag);
		}

		var header = this.header = new Z8.button.Group({ radio: true, cls: this.headerCls.join(' '), items: tags });
		var body = new Z8.Container({ cls: this.bodyCls.join(' '), items: controls });

		this.items = [header, body];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();

		if(!Z8.isEmpty(this.controls))
			this.activateTab(this.controls[0]);
	},

	getActiveTab: function() {
		return this.activeTab;
	},

	showTab: function(tab, show) {
		show ? tab.tag.show() : tab.tag.hide();
		if(!show && tab == this.getActiveTab())
			DOM.addCls(tab, 'inactive');
	},

	activateTab: function(activeTab) {
		if(this.activeTab == activeTab)
			return;

		if(this.activeTab != null)
			this.fireEvent('deactivateTab', this, this.activeTab);

		DOM.addCls(this.activeTab, 'inactive');
		this.activeTab = activeTab;
		DOM.removeCls(activeTab, 'inactive');

		activeTab.tag.setToggled(true, true);

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
		this.callParent(active);

		var activeTab = this.activeTab;
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var tab = controls[i];
			tab.setActive(active && tab == activeTab);
		}
	},
});