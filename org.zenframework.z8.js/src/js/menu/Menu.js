Z8.define('Z8.menu.Menu', {
	extend: 'Z8.list.Dropdown',

	cls: 'dropdown-menu display-none',
	itemType: 'Z8.menu.Item',
	manualItemsRendering: true,
	useTab: true,
	headers: false,
	checks: false,
	autoSelectFirst: false,

	getOwner: function() {
		return this.owner;
	},

	setOwner: function(owner) {
		this.owner = owner;
	},

	setItems: function(items) {
		for(var i = 0, length = items.length; i < length; i++)
			items[i].list = this;

		Z8.list.Dropdown.prototype.setItems.call(this, items);
	},

	completeRender: function() {
		Z8.list.Dropdown.prototype.completeRender.call(this);
		DOM.on(this, 'blur', this.onFocusOut, this);
	},

	onDestroy: function() {
		DOM.un(this, 'blur', this.onFocusOut, this);
		Z8.list.Dropdown.prototype.onDestroy.call(this);
	},

	onFocusOut: function(event, target) {
		var dom = DOM.get(this);
		target = event.relatedTarget;

		if(dom != target && !DOM.isParentOf(dom, target) && !DOM.isParentOf(this.owner, target))
			this.onCancel();
	},

	onIconClick: function(item) {
		this.onItemClick(item, -1);
	}
});