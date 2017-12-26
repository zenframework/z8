Z8.define('Z8.list.HeaderCheck', {
	extend: 'Z8.list.HeaderIcon',

	cls: 'check',

	isChecked: function() {
		return this.checked;
	},

	setChecked: function(checked) {
		this.checked = checked;
		DOM.swapCls(this.icon, checked, 'fa-check-square', 'fa-square-o');
		return checked;
	},

	toggleCheck: function() {
		return this.setChecked(!this.checked);
	},

	htmlMarkup: function() {
		this.icon = this.checked ? 'fa-check-square' : 'fa-square-o';
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
