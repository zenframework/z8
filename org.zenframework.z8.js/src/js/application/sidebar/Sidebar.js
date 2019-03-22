Z8.define('Z8.application.sidebar.Sidebar', {
	extend: 'Z8.application.sidebar.Menu',

	cls: 'menu',
	tabIndex: 0,

	isOpen: false,

	htmlMarkup: function() {
		this.itemIndex = {};

		this.data = JSON.decode(JSON.encode(User.data));

		var handle = { tag: 'i', cls: 'handle fa fa-angle-right' };
		var logo = { cls: 'logo' };

		this.items = [handle, logo];
		return this.callParent();
	},

	completeRender: function() {
		this.callParent();

		this.handle = DOM.selectNode('.handle');

		DOM.on(this, 'focus', this.onFocus, this, true);
		DOM.on(this, 'blur', this.onBlur, this, true);
		DOM.on(this, 'mouseOver', this.onMouseOver, this);
		DOM.on(this, 'mouseOut', this.onMouseOut, this);
	},

	onDestroy: function() {
		DOM.un(this, 'focus', this.onFocus, this, true);
		DOM.un(this, 'blur', this.onBlur, this, true);
		DOM.un(this, 'mouseOver', this.onMouseOver, this);
		DOM.un(this, 'mouseOut', this.onMouseOut, this);
		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);
		this.callParent();
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	open: function() {
		if(this.isOpen)
			return;

		DOM.addCls(this, 'open', 10);
		this.isOpen = true;

		this.focus();

		DOM.on(document.body, 'mouseDown', this.monitorOuterClick, this);

		this.fireEvent('open', this);
	},

	close: function() {
		if(!this.isOpen)
			return;

		this.deactivate();

		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);

		DOM.removeCls(this, 'open');
		DOM.setCssText(this, '', 100);

		this.isOpen = false;

		this.fireEvent('close', this);
	},

	toggle: function() {
		this.isOpen ? this.close() : this.open();
	},

	monitorOuterClick: function(event) {
		var target = event.target;

		if(!this.isInnerElement(target))
			this.close();
	},

	onSelectItem: function(item) {
		this.close();
		Z8.callback(this.handler, this.scope, item.data);
	},

	onFocus: function(event, target) {
		this.open();
	},

	isInnerElement: function(element) {
		return DOM.isParentOf(this, element) || DOM.isParentOf(this.owner, element);
	},

	onBlur: function(event, target) {
		if(this.closeOnBlur && !DOM.isParentOf(this, event.relatedTarget))
			this.close();
	},

	onMouseOver: function(event, target) {
		if(!this.isOpen && target == this.handle)
			this.open();
	},

	onMouseOut: function(event, target) {
		var target = event.relatedTarget;
		if(this.closeOnMouseOut && target != null && !this.isInnerElement(target))
			this.close();
		this.closeOnMouseOut = true;
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.close();
			event.stopEvent();
		} else
			this.callParent(event, target);
	}
});