Z8.query.Field = Ext.extend(Ext.util.Observable, 
{
	id: null,
	type: Z8.Types.String,
	aggregation: null,
	serverType: Z8.ServerTypes.String,
	readOnly: false,
	evaluations: null,
	visible: true,

/*
	link			means that this field is a link
	linked			means that this field belongs to a detail query
	groupId			when linked is true groupId is the id of the query owning this field
	queryId			when this field is a link queryId is the id of the detail query
	text:			when this field is a link text property is the user readable name of the corresponding query
	selector:		id of the field used to switch between links from options array
	options:		array of link ids
*/

	constructor : function(config)
	{
		Ext.apply(this, config);

		this.type = Z8.Types.fromServerType(this.serverType);
		this.gridXType = config.xtype || Z8.Grid.xtype(this.type);
		
		this.formXType = (this.linked || this.selector) ? 'z8.combo' : Z8.Form.xtype(this.serverType);

		Z8.query.Field.superclass.constructor.call(this);
	},
	
	getColumn: function()
	{
		var columnClass = Ext.grid.Column.types[this.gridXType || 'gridcolumn'];
		var column = new columnClass(Z8.Grid.newColumn(this));
		return column; 
	},
	
	render: function(value)
	{
		return this.getColumn().renderer(value);
	}
});