Z8.define('Z8.form.field.VideoDocument', {
	extend: 'Z8.form.field.Document',

	tag: 'video',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('video-document');
	},

	controlMarkup: function() {
		var markup = this.callParent();
		markup[0].controls = true;
		markup[0].src = this.getSource();
		return markup;
	},

	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? source : '');
	},

	setAutoplay: function(autoplay) {
		DOM.setAttribute(this.document, 'autoplay', autoplay);
	}
});