Ext.util.Format.comboRenderer = function(combo){
    return function(value){
        var record = combo.findRecord(combo.valueField, value);
        return record ? record.get(combo.displayField) : combo.valueNotFoundText;
    }
}

Z8.view.ImportGrid = Ext.extend(Ext.grid.EditorGridPanel, {
	
    autoHeight : false,
    border: true,
    clicksToEdit: 1,
	
	initComponent: function()
	{
	
		this.externalCombo = new Ext.form.ComboBox({
			tpl: new Ext.XTemplate(
					 "<tpl for=\".\">",
						"<tpl if=\"[xindex] == 1\">",
							"<table class=\"cbImport-list\">",
								"<tr>",
									"<th>Наименование</th>",
									"<th>Тип</th>",
								"</tr>",
						"</tpl>",
						"<tr class=\"list-item\">",
							"<td style=\"padding:3px 0px;\">{name}</td>",
							"<td>{type}</td>",
						"</tr>",
						"<tpl if=\"[xcount-xindex]==0\">",
							"</table>",
						"</tpl>",
					"</tpl>"
		    ),
		    emptyText: 'Выберите значение...',
		    displayField: 'name',
		    mode: 'local',
		    triggerAction: 'all',
		    valueField: 'id',
		    store: new Ext.data.ArrayStore({
		        autoDestroy: true,
		        idIndex: 0,  
		        fields: ['id', 'name', 'type'],
		        data: this.externalData
		    }),
		    submitValue: true,
		    itemSelector: 'tr.list-item',
	        listWidth: 300
		});

    	this.externalCombo.ownerCt = this; // нужно, чтобы правильно проставлялся z-index у выпадающего списка
    	
    	this.externalCombo.on('select', this.stopEditing, this);
		
		var config = {};
        this.buildConfig(config);
        Ext.apply(this, Ext.apply(this.initialConfig, config));

        Z8.view.ImportGrid.superclass.initComponent.call(this);
		
		// Set combo default falue
		this.on('viewready', function(grid)
		{
			var store = grid.getStore();
			store.each(function(record, index) {
				Ext.each(grid.externalData, function(data) {
					if(String(data[1]).toLowerCase() == String(record.data.name).toLowerCase()) {
						record.set('mapped', data[0]);
					}
				});
			});
			
			grid.view.on('rowUpdated', function(view)
			{
				view.updateAllColumnWidths();
			});
			
			grid.view.updateAllColumnWidths();
		});
    },
    
    findRecById: function(id)
    {
    	var index = Ext.StoreMgr.lookup(this.store).findExact('id',id);
    	var rec = Ext.StoreMgr.lookup(this.store).getAt(index);
    	
    	return {index: index, rec: rec};
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
    		data: this.data,
            fields: [{name: 'id'}, {name: 'name'}, {name: 'type'}, {name: 'query'}, {name: 'mapped'}, {name: 'keyfield', type: 'bool'}]
    	}, this);
    },
    
    buildColumns: function(config)
    {
        config.columns = [
           //{header: 'ID', width: 75, sortable: true, dataIndex: 'id'},
           {header: 'Наименование', width: 100, sortable: true, dataIndex: 'name', updateWidth: true },
           {header: 'Источник', width: 100, sortable: true, dataIndex: 'query', updateWidth: true },
           {header: 'Тип', width: 30, sortable: true, dataIndex: 'type', updateWidth: true },
           {
        	   header: 'Значение Excel',
               dataIndex: 'mapped',
               width: 130,
               editor: this.externalCombo, 
               renderer: Ext.util.Format.comboRenderer(this.externalCombo),
               updateWidth: true
           },
           {
               xtype: 'checkcolumn',
               header: 'Ключевое поле',
               dataIndex: 'keyfield',
               width: 55
           }
        ];
    },
    
    buildView: function(config)
    {
    	config.view = new Ext.grid.GridView({forceFit: true});
    }
});

Z8.view.ImportWindow = Ext.extend(Z8.Window, {
	
	title: 'Импорт',
	width: 800,
	height: 600,
	layout: 'border',

	initComponent: function()
	{
		this.importButton = new Z8.Button({
			tooltip: 'Начать импорт',
			iconCls: 'icon-import',
			iconAlign: 'top',
			text: 'Начать импорт',
			scope: this,
			width: 140,
			handler: this.onDoImport
		});
	
		this.cancelButton = new Z8.Button({
			tooltip: 'Отмена',
			iconCls: 'icon-close',
			iconAlign: 'top',
			text: 'Отмена',
			scope: this,
			width: 140,
			handler: this.close
		});
		
		this.progressBar = new Ext.ProgressBar({
			width: 300,
			hidden: true,
			align: 'left'
		});
		
		this.tabPanel = this.buildTabPanel();
		
		this.reportPanel = new Z8.view.ReportPanel({ split: true, collapseMode: 'mini', layout: 'fit', region: 'east', width: 200});

	
		var config = {
			items: [{
				layout: 'fit', border: false, region: 'center', items: this.tabPanel
			}, this.reportPanel],
				
			fbar: [this.progressBar, this.cancelButton, this.importButton]
		};
		
		// apply config
		Ext.apply(this, Ext.apply(this.initialConfig, config));

		Z8.view.ImportWindow.superclass.initComponent.call(this);
	},
	
	onDoImport: function()
	{
		this.progressBar.show();
		this.importButton.setBusy(true);
		this.cancelButton.disable();
		
		var activeTab = this.tabPanel.getActiveTab();
		var grid = activeTab.items.get(0);
		var store = grid.getStore();
		
		var mapped = [];
		
		store.each(function(record)
		{
			if(!Z8.isEmpty(record.data.mapped))
			{
				var type = '';
				
				for(var i = 0; i < grid.externalData.length; i++)
				{
					if(grid.externalData[i][0] == record.data.mapped)
					{
						type = grid.externalData[i][2];
						break;
					}
				}
				
				mapped.push({ source: record.data.mapped, target: record.data.id, type: type, key: record.data.keyfield });
			}
		});
		
		var parameters = {};
		
		parameters.source = activeTab.id;
		parameters.fields = Ext.encode(mapped);
		parameters.serverId = this.result.serverId;
		parameters.files = Ext.encode(this.result.files);
		parameters.xaction = 'importRun';
	
		if(this.result.queryId != null)
		{
			parameters.queryId = this.result.queryId;
		}

		if(this.result.recordId != null)
		{
			parameters.recordId = this.result.recordId;
		}
		
		Z8.Ajax.request(this.result.requestId, this.onImportProc, Ext.emptyFn, parameters, this);
	},
	
	onImportProc: function(job)
	{
		this.reportPanel.clear();
		
		var provider = new Ext.direct.PollingProvider({
			type:'polling',
			url: Z8.request.url,
			baseParams: { sessionId: Z8.sessionId, jobId: job.jobId }
		});
		
		Ext.Direct.addProvider(provider);
		
		provider.on('data', this.processImport, this);
		
		this.processImport(provider, job);
	},
	
	processImport: function(provider, event)
	{
		if(event.type == 'event')
		{
			if(event.success)
			{
				var total = event.total != null && event.total != 0 ? event.total : 100.0;
				var worked = event.worked != null ? event.worked : 0.0;
				var progress = (1.0 * worked) / total;
				
				this.progressBar.updateProgress((progress).toFixed(1), (progress * 100).toFixed(1) + '%');
			}

			if(event.done || !event.success)
			{
				provider.disconnect();
				this.importButton.setBusy(false);
				this.importButton.setText('Повторить импорт');
				this.cancelButton.enable();
			}
	
			this.reportPanel.onMessage(null, event.info);
			this.fireEvent('importDone');
		}
	},
	
	onClose: function()
	{
		this.close();
	},
	
	buildTabPanel: function()
	{
		var internalFields = this.result.internal.fields;
		var externalFields = this.result.external.queries.reverse();
		var tabs = [];
		
		Ext.each(externalFields, function(query)
		{
			
			var targetFields = [];
			Ext.each(internalFields, function(field)
			{
				targetFields.push([field.id, field.name, field.type, field.query, null, false]);
			}, this);
			
			var sourceFields = [];
			
			Ext.each(query.fields, function(field){
				sourceFields.push([field.id, field.name, field.type]);
			});
			
			var grid = new Z8.view.ImportGrid({data: targetFields, externalData: sourceFields});
			
			tabs.push({ title: query.name, id: query.id, items: grid, layout: 'fit' });
		}, this);
	
		return tabPanel = new Ext.TabPanel({ activeTab: 0, items: tabs });
	}
});