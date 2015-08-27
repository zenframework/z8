Z8.ReportManager = 
{
	defaultOptions: 
	{
		pageFormat: 'A4',
		pageOrientation: 'landscape', //'portrait',
		leftMargin: 10,
		rightMargin: 10,
		topMargin: 10,
		bottomMargin: 10
	},
	
	getOptions: function(query)
	{
		var settings = Z8.getUserSettings();

		if(settings.print == null)
		{
			return this.defaultOptions;
		}
		
		return settings.print[query.requestId] || this.defaultOptions;
	},
	
	runReport: function(grid, query, filters, report, format, ids, callback)
	{
		var store = query.getStore();
		
		var params = { xaction: 'report', options: Ext.encode(this.getOptions(query)) };
		
		if(!Z8.isEmpty(report))
		{
			params.report = report;
		}
		
		if(store.hasGrouping())
		{
			params.groupBy = Ext.encode(store.getGroupFields());
			params.groupDir = store.groupDir;
		}
		
		if(store.sortInfo != null)
		{
			params.sort = store.sortInfo.field;
			params.dir = store.sortInfo.direction;
		}

		if(filters != null)
		{
			Ext.apply(params, filters.buildQuery(filters.getFilterData()));
		}
		
		var columnModel = grid.getColumnModel();
		var columns = grid.getColumnModel().getColumnsBy(function(c){ return !c.hidden; });
		var reportColumns = [];
		
		for(var i = 0; i < columns.length; i++)
		{
			var column = columns[i];
			var store = grid.getStore();
			var dataIndex = column.dataIndex;
			
			if ( ! Z8.isEmpty(column.dataIndex) ) {
				reportColumns.push({ id: column.dataIndex, width: column.width });
			}
		}
		
		params.format = format;
		params.data = Ext.encode(ids);
		params.columns = Ext.encode(reportColumns);
		
		var onSuccess = this.onReportCreated.createDelegate(this, [query.text, format, callback], true);
		var onError = this.onReportFailed.createDelegate(this, [callback], true);

		query.request(onSuccess, onError, params, this);
	},

	onReportCreated: function(report, title, format, callback)
	{
		if(callback != null)
		{
			callback.call(report.info);
		}
		
		Z8.FileViewer.download(report, title, format);
	},
	
	onReportFailed: function(info, callback)
	{
		if(callback != null)
		{
			callback.call(info);
		}
	}
}