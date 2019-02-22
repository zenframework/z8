Z8.define('Z8.form.field.Filter', {
	extend: 'Z8.form.field.Control',

	tabIndex: -1,
	scrollable: true,

	setValue: function(value) {
		if(value == this.getValue())
			return;

		this.callParent(value);

		if(this.expression != null) {
			this.expression.setExpression(value);
			this.updateTools();
		}
	},

	isEqual: function(value1, value2) {
		return JSON.encode(value1) == JSON.encode(value2);
	},

	subcomponents: function() {
		return this.callParent().add(this.expression);
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('filter-control');

		var label = this.label = this.label || {};

		var newLine = this.newLineButton = new Z8.button.Button({ tooltip: 'Новая строка', icon: 'fa-file-o', handler: this.newLine, scope: this });
		var group = this.groupButton = new Z8.button.Button({ tooltip: 'Группировать', icon: 'fa-link', handler: this.group, scope: this });
		var ungroup = this.ungroupButton = new Z8.button.Button({ tooltip: 'Разгруппировать', icon: 'fa-unlink', handler: this.ungroup, scope: this });
		var removeLine = this.removeLineButton = new Z8.button.Button({ tooltip: 'Удалить строки', cls: 'remove', icon: 'fa-trash', danger: true, handler: this.removeLine, scope: this });

		label.tools = new Z8.button.Group({ items: [newLine, group, ungroup, removeLine] });

		return this.callParent();
	},

	controlMarkup: function() {
		var expression = this.expression = new Z8.filter.Expression({ cls: 'control', fields: this.fields, expression: this.getValue() });
		expression.on('select', this.onExpressionSelect, this);
		expression.on('change', this.onExpressionChange, this);
		return [expression.htmlMarkup()];
	},

	completeRender: function() {
		this.callParent();
		this.updateTools();
	},

	focus: function() {
		return this.isEnabled() ? this.expression.focus() : false;
	},

	getSelection: function() {
		return this.expression.getSelection();
	},

	updateTools: function(selection) {
		if(this.expression == null)
			return;

		var selection = selection || this.getSelection();
		this.newLineButton.setEnabled(this.isEnabled());
		this.removeLineButton.setEnabled(selection.length != 0);
		this.groupButton.setEnabled(selection != null && selection.length > 1);
		this.ungroupButton.setEnabled(this.getGroups(selection).length != 0);
	},

	onExpressionSelect: function(selection) {
		this.updateTools(selection);
	},

	onExpressionChange: function(value) {
		this.superclass.setValue.call(this, value);
		this.updateTools();
	},

	newLine: function() {
		var selection  = this.getSelection();
		var first = selection.length != 0 ? selection[0] : null;
		var container = first instanceof Z8.filter.Group ? first : (first != null ? first.container : this.expression);
		container.newLine();
	},

	removeLine: function() {
		var selection  = this.getSelection();
		var container = selection[0].container;
		container.removeLine(selection);
		container.focus();
	},

	group: function() {
		var selection = this.getSelection();
		var first = selection[0];
		var container = first.container;
		container.group(selection);
	},

	ungroup: function() {
		var groups = this.getGroups(this.getSelection());

		for(var i = 0, length = groups.length; i < length; i++)
			groups[i].ungroup();
	},

	getGroups: function(items) {
		var result = [];

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item instanceof Z8.filter.Group)
				result.push(item);
		}

		return result;
	},

	getExpression: function() {
		return this.expression.getExpression();
	},

	getExpressionText: function() {
		return this.expression.getExpressionText();
	}
});
