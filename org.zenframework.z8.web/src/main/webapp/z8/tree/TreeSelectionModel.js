Z8.tree.DefaultSelectionModel = Ext.extend(Ext.tree.DefaultSelectionModel,
{
	constructor: function(config)
	{
		Z8.tree.DefaultSelectionModel.superclass.constructor.call(this, config);
		
		this.addEvents('beforerowselect');
 	},
	
 	onNodeClick: function(node, e)
	{
		this.select(node, false, true);
	},

	select: function(node, /* private*/ selectNextNode, byClick)
	{
		// If node is hidden, select the next node in whatever direction was being moved in.
		if(!Ext.fly(node.ui.wrap).isVisible() && selectNextNode)
		{
			return selectNextNode.call(this, node);
		}
		
		var last = this.selNode;
		
		if(node == last)
		{
			node.ui.onSelectedChange(true);
			
			if(byClick)
			{
				this.fireEvent('selectionchange', this, node, last, byClick);
			}
		}
		else if(this.fireEvent('beforerowselect', this, node, last) !== false)
		{
			if(last && last.ui)
			{
				last.ui.onSelectedChange(false);
			}
			this.selNode = node;
			node.ui.onSelectedChange(true);
			this.fireEvent('selectionchange', this, node, last, byClick);
		}
		
		return node;
	}
});