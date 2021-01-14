Z8.define('Z8.filter.Line', {
	extend: 'Z8.filter.Element',

	cls: 'line',

	htmlMarkup: function() {
		var checkBox = this.checkBox = new CheckBox();
		checkBox.on('change', this.onChecked, this);

		var expression = this.expression || {};

		var store = this.getFields();
		var record = store.getById(expression.property);
		var fields = [{ name: 'name', header: Z8.$('Line.field'), icon: 'fa-tag', sortable: false, width: 200 }, { name: 'type', header: Z8.$('Line.type'), icon: 'fa-code', sortable: false, width: 100 }, { name: 'description', header: Z8.$('Line.description'), icon: 'fa-file-text-o', sortable: false, width: 400 }];
		var property = this.property = new ComboBox({ store: store, value: expression.property, emptyValue: '', fields: fields, required: true, filters: false, cls: 'property', placeholder: Z8.$('Line.field'), icons: true });
		property.on('change', this.propertyChanged, this);

		var type = record != null ? record.get('type') : null;
		store = type != null ? Z8.filter.Operator.getOperators(type) : null;
		record = store != null ? store.getById(expression.operator) : null;

		var operator = this.operator = new ComboBox({ store: store, value: expression.operator, emptyValue: '', cls: 'operator', required: true, filters: false, placeholder: Z8.$('Line.operation') });
		operator.on('change', this.operatorChanged, this);

		type = record != null ? record.get('type') : null;
		var value = this.value = this.getValueEditor(type, expression.value);

		this.items = [checkBox, property, operator, value];
		return this.callParent();
	},

	focus: function() {
		return this.isEnabled() ? this.property.focus() : false;
	},

	isSelected: function() {
		return this.checkBox.getValue();
	},

	onChecked: function(checkBox, newValue, oldValue) {
		this.toggle(newValue);
	},

	toggle: function(toggle) {
		this.checkBox.initValue(toggle);
		this.callParent(toggle);
	},

	getValueEditor: function(type, value) {
		var editor = this.value;

		if(editor != null && editor.type == type)
			return editor;

		editor = this.createValueEditor(type || null, value);
		editor.on('change', this.valueChanged, this);
		editor.type = type;
		return editor;
	},

	createValueEditor: function(type, value) {
		switch(type) {
		case null:
			return new Z8.Component({ cls: 'value hidden' });
		case Type.String:
		case Type.Text:
			return new Z8.form.field.Text({ value: value, cls: 'value', placeholder: Z8.$('Line.text'), required: true });
		case Type.Date:
			return new Z8.form.field.Date({ value: value, cls: 'value', placeholder: Z8.$('Line.date'), required: true});
		case Type.Datetime:
			return new Z8.form.field.Datetime({ value: value, cls: 'value', placeholder: Z8.$('Line.time'), required: true});
		case Type.Integer:
			return new Z8.form.field.Integer({ value: value, cls: 'value', placeholder: '0 000', required: true});
		case Type.Float:
			return new Z8.form.field.Float({ value: value, cls: 'value', placeholder: '0 000,00', required: true});
		default:
			throw 'Unsupported type: ' + type;
		}
	},

	propertyChanged: function(comboBox, newValue, oldValue) {
		var record = comboBox.store.getById(newValue);
		var type = record != null ? record.get('type') : null;
		var store = type != null ? Z8.filter.Operator.getOperators(type) : null;
		this.operator.setStore(store);
		this.operator.setValue(this.emptyValue, '');
		this.onChildChange(this);
	},

	operatorChanged: function(comboBox, newValue, oldValue) {
		var store = comboBox.store;
		var record = store != null? store.getById(newValue) : null;
		var type = record != null ? record.get('type') : null;
		var editor = this.getValueEditor(type);
		if(editor != this.value) {
			this.remove(this.value);
			this.value = editor;
			this.add(this.value);
		}

		this.onChildChange(this);
	},

	valueChanged: function(comboBox, newValue, oldValue) {
		this.onChildChange(this);
	},

	getExpression: function() {
		var property = this.property.getValue();
		if(Z8.isEmpty(property))
			return {};

		var operator = this.operator.getValue();
		if(Z8.isEmpty(operator))
			return { property: property };

		var value = this.value instanceof Z8.form.field.Control ? this.value.getValue() : null;

		return { property: property, operator: operator, value: value || undefined };
	},

	getExpressionText: function() {
		var property = this.property.getDisplayValue();
		if(Z8.isEmpty(property))
			return null;

		var operator = this.operator.getDisplayValue();
		if(Z8.isEmpty(operator))
			return null;

		var needsValue = this.value instanceof Z8.form.field.Control;
		var value = needsValue ? this.value.getRawValue() || '' : null;

		return '\'' + property + '\' ' + operator + (needsValue ? ' \'' + value + '\'' : '');
	}
});
