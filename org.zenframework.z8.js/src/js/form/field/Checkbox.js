Z8.define('Z8.form.field.Checkbox', {
	extend: 'Control',
	shortClassName: 'CheckBox',

	statics: {
		OnIconCls: 'fa fa-check-square',
		OffIconCls: 'fa fa-square-o'
	},

	isCheckBox: true,
	instantAutoSave: true,

	initComponent: function() {
		Control.prototype.initComponent.call(this);

		this.onCls = DOM.parseCls(this.onCls || CheckBox.OnIconCls).pushIf('control');
		this.offCls = DOM.parseCls(this.offCls || CheckBox.OffIconCls).pushIf('control');
	},

	getCls: function() {
		return Control.prototype.getCls.call(this).pushIf('checkbox');
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
		var markup = Control.prototype.htmlMarkup.call(this);
		markup.tabIndex = this.getTabIndex();
		return markup;
	},

	completeRender: function() {
		this.icon = this.selectNode('div[name=icon]');

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		Control.prototype.completeRender.call(this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.icon = null;

		Control.prototype.onDestroy.call(this);
	},

	isEqualValues: function(value1, value2) {
		return value1 && value2 || !(value1 || value2);
	},

	getIconCls: function() {
		return this.getValue() ? this.onCls : this.offCls;
	},

	setValue: function(value) {
		Control.prototype.setValue.call(this, value);
		DOM.setCls(this.icon, this.getIconCls());
	},

	setTabIndex: function(tabIndex) {
		tabIndex = Control.prototype.setTabIndex.call(this, tabIndex);
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