Ext.data.Record.prototype.markDirty = function() {
	this.dirty = true;

	if(!this.modified) {
		this.modified = {};

		this.fields.each(function(f) {
			this.modified[f.name] = this.data[f.name];
		}, this);
	}
};

Z8.data.Store = Ext.extend(Ext.data.JsonStore, {
	query: null, 

	url: Z8.request.url,
	method: Z8.request.method,

	pageSize: Z8.defaultPageCount,
	record: 0,
	start: 0,
	
	groupFields: [],
	
	constructor: function(config) {
		config = config || {};
		config.writer = new Z8.data.JsonWriter();

		config.fields.push(new Ext.data.Field({ name: 'style', type: 'auto' }));

		Z8.data.Store.superclass.constructor.call(this, config);
 
		this.autoDestroy = false;

		this.hasMultiSort  = true;
		this.multiSortInfo = this.multiSortInfo || {sorters: []};

		var sorters = this.multiSortInfo.sorters;
		var sortInfo = config.sortInfo || this.sortInfo;
		var groupDir = config.groupDir || this.groupDir;

		this.groupFields = config.groupFields || [];

		for(var i = 0; i < this.groupFields.length; i++)
			sorters.push({ field: this.groupFields[i], direction: 'ASC' });

		if(sortInfo)
			sorters.push(sortInfo);

		this.addEvents(
			'groupChange',
			'currentChange',
			'success',
			'error'
		);

		this.updateBaseParameters();

		this.on('exception', this.onException, this);
		this.on('write', this.onWrite, this);
		this.on('beforeload', this.onBeforeLoad, this);
		this.on('beforesave', this.onBeforeSave, this);
		this.on('load', this.onLoad, this);
	},

	/**
	 * @cfg {String} groupField
	 * The field name by which to sort the store's data (defaults to '').
	 */

	/**
	 * @cfg {String} groupDir
	 * The direction to sort the groups. Defaults to <tt>'ASC'</tt>.
	 */
	groupDir: 'ASC',

	/**
	 * Clears any existing grouping and refreshes the data using the default sort.
	 */
	
	isDirty: function() {
		return this.modified.length != 0 || this.removed.length != 0;
	},

	clearGrouping: function(skipReload) {
		this.groupFields = [];

		if(this.baseParams) {
			delete this.baseParams.groupBy;
			delete this.baseParams.groupDir;
		}

		var lo = this.lastOptions;

		if(lo && lo.params) {
			delete lo.params.groupBy;
			delete lo.params.groupDir;
		}

		if(skipReload !== true)
			this.reload();
	},

	onClear: function() {
	},
	
	updateRecord: function(store, record, action) {
		record.store = store;
		Z8.data.Store.superclass.updateRecord.call(this, store, record, action);
	},

	/**
	 * Groups the data by the specified field.
	 * @param {String} field The field name by which to sort the store's data
	 * @param {Boolean} forceRegroup (optional) True to force the group to be refreshed even if the field passed
	 * in is the same as the current grouping field, false to skip grouping on the same field (defaults to false)
	 */
	groupBy: function(fields, forceRegroup, direction, skipReload) {
		direction = direction ? (String(direction).toUpperCase() == 'DESC' ? 'DESC' : 'ASC') : this.groupDir;

		if(!forceRegroup && Z8.arrayCompare(this.groupFields, fields) && this.groupDir == direction)
			return; // already grouped by these fields

		this.groupFields = fields;
		this.groupDir = direction;
		this.updateBaseParameters();

		var fireGroupEvent = function() {
			this.fireEvent('groupChange', this, this.getGroupFields());
		};

		if(skipReload !== true) {
			this.on('load', fireGroupEvent, this, {single: true});
			this.reload();
		}
	},

	// Z8.data.Store always uses multisorting so we intercept calls to sort here to make sure that our grouping sorter object is always injected first.
	sort: function(fieldNames, dir) {
		return Z8.data.Store.superclass.sort.call(this, Ext.isArray(fieldNames) ? fieldNames[0] : fieldNames, dir);
	},

	hasGrouping: function() {
		return !Z8.isEmpty(this.groupFields);
	},
	
	/**
	 * @private
	 * Saves the current grouping field and direction to this.baseParams and this.lastOptions.params
	 * if we're using remote grouping. Does not actually perform any grouping - just stores values
	 */
	updateBaseParameters: function() {
		if(!this.baseParams)
			this.baseParams = {};

		if(this.hasGrouping())
			Ext.apply(this.baseParams, { groupBy: Ext.encode(this.groupFields), groupDir: this.groupDir });

		var lo = this.lastOptions;

		if(lo && lo.params) {
			lo.params.groupDir = this.groupDir;
			// this is deleted because of a bug reported at
			// http://www.extjs.com/forum/showthread.php?t=82907
			delete lo.params.groupBy;
		}
	},

	applyGrouping : function(alwaysFireChange) {
		if(this.hasGrouping()) {
			this.groupBy(this.groupFields, true, this.groupDir);
			return true;
		} else {
			if(alwaysFireChange === true)
				this.fireEvent('datachanged', this);
			return false;
		}
	},

	getGroupFields : function() {
		return this.groupFields;
	},

	getGroupState: function() {
		return this.hasGrouping() ? this.groupFields[0] : null;
	},

	getSubgroups: function(parent, field, level)
	{
		var start = parent != null ? parent.start: 0;
		var finish = parent != null ? parent.finish: this.getCount();

		var group = null;
		var groups = [];

		for(var i = start; i < finish; i++) {
			var record = this.getAt(i);
			var value = record.get(field);
			var comparableValue = value;

			if(Ext.isDate(value)) {
				var type = this.fields.get(field).serverType;
				comparableValue = value.format(Z8.Types.dateFormat(type)); 
			}

			if(group == null || String(comparableValue) != String(group.comparableValue)) {
				group = {
					field: field,
					value: value,
					comparableValue: comparableValue,
					start: i,
					finish: i + 1,
					records: [ record ],
					level: level,
					parent: parent
				};

				groups.push(group);
			} else {
				group.finish++;
				group.records.push(record);
			}
		}

		if(parent != null)
			parent.groups = groups;

		return groups;
	},

	findGroup: function(record, groups) {
		groups = groups != null ? groups : this.getGroups();

		if(groups == null)
			return null;

		var value = null;

		for(var i = 0; i < groups.length; i++) {
			var group = groups[i];

			if(i == 0) {
				value = record.get(group.field);

				if(Ext.isDate(value)) {
					var type = this.fields.get(group.field).serverType;
					value = value.format(Z8.Types.dateFormat(type)); 
				}
			}

			if(String(value) == String(group.value)) {
				if(group.groups != null) {
					var subgroup = this.findGroup(record, group.groups);
					return subgroup != null ? subgroup : group;
				} else
					return group;
			}
		}

		return null;
	},

	insertRecord: function(record) {
		var group  = this.findGroup(record);

		var index = group != null ? group.start : 0;
		this.insert(index, record);

		return this.getAt(index);
	},

	getGroups : function() {
		var groupFields = this.getGroupFields();

		if(groupFields.length == 0)
			return null;

		var groups = this.getSubgroups(null, groupFields[0], 0);

		if(groupFields.length == 1)
			return groups;

		for(var i = 0; i < groups.length; i++)
			this.collectGroups(groups[i], groupFields.slice(1), 1);

		return groups;
	},

	collectGroups: function(group, groupFields, level) {
		var groups = this.getSubgroups(group, groupFields[0], level);

		if(groupFields.length != 1) {
			for(var i = 0; i < groups.length; i++)
				this.collectGroups(groups[i], groupFields.slice(1), level + 1);
		}
	},

	doUpdate: function(record) {
		var oldRecord = this.getById(record.id);
		record.groups = oldRecord.groups;

		Z8.data.Store.superclass.doUpdate.call(this, record);
	},

	setChartData: function(chartData) {
		this.chartData = chartData;
	},

	getChartData: function() {
		return this.chartData;
	},

	getTotalsData: function() {
		return this.reader.jsonData != null ? this.reader.jsonData.totalsData : null;
	},

	onBeforeLoad: function(store, options) {
		Ext.apply(store.baseParams, store.query.getDataRequestParams());

		var params = options.params;
		if(params != null) {
			var sort = params.sort;
			if(Ext.isArray(sort))
				params.sort = Ext.encode(params.sort);
		}
		if(store.baseParams.limit == null)
			store.baseParams.limit = Z8.defaultPageCount;
	},

	save: function() {
		if(!this.writer)
			throw new Ext.data.Store.Error('writer-undefined');

		var queue = [], len, trans, batch, data = {}, i;

		if(this.removed.length)
			queue.push(['destroy', this.removed]);

		var rs = [].concat(this.getModifiedRecords());

		if(rs.length) {
			var phantoms = [];

			for(i = rs.length-1; i >= 0; i--) {
				if(rs[i].phantom === true) {
					var rec = rs.splice(i, 1).shift();
					if(rec.isValid())
						phantoms.unshift(rec);
				} else if(!rs[i].isValid())
					rs.splice(i,1);
			}

			if(phantoms.length)
				queue.push(['create', phantoms]);

			if(rs.length)
				queue.push(['update', rs]);
		}

		len = queue.length;

		if(len) {
			batch = ++this.batchCounter;

			for(i = 0; i < len; ++i) {
				trans = queue[i];
				data[trans[0]] = trans[1];
			}

			if(this.fireEvent('beforesave', this, data) !== false) {
				for(i = 0; i < len; ++i) {
					trans = queue[i];
					this.doTransaction(trans[0], trans[1], batch);
				}
				return batch;
			}
		}
		return -1;
	},

	onBeforeSave: function(store, data) {
		Ext.apply(store.baseParams, store.query.getDataRequestParams());
	},

	onLogin: function(data, action) {
		if(action == Ext.data.Api.actions.read)
			this.load();
		else if(action == Ext.data.Api.actions.update || action == Ext.data.Api.actions.create)
			this.save();
	},

	onLoad: function(store, r, o) {
		if(o.params != null) {
			this.start = o.params.start != null ? o.params.start : this.start;
			var record = o.params.rcrd != null ? o.params.rcrd : this.record;
			this.record = Math.min(record, Math.max(this.getTotalCount() - 1, 0));
		}

		this.fireEvent('success', []);
	},

	onWrite: function(store, action, result, response, records) {
		var info = response.raw.info;

		this.fireEvent('success', info);

		var event = this.events['success'];

		if(!Z8.isEmpty(info.messages)) {
			if(event == null || event.listeners == null || event.listeners.length == 0)
				Z8.showMessages(this.query.text || 'Z8', Z8.Format.nl2br(info.messages));
		}
	},

	onException: function(dataProxy, exceptionType, action, request, response, errors) {
		var info = null;

		if(response.raw == null) {
			try {
				response.raw = eval("(" + response.responseText + ")");
			} catch(e) {
				response.raw = {};
			}
		}

		if(response.raw != null && response.raw.status == Z8.Status.AccessDenied) {
			Z8.LoginManager.login(this.onLogin.createDelegate(this, [action], true), this, true);
			return;
		}

		if(response.raw != null) {
			if(response.raw.info != null)
				info = response.raw.info;
		}
		else if(response.statusText != null)
			info = { messages: [response.statusText] };
		else if(Ext.isDefined(errors) && errors != null) 
			info = { messages: [errors.name + ': ' + errors.message] };

		if(info == null)
			info = { messages: ['Undefined server message.'] };

		this.fireEvent('error', info);

		var event = this.events['error'];

		if(event == null || event.listeners == null || event.listeners.length == 0)
			Z8.showMessages(this.query.text || 'Z8', Z8.Format.nl2br(info.messages));
	},

	getPageData: function() {
		var records = this.getTotalCount();

		return {
			start: this.start,
			records: records,
			record: this.record + 1,
			page: Math.ceil((this.start + this.pageSize) / this.pageSize),
			pages: records < this.pageSize ? 1 : Math.ceil(records / this.pageSize)
		};
	},

	setCurrentRecord: function(record) {
		if(record == null || record.store == null)
			this.record = null;
		else {
			var index = this.indexOf(record);
			this.record = this.start + index;
		}

		this.fireEvent('currentChange', this, this.record);
	},

	getCurrentRecord: function() {
		if(this.record != null)
			return this.getAt(this.record - this.start);
		return null;
	},

	goToRecord: function(record) {
		record = Math.max(0, Math.min(record - 1, this.getTotalCount() - 1));

		var start = Math.floor(record / this.pageSize) * this.pageSize;

		if(start != this.start)
			this.loadPage(start, record);
		else {
			this.record = record;
			this.fireLoad();
		}
	},

	goToPage: function(page) {
		var start = (page - 1) * this.pageSize;
		start = start.constrain(0, this.getTotalCount());

		this.loadPage(start, start);
	},

	refresh: function() {
		this.loadPage(this.start, this.record);
	},

	loadPage: function(start, record) {
		var o = {};

		o.start = start;
		o.limit = this.pageSize;
		o.rcrd = record;

		this.load({ params: o });
	},

	fireLoad: function() {
		var params = {};
		params.record = this.record;
		params.start = this.start;

		this.fireEvent('load', this, this.data.items, { params: params });
	},

	firstRecord: function() {
		if(this.record == 0)
			throw "firstRecord: this.record == 0";

		if(this.start != 0)
			this.loadPage(0, 0);
		else {
			this.record = 0;
			this.fireLoad();
		}
	},

	firstPage: function() {
		if(this.start == 0)
			throw "firstPage: this.start == 0";
		this.loadPage(0, 0);
	},

	previousRecord: function() {
		if(this.record == 0)
			throw "previousRecord: this.start == 0";

		if(this.record == this.start)
			this.previousPage();
		else {
			this.record--;
			this.fireLoad();
		}
	},

	previousPage: function() {
		if(this.start == 0)
			throw "previousPage: this.start == 0";
		this.loadPage(Math.max(0, this.start - this.pageSize), this.start - 1);
	},

	nextRecord: function() {
		var count = this.getCount();

		if(this.record + 1 == this.getTotalCount())
			throw "nextRecord: this.record + 1 == this.getTotalCount()";

		if(this.record + 1 == this.start + this.getCount())
			this.nextPage();
		else {
			this.record++;
			this.fireLoad();
		}
	},

	nextPage: function() {
		var start = this.start + this.pageSize;
		this.loadPage(start, start);
	},

	lastRecord: function() {
		var records = this.getTotalCount();
		var extra = records % this.pageSize;
		var lastPageStart = records - (extra != 0 ? extra : this.pageSize);

		if(this.start < lastPageStart)
			this.lastPage();
		else {
			this.record = this.getTotalCount() - 1;
			this.fireLoad();
		}
	},

	lastPage: function() {
		var records = this.getTotalCount();
		var extra = records % this.pageSize;

		this.loadPage(records - (extra != 0 ? extra : this.pageSize), this.getTotalCount() - 1);
	},

	getChanges: function() {
		var changes = {};

		changes.removed = this.removed.length;
		changes.modified = 0;
		changes.created = 0;

		var modified = this.getModifiedRecords();

		for(var i = 0; i < modified.length; i++) {
			var record = modified[i];

			if(record.phantom)
				changes.created++;
			else
				changes.modified++;
		}

		return changes;
	},

	isRecordLocked: function(record) {
		var locked = false;

		if (Ext.isNumber(record))
			record = this.getAt(record);

		return this.lockProperty != null && record.data[this.lockProperty] === true;
	}
});

Ext.reg('z8-store', Z8.data.Store);
