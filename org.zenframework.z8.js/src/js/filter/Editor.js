Z8.define('Z8.filter.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'name',

	fields: [ 
		new Z8.data.field.String({ name: 'name', header: 'Фильтр', editable: true }),
		new Z8.data.field.Json({ name: 'filter' })
	]
});

Z8.define('Z8.filter.Editor', {
	extend: 'Z8.form.Fieldset',

	cls: 'filter-editor',
	plain: true,

	htmlMarkup: function() {
		var store = this.store = new Z8.data.Store({ model: 'Z8.filter.Model', data: this.filter.toStoreData() });
		store.use();

		var addTool = this.addTool = new Z8.button.Button({ icon: 'fa-file-o', tooltip: 'Новый фильтр', handler: this.onAddFilter, scope: this });
		var copyTool = this.copyTool = new Z8.button.Button({ icon: 'fa-copy', tooltip: 'Копировать запись (Shift+Insert)', handler: this.onCopyFilter, scope: this });
		var removeTool = this.removeTool = new Z8.button.Button({ cls: 'remove', danger: true, icon: 'fa-trash', tooltip: 'Удалить фильтр(ы)', handler: this.onRemoveFilter, scope: this });
		var tools = new Z8.button.Group({ items: [addTool, copyTool, removeTool] });

		var label = { text: 'Фильтры', icon: 'fa-filter', tools: tools};

		var filters = this.filters = new ListBox({ store: store, cls: 'filters', fields: [store.getField('name')], label: label, editable: true, flex: 1, colSpan: 1 });
		filters.on('select', this.onFiltersSelect, this);
		filters.on('itemEditorChange', this.onItemEditorChange, this);

		filters.setAddTool(addTool);
		filters.setRemoveTool(removeTool);
		filters.setCopyTool(copyTool);

		var expression = this.expression = new Z8.form.field.Filter({ fields: this.fields, flex: 1, enabled: false, label: { text: 'Выражение', icon: 'fa-list-ul' } });
		expression.on('change', this.expressionChanged, this);
		var expressionText = this.expressionText = new Z8.form.field.Html({ cls: 'text', label: { text: 'Текст', icon: 'fa-code' }, minHeight: 7.5 });

		var container = new Z8.form.Fieldset({ cls: 'filter', plain: true, controls: [expression, expressionText], flex: 1, colSpan: 3, colCount: 1 });
	
		this.colCount = 4;
		this.controls = [filters, container];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		this.updateTools();
	},

	onDestroy: function() {
		if(this.store != null) {
			this.store.dispose();
			this.store = null;
		}

		this.callParent();
	},

	getChecked: function() {
		return this.filters.getChecked();
	},

	getSelection: function() {
		return this.filters.getSelection();
	},

	updateTools: function() {
		var selection = this.getChecked();
		this.removeTool.setEnabled(selection.length != 0);

		selection = this.getSelection();
		this.copyTool.setEnabled(selection != null);
	},

	getFilter: function() {
		var filter = this.filter;
		filter.clear();

		var records = this.store.getRecords();
		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			filter.add(record.id, record.get('filter'));
		}

		var record = this.getSelection();
		filter.setActive(record != null);
		filter.setCurrent(record != null ? record.id : null);
		return filter;
	},

	onFiltersSelect: function(listBox, record) {
		var expression = this.expression;
		var expressionText = this.expressionText;

		expression.setEnabled(record != null);
		expressionText.setEnabled(record != null);

		var value = record != null ? record.get('filter') : null;

		if(value != expression.getValue()) {
			expression.initValue(value);
			expressionText.initValue(expression.getExpressionText());
		}

		this.updateTools();
	},

	onItemEditorChange: function(listBox, editor, newValue, oldValue) {
		var editedRecord = editor.record;

		var records = this.store.getRecords();

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			if(record != editedRecord && newValue == record.get('name')) {
				editor.setValid(false);
				return;
			}
		}

		editor.setValid(true);
	},

	expressionChanged: function(control, newValue, oldValue) {
		var selected = this.getSelection();
		selected.set('filter', this.expression.getExpression());
		this.expressionText.setValue(this.expression.getExpressionText());
	},

	onAddFilter: function(button) {
		var name = this.newFilterName();

		var record = new Z8.filter.Model({ name: name, filter: null });
		this.store.add(record);

		var filters = this.filters;
		filters.select(record);

		if(!filters.startEdit(record, 0))
			filters.focus();
	},

	onCopyFilter: function(button) {
		var record = this.getSelection();
		var name = this.newFilterName(record.id);
		var filter = JSON.decode(JSON.encode(record.get('filter')));

		var record = new Z8.filter.Model({ name: name, filter: filter });
		this.store.add(record);

		var filters = this.filters;
		filters.select(record);

		if(!filters.startEdit(record, 0))
			filters.focus();
	},

	onRemoveFilter: function(button) {
		var records = this.filters.getChecked();

		if(records.length == 0)
			throw 'records.length == 0';

		var store = this.store;
		var index = store.indexOf(records[0]);
		store.remove(records);

		var filters = this.filters;
		filters.select(index);
		filters.focus();
	},

	newFilterName: function(template) {
		template = (template || 'Фильтр') + ' ';

		var store = this.store;
		var names = store.getOrdinals();

		var index = 1;
		while(true) {
			var name = template + index;
			if(names[name] == null)
				return name;
			index++;
		}
	}
});
