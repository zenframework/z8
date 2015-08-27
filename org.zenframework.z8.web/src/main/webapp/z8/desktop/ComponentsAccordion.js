Z8.desktop.AccordionMenu = Ext.extend(Ext.Panel, {
	
	portalPanel: null,
	
	initComponent: function() {
	
		Ext.applyIf(this, {
			autoHeight: true,
			layout: 'anchor',
			items: {
				xtype: 'menu',
				floating:false,
				autoWidth:false
			},
			listeners: {
				'expand': {
					fn: function() {
						if(this.menuContent == null) {
							this.setIconClass('silk-loading');
							Z8.Ajax.request(Z8.viewport.loginInfo.requestId, this.onMenuLoaded, Ext.emptyFn, { menu: this.id }, this);
							return false;
						}
						return true;
					}
				}
			}
		});
	
		Z8.desktop.AccordionMenu.superclass.initComponent.call(this);
	},
	
	onMenuLoaded: function(result) {

		this.menuContent = result.data;
		var menuItems = [];

		Ext.iterate(this.menuContent, function(block, value) {	
			
			if(block != '') {
				menuItems.push(new Ext.menu.TextItem({ text: block }));
				menuItems.push(new Ext.menu.Separator());
			}
			
			var items = this.menuContent[block];
			
			Ext.each(items, function(item, index) {
				if ( ! item.isJob) {
					var menuItem = new Ext.menu.Item({
						text: item.text,
						scope: this,
						handler : this.onMenuItemClick.createDelegate(this, [item])
					});
					menuItems.push(menuItem);
				}
			}, this);
			
		}, this);
		
		this.items.get(0).add(menuItems);
		
		this.setIconClass('silk-empty');
	},
	
	onMenuItemClick: function(task) {
		
		var title, msg;
		
		title = this.portalPanel ? 'Добавление объекта на рабочий стол' : 'Добавление объекта';
		msg = this.portalPanel ? 'Добавить выбранный объект "' + task.text + '" на рабочий стол?' : 'Добавить выбранный объект "' + task.text + '"?';
		
		Z8.MessageBox.confirm(title, msg, function(btn) {
			if(btn == 'yes'){
				var id = Ext.isString(task) ? task : task.id;
				if(task.parameters != null) {}
				this.onParameters({}, id);
			}
		}, this);
	},
	
	onParameters: function(parameters, id) {
		Z8.Ajax.request(id, this.onAddPanel, Ext.emptyFn, { parameters: Ext.encode(parameters) }, this);
    },
    
    onAddPanel: function(response) {
		
    	if (this.portalPanel) {
    		this.portalPanel.onAddItem(response, true);
    	}
    	
    	if (this.ownerCt.ownerCt !== undefined)
    	{
    		this.ownerCt.fireEvent('selected', response);
    		this.ownerCt.ownerCt.close();
    	}
    }
});

Z8.desktop.ComponentsAccordion = Ext.extend(Ext.Panel, {
	
	layout: 'accordion',
	//margins: '0 0 0 5',
	layoutConfig: { animate: true },
	defaults: { collapsed:true },
	border: false,
	autoDestroy: false,
	
	initComponent: function()
	{
		Z8.desktop.ComponentsAccordion.superclass.initComponent.call(this);
		
		this.addEvents('selected');
        
		Z8.componentsAccordion = this;
		
		Ext.each(Z8.componentsList, function(component, index) {
			this.add(this.addMenu(component));
		}, this);
	},
	
	addMenu: function(component) {
		return new Z8.desktop.AccordionMenu({
			title: component.text,
			id: component.id
		});
	}
});