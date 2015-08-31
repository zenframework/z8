Z8.view.UserMenuList = Ext.extend(Z8.List, {

	current: null,
	draggable: true,
	droppable: true,
	ddReorder: true,
	appendOnly:false,
	dragGroup: 'UserComponentsListDD',
	dropGroup: ['UserComponentsListDD', 'UserComponentsSubListDD'],
	
	initComponent:function() {
		Z8.view.UserMenuList.superclass.initComponent.apply(this, arguments);
		
		this.addEvents('currentchange');
		
		this.on('itemedit', this.onItemEdit, this);
		this.store.on('update', this.onUpdateStore, this);
		this.store.on('add', this.onAddStore, this);
		this.on('drop', this.onDropItem, this);
		this.on('selectionchange', this.onSelectionChange, this);
		this.on('checkclick', this.onCheckClick, this);
	},
	
	onSelectionChange: function()
	{
		var selNode = this.getSelectedNodes();
		
		if (selNode[0])
		{
			var rec = this.getRecord(selNode[0]);
			this.current = rec.data.id;
			
			this.fireEvent('currentchange', this.current);
		}
	},
	
	populateData: function()
	{	
		var menuItems = Z8.viewport.applyMenuBarItemsSettings(this.settings)
		var items = [];
		
		Ext.each(menuItems, function(item, index) {
			items.push([index, item.text, item.key, item.state]);
		}, this);	
		
		return items;
	},
	
	onDropItem: function(dropzone, n, dd, e, data)
	{
		var targetNode = this.getRecord(n);
		var droppedNodes = '';
		Ext.each(data.records, function(record, index){
			droppedNodes += '"' + record.data.name + '"';
			if(index != data.records.length-1)
				droppedNodes += ','
		});
		
		Z8.MessageBox.show({
			title : 'Перемещение пунктов меню',
			msg : 'Перемещение пунктов ' + droppedNodes + ' в группу "' + targetNode.data.name + '"',
			buttons : {yes : 'Переместить', no : 'Копировать', cancel : 'Отмена'},
			icon : Ext.MessageBox.QUESTION,
			scope: this,
			fn : function(btn) {
				if(btn == 'yes'){
					this.processDrop(data, n, 'tansfer');
				}else if(btn == 'no'){
					this.processDrop(data, n, 'copy');
				}
			}
		}, this);
	},
	
	processDrop: function(data, n, command)
	{
		if (data.sourceView !== this)
        {
			var id = this.getRecord(n).data.id;
			
			var re = /^new-/;
			if (re.test(id)){
				this.onDropLoaded(null, data, id, command);
			}else{
				Z8.Ajax.request(Z8.viewport.loginInfo.requestId, this.onDropLoaded.createDelegate(this, [data, id, command], true), Ext.emptyFn, { menu: id }, this);
			}
        }
	},
	
	onDropLoaded: function(result, data, id, command)
	{
		var applyed = [];
		
		Ext.each(data.records, function(record, index){
			applyed.push(record.data);
		});
		
		this.onItemsApplyed(id, applyed, command);
	},
	
	onItemsApplyed: function(id, items, command)
	{
		if (! this.settings.components[id])
			this.settings.components[id] = {};
		
		if (! this.settings.components[this.current])
			this.settings.components[this.current] = {};
		
		// Массив удаляемых объектов для текущего пункта меню
		var removed = [];
		
		Ext.each(items, function(item, index){
			if (item.index !== null){
				removed.push(item.id);
			}else{
				removed.push(item.name);
			}
		});
		
		// Массив добавляемых объектов для меню
		var applyed = [];
		
		Ext.each(items, function(item, index){
			if (item.index !== null){
				applyed.push({item: item});
			}else{
				applyed.push({block: item.name, value: item.value});
			}
		});
		
		
		// Если добавляемые объекты уже содержаться в пункте как удаленные
		if (this.settings.components[this.current].applyed)
		{
			Ext.each(removed, function(item, index){
				Ext.each(this.settings.components[this.current].applyed, function(applyedItem, applyedIndex){
					if (applyedItem.item !== undefined) {
						if (applyedItem.item.id == item){
							removed.splice(index, 1);
						}
					}
					else if (applyedItem.block !== undefined) {
						if (applyedItem.block == item){
							removed.splice(applyedIndex, 1);
						}
					}
				}, this);
			}, this);
		}
		
		// Если удаляемые объекты из текущего меню были добавлены перетаскиванием
		// то мы их удаляем
		if (this.settings.components[this.current].applyed)
		{
			Ext.each(applyed, function(item, index){
				Ext.each(this.settings.components[this.current].applyed, function(applyedItem, applyedIndex){
					if (applyedItem)
					{
						if (applyedItem.item !== undefined && item.item !== undefined) {
							if (applyedItem.item.id == item.item.id){
								this.settings.components[this.current].applyed.splice(applyedIndex, 1);
							}
						}
						else if (applyedItem.block !== undefined && item.block !== undefined) {
							if (applyedItem.block == item.block){
								this.settings.components[this.current].applyed.splice(applyedIndex, 1);
							}
						}
					}
				}, this);
			}, this);
		}
		
		if (this.settings.components[id].removed)
		{
			Ext.each(applyed, function(item, index){
				Ext.each(this.settings.components[id].removed, function(removedItem, removedIndex){
					
					if (item.item !== undefined) {
						if (removedItem == item.item.id){
							this.settings.components[id].removed.splice(removedIndex, 1);
							applyed.splice(index, 1);
						}
					}
					else if (item.block !== undefined) {
						if (removedItem == item.block){
							this.settings.components[id].removed.splice(removedIndex, 1);
							applyed.splice(index, 1);
						}
					}
					
				}, this);
			}, this);
		}
		
		if (this.settings.components[id].removed)
		{
			Ext.each(removed, function(item, index){
				Ext.each(this.settings.components[id].removed, function(removedItem, removedIndex){
					if (item == removedItem){
						this.settings.components[id].removed.splice(removedIndex, 1);
					}
				}, this);
			}, this);
		}
		
		if(command == 'transfer')
		{
			// Удаляемые объекты настроек текущего меню
			if (this.settings.components[this.current].removed)
			{
				var oldRemoved = this.settings.components[this.current].removed;
				this.settings.components[this.current].removed = oldRemoved.concat(removed);
			} else {
				this.settings.components[this.current].removed = removed;
			}
		}
		
		
		// Добавляемые объекты нового меню
		if (this.settings.components[id].applyed)
		{
			var oldApplyed = this.settings.components[id].applyed;
			this.settings.components[id].applyed = oldApplyed.concat(applyed);
		} else {
			this.settings.components[id].applyed = applyed;
		}
	},
	
	onAddStore: function(store, records, index)
	{
		var added = this.settings.components.added || [];
		
		Ext.each(records, function(record, index){
			added.push(record.data);
		});
		
		this.settings.components.added = added;
	},
	
	onUpdateStore: function(store, record, operation)
	{
		if (operation == 'commit')
		{
			var items = [];
			var records = store.data;
			
			records.each(function(record, index){
				items.push(record.data.id);
			});
			
			this.settings.components.itemsorder = items;
		}
	},
	
	onItemEdit: function(rec)
	{
		var id = rec.data.id;
		
		if (!this.settings.components[id])
			this.settings.components[id] = {};
		
		this.settings.components[id].newname = rec.get('name');
	},
	
	onCheckClick: function(check, rec)
	{
		var id = rec.data.id;
		
		if (!this.settings.components[id])
			this.settings.components[id] = {};
		
		this.settings.components[id].state = check;
	}
	
});

Z8.view.UserMenuPanel = Ext.extend(Z8.Panel, {
	
	border: false,
	header: false,
	layout: 'fit',
	
	initComponent: function()
	{
		this.menuList = new Z8.view.UserMenuList({settings: this.settings});
		
//		this.addBtn = new Z8.Button({ iconCls: 'icon-add', align: 'right', tooltip:'Добавить', text: '', handler: this.onAddClick, scope: this });
//		this.delBtn = new Z8.Button({ iconCls: 'icon-delete',  align: 'right', disabled:true, tooltip:'Удалить', text: '', handler: this.onDeleteClick, scope: this });
		
		this.menuList.on('selectionchange', this.onSelectionChange, this);
		
		var config = {
			items: [this.menuList]/*,
			tbar: new Z8.Toolbar({ cls: 'z8-toolbar', items: [this.addBtn, this.delBtn] })*/
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.UserMenuPanel.superclass.initComponent.apply(this, arguments);
	},
	
	onSelectionChange: function()
	{
//		this.delBtn.enable();
	},
	
	
	onDeleteClick: function()
	{
		var added = this.settings.components.added;
		if(added)
		{
			var deleted = this.menuList.getSelectedRecords(); 
			var store = this.menuList.getStore();
			
			Ext.each(added, function(add, index){
				Ext.each(deleted, function(del){
					if(add.id == del.data.id){
						added.splice(index, 1);
						store.remove(del);
						
						this.settings.components[del.data.id].added = [];
					}
				}, this);
			}, this);
			
			this.settings.components.added = added;
		}
	},

	onAddClick: function()
	{
		var store = this.menuList.getStore();
			
		store.add(new store.recordType({
			index: store.getCount(), 
			name: 'Новая запись',
			id: 'new-' + new Ext.ux.UUID().id,
			check: true
		}));
	}
});


Z8.view.UserMenuItemsList = Ext.extend(Z8.List, {
	
	current: null,
	draggable: true,
	droppable: true,
	ddReorder: true,
	appendOnly: false,
	dragGroup: 'UserComponentsSubListDD',
	dropGroup: 'UserComponentsSubListDD',
	
	initComponent:function() {
		Z8.view.UserMenuList.superclass.initComponent.apply(this, arguments);
		
		this.store.on('update', this.onUpdateStore, this);
		this.store.on('add', this.onAddStore, this);
		
		this.on('itemedit', this.onItemEdit, this);
		this.on('checkclick', this.onCheckClick, this);
	},
	
	buildFields: function()
	{
    	return [{name: 'index'}, {name: 'name'}, {name: 'id'}, {name: 'check'}, {name: 'items'}, {name: 'itemobj'}];
	},
	
	loadItems: function(id)
	{
		var re = /^new-/;
		
		if (re.test(id)){
			this.onLoadAddedItems(id);
		}else{
			Z8.Ajax.request(Z8.viewport.loginInfo.requestId, this.onLoadItems.createDelegate(this, [id], true), Ext.EmptyFn, { menu: id }, this);
		}
	},
	
	onLoadAddedItems: function(id)
	{
		var items = [];
		
		if (this.settings.components[id])
		{
			var data = this.settings.components[id].added;
			var applyed = this.settings.components[id].applyed;
			
			if(data || applyed)
			{
				var menu = new Z8.desktop.MenuItem({menuContent: data});
				var menuItems = menu.generateAddedMenuItems(id, this.settings);
				
				Ext.each(menuItems, function(item, index){
					if(item.type == 'block'){
						items.push([null, item.text, null, null, null]);
					}else{
						items.push([index, item.text, item.key, item.state, null]);
					}
				});
			}
		}
		
		this.store.loadData(items, false);
	},
	
	onLoadItems: function(result, id)
	{
		var menu = new Z8.desktop.MenuItem({menuContent: result.data});
		var menuItems = menu.generateMenuItems(id, this.settings);
		var items = [];
		
		Ext.each(menuItems, function(item, index){
			if(item.type == 'block'){
				items.push([null, item.text, null, null, null]);
			}else{
				items.push([index, item.text, item.key, item.state, null]);
			}
		});
		
		this.store.loadData(items, false);
	},
	
	onAddStore: function(store, records, index)
	{
		if (!this.settings.components[this.current])
			this.settings.components[this.current] = {};
		
		var added = this.settings.components[this.current].added || [];
		
		Ext.each(records, function(record, index){
			added.push(record.data);
		});
		
		this.settings.components[this.current].added = added;
	},
	
	onUpdateStore: function(store, record, operation)
	{
		if (operation == 'commit')
		{
			var items = [];
			var records = store.data;
			
			records.each(function(record, index){
				items.push(record.data.id || record.data.name);
			});
			
			if (!this.settings.components[this.current])
				this.settings.components[this.current] = {};
			
			this.settings.components[this.current].itemsorder = items;
		}
	},
	
	onItemEdit: function(rec)
	{
		var id = rec.data.id || rec.data.name;

		this.checkSettings(id);
		this.settings.components[this.current].items[id].newname = rec.get('name');
	},
	
	onCheckClick: function(check, rec)
	{
		var id = rec.data.id || rec.data.name;
		this.checkSettings(id);
		this.settings.components[this.current].items[id].state = check;
	},
	
	checkSettings: function(id)
	{
		if (!this.settings.components[this.current])
			this.settings.components[this.current] = {};
		
		if (!this.settings.components[this.current].items)
			this.settings.components[this.current].items = {};
		
		if (!this.settings.components[this.current].items[id])
			this.settings.components[this.current].items[id] = {};
	}
	
});

Z8.view.UserMenuItemsPanel = Ext.extend(Z8.Panel, {
	
	border: false,
	header: false,
	layout: 'fit',
	
	initComponent: function()
	{
//		this.addBtn = new Z8.Button({ iconCls: 'icon-add', align: 'right', tooltip:'Добавить', text: '', handler: this.onAddClick, scope: this });
//		this.delBtn = new Z8.Button({ iconCls: 'icon-delete',  align: 'right', disabled:true, tooltip:'Удалить', text: '', handler: this.onDeleteClick, scope: this });
	
		this.menuList = new Z8.view.UserMenuItemsList({settings: this.settings});
		
		this.menuList.on('selectionchange', this.onSelectionChange, this);
		
		var config = {
/*			tbar: new Z8.Toolbar({ cls: 'z8-toolbar', items: [this.addBtn, this.delBtn] }), */
			items: [this.menuList]
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.UserMenuItemsPanel.superclass.initComponent.apply(this, arguments);
	},
	
	onSelectionChange: function()
	{
//		this.delBtn.enable();
	},
	
	onDeleteClick: function()
	{
		var added = this.settings.components[this.menuList.current].added;
		if(added)
		{
			var deleted = this.menuList.getSelectedRecords();
			var store = this.menuList.getStore();
			
			Ext.each(added, function(add, index){
				Ext.each(deleted, function(del){
					if(add.id == del.data.id){
						added.splice(index, 1);
						store.remove(del);
					}
				});
			});
			
			this.settings.components[this.menuList.current].added = added;
		}
	},
	
	onAddClick: function()
	{
		var selectList = new Z8.desktop.ComponentsAccordion();
		selectList.on('selected', this.onAddSelect, this);
		
		var w = new Z8.Window({
			width: 600,
			height: 400,
			modal: true,
			autoScroll: true,
			title: 'Выберите объект для добавления в пункт меню',
			items: selectList
		});
		
		w.show();
	},
	
	onAddSelect: function(response)
	{
		var store = this.menuList.getStore();
		
		store.add(new store.recordType({
			index: store.getCount(), 
			name: response.text,
			id: response.requestId,
			check: true
		}));
	}
});

Z8.view.UserComponents = Ext.extend(Z8.Panel, {
	
	current: null,
	header: false,
	
	initComponent: function()
	{
		if (!this.settings.components) 
			this.settings.components = {};
		
		this.leftPanel = new Z8.view.UserMenuPanel({split: true, region: 'west', layout: 'fit', border: true, width: '200', settings: this.settings});
		this.rightPanel = new Z8.view.UserMenuItemsPanel({region: 'center', layout: 'fit', border: true, settings: this.settings});

		var config = {layout: 'border', bodyStyle: 'border-width: 0px;', items: [this.leftPanel, this.rightPanel]};
		
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.UserComponents.superclass.initComponent.apply(this, arguments);
		
		this.leftPanel.menuList.on('listclick', this.onLeftPanelClick, this);
		this.leftPanel.menuList.on('currentchange', this.onCurrentChange, this);
	},
	
	onCurrentChange: function(current)
	{
		this.rightPanel.menuList.current = current;
	},
	
	onLeftPanelClick: function(rec)
	{
		this.rightPanel.menuList.loadItems(rec.data.id);
	},
	
	reset: function()
	{
		if(this.settings.componentsorder){
			delete this.settings.componentsorder;
		}
		
		if(this.settings.components){
			this.settings.components = {};
		}
		
		this.leftPanel.menuList.store.loadData(this.leftPanel.menuList.populateData());
		this.rightPanel.menuList.store.loadData([]);
	}
});

Z8.view.UserSettings = Ext.extend(Z8.Window, {
	
	title: 'Настройки',
	width: 700,
	height: 400,
	
	initComponent: function()
	{	
		this.settings = Z8.getUserSettings();
		
		this.userComponents = new Z8.view.UserComponents({settings: this.settings});
		this.userCommon = new Z8.view.UserCommon({settings: this.settings});
	
		this.tabpanel = new Ext.ux.GroupTabPanel({
			tabWidth: 200,
    		activeGroup: 0,
    		items: [/*{ expanded: true, items: { title: 'Настройки пользователя', style: 'padding: 10px;' }},*/
    		        { expanded: true, items: { title: 'Настройки меню', style: 'padding: 10px;', layout: 'fit', items: this.userComponents }},
    		        { expanded: true, items: { title: 'Настройки отображения', style: 'padding: 10px;', items: this.userCommon }}
    		       ]
		});
		
		this.saveButton = new Z8.Button({
			tooltip: 'Сохранение настроек пользователя',
//			iconCls: 'icon-save',
//			iconAlign: 'top',
			plain: true,
			text: 'Сохранить',
			scope: this,
			handler: this.onSave
		});
		
		this.resetButton = new Z8.Button({
			tooltip: 'Сброс настроек пользователя',
//			iconCls: 'icon-reset',
//			iconAlign: 'top',
			plain: true,
			text: 'По умолчанию',
			scope: this,
			handler: this.onReset
		});
		
		// create config object
		var config = {
			layout: 'fit',
			items:[this.tabpanel],
			buttons: [this.resetButton, this.saveButton]
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.UserSettings.superclass.initComponent.call(this);
	},
	
	onSave: function()
	{
		this.updateMenuNames(this.settings.components);
		Z8.Ajax.request('settings', Ext.emptyFn, Ext.emptyFn, { data: Ext.encode(this.settings) }, this);
	},
	
	onReset: function()
	{
		this.userComponents.reset();
		this.userCommon.reset();
		
		this.updateMenuNames(this.settings);

		Z8.Ajax.request('settings', Ext.emptyFn, Ext.emptyFn, { data: Ext.encode(this.settings) }, this);
	},
	
	onClose: function()
	{
		this.hide();
	},
	
	updateMenuNames: function(components)
	{
		var menuItems = Z8.viewport.menuBar.items;
		menuItems.each(function(item){
			if(item.component)
			{
				if (components)
				{
					var cmp = components[item.component.id];
					
					if(cmp)
					{
						if(cmp.newname)
						{
							item.component.name = cmp.newname;
							item.setText(cmp.newname);
						}
						
						if (cmp.state == false)
						{
							if (item.isVisible()){
								item.hide();
							}
						}
						else
						{
							if (!item.isVisible()){
								item.show();
							}
						}
					}
				}
				
				var subMenuItems = item.menu.items;
				
				if(subMenuItems)
				{
					item.forceUpdate = true;
					item.onBeforeShowMenu(item.menu, true);
				}
			}
		});
		
		Z8.viewport.menuBar.doLayout();
	}
});

Z8.view.UserCommon = Ext.extend(Z8.List, {
	
	editable: false,
	
	initComponent:function() {
		Z8.view.UserCommon.superclass.initComponent.apply(this, arguments);
		this.on('checkclick', this.onCheckClick, this);
	},
		
	populateData: function()
	{	
		var items = [];
		items.push([0, 'Показывать окно "Приступая к работе"', 'showhelper', this.settings.showhelper]);
		items.push([1, 'Отображать "Отображение бизнесс-процессов" при загрузке', 'showshortcuts', this.settings.showshortcuts]);

		return items;
	},
	
	onCheckClick: function(state, rec)
	{
		this.settings[rec.data.id] = state;
	},
	
	reset: function()
	{
		this.settings.showhelper = true;
	}
});