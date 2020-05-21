Z8.define('Z8.form.field.Html', {
	extend: 'Z8.form.field.Control',

	scrollable: true,

	getCls: function() {
		return Z8.form.field.Control.prototype.getCls.call(this).pushIf('html');
	},

	controlMarkup: function() {
		return [{ tag: 'div', cls: 'control', tabIndex: this.getTabIndex(), html: this.getValue() }];
	},

	completeRender: function() {
		this.callParent();
		this.div = this.selectNode('.control');
	},

	onDestroy: function() {
		this.div = null;
		this.callParent();
	},

	setValue: function(value, displayValue) {
		this.callParent(value, displayValue);
		DOM.setInnerHtml(this.div, value);
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this.div, !enabled, 'disabled');
		this.callParent(enabled);
	},

	setScrollTop: function(scrollTop) {
		this.div.scrollTop = scrollTop;
	},

	setScrollLeft: function(scrollLeft) {
		this.div.scrollLeft = scrollLeft;
	}
});