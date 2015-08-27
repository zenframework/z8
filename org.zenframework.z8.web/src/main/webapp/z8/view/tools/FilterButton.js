Z8.view.FilterButton = Ext.extend(Z8.SplitButton,
{
	iconAlign: 'top',
	iconCls: 'icon-filter',
	split: true,
	tooltip: 'Фильтр',
	query: null,
	filter: null,
	enableToggle: true,
	
	initComponent: function()
	{
		Z8.view.FilterButton.superclass.initComponent.call(this);
		
		var editItem = new Ext.menu.Item({ closable: false, text: 'Настроить...', handler: this.onEditFilter, scope: this});
		
		this.menu = new Z8.menu.MulticolumnMenu({ items: [editItem] });
		this.on('toggle', this.onToggle, this);
		
		this.addEvents('filterToggle');
	},
	
	onToggle: function(button, pressed)
	{
		if(pressed && this.filter == null)
		{
			this.toggle(false, true);
			this.onEditFilter();
		}
		else
		{
			this.fireEvent('filterToggle', this, pressed, this.filter);
		}
	},
	
	onEditFilter: function()
	{
		new Z8.view.FilterDialog({ query: this.query, filter: this.filter, handler: this.onOK, scope: this }).show();
	},
	
	onOK: function(dialog, filter)
	{
		this.filter = filter;
		this.toggle(true, true);
		this.onToggle(this, true);
	}	
});