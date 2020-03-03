Z8.define('Z8.list.Number', {
	extend: 'Z8.Component',

	getCls: function() {
		return Z8.Component.prototype.getCls.call(this).pushIf('column');
	},

	htmlMarkup: function() {
		var cell = { cls: 'cell', html: this.number };
		return { tag: 'td', id: this.getId(), cls: this.getCls().join(' '), cn: [cell] };
	}
});
