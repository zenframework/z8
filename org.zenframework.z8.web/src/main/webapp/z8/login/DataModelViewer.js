Z8.view.DataModelViewer = Ext.extend(Z8.Window,
{
	layout: 'fit',
	width: 600,
	height: 500, 
	
	initComponent: function()
	{
		Z8.view.DataModelViewer.superclass.initComponent.call(this);
		this.tree = createGrid(this.query, { border: true, readAction: 'model' });
		this.add(this.tree);
	}
});