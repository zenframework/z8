Z8.layout.HBoxLayout = Ext.extend(Ext.layout.HBoxLayout,
{
	calculateChildBoxes: function(visibleItems, targetSize)
	{
		var result = Z8.layout.HBoxLayout.superclass.calculateChildBoxes.call(this, visibleItems, targetSize);
		return result;
	},

	updateChildBoxes: function(boxes)
	{
		for(var i = 0, length = boxes.length; i < length; i++)
		{
			var box  = boxes[i], comp = box.component;

            if(box.dirtySize)
            {
            	if(comp.flex != null)
            	{
					comp.setWidth(box.width);
            	}
	            comp.setHeight(box.height);
			}
            
			if(!isNaN(box.left) && !isNaN(box.top))
			{
				comp.setPosition(box.left, box.top);
            }
        }
    }
});

Ext.Container.LAYOUTS.hbox = Z8.layout.HBoxLayout;

Z8.layout.VBoxLayout = Ext.extend(Ext.layout.VBoxLayout,
{
	calculateChildBoxes: function(visibleItems, targetSize)
	{
		var result = Z8.layout.VBoxLayout.superclass.calculateChildBoxes.call(this, visibleItems, targetSize);

		for(var i = 0; i < result.boxes.length; i++)
		{
			var box = result.boxes[i];
			box.height = box.component.flex != null ? box.height : undefined;
		}

		return result;
	}
});

Ext.Container.LAYOUTS.vbox = Z8.layout.VBoxLayout;

Ext.layout.boxOverflow.Menu = Ext.extend(Ext.layout.boxOverflow.None,
{
	triggerWidth: 16,
	noItemsMenuText : '<div class="x-toolbar-no-items">(None)</div>',

	constructor: function(layout)
	{
		Ext.layout.boxOverflow.Menu.superclass.constructor.apply(this, arguments);
		this.menuItems = [];
	},

	clearOverflow: function(calculations, targetSize)
	{
		var newWidth = targetSize.width;
		var items = this.menuItems;

		this.hideTrigger();

		for (var index = 0, length = items.length; index < length; index++)
		{
			items.pop().show();
		}

		return {targetSize: { height: targetSize.height, width : newWidth }};
	},

	showTrigger: function()
	{
		this.createMenu();
		this.triggerButton.show();
	},

	hideTrigger: function()
	{
		if (this.triggerButton != null)
		{
			this.triggerButton.hide();
		}
	},

	beforeMenuShow: function(menu)
	{
		var items = this.menuItems;

		var needsSep = function(group, item)
		{
			return group.isXType('buttongroup') && !(item instanceof Ext.Toolbar.Separator);
		};

		this.clearMenu(menu);

		var prev = null;
		
		for(var i = 0; i < items.length; i++) 
		{
			var item = items[i];

			if(prev && (needsSep(item, prev) || needsSep(prev, item)))
			{
				menu.add('-');
			}

			this.addComponentToMenu(menu, item);
			prev = item;
		}

		if(menu.items.length < 1)
		{
			menu.add(this.noItemsMenuText);
		}
	},

	clearMenu: function(menu)
	{
		menu.items.each(function(item)
		{
			item.menu = null;
		}, this);
		
		menu.removeAll();
	},

	createMenuConfig : function(component, hideOnClick)
	{
		var config = Ext.apply({}, component.initialConfig);
		var group = component.toggleGroup;

		Ext.copyTo(config, component, ['iconCls', 'overflowIconCls', 'icon', 'itemId', 'disabled', 'handler', 'scope', 'menu']);

		if(config.overflowIconCls != null)
		{
			config.iconCls = config.overflowIconCls;
		}
		
		Ext.apply(config, {
			text: component.overflowText || component.text,
			hideOnClick: hideOnClick
		});

		if(group || component.enableToggle)
		{
			Ext.apply(config, {
				group  : group,
				checked: component.pressed,
				listeners:
				{
					checkchange: function(item, checked)
					{
						component.toggle(checked);
					}
				}
			});
		}

		delete config.ownerCt;
		delete config.xtype;
		delete config.id;
	
		return config;
	},

	addComponentToMenu: function(menu, component)
	{
		if(component instanceof Ext.Toolbar.Separator)
		{
			menu.add('-');
		}
		else if(Ext.isFunction(component.isXType))
		{
			if(component instanceof Z8.SplitButton)
			{
				menu.add(this.createMenuConfig(component, true));
			}
			else if(component instanceof Z8.Button)
			{
				menu.add(this.createMenuConfig(component, !component.menu));
			}
			else if(component instanceof Z8.desktop.MenuItem)
			{
				menu.add(this.createMenuConfig(component, !component.menu));
			}
			else if(component.isXType('buttongroup'))
			{
				component.items.each(function(item)
				{
					this.addComponentToMenu(menu, item);
				}, this);
			}
		}
	},

	createMenu: function()
	{
		if(!this.triggerBox)
		{
			this.menu = new Z8.menu.MulticolumnMenu(
			{
				ownerCt : this.layout.container,
				listeners:
				{
					scope: this,
					beforeshow: this.beforeMenuShow
				}
			});

			this.triggerButton = new Z8.Button({ iconCls: 'x-toolbar-more-icon', hidden: true, menu: this.menu });
			this.triggerBox = new Ext.Container({ width: this.triggerWidth, items: this.triggerButton });
			this.layout.container.add(this.triggerBox);
			this.layout.container.doLayout();
		}
	},

	destroy: function()
	{
		Ext.destroy(this.menu, this.triggerBox);
	}
});

Ext.layout.boxOverflow.menu = Ext.layout.boxOverflow.Menu;


Ext.layout.boxOverflow.HorizontalMenu = Ext.extend(Ext.layout.boxOverflow.Menu,
{
	constructor: function()
	{
		Ext.layout.boxOverflow.HorizontalMenu.superclass.constructor.apply(this, arguments);

		var me = this,
		layout = me.layout,
		origFunction = layout.calculateChildBoxes;

		layout.calculateChildBoxes = function(visibleItems, targetSize)
		{
			var calcs = origFunction.apply(layout, arguments),
			meta  = calcs.meta,
			items = me.menuItems;

			//calculate the width of the items currently hidden solely because there is not enough space
			//to display them
			var hiddenWidth = 0;
			for(var index = 0, length = items.length; index < length; index++)
			{
				hiddenWidth += items[index].boxWidth;
			}

			meta.minimumWidth += hiddenWidth;
			meta.tooNarrow = meta.minimumWidth > targetSize.width;

			return calcs;
		};        
	},

	handleOverflow: function(calculations, targetSize)
	{
		this.showTrigger();

		var newWidth = targetSize.width;
		var boxes = calculations.boxes;
		var usedWidth = 0;
		var recalculate = false;

		//calculate the width of all visible items and any spare width
		for(var index = 0, length = boxes.length; index < length; index++)
		{
			usedWidth += boxes[index].width;
		}

		var spareWidth = newWidth - usedWidth;
		var showCount  = 0;

		//see if we can re-show any of the hidden components
		for(var index = 0, length = this.menuItems.length; index < length; index++)
		{
			var item = this.menuItems[index];
			var width = item.boxWidth;

			if(width < spareWidth)
			{
				item.show();
	
				spareWidth -= width;
				showCount ++;
				recalculate = true;
			}
			else
			{
				break;
			}
		}

		if(recalculate)
		{
			this.menuItems = this.menuItems.slice(showCount);
		}
		else
		{
			for(var i = boxes.length - 1; i >= 0; i--)
			{
				var item  = boxes[i].component;
				var right = boxes[i].left + boxes[i].width;

				if(item == this.triggerBox)
				{
					continue;
				}

				if(right >= (newWidth - this.triggerWidth))
				{
					if(this.menuItems.indexOf(item) == -1)
					{
						item.hide();
						item.boxWidth = boxes[i].width;
						this.menuItems.unshift(item);
					}
				}
				else
				{
					break;
				}
			}
			
			var left = 0;
		
			for(var i = 0; i < boxes.length; i++)
			{
				var box = boxes[i];
				
				if(i == 0)
				{
					left = box.left;
				}
				
				if(box.component.isVisible())
				{
					box.left = left;
					box.top = 0;
					
					left += box.width;
				}
			}
			
		}

		if(this.menuItems.length == 0)
		{
			this.hideTrigger();
		}

		return { targetSize: { height: targetSize.height, width: newWidth }, recalculate: recalculate };
	}
});

Ext.layout.boxOverflow.menu.hbox = Ext.layout.boxOverflow.HorizontalMenu;