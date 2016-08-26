Z8.view.MasterDetailPanel = Ext.extend(Z8.Panel,
{
	closable: true,
	
	query: null,
	link: null,

	layout: 'border',
	animCollapse: false,	
	
	border: false,

	margins: { top: 3, left: 3, bottom: 3, right: 3 },

	master: null,
	detail: null,

	states: {},

	masterPanel: null,
	detailPanel: null,
	
	isDetailLoading: false,
	pendingRecord: null,

	selector: 'z8-master-detail-panel',
	
	saveButtonTooltip: 'Сохранить изменения (Ctrl+S)',

	masterLocked: false,

	initComponent: function()
	{

		this.title = this.query.text;

		Z8.view.MasterDetailPanel.superclass.initComponent.call(this);

		var hasBackwards = this.query.backwards.length != 0;
		
		this.master = new Z8.view.GridView({ flex: 1, parentRequestId: this.query.requestId, query: this.query, header: false });
		this.master.on('viewready', this.onMasterRendered, this);
		this.master.on('beforeselect', this.onBeforeSelect, this);
		this.master.on('selectionchange', this.onSelectionChange, this);
		this.master.on('refresh', this.onRefresh, this);
		this.master.on('cellLinkClick', this.onCellLinkClick, this);
		this.master.on('goto', this.onGoTo, this);
		this.master.on('gotorecord', this.onGoToRecord, this);
		this.master.on('controlsUpdated', this.updateButtons, this);
		this.master.on('gridView', this.onGridViewBtnClick, this);
		this.master.on('dataView', this.applyGridView, this);
		this.master.on('cellAltClick', this.onCellAltClick, this);
		this.master.on('message', this.onMessage, this);
		this.master.on('notsaved', this.onNotSaved, this);
		this.master.on('required', this.onRequired.createDelegate(this, ['master'], true), this);
		
		this.masterStateManager = new Z8.grid.GridStateManager();
		this.masterStateManager.init(this.master.grid);

		var store = this.master.getStore();
		store.on('beforeload', this.onMasterBeforeLoad, this);

		this.masterPanel = new Ext.Panel({ region: 'center', layout:'vbox', text: this.query.text, layoutConfig: { align: 'stretch' }, frame: false, border: false, items: this.master });
		
		this.innerPanel = new Ext.Panel({ layout: 'border', border: false, region: 'center', items: [this.masterPanel]});
		
		this.reportPanel = new Z8.view.ReportPanel({showToolbar: true, hidden: true, split: true, collapseMode: 'mini', layout: 'fit', region: 'east', width: 200});

		this.initMasterToolbar();
		
		//this.add(this.masterPanel);

		if(hasBackwards)
		{
			this.link = this.query.backwards[0];

			var cmargins = { top: 0, right: 0, bottom: 0, left: 0 };
			
			this.detailPanel = new Ext.Panel({ region: 'south', collapseMode: 'mini', cmargins: cmargins, layout: 'fit', frame: false, height: 200, border: false, split: true });
			this.on('resize', this.onResized, this, { single: true });
			//this.add(this.detailPanel);
			this.innerPanel.add(this.detailPanel);
		}
		
		this.add(this.innerPanel);
		this.add(this.reportPanel);
	},


	onResized: function(panel, adjWidth, adjHeight, rawWidth, rawHeight)
	{
		var height = Math.max(rawHeight - rawHeight * this.query.height, rawHeight / 3);
		this.detailPanel.setHeight(height);
	},
	
	initMasterToolbar: function()
	{
		if(!this.query.readOnly)
		{	
			if(this.query.canAdd())
			{
				this.toolbarItems.push(this.master.addButton);
			}
			
			if(this.query.canCopy())
			{
				this.toolbarItems.push(this.master.copyButton);
			}

			if(this.query.canDelete())
			{
				this.toolbarItems.push(this.master.deleteButton);
			}
		}
		
		this.toolbarItems.push(this.master.filesButton, this.master.filterButton, this.master.printButton);
		this.toolbarItems.push(this.master.tableButton, this.master.formButton, this.master.chartButton);
		
		if(this.query.parentKey != null)
		{
			this.toolbarItems.push(this.master.treeButton);
		}

		if(Z8.user.importEnbled && this.query.canImport())
		{	
			this.toolbarItems.push(this.master.importButton);
			this.toolbarItems.push(this.master.mergeRecordsButton);
		}

		var period = this.query.period;

		if(period != null)
		{
			this.periodButton = new Z8.PeriodButton( { plain: true, period: period.period, start: period.start, finish: period.finish } );
			this.periodButton.on('changed', this.onPeriodChanged, this);
			this.toolbarItems.push(this.periodButton);
		}
		
		this.saveButton = new Z8.Button({ plain: true, align: 'right', tooltip: this.saveButtonTooltip, disabled: true, iconCls: 'icon-save', iconAlign: 'top', text: 'Сохранить', handler: this.onSave, scope: this });
		this.refreshButton = new Z8.Button({ plain: true, align: 'right', tooltip: 'Обновить', disabled: true, iconCls: 'icon-refresh', iconAlign: 'top', text: 'Обновить', handler: this.refresh, scope: this });
		this.toolbarItems.push(this.saveButton, this.refreshButton);
		
		if(this.master.commandButton != null)
		{
			this.toolbarItems.push(this.master.commandButton);
			this.master.commandButton.on('command', this.onCommand, this);
		}
		
		this.stateButton = new Z8.view.StateButton({ plain: true, align: 'right', settings: Z8.getUserSettings(), query: this.query, panel: this });
		this.stateButton.on('stateChanged', this.onStateChanged, this);
		this.stateButton.on('initClick', this.onStateInit, this);
		this.toolbarItems.push(this.stateButton);
	},

	onCommand: function(button, command)
	{
		if (this.isDirty())
		{
			Z8.MessageBox.show({
				title: this.query.text,
				msg: 'Перед выполнением необходимо выполнить сохранение данных. Сохранить?',
				buttons: Ext.Msg.YESNOCANCEL,
				fn: this.saveBeforeCommand.createDelegate(this, [button, command], true),
				icon: Ext.MessageBox.QUESTION
			});
		}
		else
		{
			this.onCommandProcess(button, command);
		}
	},
	
	saveBeforeCommand: function(action, text, msgOptions, button, command)
	{
		if (action === 'yes')
		{
			var doSave = function(saved)
			{
				if(saved)
				{
					this.onCommandProcess(button, command);
				}
			}
			
			var callback = Z8.Callback.create(doSave, this);
			
			this.save(callback, true);
		}
	},
	
	onCommandProcess: function(button, command)
	{
		command.record = this.master.getSelectedRecord();
		
		if(command.isJob)
		{
			Z8.viewport.open(command);
		}
		else
		{
			var parameters = command.parameters;
			var record = command.record;

			if(parameters.length != 0)
			{
				var parametersWindow = new Z8.view.ParametersWindow({ parameters: parameters, record: record, title: command.text });
				
				var onOk = this.onParametersOk.createDelegate(this, [command, button], true);
				var onError = this.onParametersError.createDelegate(this, [command], true);
				
				parametersWindow.on('ok', onOk, this);
				parametersWindow.on('error', onError, this);
				parametersWindow.show();
			}	
			else
			{
				this.onParametersOk({}, null, command, button);
			}
		}
	},
	
	onParametersError: function(info, command)
	{
		this.onMessage(command.text, info);
	},
	
	onParametersOk: function(source, serverId, command, button)
	{
		button.setBusy(true);

		var callback = Z8.Callback.create(this.onCommandComplete, this, button);

		var records = this.master.getSelectedRecords();

		var ids = [];

		for(var i = 0; i < records.length; i++)
		{
			ids.push(records[i].id);
		}

		var params =
		{
			xaction: 'command', 
			command: command.id,
			parameters: Ext.encode(source),
			data: Ext.encode(ids)
		};
		
		if(serverId != null)
		{
			params.serverId = serverId;
		}
		
		var success = this.onCommandSuccess.createDelegate(this, [callback, command], true);
		var error = this.onCommandFailure.createDelegate(this, [callback, command], true);
		
		this.query.request(success, error, params, this);
	},
	
	onCommandComplete: function(button, info)
	{
		button.setBusy(false);
		
		this.onMessage(this.query.text, info);
	},
	
	onCommandSuccess: function(result, callback, command)
	{
		if(result.source != null)
		{
			Z8.FileViewer.download(result, this.query.text);
		}
		
		if(result.refresh != null)
		{
			this.onRefresh(this, result.refresh);
		}
		
		callback.call();
		this.updateButtons();
		this.onMessage(command.text, result.info);
	},

	onCommandFailure: function(info, callback, command)
	{
		callback.call();
		this.updateButtons();
		this.onMessage(command.text, info);
	},

	onMessage: function(title, info)
	{
		if(info != null && (!Z8.isEmpty(info.messages) || info.log != null))
		{
			this.reportPanel.show();
			this.reportPanel.onMessage(title, info);
			this.doLayout();
		}
	},

	initEvents: function()
	{
		Z8.view.MasterDetailPanel.superclass.initEvents.call(this);
		
		var ctrlS = { key: "s", ctrl:true, handler: this.onSave, scope: this, stopEvent: true };

		this.keyMap = new Ext.KeyMap(document.body, [ctrlS]);
	},
	
	getSaveButtonTooltip: function()
	{
		var tooltip = this.saveButtonTooltip;
		
		if(this.master.isDirty())
		{
			var changes = this.master.getStore().getChanges();
			
			tooltip += '<br>';
			tooltip += 'Изменено: ' + changes.modified + '<br>';
			tooltip += 'Создано: ' + changes.created + '<br>';
			tooltip += 'Удалено: ' + changes.removed;
		}
		else if(this.detail != null && this.detail.isDirty())
		{
			tooltip += '<br>';
			tooltip += 'Изменено: ' + 1 + '<br>';
			tooltip += 'Создано: ' + 0 + '<br>';
			tooltip += 'Удалено: ' + 0;
		}
		
		return tooltip;
	},
	
	updateButtons: function()
	{
		this.saveButton.setDisabled(!this.isDirty());
		this.saveButton.setTooltip(this.getSaveButtonTooltip());
		
		this.refreshButton.setDisabled(false);
		
		if(this.detail != null)
		{
			this.detail.importToRecordButton.setDisabled(this.master.getSelectedRecords().length != 1);
		}
	},
		
	doClose: function()
	{
		this.destroy();
	},
	
	onClose: function()
	{
		var doClose = function(saved)
		{
			if(saved)
			{
				this.doClose();
			}
		}
			
		var callback = Z8.Callback.create(doClose, this);
		this.save(callback);
	},
	
	onSave: function(button)
	{
		if(!this.saveButton.disabled)
		{
			if(this.master.isDirty())
			{
				var masterChanges = this.master.getStore().getChanges();	
			}
			else if(this.detail != null && this.detail.isDirty())
			{
				var detailChanges = this.detail.getStore().getChanges();
			}
			
			if(masterChanges && masterChanges.removed || detailChanges && detailChanges.removed)
			{
				Z8.MessageBox.show({
					title: this.query.text,
					msg: this.query.text + ' содержит удаленные данные. Сохранить?',
					buttons: Ext.Msg.YESNOCANCEL,
					fn: this.onSaveDeleteMsg.createDelegate(this),
					icon: Ext.MessageBox.QUESTION
				});
			}
			else
			{
				this.save(Ext.emptyFn, true);
			}
		}
	},
	
	onSaveDeleteMsg: function(action)
	{
		if(action == 'yes')
		{
			this.save(Ext.emptyFn, true);
		}
	},
	
	refresh: function()
	{
		if(!this.refreshButton.disabled)
		{
			this.master.getStore().refresh();
		}
	},
	
	onMasterBeforeLoad: function(store, options)
	{
		if(this.isDirty())
		{
			var loadStore = function(store, options, saved)
			{
				if(saved)
				{
					this.loadStore(store, options);
				}
			}
			
			var callback = Z8.Callback.create(loadStore, this, [store, options]);
			
			this.save(callback);
			return false;
		}
		
		this.refreshButton.setDisabled(true);
		
		return true;
	},
	
	onPeriodChanged: function(button, period)
	{
		this.query.period = period;
		this.refresh();
	},
	
	onStateInit: function(button, menuItem)
	{
		var settings = Z8.getUserSettings();
		var requestId = this.query.requestId;
		
		if (settings.dataviews)
		{
			var states = settings.dataviews[requestId];
			Ext.iterate(states, function(key, state) {
			var state = settings.dataviews[requestId][key];
				state.isDefault = false;
			});
		}
		
		button.menu.items.each(function(item) {
			if ( (item instanceof Ext.menu.CheckItem) && (item.menuid != "defaultState"))
			{
				if (item.checked)
				{	
					var state = settings.dataviews[requestId][item.menuid];
					state.isDefault = true;
					
					button.setBusy(true);
				
					Z8.Ajax.request('settings', this.onStateInited.createDelegate(this, [button, item]), Ext.emptyFn, { data: Ext.encode(settings) }, this);
				}
			}
		}, this);
	},
	
	onStateInited: function(button, menuItem)
	{
		button.setBusy(false);
	},
	
	onStateChanged: function(button, menuItem)
	{
		var settings = Z8.getUserSettings();
		var requestId = this.query.requestId;
		
		if (menuItem.menuid == 'defaultState')
		{
			this.masterStateManager.resetStates();
			if(this.detail != null) {
				this.detailStateManager.resetStates();
			}
		}
		else if(menuItem.menuid == 'sendState')
		{
			Z8.viewport.messenger.messengerWindow.show();
			var messenger = Z8.viewport.messenger.messenger;
			
			var masterState = this.masterStateManager.getState();
			masterState = Ext.encode(masterState);
			
			if(this.detail != null)
			{
				var detailState = this.detailStateManager.getState();
				detailState = Ext.encode(detailState);
			}
			
			var viewLink = "<a href='#' class='viewlink' id='" + this.query.requestId + "'";
				viewLink += " masterstate='" + masterState + "'";
				if(detailState){
					viewLink += " detailstate='" + detailState + "'";
				}
				viewLink += ">";
				viewLink += this.query.text;
				viewLink += "</a>";
			
			messenger.viewsToSend.push({
				name: this.query.text,
				link: viewLink
			});
			
			messenger.messenger.textArea.setValue(this.query.text);
		}
		else
		{
			if (settings.dataviews)
			{
				var state = settings.dataviews[requestId][menuItem.menuid];
				this.masterStateManager.restoreStates(state.master);
			}
		
			if(this.detail != null) {
				this.detailStateManager.restoreStates(state.detail);
			
				if (state.dimensions != null) {
					this.restoreHeights(state.dimensions);
				}
			
				if (state.collapsed != null)
				{
					if(state.collapsed) {
						this.detailPanel.collapse(false);
					} else {
						this.detailPanel.expand(false);
					}
				}
			}
		}
	},
	
	restoreHeights: function(dimensions)
	{
		var panelHeight = this.getEl().getHeight();
		var detailHeight = Math.floor(panelHeight * dimensions.detail);

		this.detailPanel.setHeight(detailHeight);
		
		this.doLayout();
	},

	onMasterRendered: function()
	{
		if(this.states)
		{
			if(this.states.master)
			{
				this.masterStateManager.restoreStates(this.states.master);
			}
		}

		var settings = Z8.getUserSettings();
		
		if (settings.dataviews)
		{
			var states = settings.dataviews[this.query.requestId];
		
		
			if (states)
			{
				Ext.iterate(states, function(key, state) {
					if (state.isDefault)
					{
						this.stateButton.setState(key);
					
						if (state.master) {
							this.masterStateManager.restoreStates(state.master);
						}
					}
				}, this);
			}
		}

		this.reloadDetailPanel();
	},
	
	onDetailChanged: function(button, menuItem)
	{
		if (menuItem.menuId == 'backwards')
		{
			this.reloadDetailPanel(false);
		}
		else
		{
			var link = this.query.backwards[menuItem.menuId];
	
			if(this.isDetailDirty())
			{
				var doDetailChanged = function(link, menuItem, saved)
				{
					if(saved)
					{
						this.doDetailChanged(link, menuItem);
					}
				}
			
				var callback = Z8.Callback.create(doDetailChanged, this, [link, menuItem]);

				this.save(callback);
			}
			else
			{
				this.doDetailChanged(link, menuItem);
			}
		}
	},
	
	doDetailChanged: function(link, menuItem)
	{
		if(this.link != link)
		{
			this.link = link;
			this.reloadDetailPanel(true);
		}
	},

	isDirty: function()
	{
		var isMasterDirty = this.isMasterDirty();
		var isDetailDirty = this.isDetailDirty();
		
		return isMasterDirty || isDetailDirty;
	},

	isMasterDirty: function() {
		return this.master.isDirty();
	},

	isDetailDirty: function() {
		if(this.detail == null || !this.detail.isDirty())
			return false;

		var recordId = this.detail.query.recordId;
		var masterRecord = this.master.getStore().getById(recordId);
		return masterRecord != null;
	},

	selectRow: function(index)
	{
		this.master.grid.select(index);
	},
	
	onSelectionChange: function()
	{
		if(this.detailPanel != null)
		{
			var records = this.master.getSelectedRecords();
			
			if(records.length == 1)
			{
				if(this.isDetailLoading)
				{
					this.pendingRecord = records[0];
				}
				else
				{
					this.reloadDetailStore(records[0]);
				}
			}
			else
			{
				this.reloadDetailStore(null);
			}
		}
	},

	onBeforeSelect: function(index)
	{
		if(this.detailPanel != null && this.isDetailDirty())
		{
			var selectRow = function(index, saved)
			{
				if(saved)
				{
					this.selectRow(index);
				}
			}
		
			var callback = Z8.Callback.create(selectRow, this, [index]);
			this.save(callback);
			return false;
		}

		return true;
	},

	reloadDetailStore: function(record) {
		if(this.detail != null) {
			var recordId = record != null ? record.id : guid.Null;
			var store = this.detail.getStore();
			store.query.recordId = recordId;

			if(record != null)
				store.load();
			else if(store.getCount() != 0)
				store.removeAll();

			var readOnly = record == null || this.master.grid.getStore().isRecordLocked(record);
			this.detail.setReadOnly(readOnly);

			this.detail.updateFieldsVisibility(this.master.getStore(), record);
		}

		this.master.getStore().setCurrentRecord(record);
	},

	onFailure: function(info) {
		this.onMessage(this.query.text, info);
	},

	reloadDetailPanel: function(skipBackwards) {
		if(this.detailPanel != null) {
			var records = this.master.getSelectedRecords();
			var record = Z8.isEmpty(records) ? null : records[0];

			var params = {};
			params.queryId = this.link.queryId;

			params.recordId = record != null ? record.id : guid.Null;
			var success = this.createDetailPanel.createDelegate(this, [skipBackwards], true);
			var error = this.onFailure;

			Z8.Ajax.request(this.query.requestId, success, error, params, this);
		}
	},

	createDetailPanel: function(query, skipBackwards) {
		if(this.detail != null) {
			this.detail.getStore().un('beforeload', this.onDetailBeforeLoad, this);
			this.detail.getStore().un('load', this.onDetailAfterLoad, this);
			this.detail.getStore().un('exception', this.onDetailError, this);
		}

		this.detailPanel.removeAll();
		var record = this.master.getSelectedRecord();
		var links = this.query.backwards;

		query.master = this.query;
		query.recordId = record != null ? record.id : guid.Null;

		this.detail = new Z8.view.GridView({parentLocked: this.masterLocked, showToolbarBtnText: false, forceFocus: false, query: query });
		this.initDetailToolbar(links);

		this.detail.on('viewready', this.onDetailRendered, this);
		this.detail.on('refresh', this.onRefresh, this);
		this.detail.on('cellLinkClick', this.onCellLinkClick, this);
		this.detail.on('goto', this.onGoTo, this);
		this.detail.on('gotorecord', this.onGoToRecord, this);
		this.detail.on('controlsUpdated', this.updateButtons, this);
		this.detail.on('cellAltClick', this.onCellAltClick, this);
		this.detail.on('docollapse', this.onCollapseDetail, this);
		this.detail.on('close', this.onCloseDetail, this);
		this.detail.on('message', this.onMessage, this);
		this.detail.on('notsaved', this.onNotSaved, this);
		this.detail.on('required', this.onRequired.createDelegate(this, ['detail'], true), this);
		this.detail.on('import', this.onImport, this);

		this.detailStateManager = new Z8.grid.GridStateManager();
		this.detailStateManager.init(this.detail.grid);

		this.detail.getStore().on('beforeload', this.onDetailBeforeLoad, this);
		this.detail.getStore().on('load', this.onDetailAfterLoad, this);
		this.detail.getStore().on('exception', this.onDetailError, this); // именно exception, а не error

		this.detailBackwards = new Z8.view.BackwardsView({data: links});
		this.detailBackwards.on('backwardclick', this.onBackwardClick, this);


		if(!Z8.isEmpty(links) && links.length > 1 && !skipBackwards)
			this.detailPanel.add(this.detailBackwards);
		else
			this.detailPanel.add(this.detail);

		this.onMessage(this.query.text, query.info);

		this.detailPanel.doLayout();
	},

	onDetailRendered: function() {
		if(this.states != null && this.states.detail != null)
			this.detailStateManager.restoreStates(this.states.detail);

		var settings = Z8.getUserSettings();

		if (settings.dataviews) {
			var states = settings.dataviews[this.query.requestId];

			if (states) {
				Ext.iterate(states, function(key, state) {
					if (state.isDefault && state.detail)
						this.masterStateManager.restoreStates(state.detail);
				}, this);
			}
		}
	},

	initDetailToolbar: function(links) {
		if(!Z8.isEmpty(links)) {
			if(links.length == 1)
			{
				this.detailButton = new Z8.Toolbar.TextItem({ text: this.link.text });
			}
			else
			{
				this.detailButton = new Z8.view.DetailButton({ query: this.query, link: this.link });
				this.detailButton.on('detailChanged', this.onDetailChanged, this);
			}
		
			this.detail.detailButton = this.detailButton;
			this.detail.toolbarItems.push(this.detailButton);
		}
		
		if(!this.detail.query.readOnly)
		{	
			if(this.detail.query.canAdd())
			{
				this.detail.toolbarItems.push(this.detail.addButton);
			}
			
			if(this.detail.query.canCopy())
			{
				this.detail.toolbarItems.push(this.detail.copyButton);
			}

			if(this.detail.query.canDelete())
			{
				this.detail.toolbarItems.push(this.detail.deleteButton);
			}
		}
		
		this.detail.toolbarItems.push(this.detail.filesButton, this.detail.filterButton, this.detail.printButton);
		this.detail.toolbarItems.push(this.detail.tableButton, this.detail.formButton/*, this.detail.chartButton */);
		
		if(Z8.user.importEnbled && this.detail.query.canImport())
		{	
			this.detail.toolbarItems.push(this.detail.importButton);
			this.detail.toolbarItems.push(this.detail.importToRecordButton);
		}

		if(links != null && links.length > 1)
		{
			this.detail.toolbarItems.push(this.detail.closeButton);
		}
	},
	
	onImport: function(params)
	{
		var record = this.master.getSelectedRecord();
		params.recordId = record.id;
	},
	
	onCloseDetail: function()
	{
		var doClose = function(saved)
		{
			if(saved)
			{
				this.reloadDetailPanel(false);
			}
		}
			
		var callback = Z8.Callback.create(doClose, this);
		this.save(callback);
	},
	
	onBackwardClick: function(index, rec)
	{
		var link = this.query.backwards[index];
		this.link = link;
		this.reloadDetailPanel(true);
	},

	onCollapseDetail: function(panel)
	{
		var ch = this.detail.toolbar.getHeight();
		
		if ( ! this.detailPanel.minified) {
			this.detailPanel.cachedSize = this.detailPanel.lastSize;
			this.detailPanel.minified = true;
			this.detailPanel.setHeight(ch);
		} else {
			this.detailPanel.minified = false;
			this.detailPanel.setHeight(this.detailPanel.cachedSize.height);
		}
			
		this.doLayout();
	},
	
	loadStore: function(store, options)
	{
		var params = store.baseParams;
		
		if(options.params)
		{
			Ext.apply(params, options.params);
		}
		
		store.load(options);
	},
	
	save: function(callback, silent)
	{
		if(!this.isDirty())
		{
			callback.call(true);
			return;
		}
		
		if(!silent)
		{
			Z8.MessageBox.show(
			{
				title: this.query.text,
				msg: this.query.text + ' содержит несохраненные данные. Сохранить?',

				buttons: Ext.Msg.YESNOCANCEL,
				fn: this.onSaveMessage.createDelegate(this, [callback], true),
				icon: Ext.MessageBox.QUESTION
			});
		}
		else
		{
			this.doSave(callback);
		}
	},
	
	onSaveMessage: function(action, text, msgOptions, callback)
	{
		if(action == 'yes')
		{
			this.doSave(callback);
		}
		else if(action == 'no')
		{
			this.master.rejectChanges();
			if (this.detail)
				this.detail.rejectChanges();
			callback.call(true);
		}
		else
		{
			callback.call(false);
		}
	},
	
	onNotSaved: function()
	{
		this.saveButton.setBusy(false);
		this.refreshButton.setDisabled(false);
	},

	showReportPanel: function()
	{
		if(this.reportPanel.collapsed)
		{
			this.reportPanel.toggleCollapse();
		}
		else
		{
			this.reportPanel.show();
			this.reportPanel.ownerCt.doLayout();
		}
	},		
	
	hideReportPanel: function()
	{
		this.reportPanel.collapse();
	},
	
	onRequired: function(column, record, panel)
	{
		this.reportPanel.onRequired(column, record, panel);
	},

	doSave: function(callback)
	{
		this.saveButton.setBusy(true);
		this.refreshButton.setDisabled(true);
			
		var successFn = function(callback)
		{
			this.saveButton.setBusy(false);
			this.refreshButton.setDisabled(false);
			callback.call(true);
		};

		var errorFn = function()
		{
			this.saveButton.setBusy(false);
			this.refreshButton.setDisabled(false);
			callback.call(false);
		};
			
		var success = new Z8.Callback.create(successFn, this, callback);
		var error = new Z8.Callback.create(errorFn, this, callback);

		if(this.isMasterDirty())
		{
			this.saveMaster(success, error);
		}
		else if(this.isDetailDirty())
		{
			this.saveDetail(success, error);
		}
	},

	saveMaster: function(success, error)
	{
		var store = this.master.getStore();
		this.mon(store, 'write', this.onMasterSaved.createDelegate(this, [success, error], true), this, {single: true});
		this.mon(store, 'error', this.onSaveError.createDelegate(this, [error], true), this, {single: true});
		this.master.save();
	},
	
	onMasterSaved: function(store, action, result, response, records, success, error)
	{
		var refreshData = response.raw != null ? response.raw.refresh : null;

		if(this.isDetailDirty())
		{
			this.saveDetail(success, error, refreshData);
		}
		else
		{
			if(refreshData != null)
			{
				this.onRefresh(this.master, refreshData);
			}
			
			success.call();
		}
	},
	
	saveDetail: function(success, error, refreshData)
	{
		var store = this.detail.getStore();
		this.mon(store, 'write', this.onDetailSaved.createDelegate(this, [success, refreshData], true), this, {single: true});
		this.mon(store, 'error', this.onSaveError.createDelegate(this, [error], true), this, {single: true});
		this.detail.save();
	},
	
	onDetailSaved: function(store, action, result, response, records, callback, refreshData)
	{
		if(refreshData != null)
		{
			this.onRefresh(this.master, refreshData);
		}
		
		refreshData = response.raw != null ? response.raw.refresh : null;

		if(refreshData != null)
		{
			this.onRefresh(this.detail, refreshData);
		}

		callback.call(true);
	},

	onSaveError: function(info, callback)
	{
		callback.call(false);
	},

	onDetailBeforeLoad: function(store, options)
	{
		if(this.isDetailLoading)
		{
			return false;
		}
		
		this.isDetailLoading = true;
		return true;
	},

	onDetailError: function(info)
	{
		this.pendingRecord = null;
		this.isDetailLoading = false;
	},
	
	onDetailAfterLoad: function()
	{
		this.isDetailLoading = false;

		if(this.pendingRecord != null)
		{
			var record = this.pendingRecord;
			this.pendingRecord = null;
			this.reloadDetailStore(record);
		}
	},

	onRefresh: function(view, refreshData)
	{
		this.master.refresh(refreshData);
		
		if(this.detail != null)
		{
			this.detail.refresh(refreshData);
		}
	},
	
	onGoTo: function(queryId)
	{
		Z8.viewport.open(queryId);
	},

	onGoToRecord: function(grid, record, queryId, fieldId)
	{
		var store = grid.getStore();
		var field = store.fields.get(fieldId);
		
		Z8.viewport.open(queryId, { filterBy: record.get(field.linkId) });
	},
	
	onCellAltClick: function(editWith) {
		Z8.viewport.open(editWith);
	},

	onCellLinkClick: function(view, rowIndex, cellIndex) {
		var grid = view.grid;

		var column = grid.getColumnModel().columns[cellIndex];
		var record = grid.getRecord(rowIndex);

		if(column.anchorPolicy == 'custom') {
			var link = {
				fieldId: column.dataIndex,
				value: record.data[column.dataIndex]
			};

			var query = grid.getStore().query;

			if(query.primaryKey != null) {
				link.recordId = record.id;
			}
			else if(query.groups != null) {
				var ids = [];

				for(var i = 0; i < query.groups.length; i++)
					ids.push(record.data[query.groups[i]]);	

				link.groups = ids;
			}

			var callbackFn = function(info) {
				this.onMessage(this.query.text, info);
			};
			var callback = new Z8.Callback.create(callbackFn, this);
			Z8.viewport.followLink(link, grid.getStore().query, callback, callback);
		}
		else
			this.onGoToRecord(grid, grid.getStore().getAt(rowIndex), column.editWith, column.dataIndex);
	}
});