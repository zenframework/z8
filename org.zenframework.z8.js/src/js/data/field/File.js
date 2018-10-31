Z8.define('Z8.data.field.File', {
	extend: 'Z8.data.field.Json',

	type: Type.File,

	convert: function(value) {
		return this.callParent(value) || [];
	}
});
