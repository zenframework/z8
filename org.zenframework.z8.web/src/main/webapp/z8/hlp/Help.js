Z8.HelpWindow = Ext.extend(Z8.Window, 
{
	width:530,
	height:500,
	title: '',
	closeAction:'hide',
	id:'winhelp',
	plain: false,
	collapsible: false,
	maximizable:false,
	resizable: true,
	title: 'Справка',
	layout:'fit',
	
	center: null,
	tree: null,
	west: null,
	shadow: false,

	count: 0,
	
	initComponent: function()
	{
		Z8.HelpWindow.superclass.initComponent.call(this);
	 	
		this.tree = new Ext.tree.TreePanel(
		{ 
			id: 'help-tree',
			border: false,
			useArrows: true,
			autoScroll: true,
			animate: true,
			containerScroll: true,
			bodyCssClass: 'tree-body',
			dataUrl: Z8.request.url,
			requestMethod: Z8.request.method,
			rootVisible: false,
			root: { nodeType: 'async', draggable: false, id: 'root-node' }
		});		

		this.west = new Ext.Panel(
		{
			region: 'west', 
			width: 220,
			minSize: 100, 
			maxSize: 400,
			frame: false,
			split: true,
			header:false,
			cls:'x-panel-treemenuhelp',
			padding:'5 0 5 5',
			collapsible: true,
			border: true,
			collapseMode: 'mini',
			layout: 'fit',
			layoutConfig: { animate: true, fill: true },
			items:[this.tree] 
		});							
	
		this.previos = [];
		this.next = [];

		this.tree.on('click', this.onClickHelp, this);
		this.tree.on('load', this.onLoad, this);

		var toolbar = new Ext.Container({ cls: 'help-history-bar', height:30, layout: 'hbox', layoutConfig: { align: 'stretch', pack: 'end' },  items: this.createHistoryItems() });

		this.center = new Ext.Panel(
		{
			region: 'center', 
			layout: 'vbox',
			tbar: toolbar,
			id:'help-content',
			cls:'x-panel-content-help',
			layoutConfig: { animate: true, align: 'stretch' }, 
			collapsible: false,
			border:false,
			autoScroll: true,
			frame: false
		});  

		var panel = new Ext.Container({ layout: 'border', items: [this.west, this.center ] });
		this.add(panel)
		
		this.addEvents('activate');
	},
	
	createHistoryItems: function()
	{
		this.previousButton = new Z8.Button({ iconCls: 'icon-prev-page', disabled:true, tooltip:'Назад', text: '', handler: this.onClickHistoryPrev, scope: this });
		this.nextButton = new Z8.Button({ iconCls: 'icon-next-page',  disabled:true, tooltip:'Вперед', text: '', handler: this.onClickHistoryNext, scope: this });

		return [this.previousButton, this.nextButton];
	},
	
	activate: function(file)
	{
		if(file == null)
		{
			file = this.GetActiveHelpFile();
		}
		
		this.previos.push(file);
		var flag = false; 
		var k, n;
		   	
		for(var i = 0; helpfiles != null && i < helpfiles.length; i++)
		{
			if(file == helpfiles[i].helpfile)
			{
				flag = true;
				helpfiles[i]["expanded"] = true;
				k = i;
			}
		    	
			for(var j=0; j < helpfiles2[i].length; j++)
			{
				if(file == helpfiles2[i][j].helpfile)
				{
					flag = true;
					k = i;
					n = j;
					helpfiles2[i][j]["expanded"] = true;
				}
				   
				   
				for(var j1=0; j1 < helpfiles3[i][j].length; j1++)
				{
					if(file == helpfiles3[i][j][j1].helpfile)
					{
						flag = true;
						helpfiles2[i][j]["expanded"] = true;
						helpfiles[i]["expanded"] = true;
						k = i;
						n = j; 
					}
				}
			}   
		}

		root = this.tree.getRootNode();

		if(this.count == 0)
		{	  
			for(var j = 0; helpfiles != null && j < helpfiles.length; j++)
			{
				root.appendChild(helpfiles[j]);
			}
		}  
		else
		{
			try
			{ 
				var id = "help-files-" + k;
				this.tree.getNodeById(id).expand();

				id2 = "help-files-" + k + "-" + n;
				this.tree.getNodeById(id2).expand();
			}
			catch(e){}
		} 
		
		if (file=="Help/start.html") {flag=true}
		if (flag==false) file="Help/404.html";
		
		this.RecordActiveHelpFile(file);
		this.count++;
		this.selectActiveNode(file);
	},
	

	onClickHelp: function(node, event)
	{	
		if(node.attributes.helpfile != "")
		{	
			this.RecordActiveHelpFile(node.attributes.helpfile);
			this.previos.push(node.attributes.helpfile);
			
			if(this.previos.length>1)
			{
				this.previousButton.setDisabled(false);
			}
		} 
	},
	
	onClickHistoryPrev: function()
	{
		n = this.previos.length-2;
		n2 = this.previos.length-1;
		this.RecordActiveHelpFile(this.previos[n]);
		
		this.next.push(this.previos[n2]);
		this.selectActiveNode(this.previos[n]);
		this.previos.pop();
		
		if(this.previos.length == 1)
		{
			this.previousButton.setDisabled(true);
		}
		
		if(this.next.length > 0)
		{
			this.nextButton.setDisabled(false);
		}
		
	},
	
	onClickHistoryNext: function()
	{
		n = this.next.length - 1;
		this.RecordActiveHelpFile(this.next[n]);
		
		this.previos.push(this.next[n]);
		
		this.selectActiveNode(this.next[n]);
		
		this.next.pop();
		
		if(this.previos.length > 1)
		{
			this.previousButton.setDisabled(false);
		}
		
		if(this.next.length == 0)
		{
			this.nextButton.setDisabled(true);
		}
	},
	
	onLoad: function(node)
	{
		try
		{	
			for(var i = 0; helpfiles != null && i < helpfiles.length; i++)
		    {
				if (node.attributes.helpfile == helpfiles[i].helpfile)
				{
					for(var i1 = 0; i1 < helpfiles2[i].length; i1++)
					{
						node.appendChild(helpfiles2[i][i1] );
					}
				}
				else
				{ 
					for(var j = 0; j < helpfiles2[i].length; j++)
					{
						if(node.attributes.helpfile == helpfiles2[i][j].helpfile)
						{
							for(j1 = 0; j1 < helpfiles3[i][j].length; j1++)
							{
								node.appendChild(helpfiles3[i][j][j1]);
							}
						}
					}   
				}
			}
		} 		
		catch (e){} 
	},
		
	GetActiveHelpFile: function()
	{
		var objects = Z8.TaskManager.items;
			file = "Help/start.html";
		
		Ext.each(objects, function(object, index) {
			if (object.isVisible()) {
				if (object.query !== undefined && object.query.help != null) {
					file = object.query.help + '.html';
				}
			}
		});
		/*
		for(var i = 0; i < objects.length; i++)
		{
			if (objects[i].isVisible()) {
				
					if (objects[i].query.help != null) file=objects[i].query.help + '.html';
			}  
		}*/
		return file;
	},
	
	RecordActiveHelpFile: function(file)
	{
		this.center.removeAll();
		this.center.load(file);
	},
	
	selectActiveNode: function(files){
		
		try
		{	
			for(var i = 0; helpfiles != null && i < helpfiles.length; i++)
			{
				if(files == helpfiles[i].helpfile)
				{
					id="help-files-"+i;
					nodes = this.tree.getNodeById(id);
					nodes.select();
				}

				for(j = 0; j < helpfiles2[i].length; j++)
				{
					if(files == helpfiles2[i][j].helpfile)
					{
						id = "help-files-" + i + "-" + j;
						nodes = this.tree.getNodeById(id);
						nodes.select();
					}

					for(var j1 = 0; j1 < helpfiles3[i][j].length; j1++)
					{
						if(files == helpfiles3[i][j][j1].helpfile)
						{
							id = "help-files-" + i + "-" + j + "-" + j1; 
							nodes = this.tree.getNodeById(id);
							nodes.select();
						}
					}
				}   
			}
		} 		
		catch (e){} 
	},

	onClose: function()
	{
		this.hide();
	}
});


function showfile(file)
{
   var helpwin = Ext.getCmp("winhelp");
   file="Help/"+file;
   helpwin.activate(file);
}
