Z8.define('Z8.menu.Menu', {
	extend: 'Z8.list.Dropdown',

	cls: 'dropdown-menu display-none',
	itemType: 'Z8.menu.Item',
	manualItemsRendering: true,
	useTAB: true,
	headers: false,
	checks: false,
	autoSelectFirst: true,

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'blur', this.onFocusOut, this, true);
	},

	onDestroy: function() {
		DOM.un(this, 'blur', this.onFocusOut, this, true);
		this.callParent();
	},

	onFocusOut: function(event) {
		var dom = DOM.get(this);
		var target = event.relatedTarget;

		if(dom != target && !DOM.isParentOf(dom, target) && !DOM.isParentOf(this.owner, target))
			this.onCancel();
	},

	toggle: function() {
		if(this.getCount() != 0)
			this.callParent();
	}
});