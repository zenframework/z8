Z8.view.DetailButton = Ext.extend(Z8.SplitButton,
{
	cls: 'z8-panel-title',
	
	query: null,
	link: null,
	
	split: false,
	
	initComponent: function()
	{
		Z8.view.DetailButton.superclass.initComponent.call(this);

		this.addEvents('detailChanged');
		
		if(Z8.isEmpty(this.query.backwards))
		{
			return;
		}

		var links = this.query.backwards;
	
		var items = [];
			
		for(var i = 0; i < links.length; i++)
		{
			var link = links[i];
			items[i] = new Ext.menu.CheckItem({ menuId: i, checked: link == this.link, group: 'backwards', text: link.text, handler: this.onMenu, scope: this });
		}
		
		var backwardsItem = new Ext.menu.Item({ menuId: 'backwards', text: 'К списку форм', handler: this.onMenu, scope: this });
		items.push([new Ext.menu.Separator(), backwardsItem]);
			
		if(items.length > 1)
		{
			this.menu = new Z8.menu.MulticolumnMenu({ items: items });
			this.split = true; 
		}

		this.setText(this.link.text);
		this.setTooltip(this.link.text);
		
		this.on('click', this.toggleMenu, this);
	},
	
	onMenu: function(menuItem)
	{
		this.fireEvent('detailChanged', this, menuItem);
	}
});		
