Z8.Panel = Ext.extend(Ext.Panel,
{
	cls: 'z8-panel',
	border: true,
	showToolbarBtnText: true,

	initComponent: function()
	{
		if(this.toolbarItems == null)
		{
			this.toolbarItems = [];
		}
		
		if(this.header !== false)
		{
			if(this.title != null)
			{
				this.titleText = this.title;
				this.title = null;
			}

			this.header = true;
		}

		Z8.Panel.superclass.initComponent.call(this);
		
		this.addEvents('close');
	},
	
	afterRender: function(container)
	{
		Z8.Panel.superclass.afterRender.call(this, container);
	
		if(this.header != null && this.toolbar == null)
		{
			if(this.titleText != null)
			{
				this.titleItem = new Z8.Toolbar.TextItem({ text: this.titleText });
				this.toolbarItems.unshift(this.titleItem);
			}
	
			if(this.closable)
			{
				this.closeButton = new Z8.Button({ align: 'right', tooltip: 'Закрыть', iconCls: 'icon-close', iconAlign: 'top', text: 'Закрыть', handler: this.onClose, scope: this});
				this.toolbarItems.push(this.closeButton);
			}
			
			this.toolbar = new Z8.Toolbar({
				cls: this.showToolbarBtnText ? 'z8-header-bar' : 'z8-header-bar-small' ,
				renderTo: this.header,
				items: this.toolbarItems
			});
		}
	},
	
	onLayout: function(shallow, force)
	{
		Z8.Panel.superclass.onLayout.call(this, shallow, force);
	
		if((!shallow || force) && this.toolbar != null)
		{
			this.toolbar.setWidth(this.body.dom.offsetWidth);
			this.toolbar.doLayout();
		}
	},
	
	setTitle: function(text)
	{
		if (this.titleItem)
		{
			this.titleItem.setText(text);
		}
	},

	close: function()
	{
		this.onClose();
	},
	
	onClose: function()
	{
		this.fireEvent('close', this);
	},
	
	onDestroy: function()
	{
		if(this.toolbar != null)
		{
			this.toolbar.destroy();
		}
		
		Z8.Panel.superclass.onDestroy.call(this);
	}
});