Z8.define('Z8.list.Dropdown', {
	extend: 'Z8.list.List',

	visible: false,

	cls: 'dropdown-list display-none',
	manualItemsRendering: true,

	confirmSelection: true,
	autoSelectFirst: false,

	autoAlign: true,

	createItem: function(record) {
		var item = Z8.list.List.prototype.createItem.call(this, record);
		item.follow = false;
		return item;
	},

	render: function(container) {
		if(!this.inRender) {
			Z8.list.List.prototype.render.call(this, container);
			this.renderItems();
		}
	},

	renderItems: function() {
		var justRendered = Z8.list.List.prototype.renderItems.call(this);
		this.needAlign = this.needAlign || justRendered;

		if(this.visible && this.needAlign && !this.inShow) {
			this.inRender = true;
			this.show(null, null, false);
			this.inRender = false;
		}

		return justRendered;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.Z8.list.List.prototype.setTabIndex.call(this, tabIndex);

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

		if(!this.visible)
			this.fireEvent('beforeShow', this);

		this.render();

		if(this.visible && !this.needAlign) {
			if(focusAt !== false)
				this.focus(focusAt);
			this.inShow = false;
			return;
		}

		this.needAlign = false;

		this.setPosition(left, top);

		var wasVisible = this.visible;

		Z8.list.List.prototype.show.call(this);
		this.setActive(true);

		if(!wasVisible)
			this.fireEvent('show', this);

		this.alignAdjust();

		if(focusAt !== false)
			this.focus(focusAt);

		this.inShow = false;
	},

	align: function() {
		DOM.setBottom(this, 'auto');
		Z8.list.List.prototype.align.call(this);
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

		this.fireEvent('hide', this);

		this.setActive(false);
		this.selectItem(null);

		Z8.list.List.prototype.hide.call(this);
	},

	toggle: function() {
		this.visible ? this.hide() : this.show();
	},

	setSelection: function(item) {
		this.hide();
		Z8.list.List.prototype.setSelection.call(this, item);
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

		Z8.list.List.prototype.onKeyDown.call(this, event, target);
	}
});