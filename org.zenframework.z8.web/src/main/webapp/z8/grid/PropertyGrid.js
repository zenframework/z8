Ext.grid.PropertyRecord = Ext.data.Record.create([
    {name:'name', type:'string'}, 'value', 'serverType'
]);

Z8.grid.PropertyColumnModel = Ext.extend(Ext.grid.PropertyColumnModel, 
{
	dateFormat : Z8.Format.Date,
	trueText: Z8.Format.TrueText,
	falseText: Z8.Format.FalseText,
	
	constructor : function(grid, store)
	{
		Z8.grid.PropertyColumnModel.superclass.constructor.call(this, grid, store);

		var dateField = new Ext.form.DateField({ format: Z8.Format.Date, selectOnFocus:true });
		this.editors.date = new Ext.grid.GridEditor(dateField);

		var comboBox = new Z8.form.SearchBox({ selectOnFocus:true, ownerCt: grid });
		this.editors.object = new Ext.grid.GridEditor(comboBox);
	},

	renderNumber: function(number)
	{
		var rounded = Ext.util.Format.round(number, 0);
		return Ext.util.Format.number(number, rounded == number ? Z8.Format.Integer : Z8.Format.Float);
	},

	renderCell : function(value, meta, rec)
	{
		var renderer = this.grid.customRenderers[rec.get('name')];

		if(renderer)
		{
			return renderer.apply(this, arguments);
		}

		if(Ext.isObject(value) && value.queryId != null)
		{
			value = value.value;
		}
		
		var rv = value;

		if(Ext.isDate(value))
		{
			rv = this.renderDate(value);
		}
		else if(typeof value == 'boolean')
		{
			rv = this.renderBool(value);
		}
		else if(Ext.isNumber(value))
		{
			rv = this.renderNumber(value);
		}

		return Ext.util.Format.htmlEncode(rv);
	},

	getCellEditor: function(colIndex, rowIndex)
	{
		var property = this.store.getProperty(rowIndex);
		var value = property.data.value;

		if(Ext.isObject(value))
		{
			return this.editors.object;
		}
		
		return Z8.grid.PropertyColumnModel.superclass.getCellEditor.call(this, colIndex, rowIndex);
	}
});

Z8.grid.PropertyStore = Ext.extend(Ext.grid.PropertyStore,
{
	isEditableValue: function(val)
	{
		return true;
	}
});

Z8.grid.PropertyGrid = Ext.extend(Ext.grid.PropertyGrid,
{
	frame: false,
	border: false,

	initComponent: function()
	{
		this.customRenderers = this.customRenderers || {};
		this.customEditors = this.customEditors || {};
		this.lastEditRow = null;
		this.propStore = new Z8.grid.PropertyStore(this);

		this.cm = new Z8.grid.PropertyColumnModel(this, this.propStore);
		this.ds = this.propStore.store;
		
		this.addEvents('beforepropertychange', 'propertychange');

		Ext.grid.PropertyGrid.superclass.initComponent.call(this);

		this.mon(this.selModel, 'beforecellselect', function(sm, rowIndex, colIndex)
		{
			if(colIndex === 0)
			{
				this.startEditing.defer(200, this, [rowIndex, 1]);
				return false;
			}
		}, this);
	}
});
