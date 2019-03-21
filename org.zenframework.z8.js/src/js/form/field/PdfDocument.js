Z8.define('Z8.form.field.PdfDocument', {
	extend: 'Z8.form.field.Document',

	tag: 'iframe',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('pdf-document');
	},

	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? (window._DEBUG_ ? '/' : '') + source + '&preview=true' : '');
	}
});