Z8.grid.GridPanel = Ext.extend(Ext.grid.GridPanel,
{
	minColumnWidth: 100,
	
	constructor: function(config)
	{
		Z8.grid.GridPanel.superclass.constructor.call(this, config);
	},
	
	initComponent : function()
	{
		Z8.grid.GridPanel.superclass.initComponent.call(this);
	
		if(this.handlers != null)
		{
			for(var i = 0; i < this.handlers.length; i++)
			{
				var handler = this.handlers[i];
				var scope = handler.scope;
				scope.mon(this, handler.name, handler.handler, scope);
			}
		}
	},

	canAddRecord: function()
	{
		return true;
	}, 

	canDeleteRecord: function()
	{
		var records = this.getSelectedRecords();
		var store = this.getStore();
		
		for(var i = 0; i < records.length; i++)
		{
			if(store.lockProperty == null || records[i].data[store.lockProperty] !== true)
			{
				return true;
			}
		}
		
		return false;
	}, 

	getSelectedRecord: function()
	{
		var record = this.getSelectionModel().getSelected();
		return record != null? this.getStore().getById(record.id) : null;
	},

	getSelectedRecords: function()
	{
		return this.getSelectionModel().getSelections();
	},

	getRecord: function(index)
	{
		return this.getStore().getAt(index);
	},

	getRowIndex: function(record)
	{
		return this.getStore().indexOf(record);
	},

	ensureVisible: function(record)
	{
		var rowIndex = this.getRowIndex(record);
		var view = this.getView();
		
		if(rowIndex != -1 && view.hasRows())
		{
			this.getView().ensureVisible(rowIndex, 0);
		}
	},
	
	addRecord: function(startEdit, success, error, scope)
	{
		var query = this.getStore().query;
		
		var success = this.onRecordAdded.createDelegate(this, [startEdit, success, scope], true);
		var error = this.onRecordFailure.createDelegate(this, [error, scope], true);

		query.request(success, error, { xaction: 'new' }, this);
	},

	copyRecord: function(startEdit, success, error, scope)
	{
		var query = this.getStore().query;
		var record = this.getSelectedRecord();
		
		var success = this.onRecordAdded.createDelegate(this, [startEdit, success, scope], true);
		var error = this.onRecordFailure.createDelegate(this, [error, scope], true);

		query.request(success, error, { xaction: 'copy', source: record.id, data: Ext.encode(record.data) }, this);
	},

	refreshRecord: function(recordId, success, error, scope)
	{
		if(this.el != null && this.el.dom != null)
		{
			var query = this.getStore().query;

			var success = this.onRecordRefreshed.createDelegate(this, [success, scope], true);
			var error = this.onRecordFailure.createDelegate(this, [error, scope], true);
			
			query.request(success, error, { xaction: 'read', filterBy: recordId }, this);
		}
	},

	onRecordRefreshed: function(query, callback, scope)
	{
		if(this.el.dom != null)
		{
			var scroll = this.getView().getScrollState();
			
			var store = this.getStore();
			
			query.total = store.getTotalCount();
			store.loadData(query, true);
			
			this.getSelectionModel().onRefresh();
			
			this.getView().restoreScroll(scroll);
		}
		
		if(callback != null)
		{
			callback.call(scope);
		}
	},

	getModifiedFieds: function(record)
	{
		var modified = {};
		var lockProperty = this.getStore().lockProperty;
		
		for(var field in record.data)
		{
			if(field != lockProperty)
			{
				var value = record.data[field];
			
				if(!Z8.isEmpty(value))
				{
					modified[field] = value;
				}
			}
		}
		
		return modified;
	},
	
	onRecordAdded: function(query, startEdit, callback, scope)
	{
		var records = [];
		var store = this.getStore();

		var result = store.reader.readRecords(query);
		var records = result.records;
		
		if(records.length > 1)
		{
			throw 'More than one record created.';
		}
		
		if(records.length != 0)
		{
			this.stopEditing();
			
			var record = records[0];
			record.dirty = true;
			record.phantom = true;
			record.modified = this.getModifiedFieds(record);

			record = store.insertRecord(record);

			this.selectRecord(record);
			
			if(startEdit)
			{
				this.startEditing(record);
			}
		}
		
		if(callback != null)
		{
			callback.call(scope, query.info);
		}
	},

	onRecordFailure: function(info, callback, scope)
	{
		if(callback != null)
		{
			callback.call(scope, info);
		}
	},
	
	getRowEditor: function()
	{
		if(this.hasRowEditor == null && this.plugins != null)
		{
			this.hasRowEditor = false;
			
			for(var i = 0; i < this.plugins.length; i++)
			{
				if(this.plugins[i].isRowEditor)
				{
					this.rowEditor = this.plugins[i];
					this.hasRowEditor = true;
					break;
				}
			}
		}
		
		return this.rowEditor;
	},
	
	stopEditing: function()
	{
		var rowEditor = this.getRowEditor();
		
		if(rowEditor != null)
		{
			rowEditor.stopEditing(true);
		}
	},

	startEditing: function(record)
	{
		var rowEditor = this.getRowEditor();
		
		if(rowEditor != null)
		{
			rowEditor.startEditing(record, true);
		}
	},

	isDirty: function()
	{
		var store = this.getStore();
		
		if(store == null)
		{
			return false;
		}
		
		if(store.isDirty())
		{
			return true;
		}
			
		var rowEditor = this.getRowEditor();
		
		if(rowEditor != null && rowEditor.isVisible())
		{
			return rowEditor.isDirty();
		}
		
		return false;
	},
	
	deleteRecord: function()
	{
		var store = this.getStore();
		var records = this.getSelectedRecords();

		var index = this.getRowIndex(records[0]);
		
		for(var i = 0; i < records.length; i++)
		{
			if(!store.isRecordLocked(records[i]))
			{
				store.remove(records[i]);
			}
		}
		
		var count = store.getCount();
		index = index < count ? index : count - 1;
		
		if(index != -1)
		{
			this.select(index);
		}
	},

	saveRecords: function()
	{
		this.getStore().save();
	},

	focus: function()
	{
		var focusEl = this.getView().focusEl;
		
		if(Ext.isGecko)
		{
			focusEl.focus();
		}
		else
		{
			focusEl.focus.defer(1, focusEl);
		}
	},

	select: function(index)
	{
		if(this.viewReady && !this.isDestroyed)
		{
			this.getSelectionModel().selectRow(index);
		}
	},
	
	selectRecord: function(record, forceSelect)
	{
		if(this.viewReady && !this.isDestroyed)
		{
			var selected = this.getSelectedRecord();
			var selectedId = selected != null ? selected.id : null;
			var recordId = record != null ? record.id : null;
	
			if(selectedId != recordId || forceSelect)
			{
				var index = this.getStore().indexOf(record);
		
				if(index != -1)
				{
					this.getSelectionModel().selectRow(index);
				}
			}
			
			this.ensureVisible(record);
		}
	},

	hasNextToSelect: function()
	{
		return this.getSelectionModel().hasNext();
	},

	hasPreviousToSelect: function()
	{
		return this.getSelectionModel().hasPrevious();
	},

	selectNext: function()
	{
		if(this.viewReady && !this.isDestroyed)
		{
			if(this.getSelectedRecord() == null)
			{
				return this.selectFirst();
			}
			else
			{
				return this.getSelectionModel().selectNext();
			}
		}
	},

	selectPrevious: function()
	{
		if(this.viewReady && !this.isDestroyed)
		{
			return this.getSelectionModel().selectPrevious();
		}
	},

	selectFirst: function()
	{
		if(this.viewReady && !this.isDestroyed)
		{
			return this.getSelectionModel().selectFirstRow();
		}
	},

	selectLast: function()
	{
		if(this.viewReady && !this.isDestroyed)
		{
			return this.getSelectionModel().selectLastRow();
		}
	},

	clearSelections: function()
	{
		if(this.viewReady && !this.isDestroyed)
		{
			this.getSelectionModel().clearSelections();
		}
	},
	
	updateColumnsVisibility: function(fieldsToShow)
	{
		var model = this.getColumnModel();
		var count = model.getColumnCount();

		for(var i = 0; i < count; i++)
		{
			var column = model.columns[i];
			
			if(column.dataHidable)
			{
				model.setHidden(i, fieldsToShow.indexOf(column.dataIndex) == -1);
			}
		}
	}
});