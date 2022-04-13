Z8.define('Z8.query.Store', {
	extend: 'Z8.data.Store',

	statics: {
		definedModels: {},

		config: function(field) {
			var config = {};

			var isComboBox = field.isCombobox || field.isComboBox;
			var isListBox = field.isListbox || field.isListBox;

			if(!isComboBox && !isListBox)
				throw 'Z8.query.Store.config() accepts only listbox or combobox config';

			var query = field.query;
			var primaryKey = query.primaryKey;
			var parentKey = field.query.parentKey;
			var periodKey = isListBox ? field.query.periodKey : null;
			var lockKey = field.query.lockKey;

			var link = isListBox ? query.link : field.link;
			var fields = query.fields || query.columns || [field];
			if(isListBox && link != null)
				fields = fields.concat([link]);
			if(field.fields != null)
				fields = fields.concat(field.fields);
			if(field.columns != null)
				fields = fields.concat(field.columns);

			config.request = query.request;
			config.fields = fields;
			config.requestFields = fields;
			config.primaryKey = primaryKey;
			config.parentKey = parentKey;
			config.lockKey = lockKey;
			config.periodKey = periodKey;
			config.link = (isComboBox && link != null) ? link.name : null;
			config.query = query.name;
			config.totals = isListBox ? query.totals : false;
			config.limit = query.limit || 200;
			config.where = (isListBox || isComboBox) ? query.where : null;
			config.sort = (isListBox || isComboBox) ? query.sort : null;
			config.values = field.values;
			config.access = query.access;

			return config;
		}
	},

	constructor : function(config) {
		var fields = this.getFieldsInfo(config);

		var form = { colCount: config.colCount, controls: config.controls, text: config.text, presentation: config.presentation, icon: config.icon, actions: config.actions, reports: config.reports, readOnly: config.readOnly, dependencies: config.dependencies };

		this.readOnly = config.readOnly || false;

		var request = config.request;
		var className = request + '/' + fields.hash;

		var model = Z8.define(className, {
			extend: 'Z8.data.Model',
			single: true,

			name: request,
			link: config.link,
			query: config.query,
			totalCount: config.total !== false,
			totals: config.totals === true,

			idProperty: config.primaryKey,

			access: config.access,

			parentIdProperty: config.parentKey,
			lockProperty: config.lockKey,
			periodProperty: config.periodKey,
			filesProperty: config.attachmentsKey,
			iconProperty: fields.iconProperty,
			sourceCode: config.sourceCode,

			fields: fields.data,
			names: fields.names,
			columns: fields.columns,
			quickFilters: fields.quickFilters,
			filterFields: fields.filterFields,
			requestFields: fields.request,

			links: fields.links,
			valueFor: fields.valueFor,
			valueFrom: fields.valueFrom
		});

		var storeConfig = {
			model: model,

			data: config.data,
			totalCount: config.total,
			summaryData: config.summaryData,
			totalsData: config.totalsData,
			limit: config.limit || 200,

			filters: config.filter,
			where: config.where,
			period: config.period,
			sorter: config.sort,
			values: config.values,

			params: config.params,
			form: form
		};

		this.callParent(storeConfig);
	},

	getFieldsInfo: function(config) {
		config = config || {};
		var result = {};

		var fieldsMap = {};
		result.data = this.createFields(config.fields, fieldsMap);
		result.names = this.createFields(config.nameFields, fieldsMap);
		result.columns = this.createFields(config.columns, fieldsMap);
		result.quickFilters = this.createFields(config.quickFilters, fieldsMap);
		result.filterFields = this.createFields(config.filterFields, fieldsMap);
		result.request = this.createFields(config.requestFields, fieldsMap);
		result.links = this.getFieldsBy(result.data, 'isLink');
		result.valueFor = this.getFieldsBy(result.data, 'valueFor');
		result.valueFrom = this.getFieldsBy(result.data, 'valueFrom');
		var icons = this.getFieldsBy(result.data, 'hasIcon');
		result.iconProperty = icons.length != 0 ? icons[0].name : null;
		result.hash = MD5.hex(Object.keys(fieldsMap).sort().join(';') + config.link + config.primaryKey);

		return result;
	},

	createFields: function(configs, fieldsMap) {
		var result = [];

		if(configs == null || configs.length == 0)
			return result;

		for(var i = 0, length = configs.length; i < length; i++) {
			var config = configs[i];
			var name = config.name;
			if(name == 'icon' || name.endsWith('.icon'))
				config.hasIcon = true;
			var field = fieldsMap[name];
			if(field == null) {
				var type = this.getFieldType(config);
				field = Z8.create(type, config);
			}
			result.push(field);
			fieldsMap[name] = field;
		}
		return result;
	},

	getFieldsBy: function(fields, property) {
		var result = [];

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(field[property] != null)
				result.push(field);
		}

		return result;
	},

	getFieldType: function(config) {
		var type = config.type;

		if(type == Type.Boolean)
			return 'Z8.data.field.Boolean';
		else if(type == Type.Integer)
			return 'Z8.data.field.Integer';
		else if(type == Type.Float)
			return 'Z8.data.field.Float';
		else if(type == Type.Date)
			return 'Z8.data.field.Date';
		else if(type == Type.Datetime)
			return 'Z8.data.field.Datetime';
		else if(type == Type.Guid)
			return 'Z8.data.field.Guid';
		else if(type == Type.File)
			return 'Z8.data.field.File';
		else if(type == Type.Files)
			return 'Z8.data.field.Files';
		else if(type == Type.Json)
			return 'Z8.data.field.Json';
		else
			return 'Z8.data.field.String';
	}
});