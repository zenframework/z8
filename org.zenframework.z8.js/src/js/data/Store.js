Z8.define('Z8.data.Store', {
	extend: 'Z8.Object',
	shortClassName: 'Store',

	isStore: true,

	filters: [],
	quickFilters: [],
	where: [],
	period: [],
	sorter: [],

	remoteFilter: true,
	remoteSort: true,

	page: 0,
	limit: 0,
	totalCount: 0,

	loadCount: 0,
	loading: 0,
	loadIndex: 0,
	transaction: 0,

	constructor: function(config) {
		var data = config.data;
		delete config.data;

		Z8.Object.prototype.constructor.call(this, config);

		this.useCount = 0;

		this.inserted = [];
		this.positions = [];
		this.removed = [];

		this.setWhere(this.where);
		this.setFilter(this.filters);
		this.setQuickFilter(this.quickFilters);
		this.setPeriod(this.period);
		this.setSorter(this.sorter);

		this.initRecords(data);

		if(this.getCount() != 0) {
			this.loadTotalCount();
			this.loadTotals();
		}
	},

	use: function() {
		this.useCount++;
	},

	dispose: function() {
		if(this.useCount == 0 || --this.useCount != 0)
			return;

		for(var record of this.records)
			record.dispose();

		this.records = [];

		Z8.Object.prototype.dispose.call(this);
	},

	getModel: function() {
		if(String.isString(this.model))
			this.model = Z8.classes[this.model];
		return this.model;
	},

	getModelName: function() {
		return this.isRemote() ? this.getModel().prototype.getName() : null;
	},

	getSourceCodeLocation: function() {
		return this.isRemote() ? this.getModel().prototype.getSourceCodeLocation() : null;
	},

	isLocal: function() {
		return this.getModel().prototype.isLocal();
	},

	isTree: function() {
		return this.getParentIdProperty() != null;
	},

	isRemote: function() {
		return this.getModel().prototype.isRemote();
	},

	hasTotals: function() {
		return this.getModel().prototype.hasTotals();
	},

	hasTotalCount: function() {
		return this.getModel().prototype.hasTotalCount();
	},

	getAccess: function() {
		return this.getModel().prototype.getAccess();
	},

	hasReadAccess: function() {
		return this.getAccess().read !== false;
	},

	hasWriteAccess: function() {
		return this.getAccess().write !== false;
	},

	hasCreateAccess: function() {
		return this.getAccess().create !== false;
	},

	hasCopyAccess: function() {
		return this.getAccess().copy !== false;
	},

	hasDestroyAccess: function() {
		return this.getAccess().destroy !== false;
	},

	getIdProperty: function() {
		return this.getModel().prototype.getIdProperty();
	},

	getParentIdProperty: function() {
		return this.getModel().prototype.getParentIdProperty();
	},

	getPeriodProperty: function() {
		return this.getModel().prototype.getPeriodProperty();
	},

	getFilesProperty: function() {
		return this.getModel().prototype.getFilesProperty();
	},

	getIconProperty: function() {
		return this.getModel().prototype.getIconProperty();
	},

	getValueForFields: function() {
		return this.getModel().prototype.getValueFor();
	},

	getValueFromFields: function() {
		return this.getModel().prototype.getValueFrom();
	},

	getRecords: function() {
		return this.records;
	},

	getRange: function(start, end) {
		return this.records.slice(start, end + 1);
	},

	getFields: function() {
		return this.getModel().prototype.getFields();
	},

	getRequestFields: function() {
		return this.getModel().prototype.getRequestFields();
	},

	getLink: function() {
		return this.getModel().prototype.getLink();
	},

	getQuery: function() {
		return this.getModel().prototype.getQuery();
	},

	getPath: function() {
		return this.getModel().prototype.getPath();
	},

	getField: function(name) {
		return this.getModel().prototype.getField(name);
	},

	getNames: function() {
		return this.getModel().prototype.getNames();
	},

	getColumns: function() {
		return this.getModel().prototype.getColumns();
	},

	getQuickFilters: function() {
		return this.getModel().prototype.getQuickFilters();
	},

	getFilterFields: function() {
		return this.getModel().prototype.getFilterFields();
	},

	getLimit: function() {
		return (this.isTree() ? 0 : this.limit) || 0;
	},

	getPage: function()  {
		return this.page || 0;
	},

	getCount: function() {
		return this.records.length;
	},

	getTotalCount: function() {
		return this.isRemote() ? this.totalCount : this.getCount();
	},

	getTotals: function() {
		return this.totals;
	},

	getRemoteFilter: function() {
		return this.isRemote() && this.remoteFilter;
	},

	getRemoteSort: function() {
		return this.isRemote() && this.remoteSort;
	},

	isEmpty: function() {
		return this.getCount() == 0;
	},

	getAt: function(index) {
		return this.records[index];
	},

	indexOf: function(record) {
		if(record == null)
			return -1;
		var index = this.getOrdinals()[record.isModel ? record.id : record];
		return index !== null ? index : -1;
	},

	getById: function(id) {
		return this.records[this.getOrdinals()[id]];
	},

	add: function(records) {
		this.insert(records, this.getCount());
	},

	attach: function(records) {
		this.setStore(records, this);
	},

	detach: function(records) {
		this.setStore(records, null);
	},

	beginTransaction: function() {
		this.transaction++;
	},

	commit: function() {
		var transaction = this.transaction = Math.max(this.transaction - 1, 0);

		if(transaction != 0)
			return;

		var removed = this.removed;
		var hasRemoved = !Z8.isEmpty(removed);

		var inserted = this.inserted;
		var hasInserted = !Z8.isEmpty(inserted);

		if(!hasRemoved && !hasInserted)
			return;

		var records = this.records;
		var cache = this.cache;

		this.ordinals = null;

		if(hasRemoved) {
			records.removeAll(removed);
			if(cache != null)
				cache.removeAll(removed);

			this.detach(removed);
			this.totalCount -= removed.length;
			this.fireEvent('remove', this, removed);
		}

		if(hasInserted) {
			var added = [];
			var positions = this.positions;
			for(var i = 0, length = inserted.length; i < length; i++) {
				var insert = inserted[i];
				records.insert(insert, positions[i]);
				added = added.concat(insert);

				if(cache != null)
					cache.add(insert);
			}

			this.attach(added);
			this.totalCount += added.length;

/*
			Закомментировано, чтобы записи добавлялись в начало
			this.sortRecords();
*/
			this.treefyRecords();

			var ranges = this.getIndexRanges(added);

			if(Array.isArray(ranges)) {
				for(var i = 0, length = ranges.length; i < length; i++) {
					var range = ranges[i];
					var start = range[0];
					var end = range[range.length - 1];
					this.fireEvent('add', this, this.getRange(start, end), start);
				}
			} else
				this.fireEvent('add', this, this.getRange(ranges, ranges + added.length - 1), ranges);
		}

		this.inserted = [];
		this.positions = [];
		this.removed = [];

		this.loadTotals();
	},

	rollback: function() {
		var transaction = this.transaction = Math.max(this.transaction - 1, 0);

		if(transaction != 0)
			return;

		this.inserted = [];
		this.positions = [];
		this.removed = [];
	},

	setStore: function(records, store) {
		records = Array.isArray(records) ? records : [records];
		for(var record of records)
			record.setStore(store);
	},

	insert: function(records, index) {
		if(records == null)
			return;

		records = Array.isArray(records) ? records : [records];

		if(records.isEmpty())
			return;

		this.inserted.push(records);
		this.positions.add(index);

		if(this.transaction == 0)
			this.commit();
	},

	remove: function(records) {
		if(records == null)
			return;

		records = Array.isArray(records) ? records : [records];
		this.removed.add(records);

		if(this.transaction == 0)
			this.commit();
	},

	removeAll: function() {
		this.removed = [].concat(this.records);

		if(this.transaction == 0)
			this.commit();
	},

	removeAt: function(index) {
		if(index < 0 || index > this.getCount())
			return;

		var record = this.records[index];
		this.remove([record]);
	},

	getIndexRanges: function(records) {
		var ordinals = this.getOrdinals();
		var index = [];

		var length = records.length;

		if(length == 1)
			return ordinals[records[0].id];

		for(var i = 0; i < length; i++)
			index.push(ordinals[records[i].id]);

		index.sort(Number.compare);

		var previous = index[0];
		var range = [index[0]];
		var ranges = [range];

		for(var i = 1, length = index.length; i < length; i++) {
			var next = index[i];
			if(previous + 1 != next) {
				range = [next];
				ranges.push(range);
			} else
				range.push(next);

			previous = next;
		}

		if(ranges.length == 1)
			return ranges[0][0];

		return ranges;
	},

	getOrdinals: function() {
		if(this.ordinals != null)
			return this.ordinals;

		var ordinals = this.ordinals = {};
		var records = this.records;
		for(var i = 0, length = records.length; i < length; i++)
			ordinals[records[i].id] = i;

		return ordinals;
	},

	getValues: function() {
		var values = this.values;
		return values != null ? (values.isModel ? values.data : values) : null;
	},

	setValues: function(values) {
		this.values = values;
	},

	getWhere: function() {
		return this.where;
	},

	setWhere: function(where) {
		where = where || [];
		return (this.where = Array.isArray(where) ? where : [where]);
	},

	getFilter: function() {
		return this.filters;
	},

	setFilter: function(filters) {
		this.page = 0;
		filters = filters || [];
		return (this.filters = Array.isArray(filters) ? filters : [filters]);
	},

	getQuickFilter: function() {
		return this.quickFilters;
	},

	setQuickFilter: function(quickFilters) {
		this.page = 0;
		quickFilters = quickFilters || [];
		return (this.quickFilters = Array.isArray(quickFilters) ? quickFilters : [quickFilters]);
	},

	getPeriod: function() {
		return this.period;
	},

	setPeriod: function(period) {
		this.page = 0;
		return this.period = period;
	},

	getSorter: function() {
		return this.sorter;
	},

	setSorter: function(sorter) {
		sorter = sorter || [];
		return (this.sorter = Array.isArray(sorter) ? sorter : [sorter]);
	},

	filter: function(filters, options) {
		this.setFilter(filters);

		if(!this.getRemoteFilter()) {
			this.filterRecords();
			var records = this.records;
			this.fireEvent('load', this, records, true);
			Z8.callback(options, this, records, true);
		} else
			this.load(options);
	},

	quickFilter: function(quickFilters, options) {
		this.setQuickFilter(quickFilters);

		if(!this.getRemoteFilter()) {
			this.filterRecords();
			var records = this.records;
			this.fireEvent('load', this, records, true);
			Z8.callback(options, this, records, true);
		} else
			this.load(options);
	},

	sort: function(sorter, options) {
		this.setSorter(sorter);

		if(!this.getRemoteSort()) {
			this.sortRecords();
			this.treefyRecords();
			var records = this.getRecords();
			this.fireEvent('load', this, records, true);
			Z8.callback(options, this, records, true);
		} else
			this.load(options);
	},

	initRecords: function(data) {
		var hasData = data != undefined;

		data = data || [];

		var records = this.records = [];
		this.ordinals = null;
		this.cache = null;

		if(hasData) {
			var model = this.getModel();
			for(var i = 0, length = data.length; i < length; i++) {
				var record = Z8.create(model, data[i]);
				this.attach(record);
				records.push(record);
			}

			if(this.isLocal() && this.getSorter().length != 0)
				this.sortRecords();

			this.treefyRecords();

			this.loadCount++;
		}

		return this.records;
	},

	isLoaded: function() {
		return this.loadCount != 0 || this.isLocal();
	},

	isLoading: function() {
		return this.loading != 0;
	},

	getLoadParams: function() {
		return {
			action: 'read',
			request: this.getModelName(),
			link: this.getLink(),
			query: this.getQuery(),
			fields: Model.getFieldNames(this.getRequestFields()),
			where: this.getWhere(),
			filter: this.getFilter(),
			quickFilter: this.getQuickFilter(),
			sort: this.getSorter(),
			period: this.getPeriod(),
			values: this.getValues(),
			start: this.getPage() * this.getLimit(),
			limit: this.getLimit() // not to send limit if unlimited, e.g. limit == 0
		};
	},

	load: function(callback) {
		var params = this.getLoadParams();

		this.fireEvent('beforeLoad', this, params);

		var loadCallback = function(response, success) {
			var records = success ? this.initRecords(response.data || []) : [];
			this.fireEvent('load', this, records, success);
			Z8.callback(callback, this, records, success);

			if(success) {
				this.loadTotalCount();
				this.loadTotals();
			}
		};

		this.loadIndex++;
		this.sendLoadRequest(params, { fn: loadCallback, scope: this });
	},

	loadTotalCount: function(options) {
		if(!this.hasTotalCount())
			return;

		var count = this.getCount();

		if(count == 0 || (count < this.getLimit() && this.getPage() == 0) || !this.isRemote() || this.isTree()) {
			this.calcTotalCount(count);
			this.fireEvent('count', this, true);
			Z8.callback(options, this, true);
			return;
		}

		var params = this.getLoadParams();
		params.count = true;

		var callback = function(response, success) {
			if(success) {
				this.calcTotalCount(response.total || 0);
				this.fireEvent('count', this, true);
			}

			Z8.callback(options, this, success);
		};

		this.fireEvent('beforeCount', this, params);
		this.sendLoadRequest(params, { fn: callback, scope: this });
	},

	calcTotalCount: function(totalCount) {
		this.totalCount = totalCount;
		var limit = this.getLimit();
		this.page = limit != 0 && totalCount != 0 ? Math.min(this.page || 0, Math.floor(totalCount / limit) + (totalCount % limit == 0 ? 0 : 1) - 1) : 0;
	},

	loadTotals: function(options) {
		if(!this.hasTotals())
			return;

		var count = this.getCount();

		if(count == 0 || !this.isRemote()) {
			var totals = this.totals = this.calcTotals();
			this.fireEvent('totals', this, totals, true);
			Z8.callback(options, this, totals, true);
			return;
		}

		var params = this.getLoadParams();
		params.totals = true;

		var callback = function(response, success) {
			var totals = this.totals = Z8.create(this.getModel(), success ? response.data : {});
			this.fireEvent('totals', this, totals, success);
			Z8.callback(options, this, totals, success);
		};

		this.sendLoadRequest(params, { fn: callback, scope: this });
	},

	sendLoadRequest: function(params, callback) {
		this.loading++;
		var loadIndex = this.loadIndex;

		var sendCallback = function(response, success) {
			this.loading--;
			if(loadIndex != this.loadIndex)
				return;
			Z8.callback(callback, response, success);
		};

		HttpRequest.send(params, { fn: sendCallback, scope: this });
	},

	loadData: function(data) {
		var records = this.initRecords(data);
		this.fireEvent('load', this, records, true);
	},

	loadPage: function(page, options) {
		this.page = page;
		this.load(options);
	},

	unload: function() {
		this.loadCount = 0;
	},

	onRecordChanged: function(record, modified) {
		var idProperty = this.getIdProperty();
		var parentIdProperty = this.getParentIdProperty();

		if(idProperty in modified)
			this.onIdChanged(record, modified[idProperty]);

		if(parentIdProperty != null && parentIdProperty in modified)
			this.onParentIdChanged(record, modified[parentIdProperty]);

		this.loadTotals();

		this.fireEvent('recordChange', this, record, modified);
	},

	onIdChanged: function(record, oldId) {
		var ordinals = this.ordinals;
		if(ordinals != null) {
			index = ordinals[oldId];
			ordinals[record.id] = index;
			delete ordinals[oldId];
		}
		this.fireEvent('idChange', this, record, oldId);
	},

	onParentIdChanged: function(record, oldId) {
		this.sortRecords();
		this.treefyRecords();
		this.fireEvent('load', this, this.getRecords(), true);
	},

	sortRecords: function() {
		var sorters = this.getSorter();

		if(sorters.length == 0)
			return;

		var fields = [];
		for(var sorter of sorters)
			fields.push(this.getField(sorter.property));

		var sortFn = function(left, right) {
			for(var i = 0, length = sorters.length; i < length; i++) {
				var field = fields[i];
				if(field == null)
					continue;

				var sorter = sorters[i];
				var property = sorter.property;
				var leftValue = left.get(property);
				var rightValue = right.get(property);
				var result = field.compare(leftValue, rightValue);
				if(result != 0)
					return sorter.direction == 'asc' ? result : -result;
			}
			return 0;
		};

		this.records.sort(sortFn);
		this.ordinals = null;
	},

	filterRecords: function() {
		var filters = this.filters.concat(this.where).concat(this.quickFilters).concat(this.period);

		var hasCache = this.cache != null;

		if(hasCache) {
			this.records = this.cache;
			this.cache = null;
			this.ordinals = null;
		}

		if(filters.length == 0)
			return this.treefyRecords();

		var cache = this.cache = this.records;
		var records = this.records = [];

		for(var record of cache) {
			var filtered = false;
			for(var filter of filters) {
				if(!Z8.filter.Operator.applyFilter(record, filter)) {
					filtered = true;
					break;
				}
			}

			if(!filtered)
				records.push(record);
		}

		this.ordinals = null;

		this.treefyRecords();
	},

	treefyRecords: function() {
		if(!this.isTree())
			return;

		var map = {};
		var roots = [];

		var records = this.getRecords();

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			var recordId = record.id;
			var parentId = record.parentId;

			var children = [];
			map[recordId] = children;

			var parentChildren = map[parentId];
			if(parentChildren != null)
				parentChildren.push(record);
			else
				roots.add(record);

			for(var j = 0; j < roots.length; j++) {
				var root = roots[j];
				if (root.parentId == recordId) {
					children.push(root);
					roots.removeAt(j);
					j--;
				}
			}
		}

		var populate = function(records, level) {
			for(var record of records) {
				var children = map[record.id];
				var data = record.data;
				var hasChildren = data.hasChildren = children != null && children.length != 0;
				data.level = level;
				result.push(record);
				if(hasChildren)
					populate(children, level + 1);
			}
		};

		var result = [];
		populate(roots, 0);

		this.records = result;
		this.ordinals = null;
	},

	calcTotals: function() {
		return this.totals = Z8.create(this.getModel(), {});
	}
});