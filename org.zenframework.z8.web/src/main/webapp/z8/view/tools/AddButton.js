Z8.view.AddButton = Ext.extend(Z8.SplitButton,
{
	query: null,
	
	plain: false,
	text: 'Добавить',	
	tooltip: 'Добавить запись (Ctrl+I)',
	disabled: true,
	iconCls: 'icon-add',
	iconAlign: 'top',
	split: false,
	
	initComponent: function()
	{
		Z8.view.AddButton.superclass.initComponent.call(this);
		
		this.addEvents('addRecord', 'addRootRecord');
		
		if(this.query.parentKey != null)
		{
			var items = [];
			items[0] = new Ext.menu.Item({ text: 'Добавить запись (Ctrl+I)', handler: this.onAddRecord, scope: this });
			items[1] = new Ext.menu.Item({ text: 'Добавить корневую запись', handler: this.onAddRootRecord, scope: this });
			
			this.menu = new Z8.menu.MulticolumnMenu({ items: items });
			
			this.split = true;
		}
		
		this.on('click', this.onAddRecord, this);
	},
	
	onAddRecord: function()
	{
		this.fireEvent('addRecord', this);
	},
	
	onAddRootRecord: function()
	{
		this.fireEvent('addRootRecord', this);
	}
});