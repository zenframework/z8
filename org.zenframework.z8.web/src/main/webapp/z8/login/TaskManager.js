Z8.desktop.TaskManager = Ext.extend(Ext.util.MixedCollection, 
{
	shortcutsState: null,
	
	constructor: function(config)
	{
		Ext.apply(this, config);
		Z8.desktop.TaskManager.superclass.constructor.call(this);
		
		this.addEvents('activate');
	},
	
	getDirtyViews: function()
	{
		var result = [];
		
		for(var i = 0; i < this.items.length; i++)
		{
			var task = this.items[i];
			
			if(task.query != null && task.isDirty())
			{
				result.push(task);
			}
		}
		
		return result;
	},

	reserve: function(id)
	{
		this.add(id, {});
	},
	
	isReserved: function(id)
	{
		var obj = this.get(id);
		
		if(obj === undefined){
			return false;
		} else {
			return Z8.isEmpty(obj);
		}
	},
	
	register: function(task, id)
	{
		this.insert(0, id, task);
		
		//this.highlightMenuItem(task);
		
		task.on('destroy', this.onDestroy, this);
		
		Z8.viewport.desktopBody.add(task);
		task.deferLayout = true;

		this.activate(task);
	},
	
	highlightMenuItem: function(task)
	{
		var menuItems = Z8.viewport.menuBar.items.items;
		
		//var highlightDone = false;
		
		Ext.each(menuItems, function(menuItem, index) {
			if (menuItem.menuQueries)
			{
				if (task.query)
				{
					var r = task.query.requestId;
					if (menuItem.menuQueries.indexOf(r) !== -1){
						menuItem.addClass('x-btn-top-highlight');
						//highlightDone = true;
					} else {
						menuItem.removeClass('x-btn-top-highlight');
					}
				}
				else
				{
					menuItem.removeClass('x-btn-top-highlight');
				}
			}
		});
	},

	onDestroy: function(task)
	{
		task.un('destroy', this.onDestroy, this);
		this.unregister(task);
		this.activateLast();
	},
	
	unregister: function(task)
	{
		Ext.isString(task) ? this.removeKey(task) : this.remove(task);
		
		//this.unhighlightMenuItem();
	},
	
	unhighlightMenuItem: function()
	{
		var menuItems = Z8.viewport.menuBar.items.items;
		
		Ext.each(menuItems, function(menuItem, index) {
			if (menuItem.menuQueries)
			{
				var un = true;
				
				Ext.each(this.keys, function(key, i) {
					if (menuItem.menuQueries.indexOf(key) !== -1){
						un = false;
					}
				});
		
				if (un) {
					menuItem.removeClass('x-btn-top-highlight');
				}
			}
		}, this);
	},
	
	getTask: function(task)
	{
		return Ext.isString(task) ? this.item(task) : task;
	},
	
	activate: function(task)
	{
		if (task.query)
		{
			this.current = task.query.requestId;
			Z8.History.add(task.query.requestId);
		}
		else
		{
			this.current = task.id;
			
			if(Ext.isIE)
			{
				Z8.History.add(task.id);
			}
		}
		
		this.highlightMenuItem(task);
		
		var index = this.indexOf(task);
		var task = this.getTask(task);
		
		if(index != 0)
		{
			var id = this.keys[index];		
			this.remove(task);
			this.insert(0, id, task);
		}

		for(var i = 0; i < this.items.length; i++)
		{
			var item = this.items[i];
			
			if(item != task && !Z8.isEmpty(item))
			{
				item.setVisible(false);
			}
		}
		
		task.setVisible(true);

		Z8.viewport.desktopBody.doLayout(true, false);

		this.fireEvent('activate', task, index);
	},
	
	activateLast: function()
	{
		var items = this.items;
		
		if(items.length != 0)
		{
			this.activate(this.items[0]);
		}
	},
	
	destroy: function(task)
	{
		var view = this.getTask(task);
		view.destroy();
		this.activateLast();
	}
});

Ext.apply(Z8.TaskManager, new Z8.desktop.TaskManager());
