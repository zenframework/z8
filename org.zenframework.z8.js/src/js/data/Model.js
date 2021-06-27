Z8.define('Z8.data.Model', {
	extend: 'Z8.Object',
	shortClassName: 'Model',

	isModel: true,

	local: false,
	totals: false,

	destroyed: false,
	phantom: false,

	idProperty: 'recordId',
	lockProperty: 'lock',
	periodProperty: null,
	filesProperty: null,

	fields: null,
	ordinals: null,
	modified: null,
	original: null,

	busy: 0,

	statics: {
		activeSaves: 0,

		idSeed: 0,
		nextId: function() {
			Model.idSeed++;
			return 'r-' + Model.idSeed;
		},

		getFieldNames: function(fields) {
			var result = [];

			if(fields == null)
				return result;

			for(var i = 0, length = fields.length; i < length; i++)
				result.push(fields[i].name);

			return result;
		}
	},

	constructor: function(data) {
		data = data || {};
		data = data.isModel ? data.data : data;

		var myData = this.data = {};
		this.editing = 0;

		for(var name in data) {
			var value = data[name];
			var field = this.getField(name);
			value = field != null ? field.convert(value, this) : value;
			myData[name] = value;
		}

		var idProperty = this.idProperty;
		var id = this.id = data != null ? data[idProperty] : null;
		if(id == null) {
			id = this.id = Model.nextId();
			myData[idProperty] = id;
			this.phantom = true;
		}

		var parentIdProperty = this.parentIdProperty;
		if(parentIdProperty == null)
			return;

		var parentId = this.parentId = data != null ? data[parentIdProperty] : null;
		if(parentId == null) {
			parentId = this.parentId = guid.Null;
			myData[parentIdProperty] = parentId;
		}
	},

	dispose: function() {
		this.store = null;
		this.callParent();
	},

	getStore: function() {
		return this.store;
	},

	getName: function() {
		return this.name || this.$className;
	},

	getSourceCodeLocation: function() {
		return this.sourceCode || this.name || this.$className;
	},

	isLocal: function() {
		return this.local === true;
	},

	isRemote: function() {
		return this.local !== true;
	},

	hasTotalCount: function() {
		return this.totalCount !== false;
	},

	hasTotals: function() {
		return this.totals === true;
	},

	getAccess: function() {
		return this.access || {};
	},

	getIdProperty: function() {
		return this.idProperty;
	},

	getParentIdProperty: function() {
		return this.parentIdProperty;
	},

	getPeriodProperty: function() {
		return this.periodProperty;
	},

	getFilesProperty: function() {
		return this.filesProperty;
	},

	getIconProperty: function() {
		return this.iconProperty || 'icon';
	},

	isEqual: function(left, right) {
		if(left instanceof Date && right instanceof Date)
			return Date.isEqual(left, right);
		return left == right;
	},

	getFields: function() {
		return this.fields;
	},

	getField: function(name) {
		return this.fields[this.getOrdinals()[name]];
	},

	getLinks: function() {
		return this.links;
	},

	getNames: function() {
		return this.names;
	},

	getColumns: function() {
		return this.columns;
	},

	getQuickFilters: function() {
		return this.quickFilters;
	},

	getRequestFields: function() {
		return this.requestFields;
	},

	getValueFor: function() {
		return this.valueFor;
	},

	getValueFrom: function() {
		return this.valueFrom;
	},

	getIcons: function() {
		return this.icons;
	},

	getLink: function() {
		return this.link;
	},

	getQuery: function() {
		return this.query;
	},

	getPath: function() {
		return Z8.isEmpty(this.query) ? '' : (this.query + '.');
	},

	getId: function() {
		return this.id;
	},

	setId: function(id) {
		this.set(this.idProperty, id);
		return this;
	},

	getParentId: function() {
		return this.get(this.parentIdProperty);
	},

	setParentId: function(parentId) {
		this.set(this.parentIdProperty, parentId);
		return this;
	},

	getLock: function() {
		var lockProperty = this.lockProperty;
		return lockProperty == null ? RecordLock.None : this.get(lockProperty);
	},

	getFiles: function() {
		var filesProperty = this.filesProperty;
		return filesProperty != null ? this.get(filesProperty) : null;
	},

	isEditable: function() {
		var lock = this.getLock();
		return lock != RecordLock.Edit && lock != RecordLock.Full;
	},

	isDestroyable: function() {
		var lock = this.getLock();
		return lock != RecordLock.Destroy && lock != RecordLock.Full;
	},

	getIndex: function() {
		var store = this.getStore();
		return store != null ? store.indexOf(this) : -1;
	},

	getOrdinals: function() {
		if(this.ordinals != null)
			return this.ordinals;

		var ordinals = this.self.ordinals = {};
		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++)
			ordinals[fields[i].name] = i;

		return ordinals;
	},

	isPhantom: function() {
		return this.phantom;
	},

	isDirty: function() {
		return this.modified != null;
	},

	beginEdit: function() {
		this.editing++;
		return this;
	},

	cancelEdit: function() {
		this.editing = 0;

		var data = this.data;
		var modified = this.modified;

		for(name in modified)
			data[name] = modified[name];

		this.modified = this.original = null;
		return this;
	},

	endEdit: function() {
		var editing = this.editing = Math.max(0, this.editing - 1);

		if(editing != 0)
			return this;

		var modified = this.modified;

		if(modified != null && Object.keys(modified).length != 0) {
			var store = this.store;
			if(store != null)
				store.onRecordChanged(this, modified);
			this.fireEvent('change', this, modified);
		}

		this.modified = this.original = null;
		return this;
	},

	getModifiedFields: function() {
		var fields = {};
		var modified = this.modified;
		var data = this.data;

		for(var name in modified)
			fields[name] = data[name];

		return fields;
	},

	get: function(name) {
		var value = this.data[name];
		return value !== undefined ? value : null;
	},

	set: function(name, value, silent, reset) {
		var data = this.data;
		var currentValue = data[name];
		var field = this.getField(name);

		var modified = this.modified;
		var original = this.original;

		var comparator = this;

		if(field != null) {
			value = field.convert(value, this);
			comparator = field;
		}

		if(!reset && comparator.isEqual(currentValue, value))
			return this;

		if(silent) {
			data[name] = value;
			return this;
		}

		if(original == null || !original.hasOwnProperty(name)) {
			original = this.original = this.original || {};
			modified = this.modified = this.modified || {};
			original[name] = currentValue;
			modified[name] = currentValue;
		} else if(original != null) {
			var originalValue = original[name];
			if(!reset && comparator.isEqual(originalValue, value)) {
				delete modified[name];
				if(Object.keys(modified).length == 0) {
					this.modified = null;
					this.original = null;
				}
			} else
				modified[name] = currentValue;
		}

		var idProperty = this.getIdProperty();
		var parentIdProperty = this.getParentIdProperty();

		if(idProperty == name)
			this.id = value;
		else if(parentIdProperty == name)
			this.parentId = value;

		data[name] = value;

		if(!this.editing) {
			var changes = {};
			changes[name] = currentValue;

			var store = this.store;
			if(store != null)
				store.onRecordChanged(this, changes);

			this.fireEvent('change', this, changes);
		}

		return this;
	},

	setStore: function(store) {
		this.store = store;
	},

	getServerData: function(action) {
		var phantom = this.isPhantom();

		var data = {};

		if(!this.destroyed)
			Z8.apply(data, phantom ? this.data: this.getModifiedFields());

		data[this.idProperty] = phantom ? guid.Null : this.id;
		return data;
	},

	startOperation: function() {
		this.busy++;
		Z8.data.Model.activeSaves++;
	},

	finishOperation: function() {
		this.busy--;
		Z8.data.Model.activeSaves--;
	},

	isBusy: function() {
		return this.busy != 0;
	},

	reload: function(callback, options) {
		if(this.getAccess().read === false)
			throw 'Model ' + this.getName() + ' does not have read record privilege';

		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(this.isPhantom())
			throw 'phantom record can not be reloaded';

		this.executeAction('read', callback, Z8.apply(options || {}, { recordId: this.id }));
	},

	create: function(callback, options) {
		if(this.getAccess().create === false)
			throw 'Model ' + this.getName() + ' does not have create record privilege';

		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(!this.isPhantom())
			throw 'new record must be a phantom';

		this.executeAction('create', callback, options);
	},

	update: function(callback, options) {
		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(this.isPhantom())
			throw 'phantom record can not be updated';

		if(!this.isDirty())
			Z8.callback(callback, this, true);
		else
			this.executeAction('update', callback, options);
	},

	copy: function(record, callback, options) {
		if(this.getAccess().copy === false)
			throw 'Model ' + this.getName() + ' does not have copy record privilege';

		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(!this.isPhantom())
			throw 'copy of the record ' + this.id + ' must be a phantom';

		this.executeAction('copy', callback, Z8.apply(options || {}, { recordId: record.isModel ? record.id : record }));
	},

	destroy: function(callback, options) {
		if(this.getAccess().destroy === false)
			throw 'Model ' + this.getName() + ' does not have destroy record privilege';

		if(this.isPhantom()) {
			var store = this.store;
			if(store != null)
				store.remove(this);
			Z8.callback(callback, this, true);
		} else
			this.executeAction('destroy', callback, options);
	},

	executeAction: function(action, callback, options) {
		if(this.isRemote()) {
			var batch = new Z8.data.Batch({ model: this, singleMode: true });
			batch.execute(action, [this], callback, options);
		} else
			Z8.callback(callback, this, true);
	},

	onAction: function(action, data) {
		if(action != 'destroy') {
			this.phantom = false;
			this.beginEdit();
			for(var name in data)
				this.set(name, data[name]);
			this.endEdit();
		} else {
			var store = this.store;
			if(store != null)
				store.remove(this);
			this.destroyed  = true;
		}
	},

	afterAction: function(action) {
		this.modified = this.original = null;
	},

	attach: function(name, files, callback) {
		if(this.getAccess().update === false)
			throw 'Model ' + this.getName() + ' does not have update record privilege';

		var filesToUpload = [];

		for(var i = 0, length = files.length; i < length; i++) {
			var file = files[i];
			if(Application.checkFileSize(file))
				filesToUpload.push(file);
		}

		if(filesToUpload.length == 0) {
			Z8.callback(callback, this, [], false);
			return;
		}

		var data = {
			request: this.getName(),
			action: 'attach',
			recordId: this.phantom ? guid.Null : this.id,
			field: name,
			fields: Model.getFieldNames(this.getFields()),
			link: this.getLink(),
			query: this.getQuery()
		};

		var requestCallback = function(response, success) {
			if(success) {
				this.onAction('attach', response.data[0]);
				Z8.callback(callback, this, this.get(name), true);
			} else
				Z8.callback(callback, this, [], false);
		};

		HttpRequest.upload(data, filesToUpload, { fn: requestCallback, scope: this });
	},

	detach: function(name, files, callback) {
		if(this.getAccess().update === false)
			throw 'Model ' + this.getName() + ' does not have update record privilege';

		var data = {
			request: this.getName(),
			action: 'detach',
			recordId: this.id,
			field: name,
			data: files,
			fields: Model.getFieldNames(this.getFields()),
			link: this.getLink(),
			query: this.getQuery()
		};

		var requestCallback = function(response, success) {
			if(success) {
				this.onAction('detach', response.data[0]);
				Z8.callback(callback, this, this.get(name), true);
			} else
				Z8.callback(callback, this, [], false);
		};

		HttpRequest.send(data, { fn: requestCallback, scope: this });
	}
});
