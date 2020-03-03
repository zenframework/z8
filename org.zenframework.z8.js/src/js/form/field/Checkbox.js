Z8.define('Z8.form.field.Checkbox', {
	extend: 'Z8.form.field.Control',

	isCheckbox: true,
	instantAutoSave: true,

	initComponent: function() {
		this.callParent();

		this.onCls = DOM.parseCls(this.onCls || 'fa-check-square fa control');
		this.offCls = DOM.parseCls(this.offCls || 'fa-square-o fa control');
	},

	getCls: function() {
		return Z8.form.field.Control.prototype.getCls.call(this).pushIf('checkbox');
	},

	isValid: function() {
		return true;
	},

	validate: function() {},

	controlMarkup: function() {
		var label = this.label;

		if(label != null) {
			label.align = label.align || 'right';
			label.icon = null;
		}

		return [{ tag: 'div', name: 'icon', cls: this.getIconCls().join(' '), html: String.htmlText('') }];
	},

	htmlMarkup: function() {
		var markup = this.callParent();
		markup.tabIndex = this.getTabIndex();
		return markup;
	},

	completeRender: function() {
		this.icon = this.selectNode('div[name=icon]');

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.icon = null;

		this.callParent();
	},

	isEqualValues: function(value1, value2) {
		return value1 && value2 || !(value1 || value2);
	},

	getIconCls: function() {
		return this.getValue() ? this.onCls : this.offCls;
	},

	setValue: function(value) {
		this.callParent(value);

		DOM.setCls(this.icon, this.getIconCls());
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);
		DOM.setTabIndex(this, tabIndex);
		return tabIndex;
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled() || this.isReadOnly())
			return;

		this.setValue(!this.getValue());
	},

	onKeyDown: function(event, target) {
		if(!this.isEnabled() || this.isReadOnly())
			return;

		var key = event.getKey();

		if(key == Event.SPACE)
			this.onClick(event, target);
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this) : false;
	}
});