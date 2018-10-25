Z8.define('Z8.form.field.ImageDocument', {
	extend: 'Z8.form.field.Document',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('image-document');
	},

	setSource: function(source) {
		DOM.setStyle(this.document, 'backgroundImage', source != null ? 'url(' +  source + ')' : 'none');
	}
});