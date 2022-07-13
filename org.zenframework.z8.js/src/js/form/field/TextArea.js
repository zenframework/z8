Z8.define('Z8.form.field.TextArea', {
	extend: 'TextBox',
	shortClassName: 'TextArea',

	scrollable: true,

	trigger: false,
	tag: 'textarea',
	inputCls: 'textarea',

	constructor: function(config) {
		config = config || {};
		config.minHeight = config.minHeight && config.minHeight != 0 ? config.minHeight : 10;

		Z8.form.field.Text.prototype.constructor.call(this, config);
	},

	getCls: function() {
		return Z8.form.field.Text.prototype.getCls.call(this).pushIf('textarea');
	}
});