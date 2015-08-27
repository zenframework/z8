Z8.view.FilterPanel = Ext.extend(Z8.Panel,
{
	border: false,
	header: false,
	padding: 10,
	
	layout: 'vbox',
	layoutConfig: { align: 'stretch', pack: 'start' },
	
	operators: {},
	rowStates: new Ext.util.MixedCollection(),

	constructId: function(prefix, index) { return this.id + '_' + prefix + '_'+ index; },
	
	buttonBoxId: function(index) { return this.constructId('buttonBox', index); },
	addButtonId: function(index) { return this.constructId('addButton', index); },
	removeButtonId: function(index) { return this.constructId('removeButton', index); },

	andOrBoxId: function(index) { return this.constructId('andOrBox', index); },
	andOrId: function(index) { return this.constructId('andOr', index); },

	fieldBoxId: function(index) { return this.constructId('fieldBox', index); },
	fieldId: function(index) { return this.constructId('field', index); },
	
	operatorBoxId: function(index) { return this.constructId('operatorBox', index); },
	operatorId: function(index) { return this.constructId('operator', index); },

	valueBoxId: function(index) { return this.constructId('valueBox', index); },
	valueId: function(index) { return this.constructId('value', index); },

	tableId: function(index) { return this.id + '_container'; },
	itemId: function(index) { return this.constructId('row', index); },

	initComponent: function()
	{
		this.id = Ext.id(this);

		Z8.view.FilterPanel.superclass.initComponent.call(this);

		this.operators[Z8.ServerTypes.Boolean] = [['равно', 'eq']];
		this.operators[Z8.ServerTypes.String] = [['равно', 'eq'], ['не равно', 'notEq'], ['начинается', 'beginsWith'], ['заканчивается', 'endsWith'], ['содержит', 'contains']],
		this.operators[Z8.ServerTypes.Integer] = [['равно', 'eq'], ['не равно', 'notEq'], ['больше', 'gt'], ['больше или равно', 'ge'], ['меньше', 'lt'], ['меньше или равно', 'le']],
		this.operators[Z8.ServerTypes.Float] = [['равно', 'eq'], ['не равно', 'notEq'], ['больше', 'gt'], ['больше или равно', 'ge'], ['меньше', 'lt'], ['меньше или равно', 'le']],
		this.operators[Z8.ServerTypes.Date] = [['равно', 'eq'], ['не равно', 'notEq'], ['позже', 'gt'], ['не раньше', 'ge'], ['раньше', 'lt'], ['не позже', 'le']]
	
		this.rowTemplate = new Ext.Template(
			'<tr id = "' + this.itemId('{itemId}') + '">',
				'<td><div style="width:30px;" id="'+ this.buttonBoxId('{itemId}') + '"></div></td>',
				'<td><div id="' + this.andOrBoxId('{itemId}') + '"></div></td>',
				'<td><div style="width:140px;" id="' + this.fieldBoxId('{itemId}') + '"></div></td>',
				'<td><div style="width:140px;" id="' + this.operatorBoxId('{itemId}') + '"></div></td>',
				'<td><div style="width:125px;" id="' + this.valueBoxId('{itemId}') + '" ></div></td>',
			'</tr>'
		);

		this.rowTemplate.compile();

		this.tableHtml = '<table cellspacing="3"><tbody id="' + this.tableId() + '"></tbody></table>';

		this.label = new Ext.form.Label({ text: 'Задайте условия фильтрации данных:', height: 20 });
		this.tableContainer = new Ext.Container({ flex: 1, html: this.tableHtml, autoScroll: true });
		
		this.add(this.label);
		this.add(this.tableContainer);
		
		this.tableContainer.on('afterrender', this.onAfterRender, this);
	},
	
	getFieldsStore: function()
	{
		if(this.fieldsStore == null)
		{
			var data = [];
	
			for(var i = 0; i < this.query.fields.length; i++)
			{
				var field = this.query.fields[i];
				if(field.serverType == Z8.ServerTypes.String ||
					field.serverType == Z8.ServerTypes.Integer ||
					field.serverType == Z8.ServerTypes.Float ||
					field.serverType == Z8.ServerTypes.Date ||
					field.serverType == Z8.ServerTypes.Boolean)
				{
					data.push([field.id, field.header, field.serverType]);
				}
			}
			
			this.fieldsStore = new Ext.data.SimpleStore({ fields: [{name: 'id'}, {name: 'name'}, {name: 'type'}], data: data });
		}
		
		return this.fieldsStore;
	},

	getAndOrStore: function()
	{
		if(this.andOrStore == null)
		{
			this.andOrStore = new Ext.data.SimpleStore(
			{
				fields: [ { name: 'name', type:'string' }, {name: 'value'} ],
				data: [['And', 'and'], ['Or', 'or']]
			});
		}
	
		return this.andOrStore;
	},

	getTrueFalseStore: function()
	{
		if(this.trueFalseStore == null)
		{
			this.trueFalseStore = new Ext.data.SimpleStore(
			{
				fields: [{name: 'name'}, {name: 'value'}],
				data: [['True','1'], ['False', '0']]
			});
		}
	
		return this.trueFalseStore;
	},
		
	operatorStore: function(data)
	{
		return new Ext.data.SimpleStore({ fields: [{ name: 'name', type:'string' }, { name: 'value' }], data: data });
	},
	
	load: function(filter)
	{
		this.filter = filter;
		this.clear();

		if(this.filter)
		{
			for(var i = 0; i < filter.items.length; i++)
			{
				this.addItem(filter.items[i]);
			}
		}
		else
		{
			this.addItem();
		}
	},
	
	onAfterRender: function()
	{
		this.load(this.filter);
	},

	clear: function()
	{
		for(var i = 0; i < this.rowStates.getCount(); i++)
		{
			this.removeItem(this.rowStates.get(i));
		}
		
		this.rowStates.clear();
	},
	
	reset: function()
	{
		this.clear();
		this.addRow();
	},

	getFilter: function()
	{
		var filter = this.filter || { id: new Ext.ux.UUID().id };
		filter.items = this.rowStates.getRange();
		return filter;
	},
	
	addButton: function(index)
	{
		return Ext.getCmp(this.addButtonId(index));
	},

	removeButton: function(index)
	{
		return Ext.getCmp(this.removeButtonId(index));
	},

	andOr: function(index)
	{
		return Ext.getCmp(this.andOrId(index));
	},

	andOr: function(index)
	{
		return Ext.getCmp(this.andOrId(index));
	},
	
	field: function(index)
	{
		return Ext.getCmp(this.fieldId(index));
	},
	
	operator: function(index)
	{
		return Ext.getCmp(this.operatorId(index));
	},

	value: function(index)
	{
		return Ext.getCmp(this.valueId(index));
	},

	addItem: function(item)
	{
		var empty = this.rowStates.getCount() == 0;
		
		if(item == null)
		{
			item = { id: new Ext.ux.UUID().id, dataType: Z8.ServerTypes.String, andOr: 'and', operator: 'eq' };
		}

		this.rowStates.add(item.id, item);
		
		var id = item.id;

		this.rowTemplate.append(this.tableId(), { itemId: id } );
		
		if(empty)
		{
			new Z8.Button({ id: this.addButtonId(id), cls: 'x-chart-panel-btn', overCls: 'x-chart-panel-btn-over', tooltip: 'Добавить новое условие', iconCls: 'icon-add-small', renderTo: this.buttonBoxId(id), handler: this.onAddItem, scope: this, ownerCt: this });
		}
		else
		{
			new Z8.Button({ id: this.removeButtonId(id), tooltip: 'Удалить условие', cls: 'x-chart-panel-btn', overCls: 'x-chart-panel-btn-over', iconCls: 'icon-delete-small', renderTo: this.buttonBoxId(id), item: item, handler: this.onRemoveItem, scope: this, ownerCt: this });
			var operator = new Ext.form.ComboBox({ id: this.andOrId(id), renderTo: this.andOrBoxId(id), store: this.getAndOrStore(), value: item.andOr, item: item, displayField: 'name', valueField: 'value', typeAhead: true, width: 75, mode: 'local', forceSelection: true, triggerAction: 'all', selectOnFocus:true, ownerCt: this });
			operator.on('beforeselect', this.onAndOrChanged, this);
		}
		
		var field = new Ext.form.ComboBox({ id: this.fieldId(id), renderTo: this.fieldBoxId(id), store: this.getFieldsStore(), item: item, value: item.field, displayField: 'name', valueField: 'id', triggerAction: 'all', typeAhead: false, width: 140, mode:'local', hideTrigger: false, emptyText: 'Выберите поле', ownerCt: this });
		field.on('beforeselect', this.onFieldChanged, this);
		
		var operator = new Ext.form.ComboBox({ id: this.operatorId(id), renderTo: this.operatorBoxId(id), store: this.operatorStore(this.operators[item.dataType]), value: item.operator, item: item, displayField: 'name', valueField: 'value', typeAhead: true, width: 140, mode: 'local', forceSelection: true, triggerAction: 'all', emptyText: 'Выберите оператор', selectOnFocus: true, ownerCt: this });
		operator.on('beforeselect', this.onOperatorChanged, this);

		this.addValueField(item);
	},
	
	removeItem: function(item)
	{	
		var id = item.id;
		Ext.destroy(this.andOr(id));
		Ext.destroy(this.field(id));
		Ext.destroy(this.operator(id));
		Ext.destroy(this.value(id));
		Ext.destroy(this.addButton(id));
		Ext.destroy(this.removeButton(id));
		
		var row = Ext.get(this.itemId(id));
		if(row != null)
		{
			row.remove();
		}
		
		this.rowStates.removeKey(id);
	},
	
	onAddItem: function(button)
	{
		this.addItem();
	},
	
	onRemoveItem:  function(button)
	{
		this.removeItem(button.item);
	},

	onAndOrChanged: function(combo, record)
	{
		combo.item.andOr = record.data.value;
	},
	
	onFieldChanged: function(combo, record, index)
	{
		var item = combo.item;
		
		item.field = record != null ? record.data.id : null;
		item.name = record != null ? record.data.name : null;
		var dataType = record != null ? record.data.type : Z8.ServerTypes.String;
		
		if(item.dataType != dataType)
		{
			item.value = null;
		}
		
		item.dataType = dataType;
		
		this.loadOperator(item);
		this.addValueField(item);
	},
	
	loadOperator: function(item)
	{
		var operator = this.operator(item.id);
		var store = operator.getStore();
		var data = this.operators[item.dataType];
		store.loadData(data);

		operator.setValue(item.operator);
	},

	addValueField: function(item)
	{
		var id = item.id;
		
		Ext.destroy(this.value(id));
		
		var v = Z8.decode(item.value, item.dataType);
		var value = null;
		
		if(item.dataType == Z8.ServerTypes.Float || item.dataType == Z8.ServerTypes.Integer)
		{
			value = new Ext.form.NumberField({ id: this.valueId(id), renderTo: this.valueBoxId(id), item: item, value: v, width: 125 });
		}			
		else if(item.dataType == Z8.ServerTypes.Date)
		{
			value = new Ext.form.DateField({ id: this.valueId(id), renderTo: this.valueBoxId(id), item: item, value: v, width: 125 });
		}	
		else if(item.dataType == Z8.ServerTypes.Boolean)
		{
			value = new Ext.form.ComboBox({ id: this.valueId(id), renderTo: this.valueBoxId(id), item: item, store: this.getTrueFalseStore(), value: v, width: 125, displayField: 'name', valueField: 'value', typeAhead: true, mode: 'local', forceSelection: true, triggerAction: 'all', selectOnFocus: true });
		}
		else // Z8.ServerTypes.String
		{
			value = new Ext.form.TextField({ id: this.valueId(id), renderTo: this.valueBoxId(id), item: item, value: v, width: 125 });
		}
	
		value.on('blur', this.setValue, this);
	},

	setValue: function(textField)
	{
		var item = textField.item;
		var value = textField.getValue();
		item.value = item.dataType == Z8.ServerTypes.Date ? Ext.util.Format.date(value) : value;
	},
	
	onOperatorChanged: function(combo, record, index)
	{
		var item = combo.item;
		item.operator = record != null ? record.data.value : null;
	}
});


Z8.view.FilterDialog = Ext.extend(Z8.Window, 
{
	width:600,
	height: 400,
	resizable: false,
	modal: true, 

	title: 'Фильтр',
	
	layout: 'fit',

	initComponent: function()
	{
		Z8.view.FilterDialog.superclass.initComponent.call(this);
		
		this.panel = new Z8.view.FilterPanel({ query: this.query, filter: this.filter });
		this.add(this.panel);

		this.okButton = new Ext.Button({ text: 'OK', iconCls: '', handler: this.onOK, scope: this });
		this.addButton(this.okButton);

		this.cancelButton = new Ext.Button({ text: 'Oтмена', handler: this.onCancel, scope: this });
		this.addButton(this.cancelButton);

		this.on('close', this.onCancel, this);
	},
	
	getFilter: function()
	{
		return this.panel.getFilter();
	},
	
	onCancel: function()
	{
		this.destroy();
	},

	onOK: function()
	{
		if(this.handler != null)
		{
			this.handler.call(this.scope, this, this.panel.getFilter());
		}
		
		this.destroy();
	}
});