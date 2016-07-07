Ext.apply(Z8.Console, 
{
	jobs: new Ext.util.MixedCollection(),
	
	getJob: function(jobId) {
		return this.jobs.get(jobId);
	},
	
	start: function(job, parameters) {
		job.parameters = parameters;
		
		if(this.getJob(job.jobId) == null)
			this.jobs.add(job.jobId, job);
		
		var id = 'console';
		
		if(this.view == null) {
			this.view = new Z8.console.Panel({flex:1, data: this.prepareData()});
			Z8.TaskManager.register(this.view, id);
		} else {
			var view = Z8.TaskManager.getTask(id);
			Z8.TaskManager.activate(view);
			view.jobsGrid.store.loadData(this.prepareData(), false);
		}
		
		this.createPolling(job);
		
		this.view.on('destroyed', this.onViewDestroyed, this);
	},
	
	getProgressAsString: function(job) {
		try {
			return ((job.progress || 0.0) * 100).toFixed(1) + '%';
		} catch(e) {
			return '0%';
		}
	},
	
	prepareData: function() {
		var data = [];
		
		this.jobs.each(function(job) {
			var params = '';
			data.push([job.jobId, job.text, params, this.getProgressAsString(job)]);
		}, this);
		
		return data;
	},
	
	createPolling: function(job) {
		var provider = new Ext.direct.PollingProvider({
			type:'polling',
			url: Z8.request.url,
			baseParams: { sessionId: Z8.sessionId, jobId: job.jobId }
		});
		
		Ext.Direct.addProvider(provider);
		
		provider.on('data', this.processEvent, this);
		
		this.processEvent(provider, job);
	},
	
	processEvent: function(provider, event) {
		if(event.type == 'event') {
			var job = this.getJob(event.jobId);
			
			if(job != null) {
				job.info.messages = job.info.messages.concat(event.info.messages);
				job.info.log = event.info.log;
	
				job.total = event.total != null && event.total != 0 ? event.total : 100.0;
				job.worked = event.worked != null ? event.worked : 0.0;
				job.progress = (1.0 * job.worked) / job.total;

				if(event == job)
					this.select(job);
				else
					this.updateProgress(job);
			}
			
			if(event.done || job == null)
				provider.disconnect();
		}
	},

	select: function(job) {
		if(this.view) {
			var grid = this.view.jobsGrid;
			var record = grid.findRecord(job.jobId);
			grid.getSelectionModel().selectRecords([record]);
		}
	},
	
	updateProgress: function(job) {
		if(this.view) {
			var grid = this.view.jobsGrid;
			var record = grid.findRecord(job.jobId);
	
			if(record != null) {
				record.data['progress'] = this.getProgressAsString(job);
				record.afterEdit();
			}
			
			this.view.report.updateResult(job);
		}
	},
	
	stop: function() {
		if(this.view) {
			this.view.destroy();
			this.view = null;
		}
	},
	
	clear: function() {
	},
	
	onViewDestroyed: function() {
		this.view = null;
	}
});

Z8.console.Panel = Ext.extend(Z8.Panel,
{
	closable: true,
	layout: 'border',
	animCollapse: false,
	border: false,
	margins: { top: 3, left: 3, bottom: 3, right: 3 },
	title: 'Выполнение задач',

	initComponent: function()
	{
		Z8.console.Panel.superclass.initComponent.call(this);
		
		if(!this.jobsGrid)
		{
			this.jobsGrid = new Z8.console.Grid({data: this.data});
		}
		
		this.report = new Z8.console.ReportPanel({
			flex: 1,
			header: false,
			grid: this.jobsGrid
		});
		
		this.reportPanel = new Ext.Panel({
			region: 'center',
			layout:'vbox',
			layoutConfig: { align: 'stretch' },
			frame: false,
			border: false,
			items: this.report
		});
		
		this.add(this.reportPanel);
	},
	
	onClose: function()
	{
		Z8.Console.stop();
	}
});

Z8.console.ReportPanel = Ext.extend(Z8.Panel,
{
	layout: 'border',	
	border: false,
	frame: false,
	
	grid: null,
	report: null,
	
	constructor: function(config)
	{
		this.center = new Ext.Container({ region: 'center', layout: 'fit' });
		this.east = new Ext.Container({ region: 'east', layout: 'fit', width: '50%', split: true, layoutConfig: { align: 'stretch' } });
		
		config.items = [this.center, this.east];

		Z8.view.GridView.superclass.constructor.call(this, config);
	},
	
	initComponent: function()
	{
		Z8.view.GridView.superclass.initComponent.call(this);
		
		this.reportPanel = new Z8.view.ReportPanel({autoScroll: true, closable: false, clearable: false});
		this.center.add(this.grid);
		this.east.add(this.reportPanel);
		
		this.grid.getSelectionModel().on('rowselect', this.onRowSelect, this);
	},
	
	onRowSelect: function(sm, rowIndex, record)
	{
		this.updateResult(Z8.Console.getJob(record.id));
	},
	
	updateResult: function(job)
	{
		var selectionModel = this.grid.getSelectionModel();
		var record = selectionModel.getSelected();
		
		if(record != null && record.id == job.jobId)
		{
			this.reportPanel.onMessage(null/*job.text*/, job.info, true);
		}
	}
});

Z8.console.Grid = Ext.extend(Ext.grid.GridPanel, {
	
    autoHeight : false,
    border: true,
	
	initComponent: function() {

		var config = {};
        this.buildConfig(config);
        Ext.apply(this, Ext.apply(this.initialConfig, config));

        Z8.console.Grid.superclass.initComponent.call(this);
    },
    
    findRecord: function(id)
    {
    	return this.store.getById(id);
    },
    
    addRecord: function(job)
	{
		this.store.add(new this.store.recordType({
			id: job.jobId,
			parameters: job.parameters,
			progress: job.progress
		}));
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
    		idIndex: 0,
            fields: [{name: 'id'}, {name: 'name'}, {name: 'parameters'}, {name: 'progress'}]
    	}, this);
    },
    
    buildColumns: function(config)
    {
        config.columns = [
           {header: 'Задача', width: 75, sortable: true, dataIndex: 'name'},
           {header: 'Параметры', width: 100, sortable: true, dataIndex: 'parameters'},
           {header: 'Прогресс', width: 30, sortable: true, dataIndex: 'progress'}
        ];
    },
    
    buildView: function(config)
    {
    	config.view = new Ext.grid.GridView({forceFit: true});
    }
});