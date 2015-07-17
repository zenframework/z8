Z8.grid.DataSourcePanel = Ext.extend(Z8.Panel,
{
	closable: false,
	layout: 'absolute',
	border: false,
	header: false,
	
	initComponent: function()
	{
		Z8.grid.DataSourcePanel.superclass.initComponent.call(this);
		
		this.textLabel = new Ext.form.Label({ text: 'Выберите запись, с которой будут объеденены записи, выбранные в справочнике', x: 10, y: 13/*, style: 'font-weight: normal;'*/ });
//		this.nameLabel = new Ext.form.Label({ text: '', x: 10, y: 33 });

		this.gridContainer = new Ext.Panel({ x: 10, y: 40, width: 580, height: 303, layout: 'fit', bodyBorder: false, border: true, style: 'border-top-width: 1px; border-left-width: 1px; border-right-width: 1px; border-bottom-width: 0px;' });

		this.add(this.textLabel);
//		this.add(this.nameLabel);
		this.add(this.gridContainer);
	},

	onActivate: function()
	{
		this.wizard.nextButton.setDisabled(true);
		
		this.load();
	},
	
	load: function()
	{
		if(this.grid != null)
		{
			this.grid.destroy();
		}

		this.gridContainer.addClass('x-panel-busy');

		var params = {};
		
		Z8.Ajax.request(this.config.query, this.onQueryLoaded, this.onQueryLoadError, params, this);
	},
	
	onQueryLoaded: function(query)
	{
		this.gridContainer.removeClass('x-panel-busy');

		delete query.groupBy;
		
		this.grid = createGrid(query, { editable: false, border: false, multiselect: false });

		this.gridContainer.add(this.grid);
		this.gridContainer.doLayout();

//		this.nameLabel.setText(query.text);
		
		var selectionModel = this.grid.getSelectionModel();
		selectionModel.on('selectionchange', this.onSelectionChange, this);
	},
	
	onSelectionChange: function()
	{
		var disabled = false;
		
		var records = this.grid.getSelectedRecords();

		if(records.length == 0 || (this.config.records.length == 1 && this.config.records[0] == records[0].id))
		{
			disabled = true;
		}
		
		this.wizard.nextButton.setDisabled(disabled);
	},
	
	onNext: function()
	{
		var recordId = this.grid.getSelectedRecord().id;
		this.config.recordId = recordId;
		
		return true;
	},

	onQueryLoadError: function(info)
	{
		this.gridContainer.removeClass('x-panel-busy');
		Z8.showMessages('Z8', Z8.Format.nl2br(info.messages));
	}
});

Z8.grid.RecordsMergerConsole = Ext.extend(Z8.Panel,
{
	closable: false,
	layout: 'absolute',
	border: false,
	header: false,
	
	initComponent: function()
	{
		Z8.grid.DataSourcePanel.superclass.initComponent.call(this);
		
		this.label = new Ext.form.Label({ text: 'Сообщения', x: 10, y: 13 });

		this.reportPanel = new Z8.view.ReportPanel({ x: 10, y: 40, width: 580, height: 263, autoScroll: true, header: false, bodyBorder: false, border: true, style: 'border-top-width: 1px; border-left-width: 1px; border-right-width: 1px; border-bottom-width: 0px;'});
		this.progressBar = new Ext.ProgressBar({ align: 'center', width: 580 });
//		this.progressBarContainer = new Ext.Panel({ layout: 'fit', x:10, y:333, width: 580, height: 20, items: this.progressBar});


		this.add(this.label);
		this.add(this.reportPanel);
		this.addButton(this.progressBar);
	},

	onActivate: function()
	{
		this.wizard.backButton.setDisabled(true);
		this.wizard.nextButton.setDisabled(true);
		this.start();
	},
	
	start: function()
	{
		var onSuccess = this.onStarted;
		var onError = Ext.emptyFn;
		
		var params = {};
		params.table = this.config.table;
		params.records = Ext.encode(this.config.records);
		params.recordId = this.config.recordId;
		
		Z8.Ajax.request('org.zenframework.z8.generator.fk.RecordsMergeJob', onSuccess, onError, params, this);
	},
	
	onStarted: function(job)
	{
		this.job = job;
		
		this.provider = new Ext.direct.PollingProvider({
			type:'polling',
			url: Z8.request.url,
			baseParams: { sessionId: Z8.sessionId, jobId: job.jobId }
		});
		
		Ext.Direct.addProvider(this.provider);
		
		this.provider.on('data', this.processEvent, this);
	},
	
	processEvent: function(provider, event)
	{
		if(event.type == 'event')
		{
			this.job.info.messages = this.job.info.messages.concat(event.info.messages);
			this.job.info.log = event.info.log;
			this.job.info.serverId = event.serverId;
			this.job.totalWork = event.totalWork != null && event.totalWork != 0 ? event.totalWork : 100.0;
			this.job.worked = event.worked != null ? event.worked : 0.0;
			this.job.progress = (1.0 * this.job.worked) / this.job.totalWork;

			this.reportPanel.onMessage(null/*job.text*/, this.job.info, true);
			this.progressBar.updateProgress(this.job.progress, (this.job.progress * 100).toFixed(1) + '%');
//			this.updateProgress();
			
			if(event.done)
			{
				this.provider.disconnect();
				this.wizard.nextButton.setDisabled(false);
			}
		}
	},
	
	beforeDestroy: function()
	{
		if(this.provider != null)
		{
			this.provider.disconnect();
		}
		
		Z8.grid.RecordsMergerConsole.superclass.beforeDestroy.call(this);
	}
	
});
