Z8.define('Z8.filter.Dialog', {
	extend: 'Z8.window.Window',

	htmlMarkup: function() {
		var editor = this.editor = new Z8.filter.Editor({ filter: this.filter, fields: this.fields });
		this.body = [editor];
		return this.callParent();
	},

	getFilter: function() {
		return this.editor.getFilter();
	}
});
