Z8.define('Z8.form.field.ImageDocument', {
	extend: 'Z8.form.field.Document',

	getCls: function() {
		return Z8.form.field.Document.prototype.getCls.call(this).pushIf('image-document');
	},

	setSource: function(source) {
		DOM.setStyle(this.document, 'backgroundImage', source != null ? 'url(' +  source + ')' : 'none');
	}
});