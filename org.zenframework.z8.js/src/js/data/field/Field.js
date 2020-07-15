Z8.define('Z8.data.field.Field', {
	isField: true,

	name: null,
	type: Type.String,

	persist: true,

	constructor: function(config) {
		this.callParent(config);
	},

	compare: function(left, right) {
		if((left == null || left === '') && (right == null || right === ''))
			return 0;

		if(left == right)
			return 0;

		if(left == null)
			return -1;

		if(right == null)
			return 1;

		return left < right ? -1 : 1;
	},

	isEqual: function(left, right) {
		return this.compare(left, right) == 0;
	},

	getType: function() {
		return this.type;
	},

	getName: function() {
		return this.name;
	},

	getFormat: function() {
		return this.format;
	},

	getRenderer: function() {
		return this.renderer;
	},

	serialize: function(value, record) {
		return value;
	},

	convert: function(value, record) {
		return value;
	}
});