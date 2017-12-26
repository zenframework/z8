Z8.define('Z8.data.field.Files', {
	extend: 'Z8.data.field.Json',

	type: Type.Files,

	convert: function(value) {
		return this.callParent(value) || [];
	}
});
