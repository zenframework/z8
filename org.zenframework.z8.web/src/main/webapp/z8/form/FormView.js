Z8.form.FormView = Ext.extend(Z8.Panel,
{
	flex: 1,
	layout: 'border',
	header: false,

	constructor: function(config)
	{
		var query = config.query;
		
		this.pageBar = new Z8.PagingToolbar({ store: query.getStore() });
		this.pageBar.setOneRecordMode();
		
		config.bbar = this.pageBar;
		
		Z8.form.FormView.superclass.constructor.call(this, config);
	},
	
	initComponent: function()
	{
		Z8.form.FormView.superclass.initComponent.call(this);

		this.navigator = new Z8.Panel({ region: 'west', query: this.query, header: false, border: true, width: 200, style:'border-left-width: 0px;' });
	
		this.form = new Z8.form.Form({ region: 'center', border: false, query: this.query });
		
//		this.add(this.navigator);
		this.add(this.form);
	},
	
	setRecord: function(record)
	{
		this.form.setRecord(record);
	}
});