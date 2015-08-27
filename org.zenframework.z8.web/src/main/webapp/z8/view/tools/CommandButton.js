Z8.view.CommandButton = Ext.extend(Z8.SplitButton,
{
	query: null,
	
	text: 'Действия',	
	tooltip: 'Действия',
	disabled: true,
	iconCls: 'icon-folder',
	iconAlign: 'top',
	split: false,

	command: null,
	
	initComponent: function()
	{
		Z8.view.CommandButton.superclass.initComponent.call(this);

		this.addEvents('command');
		
		var menuItems = [];
		
		var commands = this.query.commands || [];
	
		for(var i = 0; i < commands.length; i++)
		{
			var icon = Ext.isEmpty(commands[i].icon) ? '' : commands[i].icon;
			menuItems[i] = new Ext.menu.Item({ menuId:i, text: commands[i].text, handler: this.onMenu, scope: this, iconCls: icon});
		}

		this.on('click', this.toggleMenu, this);
		
		if(!Z8.isEmpty(menuItems))
		{
			this.split = true; 
			this.menu = new Z8.menu.MulticolumnMenu({ items: menuItems });
		}
	},
	
	onMenu: function(menuItem)
	{
		this.command = this.query.commands[menuItem.menuId];
		this.fireEvent('command', this, this.command);
	}
});