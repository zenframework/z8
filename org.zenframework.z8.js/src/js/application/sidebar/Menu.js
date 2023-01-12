Z8.define('Z8.application.sidebar.Menu', {
	extend: 'Z8.Container',

	cls: 'submenu',

	closeOnMouseOut: false,
	closeOnBlur: true,

	htmlMarkup: function() {
		var items = this.items;
		var menuItems = this.menuItems = [];
		var data = this.data;

		for(var i = 0, length = data.length; i < length; i++) {
			var item = new Z8.application.sidebar.Item({ data: data[i], parent: this });
			items.push(item);
			menuItems.push(item);
		}

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);
		DOM.on(this, 'click', this.onClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		DOM.un(this, 'click', this.onClick, this);
		this.callParent();
	},

	getTopLevelParent: function() {
		var item = this;

		while(item != null) {
			var parent = item.parent;
			if(parent == null)
				return item;
			item = parent;
		}
	},

	onBeforeActivateItem: function(item) {
		this.activate(item);
	},

	onAfterActivateItem: function(item) {
	},

	deactivate: function() {
		var active = this.active;
		if(active != null) {
			active.deactivate();
			delete this.active;
		}
	},

	activate: function(item) {
		item = item || this.menuItems[0];

		if(this.active != item) {
			this.deactivate();
			this.active = item;
			if(item != null)
				item.activate();
		}
	},

	hide: function() {
		this.deactivate();
		this.callParent();
	},

	onClick: function(event, target) {
		var active = this.active;
		var dom = DOM.get(active);
		if((dom == target || DOM.isParentOf(dom, target)) && active.data.request != null)
			this.getTopLevelParent().onSelectItem(active);
	},

	onKeyDown: function(event, target) {
		var items = this.menuItems;

		var active = this.active;
		var parent = this.parent;
		var key = event.getKey();

		if(key == Event.DOWN || key == Event.UP) {
			var index = items.indexOf(active);
			index = key == Event.DOWN ? (index != -1 && index < items.length - 1 ? index + 1 : 0) :
				(index != -1 && index > 0 ? index - 1 : items.length - 1);
			this.activate(items[index]);
			event.stopEvent();
		} else if(key == Event.LEFT && parent != null) {
			var topLevelParent = this.getTopLevelParent();
			topLevelParent.closeOnMouseOut = false;
			parent.activate();
			parent.hideMenu();
			event.stopEvent();
		} else if(key == Event.RIGHT && active != null && active.menu != null) {
			active.showMenu();
			active.menu.activate();
			event.stopEvent();
		} else if(key == Event.ENTER && active != null && active.data.request != null)
			this.getTopLevelParent().onSelectItem(active);
	},

	onSelectItem: function(item) {
	}
});
