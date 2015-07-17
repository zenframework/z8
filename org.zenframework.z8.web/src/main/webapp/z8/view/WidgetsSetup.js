Z8.view.WidgetsCombo = Ext.extend(Ext.form.ComboBox, {
	store: new Ext.data.ArrayStore({
		fields: ['id', 'name'],
		data: this.buildData
	}),
	valueField: 'id',
	displayField: 'name',
	typeAhead: true,
	mode: 'local',
	triggerAction: 'all',
	emptyText: 'Выберите значение...',
	selectOnFocus: true,
	width: 245,
	
	initComponent: function() {

        Ext.apply(this, {
            store: this.createStore()
        });

        Z8.view.WidgetsCombo.superclass.initComponent.apply(this, arguments);
    },
    
    createStore: function()
    {
        return new Ext.data.ArrayStore({
    		fields: ['id', 'name'],
    		data: this.buildData()
    	});
	},
	
	buildData: function()
	{
		return [];
	}
});

Z8.view.WidgetsMenuCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'Контур',
	hiddenName: 'kontur',
	
	buildData: function()
	{
		var menuItems = Z8.viewport.loginInfo.user.components;
		var items = [];
		Ext.each(menuItems, function(item, index) {
			items.push([item.id, item.text]);
		});
		
		return items;
	}
});

Z8.view.WidgetsSubMenuCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'Форма',
	hiddenName: 'form',
	
	initComponent: function() {
		Z8.view.WidgetsSubMenuCombo.superclass.initComponent.apply(this, arguments);
		this.addEvents('formloaded');
    },
    
    loadFormData: function(id, xValue)
    {
    	Z8.Ajax.request(id, this.onFormDataLoaded.createDelegate(this, [xValue], true), Ext.emptyFn, {}, this);
    },
    
    onFormDataLoaded: function(response, xValue)
    {
    	this.fireEvent('formloaded', response, xValue);
    },
	
	loadData: function(id, value)
	{
		this.disable();
		Z8.Ajax.request(Z8.viewport.loginInfo.requestId, this.onLoadData.createDelegate(this, [id, value], true), Ext.EmptyFn, { menu: id }, this);
	},
	
	onLoadData: function(result, id, value)
	{
		var menu = new Z8.desktop.MenuItem({menuContent: result.data});
		var menuItems = menu.generateMenuItems(id, Z8.getUserSettings());
		
		var items = [];
		
		Ext.each(menuItems, function(item, index){
			if(item.type == 'block'){
				//items.push([null, item.text]);
			} else {
				items.push([item.key, item.text]);
			}
		});
		
		this.clearValue();
		this.enable();
		
		this.store.loadData(items, false);
		
		if (value) {
			this.setValue(value);
		}
	}
});

Z8.view.WidgetsChartDisplayCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'Отображение',
	hiddenName: 'chartdisplay',
	
	buildData: function()
	{
		return [['horisontal', 'Горизонтальное'], ['vertical', 'Вертикальное']];
	}
});

Z8.view.WidgetsChartTypeCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'Тип графика',
	hiddenName: 'charttype',
	
	buildData: function()
	{
		return [['linebar', 'Линейно-столбчатый'], ['radar', 'Радио'], ['rectangle', 'Распределение'], ['stacked', 'Стыковка']];
	}
});

Z8.view.WidgetsDataCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'Поле',
	
	loadData: function(fields)
	{
		var items = [];
		
		Ext.each(fields, function(field, index){
			items.push([field.id, field.header]);
		});
	
		this.clearValue();
		this.store.loadData(items, false);
	}
});

Z8.view.WidgetsYAgregateCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'Агрегация',
	
	buildData: function()
	{
		return [['sum', 'По сумме']];
	}
});

Z8.view.WidgetsYChartTypeCombo = Ext.extend(Z8.view.WidgetsCombo, {
	fieldLabel: 'График',
	
	buildData: function()
	{
		return [['line', 'Линейный'], ['bar', 'Столбчатый']];
	}
});

Z8.view.WidgetsFieldset = Ext.extend(Ext.form.FieldSet, {
	collapsible: true,
	autoHeight:true
});

Z8.view.WidgetsYDataview = Ext.extend(Ext.DataView, {

	autoHeight: true,
	multiSelect: true,
	singleSelect: false,
	frame: true,
	itemSelector: 'li.x-view-plainlist-wrap',
	overClass: 'x-view-over',
    
    initComponent: function() {

        Ext.apply(this, {
            store: this.createStore(),
            tpl: this.createTpl(),
            listeners: {
    			click: this.onItemClick,
    			scope: this
        	}
        });

        Z8.view.WidgetsYDataview.superclass.initComponent.apply(this, arguments);
    },
    
    createTpl: function() {
    	return [
    			'<ul class="x-view-plainlist">',
    			'<tpl for=".">', 
    				'<li class="x-view-plainlist-wrap">',
    					'{caption}',
    					'<span class="remove"></span>',
    				'</li>',
    			'</tpl>',
    		'</ul>'
    	];
    },
    
    createStore: function() {
    	return new Ext.data.ArrayStore({
    		fields: ['id', 'caption', 'agregation', 'type'],
    		data: this.buildData()
    	});
    },
    
    buildData: function() {
    	return [];
    },
    
    onItemClick: function(view, index, node, e) {
    	if (e && e.getTarget('span.remove')) {
    		view.getStore().removeAt(index);
    	}
    },
    
    loadData: function(axes)
	{
		var items = [];
		
		Ext.each(axes, function(axe, index){
			items.push([axe.field, axe.caption, axe.agregation, axe.type]);
		});
		
		this.store.loadData(items, false);
	}
});

Z8.view.WidgetsGroupDataview = Ext.extend(Ext.DataView, {

	autoHeight: true,
	multiSelect: true,
	singleSelect: false,
	frame: true,
	itemSelector: 'li.x-view-plainlist-wrap',
	overClass: 'x-view-over',
    
    initComponent: function() {

        Ext.apply(this, {
            store: this.createStore(),
            tpl: this.createTpl(),
            listeners: {
    			click: this.onItemClick,
    			scope: this
        	}
        });

        Z8.view.WidgetsYDataview.superclass.initComponent.apply(this, arguments);
    },
    
    createTpl: function() {
    	return [
    			'<ul class="x-view-plainlist">',
    			'<tpl for=".">', 
    				'<li class="x-view-plainlist-wrap">',
    					'{name}',
    					'<span class="remove"></span>',
    				'</li>',
    			'</tpl>',
    		'</ul>'
    	];
    },
    
    createStore: function() {
    	return new Ext.data.ArrayStore({
    		fields: ['id', 'name'],
    		data: this.buildData()
    	});
    },
    
    buildData: function() {
    	return [];
    },
    
    onItemClick: function(view, index, node, e) {
    	if (e && e.getTarget('span.remove')) {
    		view.getStore().removeAt(index);
    	}
    },
    
    loadData: function(data) {
		var items = [];
		
		Ext.each(data, function(item, index){
			items.push([item.field, item.name]);
		});
		
		this.store.loadData(items, false);
	}
});

Z8.view.WidgetsChartForm = Ext.extend(Ext.FormPanel, {
	
    frame:true,
    bodyStyle: 'padding:5px 5px 0',
    autoScroll: true,
    
    initComponent: function()
	{
		this.nameField = new Ext.form.TextField({fieldLabel: 'Название', name: 'name', anchor:'90%' });
		this.idField = new Ext.form.Hidden({name: 'id'});
		
		// Комбобокс выбора контура
		this.menuCombo = new Z8.view.WidgetsMenuCombo({anchor:'90%'});
		this.menuCombo.on('beforeselect', this.onMenuComboSelect, this);
		
		// Комбобокс выбора формы
		this.subMenuCombo = new Z8.view.WidgetsSubMenuCombo({anchor:'90%'});
		this.subMenuCombo.on('beforeselect', this.onSubMenuComboSelect, this);
		this.subMenuCombo.on('formloaded', this.onFormLoaded, this);
		
		// Комбобокс выбора типа графика
		this.chartTypeCombo = new Z8.view.WidgetsChartTypeCombo({anchor:'90%'});
		
		this.chartDisplayCombo = new Z8.view.WidgetsChartDisplayCombo({anchor:'90%'});
		
		this.dataFieldSet = new Z8.view.WidgetsFieldset({title: 'Источники данных', items :[this.menuCombo, this.subMenuCombo]});
		
		//Комбобокс выбора оси X
		this.xDataCombo = new Z8.view.WidgetsDataCombo({hiddenName: 'x', anchor:'90%'});
		this.xDataCombo.on('beforeselect', this.onXAxeSelect, this);
		this.xCaption = new Ext.form.TextField({fieldLabel: 'Название', name: 'xCaption', anchor:'90%' });
		
		this.xFieldSet = new Z8.view.WidgetsFieldset({title: 'Ось X',
			items: [{
				layout:'column',
				items:[{
					columnWidth:.50, layout: 'form', items: [this.xDataCombo]
				},{
					columnWidth:.50, layout: 'form', items: [this.xCaption]
				}]
			}]
		});
		
		// Комбобокс выбора группировки
		this.groupCombo = new Z8.view.WidgetsDataCombo({hiddenName: 'group', anchor:'90%'});
		this.groupDataView = new Z8.view.WidgetsGroupDataview({height: 150});
		this.addGroupBtn = new Z8.Button({ width: 100, cls: 'x-widget-addaxis-btn', iconCls: 'icon-add', text: 'Добавить поле', scope: this, handler: this.onAddGroup });
		this.groupFieldSet = new Z8.view.WidgetsFieldset({title: 'Группировка',
			items: [{
				layout:'column',
				items:[{
					columnWidth:.70, layout: 'form', items: [this.groupCombo]
				},{
					columnWidth:.30, layout: 'form', items: [this.addGroupBtn]
				}]
			}, this.groupDataView]
		});
		
		// Комбобокс выбора оси Y
		this.yDataCombo = new Z8.view.WidgetsDataCombo({hiddenName: 'y', anchor:'90%'});
		this.yDataCombo.on('beforeselect', this.onYAxeSelect, this);
		
		this.yAgregateCombo = new Z8.view.WidgetsYAgregateCombo({hiddenName: 'yAgr', anchor:'90%'});
		this.yChartTypeCombo = new Z8.view.WidgetsYChartTypeCombo({hiddenName: 'yChartType', anchor:'90%'});
		this.yCaption = new Ext.form.TextField({fieldLabel: 'Название', name: 'yCaption', anchor:'90%' });
		this.addDataBtn = new Z8.Button({ width: 100, cls: 'x-widget-addaxis-btn', iconCls: 'icon-add', text: 'Добавить ось', scope: this, handler: this.onAddChartY });
		
		// Список выбранных осей Y
		this.yDataView = new Z8.view.WidgetsYDataview({height: 150});
		
		this.yFieldSet = new Z8.view.WidgetsFieldset({title: 'Ось Y',
			items: [{
				layout:'column',
				items:[{
					columnWidth:.50, layout: 'form', items: [this.yDataCombo, this.yChartTypeCombo]
				},{
					columnWidth:.50, layout: 'form', items: [this.yAgregateCombo, this.yCaption, this.addDataBtn]
				}]
			}, this.yDataView]
		});
		
		// create config object
		var config = {
			labelWidth: 100,
			items: [
		        this.nameField,
		    	this.idField,
		        this.chartTypeCombo,
		        this.chartDisplayCombo,
		        this.dataFieldSet,
		        this.xFieldSet,
		        this.groupFieldSet,
		        this.yFieldSet
			]
		};
	
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.WidgetsChartForm.superclass.initComponent.call(this);
		
		this.addEvents('saved');
	},
	
	onSave: function() {
		var formValues = this.getForm().getFieldValues();
		
		var yAxes = [];
		var yAxesStore = this.yDataView.getStore();
		yAxesStore.each(function(record) {
			yAxes.push({
				field: record.data.id,
				caption: record.data.caption,
				agregation: record.data.agregation,
				type: record.data.type
			});
		});
		
		var groups = [];
		var groupStore = this.groupDataView.getStore();
		groupStore.each(function(record) {
			groups.push({
				field: record.data.id,
				name: record.data.name
			});
		});
		
		var id = formValues.id;
		
		var chartObject = {
			id: id ? id : new Ext.ux.UUID().id,
			kontur: formValues.kontur,
			form: formValues.form,
			name: formValues.name,
			charttype: formValues.charttype,
			chartdisplay: formValues.chartdisplay ? formValues.chartdisplay : 'horisontal',
			x: {
				field: formValues.x,
				caption: formValues.xCaption
			},
			y: yAxes,
			groups: groups
		};
		
		var settings = Z8.getUserSettings();
		
		if ( ! settings.charts) {
			settings.charts = [chartObject];
		} else {
			if (id) {
				Ext.each(settings.charts, function(chart, index) {
					if (chart.id == id) {
						settings.charts.splice(index, 1, chartObject);
					}
				});
			} else {
				settings.charts.push(chartObject);
			}
		}
		
		Z8.Ajax.request('settings', this.onChartAdded.createDelegate(this, [chartObject]), Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},
	
	onChartAdded: function(chartObject) {
		this.fireEvent('saved', chartObject);
	},
	
	onAddGroup: function(button) {
		var formValues = this.getForm().getFieldValues();
		var groupStore = this.groupCombo.getStore();
		var rec = groupStore.getAt(groupStore.find('id', formValues.group));
		var store = this.groupDataView.getStore();
		var errors = false;
		
		if (Z8.isEmpty(formValues.group)) {
			this.groupCombo.markInvalid('Необходимо указать поле для группировки');
			errors = true;
		} else {
			this.groupCombo.clearInvalid();
		}
		
		if (store.find('id', formValues.group) !== -1) {
			Z8.MessageBox.show({
				 title: 'Ошибка',
				 msg: 'Поле уже задано для группировки',
				 buttons: Ext.Msg.OK,
				 icon: Ext.MessageBox.ERROR,
				 width: 275
			});
			
			errors = true;
		}
		
		if (errors == false)
		{
			var record = new store.recordType({
				id: formValues.group,
				name: rec.data.name
			});
			
			store.add(record);
		}
	},
	
	onAddChartY: function(button) {
		
		var formValues = this.getForm().getFieldValues();
		var errors = false;
		
		// Validation Y fields
		if (Z8.isEmpty(formValues.y)) {
			this.yDataCombo.markInvalid('Необходимо указать поле Y');
			errors = true;
		} else {
			this.yDataCombo.clearInvalid();
		}
		if (Z8.isEmpty(formValues.yCaption)) {
			this.yCaption.markInvalid('Необходимо указать название');
			errors = true;
		} else {
			this.yCaption.clearInvalid();
		}
		if (Z8.isEmpty(formValues.yChartType)) {
			this.yChartTypeCombo.markInvalid('Необходимо указать тип графика');
			errors = true;
		} else {
			this.yChartTypeCombo.clearInvalid();
		}
		if (Z8.isEmpty(formValues.yAgr)) {
			this.yAgregateCombo.markInvalid('Необходимо указать тип агрегации');
			errors = true;
		} else {
			this.yAgregateCombo.clearInvalid();
		}
		
		var store = this.yDataView.getStore();
		
		if (store.find('id', formValues.y) !== -1) {
			Z8.MessageBox.show({
				 title: 'Ошибка',
				 msg: 'Нельзя добавить одну ось на график дважды',
				 buttons: Ext.Msg.OK,
				 icon: Ext.MessageBox.ERROR,
				 width: 275
			});
			
			errors = true;
		}
		
		if (errors == false)
		{
			var record = new store.recordType({
				id: formValues.y,
				caption: formValues.yCaption,
				agregation: formValues.yAgr,
				type: formValues.yChartType
			});
			
			store.add(record);
		}
	},
	
	onYAxeSelect: function(combo, record, index) {
		this.yCaption.setValue(record.data.name);
	},
	
	onXAxeSelect: function(combo, record, index) {
		this.xCaption.setValue(record.data.name);
	},
	
	onMenuComboSelect: function(combo, record, index) {
		this.subMenuCombo.loadData(record.data.id);
	},
	
	onSubMenuComboSelect: function(combo, record, index) {
		this.subMenuCombo.loadFormData(record.data.id);
	},
	
	onFormLoaded: function(response, xValue) {
		
		var yFields = [];
		var xFields = [];
		
		Ext.each(response.fields, function(field) {

			if (field.aggregation && field.aggregation != 'max' && field.serverType != 'string') {
				yFields.push(field);
			} else {
				if (field.serverType != 'guid') {
					xFields.push(field);
				}
			}
		});
		
		this.yDataCombo.loadData(yFields);
		this.groupCombo.loadData(xFields);
		this.xDataCombo.loadData(xFields);
		
		if (xValue) {
			this.xDataCombo.setValue(xValue);
		}
	},
	
	resetValues: function() {
		this.getForm().reset();
		this.groupDataView.getStore().removeAll();
		this.yDataView.getStore().removeAll();
	},
	
	loadData: function(chartId) {
		var chartData;
		var settings = Z8.getUserSettings();
		
		Ext.each(settings.charts, function(chart) {
			if(chart.id == chartId) {
				chartData = chart;
			}
		});
		
		if (chartData)
		{
			this.nameField.setValue(chartData.name);
			this.idField.setValue(chartData.id);
			
			this.chartTypeCombo.setValue(chartData.charttype);
			this.menuCombo.setValue(chartData.kontur);

			this.subMenuCombo.loadData(chartData.kontur, chartData.form);
			
			this.subMenuCombo.loadFormData(chartData.form, chartData.x.field);
			this.xCaption.setValue(chartData.x.caption);
			
			this.yDataView.loadData(chartData.y);
			
			this.groupDataView.loadData(chartData.groups);
		}
	}
});

Z8.view.WidgetsChartGrid = Ext.extend(Ext.grid.GridPanel, {
	
    autoHeight : false,
    border: true,
	
	initComponent: function() {

		var config = {};
        this.buildConfig(config);
        Ext.apply(this, Ext.apply(this.initialConfig, config));

        Z8.view.WidgetsChartGrid.superclass.initComponent.call(this);
        
        this.addEvents('selected');
    },
    
    buildConfig:function(config)
    {
        this.buildStore(config);
        this.buildColumns(config);
        this.buildView(config);
    },
    
    buildStore: function(config)
    {	
    	config.store =  new Ext.data.ArrayStore({
    		data: this.buildData(),
            fields: [{name: 'id'}, {name: 'name'}]
    	}, this);
    },
    
    buildColumns: function(config)
    {
        config.columns = [
           {header: 'Название виджета', dataIndex: 'name'}
        ];
    },
    
    buildData: function() {
    	
    	var data = [];
    	var settings = Z8.getUserSettings();
    	
    	if (settings.charts)
    	{
    		Ext.each(settings.charts, function(chart){
    			data.push([chart.id, chart.name]);
    		});
    	}
    	
    	return data;
    },
    
    reloadStore: function()
    {
    	var store = this.getStore(); 
    	store.loadData(this.buildData(), false);
    },
    
    buildView: function(config)
    {
    	config.view = new Ext.grid.GridView({forceFit: true});
    }
});

Z8.view.WidgetSetupPanel = Ext.extend(Z8.Panel, {
	layout: 'card',
	activeItem: 0,
	
	initComponent: function()
	{
		this.widgetsGrid = new Z8.view.WidgetsChartGrid();
		this.widgetsGrid.on('rowclick', this.onGridRowClick, this);
		
		this.widgetsForm = new Z8.view.WidgetsChartForm();
		this.widgetsForm.on('saved', this.onChartSaved, this);
		
		// create config object
		var config = {
			items: [this.widgetsGrid, this.widgetsForm]
		};
	
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.WidgetSetupPanel.superclass.initComponent.call(this);
		
		this.initToolbar();
	},
	
	initToolbar: function()
	{
		this.addBtn = new Z8.Button({ plain: true, text: 'Добавить', tooltip: 'Добавить виджет', iconCls: 'icon-add', iconAlign: 'top', handler: this.onAddClick, scope: this });
		this.toolbarItems.push(this.addBtn);
		
		this.editBtn = new Z8.Button({ plain: true, text: 'Изменить', tooltip: 'Редактировать виджет', disabled: true, iconCls: 'icon-edit', iconAlign: 'top', handler: this.onEditClick, scope: this });
		this.toolbarItems.push(this.editBtn);
		
		this.deleteBtn = new Z8.Button({ plain: true, text: 'Удалить', tooltip: 'Удалить виджет', disabled: true, iconCls: 'icon-delete', iconAlign: 'top', handler: this.onDeleteClick, scope: this });
		this.toolbarItems.push(this.deleteBtn);
		
		this.tableBtn = new Z8.Button({ plain: true, text: 'Таблица', tooltip: 'Таблица', hidden: true, iconCls: 'icon-table', iconAlign: 'top', handler: this.onTableShow, scope: this });
		this.toolbarItems.push(this.tableBtn);
		
		this.saveBtn = new Z8.Button({ plain: true, text: 'Сохранить', tooltip: 'Сохранить виджет', hidden: true, iconCls: 'icon-save', iconAlign: 'top', handler: this.onSaveClick, scope: this });
		this.toolbarItems.push(this.saveBtn);
	},
	
	onAddClick: function()
	{
		this.widgetsForm.resetValues();
		this.onFormShow();
	},
	
	onEditClick: function()
	{
		this.widgetsForm.resetValues();
		this.onFormShow();
		
		var sm = this.widgetsGrid.getSelectionModel();
		var rec = sm.getSelected();
		
		this.widgetsForm.loadData(rec.data.id);
	},
	
	onSaveClick: function()
	{
		this.widgetsForm.onSave();
	},
	
	onChartSaved: function(chartObject)
	{
		this.widgetsGrid.reloadStore();
		this.onTableShow();
	},
	
	onFormShow: function()
	{
		this.layout.setActiveItem(1);
		
		this.addBtn.hide();
		this.editBtn.hide();
		this.deleteBtn.hide();
		
		this.tableBtn.show();
		this.saveBtn.show();
		
		this.toolbar.doLayout();
	},
	
	onTableShow: function()
	{
		this.layout.setActiveItem(0);
		
		this.saveBtn.hide();
		this.tableBtn.hide();
		
		this.addBtn.show();
		this.editBtn.show();
		this.deleteBtn.show();
		
		this.toolbar.doLayout();
	},
	
	onGridRowClick: function(grid, rowIndex, e)
	{
		this.editBtn.enable();
		this.deleteBtn.enable();
	},
	
	onDeleteClick: function(button)
	{
		this.deleteBtn.setBusy(true);
		var sm = this.widgetsGrid.getSelectionModel();
		var rec = sm.getSelected();
		
		var settings = Z8.getUserSettings();
		var charts = settings.charts;
		
		Ext.each(charts, function(chart, index) {
			if (chart)
			{
				if (rec.data.id == chart.id) {
					charts.splice(index, 1);
				}
			}
		});
		
		settings.charts = charts;
		Z8.Ajax.request('settings', this.onChartDeleted.createDelegate(this, [rec, sm]), Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},
	
	onChartDeleted: function(record, sm)
	{
		this.widgetsGrid.getStore().remove(record);
		this.deleteBtn.setBusy(false);
		sm.selectFirstRow();
	}
});

Z8.view.WidgetsDashBoardSelectForm = Ext.extend(Ext.FormPanel, {
	
    frame:true,
    bodyStyle: 'padding:5px 5px 0',
    
    initComponent: function()
	{
		this.dasboardView = new Z8.view.WidgetsDashBoardSelectView();
		
		// create config object
		var config = {
			items: [this.dasboardView],
			buttons: [{
				text: 'Сохранить вид',
				handler: this.onSave,
				scope: this
			}]
		};
	
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.WidgetsDashBoardSelectForm.superclass.initComponent.call(this);
	},
	
	onSave: function()
	{
		var settings = Z8.getUserSettings();
		
		settings.dashboardType = this.dasboardView.dashboardType;
		
		Z8.Ajax.request('settings', this.onDashboardTypeSaved, Ext.emptyFn, { data: Ext.encode(settings) }, this);
	},
	
	onDashboardTypeSaved: function()
	{
		
	}
});

Z8.view.WidgetsDashBoardSelectView = Ext.extend(Ext.DataView, {

	id: 'dashboardselect-view',
	autoHeight:true,
    multiSelect: true,
    overClass:'x-view-over',
    itemSelector:'div.thumb-wrap',
    emptyText: 'No images to display',
    
    dashboardType: null,
    
    initComponent: function() {

        Ext.apply(this, {
            store: this.createStore(),
            tpl: this.createTpl(),
            listeners: {
            	selectionchange: this.onSelectionChange,
            	scope: this
            }
        });

        Z8.view.WidgetsDashBoardSelectView.superclass.initComponent.apply(this, arguments);
    },
    
    createTpl: function() {
    	return new Ext.XTemplate(
    		'<tpl for=".">',
    			'<div class="thumb-wrap" id="{id}">',
    				'<div class="thumb"><img src="/resources/images/z8/ui/{id}.png" title="{name}"></div>',
    				'<span>{name}</span>',
    			'</div>',
    		'</tpl>',
    	    '<div class="x-clear"></div>'
    	);
    },
    
    createStore: function() {
    	return new Ext.data.ArrayStore({
    		fields: ['id', 'name'],
    		data: this.buildData()
    	});
    },
    
    buildData: function() {
    	return [
            ['3-column-regular-dashboard', '3 column regular dashboard'],
            ['3-column-multyfocused-dashboard', '3 column multyfocused dashboard'],
            ['2-column-regular-dashboard', '2 column regular dashboard'],
            ['3-column-overview-dashboard', '3 column overview dashboard']
    	];
    },
    
    onSelectionChange: function(dv, nodes) {
    	var rec = this.getRecord(nodes[0]);
    	this.dashboardType = rec.data.id;
	}
});

Z8.view.WidgetsSetup = Ext.extend(Z8.Window, {
	
	title: 'Настройка виджетов',
	width: 800,
	height: 600,
	modal: true,
	cls: 'z8-profile',
	
	initComponent: function()
	{
		this.chartSetupPanel = new Z8.view.WidgetSetupPanel();
		this.dashboardSelect = new Z8.view.WidgetsDashBoardSelectForm();
		
		this.tabpanel = new Ext.TabPanel({
	        activeTab: 0,
	        plain: true,
	        border: false,
	        defaults:{autoScroll: true},
	        items:[{
	        	title: 'Список виджетов',
	        	layout: 'fit',
	        	items: this.chartSetupPanel
	        },{
	        	title: 'Настройки рабочего стола',
	        	layout: 'fit',
	        	items: this.dashboardSelect
	        }]
	    });
		
		// create config object
		var config = {
			layout: 'fit',
			items:[this.tabpanel]
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.UserSettings.superclass.initComponent.call(this);
	},
	
	onClose: function()
	{
		this.hide();
	}
});