Z8.GridViewMode = 
{
	Table: 'table',
	Form: 'form',
	Chart: 'chart',
	TableForm: 'table/form',
	TableChart: 'table/chart'
};

Z8.view.GridView = Ext.extend(Z8.Panel,
{
	query: null,

	layout: 'border',
	
	border: false,
	frame: false,
	
	grid: null,
	form: null,
	
	showToolbarBtnText: true,
	
	addButton: null,
	copyButton: null,
	deleteButton: null,
	
	filesButton: null,
	printButton: null,

	tableButton: null,
	formButton: null,
	chartButton: null,
	
	treeButton: null,
	
	importButton: null,
	importToRecordButton: null,
	
	readOnly: false,

	viewMode: Z8.GridViewMode.Grid,
	
	constructor: function(config)
	{
		this.center = new Ext.Container({ region: 'center', layout: 'fit' });
		this.east = new Ext.Container({ region: 'east', width: '50%', split: true, layout: 'vbox', layoutConfig: { align: 'stretch' }, hidden: true });
		
		config.items = [this.center, this.east];

		this.updateTask = new Ext.util.DelayedTask(this.update, this);

		Z8.view.GridView.superclass.constructor.call(this, config);
	},
	
	initComponent: function()
	{
		Z8.view.GridView.superclass.initComponent.call(this);

		this.addEvents(
			'beforeselect', 
			'selectionchange', 
			'refresh', 
			'goto', 
			'gotorecord', 
			'cellLinkClick',
			'cellAltClick',
			'viewready',
			'controlsUpdated',
			'docollapse',
			'message',
			'notsaved',
			'required',
			'import'
		);

		this.grid = createGrid(this.query, { border: true });
		
		this.center.add(this.grid);

		var selectionModel = this.grid.getSelectionModel();
		selectionModel.on('selectionchange', this.onSelectionChange, this);
		selectionModel.on('beforerowselect', this.onBeforeRowSelect, this);

		var store = this.getStore();
		store.on('update', this.onDataChanged, this);
		store.on('remove', this.onDataChanged, this);
		store.on('datachanged', this.onDataChanged, this);
		store.on('clear', this.onDataChanged, this);
		store.on('save', this.onDataSaved, this);
		store.on('load', this.onLoad, this);
		store.on('beforesave', this.onBeforeSave, this);
		store.on('success', this.onDataSuccess, this);
		store.on('error', this.onDataError, this);

		this.grid.on('keydown', this.onKeyDown, this);
		this.grid.on('cellcontextmenu', this.onCellContextMenu, this);
		this.grid.on('groupcontextmenu', this.onGroupContextMenu, this);
		this.grid.on('cellmousedown', this.onCellMouseDown, this);
		this.grid.on('viewready', this.onGridRendered, this);

		this.createButtons();

		this.setViewMode(this.query.viewMode);
		
		this.grid.on('cellcontextmenu', this.onFileContextMenuShow, this);
		this.grid.on('cellclick', this.onFileContextMenuShow, this);
		this.grid.on('celldblclick', this.onCellDblClick, this);

	},
	
	afterRender: function(container)
	{
		if (this.showToolbarBtnText === false)
		{
			Ext.each(this.toolbarItems, function(item, index){
				if (item != this.titleItem && item != this.detailButton){
					item.overflowText = item.text;
					item.text = null;
				}
			}, this);
		}
		
		Z8.view.GridView.superclass.afterRender.call(this, container);
	},
	
	onBeforeSave: function(store, data)
	{
		var colModel = this.grid.getColumnModel();
		var canSave = true;
		var records = data.update || data.create;
		
		if (records)
		{
			Ext.each(colModel.columns, function(column)
			{
				if(column.required)
				{
					Ext.each(records, function(record)
					{
						var store = record.store;
						var field = store.fields.get(column.dataIndex);
						var value = record.get(field.linked ? field.linkId : field.id);

						if(Z8.isEmpty(value))
						{
							this.fireEvent('required', column , record);
							canSave = false;
						}
					}, this);
				}
			}, this);
		}
		
		if(!canSave)
		{
			this.fireEvent('notsaved', this);
		}

		return canSave;
	},
	
	
	alertEmptyMessage: function(header, record)
	{
		Z8.MessageBox.show({
			title: header,
			msg: 'Поле "' + header + '" обязательно для заполнения',

			buttons: Ext.Msg.OK,
			scope: this,
			fn: function(btn)
			{
				this.grid.selectRecord(record);
			},
			icon: Ext.MessageBox.QUESTION
		}, this);
	},
	
	onCellDblClick: function(grid, rowIndex, cellIndex, e)
	{
		grid.skipFirstClick = true;
		grid.skipSecondClick = true;
	},
	
	onFileContextMenuShow: function(grid, rowIndex, cellIndex, e)
	{
		if(grid.skipFirstClick)
		{
			delete grid.skipFirstClick;
			return;
		}
		else if (grid.skipSecondClick) {
			delete grid.skipSecondClick;
			return;
		}
		
		var el = Ext.get(e.getTarget());
		
		if (el.hasClass('silk-attach'))
		{
		    var store = grid.getStore();
			var record = grid.getRecord(rowIndex);
			
			var files = Ext.decode(record.get(store.filesProperty));
			
			if (files.length > 0)
			{
				var menu = new Z8.view.FilesButtonMenu({files: files, iconClickable: this.readOnly, locked: store.isRecordLocked(record) });
				menu.showAt(e.getXY());
				menu.on('itemRemoved', this.onFileRemoved, this);
				e.stopEvent();
			}
		}
	},

	createButtons: function()
	{
		this.addButton = new Z8.view.AddButton({ plain: true, query: this.query });
		this.addButton.on('addRecord', this.onAddRecord, this);
		this.addButton.on('addRootRecord', this.onAddRootRecord, this);
	
		this.copyButton = new Z8.Button({ plain: true, tooltip: 'Копировать запись', disabled: true, iconCls: 'icon-copy', iconAlign: 'top', text: 'Копировать', handler: this.onCopyRecord, scope: this });
		this.deleteButton = new Z8.Button({ plain: true, tooltip: 'Удалить запись (Delete)', disabled: true, iconCls: 'icon-delete', iconAlign: 'top', text: 'Удалить', handler: this.onDeleteRecord, scope: this });
	
		this.filesButton = new Z8.view.FilesButton({ plain: true, query: this.query });
		this.filesButton.on('beforeMenuShow', this.onFilesMenuBeforeShow, this);
		this.filesButton.on('click', this.onFileAttach, this);
		//this.filesButton.on('menuItemRemoved', this.onFileRemoved, this);
		
		this.printButton = new Z8.view.PrintButton({ plain: true, query: this.query });
		this.printButton.on('report', this.onPrint, this);

		if(!Z8.isEmpty(this.query.commands))
		{
			this.commandButton = new Z8.view.CommandButton({plain: true, align: 'right', query: this.query });
		}
		
		this.tableButton = new Z8.Button({ plain: true, text: 'Таблица', tooltip: 'Таблица', disabled: false, iconCls: 'icon-table', iconAlign: 'top', enableToggle: true, pressed: true, toggleHandler: this.onTableToggle, scope: this });
		this.formButton = new Z8.Button({ plain: true, text: 'Форма', tooltip: 'Форма', disabled: false, iconCls: 'icon-form', iconAlign: 'top', enableToggle: true, pressed: false, toggleHandler: this.onFormToggle, scope: this });
		this.chartButton = new Z8.Button({ plain: true, text: 'График', tooltip: 'График', disabled: true, iconCls: 'icon-chart', iconAlign: 'top', enableToggle: true, pressed: false, toggleHandler: this.onChartToggle, scope: this });

		this.treeButton = new Z8.Button({ plain: true, text: 'Иерархия', tooltip: 'Иерархия / Список', disabled: false, iconCls: 'icon-chart', iconAlign: 'top', enableToggle: true, pressed: true, toggleHandler: this.onTreeToggle, scope: this });
		
		this.importButton = new Z8.Button({ plain: true, tooltip: 'Импорт', iconCls: 'icon-copy', iconAlign: 'top', text: 'Импорт', handler: this.onImport, scope: this });
		this.importToRecordButton = new Z8.Button({ plain: true, tooltip: 'Импорт 1', iconCls: 'icon-copy', iconAlign: 'top', text: 'Импорт 1', handler: this.onImport, scope: this });

		this.mergeRecordsButton = new Z8.Button({ plain: true, tooltip: 'Объединение записей', iconCls: '', iconAlign: 'top', text: 'Объединить<br>записи', handler: this.onMergeRecords, scope: this });
		
		this.collapseButton =  new Z8.Button({ plain: true, align: 'right', tooltip:{text: 'Свернуть / Развернуть', align: 'br-tl'}, iconCls: 'icon-collapse', iconAlign: 'top', text: 'Свернуть', handler: this.onCollapse, scope: this});
		this.closeButton =  new Z8.Button({ plain: true, align: 'right', tooltip:{text: 'Закрыть', align: 'br-tl'}, iconCls: 'icon-close', iconAlign: 'top', text: 'Закрыть', handler: this.onClose, scope: this});

		this.filterButton = new Z8.view.FilterButton({ plain: true, query: this.query, filter: this.getFilter(), iconAlign: 'top', text: 'Фильтр', iconCls: 'icon-magnifier' });
		this.filterButton.on('filterToggle', this.onFilterToggle, this);
	},
	
	getFilter: function()
	{
		var settings = Z8.getUserSettings();
		
		if(settings.filters != null)
		{
			var filter = settings.filters[this.query.requestId];
	
			if(filter != null)
			{
				return Ext.apply({}, filter);
			}
		}
		
		return null;
	},
	
	setFilter: function(filter)
	{
		var settings = Z8.getUserSettings();
		
		if(settings.filters == null)
		{
			settings.filters = {};
		}
		
		if(filter != null)
		{
			settings.filters[this.query.requestId] = Ext.apply({}, filter);
		}
		else
		{
			delete settings.filters[this.query.requestId];
		}
		
		Z8.Ajax.request('settings', Ext.emptyFn, Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},

	onFilterToggle: function(item, pressed, filter)
	{
		this.setFilter(filter);
		this.query.filter1 = pressed ? filter : null;
		this.getStore().refresh();
	},

	onCollapse: function()
	{
		if(!this.collapseButton.minified)
		{
			if(this.showToolbarBtnText){
				this.collapseButton.setText('Развернуть');
			}
			this.collapseButton.setIconClass('icon-expand');
			this.collapseButton.minified = true;
		}
		else
		{
			if(this.showToolbarBtnText){
				this.collapseButton.setText('Свернуть');
			}
			this.collapseButton.setIconClass('icon-collapse');
			this.collapseButton.minified = false;
		}
		this.fireEvent('docollapse', this);
	},

	onRecordFailure: function(info)
	{
		this.fireEvent('message', this.query.text, info);
	},
	
	onRecordSuccess: function(info)
	{
		this.fireEvent('message', this.query.text, info);
	},

	onAddRecord: function(button)
	{
		this.grid.addRecord(this.isTableVisible(), this.onRecordSuccess, this.onRecordFailure, this);
	},

	onAddRootRecord: function(button)
	{
		this.grid.addRootRecord(this.isTableVisible(), this.onRecordSuccess, this.onRecordFailure, this);
	},

	onCopyRecord: function()
	{
		this.grid.copyRecord(this.isTableVisible(), this.onRecordSuccess, this.onRecordFailure, this);
	},

	onDeleteRecord: function()
	{
		this.grid.deleteRecord();
	},

	onFilesMenuBeforeShow: function(button, menu)
	{
		var record = this.grid.getSelectedRecord();
		
		var files = record.data[this.getStore().filesProperty];
		files = Z8.isEmpty(files) ? [] : eval(files);
		
		if(Z8.isEmpty(files))
		{
			return false;
		}
		
		button.initializeMenu(files);
		
		return true;
	},
	
	onFileAttach: function()
	{
		this.grid.stopEditing();
		var record = this.grid.getSelectedRecord();

		var store = this.getStore();
		
		var params = { 
			requestId: store.query.requestId, 
			recordId: record.id, 
			xaction: 'attach',
			field: store.filesProperty
		};

		if(this.query.queryId != null)
			params.queryId = this.query.queryId;
		
		var uploadDialog = new Z8.view.UploadDialog({ title: 'Присоединить файл', params: params });
		uploadDialog.on('ok', this.onFileAttached.createDelegate(this, [record.id], true), this);
		uploadDialog.show();
	},
	
	onFileAttached: function(newFiles, recordId)
	{
		var store = this.getStore();
		var record = store.getById(recordId);
		
		if(record != null)
		{
			record.data[store.filesProperty] = Ext.encode(newFiles);
			record.store.fireEvent('update', record.store, record, Ext.data.Record.EDIT);
		}
	},	
	
	onFileRemoved: function(menu, item)
	{
		var store = this.getStore();
		var record = this.grid.getSelectedRecord();

		var params = { 
        	xaction: 'detach',
            field: store.filesProperty,
            requestId: store.query.requestId,
            recordId: record.id,
            data: Ext.encode([item.file.id])
		};

		if(this.query.queryId != null)
			params.queryId = this.query.queryId;

		var onSuccess = function(result) {
			var files = eval(record.data[store.filesProperty]); 
			var result = [];
			
			for(var i = 0; i < files.length; i++) {
				if(files[i].id != item.file.id)
					result.push(files[i]);
			}
			
			record.data[store.filesProperty] = Z8.isEmpty(result) ? '' : Ext.encode(result);
			record.store.fireEvent('update', record.store, record, Ext.data.Record.EDIT);
			menu.initialize(result);
			if(Z8.isEmpty(result))
				menu.hide();
		}
		
		var onError = this.onDataError;
		
		Z8.Ajax.request(store.query.requestId, onSuccess, onError, params, this);
	},
	
	onPrint: function(button, format, report)
	{
		button.setBusy(true);

		var callback = Z8.Callback.create(this.onPrintComplete, this, button);

		var records = this.getSelectedRecords();

		var ids = [];

		for(var i = 0; i < records.length; i++)
		{
			ids.push(records[i].id);
		}

		Z8.ReportManager.runReport(this.grid, this.query, this.grid.filters, report, format, ids, callback);
	},
	
	onPrintComplete: function(button, messages)
	{
		button.setBusy(false);
		
		this.fireEvent('message', this.query.text, messages);
	},
	
	isDirty: function()
	{
		return this.grid.isDirty();
	},
	
	stopEditing: function()
	{
		this.grid.stopEditing();
	},
	
	onKeyDown: function(e)
	{
		var key = e.getKey();

/*		if(key == e.S && e.ctrlKey)
		{
//			if(this.saveButton != null && !this.saveButton.disabled)
//			{
//				this.onSave();
//			}
//			Z8.stopEvent(e);
		}
		else*/ if(key == e.DELETE)
		{
			if(this.deleteButton != null && !this.deleteButton.disabled)
			{
				this.onDeleteRecord();
			}
			Z8.stopEvent(e);
		}
		else if(key == e.F5)
		{
//			Z8.stopEvent(e);
		}
		else if(this.addButton != null && key == e.I && e.ctrlKey)
		{
			if(!this.addButton.disabled)
			{
				this.onAddRecord();
			}
			Z8.stopEvent(e);
		}
		else if(key == e.A && e.ctrlKey)
		{
			this.grid.getSelectionModel().selectAll();
			Z8.stopEvent(e);
		}
		else if(key == e.M && e.ctrlKey)
		{
			this.showDataModel();
			Z8.stopEvent(e);
		}
	},

	onGridRendered: function(grid)
	{
		var view = grid.getView();
		
		var menu = view.hmenu;
		
		if(menu.context == null)
		{
			menu.context = {};
			menu.context.filterByItem = new Ext.menu.Item({ text: '', handler: this.onFilterBy, scope: this });
			menu.insert(menu.items.getCount() - 1, menu.context.filterByItem);

			menu.context.separator = menu.addSeparator();
			menu.context.editWithItem = new Ext.menu.Item({ text: '', handler: this.onEditWith, scope: this, iconCls: 'silk-table-go' });
			menu.addItem(menu.context.editWithItem);
			menu.context.editRecordItem = new Ext.menu.Item({ text: '', handler: this.onEditRecord, scope: this, iconCls: 'silk-table-go' });
			menu.addItem(menu.context.editRecordItem);
			
			if(this.query.parentKey != null)
			{
				menu.addSeparator();
				menu.context.moveItem = new Ext.menu.Item({ text: 'Перенести...', handler: this.onMoveItem, scope: this });
				menu.addItem(menu.context.moveItem);
				menu.context.moveToRootItem = new Ext.menu.Item({ text: 'Сделать корневым', handler: this.onMoveToRootItem, scope: this });
				menu.addItem(menu.context.moveToRootItem);
			}
			
			menu.on('beforeshow', this.onShowContextMenu, this);
			menu.on('beforehide', this.onHideContextMenu, this);
		}
		
		grid.selectFirst();
		
		if(this.forceFocus !== false)
		{
			grid.focus();
		}
		
		this.updateButtons();
		
		this.fireEvent('viewready', this);
	},	
	
	onDestroy: function()
	{
		this.updateTask.cancel();

		Z8.view.GridView.superclass.onDestroy.call(this);
	},
	
	onGroupContextMenu: function(grid, field, groupValue, e)
	{
		if(this.groupMenu == null)
		{
			this.groupMenu = new Ext.menu.Menu({id: grid.id + '-groupctx'});
            this.groupMenu.add(
                {itemId:'expandAll',  text: 'Развернуть все группы'},
                {itemId:'collapseAll', text: 'Cвернуть все группы'},
                {itemId:'collapseLevel', text: 'Cвернуть этот уровень'}
            );

			this.groupMenu.on('itemclick', this.onGroupMenuClick, this);
		}
		
        var hd = e.getTarget('.x-grid-group-hd', this.mainBody);
		var id = hd.id.substr(0, hd.id.length - 3);

		this.groupMenu.groupId = id;
		this.groupMenu.showAt(e.xy);
		Z8.stopEvent(e);
	},

	onGroupMenuClick: function(item)
	{
		var grid = this.grid;
		var view = grid.getView();
		var store = grid.getStore();
		
		var groupEls = view.getAllGroups();

		var id = item.getItemId();

		if(id == 'expandAll' || id == "collapseAll")
		{
			
			for(var i = 0; i < groupEls.length; i++)
			{
				view.toggleGroup(groupEls[i], id == 'expandAll');
			}
		}
        else
        {
			var groups = store.getGroups();
			var group = view.findGroupById(groups, this.groupMenu.groupId);
		
			for(var i = 0; i < groupEls.length; i++)
			{
				if(groupEls[i].attributes['level'].nodeValue == group.level)
				{
					view.toggleGroup(groupEls[i], false);
				}
			}
        }
	},
	
	onCellContextMenu: function(grid, rowIndex, colIndex, e)
	{
		var view = grid.getView();
		
		view.hdCtxIndex = colIndex;
		
		var menu = view.hmenu;
		
		var sortable = grid.getColumnModel().isSortable(colIndex);
		menu.items.get('asc').setDisabled(!sortable);
		menu.items.get('desc').setDisabled(!sortable);

		menu.context.rowIndex = rowIndex;
		
		grid.select(rowIndex);
		
		menu.showAt(e.xy);
		
		Z8.stopEvent(e);
	},
	
	onShowContextMenu: function(menu)
	{
		var context = menu.context;
		var store = this.getStore();
		
		var rowIndex = context.rowIndex;
		var view = this.grid.getView();
		var colIndex = view.hdCtxIndex;
		
		var column = this.grid.getColumnModel().columns[colIndex];
		var dataIndex = column.dataIndex;

		context.colIndex = colIndex;
		context.dataIndex = dataIndex;
		
		var selectedRecord = this.grid.getSelectedRecord();
		
		if(rowIndex == null && selectedRecord != null)
		{
			rowIndex = context.rowIndex = this.grid.getRowIndex(selectedRecord);
		}

		var showFilterBy = rowIndex != null && column.filterable !== false;
		
		context.filterByItem.setVisible(showFilterBy);
		
		if(showFilterBy)
		{
			var record = this.grid.getRecord(rowIndex);
			
			context.value = record.data[dataIndex];
			var text = column.renderer.call(column.scope, menu.context.value, null, record, rowIndex, colIndex, store);
			context.filterByItem.setText("Фильтровать по '" + Ext.util.Format.ellipsis(text, 40, true) + "'");
		}
		
		var field = this.getStore().query.getFieldById(dataIndex);
	
		var editWith = field != null ? field.editWith : null;

		context.separator.setVisible(editWith != null);
		context.editWithItem.setVisible(editWith != null);
		context.editRecordItem.setVisible(editWith != null && rowIndex != null);
	
		if(editWith != null)
		{
			context.editWith = editWith;
			context.editWithItem.setText("Открыть '" + Ext.util.Format.ellipsis(field.editWithText, 40, true) + "'");
			
			if(rowIndex != null)
			{
				var record = this.grid.getRecord(rowIndex);
				
				var value = record.data[dataIndex];
				
				if(Z8.isEmpty(value))
				{
					context.editRecordItem.setVisible(false);
				}
				else
				{
					var text = column.renderer.call(column.scope, record.data[dataIndex], null, record, rowIndex, colIndex, store);
					context.editRecordItem.setText("Открыть '" + Ext.util.Format.ellipsis(text, 40, true) + "'");
				}
			}
		}
		
		if(context.moveItem != null)
		{
			context.moveItem.setDisabled(rowIndex == null);
			context.moveToRootItem.setDisabled(rowIndex == null);
		}
	},

	onCellMouseDown: function(grid, rowIndex, cellIndex, e)
	{
		if (e.altKey)
		{
			this.onShowAltContent(grid, rowIndex, cellIndex);
		}
		
		if(e.button == 0 && e.target.tagName == 'A')
		{
			this.fireEvent('cellLinkClick', this, rowIndex, cellIndex);
		}
	},
	
	onShowAltContent: function(grid, rowIndex, cellIndex)
	{
		var column = grid.getColumnModel().columns[cellIndex];
		var dataIndex = column.dataIndex;
		var field = grid.getStore().query.getFieldById(dataIndex);
		var editWith = field != null ? field.editWith : null;
		
		if(editWith != null)
		{
			this.fireEvent('cellAltClick', editWith);
		}
	},

	onEditWith: function(menuItem)
	{
		var context = menuItem.parentMenu.context;
		this.fireEvent('goto', context.editWith);
	},
	
	onEditRecord: function(menuItem)
	{
		var context = menuItem.parentMenu.context;
		var record = this.grid.getRecord(context.rowIndex);
		this.fireEvent('gotorecord', this.grid, record, context.editWith, context.dataIndex);
	},

	onFilterBy: function(menuItem)
	{
		var filters = this.grid.filters;
		var filter = filters.getMenuFilter();
		var value = menuItem.parentMenu.context.value;
		
		if(filter instanceof Ext.ux.grid.filter.DateFilter)
		{
			filter.setValue({ on: value });
		}
		else if(filter instanceof Ext.ux.grid.filter.NumericFilter)
		{
			filter.setValue({ eq: value });
		}
		else
		{
			filter.setValue(value);
		}
		
		filter.setActive(true);
	},

	onMoveItem: function(menuItem)
	{
		var context = menuItem.parentMenu.context;
		var record = this.grid.getRecord(context.rowIndex);

		var onSuccess = this.onRequest.createDelegate(this, [record], true);
		var onError = this.onDataError;

		Z8.Ajax.request(this.query.requestId, onSuccess, onError, {}, this);
	},

	onMoveToRootItem: function(menuItem)
	{
		var context = menuItem.parentMenu.context;
		var record = this.grid.getRecord(context.rowIndex);
		
		this.onMove('', record);
	},

	onRequest: function(query, record)
	{
		var moveWindow = new Z8.view.MoveDialog({ query: query });
		moveWindow.on('ok', this.onMove.createDelegate(this, [record], true), this);
		moveWindow.show();
	},
	
	onMove: function(parentId, record)
	{
		Z8.Ajax.request(this.query.requestId, this.onMoved, this.onDataError, { xaction: 'move', recordId: record.id, parentId: parentId }, this);
	},
	
	onMoved: function()
	{
		this.getStore().reload();
	},
	
	onHideContextMenu: function(menu)
	{
		delete menu.context.rowIndex;
	},
	
	onLoad: function()
	{
		var store = this.getStore();
		
		if(store != null)
		{
			var record = store.getCurrentRecord();
			this.grid.selectRecord(record, true);
			this.updateForm();
		}
	},

	setViewMode: function(viewMode)
	{
		if(this.viewMode != viewMode)
		{
			if(viewMode == Z8.GridViewMode.Table)
			{
				this.hideForm();
				this.hideChart();
			}
			else if(viewMode == Z8.GridViewMode.Form)
			{
				this.hideTable();
				this.showForm();
			}
			else if(viewMode == Z8.GridViewMode.TableForm)
			{
				this.hideChart();
				this.showForm();
			}
			else if(viewMode == Z8.GridViewMode.TableChart)
			{
				if(!this.isChartAvailable())
				{
					return;
				}
				
				this.hideForm();
				this.showChart();
			}
			else if(viewMode == Z8.GridViewMode.Chart)
			{
				if(!this.isChartAvailable())
				{
					return;
				}
				
				this.hideTable();
				this.showChart();
			}
			else
			{
				return;
			}

			this.viewMode = viewMode;
			this.doLayout();
		}
	},
	
	isTableVisible: function()
	{
		return this.center.isVisible();
	},

	isFormVisible: function()
	{
		return this.east.isVisible() && this.form != null;
	},
	
	isChartVisible: function()
	{
		return this.east.isVisible() && this.chart != null;
	},

	isChartAvailable: function()
	{
		return true; //this.isAggregated();
	},
	
	display: function(component, show)
	{
		delete component.deferLayout;
		component.setVisible(show);
	},

	showTable: function()
	{
		this.display(this.center, true);
		this.tableButton.toggle(true, true);
		this.updateForm();

		if(this.center.items.getCount() == 0)
		{
//			this.center.add(this.grid);
		}
	},

	hideTable: function()
	{
		this.grid.stopEditing();

		this.display(this.center, false);
		this.tableButton.toggle(false, true);

		if(this.center.items.getCount() == 1)
		{
			this.grid.colspan = this.query.columns;
			this.grid.stretch = true;
			this.grid.fieldLabel = this.query.text;
			this.grid.showLabel = this.query.text;
			this.grid.minWidth = 100;

			if(this.form != null)
			{
//				this.form.form.add(this.grid);
			}
		}
	},

	showForm: function()
	{
		this.grid.stopEditing();

		if(this.form == null)
		{
			this.form = new Z8.form.FormView({ query: this.query, flex: 1, border: true });
			this.east.add(this.form);
		}

		this.display(this.east, true);
		this.display(this.form, true);
		
		this.formButton.toggle(true, true);
		this.updateForm();
	},
	
	hideForm: function()
	{
		if(this.form != null)
		{
			this.display(this.east, false);
			this.display(this.form, false);
		}

		this.formButton.toggle(false, true);
	},
	
	showChart: function()
	{
		this.grid.stopEditing();

		if(this.chart == null)
		{
			var settings = this.getSettings();
			this.chart = new Z8.chart.ChartPanel({ flex: 1, query: this.query, chartConfig: settings.chart, changeHandler: this.onChartChanged, scope: this });
			this.east.add(this.chart);
		}

		this.display(this.east, true);
		this.display(this.chart, true);
		
		this.chartButton.toggle(true, true);
	},

	getSettings: function()
	{
		var settings = Z8.getUserSettings();
		return settings[this.query.requestId] || {};
	},
	
	setSettings: function(settings)
	{
		var userSettings = Z8.getUserSettings();
		userSettings[this.query.requestId] = settings;
		Z8.Ajax.request('settings', Ext.emptyFn, Ext.emptyFn, { data: Ext.encode(userSettings) }, this);
	},

	onChartChanged: function(chart, config)
	{
		var settings = this.getSettings();
		
		if(config != null)
		{
			delete config.dataSource.query;
			settings.chart = config;
		}
		else
		{
			delete settings.chart;
		}
		
		this.setSettings(settings);
	},
	
	hideChart: function()
	{
		if(this.chart != null)
		{
			this.display(this.east, false);
			this.display(this.chart, false);
		}

		this.chartButton.toggle(false, true);
	},
	
	onTableToggle: function(button, showTable)
	{
		if(showTable)
		{
			this.showTable();
		}
		else
		{
			this.hideTable();
			
			if(!this.isFormVisible() && !this.isChartVisible())
			{
				this.showForm();
			}
		}

		this.doLayout(true);
	},

	onFormToggle: function(button, showForm)
	{
		if(showForm)
		{
			this.hideChart();
			this.showForm()			
		}
		else
		{
			this.hideForm();
			this.showTable();
		}

		this.doLayout(true);
	},

	onChartToggle: function(button, showChart)
	{
		if(showChart)
		{
			this.hideForm();
			this.showChart();
		}
		else
		{
			this.hideChart();
			this.showTable();
		}
	
		this.doLayout(true);
	},

	onTreeToggle: function(button, showTree)
	{
		showTree ? this.grid.showAsTree() : this.grid.showAsGrid();
	},

	onMergeRecords: function(button)
	{
		var records = this.getSelectedRecords();
		var ids = [];
		
		for(var i = 0; i < records.length; i++)
		{
			ids.push(records[i].id);
		}
		
		var config = { records: ids, query: this.query.requestId, table: this.query.name };

		var steps = [];
		
		steps.push(new Z8.grid.DataSourcePanel({ config: config }));
		steps.push(new Z8.grid.RecordsMergerConsole({ config: config }));

		var wizard = new Z8.view.WizardWindow({ title: 'Объединение записей', steps: steps });
		wizard.on('finish', this.onMergeDone, this);
		wizard.show();
	},
	
	onMergeDone: function(wizard)
	{
	},
	
	onImport: function(button)
	{
		var params = {};
		
		params.requestId = this.query.requestId;
		params.xaction = 'import';
		
		if(!Z8.isEmpty(this.query.queryId))
		{
			params.queryId = this.query.queryId;
		}
		
		if(button == this.importToRecordButton)
		{
			this.fireEvent('import', params);
		}
		
		var uploadDialog = new Z8.view.UploadDialog({ title: 'Импортировать файл', params: params });
		uploadDialog.on('ok', this.onImported, this);
		uploadDialog.on('error', this.onUploadError, this);
		uploadDialog.show();
	},
	
	onUploadError: function(info)
	{
		this.fireEvent('message', this.query.text, info);
	},
	
	onImported: function(result)
	{
		var win = new Z8.view.ImportWindow({result: result});
		win.on('importDone', this.onImportDone, this);
		win.show();
	},

	onImportDone: function()
	{
		this.getStore().reload();
	},
	
	getSelectedRecord: function()
	{
		return this.grid.getSelectedRecord();
	},

	getSelectedRecords: function()
	{
		return this.grid.getSelectedRecords();
	},

	refresh: function(refreshData)
	{
		var records = refreshData.records[this.query.name];
		var index = refreshData.queries.indexOf(this.query.name);
		
		if(records != null)
		{
			for(var i = 0; i < records.length; i++)
			{
				this.grid.refreshRecord(records[i], this.onRecordSuccess, this.onRecordFailure, this);
			}
			delete refreshData.records[this.query.name];
		}
		else if(index != -1)
		{
			this.getStore().reload();
			refreshData.queries.splice(index, 1);
		}
	},
	
	setReadOnly: function(readOnly)
	{
		this.readOnly = readOnly;
	},
	
	updateButtons: function()
	{
		if(this.el != null && this.el.dom != null)
		{
			this.addButton.setDisabled(!this.canAddRecord() || this.readOnly);
			this.copyButton.setDisabled(!this.canCopyRecord() || this.readOnly);
			this.deleteButton.setDisabled(!this.grid.canDeleteRecord() || this.readOnly);
			
			this.filesButton.setDisabled((this.getSelectedRecords().length != 1) || this.readOnly);
			
			this.chartButton.setDisabled(!this.isChartAvailable());
			
			this.printButton.enable();
			
			if(this.commandButton != null)
			{
				this.commandButton.enable();
			}

			this.mergeRecordsButton.setDisabled(this.getSelectedRecords().length == 0 || this.readOnly);

			this.fireEvent('controlsUpdated', this);
		}
	},

	canAddRecord: function()
	{
		return this.grid.canAddRecord() && this.query.recordId != "";
	},
	
	canCopyRecord: function()
	{
		return this.grid.getSelectedRecord() != null;
	},

	getStore: function()
	{
		return this.grid.getStore();
	},
	
	isStoreDirty: function()
	{
		var store = this.getStore();
		return store.modified.length != 0 || store.removed.length != 0;
	},

	isAggregated: function()
	{
		var store = this.getStore();
		var groupField = store.getGroupState();
		
		if(groupField == null)
		{
			return false;
		}
		
		for(var i = 0; i < store.fields.items.length; i++)
		{
			var field = store.fields.items[i];
			
			if(field != groupField && field.aggregation != null)
			{
				return true;
			}
		}
		
		return false;
	},
	
	onDataChanged: function()
	{
		this.updateButtons();
		this.updateForm();
	},

	onDataSaved: function()
	{
		this.updateButtons();
	},

	onDataSuccess: function(info)
	{
		this.fireEvent('message', this.query.text, info);
	},

	onDataError: function(info)
	{
		this.fireEvent('message', this.query.text, info);
		this.updateButtons();
	},

	onBeforeRowSelect: function(selectionModel, row)
	{
		return this.fireEvent('beforeselect', row);
	},

	updateForm: function()
	{
		var record = this.grid.getSelectedRecord();
		this.getStore().setCurrentRecord(record);

		if(this.form != null)
		{
			this.form.setRecord(record);
		}
	},
	
	onSelectionChange: function()
	{
		this.fireEvent('selectionchange', this);
		this.startUpdate();
	},
	
	startUpdate: function()
	{
		this.updateTask.cancel();
		this.updateTask.delay(100);
	},
	
	update: function()
	{
		this.updateButtons();
		this.updateForm();
	},
	
	save: function()
	{
		this.grid.stopEditing();
		this.grid.getStore().save();
	},
	
	rejectChanges: function()
	{
		this.stopEditing();
		this.getStore().rejectChanges();
	},
	
	showDataModel: function()
	{
		this.query.request(this.doShowDataModel, Ext.emptyFn, { xaction: 'model', parentId: "" });
	},
	
	doShowDataModel: function(query)
	{
		var viewer = new Z8.view.DataModelViewer({ modal: true, closable: true, query: query });
		viewer.show();
	},
	
	updateFieldsVisibility: function(store, record)
	{
		var fieldsToShow = [];
		
		for(var i = 0; i < store.fields.getCount(); i++)
		{
			var field = store.fields.get(i);
			
			if(field.fieldsToShow != null)
			{
				var value = record != null ? record.get(field.linkedVia) : null;
				fieldsToShow = fieldsToShow.concat(field.fieldsToShow[value] || []);
			}
		}
		
		if(this.grid.updateColumnsVisibility != null)
		{
			this.grid.updateColumnsVisibility(fieldsToShow);
		}
	}
});