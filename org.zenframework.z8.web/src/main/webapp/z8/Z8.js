Ext.isIE6 = false;
Ext.BLANK_IMAGE_URL = 'resources/images/default/s.gif';

guid = {
	Null: '00000000-0000-0000-0000-000000000000'
};

Z8 = {
	viewport: null,

	componentsList: null,

	user: null,
	sessionId: null,

	decimalSeparator: ',',
	integerSeparator: ' ',

	minDate: '31/12/1899',
	minDatetime: '31/12/1899 00:00:00',

	maxDate: '31/12/4712',
	maxDatetime: '31/12/4712 00:00:00',

	charWidth: 6.5,

	emptyString: '&#160;',

	dataProperty: 'data',
	messageProperty: 'message',
	totalProperty: 'total',
	startProperty: 'start',
	limitProperty: 'limit',
	lookupProperty: 'lookup',
	lookupFieldsProperty: 'lookupFields',
	lookupIdProperty: 'lookupId',

	defaultPageCount: 50,

	initialize: function() {
		var userAgent = navigator.userAgent.toLowerCase();
		Ext.isIE9 = Ext.isIE && /msie 9/.test(userAgent);
		Ext.isIE10 = Ext.isIE && /msie 10/.test(userAgent);

		var body = document.body || document.getElementsByTagName('body')[0];

		if(body != null) {
			var bodyEl = Ext.fly(body, '_internal');

			if(Ext.isIE9)
				bodyEl.addClass('ext-ie9');

			if(Ext.isIE10)
				bodyEl.addClass('ext-ie10');
		}
	
		Ext.WindowMgr.zseed = 12000;

		Ext.QuickTips.init(true);
		Ext.form.Field.prototype.msgTarget = 'side';

		Z8.PageManager.regBeforeUnload();
	},

	log: function(message) {
		try { console.log(message); } catch(e) {}
	},

	isEmpty: function(object) {
		if(Ext.isEmpty(object))
			return true;

		if(Ext.isObject(object)) {
			var isEmpty = true;
			Ext.iterate(object, function(key, value, object) { isEmpty = false; return false; });
			return isEmpty; 
		}

		return false;
	},

	arrayCompare: function(array1, array2) {
		if(!Ext.isArray(array1) || !Ext.isArray(array2))
			return false;

		if(array1.length != array2.length)
			return false;

		for(var i = 0; i < array1.length; i++) {
			if(array1[i] != array2[i])
				return false;
		}

		return true;
	},

	stopEvent: function(event) {
		event.stopEvent();
		event.stopPropagation();
	},

	getRequestUrl: function() {
		return getRequestUrl();
	},

	getUserSettings: function() {
		if(Z8.user.encodedSettings == null) {
			try {
				Z8.user.encodedSettings = Ext.decode(Z8.user.settings);
			} catch(e) {}

			if(Z8.user.encodedSettings == null || Ext.isArray(Z8.user.encodedSettings))
				Z8.user.encodedSettings = {};
		}

		return Z8.user.encodedSettings;
	},

	request: {
		url: getRequestUrl() + '/request.json',
		method: 'post',
		desktopId: "desktop"
	},

	Status: {
		AccessDenied: 401
	},

	ServerTypes: {
		Guid: 'guid',
		Integer: 'int',
		Float: 'float',
		Boolean: 'boolean',
		Date: 'date',
		Datetime: 'datetime',
		Datespan: 'datespan',
		String: 'string',
		Binary: 'binary',
		Text: 'text',
		File: 'file'
	},

	GoogleTypes: {
		String: 'string',
		Number: 'number',
		Boolean: 'boolean',
		Date: 'date',
		Datetime: 'datetime',
		Time: 'timeofday',

		fromServerType: function (serverType) {
			switch(serverType) {
			case Z8.ServerTypes.Integer:
			case Z8.ServerTypes.Float:
				return Z8.GoogleTypes.Number;
			case Z8.ServerTypes.Boolean:
				return Z8.GoogleTypes.Boolean;
			case Z8.ServerTypes.Date:
				return Z8.GoogleTypes.Date;
			case Z8.ServerTypes.Datetime:
				return Z8.GoogleTypes.Datetime;
			case Z8.ServerTypes.Datespan:
			case Z8.ServerTypes.String:
			case Z8.ServerTypes.Guid:
			case Z8.ServerTypes.Binary:
			case Z8.ServerTypes.Text:
			default:
				return Z8.Types.String;
			}
		}
	},

	Types: {
		Integer: Ext.data.Types.INTEGER,
		Float: Ext.data.Types.FLOAT,
		Boolean: Ext.data.Types.BOOLEAN,
		Date: Ext.data.Types.DATE,
		String: Ext.data.Types.STRING,

		fromServerType: function (serverType) {
			switch(serverType) {
			case Z8.ServerTypes.Integer:
				return Z8.Types.Integer;
			case Z8.ServerTypes.Float:
				return Z8.Types.Float;
			case Z8.ServerTypes.Boolean:
				return Z8.Types.Boolean;
			case Z8.ServerTypes.Date:
			case Z8.ServerTypes.Datetime:
				return Z8.Types.Date;
			case Z8.ServerTypes.Datespan:
			case Z8.ServerTypes.String:
			case Z8.ServerTypes.Guid:
			case Z8.ServerTypes.Binary:
			case Z8.ServerTypes.Text:
			default:
				return Z8.Types.String;
			}
		},

		dateFormat: function(serverType) {
			switch(serverType) {
			case Z8.ServerTypes.Date:
				return Z8.Format.Date;
			case Z8.ServerTypes.Datetime:
				return Z8.Format.Datetime;
			default:
				return null;
			}
		}
	},

	Grid: {
		xtype: function (type) {
			switch(type) {
			case Z8.Types.Integer:
			case Z8.Types.Float:	
				return 'numbercolumn';
			case Z8.Types.Boolean:
				return 'booleancolumn';
			case Z8.Types.Date:
				return 'datecolumn';
			default:
				return 'gridcolumn';
			}
		},

		fieldWidth: function(width, field) {
			width = width || null;

			var format = field.format;

			if(width != null)
				return width * Z8.charWidth;

			if(field.type == Z8.Types.Date)
				return format != null ? (new Date(2000, 12, 25).format(format).length * Z8.charWidth + 20) : 90;
			else if(field.type == Z8.Types.Boolean)
				return width > 0 ? width * Z8.charWidth : 30;
			else if(field.type == Z8.Types.Integer)
				return format != null ? Ext.util.Format.number(10000000, format).length * Z8.charWidth : 70;
			else if(field.type == Z8.Types.Float)
				return format != null ? Ext.util.Format.number(1000000000.00, format).length * Z8.charWidth : 70;
			else
				return 120;
		},

		newColumn: function(field) {
			var column = {};

			column.id = field.name;
			column.dataIndex = field.name;
			column.header = field.header;
			column.tooltip = field.description || field.header;
			column.width = this.fieldWidth(field.columnWidth || field.width, field);
			column.required = field.required;
			column.stretch = field.stretch;
			column.dataHidable = field.hidable;
			column.index = field.column | 0;

			if(field.hidden === true) {
				column.hidden = true;
				column.hideable = false;
			} else {
				column.hidden = field.visible !== true;

				if(field.required && !field.query.readOnly && !field.readOnly) {
					column.hidden = false;
					column.hideable = false;
				}
			}

			column.linkId = field.linkId;
			column.linkedVia = field.linkedVia;

			column.anchor = field.anchor;
			column.anchorPolicy = field.anchorPolicy;

			column.editWith = field.editWith;
			column.editWithText = field.editWithText;

			if(Ext.isEmpty(column.header))
				column.header = Z8.emptyString;

			if(field.required)
				column.header = '<span style="color:#CC0000;">*</span>&nbsp;' + column.header;

			if(field.format != null)
				column.format = field.format;

			if(field.gridXType != null)
				column.xtype = field.gridXType;

			if(field.type == Z8.Types.Date) {
				column.filter = { type: 'date', dateFormat: field.format };
			} else if(field.type == Z8.Types.Boolean) {
				column.trueText = Z8.Format.TrueText;
				column.falseText = Z8.Format.FalseText;
				column.filter = { type: 'boolean' };
			} else if(field.type == Z8.Types.Integer) {
				column.filter = { type: 'numeric' };
				column.align = 'right';
			} else if(field.type == Z8.Types.Float) {
				column.filter = { type: 'numeric' };
				column.align = 'right';
			} else {
				if(field.serverType == Z8.ServerTypes.Text) {
					column.groupable = false;
					column.filterable = false;
				} else
					column.filter = { type: 'string' };
			}
			return column;
		}
	},

	Form: {
		xtype: function(serverType) {
			switch(serverType) {
			case Z8.ServerTypes.Integer:
			case Z8.ServerTypes.Float:	
				return 'numberfield';
			case Z8.ServerTypes.Boolean:
				return 'checkbox';
			case Z8.ServerTypes.Date:
				return 'datefield';
			case Z8.ServerTypes.Datetime:
				return 'datetimefield';
			case Z8.ServerTypes.Text:
				return 'textarea';
			default:
				return 'textfield';
			}
		},

		newField: function(field) {
			var width = Z8.Grid.fieldWidth(field.width, field);

			var formField = {
				dataIndex: field.name,
				fieldId: field.name,
				linkId: field.linkId,
				linkedVia: field.linkedVia,
				fieldLabel: field.label || field.header,
				readOnly: field.readOnly,
				cls: field.readOnly ? ' x-readonly' : '',
				rowspan: field.rowspan,
				colspan: field.colspan,
				showLabel: field.showLabel,
				stretch: field.stretch,
				width: width,
				minWidth: width,
				labelWidth: field.labelWidth * Z8.charWidth,
				evaluations: field.evaluations,
				dependencies: field.dependencies,
				autoAddRecords: field.autoAddRecords,
				depth: field.depth,
				fieldsToShow: field.fieldsToShow,
				hidable: field.hidable
			};

			if(field.hidden === true)
				formField.hidden = true;

			if(field.filter != null)
				formField.filter = field.filter;

			var minLength = field.min || 0;
			var maxLength = field.max || field.length || 0;

			if(maxLength == minLength && minLength != 0) {
				formField.minLength = minLength;
				formField.maxLength = minLength;
				formField.minLengthText = "Длина поля '" + formField.fieldLabel + "' - строго " + minLength + " символов";
				formField.autoCreate = { tag: 'input', type: 'text', autocomplete: 'off', maxlength: maxLength };
			} else if(maxLength != 0) {
				formField.maxLength = maxLength;
				formField.maxLengthText = "Максимальная длина поля '" + formField.fieldLabel + "' - " + maxLength + " символов";
				formField.autoCreate = { tag: 'input', type: 'text', autocomplete: 'off', maxlength: maxLength };
			} else if(minLength != 0) {
				formField.minLength = minLength;
				formField.minLengthText = "Мининмальная длина поля '" + formField.fieldLabel + "' - " + minLength + " символов";
			}

			if(field.formXType != null)
				formField.xtype = field.formXType;

			if(field.format != null)
				formField.format = field.format;

			if(field.type == Z8.Types.Integer || field.type == Z8.Types.Float)
				formField.align = 'right';

			if(field.serverType == Z8.ServerTypes.Date) {
				formField.minValue = Z8.minDate;
				formField.maxValue = Z8.maxDate;

				formField.plugins = field.readOnly ? null : [new Ext.ux.InputTextMask('X[0|1|2|3]X9/X[0|1]X9/9999', true)];
				formField.emptyText = field.readOnly ? null : '__/__/____';
			}

			if(field.serverType == Z8.ServerTypes.Datetime) {
				formField.minValue = Z8.minDatetime;
				formField.maxValue = Z8.maxDatetime;

				formField.timeFormat = Z8.Format.Time;
				formField.dateFormat = Z8.Format.Date;

				formField.plugins = field.readOnly ? null : [new Ext.ux.InputTextMask('X[0|1|2|3]X9/X[0|1]X9/9999 X[0|1]X9:99:99', true)];
				formField.emptyText = field.readOnly ? null : '__/__/____ __:__:__';
			}

			if(field.type == Z8.Types.Boolean)
				formField.disabled = field.readOnly;

			if(field.serverType == Z8.ServerTypes.Text) {
				formField.height = Math.max(22, (field.lines || 5) * 14 + 6);
				formField.labelAlign = 'top';
			}
			return formField;
		}
	},

	Format: {
		Date: 'd/m/Y',
		LongDate: 'j F Y',
		Time: 'G:i:s',
		Datetime: 'd/m/Y G:i:s',
		Integer: '0,000',
		Float: '0,000.00',
		TrueText: 'да',
		FalseText: 'нет',

		nl2br: function(message) {
			var result = null;
			if(Ext.isArray(message)) {
				var result = [];
				for(var i = 0; i < message.length; i++)
					result.push(message[i].replace(/\\n/g, '<br>'));
			} else
				result = message.replace(/\\n/g, '<br>');

			return result;
		},

		isoDate: function(date) {
			if(Ext.isEmpty(date))
				return '';

			var zoneOffset = -date.getTimezoneOffset();

			var year = date.getFullYear();
			var month = date.getMonth() + 1;
			var day = date.getDate();
			var hours = date.getHours();
			var minutes = date.getMinutes();
			var seconds = date.getSeconds();

			var offset = '';
			if(zoneOffset != 0) {
				var zoneHours = Math.floor(zoneOffset / 60);
				var zoneMinutes = zoneOffset % 60;
				offset = (zoneOffset >= 0 ? '+' : '-') +
					(zoneHours < 10 ? '0' : '') + zoneHours + ':' +
					(zoneMinutes < 10 ? '0' : '') + zoneMinutes;
			}

			return year + '-' +
				(month < 10 ? '0' : '') + month + '-' +
				(day < 10 ? '0' : '') + day + 'T' +
				(hours < 10 ? '0' : '') + hours + ':' +
				(minutes < 10 ? '0' : '') + minutes +
				(seconds != 0 ? ':' + (seconds < 10 ? '0' : '') + seconds : '') + offset;
		}
	},

	Report: {
		pdf: 'pdf',
		xls: 'xls',
		doc: 'doc',
		unknown: 'unknown',

		acrobat: 'Acrobat Reader',
		excel: 'Microsoft Excel',
		word: 'Microsoft Word',

		acrobatIcon: 'silk-page-white-acrobat',
		excelIcon: 'silk-page-excel',
		wordIcon: 'silk-page-word',
		unknownIcon: 'silk-page',

		icons: {},
		names: {}
	},

	decode: function(value, serverType) {
		var format = Z8.Types.dateFormat(serverType);
		return format != null ? (!Ext.isEmpty(value) ? new Date(value) : '') : value;
	},

	showMessages: function(title, messages) {
		text = '';

		if(Ext.isArray(messages)) {
			for(var i = 0; i < messages.length; i++)
				text += (i != 0 ? '<br>' : '') + messages[i];
		} else
			text = messages;

		if(Z8.isEmpty(text))
			return;

		if(Z8.messagesContainer == null)
			Z8.messagesContainer = Ext.DomHelper.insertFirst(document.body, { id: 'x-messages-container' }, true);

		Z8.messagesContainer.alignTo(document, 't-t');

		var boxMarkup = ['<div class="msg">',
			'<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
			'<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', title, '</h3>', text, '</div></div></div>',
			'<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
			'</div>'].join('');

		var m = Ext.DomHelper.append(Z8.messagesContainer, { html: boxMarkup }, true);
		m.slideIn('t').pause(5).ghost('t', {remove:true});
	}
};

Z8.Report.names[Z8.Report.pdf] = Z8.Report.acrobat;
Z8.Report.names[Z8.Report.xls] = Z8.Report.excel;
Z8.Report.names[Z8.Report.doc] = Z8.Report.word;

Z8.Report.icons[Z8.Report.pdf] = Z8.Report.acrobatIcon;
Z8.Report.icons[Z8.Report.xls] = Z8.Report.excelIcon;
Z8.Report.icons[Z8.Report.doc] = Z8.Report.wordIcon;
Z8.Report.icons[Z8.Report.unknown] = Z8.Report.unknownIcon;

function getRequestUrl() {
	var url = window.document.URL;
	var index = url.lastIndexOf('/');

	if(index != -1)
		url = url.substring(0, index);

	return url;
}

function getRequestUrlParameters() {
	var url = window.document.URL;

	var index = url.indexOf('#');

	if(index != -1)
		url = url.substring(0, index);

	index = url.indexOf('?');

	var result = {};

	if(index != -1) {
		var params = url.substring(index + 1).split('&');

		for(var i = 0; i < params.length; i++) {
			var p = params[i].split('=');

			var name = p[0];
			var value = p.length > 1 ? p[1] : '';

			if(!Z8.isEmpty(name))
				result[name] = value;
		}
	}

	return result;
}

function getRequestDomain() {
	var url = window.location.hostname;
	var index = url.lastIndexOf('www.');
	if(index != -1)
		url = url.substring(index+4);

	return url;
}

function cyr2lat(str) {
	var cyr2latChars = new Array(
			['а', 'a'], ['б', 'b'], ['в', 'v'], ['г', 'g'],
			['д', 'd'],  ['е', 'e'], ['ё', 'yo'], ['ж', 'zh'], ['з', 'z'],
			['и', 'i'], ['й', 'y'], ['к', 'k'], ['л', 'l'],
			['м', 'm'],  ['н', 'n'], ['о', 'o'], ['п', 'p'],  ['р', 'r'],
			['с', 's'], ['т', 't'], ['у', 'u'], ['ф', 'f'],
			['х', 'h'],  ['ц', 'c'], ['ч', 'ch'],['ш', 'sh'], ['щ', 'shch'],
			['ъ', ''],  ['ы', 'y'], ['ь', ''],  ['э', 'e'], ['ю', 'yu'], ['я', 'ya'],

			['А', 'A'], ['Б', 'B'],  ['В', 'V'], ['Г', 'G'],
			['Д', 'D'], ['Е', 'E'], ['Ё', 'YO'],  ['Ж', 'ZH'], ['З', 'Z'],
			['И', 'I'], ['Й', 'Y'],  ['К', 'K'], ['Л', 'L'],
			['М', 'M'], ['Н', 'N'], ['О', 'O'],  ['П', 'P'],  ['Р', 'R'],
			['С', 'S'], ['Т', 'T'],  ['У', 'U'], ['Ф', 'F'],
			['Х', 'H'], ['Ц', 'C'], ['Ч', 'CH'], ['Ш', 'SH'], ['Щ', 'SHCH'],
			['Ъ', ''],  ['Ы', 'Y'], ['Ь', ''], ['Э', 'E'], ['Ю', 'YU'], ['Я', 'YA'],

			['a', 'a'], ['b', 'b'], ['c', 'c'], ['d', 'd'], ['e', 'e'],
			['f', 'f'], ['g', 'g'], ['h', 'h'], ['i', 'i'], ['j', 'j'],
			['k', 'k'], ['l', 'l'], ['m', 'm'], ['n', 'n'], ['o', 'o'],
			['p', 'p'], ['q', 'q'], ['r', 'r'], ['s', 's'], ['t', 't'],
			['u', 'u'], ['v', 'v'], ['w', 'w'], ['x', 'x'], ['y', 'y'],
			['z', 'z'],

			['A', 'A'], ['B', 'B'], ['C', 'C'], ['D', 'D'],['E', 'E'],
			['F', 'F'],['G', 'G'],['H', 'H'],['I', 'I'],['J', 'J'],['K', 'K'],
			['L', 'L'], ['M', 'M'], ['N', 'N'], ['O', 'O'],['P', 'P'],
			['Q', 'Q'],['R', 'R'],['S', 'S'],['T', 'T'],['U', 'U'],['V', 'V'],
			['W', 'W'], ['X', 'X'], ['Y', 'Y'], ['Z', 'Z'],

			[' ', '-'],['0', '0'],['1', '1'],['2', '2'],['3', '3'],
			['4', '4'],['5', '5'],['6', '6'],['7', '7'],['8', '8'],['9', '9'],
			['-', '-']
	);

	var newStr = new String();

	for (var i = 0; i < str.length; i++) {

		ch = str.charAt(i);
		var newCh = '';

		for (var j = 0; j < cyr2latChars.length; j++) {
			if (ch == cyr2latChars[j][0])
				newCh = cyr2latChars[j][1];
		}
		newStr += newCh;
	}
	return newStr.replace(/[-]{2,}/gim, '-').replace(/\n/gim, '');
}


Z8.getEl = function (el, skipDeep) {
	if(Ext.isEmpty(el, false))
		return null;
	if(el.isComposite)
		return el;
	if(el.getEl)
		return el.getEl();
	if(el.el)
		return el.el;

	var cmp = Ext.getCmp(el);
	if (!Ext.isEmpty(cmp))
		return cmp.getEl();

	var tEl = Ext.get(el);

	if (Ext.isEmpty(tEl) && skipDeep !== true) {
		try {
			return Z8.getEl(eval("(" + el + ")"), true);
		} catch (e) {}
	}

	return tEl;
};

Z8.clone = function (o) {
	if (!o || "object" !== typeof o)
		return o;

	var c = "[object Array]" === Object.prototype.toString.call(o) ? [] : {}, p, v;

	for(p in o) {
		if (o.hasOwnProperty(p)) {
			v = o[p];
			c[p] = (v && "object" === typeof v) ? Z8.clone(v) : v;
		}
	}
	return c;
};

Z8.ProxyDDCreator = function (config) {

	Z8.ProxyDDCreator.superclass.constructor.call(this, config);

	this.config = config || {};

	if (!Ext.isEmpty(this.config.target, false)) {
		var targetEl = Z8.getEl(this.config.target);

		if (!Ext.isEmpty(targetEl)) {
			this.initDDControl(targetEl);
		} else {
			this.task = new Ext.util.DelayedTask(function () {
				targetEl = Z8.getEl(this.config.target);
				if (!Ext.isEmpty(targetEl)) {
					this.task.cancel();
					this.initDDControl(targetEl);
				} else
					this.task.delay(500);
			}, this);
			this.task.delay(1);
		}
	}
};

Ext.extend(Z8.ProxyDDCreator, Ext.util.Observable, {
	initDDControl: function (target) {
		target = Z8.getEl(target);
		if (target.isComposite) {
			this.ddControl = [];
			target.each(function (targetEl) {
				this.ddControl.push(this.createControl(Ext.apply(Z8.clone(this.config), { id: Ext.id(targetEl) })));
			}, this);
		} else {
			this.ddControl = this.createControl(Ext.apply(Z8.clone(this.config), {
				id: Ext.id(target)
			}));
		}
	},

	createControl: function (config) {
		var ddControl;
		if (config.group) {
			ddControl = new config.type(config.id, config.group, config.config);
			Ext.apply(ddControl, config.config);
		} else
			ddControl = new config.type(config.id, config.config);
		return ddControl;
	},

	lock: function () {
		Ext.each(this.ddControl, function (dd) {
			if (dd && dd.lock)
				dd.lock();
		});
	},

	unlock: function () {
		Ext.each(this.ddControl, function (dd) {
			if (dd && dd.unlock) {
				dd.unlock();
			}
		});
	},

	unreg: function () {
		Ext.each(this.ddControl, function (dd) {
			if (dd && dd.unreg)
				dd.unreg();
		});
	},

	destroy: function () {
		Ext.each(this.ddControl, function (dd) {
			if (dd && dd.unreg)
				dd.unreg();
		});
	}
});

Z8.Callback = Ext.extend(Object, {
	fn: Ext.emptyFn,
	scope: null,
	options: null,

	call: function() {
		var args = [];

		if(Ext.isArray(this.options))
			args = [].concat(this.options);
		else if(this.options != null)
			args = [this.options];

		if(arguments != null) {
			for(var i = 0; i < arguments.length; i++)
				args.push(arguments[i]);
		}

		this.fn.apply(this.scope, args);
	}
});

Z8.Callback.create = function(fn, scope, options) {
	var callback = new Z8.Callback();
	callback.fn = fn;
	callback.scope = scope;
	callback.options = options;

	return callback;
};

Ext.ns('Z8.chart');
Ext.ns('Z8.data');
Ext.ns('Z8.view');
Ext.ns('Z8.form');
Ext.ns('Z8.grid');
Ext.ns('Z8.tree');
Ext.ns('Z8.menu');
Ext.ns('Z8.layout');
Ext.ns('Z8.layout.boxOverflow');
Ext.ns('Z8.query');
Ext.ns('Z8.desktop');
Ext.ns('Z8.login');
Ext.ns('Z8.view');
Ext.ns('Z8.evaluations');
Ext.ns('Z8.messenger');
Ext.ns('Z8.console');

Ext.ns('Z8.Ajax');
Ext.ns('Z8.ReportManager');
Ext.ns('Z8.FileViewer');
Ext.ns('Z8.TaskManager');
Ext.ns('Z8.LoginManager');
Ext.ns('Z8.ChartManager');
Ext.ns('Z8.Messenger');
Ext.ns('Z8.Console');
Ext.ns('Z8.Helper');
Ext.ns('Z8.Periods');