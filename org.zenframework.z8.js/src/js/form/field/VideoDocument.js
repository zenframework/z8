Z8.define('Z8.form.field.VideoDocument', {
	extend: 'Z8.form.field.Document',

	tag: 'video',
	tabIndex: 1,

	getCls: function() {
		return Z8.form.field.Document.prototype.getCls.call(this).pushIf('video');
	},

	controlMarkup: function() {
		var markup = this.callParent();
		markup[0].controls = true;
		markup[0].src = this.getSource();
		return markup;
	},

	focus: function(select) {
		return this.isEnabled() ? DOM.focus(this.document, select) : false;
	},

	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? source : '');
	},

	setAutoplay: function(autoplay) {
		DOM.setAttribute(this.document, 'autoplay', autoplay);
	}
});