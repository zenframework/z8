Z8.define('Z8.list.Number', {
	extend: 'Z8.Component',

	htmlMarkup: function() {
		var cell = { cls: 'cell', html: this.number };

		var cls = this.cls = DOM.parseCls(this.cls).pushIf('column');
		return { tag: 'td', id: this.getId(), cls: cls.join(' '), cn: [cell] };
	}
});
