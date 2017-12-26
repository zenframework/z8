Z8.define('Z8.list.Total', {
	extend: 'Z8.Component',

	htmlMarkup: function() {
		var text = this.formatText(this.getTotal());
		text = { cls: 'text', html: text, title: text } ;
		var cls = this.cls = DOM.parseCls(this.cls).pushIf('column').pushIf(this.field.type);
		return { tag: 'td', id: this.getId(), cls: cls.join(' '), cn: [text] };
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
		DOM.setTitle(this.textElement, text);
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
