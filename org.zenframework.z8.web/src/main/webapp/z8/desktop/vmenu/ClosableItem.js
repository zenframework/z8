Z8.desktop.ClosableItem = Ext.extend(Ext.Toolbar, 
{
	cls: 'x-menu-toolbar',
	
	initComponent: function()
	{
		this.addEvents('close', 'click');
		
		Z8.desktop.ClosableItem.superclass.initComponent.call(this);

		var textItem = new Ext.Toolbar.TextItem({ text: this.text });
		var closeButton = new Ext.Button({ iconCls: 'silk-cross', handler: this.onClose, scope: this });
		
		this.addButton([ textItem, new Ext.Toolbar.Fill(), closeButton ]);
	},
	
	onRender: function(ct, position)
	{
		Z8.desktop.ClosableItem.superclass.onRender.call(this, ct, position);
		
		this.getEl().on('mouseenter', this.onMouseEnter, this);
		this.getEl().on('mouseleave', this.onMouseLeave, this);
		this.getEl().on('click', this.onMouseClick, this);
	},
	
	onDestroy: function()
	{
		var el = this.getEl();
		
		if(el != null)
		{
			el.un('mouseenter', this.onMouseEnter, this);
			el.un('mouseleave', this.onMouseLeave, this);
			el.un('click', this.onMouseClick, this);
		}

		Z8.desktop.ClosableItem.superclass.onDestroy.call(this);
	},

	onMouseEnter: function(event, target, object)
	{
		this.getEl().addClass('x-menu-toolbar-over')
	},
	
	onMouseLeave: function(event, target, object)
	{
		this.getEl().removeClass('x-menu-toolbar-over')
	},
	
	onClose: function(button)
	{
		this.fireEvent('close', this);
	},
	
	onMouseClick: function(event, target, object)
	{
		this.fireEvent('click', this);
	}
});
