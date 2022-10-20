Z8.define('Z8.form.field.TextArea', {
	extend: 'TextBox',
	shortClassName: 'TextArea',

	scrollable: true,

	trigger: false,
	tag: 'textarea',
	inputCls: 'textarea',

	getCls: function() {
		return Z8.form.field.Text.prototype.getCls.call(this).pushIf('textarea');
	}
});