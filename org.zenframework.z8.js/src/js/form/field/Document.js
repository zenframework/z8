Z8.define('Z8.form.field.Document', {
	extend: 'Z8.form.field.Control',

	isDocument: true,
	label: '',

	tag: 'div',
	height: Ems.unitsToEms(3),

	isValid: function() {
		return true;
	},

	validate: function() {},

	controlMarkup: function() {
		return [{ tag: this.tag, cls: 'control' }];
	},

	completeRender: function() {
		this.callParent();
		this.document = this.selectNode('.control');
		this.setValue(this.getValue());
	},

	onDestroy: function() {
		this.document = null;
		this.callParent();
	},

	getSource: function(value) {
		return value != null ? encodeURI(value.replace(/\\/g, '/')) + '?&session=' + Application.session : null;
	},

	setSource: function(source) {
		throw "Z8.form.field.Document.setSource is not implemented";
	},

	setValue: function(value) {
		this.callParent(value);
		this.setSource(this.getSource(value));
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this) : false;
	}
});