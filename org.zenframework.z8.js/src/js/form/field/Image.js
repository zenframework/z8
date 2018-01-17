Z8.define('Z8.form.field.Image', {
	extend: 'Z8.form.field.Document',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('image');
	},

	setSource: function(source) {
		DOM.setStyle(this.document, 'backgroundImage', source != null ? 'url(' +  source + ')' : 'none');
	}
});