/**
 * Список закрепленных объектов
 */
Z8.desktop.PinnedTasksList = Ext.extend(Z8.List,
{
	checkable: false,
	selectedClass: null,
	btnCls: 'icon-unpin',
	overBtnCls: 'icon-unpin-over',
	
	initComponent: function()
	{
		Z8.desktop.PinnedTasksList.superclass.initComponent.call(this);
		
		this.on('listclick', this.openView, this);
		
		this.store.on('add', this.onAddRecord, this);
		this.store.on('remove', this.onRemoveRecord, this);
	},
	
	openView: function(rec)
	{
		Z8.viewport.open({id: rec.data.id, text: rec.data.name});
	},
	
	onAddRecord: function(store, records, index)
	{		
		var height = this.store.getCount() * 24;
		
		if (this.ownerCt)
		{
			if(this.ownerCt.hidden){
				this.ownerCt.setVisible(true);
			}
			this.ownerCt.setHeight(height + 30);
		}
	},
	
	onRemoveRecord: function(store, record, index)
	{
		var height = this.store.getCount() * 24;
		
		if(this.store.getCount() == 0){
			this.ownerCt.setVisible(false);
		}
		
		this.ownerCt.setHeight(height + 30);
	},
	
	populateData: function()
	{
		var items = [];
		
		Ext.each(this.pinnedItems, function(item, index) {
			items.push([index, item.text, item.objectId, item.pinned, false]);
		}, this);
		
		return items;
	}
});

/**
 * Список запущенных объектов
 */
Z8.desktop.LastTasksList = Ext.extend(Z8.List,
{
	checkable: false,
	selectedClass: null,
	btnCls: 'icon-pin',
	overBtnCls: 'icon-pin-over',
	editable: false,
	
	initComponent: function()
	{
		Z8.desktop.LastTasksList.superclass.initComponent.call(this);
		
		this.store.on('add', this.onAddRecord, this);
		this.store.on('remove', this.onRemoveRecord, this);
		
		this.on('listclick', this.openView, this);
	},
	
	openView: function(rec)
	{
		Z8.viewport.open({id: rec.data.id, text: rec.data.name});
	},
	
	onAddRecord: function(store, records, index)
	{
		if(this.ownerCt)
		{
			if(this.ownerCt.hidden){
				this.ownerCt.setVisible(true);
			}
		}
	},
	
	onRemoveRecord: function(store, record, index)
	{
		var height = this.store.getCount() * 24;
		
		if(this.store.getCount() == 0){
			this.ownerCt.setVisible(false);
		}
		
	},
	
	updateList: function()
	{
		var store = this.getStore();
		
		if(store != null)
		{
			store.removeAll();
			
			var items = this.populateData();
			
			Ext.each(items, function(item, index){
				store.add(new Ext.data.Record({
					index: index, name: item[1], id: item[2], pinned: item[3], is_group: item[4]
				}));
			});
		}
	},
	
	populateData: function()
	{
		var items = [];
		
		Ext.each(this.taskItems, function(item, index) {
			items.push([index, item.text, item.objectId, item.pinned, false]);
		}, this);
		
		return items;
	}
});

Z8.desktop.PinnedTasksPanel = Ext.extend(Ext.Panel,
{
	border: false,
	layout: 'fit',
	layoutConfig: { align: 'stretch' },
	title: 'Закрепленные объекты',

	initComponent: function()
	{
		Z8.desktop.PinnedTasksPanel.superclass.initComponent.call(this);
		this.pinnedList = new Z8.desktop.PinnedTasksList({ pinnedItems: this.pinnedItems });	
		this.add(this.pinnedList);
	}
});

Z8.desktop.LastTasksPanel = Ext.extend(Ext.Panel,
{
	border: false,
	layout: 'fit',
	layoutConfig: { align: 'stretch' },
	title: 'Запущенные объекты',

	initComponent: function()
	{
		Z8.desktop.LastTasksPanel.superclass.initComponent.call(this);
		this.tasksList = new Z8.desktop.LastTasksList({ taskItems: this.taskItems });	
		this.add(this.tasksList);
	}
});

Z8.desktop.LastTasks = Ext.extend(Z8.desktop.BaseMenuItem,
{
	iconCls: 'icon-active-tasks',
	vertical: true,
	menuAlign: 'tr',
	zIndex: 14001,
	totalUnpinned: 10,
	
	tasksCollection: new Ext.util.MixedCollection(),
	pinnedCollection: new Ext.util.MixedCollection(),

	initComponent: function()
	{
		Z8.desktop.Feedback.superclass.initComponent.call(this);
		
		this.taskItems = this.initItems();
		
		var pinnedHeight = this.taskItems.pinned.length * 24;
		var pinnedHidden = (this.taskItems.pinned.length == 0) ? true: false;
		var unpinnedHeight = 350 - pinnedHeight;
		var unpinnedHidden = (this.taskItems.unpinned.length == 0) ? true: false;
		
		/*if (unpinnedHidden && pinnedHeight < 350)
		{
			pinnedHeight = 350;
		}*/
		
		this.pinnedListPanel = new Z8.desktop.PinnedTasksPanel({ hidden: pinnedHidden, pinnedItems: this.taskItems.pinned, height: pinnedHeight + 30, width: 310 });
		this.tasksListPanel = new Z8.desktop.LastTasksPanel({ hidden: unpinnedHidden, taskItems: this.taskItems.unpinned, height: unpinnedHeight, width: 310 });
		
		var items = [this.pinnedListPanel, this.tasksListPanel];
		this.menu = new Z8.menu.MulticolumnMenu({ defaultOffsets: [-1, 0], headerText: this.menuHeaderText, zIndex: 14000, items: items });
		
		Z8.TaskManager.on('activate', this.onTasksActivate, this);
		Z8.TaskManager.on('remove', this.onTasksChanged, this);
		
		this.tasksListPanel.tasksList.on('btnclick', this.onPinClick, this);
		this.pinnedListPanel.pinnedList.on('btnclick', this.onUnPinClick, this);
		this.tasksListPanel.tasksList.on('listclick', this.onOpenView, this);
		this.pinnedListPanel.pinnedList.on('listclick', this.onOpenView, this);
	},
	
	onOpenView: function()
	{
		this.menu.hide();
	},
	
	onPinClick: function(el, rec)
	{
		this.tasksListPanel.tasksList.store.remove(rec);
		this.tasksCollection.removeKey(rec.data.id);
		
		Ext.each(this.settings.lasttasks, function(item, index) {
			if(item.objectId == rec.data.id){
				this.settings.lasttasks[index].pinned = true;
			}
		}, this);
		
		Z8.Ajax.request('settings', this.onPinClickSaved.createDelegate(this, [rec]), Ext.emptyFn, { data: Ext.encode(this.settings) }, this);
	},
	
	onPinClickSaved: function(rec)
	{
		var task = {text: rec.data.name, objectId: rec.data.id, is_group: false, pinned: false};
		this.pinnedListPanel.pinnedList.store.add(rec);
		this.pinnedCollection.add(rec.data.id, task);
	},
	
	onUnPinClick: function(el, rec)
	{
		this.pinnedListPanel.pinnedList.store.remove(rec);
		this.pinnedCollection.removeKey(rec.data.id);
		
		Ext.each(this.settings.lasttasks, function(item, index) {
			if(item.objectId == rec.data.id){
				this.settings.lasttasks[index].pinned = false;
			}
		}, this);
		
		Z8.Ajax.request('settings', this.onUnPinClickSaved.createDelegate(this, [rec]), Ext.emptyFn, { data: Ext.encode(this.settings) }, this);
	},
	
	onUnPinClickSaved: function(rec)
	{
		var task = {text: rec.data.name, objectId: rec.data.id, is_group: false, pinned: false};
		this.tasksCollection.add(rec.data.id, task);
		this.tasksListPanel.tasksList.store.add(rec);
	},
	
	onTasksActivate: function(task, index)
	{
		if (task.query)
		{
			var key = task.query.requestId;
			this.onTasksChanged(index, task, key);
		}
	},
	
	onTasksChanged: function(index, object, key)
	{
		if(object && object.master != null){
			this.addTask(object.titleText, key);
		}
	},
	
	addTask: function(title, key)
	{
		if(!this.tasksCollection.containsKey(key) && !this.pinnedCollection.containsKey(key))
		{
			this.saveAddedTask(title, key);
		}
		else if(this.tasksCollection.containsKey(key))
		{
			this.swapTasks(title, key);
		}
	},
	
	swapTasks: function(title, key)
	{
		var task = {text: title, objectId: key, is_group: false, pinned: false};
		
		Ext.each(this.taskItems.unpinned, function(item, index){
			if(item.objectId == key){
				this.taskItems.unpinned.splice(index, 1);
				this.taskItems.unpinned.splice(0,0,task);
			}
		}, this);
		
		Ext.each(this.settings.lasttasks, function(item, index){
			if(item.objectId == key){
				this.settings.lasttasks.splice(index, 1);
				this.settings.lasttasks.splice(0,0,task);
			}
		}, this);
		
		Z8.Ajax.request('settings', this.updateTaskList, Ext.emptyFn, { data: Ext.encode(this.settings) }, this);
	},
	
	saveAddedTask: function(title, key)
	{
		var task = {text: title, objectId: key, is_group: false, pinned: false};
		
		this.tasksCollection.add(key, task);
		this.taskItems.unpinned.splice(0,0,task);
		this.settings.lasttasks.splice(0,0,task);
		
		if(this.taskItems.unpinned.length > this.totalUnpinned)
		{
			Ext.each(this.taskItems.unpinned, function(task, index){
				if(index >= this.totalUnpinned)
				{
					if (task)
					{
						Ext.each(this.settings.lasttasks, function(lasttask, i){
							if (task == lasttask)
							{
								this.settings.lasttasks.splice(i, 1);
							}
						}, this);
						
						this.tasksCollection.removeKey(task.objectId);
						this.taskItems.unpinned.splice(index, 1);
					}
				}
			}, this);
		}

		Z8.Ajax.request('settings', this.updateTaskList, Ext.emptyFn, { data: Ext.encode(this.settings) }, this);
	},
	
	updateTaskList: function()
	{
		this.tasksListPanel.tasksList.updateList();
	},
	
	initItems: function()
	{
		var pinned = [];
		var unpinned = [];
		
		this.settings = Z8.getUserSettings();
		
		var disabled = [];
		
		if (this.settings.components)
		{
			Ext.iterate(this.settings.components, function(key, component){
				if (component.items)
				{
					Ext.iterate(component.items, function(ikey, item){
						if(item.state == false)
						{
							disabled.push(ikey);
						}
					});
				}
			});
		}
		
		if (this.settings.lasttasks != undefined)
		{
			Ext.each(this.settings.lasttasks, function(item, index) {
				
				if (disabled.indexOf(item.objectId) === -1)
				{
					if(item.pinned){
						this.pinnedCollection.add(item.objectId, item);
						pinned.push(item);
					}else{
						this.tasksCollection.add(item.objectId, item);
						unpinned.push(item);
					}
				}
				
			}, this);
			
			return {pinned: pinned, unpinned: unpinned};
		}
		else
		{
			this.settings.lasttasks = [];
			return {pinned: [], unpinned: []};
		}
	},
	
	onOpenView: function()
	{
		this.hideMenu();
	}
});