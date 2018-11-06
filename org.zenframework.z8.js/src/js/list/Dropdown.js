Z8.define('Z8.list.Dropdown', {
	extend: 'Z8.list.List',

	visible: false,

	cls: 'dropdown-list display-none',
	manualItemsRendering: true,

	confirmSelection: true,
	autoSelectFirst: false,

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

		DOM.setBottom(this, 'auto');
		DOM.removeCls(this, 'display-none');

		var wasVisible = this.visible;
		this.visible = true;

		this.adjustAutoFit();
		this.align();

		if(!wasVisible)
			this.fireEvent('show', this);

		if(focusAt !== false)
			this.focus(focusAt);

		delete this.inShow;
	},

	hide: function() {
		if(!this.visible)
			return;

		this.visible = false;

		DOM.addCls(this, 'display-none');

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

		var ul = this.getDom();

		if(key == Event.ESC || key == Event.TAB && target == ul) {
			this.onCancel();
			event.stopEvent();
			return;
		}

		this.callParent(event, target);
	}
});