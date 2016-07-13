Z8.query.Query = Ext.extend(Ext.util.Observable, 
{
	requestId: null,
	
	queryId: null,
	recordId: null,
	
	primaryKey: null,
	parentKey: null,
	lockKey: null,

	fields: [],
	controls: [],

	sort: null,
	dir: null,
	
	backwards: [],

	store: null,

	constructor : function(config)
	{
		Ext.apply(this, config);
		
		for(var i = 0; i < this.fields.length; i++)
		{
			this.fields[i] = new Z8.query.Field(this.fields[i]);
			this.fields[i].query = this;
			
			if(this.readOnly)
			{
				this.fields[i].readOnly = true;
			}
		}

		if(this.section != null)
		{
			this.section = new Z8.query.Section(this.section);
		}

		this.width = Math.max(Math.min(this.width || 1, 1), 0);
		this.height = Math.max(Math.min(this.height || 1, 1), 0);
		
		Z8.query.Query.superclass.constructor.call(this);
	},

	getFieldById: function(id)
	{
		for(var i = 0; i < this.fields.length; i++)
		{
			var field = this.fields[i];
			if(field.id == id)
			{
				return field;
			}
		}
		return null;
	},

	getFieldsByGroupId: function(groupId)
	{
		var result = [];
		for(var i = 0; i < this.fields.length; i++)
		{
			var field = this.fields[i];
			if(field.groupId == groupId)
			{
				result.push(field);
			}
		}
		return result;
	},

	getFields: function()
	{
		return this.fields;
	},

	getColumns: function()
	{
		var columns = this.section != null ? this.section.getAllColumns() : [];

		var fields = [];
		
		for(var i = 0; i < columns.length; i++)
		{
			var field = this.getFieldById(columns[i].id);
			fields.push(field);
		}
		
		return fields;
	},

	request: function(callback, errorCallback, params, scope)
	{
		var p = this.getDataRequestParams();
		Ext.apply(p, params);
		p.title = this.text;
		Z8.Ajax.request(this.requestId, callback, errorCallback, p, scope);
	},

	getDataRequestParams: function()
	{
		var params = { sessionId: Z8.sessionId, requestId: this.requestId, requestUrl: Z8.getRequestUrl() };

		if(this.queryId != null)
		{
			params.queryId = this.queryId;
		}

		if(this.ids != null)
		{
			params.ids = Ext.encode(this.ids);
		}

		if(this.fieldId != null)
		{
			params.fieldId = this.fieldId;
		}

		if(this.filterBy != null)
		{
			params.filterBy = this.filterBy;
			delete this.filterBy;
		}
		
		if(this.recordId != null)
		{
			params.recordId = this.recordId;

			var record = this.getParentRecord();
			
			if(record != null)
			{
				params.record = record;
			}
		}
		
		if(this.parentId != null)
		{
			params.parentId = this.parentId;
		}
		
 		params.period = this.period != null ? Ext.encode(this.period) : null;
		params.filter1 = this.filter1 != null ? Ext.encode(this.filter1) : null;

		if(this.showAsTree != null)
		{
 			params.grid = Ext.encode(!this.showAsTree);
		}

		return params;
	},

	getParentRecord: function()
	{
		if(this.parent != null)
		{
			var record = this.parent.getStore().getById(this.recordId);
			return record != null ? record.encode() : null;
		}
		
		return null;
	},
	
	createStoreFields: function()
	{
		var fields = [];

		for(var i = 0; i < this.fields.length; i++)
		{
			var field = new Ext.data.Field(this.fields[i]);
			field.useNull = !field.required;
			field.name = field.id;

			if(field.type == Z8.Types.Date)
			{
				field.convert = function(value, record)
				{
					var isDatetime = this.serverType == Z8.ServerTypes.Datetime;
					return Date.parseDate(value, isDatetime ? Z8.Format.Datetime : Z8.Format.Date);
				}
			}

			fields.push(field);
		}
		return fields;
	},

	getStore: function()
	{
		if(this.store != null)
		{
			return this.store;
		}

		var fields = this.createStoreFields();

		this.store = new Z8.data.Store(
		{
			query: this,
			fields: fields,
			storeId: this.id,

			baseParams: {},

			autoLoad: false,
			autoSave: false,
			autoDestroy: false,
			
			root: Z8.dataProperty,
			idProperty: this.primaryKey,
			lockProperty: this.lockKey,
			filesProperty: this.attachments,
			childrenProperty: this.children,
			
			messageProperty: Z8.messageProperty,
			totalProperty: Z8.totalProperty,
			
			remoteSort: this.remoteSort != null ? this.remoteSort : true,

			sortInfo: this.sort != null ? { field: this.sort, direction: this.dir } : null,
			
			groupFields: this.groupBy,
			groupDir: this.groupDir
		});

		if(this.data != null)
		{
			this.store.loadData(this);
		}
		
		return this.store;
	},
	
	canAdd: function()
	{
		return this.actions.indexOf('add') != -1;
	},
	
	canCopy: function()
	{
		return this.actions.indexOf('copy') != -1;
	},
	
	canDelete: function()
	{
		return this.actions.indexOf('delete') != -1;
	},
	
	canImport: function()
	{
		return false;
	}
});