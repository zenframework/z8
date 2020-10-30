Z8.define('Z8.form.field.PdfDocument', {
	extend: 'Z8.form.field.Document',

	tag: 'iframe',

	getCls: function() {
		return Z8.form.field.Document.prototype.getCls.call(this).pushIf('pdf');
	},

	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? (window._DEBUG_ ? '/' : '') + source + '&preview=true' : '');
	}
});