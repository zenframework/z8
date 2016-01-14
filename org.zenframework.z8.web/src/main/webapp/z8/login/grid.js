/*Z8.decoratedFields = {
	'org.zenframework.z8.server.base.table.system.Sequences': {
		'value': true
	}
};*/

function createColumns(query, editable)
{
	var columns = [];
	var indexedColumns = [];
	
	var fields = query.getColumns();

	var counted = false;
	
	var decoratedFields = (Z8.decoratedFields || {})[query.requestId] || {};
	
	for(var i = 0; i < fields.length; i++)
	{
		var field = fields[i];

		var column = Z8.Grid.newColumn(field);

		if(editable)
		{
			column.editor = Z8.Form.newField(field);
			column.editor.parentQuery = query;
		}

		if(decoratedFields[field.id] != null)
			column.xtype = 'decoratedColumn';
		
		if(field.aggregation == 'count')
		{
			if(!counted)
			{
				counted = true;
				
				column.summaryType = field.aggregation;
				column.summaryRenderer = function(v, params, data)
				{
					return v + ' item(s)';
				}
			}
		}
		else
		{
			column.summaryType = field.aggregation;
		}

		if(column.index == 0)
		{
			columns.push(column);
		}
		else
		{
			indexedColumns.push(column);
		}
	}

	indexedColumns.sort(function(c1, c2)
	{
		return c1.index - c2.index;
	});

	return indexedColumns.concat(columns);
}

function createGrid(query, config)
{
	var defaultConfig =
	{
		editable: true,
		multiselect: true,
		renderTo: null,
		handlers: null,
		columnLines: true,
		stripeRows: true,
		border: false,
		hasFilters: true,
		hasPageBar: true
	};

	config = config || {};
	
	Ext.applyIf(config, defaultConfig);
	
	if(query.readOnly)
	{
		config.editable = false;
	}
	
	var columns = createColumns(query, config.editable);

	var store = query.getStore();
	
	var f = [];
	
/*	
	if(query.requestId == "общесистемнаяНСИ.нсиУправленияДС.банковскиеРеквизиты.ЮридическиеЛицаView" && query.queryId == null)
	{
		f.push({
			dataIndex: "id1_15_15_9",
			type: 'string',
			value: 'ООО',
			active: true
		});
	}
*/	
	var plugins = [];
	
	if(config.editable) 
	{
		var editor = new Z8.grid.RowEditor();
		plugins.push(editor);
	}
	
	var filters = null;
	
	if(config.hasFilters) 
	{
		filters = new Ext.ux.grid.GridFilters({ encode: true, local: false, filters: f });    
		plugins.push(filters);
		config.filters = filters;
	}

	if(config.hasPageBar)
	{
		config.bbar = new Z8.PagingToolbar({ plugins: config.hasFilters ? [filters] : [], store: store });
	}
	
	if(query.parentKey != null)
	{
		if(store.lockProperty != null && config.showStatus !== false)
		{
			columns.unshift({id: 'status', xtype: 'lockStatusColumn'});	
		}
		
		if(store.filesProperty != null && config.showStatus !== false)
		{
			columns.unshift({id: store.filesProperty, dataIndex: store.filesProperty, xtype: 'attachmentsStatusColumn'});	
		}
				
		var treeConfig = 
		{
			flex: 1,
			rootVisible: false,
			filters: filters,
			plugins: plugins,

			store: store,

			cm: new Ext.grid.ColumnModel(
			{
				columns: columns,
				defaults: { sortable: true, menuDisabled: false, resizable: true }
			})
		};
		
		Ext.apply(treeConfig, config);
		
		return new Z8.tree.TreePanel(treeConfig);
	}
	else
	{
		if(store.lockProperty != null && config.showStatus !== false)
		{
			columns.unshift({id: store.lockProperty, header: Z8.emptyString, dataIndex: store.lockProperty, xtype: 'lockStatusColumn'});	
		}
		
		if(store.filesProperty != null && config.showStatus !== false)
		{
			columns.unshift({id: store.filesProperty, header: Z8.emptyString, dataIndex: store.filesProperty, xtype: 'attachmentsStatusColumn'});	
		}

		var selectionModel = new Ext.grid.CheckboxSelectionModel(
		{
        	sortable: false,
        	singleSelect: !config.multiselect,
        	noEditor: true,
			getEditor: function() { return null; }
    	});

		columns.unshift(selectionModel);
		
		var summary = new Z8.grid.HybridSummary();
		
		plugins.push(summary);
		
		var gridConfig =
		{
			flex: 1,
			
			store: store,
			filters: filters,
			
			plugins: plugins,

			colModel: new Ext.grid.ColumnModel(
			{
				defaults: { width: 120, sortable: true },
				columns: columns
			}),

			view: new Z8.grid.GridView(
			{
				groupTextTpl: '{text} ({[values.records.length == values.count ? values.count : (values.records.length + " из " + values.count)]})',
				startCollapsed: query.collapseGroups
			}),

			sm: selectionModel
//				new Ext.grid.RowSelectionModel({ singleSelect: !config.multiselect })
		};
		
		Ext.apply(gridConfig, config);

		return new Z8.grid.GridPanel(gridConfig);
	}
}


function createMenuItemTree(query, config)
{
	var defaultConfig =
	{
		editable: true,
		multiselect: true,
		renderTo: null,
		handlers: null,
		columnLines: true,
		stripeRows: true,
		border: false
	};

	config = config || {};
	
	Ext.applyIf(config, defaultConfig);
	
	if(query.readOnly)
	{
		config.editable = false;
	}
	
	var columns = createColumns(query, config.editable);

	var store = query.getStore();
	var editor = config.editable ? new Z8.grid.RowEditor() : null;
	var filters = new Ext.ux.grid.GridFilters({ encode: true, local: false });    
	var plugins = config.editable ? [editor, filters] : [filters];

	config.filters = filters;
	
				
		var treeConfig = 
		{
			rootVisible: false,
			autoScroll: true,
			containerScroll: false,
		
			flex: 1,
			border: false,
			frame: false,
			
			filters: filters,
		
			plugins: plugins,

			store: store,

			cm: new Ext.grid.ColumnModel(
			{
				columns: columns,
				defaults: { sortable: true, menuDisabled: false, resizable: true }
			})
		};
		
		Ext.apply(treeConfig, config);
		
		return new Z8.tree.TreeMenu(treeConfig);
}
	

