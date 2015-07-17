Z8.view.MoveDialog = Ext.extend(Z8.Window, 
{
	width:600,
	height: 300,
	resizable: true,
	modal: true, 

	title: 'Перенести',
	layout: 'border',
//	layoutConfig: { align: 'stretch' },

	initComponent: function()
	{
		Z8.view.MoveDialog.superclass.initComponent.call(this);
		
		this.grid = createGrid(this.query, { region: 'center', border: true });

		var selectionModel = this.grid.getSelectionModel();
		selectionModel.on('selectionchange', this.onSelectionChange, this);
		
		this.add(this.grid);
		
		this.okButton = new Ext.Button({ text: 'OK', disabled: true, iconCls: '', handler: this.onOK, scope: this });
		this.addButton(this.okButton);
		
		this.cancelButton = new Ext.Button({ text: 'Oтмена', handler: this.onCancel, scope: this });
		this.addButton(this.cancelButton);
		
		this.on('close', this.onCancel, this);

		this.addEvents('ok', 'error');
	},

	onSelectionChange: function()
	{
		var record = this.grid.getSelectedRecord();
		this.okButton.setDisabled(record == null);
	},
	
	onCancel: function()
	{
		this.destroy();
	},

	onOK: function()
	{
		var record = this.grid.getSelectedRecord();
		this.fireEvent('ok', record.id);
		this.destroy();
	}
});