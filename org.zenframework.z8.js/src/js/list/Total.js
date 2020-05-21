Z8.define('Z8.list.Total', {
	extend: 'Z8.Component',

	getCls: function() {
		return Z8.Component.prototype.getCls.call(this).pushIf('column', this.field.type);
	},

	htmlMarkup: function() {
		var text = this.formatText(this.getTotal());
		text = { tag: 'span', cls: 'text', html: text };
		var cell = { cls: 'cell', cn: [text] };

		return { tag: 'td', id: this.getId(), cls: this.getCls().join(' '), cn: [cell], title: text };
	},

	completeRender: function() {
		this.callParent();
		this.textElement = this.selectNode('.text');
	},

	onDestroy: function() {
		this.textElement = null;
		this.callParent();
	},

	getTotal: function() {
		var totals = this.list.getStore().getTotals();
		return totals != null ? totals.get(this.field.name) : null;
	},

	getText: function(field) {
		return this.text;
	},

	setText: function(text) {
		this.text = text;
		var text = String.htmlText(this.formatText(text));
		DOM.setValue(this.textElement, text);
		DOM.setTitle(this, text);
	},

	formatText: function(value) {
		var field = this.field;

		if(field.renderer != null)
			return field.renderer.call(field, value);

		switch(field.type) {
		case Type.Date:
			return Format.date(value, field.format);
		case Type.Datetime:
			return Format.datetime(value, field.format);
		case Type.Integer:
			return Format.integer(value, field.format);
		case Type.Float:
			return Format.float(value, field.format);
		default:
			return '';
		}
	}
});
