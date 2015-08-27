Z8.desktop.DashboardPanel = Ext.extend(Z8.Panel,
{
	layout: 'fit',
	border: false,
	header: false,
	
	initComponent: function()
	{
		Z8.desktop.DashboardPanel.superclass.initComponent.call(this);

		this.chartPanel = new Z8.chart.ChartPanel();
		this.add(this.chartPanel);
	}
});

Z8.desktop.Dashboard = Ext.extend(Ext.Panel,
{
	layout: 'hbox',
	layoutConfig: { align: 'stretch', pack: 'start' }	,
	margins: { top: 3, left: 3, bottom: 3, right: 3 },

	initComponent: function()
	{
		Z8.desktop.Dashboard.superclass.initComponent.call(this); 

		this.add(this.prepareItems());  
	},
	
	prepareItems: function()
	{
		var settings = Z8.getUserSettings();
		var dashboard = settings.dashboard || { type: '2-2' };

		switch(dashboard.type)
		{
			case '3-3':
				layoutConfig = [{
					region: 'north', split: true, layout: 'border', border: false,
					items: [{
						region:'west', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp1'}), layout: 'fit'
					},{
						region:'center', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp2'}), layout: 'fit'
					},{
						region:'east', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp3'}), layout: 'fit'
					}]
				}, {
					region: 'center', layout: 'border', border: false,
					items: [{
						region:'west', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp4'}), layout: 'fit'
					},{
						region:'center', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp5'}), layout: 'fit'
					},{
						region:'east', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp6'}), layout: 'fit'
					}]
				}];
				
				break;
			
			case '3-2':
				layoutConfig = [{
					layout: 'hbox', layoutConfig: { align: 'stretch', pack: 'start' }, border: false,
					items: [{
						region:'west', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp1'}), layout: 'fit'
					},{
						region:'center', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp2'}), layout: 'fit'
					},{
						region:'east', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp3'}), layout: 'fit'
					}]
				}, {
					region: 'center', layout: 'border', border: false,
					items: [{
						region:'center', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp4'}), layout: 'fit'
					},{
						region:'east', split:true, width: '50%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp5'}), layout: 'fit'
					}]
				}];
				break;
			
			case '2-2':
				layoutConfig = [
				{
					layout: 'vbox', flex: 0.5, layoutConfig: { align: 'stretch', pack: 'start' }, border: false,
					items: 
					[
						new Z8.chart.ChartPanel({ flex: 0.5, border: true, style: 'border-top-width: 1px;', id: 'dp1', margins: { top: 0, left: 0, bottom: 3, right: 3 }, chartConfig: dashboard['dp1'], changeHandler: this.onChartChanged, scope: this }),
						new Z8.chart.ChartPanel({ flex: 0.5, border: true, style: 'border-top-width: 1px;', id: 'dp2', margins: { top: 0, left: 0, bottom: 0, right: 3 }, chartConfig: dashboard['dp2'], changeHandler: this.onChartChanged, scope: this }),
					]
				},
				{
					layout: 'vbox', flex: 0.5, layoutConfig: { align: 'stretch', pack: 'start' }, border: false,
					items:
					[
						new Z8.chart.ChartPanel({ flex: 0.5, border: true, style: 'border-top-width: 1px;', id: 'dp3', margins: { top: 0, left: 0, bottom: 3, right: 0 }, chartConfig: dashboard['dp3'], changeHandler: this.onChartChanged, scope: this }),
						new Z8.chart.ChartPanel({ flex: 0.5, border: true, style: 'border-top-width: 1px;', id: 'dp4', margins: { top: 0, left: 0, bottom: 0, right: 0 }, chartConfig: dashboard['dp4'], changeHandler: this.onChartChanged, scope: this }),
					]
				}];
				break;
				
			case '3-1':
				layoutConfig = [
					{ 
						region: 'north', split: true, layout: 'border', border: false,
						items: [
							{ region:'west', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp1'}), layout: 'fit' },
							{ region:'center', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp2'}), layout: 'fit' },
							{ region:'east', split:true, width: '33%', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp3'}), layout: 'fit' }
						]
					},
					{ 
						region: 'center', layout: 'border', border: false,
						items: [
							{ region:'center', border: false, cls: 'dropable', items: new Z8.desktop.DashboardPanel({id: 'dp4'}), layout: 'fit' }
						]
					}
				];
				break;
		}
		
		return layoutConfig;
	},
	
	getDashboardSettings: function()
	{
		var settings = Z8.getUserSettings();
		return settings.dashboard || { type: '2-2' };
	},
	
	setDashboardSettings: function(dashboard)
	{
		var settings = Z8.getUserSettings();
		settings.dashboard = dashboard;
		Z8.Ajax.request('settings', Ext.emptyFn, Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},
	
	saveConfig: function(chart, config)
	{
		if(config != null)
		{
			delete config.dataSource.query;
		}
		
		var dashboard = this.getDashboardSettings();
		dashboard[chart.id] = config;
		
		this.setDashboardSettings(dashboard);
	},
	
	onChartChanged: function(chart, config)
	{
		this.saveConfig(chart, config);
	}
});