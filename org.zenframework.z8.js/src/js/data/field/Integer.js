Z8.define('Z8.data.field.Integer', {
	extend: 'Z8.data.field.Float',

	type: Type.Integer,

	convert: function(value) {
		value = this.callParent(value);
		return value > 0 ? Math.floor(value) : Math.ceil(value);
	}
});
