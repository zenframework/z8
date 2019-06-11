Z8.define('Z8.form.field.Html', {
	extend: 'Z8.form.field.Control',

	scrollable: true,

	initComponent: function() {
		this.callParent();
	},

	controlMarkup: function() {
		var cls = DOM.parseCls(this.cls).pushIf('control', 'html-text');
		return [{ tag: 'div', cls: cls.join(' '), tabIndex: this.getTabIndex(), html: this.getValue() }];
	},

	completeRender: function() {
		this.callParent();
		this.div = this.selectNode('.html-text');
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