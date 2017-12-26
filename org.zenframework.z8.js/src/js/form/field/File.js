Z8.define('Z8.form.field.File', {
	extend: 'Z8.form.field.Document',

	tag: 'iframe',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('file');
	},

	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? source + '&preview=true' : '');
	}
});