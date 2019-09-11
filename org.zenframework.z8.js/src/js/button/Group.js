Z8.define('Z8.button.Group', {
	extend: 'Z8.Container',

	vertical: false,
	radio: false,

	constructor: function(config) {
		config = config || {};
		config.items = config.items || [];

		this.callParent(config);

		this.cls = DOM.parseCls(this.cls).pushIf('btn-group' + (this.vertical ? '-vertical' : ''));
	},

	getToggled: function() {
		return this.toggled;
	},

	onRadioToggle:function(button, toggled) {
		if(this.updatingRadioState)
			return;

		this.updatingRadioState = true;

		var items = this.items;

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item != button && item.toggled)
				item.setToggled(false, true);
		}

		this.toggled = button;

		this.updatingRadioState = false;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		var items = this.items;

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isComponent)
				item.setTabIndex(tabIndex);
		}
	},

	focus: function() {
		if(this.enabled) {
			var toggled = this.getToggled();
			return toggled != null && toggled.focus() ? true : this.callParent();
		}
		return false;
	}
});
