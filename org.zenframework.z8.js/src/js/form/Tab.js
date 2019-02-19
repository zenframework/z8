Z8.define('Z8.form.Tab', {
	extend: 'Z8.form.Fieldset',
	shortClassName: 'Tab',

	plain: true,

	setText: function(text) {
		var tag = this.tag;
		if(tag != null)
			tag.setText(text);
	},
	
	isVisible: function() {
		return this.tag.isVisible();
	}
});