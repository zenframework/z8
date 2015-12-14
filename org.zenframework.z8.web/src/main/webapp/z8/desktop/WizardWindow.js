Z8.view.WizardWindow = Ext.extend(Z8.Window,
{
	title: 'Wizard Window',
	width: 600,
	height: 400,
	modal: true,
	resizable: false,
	
	layout: 'hbox',
	layoutConfig: { align: 'stretch', pack: 'start' },

	steps: [],
	currentStep: 0,
	
	initComponent: function()
	{
		Z8.view.WizardWindow.superclass.initComponent.call(this);
		
		for(var i = 0; i < this.steps.length; i++)
		{
			var step = this.steps[i];
			step.flex = 1;
			step.hidden = i != this.currentStep;
			step.wizard = this;
			
			this.add(step);
		}
		
		this.cancelButton = new Z8.Button({ width: 70, text: 'Oтмена', handler: this.onCancel, scope: this });
		this.addButton(this.cancelButton);

		this.backButton = new Z8.Button({ width: 70, text: 'Назад', iconCls: '', handler: this.onBackPressed, scope: this,disabled: true });
		this.addButton(this.backButton);
		
		this.nextButton = new Z8.Button({ width: 70, text: 'Далее', iconCls: '', handler: this.onNextPressed, scope: this });
		this.addButton(this.nextButton);
		
		this.on('close', this.onCancel, this);
		
		this.addEvents('finish');
		
		var step = this.getCurrentStep();

		if(step.onActivate != null)
		{
			step.onActivate();
		}
		
		this.updateButtons();
	},
	
	getCurrentStep: function()
	{
		return this.steps[this.currentStep];
	},
	
	isFirstStep: function()
	{
		return this.currentStep == 0;
	},

	isLastStep: function()
	{
		var step = this.getCurrentStep();
		
		if(step.isLast != null && step.isLast())
			return true;
		
		return this.currentStep == this.steps.length - 1;
	},
	
	onNextPressed: function()
	{
		var step = this.getCurrentStep();
		
		if(!this.isLastStep())
		{
			if(this.onNext() && (step.onNext == null || step.onNext()))
			{
				step.setVisible(false);
				
				this.currentStep++;
				
				step = this.getCurrentStep();

				this.updateButtons();

				if(step.onActivate != null)
				{
					step.onActivate();
				}
				
				step.setVisible(true);
				
				this.doLayout();
			}
		}
		else if(this.onFinish())
		{
			this.destroy();
		}
	},
	
	onBackPressed: function()
	{
		if(!this.isFirstStep())
		{
			var step = this.getCurrentStep();
		
			if(this.onBack() && (step.onBack == null || step.onBack()))
			{
				step.setVisible(false);
				
				this.currentStep--;
				
				step = this.getCurrentStep();

				step.setVisible(true);
				
				this.updateButtons();
			}
			
			this.doLayout();
		}
	},
	
	updateButtons: function()
	{
		this.backButton.setDisabled(this.isFirstStep());
		this.nextButton.setText(this.isLastStep() ? 'Готово' : 'Далее');
	},
	
	onCancel: function()
	{
		this.destroy();
	},
	
	onClose: function()
	{
		this.onCancel();
	},
	
	onNext: function(index, step)
	{
		return true;
	},
	
	onBack: function(index, step)
	{
		return true;
	},

	onFinish: function()
	{
		this.fireEvent('finish', this);
		return true;
	}
});

Z8.view.ChartPreviewPanel = Ext.extend(Z8.Panel,
{
	closable: false,
	layout: 'absolute',
	border: false,
	header: false,

 	data: google.visualization.arrayToDataTable([
		['Год', 'Продажи', 'Затраты'],
		['2013',  1000,      400],
		['2014',  1170,      460],
		['2015',  660,       1120],
		['2016',  1030,      540]
	]),

 	gridData: [ 
		['2013',  1000,      400],
		['2014',  1170,      460],
		['2015',  660,       1120],
		['2016',  1030,      540]
	],

 	chartOptions: { 
		title: '', 
		fontName: 'Segoe UI', 
		fontSize: 11, 
		is3D: true, 
		legend: { position: 'bottom' },
		chartArea:{ left:60, top:60, width: '75%' }
	},

	initComponent: function()
	{
		Z8.view.ChartPreviewPanel.superclass.initComponent.call(this);

		this.chart = new google.visualization.ChartWrapper({ options: Ext.apply({}, this.chartOptions) });
		
		this.chartNameLabel = new Ext.form.Label({ text: 'Заголовок графика:', x: 10, y: 13 });
		this.chartName = new Ext.form.TextField({ x: 170, y:10, width: 420, value: this.config.chartName, enableKeyEvents: true });

		this.chartTypeLabel = new Ext.form.Label({ text: 'Тип графика:', x: 10, y: 48 });

		var chartType = this.config.chartType || 'BarChart';
		
		this.barChart = new Ext.form.Radio({ x: 30, y: 73, checked: chartType == 'BarChart', boxLabel: 'Bar chart', name: 'chartType', inputValue: 'BarChart', handler: this.onTypeChanged, scope: this });
		this.columnChart = new Ext.form.Radio({ x: 30, y: 98, checked: chartType == 'ColumnChart', boxLabel: 'Column chart', name: 'chartType', inputValue: 'ColumnChart', handler: this.onTypeChanged, scope: this });
		this.steppedAreaChart = new Ext.form.Radio({ x: 30, y: 123, checked: chartType == 'SteppedAreaChart', boxLabel: 'Stepped area chart', name: 'chartType', inputValue: 'SteppedAreaChart', handler: this.onTypeChanged, scope: this });
		this.lineChart = new Ext.form.Radio({ x: 30, y: 148, checked: chartType == 'LineChart', boxLabel: 'Line chart', name: 'chartType', inputValue: 'LineChart', handler: this.onTypeChanged, scope: this });
		this.pieChart = new Ext.form.Radio({  x: 30, y: 173, checked: chartType == 'PieChart', boxLabel: 'Pie chart', name: 'chartType', inputValue: 'PieChart', handler: this.onTypeChanged, scope: this });
		
		if(this.showTable)
			this.tableChart = new Ext.form.Radio({  x: 30, y: 198, checked: chartType == 'Table', boxLabel: 'Table', name: 'chartType', inputValue: 'Table', handler: this.onTypeChanged, scope: this });

		this.chartContainer = new Ext.Panel({ x: 170, y: 47, width: 420, height: 300, bodyBorder: false, border: true, style: 'border-width: 1px;' });

		var grid = this.createGrid({ x: 170, y: 47, width: 420, height: 300 });
		this.gridContainer = new Ext.Panel({ x: 170, y: 47, width: 420, height: 300, bodyBorder: false, border: true, items: grid, style: 'border-width: 1px;', hidden: true });
		
		this.add(this.chartNameLabel);
		this.add(this.chartName);

		this.add(this.chartTypeLabel);
		this.add(this.barChart);
		this.add(this.columnChart);
		this.add(this.steppedAreaChart);
		this.add(this.lineChart);
		this.add(this.pieChart);
		
		if(this.showTable)
			this.add(this.tableChart);

		this.add(this.chartContainer);
		this.add(this.gridContainer);
		
		this.chartName.on('change', this.onNameChanged, this);
		this.chartContainer.on('afterrender', this.onChartContainerReady, this);
	},
	
	createGrid: function(config) {
		var yearColumn = { dataIndex: 'year', header: 'Год', width: 450 };
		var salesColumn = { dataIndex: 'sales', header: 'Продажи', width: 100 };
		var expendsColumn = { dataIndex: 'expends', header: 'Затраты', width: 100 };
		var selModel = new Ext.grid.CheckboxSelectionModel({ singleSelect: false });
		
		var store = new Ext.data.ArrayStore({ fields: ['year', 'sales', 'expends'], idIndex: 0, data: this.gridData });

		var colModel = new Ext.grid.ColumnModel({
			defaults: { sortable: true },
			columns: [ selModel, yearColumn, salesColumn, expendsColumn ]
		});
		
		config = Ext.apply(config || {}, {
			border: false,
			store: store,
			colModel: colModel,
			viewConfig: { forceFit: true },
		    sm: selModel
		});
		
		return new Ext.grid.GridPanel(config);
	},

	onChartContainerReady: function()
	{
		var type = this.barChart.getGroupValue();
		
		this.chartContainer.setVisible(type != 'Table');
		this.gridContainer.setVisible(type == 'Table');
		
		this.chart.setContainerId(this.chartContainer.body.id);
		this.chart.setOption('title', this.chartName.getValue());
		this.chart.setOption('width', '100%');
		this.chart.setChartType(this.barChart.getGroupValue());
		this.chart.setDataTable(this.data);
		this.chart.draw();
	},
	
	onTypeChanged: function(field, checked)
	{
		if(checked)
		{
			var type = field.inputValue;
			
			this.chartContainer.setVisible(type != 'Table');
			this.gridContainer.setVisible(type == 'Table');

			this.chart.setChartType(type);
			this.chart.draw();
		}
	},

	onNameChanged: function(field, newValue, oldValue)
	{
		this.chart.setOption('title', newValue);
		this.chart.draw();
	},
	
	onDestroy: function()
	{
		Ext.destroy(this.chart);
		delete this.chart;
		
		Z8.view.ChartPreviewPanel.superclass.onDestroy.call(this);
	},
	
	onNext: function()
	{
		this.config.chartType = this.barChart.getGroupValue();
		this.config.chartName = this.chartName.getValue();
		
		return true;
	}
});


Z8.view.ChartDataSourcePanel = Ext.extend(Z8.Panel,
{
	closable: false,
	layout: 'absolute',
	border: false,
	header: false,
	
	initComponent: function()
	{
		Z8.view.ChartDataSourcePanel.superclass.initComponent.call(this);
		
		this.contureLabel = new Ext.form.Label({ text: 'Контур:', x: 10, y: 13 });
		this.conture = new Ext.form.ComboBox({ x: 170, y:10, width: 420, valueField: 'id', displayField: 'name', editable: false, forceSelection: true, mode: 'local', triggerAction: 'all', emptyText: 'Выберите значение...', store: this.createStore(this.contureStoreData()) });

		this.dataSourceLabel = new Ext.form.Label({ text: 'Источник данных:', x: 10, y: 48 });
		this.dataSource = new Ext.form.ComboBox({ x: 170, y:45, width: 320, valueField: 'id', displayField: 'name', editable: false, forceSelection: true, mode: 'local', triggerAction: 'all', emptyText: 'Выберите значение...', store: this.createStore(), disabled: true });

		var dataSource = this.config != null ? this.config.dataSource : null;
		var filterOn = dataSource != null ? dataSource.filterOn : null;
		this.filterButton = new Z8.view.FilterButton({ x: 500, y:43, width: 80, text: 'Фильтр', disabled: true, pressed: filterOn, plain: true, iconCls: null });
		this.filterButton.on('filterToggle', this.onFilterToggle, this);

		this.gridContainer = new Ext.Panel({ x: 10, y: 80, width: 580, height: 263, layout: 'fit', bodyBorder: false, border: true, style: 'border-top-width: 1px; border-left-width: 1px; border-right-width: 1px; border-bottom-width: 0px;' });

		this.add(this.contureLabel);
		this.add(this.conture);
		
		this.add(this.dataSourceLabel);
		this.add(this.dataSource);
		this.add(this.filterButton);

		this.add(this.gridContainer);

		this.conture.on('select', this.onContureChanged, this);
		this.dataSource.on('select', this.onDataSourceChanged, this);
	},

	isLast: function() {
		return this.config.chartType == 'Table';
	},
	
	onActivate: function()
	{
		if(this.config.conture != null && !this.activated)
		{
			this.activated = true;
			
			var index = this.conture.getStore().indexOfId(this.config.conture.id);
			
			if(index != -1)
			{
				this.conture.setValue(this.config.conture.text);
				var record = this.conture.getStore().getAt(index);
				this.onContureChanged(this.conture, record, index);
			}
			else
			{
				this.config.conture = null;
				this.config.dataSource = null;
			}
		}

		this.wizard.nextButton.setDisabled(this.config.conture == null);
	},
	
	onBack: function()
	{
		this.wizard.nextButton.setDisabled(false);
		return true;
	},

	createStore: function(data)
	{
		return new Ext.data.ArrayStore({ fields: ['id', 'name'], idIndex: 0, data: data || [] });
	},

	contureStoreData: function()
	{
		var contures = Z8.viewport.loginInfo.user.components;

		var data = [];
		
		for(var i = 0; i < contures.length; i++)
		{
			data.push([contures[i].id, contures[i].text]);
		}
		
		return data;
	},
	
	onContureChanged: function(comboBox, record, index)
	{
		this.dataSource.getStore().removeAll();
		this.dataSource.setValue(null);
		this.dataSource.addClass('x-panel-busy');
		this.dataSource.setDisabled(false);
		
		this.wizard.nextButton.setDisabled(true);
		
		if(this.grid != null)
		{
			this.grid.destroy();
		}
		
		this.config.conture = { id: record.id, text: record.data.name };

		Z8.Ajax.request(Z8.viewport.loginInfo.requestId, this.onContureLoaded, this.onContureLoadError, { menu: record.id }, this);
	},
	
	onContureLoaded: function(result)
	{
		var data = [];
		
		Ext.iterate(result.data, function(key, value)
		{
			for(var i = 0; i < value.length; i++)
			{
				if(!value[i].isJob)	
				{
					data.push([value[i].id, value[i].text]);
				}
			}
		});
		
		this.dataSource.getStore().loadData(data);
		this.dataSource.removeClass('x-panel-busy');
		this.dataSource.setDisabled(false);
		
		if(this.config.dataSource != null)
		{
			var index = this.dataSource.getStore().indexOfId(this.config.dataSource.id);
			
			if(index != -1)
			{
				this.dataSource.setValue(this.config.dataSource.text);
				var record = this.dataSource.getStore().getAt(index);
				this.onDataSourceChanged(this.dataSource, record, index);
			}
			else
			{
				this.config.dataSource = null;
			}
		}
	},
	
	onContureLoadError: function(info)
	{
		this.dataSource.removeClass('x-panel-busy');

		Z8.showMessages('Z8', Z8.Format.nl2br(info.messages));
	},

	onDataSourceChanged: function(comboBox, record, index)
	{
		if(this.grid != null)
		{
			this.grid.destroy();
		}

		this.gridContainer.addClass('x-panel-busy');

		if(this.config.dataSource == null)
		{
			this.config.dataSource = {};
			
		}
		
		if(this.config.dataSource.id != record.id)
		{
			this.config.dataSource.filterOn = false;
			this.config.dataSource.filter = null;
		}
		
		this.config.dataSource.id = record.id;
		this.config.dataSource.text = record.data.name;

		var filter = this.config.dataSource.filterOn ? this.config.dataSource.filter : null;
		
		var params = 
		{
			filter1: filter != null ? Ext.encode(filter) : null
		};
		
		Z8.Ajax.request(record.id, this.onQueryLoaded, this.onQueryLoadError, params, this);
	},
	
	onQueryLoaded: function(query)
	{
		this.gridContainer.removeClass('x-panel-busy');

		this.grid = createGrid(query, { editable: false, border: false });

		this.gridContainer.add(this.grid);
		this.gridContainer.doLayout();

		this.wizard.nextButton.setDisabled(false);

		this.config.dataSource.query = query;
		
		if(Z8.isEmpty(this.config.chartName))
		{
			this.config.chartName = query.text;
		}
		
		this.filterButton.query = this.config.dataSource.query;
		this.filterButton.filter = this.config.dataSource.filter;
		this.filterButton.toggle(this.config.dataSource.filterOn, true);
		
		this.filterButton.setDisabled(false);
	},
	
	onQueryLoadError: function(info)
	{
		this.gridContainer.removeClass('x-panel-busy');

		Z8.showMessages('Z8', Z8.Format.nl2br(info.messages));
	},
	
	onFilterToggle: function(item, pressed, filter)
	{
		this.config.dataSource.filter = filter;
		this.config.dataSource.filterOn = pressed;
		
		var store = this.grid.getStore();
		store.query.filter1 = pressed ? filter : null;
		store.refresh();
	}
});

Z8.view.ChartAxesPanel = Ext.extend(Z8.Panel,
{
	closable: false,
	layout: 'absolute',
	border: false,
	header: false,
	
	initComponent: function()
	{
		Z8.view.ChartAxesPanel.superclass.initComponent.call(this);
		
		this.xAxesLabel = new Ext.form.Label({ x: 10, y: 13, text: 'Ось аргументов:'});
		this.xAxes = new Ext.form.ComboBox({ x: 170, y:10, width: 420, valueField: 'id', displayField: 'name', editable: false, forceSelection: true, mode: 'local', triggerAction: 'all', emptyText: 'Выберите ось аргументов...', store: this.createStore() });

		this.yAxesLabel = new Ext.form.Label({ x: 10, y: 48, text: 'Оси значений:' });
		this.yAxesDescription = new Ext.form.Label({ x: 10, y: 68, text: 'Выберите одно или несколько полей источника данных для отображения нескольких графиков одновременно.', style: 'font-weight: normal' });
		
		this.yAxesContainer = new Ext.Panel({ x: 10, y: 110, width: 580, height: 235, layout: 'fit', bodyBorder: false, border: true, style: 'border-width: 1px;' });
		this.yAxes = this.createYAxes();
	
		this.add(this.xAxesLabel);	
		this.add(this.xAxes);	

		this.add(this.yAxesLabel);	
		this.add(this.yAxesDescription);	

		this.add(this.yAxesContainer);	
		this.yAxesContainer.add(this.yAxes);
		
		this.xAxes.on('select', this.onXAxesChanged, this);
		this.yAxes.getSelectionModel().on('selectionchange', this.onYAxesChanged, this);
	},

	createYAxes: function()
	{
		var nameColumn = { dataIndex: 'name', header: 'Наименование', width: 450 };
		var typeColumn = { dataIndex: 'type', header: 'Тип', width: 100 };
		var selModel = new Ext.grid.CheckboxSelectionModel({ singleSelect: false });
		var colModel = new Ext.grid.ColumnModel(
		{
			defaults: { sortable: true },
			columns: [selModel, nameColumn, typeColumn]
		});
		
		
		return new Ext.grid.GridPanel(
		{
			border: false,
			store: this.createStore(),
			colModel: colModel,
			viewConfig: { forceFit: true },
		    sm: selModel
		});
	},
	
	createStore: function()
	{
		return new Ext.data.ArrayStore({ fields: ['id', 'name', 'type', 'serverType', 'format'], idIndex: 0, data: [] });
	},
	
	onActivate: function()
	{
		this.loadXStore();
		this.loadYStore();
		
		if(this.config.dataSource.xAxes != null)
		{
			var xAxes = this.config.dataSource.xAxes;
			var store = this.xAxes.getStore();
			var index = store.indexOfId(xAxes.id);
			
			if(index != -1)
			{
				this.xAxes.setValue(xAxes.header);
			}
			else
			{
				this.config.xAxes = null;
			}
		}

		if(this.config.dataSource.yAxes != null)
		{
			var yAxes = this.config.dataSource.yAxes;
			var store = this.yAxes.getStore();
			var records = [];
			
			for(var i = 0; i < yAxes.length; i++)
			{
				var record = store.getById(yAxes[i].id);
			
				if(record != null)
				{
					records.push(record);
				}
				else
				{
					this.config.yAxes = null;
					break;
				}
			}
			
			if(this.config.dataSource.yAxes != null)
			{
				this.yAxes.getSelectionModel().selectRecords(records);
			}
		}
		
		this.updateButtons();
	},
	
	onBack: function()
	{
		this.wizard.nextButton.setDisabled(false);
		return true;
	},
	
	loadXStore: function()
	{
		var data = [];

		var fields = this.config.dataSource.query.getFields();
		
		for(var i = 0; i < fields.length; i++)
		{
			var field = fields[i];
			
			if(field.serverType == Z8.ServerTypes.String || field.serverType == Z8.ServerTypes.Date)
			{
				data.push([field.id, field.header, Z8.GoogleTypes.fromServerType(field.serverType), field.serverType, field.format]);
			}
		}
		
		this.xAxes.getStore().loadData(data);
	},
	
	loadYStore: function()
	{
		var data = [];

		var fields = this.config.dataSource.query.getFields();
		
		for(var i = 0; i < fields.length; i++)
		{
			var field = fields[i];
			
			if(field.serverType == Z8.ServerTypes.Integer || field.serverType == Z8.ServerTypes.Float)
			{
				data.push([field.id, field.header, Z8.GoogleTypes.fromServerType(field.serverType), field.serverType, field.format]);
			}
		}
		
		this.yAxes.getStore().loadData(data);
	},
	
	onXAxesChanged: function(comboBox, record, id)
	{
		this.config.dataSource.xAxes = { id: record.id, header: record.data.name, type: record.data.type, serverType: record.data.serverType, format: record.data.format };
		this.updateButtons();
	},
	
	onYAxesChanged: function()
	{
		var yAxes = [];
		var selected = this.yAxes.getSelectionModel().getSelections();
		
		for(var i = 0; i < selected.length; i++)
		{
			var record = selected[i];
			yAxes.push({ id: record.id, header: record.data.name, type: record.data.type, serverType: record.data.serverType, format: record.data.format });
		}
		
		this.config.dataSource.yAxes = yAxes.length != 0 ? yAxes : null;
		
		this.updateButtons();
	},
	
	updateButtons: function()
	{
		this.wizard.nextButton.setDisabled(this.config.dataSource.xAxes == null || this.config.dataSource.yAxes == null);
	}
});

