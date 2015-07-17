Z8.view.BackwardsDataView = Ext.extend(Z8.Dataview,
{
	itemSelector: 'li.backward',
    overClass   : 'backward-hover',
    singleSelect: true,
    multiSelect : false,
    autoScroll  : true,
    cls: 'backwards',
    
	initComponent: function()
	{
		Z8.view.BackwardsDataView.superclass.initComponent.call(this);
	},
	
	buildTpl: function(config) {
		config.tpl = new Ext.XTemplate(
			'<ul>',
				'<tpl for=".">',
					'<li class="backward">',
						'<img width="32" height="32" src="{icon}" />',
						'<strong>{name}</strong>',
						//'<span>{desc}</span>',
					'</li>',
				'</tpl>',
			'</ul>'
		);
	}
});

Z8.view.BackwardsView = Ext.extend(Z8.Panel,
{	
	layout: 'border',
	border: false,
	frame: false,
	view: null,
	header: false,
	
	constructor: function(config)
	{
		this.center = new Ext.Container({ region: 'center', layout: 'fit' });
		config.items = [this.center];
		
		Z8.view.BackwardsView.superclass.constructor.call(this, config);
		
		this.addEvents("backwardclick");
	},
	
	
	initComponent: function()
	{
		Z8.view.BackwardsView.superclass.initComponent.call(this);
		
		var data = [];

		
		Ext.each(this.data, function(item)
		{
			var icon = item.icon || 'form_32.png';
			icon = Z8.getRequestUrl() + '/resources/images/icons/' + icon;
			
			data.push([item.text, item.queryId, icon]);
		});
		
		this.view = new Z8.view.BackwardsDataView(
		{
			data: data,
			fields: [{name: 'name'}, {name: 'id'},  {name: 'icon'}]
		});
		
		this.view.on('click', this.onClick, this);
		
		this.center.add(this.view);
	},
	
	onClick: function(view, index, item, e)
	{
		var rec = view.getRecord(item);
		this.fireEvent('backwardclick', index, rec);
	}
});