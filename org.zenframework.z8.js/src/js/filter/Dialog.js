Z8.define('Z8.filter.Dialog', {
	extend: 'Z8.window.Window',

	cls: 'filters',

	htmlMarkup: function() {
		var editor = this.editor = new Z8.filter.Editor({ filter: this.filter, fields: this.fields, flex: 1 });
		this.controls = [editor];
		return this.callParent();
	},

	getFilter: function() {
		return this.editor.getFilter();
	}
});
