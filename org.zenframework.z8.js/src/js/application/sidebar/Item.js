Z8.define('Z8.application.sidebar.Item', {
	extend: 'Z8.Component',

	subcomponents: function() {
		return [this.menu];
	},

	htmlMarkup: function() {
		var data = this.data;
		var icon = { tag: 'i', cls: DOM.parseCls(data.icon).pushIf('fa', 'icon').join(' '), html: String.htmlText() };
		var text = { tag: 'span', cls: 'text', html: data.text };

		var cn = [icon, text];

		var items = data.items;
		if(!Z8.isEmpty(items)) {
			var chevron = { tag: 'i', cls: 'fa fa-chevron-right chevron' };
			cn.push(chevron);

			var menu = this.menu = new Z8.application.sidebar.Menu({ data: items, parent: this });
			cn.push(menu.htmlMarkup());
		}

		return { id: this.getId(), cls: 'item', tabIndex: this.getTabIndex(), cn: cn };
	},

	completeRender: function() {
		DOM.on(this, 'mouseOver', this.onMouseOver, this);
		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this, 'mouseOver', this.onMouseOver, this);
		this.callParent();
	},

	activate: function() {
		this.showMenu();
		DOM.addCls(this, 'active');
		DOM.setTabIndex(this, 0);
		this.focus();
	},

	deactivate: function() {
		DOM.removeCls(this, 'active');
		DOM.setTabIndex(this, -1);
		if(this.menu != null)
			this.menu.deactivate();
	},

	showMenu: function() {
		if(this.menu != null)
			this.menu.show();
	},

	hideMenu: function() {
		if(this.menu != null)
			this.menu.hide();
	},

	onMouseOver: function(event, target) {
		var parent = this.parent;
		parent.onBeforeActivateItem(this);
		this.activate(true);
		parent.onAfterActivateItem(this);
		event.stopEvent();
	}
});