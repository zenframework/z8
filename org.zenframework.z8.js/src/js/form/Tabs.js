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
			DOM.addCls(this.getActiveTab(), 'inactive');
			var active = this.activeTab = tag.tab;
			DOM.removeCls(active, 'inactive');
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
		DOM.swapCls(tab.tag, !show, 'display-none');
		if(!show && tab == this.getActiveTab())
			DOM.addCls(tab, 'inactive');
	},

	activateTab: function(tab) {
		tab.tag.setToggled(true);
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

});