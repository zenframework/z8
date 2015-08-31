Z8.view.FilesButton = Ext.extend(Z8.SplitButton,
{
	query: null,

	text: 'Файлы', 
	tooltip: 'Присоединенные документы',
	disabled: true,
	iconCls: 'silk-attach',
	iconAlign: 'top',
	
	split: false,

	initComponent: function()
	{
		Z8.view.FilesButton.superclass.initComponent.call(this);

		this.addEvents('beforeMenuShow', 'menuItemRemoved');
		
		//this.menu = new Z8.view.FilesButtonMenu();
		//this.menu.on('beforeShow', this.onBeforeShow, this);
		//this.menu.on('itemRemoved', this.onItemRemoved, this);
	},
	
	onBeforeShow: function(menu)
	{
		return this.fireEvent('beforeMenuShow', this, menu);
	},

	onItemRemoved: function(menu, item)
	{
		return this.fireEvent('menuItemRemoved', this, menu, item);
	},
	
	initializeMenu: function(files)
	{
		this.menu.initialize(files);
	}
});

Z8.view.FilesButtonMenu = Ext.extend(Z8.menu.MulticolumnMenu,
{
	initComponent: function()
	{		
		Z8.view.FilesButtonMenu.superclass.initComponent.call(this);
		
		this.initialize(this.files || []);
	
		this.addEvents('itemRemoved');
	},

	initialize: function(files)
	{
		var active = [];
		var deleted = [];
		
		for(var i = 0; i < files.length; i++)
		{
			var file = files[i];
			file.deleted ? deleted.push(file) : active.push(file);
		}

		this.removeAll();
		
		if(active.length != 0)
		{
			this.add(new Ext.menu.TextItem({ text: 'Документы' }));
			this.add(new Ext.menu.Separator());
			this.addItems(active, true);
		}
		
		if(deleted.length != 0)
		{
			this.add(new Ext.menu.TextItem({ text: 'История' }));
			this.add(new Ext.menu.Separator());
			this.addItems(deleted, false);
		}
		
		this.doLayout();
	},

	addItems: function(files, closable)
	{
		for(var i = 0; i < files.length; i++)
		{
			var file = files[i];
			
			var item = new Ext.menu.Item({
				text: file.name + (file.time != null ? ' (' + file.time + ')' : ''),
				file: file,
				closable: closable ? !this.locked : false,
				handler: this.onFileOpen
			});
			
			if(item.closable)
			{
				item.on('closeclick', this.onCloseClick, this);
			}
			
			this.add(item);
		}
	},
	
	onCloseClick: function(item, e)
	{
		this.fireEvent('itemRemoved', this, item);
	},
	
	onFileOpen: function(item)
	{
		Z8.FileViewer.show(item.file.file, item.file.name);
	}
});
