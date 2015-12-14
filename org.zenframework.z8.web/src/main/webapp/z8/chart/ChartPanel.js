Z8.chart.ChartPanel = Ext.extend(Z8.Panel, 
{
	cls: 'z8-chart-panel',	
	
	header: false,
	layout: 'vbox',
	layoutConfig: { align: 'stretch', pack: 'start' },
	
	query: null, // only when chartPanel is used with existing query, i.e. Z8.view.GridView
	chartConfig: null,
	
	constructor: function(config)
	{
//		var printChart = new Z8.Button({ iconCls: 'silk-printer', handler: this.onChartPrint, scope: this });
		
		Z8.chart.ChartPanel.superclass.constructor.call(this, config);
	},

	initComponent: function()
	{
		Z8.chart.ChartPanel.superclass.initComponent.call(this);

		this.label = new Ext.form.Label({ text: '', flex: 1, margins: { top: 5, left: 20, right: 0, bottom: 0 } });
		this.refreshButton = new Z8.Button({ tooltip: 'Обновить', cls: 'x-chart-panel-btn', overCls: 'x-chart-panel-btn-over', iconCls: 'silk-arrow-refresh-grey', margins: { top: 0, left: 0, right: 10, bottom: 0 }, plain: true, height: 20, width: 20, handler: this.onRefresh, scope:this });
		this.printButton = new Z8.Button({ tooltip: 'Изображение', cls: 'x-chart-panel-btn', overCls: 'x-chart-panel-btn-over', iconCls: 'silk-printer-grey', margins: { top: 0, left: 0, right: 10, bottom: 0 }, plain: true, height: 20, width: 20, handler: this.onPrint, scope:this });
		this.settingsButton = new Z8.Button({ tooltip: 'Настроить', cls: 'x-chart-panel-btn', overCls: 'x-chart-panel-btn-over', iconCls: 'silk-cog', margins: { top: 0, left: 0, right: 10, bottom: 0 }, plain: true, height: 20, width: 20, handler: this.onSettings, scope:this });
		this.clearButton = new Z8.Button({ tooltip: 'Удалить', cls: 'x-chart-panel-btn', overCls: 'x-chart-panel-btn-over', iconCls: 'silk-cross-grey', margins: { top: 0, left: 0, right: 20, bottom: 0  }, plain: true, height: 20, width: 20, handler: this.onClear, scope:this });
		this.toolbar = new Ext.Container({ height: 25, layout: 'hbox', cls: 'x-chart-panel-header', layoutConfig: { align: 'middle', pack: 'start' } });

		this.toolbar.add(this.label);
		this.toolbar.add(this.refreshButton);
		this.toolbar.add(this.printButton);
		this.toolbar.add(this.settingsButton);
		this.toolbar.add(this.clearButton);
		
		this.chartContainer = new Ext.Container({ flex: 1, layout: 'fit' });
		
		this.add(this.toolbar);
		this.add(this.chartContainer);

		this.setChartConfig(this.chartConfig);
	},

	setChartConfig: function(config)
	{
		if(config != null)
		{
			if(this.chartConfig == null)
			{
				this.chartConfig = {};
			}
			
			Ext.apply(this.chartConfig, config);

			if(this.store != null)
			{
				this.store.un('load', this.onDataChanged, this);
				this.store.un('write', this.onDataChanged, this);
			}
			
			if(this.query != null)
			{
				this.store = this.query.getStore();
			
				this.store.on('write', this.onDataChanged, this);
				this.store.on('load', this.onDataChanged, this);
			}
	
			this.onDataChanged();
		}
		else
		{
			this.chartConfig = null;
			this.clear();
		}
		
		this.updateButtons();
	},
	
	onDataChanged: function()
	{
		var filter = null;
		
		if(this.query != null)
		{
			filter = this.query.filter1;
		}
		else if(this.chartConfig.dataSource.filterOn)
		{
			filter = this.chartConfig.dataSource.filter;
		}
		
		var params = {
			filter1: filter != null ? Ext.encode(filter) : null
		};
		
		if(this.chartConfig.chartType != 'Table') {
			params.xaction = 'read';
			params.totalsBy = this.getXAxes().id;
			params.aggregation = Ext.encode(this.getYAxesIds());
		}
		
		var requestId = this.query != null ? this.query.requestId : this.chartConfig.dataSource.id;
		
		this.refreshButton.setBusy(true);
		
		Z8.Ajax.request(requestId, this.onChartDataLoaded, this.onChartDataLoadError, params, this);
	}, 

	onChartDataLoaded: function(result)
	{
		this.refreshButton.setBusy(false);
		
		if(this.chartConfig.chartType != 'Table')
			this.chartData = this.getDataTable(result.data);
		else
			this.chartData = result;

		this.drawChart();
	},
	
	onChartDataLoadError: function(info)
	{
		this.refreshButton.setBusy(false);

		Z8.showMessages('Z8', Z8.Format.nl2br(info.messages));
	},

	drawChart: function()
	{
		if(this.chartConfig != null)
		{
			this.draw();
		}
	},
	
	clear: function()
	{
		this.label.setText('');
		
		if(this.chart != null && this.chartContainer.rendered)
		{
			var children = this.chartContainer.getEl().dom.children;
			
			for(var i = children.length - 1; i >= 0; i--)
			{
				Ext.removeNode(children[i]);
			}
		}

		this.chart = null;
	},
	
	draw: function()
	{
		if(this.chartContainer.rendered)
		{
			if(this.chartConfig != null && this.chartData != null)
			{
				this.label.setText(this.chartConfig.chartName);

				if(this.chartConfig.chartType != 'Table') {
					if(this.chart == null)
					{
						this.chart = new google.visualization.ChartWrapper({ options: { fontName: 'Segoe UI', fontSize: 11, is3D: true } });
					}
					
	
					this.chart.setContainerId(this.chartContainer.id);
					this.chart.setChartType(this.chartConfig.chartType);
			//		this.chart.setOption('series', this.getSeriesOptions());
					this.chart.setDataTable(this.chartData);
					this.chart.draw();
				} else {
					if(this.grid != null)
						this.grid.destroy();
					
					this.grid = createGrid(this.chartData, { editable: false, border: false });
					this.chartContainer.add(this.grid);
					this.chartContainer.doLayout();
				}
			}
		}
		else
		{
			this.chartContainer.on('afterrender', this.draw, this);
		}
	},
	
	getDataTable: function(data)
	{
		var dataTable = new google.visualization.DataTable();
		
		var fields = this.getAllFields();
		
		for(var i = 0; i < fields.length; i++)
		{
			var field = fields[i];
			dataTable.addColumn(i != 0 ? field.type : Z8.GoogleTypes.String, field.header);
		}

		for(var i = 0; i < data.length; i++)
		{
			var row = [];

			for(var j = 0; j < fields.length; j++)
			{
				var field = new Z8.query.Field(fields[j]);
				var v = Z8.decode(data[i][field.id], field.serverType);
				var f = Ext.util.Format.ellipsis(field.render(v), 20, true);
				row.push({ v: j == 0 ? f : v, f: f });
			}
			
			dataTable.addRow(row);
		}
		
		return dataTable;
	},

	onLayout: function(shallow, force)
	{
		Z8.chart.ChartPanel.superclass.onLayout.call(this, shallow, force);
		this.draw();
	},

/*	
	getSeriesOptions: function()
	{
		var series = {};
		
		if (this.fieldsButton.menu.items)
		{
			var i = 0;
			
			this.fieldsButton.menu.items.each(function(field, index){
				
				if(field.checked)
				{
					if(field.menu)
					{
						field.menu.items.each(function(subitem, subindex){
							if(subitem.checked)
							{
								series[i] = {type: subitem.type};
							}
						});
						i++;
					}
				}
			});
		}
		
		return series;
	},
*/

	getXAxes: function()
	{
		return this.chartConfig.dataSource.xAxes;
	},
	
	getYAxes: function()
	{
		return this.chartConfig.dataSource.yAxes;
	},

	getYAxesIds: function()
	{
		var fields = [];
		
		var yAxes = this.chartConfig.dataSource.yAxes;
		
		for(var i = 0; i < yAxes.length; i++)
		{
			fields.push(yAxes[i].id);
		}
		
		return fields;
	},

	getAllFields: function()
	{
		return [this.getXAxes()].concat(this.getYAxes());
	},

	updateButtons: function()
	{
		this.printButton.setVisible(this.chartConfig != null);
		this.toolbar.doLayout(true, true);
	},

	
	onSettings: function()
	{
		var config = {};

		if(this.chartConfig != null)
		{
			Ext.apply(config, this.chartConfig);
		}

		if(this.query != null)
		{
			config.chartName = this.query.text;
			
			if(config.dataSource != null)
			{
				config.dataSource.query = this.query;
			}
			else
			{
				config.dataSource = { query: this.query };
			}
		}

		var steps = [];
		
		steps.push(new Z8.view.ChartPreviewPanel({ config: config, showTable: this.query == null }));
		
		if(this.query == null)
		{
			steps.push(new Z8.view.ChartDataSourcePanel({ config: config }));
		}
		
		steps.push(new Z8.view.ChartAxesPanel({ config: config }));

		var wizard = new Z8.view.WizardWindow({ steps: steps, chartConfig: config });
		wizard.on('finish', this.onSettingsChanged, this);
		wizard.show();
	},
	
	onRefresh: function()
	{
		this.setChartConfig(this.chartConfig);
	},

	onPrint: function()
	{
 		var svg = Ext.DomQuery.selectNode('svg', this.getEl().dom);
 		var canvas = document.createElement('canvas');
 		
 		canvg(canvas, svg.parentNode.innerHTML);
 		
 		var base64 = canvas.toDataURL("image/png");
 		
		var data = 
		{
			base64: base64.substring(base64.indexOf(',') + 1),
			source: this.chartConfig.chartName + '.' + 'png' //strType.toLowerCase()
		};
		
		Z8.FileViewer.download(data);
		
		Ext.removeNode(canvas);
	},
	
	onSettingsChanged: function(wizard)
	{
		this.setChartConfig(wizard.chartConfig);
		
		if(this.changeHandler != null)
		{
			this.changeHandler.call(this.scope, this, wizard.chartConfig);
		}
	},

	onClear: function()
	{
		this.setChartConfig(null);
		
		if(this.changeHandler != null)
		{
			this.changeHandler.call(this.scope, this, null);
		}
	},
	
	beforeDestroy: function()
	{
		if(this.store != null)
		{
			this.store.un('load', this.onDataChanged, this);
			this.store.un('write', this.onDataChanged, this);
		}
		
		this.clear();
		
		Z8.chart.ChartPanel.superclass.beforeDestroy.call(this);
	}
});
