Z8.define('Z8.form.Tab', {
	extend: 'Z8.form.Fieldset',
	shortClassName: 'Tab',

	minHeight: false,
	plain: true,

	setText: function(text) {
		var tag = this.tag;
		if(tag != null)
			tag.setText(text);
	}
});