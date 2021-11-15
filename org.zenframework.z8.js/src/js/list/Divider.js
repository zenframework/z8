Z8.define('Z8.list.Divider', {
	extend: 'Z8.Component',
	shortClassName: 'ListDivider',

	cls: 'divider',
	enabled: false,

	htmlMarkup: function() {
		var list = this.list;
		var headers = list != null ? list.getHeaders() : null;
		return { tag: 'tr', cls: 'divider', cn: { tag: 'td', colspan: headers ? headers.length || 1 : 1 } };
	}
});