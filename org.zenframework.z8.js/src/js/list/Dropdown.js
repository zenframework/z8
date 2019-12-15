Z8.define('Z8.list.Dropdown', {
	extend: 'Z8.list.List',

	visible: false,

	cls: 'dropdown-list display-none',
	manualItemsRendering: true,

	confirmSelection: true,
	autoSelectFirst: false,

	autoAlign: true,

	createItem: function(record) {
		var item = this.callParent(record);
		item.follow = false;
		return item;
	},

	render: function(container) {
		if(!this.inRender) {
			this.callParent(container);
			this.renderItems();
		}
	},

	renderItems: function() {
		var justRendered = this.callParent();
		this.needAlign = this.needAlign || justRendered;

		if(this.visible && this.needAlign && !this.inShow) {
			this.inRender = true;
			this.show(null, null, false);
			delete this.inRender;
		}

		return justRendered;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		var items = this.getItems();

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(!String.isString(item))
				item.setTabIndex(tabIndex);
		}

		return tabIndex;
	},

	show: function(left, top, focusAt) {
		this.inShow = true;

		this.render();

		if(this.visible && !this.needAlign) {
			if(focusAt !== false)
				this.focus(focusAt);
			delete this.inShow;
			return;
		}

		this.needAlign = false;

		this.setPosition(left, top);

		var wasVisible = this.visible;

		this.callParent();
		this.setActive(true);

		this.alignAdjust();

		if(!wasVisible)
			this.fireEvent('show', this);

		if(focusAt !== false)
			this.focus(focusAt);

		delete this.inShow;
	},

	align: function() {
		DOM.setBottom(this, 'auto');
		this.callParent();
	},

	alignAdjust: function() {
		this.align();
		this.adjustAutoFit();

		if(this.aligned)
			return;

		this.aligned = true;

		this.align();
		this.adjustAutoFit();
	},

	hide: function() {
		if(!this.isVisible())
			return;

		this.callParent();
		this.setActive(false);

		this.selectItem(null);

		this.fireEvent('hide', this);
	},

	toggle: function() {
		this.visible ? this.hide() : this.show();
	},

	setSelection: function(item) {
		this.hide();
		this.callParent(item);
	},

	onCancel: function() {
		this.hide();
		this.fireEvent('cancel', this);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC || key == Event.TAB && target == this.getDom()) {
			this.onCancel();
			event.stopEvent();
			return;
		}

		this.callParent(event, target);
	}
});