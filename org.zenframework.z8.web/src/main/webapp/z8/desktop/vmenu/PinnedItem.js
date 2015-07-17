Z8.desktop.PinnedItem = Ext.extend(Ext.Toolbar, 
{
	cls: 'x-menu-toolbar',
	
	initComponent: function()
	{
		this.addEvents('close', 'click', 'pin');
		
		Z8.desktop.PinnedItem.superclass.initComponent.call(this);

		var textItem = new Ext.Toolbar.TextItem({ text: this.text });
		var pinButton = new Ext.Button({ iconCls: this.pinned ? 'icon-unpin' : 'icon-pin', handler: this.onPin, scope: this });
		
		this.addButton([ textItem, new Ext.Toolbar.Fill(), pinButton ]);
	},
	
	onRender: function(ct, position)
	{
		Z8.desktop.PinnedItem.superclass.onRender.call(this, ct, position);
		
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

		Z8.desktop.PinnedItem.superclass.onDestroy.call(this);
	},
	
	onPin: function(button)
	{
		if ( ! this.pinned) {
			button.setIconClass('icon-unpin');
			this.pinned = true;
		} else {
			button.setIconClass('icon-pin');
			this.pinned = false;
		}
		
		this.fireEvent('pin', this);
	},

	onMouseEnter: function(event, target, object)
	{
		this.getEl().addClass('x-menu-toolbar-over')
	},
	
	onMouseLeave: function(event, target, object)
	{
		this.getEl().removeClass('x-menu-toolbar-over')
	},
	
	onMouseClick: function(e, target, object)
	{
		var el = e.getTarget(null, null, true);
		
		if (! el.hasClass('icon-pin') && ! el.hasClass('icon-unpin')) {
			this.fireEvent('click', this);
		}
	}
});
