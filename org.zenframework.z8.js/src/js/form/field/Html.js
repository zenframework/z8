Z8.define('Z8.form.field.Html', {
	extend: 'Z8.form.field.Control',

	scrollable: true,

	getCls: function() {
		return Z8.form.field.Text.prototype.getCls.call(this).pushIf('html');
	},

	controlMarkup: function() {
		return [{ tag: 'div', cls: 'html control', tabIndex: this.getTabIndex(), html: this.getValue() }];
	},

	completeRender: function() {
		Z8.form.field.Control.prototype.completeRender.call(this);
		this.div = this.selectNode('.control');
	},

	onDestroy: function() {
		this.div = null;
		Z8.form.field.Control.prototype.onDestroy.call(this);
	},

	setValue: function(value, displayValue) {
		Z8.form.field.Control.prototype.setValue.call(this, value, displayValue);
		DOM.setInnerHtml(this.div, value);
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this.div, !enabled, 'disabled');
		Z8.form.field.Control.prototype.setEnabled.call(this, enabled);
	},

	setScrollTop: function(scrollTop) {
		this.div.scrollTop = scrollTop;
	},

	setScrollLeft: function(scrollLeft) {
		this.div.scrollLeft = scrollLeft;
	}
});