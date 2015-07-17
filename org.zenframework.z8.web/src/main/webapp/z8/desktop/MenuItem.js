Z8.desktop.BaseMenuItem = Ext.extend(Z8.Button,
{
	iconAlign: 'left',
	
	initComponent: function()
	{
		Z8.desktop.BaseMenuItem.superclass.initComponent.call(this);
	},
	
	getTemplateArgs: function()
	{
		var args = Z8.desktop.BaseMenuItem.superclass.getTemplateArgs.call(this);

		if(this.zIndex != null)
		{
			args.style = 'z-index: ' + this.zIndex;
		}
		 
		return args;
    },
	
	getZIndex: function()
	{
		return this.zIndex != null ? this.zIndex - 1 : (Ext.WindowMgr.zseed - 100); 
	},
	
	getMenuZIndex: function()
	{
		return this.getZIndex() - 1; 
	}

});

Z8.desktop.MenuItem = Ext.extend(Z8.desktop.BaseMenuItem,
{
	component: null,
	menuContent: null,
	menuQueries: null,
	forceUpdate: true,
	
	initComponent: function()
	{
		Z8.desktop.MenuItem.superclass.initComponent.call(this);

		if(this.component)
		{
			var text = Ext.util.Format.ellipsis(this.component.text, 40, true);
			this.setText(text);
		}
		
		this.setIconClass('silk-empty');
		
		this.menu = new Z8.menu.MulticolumnMenu();
		this.menu.on('beforeshow', this.onBeforeShowMenu, this);
		
		this.on('mouseover', this.onButtonMouseOver, this);
		this.on('mouseout', this.onButtonMouseOut, this);
	},
	
	onBeforeShowMenu: function(menu, dontShow)
	{
		if(! this.menuContent || this.forceUpdate)
		{
			var re = /^new-/;
			if(re.test(this.component.id))
			{
				var settings = Z8.getUserSettings();
				var result = {};
				var items = [];
				var data = settings.components[this.component.id].added;
				var applyed = settings.components[this.component.id].applyed;
				
				Ext.each(data, function(item){
					items.push({id: item.id, text: item.name});
				});
				
				result.data = settings.components[this.component.id].added;

				if(data || applyed){
					this.onMenuLoaded(result, this.component.id, dontShow);
					this.forceUpdate = false;
					return false;
				}
			}
			else
			{
				this.setIconClass('silk-loading');
				Z8.Ajax.request(Z8.viewport.loginInfo.requestId, this.onMenuLoaded.createDelegate(this, [this.component.id, dontShow], true), this.onMenuLoadException, { menu: this.component.id }, this);
				this.forceUpdate = false;
				return false;
			}
		}
		
		return true;
	},
	
	onMenuLoaded: function(result, componentId, dontShow)
	{
		this.menuContent = result.data;

		var parentMenu = this.menu.parentMenu;
		var ownerCt = this.menu.ownerCt;
		
		this.menu.destroy();
		
		this.menu = new Z8.menu.MulticolumnMenu({ items: this.createMenuItems(componentId) });
		this.menu.parentMenu = parentMenu;
		this.menu.ownerCt = ownerCt;
		
		this.menu.on('itemclick', this.onItemClick, this);
		this.menu.on('show', this.onMenuShow, this);
		this.menu.on('hide', this.onMenuHide, this);
		
		this.setIconClass('silk-empty');
		
		if(parentMenu != null)
		{
			ownerCt.menu = this.menu;
			if (!dontShow)
				this.menu.show(ownerCt.container, parentMenu.subMenuAlign || 'tl-tr?', parentMenu);
		}
		else
		{
			if (!dontShow)
				this.showMenu();
		}
		
		this.forceUpdate = false;
	},
	
	generateMenuItems: function(componentId, settings)
	{
		this.menuQueries = [];
		var menuItems = [];
		var pBlock;
		
		Ext.iterate(this.menuContent, function(block, items){
			
			if(block != '')
			{
				pBlock = block;
				menuItems.push({type: 'block', key: block, value: block, text: block, state: true});
			} else {
				pBlock = null;
			}
			
			Ext.each(items, function(item, index){
				menuItems.push({type: 'item', key: item.id, value: item, text: item.text, parent: pBlock, state: true});
			});
		});
		
		menuItems = this.applyMenuSettings(menuItems, componentId, pBlock, settings);
		
		return menuItems;
	},
	
	generateAddedMenuItems: function(componentId, settings)
	{
		this.menuQueries = [];
		var menuItems = [];
		
		Ext.iterate(this.menuContent, function(item, index){
			menuItems.push({type: 'item', key: item.id, value: item, text: item.name, parent: null, state: true});
		});
		
		menuItems = this.applyMenuSettings(menuItems, componentId, null, settings);
		
		return menuItems;
	},
	
	applyMenuSettings: function(menuItems, componentId, pBlock, settings)
	{
		var menuSettings = null;
		
		if(settings.components != null) {
			menuSettings = settings.components[componentId];
		}
		
		if(menuSettings == null) {
			menuSettings = { items: [] };
		}
		
		var removed = menuSettings.removed;
		if(removed)
		{
			Ext.each(menuItems, function(item, index) {
				
				if (item !== undefined)
				{
					if(removed.indexOf(item.key) !== -1) {
						menuItems.splice(index, 1);
					}
				}
			});
		}
		
		var applyed = menuSettings.applyed;
		
		if (applyed)
		{
			Ext.each(applyed, function(item, index){
				if (item.block){
					pBlock = item.block;
					menuItems.push({type: 'block', key: item.block, value: item.block, text: item.block, state: true});
				} else {
					menuItems.push({type: 'item', key: item.item.id, value: item.item, text: item.item.name, parent: pBlock, state: true});
				}
			}, this);
		}
		
		Ext.each(menuItems, function(item, index){
			
			if (menuSettings.items)
			{
				var setted = menuSettings.items[item.key];
				if (setted)
				{
					item.state = setted.state == true ? true : false;
					item.text = setted.newname || item.value.text;
				}
			}
		});	
		
		var itemsorder = menuSettings.itemsorder;
		
		if (itemsorder)
		{
			var orderedItems = [];
			var nonOrderedItems = [];
			
			Ext.each(menuItems, function(item, index){
				var cindex = itemsorder.indexOf(item.key);
				
				if(cindex !== -1){
					orderedItems[cindex] = item;
				} else {
					nonOrderedItems.push(item);
				}
			});
			
			Ext.each(orderedItems, function(item, index){
				if (item === undefined) {
					orderedItems.splice(index, 1);
				}
			});
			
			Ext.each(nonOrderedItems, function(item, index){
				if(item.type == 'block'){
					orderedItems.push(item);
				}else
				{
					var cindex = itemsorder.indexOf(item.parent);
					if(cindex !== -1) {
						orderedItems.splice(cindex+1, 0, item);
					}else{
						orderedItems.push(item);
					}
				}
			});
			
			menuItems = orderedItems;
		}
		
		return menuItems;
	},
	
	createMenuItems: function(componentId)
	{
		var settings = Z8.getUserSettings();
		var menuItems;
		
		var re = /^new-/;
		if(re.test(componentId)){
			menuItems = this.generateAddedMenuItems(componentId, settings);
		}else{
			menuItems = this.generateMenuItems(componentId, settings);
		}
		
			
		var items = [];
		
		Ext.each(menuItems, function(item, index){
			if (item !== undefined)
			{
				if (item.state === true)
				{
					var text = Ext.util.Format.ellipsis(item.text, 40, true);

					if (item.type == 'block')
					{
						items.push(new Ext.menu.TextItem({ text: text }));
						items.push(new Ext.menu.Separator());
					}
					else
					{
						var menuItem = new Ext.menu.Item({
							text: text,
							task: item.value,
							qtitle: item.text,
							qtip: item.value.description,
							listeners: {
								afterrender: function( thisMenuItem ) { 
									if (thisMenuItem.initialConfig.qtip) {
										new Ext.ToolTip({
											target: thisMenuItem.getEl().getAttribute('id'),
											anchor: 'right',
											html: thisMenuItem.initialConfig.qtip
										});
									}
								}
							}
						});
						items.push(menuItem);
						
						this.menuQueries.push(item.key);
					}
				}
			}
		}, this);
		
		return items;
	},
	
	onMenuLoadException: function()
	{
		this.setIconClass('silk-empty');
	},
	
	onItemClick: function(menuItem, event)
	{
		if(menuItem.task != null)
		{
			Z8.viewport.open(menuItem.task);
		}
	},
	
	onButtonMouseOver: function(button, event)
	{
		this.mouseIsOver = true;
	},
	
	onButtonMouseOut: function(button, event)
	{
		this.mouseIsOver = false;
	}
});