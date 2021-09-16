Z8.define('Z8.list.HeaderCheck', {
	extend: 'HeaderIcon',

	cls: 'check',

	isChecked: function() {
		return this.checked;
	},

	getCheckBoxCls: function(value) {
		return DOM.parseCls(value ? CheckBox.OnIconCls : CheckBox.OffIconCls).pushIf(value ? 'on' : 'off').pushIf('icon');
	},

	setChecked: function(checked) {
		this.checked = checked;
		DOM.setCls(this.icon, this.getCheckBoxCls(checked));
		return checked;
	},

	toggleCheck: function() {
		return this.setChecked(!this.checked);
	},

	htmlMarkup: function() {
		this.icon = this.getCheckBoxCls(this.checked);
		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'click', this.onClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		this.callParent();
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(this.enabled)
			this.list.onCheckHeaderClick(this);
	}
});
