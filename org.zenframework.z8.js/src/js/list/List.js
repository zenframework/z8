Z8.define('Z8.list.List', {
	extend: 'Z8.Component',

	checks: true,
	autoFit: true,

	headers: null,
	hideHeaders: false,
	numbers: false,
	items: null,
	totals: false,

	headerType: null, // default 'Z8.list.Header',
	itemType: null,   // default 'Z8.list.Item',

	store: null,
	name: 'id',
	fields: 'name',

	visible: true,

	editable: false,

	useTAB: false,
	useENTER: true,

	confirmSelection: false,
	autoSelectFirst: true,

	itemsRendered: false,
	manualItemsRendering: false,

	ordinals: null,
	filterVisible: false,
	lastEditedColumn: null,

	tabIndex: 0,

	constructor: function(config) {
		this.callParent(config);
	},

	initComponent: function() {
		this.callParent();

		if(this.startCollapsed == null)
			this.startCollapsed = Application.listbox.collapsed;

		var items = this.items = this.items || [];
		this.fragments = [];

		this.fields = this.createFields(this.fields || 'name');
		this.headers = this.createHeaders();
		this.numbers = this.createNumbers();
		this.filters = this.createQuickFilters();
		this.totals = this.createTotals();

		this.initItems();
		this.initStore();
		this.editors = this.createEditors();
	},

	initItems: function() {
		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++)
			items[i].list = this;
	},

	initStore: function() {
		var store = this.store;
		this.store = null;
		this.setStore(store);
	},

	setStore: function(store) {
		if(this.store == store)
			return;

		if(this.store != null) {
			this.store.un('load', this.onStoreLoad, this);
			this.store.un('add', this.onStoreAdd, this);
			this.store.un('remove', this.onStoreRemove, this);
			this.store.un('totals', this.onStoreTotals, this);
			this.store.un('idChange', this.onStoreIdChange, this);
			this.store.dispose();
		}

		this.store = store;

		if(store == null)
			return;

		store.use();

		store.on('load', this.onStoreLoad, this);
		store.on('add', this.onStoreAdd, this);
		store.on('remove', this.onStoreRemove, this);
		store.on('totals', this.onStoreTotals, this);
		store.on('idChange', this.onStoreIdChange, this);

		if(store.isLoaded())
			this.setRecords(store.getRecords());
	},

	onStoreLoad: function(store, records, success) {
		if(success) {
			this.setRecords(records);
			this.updateSort();
			this.updateFilter();
			this.updateChecks();
		}
	},

	onStoreAdd: function(store, records, index) {
		this.addRecords(records, index);
	},

	onStoreRemove: function(store, records) {
		this.removeRecords(records);
	},

	onStoreTotals: function(store, totals, success) {
		if(success)
			this.setTotals(totals);
	},

	onStoreIdChange: function(store, record, oldId) {
		if(this.getValue() == oldId)
			this.setValue(record.id);
		this.resetOrdinals();
	},

	createEditors: function() {
		var editors = [];

		if(this.store == null)
			return editors;

		var fields = this.fields;

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var type = field.type;

			if(!field.editable || type == null || type == Type.Boolean) {
				editors.push(null);
				continue;
			}

			var editor = field.editor;

			if(editor == null) {
				var config = Z8.apply({}, field);
				config.label = false;
				config.type = type != Type.String ? type : Type.Text;
				config.enterOnce = true;
				config.height = null;
				editor = Z8.form.Helper.createControl(config);
			}

			editor.on('change', this.onItemEditorChange, this);
			editor.index = i;
			editors.push(editor);
		}

		return editors;
	},

	setEditable: function(editable) {
		this.editable = editable;
	},

	headersMarkup: function() {
		var headerCells = [];
		var numberCells = [];
		var filterCells = [];

		var headers = this.headers;

		var numbers = this.numbers;
		var hasNumbers = this.hasNumbers();

		var filters = this.filters;
		var hasFilters = this.hasQuickFilters();

		for(var i = 0, length = headers.length; i < length; i++) {
			headerCells.push(headers[i].htmlMarkup());
			if(hasNumbers)
				numberCells.push(numbers[i].htmlMarkup());
			if(hasFilters)
				filterCells.push(filters[i].htmlMarkup());
		}

		headerCells.push({ tag: 'td', cls: 'extra', style: 'width: 0;' });

		var result = [{ tag: 'tr', cls: 'header', cn: headerCells }];

		if(hasNumbers)
			result.push({ tag: 'tr', cls: 'number', cn: numberCells });

		if(hasFilters)
			result.push({ tag: 'tr', cls: 'filter' + (this.filterVisible ? '' : ' display-none'), cn: filterCells });

		return result;
	},

	itemsMarkup: function(items) {
		var markup = [];

		var items = items || this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			item = item == '-' ? new Z8.list.Divider() : item;
			markup.push(item.htmlMarkup());
		}

		this.itemsRendered = true;

		return markup;
	},

	totalsMarkup: function() {
		var cells = [];

		var totals = this.totals;

		for(var i = 0, length = totals.length; i < length; i++)
			cells.push(totals[i].htmlMarkup());

		cells.push({ tag: 'td', cls: 'extra', style: 'width: 0;' });

		return [{ tag: 'tr', cls: 'total', cn: cells }];
	},

	tableMarkup: function(type) {
		var hasHeaders = this.headers.length != 0;

		var width = hasHeaders ? this.getTotalWidth() : 0;
		var style = width != 0 ? 'width: ' + width + 'em;' : '';
		var colGroup = hasHeaders ? { tag: 'colGroup', cn: this.colGroupMarkup(type) } : null;

		var rows = [];
		if(type == 'headers')
			rows = this.headersMarkup();
		else if(type == 'totals')
			rows = this.totalsMarkup();
		else if(type == 'items' && !this.manualItemsRendering)
			rows = this.itemsMarkup();

		var tbody = { tag: 'tbody', cn: rows };
		return { tag: 'table', style: style, cn: colGroup != null ? [colGroup, tbody] : [tbody] };
	},

	colGroupMarkup: function(type) {
		var headers = this.headers;

		var markup = [];

		for(var i = 0, length = headers.length; i < length; i++)
			markup.push({ tag: 'col', style: 'width: ' + headers[i].getWidth() + 'em;' });

		if(type != 'items')
			markup.push({ tag: 'col', cls: 'extra', style: 'width: 0;' });

		return markup;
	},

	htmlMarkup: function() {
		var hasHeaders = this.headers.length != 0;
		var hasTotals = this.totals.length != 0;

		var cls = DOM.parseCls(this.cls).pushIf('list');
		if(hasHeaders)
			cls.pushIf('columns');

		if(hasTotals)
			cls.pushIf('totals');

		if(this.autoFit)
			cls.pushIf('auto-fit');

		var cn = [];

		if(hasHeaders) {
			var headers = this.tableMarkup('headers');
			var headersScroller = { cls: 'scroller headers' + (this.hideHeaders ? ' display-none' : ''), cn: [headers] };
			cn.push(headersScroller);
		}

		var items = this.tableMarkup('items');
		var itemsScroller = { cls: 'scroller items', cn: [items] };
		cn.push(itemsScroller);

		if(hasTotals) {
			var totals = this.tableMarkup('totals');
			var totalsScroller = { cls: 'scroller totals', cn: [totals] };
			cn.push(totalsScroller);
		}

		return { id: this.getId(), tabIndex: this.getTabIndex(), cls: cls.join(' '), cn: cn };
	},

	subcomponents: function() {
		return this.headers.concat(this.filters).concat(this.itemsRendered ? this.items : []).concat(this.totals);
	},

	completeRender: function() {
		this.callParent();

		if(this.itemsTable == null) {
			var itemsScroller = this.itemsScroller = this.selectNode('.scroller.items');
			var headersScroller = this.headersScroller = this.selectNode('.scroller.headers');
			var totalsScroller = this.totalsScroller = this.selectNode('.scroller.totals');

			this.headersTable = this.selectNode('.scroller.headers>table');
			this.headerCols = this.queryNodes('.scroller.headers>table>colgroup>col') || [];
			this.filter = this.selectNode('.scroller.headers tr.filter');

			this.totalsTable = this.selectNode('.scroller.totals>table');
			this.totalCols = this.queryNodes('.scroller.totals>table>colgroup>col') || [];

			this.itemsTable = this.selectNode('.scroller.items>table');
			this.itemsTableBody = this.selectNode('.scroller.items>table>tbody');
			this.itemCols = this.queryNodes('.scroller.items>table>colgroup>col');

			DOM.on(this, 'keyDown', this.onKeyDown, this);

			if(headersScroller != null) {
				DOM.on(window, 'resize', this.onResize, this);
				DOM.on(totalsScroller, 'scroll', this.onScroll, this);
				DOM.on(itemsScroller, 'scroll', this.onScroll, this);
			}
		}
	},

	afterRender: function() {
		this.callParent();

		if(!this.manualItemsRendering) {
			var item = this.getCurrentItem() || (this.autoSelectFirst ? 0 : null);
			this.selectItem(item, this.autoSelectFirst);
		}

		this.adjustAutoFit();
	},

	getTotalWidth: function() {
		var width = 0;

		var headers = this.headers;
		for(var i = 0, length = headers.length; i < length; i++)
			width += headers[i].getWidth();

		return width;
	},

	setAutoFit: function(autoFit, silent) {
		this.autoFit = autoFit;
		DOM.swapCls(this, autoFit, 'auto-fit');
		this.adjustAutoFit();

		if(!this.silent)
			this.fireEvent('autoFit', this, autoFit);
	},

	hasVScrollbar: function() {
		return this.itemsScroller.clientHeight < this.itemsTable.clientHeight;
	},

	hasHScrollbar: function() {
		return this.itemsScroller.clientWidth < this.itemsTable.clientWidth;
	},

	getScrollbarWidth: function() {
		var scroller = this.itemsScroller;
		var scrollbarWidth = new Rect(scroller).width - DOM.getClientWidth(scroller);
		return Math.max(scrollbarWidth, 0);
	},

	adjustAutoFit: function() {
		if(!this.isActive())
			return;

		var headers = this.headers;
		var itemsScroller = this.itemsScroller;

		if(this.headers.length == 0 || itemsScroller == null)
			return;

		if(!this.autoFit) {
			this.adjustScrollers();
			return;
		}

		var fixedWidth = 0;
		var flexibleWidth = 0;
		var totalWidth = 0;

		var headerCols = this.headerCols;
		var totalCols = this.totalCols;
		var itemCols = this.itemCols;

		var flexible = [];

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var width = header.getWidth();
			totalWidth += width;
			if(!header.fixed) {
				flexibleWidth += width;
				flexible.push(i);
			} else
				fixedWidth += width;
		}

		var clientWidth = new Rect(itemsScroller).width - this.getScrollbarWidth();

		if(clientWidth - fixedWidth <= 0 || totalWidth == fixedWidth)
			return;

		do {
			var continueAdjusting = false;
			var ratio = (clientWidth - fixedWidth) / flexibleWidth;
			flexibleWidth = 0;
			for(var i = 0, length = flexible.length; i < length; i++) {
				var index = flexible[i];
				var header = headers[index];
				var width = header.getWidth();
				var minWidth = header.getMinWidth();

				if(header.hidden || width == minWidth && header.adjusted)
					continue;

				width = Math.max(minWidth, (i != flexible.length - 1) ? header.getWidth() * ratio : (clientWidth - fixedWidth - flexibleWidth));

				if(width == minWidth) {
					continueAdjusting = true;
					fixedWidth += width;
				} else
					flexibleWidth += width;

				header.adjusted = true;

				header.setWidth(width);
			}
		} while(continueAdjusting);

		for(var i = 0, length = flexible.length; i < length; i++) {
			var index = flexible[i];
			var header = headers[index];
			var width = header.getWidth();
			DOM.setPoint(headerCols[index], 'width',  width);
			DOM.setPoint(itemCols[index], 'width',  width);
			DOM.setPoint(totalCols[index], 'width',  width);

			header.adjusted = false;
		}

		var width = fixedWidth + flexibleWidth;
		DOM.setPoint(this.itemsTable, 'width',  width);

		this.adjustScrollers(width);
	},

	adjustScrollers: function(width) {
		var headers = this.headers;
		var itemsScroller = this.itemsScroller;

		if(headers.length == 0 || itemsScroller == null)
			return;

		var headerCols = this.headerCols;
		var totalCols = this.totalCols;

		width = width || Ems.pixelsToEms(parseFloat(DOM.getComputedStyle(this.itemsTable).width));
		var scrollbarWidth = this.getScrollbarWidth();

		DOM.setPoint(headerCols[headerCols.length - 1], 'width',  scrollbarWidth);
		DOM.setPoint(totalCols[totalCols.length - 1], 'width',  scrollbarWidth);

		DOM.setPoint(this.headersTable, 'width',  width + scrollbarWidth);
		DOM.setPoint(this.totalsTable, 'width',  width + scrollbarWidth);

		this.onScroll();
	},

	renderItems: function() {
		if(this.itemsRendered && !this.itemsAdded || this.getDom() == null)
			return false;

		var items = this.getItems();

		if(!this.itemsRendered) {
			DOM.append(this.itemsTableBody, this.itemsMarkup());
			this.renderDone();
			return true;
		} 

		if(!this.itemsAdded)
			throw 'List: invalid render state';

		var fragments = this.fragments;

		for(var i = 0, length = fragments.length; i < length; i++) {
			var fragment = fragments[i];
			var items = fragment.items;
			var parents = this.getParents(items);

			var before = DOM.get(this.itemsTableBody).childNodes[fragment.index];
			if(before != null)
				DOM.insertBefore(before, this.itemsMarkup(items));
			else
				DOM.append(this.itemsTableBody, this.itemsMarkup(items));

			this.updateCollapser(parents);
		}

		this.fragments = [];
		this.itemsAdded = false;

		this.renderDone();
		return true;
	},

	onDestroy: function() {
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		DOM.un(this.totalsScroller, 'scroll', this.onScroll, this);
		DOM.un(this.itemsScroller, 'scroll', this.onScroll, this);
		DOM.un(window, 'resize', this.onResize, this);

		this.setStore(null);

		Component.destroy(this.editors);

		this.callParent();

		this.itemsScroller = this.headersScroller = this.totalsScroller = null;
		this.headersTable = this.headerCols = this.filter = this.totalsTable = null;
		this.totalCols = this.itemsTable = this.itemsTableBody = this.itemCols = null;
		this.items = this.fields = this.fragment = this.headers = this.checkHeader = null;
		this.filters = this.totals = this.editors = null;
	},

	getStore: function() {
		return this.store;
	},

	isLoaded: function() {
		return this.store != null ? this.store.isLoaded() : (this.getCount() != 0);
	},

	load: function(callback, filter) {
		var store = this.store;

		if(store == null) {
			this.setItems(this.getItems());
			return;
		}

		filter == null ? store.load(callback) : store.filter(filter, callback);
	},

	clearFilter: function() {
		var store = this.getStore();
		store.setFilter([]);
		store.unload();
	},

	createHeader: function(field, cls) {
		var type = this.headerType || (field.type != Type.Boolean || field.columnHeader ? 'Z8.list.Header' : 'Z8.list.HeaderIcon');
		return Z8.create(type, { list: this, field: field, cls: cls });
	},

	createNumber: function(number, cls) {
		var type = this.numberType || 'Z8.list.Number';
		return Z8.create(type, { list: this, number: number, cls: cls });
	},

	createTotal: function(field, cls) {
		var type = this.totalType || 'Z8.list.Total';
		return Z8.create(type, { list: this, field: field, cls: cls });
	},

	hasNumbers: function() {
		return this.numbers.length != 0;
	},

	hasQuickFilters: function() {
		return this.filters.length != 0;
	},

	createQuickFilter: function(field, cls) {
		var filter = new Z8.list.HeaderFilter({ list: this, field: field, cls: cls });
		filter.on('focusIn', this.onQuickFilterFocusIn, this);
		filter.on('focusOut', this.onQuickFilterFocusOut, this);
		return filter;
	},

	createHeaders: function() {
		var headers = [];
		var fields = this.fields;

		if(fields.length == 0 || this.headers === false)
			return headers;

		if(this.checks) {
			var header = this.checkHeader = new Z8.list.HeaderCheck({ list: this });
			headers.push(header);
		}

		if(this.locks) {
			header = new Z8.list.HeaderIcon({ list: this, cls: 'lock', icon: 'fa-lock' });
			headers.push(header);
		}

		var store = this.store;
		var sorter = store != null && store.getSorter != null ? store.getSorter() : [];
		sorter = sorter.length != 0 ? sorter[0] : null;

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var isSortHeader = sorter != null && field.name == sorter.property && field.sortable !== false;
			field.sortDirection = isSortHeader ? sorter.direction : null;
			var header = this.createHeader(fields[i], cls);
			if(isSortHeader)
				this.sortHeader = header;
			headers.push(header);
		}

		return headers;
	},

	createNumbers: function() {
		var numbers = [];
		var fields = this.fields;

		if(fields.length == 0 || this.numbers === false)
			return numbers;

		if(this.checks) {
			var number = this.checkHeader = new Z8.list.HeaderIcon({ list: this });
			numbers.push(number);
		}

		if(this.locks) {
			number = new Z8.list.HeaderIcon({ list: this });
			numbers.push(number);
		}

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var number = this.createNumber(i + 1, cls);
			numbers.push(number);
		}

		return numbers;
	},

	createTotals: function() {
		var totals = [];

		if(!this.totals)
			return totals;

		if(this.checks) {
			var total = new Z8.list.HeaderIcon({ list: this, title: 'Σ' });
			totals.push(total);
		}

		if(this.locks) {
			var total = new Z8.list.HeaderIcon({ list: this, title: this.checks ? '' : 'Σ' });
			totals.push(total);
		}

		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var total = this.createTotal(fields[i], cls);
			totals.push(total);
		}

		return totals;
	},

	createQuickFilters: function() {
		var filters = [];
		var headers = this.headers;

		if(headers.length == 0 || this.filters === false)
			return filters;

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var filter = this.createQuickFilter(field, cls);
			filters.push(filter);
		}

		return filters;
	},

	createItem: function(record) {
		var config = { list: this, record: record, name: this.name || 'id', icon: this.icons ? '' : null, collapsed: this.startCollapsed };
		if (this.itemConfig != null)
			config = Z8.apply(config, this.itemConfig);
		return this.itemType != null ? Z8.create(this.itemType, config) : new  Z8.list.Item(config);
	},

	createItems: function(records) {
		var items = [];

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			var item = this.createItem(record); 
			items.push(item);
		}

		return items;
	},

	createFields: function(fields) {
		if(fields == null)
			return [];

		if(String.isString(fields))
			return [{ name: fields, header: fields }];

		if(Array.isArray(fields)) {
			var result = [];
			for(var i = 0, length = fields.length; i < length; i++) {
				var field = fields[i];
				result.push(String.isString(field) ? { name: field } : field);
			}
			return result;
		}

		return [fields];
	},

	setFields: function(fields) {
/*
		this.fields = this.createFields(fields);
		this.setRecords();
*/
	},

	setRecords: function(records) {
		var items = this.createItems(records || this.store.getRecords());

		this.setItems(items);

		if(this.isVisible())
			this.renderItems();
	},

	addRecords: function(records, index) {
		var items = this.createItems(records || this.store.geRecords());
		this.addItems(items, index, true);

		if(this.isVisible())
			this.renderItems();
	},

	removeRecords: function(records) {
		var items = [];
		for(i = 0, length = records.length; i < length; i++)
			items.push(this.getItem(records[i].id));

		this.removeItems(items);
	},

	setTotals: function(record) {
		var totals = this.totals;

		if(totals.length == 0)
			return;

		var start = 0 + (this.checks ? 1 : 0) + (this.locks ? 1 : 0);

		for(var i = start, length = totals.length; i < length; i++) {
			var total = totals[i];
			totals[i].setText(record.get(total.field.name));
		}
	},

	getItems: function() {
		return this.items || [];
	},

	getCount: function() {
		return this.getItems().length;
	},

	getAt: function(index) {
		var items = this.getItems();
		return index < items.length ? items[index] : null;
	},

	getItem: function(value) {
		var index = this.getIndex(value);
		return index != -1 ? this.getAt(index) : null;
	},

	getIndex: function(value) {
		var ordinals = this.getOrdinals();

		if(value instanceof Z8.list.Item) {
			var index = ordinals[value.getValue()];
			return index != null ? index : -1;
		}

		index = ordinals[value];
		return index != null ? index : -1;
	},

	getOrdinals: function() {
		if(this.ordinals != null)
			return this.ordinals;

		var ordinals = this.ordinals = {};

		var items = this.items;

		if(items == null)
			return ordinals;

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(!item.isComponent)
				continue;

			var value = item.getValue != null ? item.getValue() : null;
			if(value != null)
				ordinals[value] = i;

			ordinals[item.getId()] = i;
		}

		return ordinals;
	},

	resetOrdinals: function() {
		this.ordinals = null;
	},

	getTreeState: function() {
		if(this.items == null || !this.isTree())
			return null;

		var state = {};
		for(var i = 0, items = this.items, length = items.length; i < length; i++) {
			var item = items[i];
			state[item.record.id] = item.isCollapsed();
		}

		return state;
	},

	applyTreeState: function(state) {
		if(state == null || this.items == null)
			return;

		for(var i = 0, items = this.items, length = items.length; i < length; i++) {
			var item = items[i];
			var collapsed = state[item.record.id];
			if(collapsed != null)
				item.collapsed = collapsed;
		}
	},
	
	getFields: function() {
		return this.fields;
	},

	getHeaders: function() {
		return this.headers;
	},

	getEditors: function() {
		return this.editors;
	},

	getField: function(name) {
		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(field.name == name)
				return field;
		}
		return null;
	},

	getEditor: function(name) {
		var editors = this.editors;
		for(var i = 0, length = editors.length; i < length; i++) {
			var editor = editors[i];
			if(editor != null && (editor.name == name || editor.displayName == name))
				return editor;
		}
		return null;
	},

	getHeader: function(name) {
		var headers = this.headers;
		var start = 0 + (this.checks ? 1 : 0) + (this.locks ? 1 : 0);
		for(var i = start, length = headers.length; i < length; i++) {
			var header = headers[i];
			if(header.field.name == name)
				return header;
		}
		return null;
	},

	getHeaderIndex: function(header) {
		var headers = this.headers;
		for(var i = 0, length = headers.length; i < length; i++) {
			if(header == headers[i])
				return i;
		}
		return -1;
	},

	getValue: function() {
		return this.value;
	},

	setValue: function(value) {
		this.value = value;
	},

	addItems: function(items, index, attached) {
		if(!attached) {
			for(var i = 0, length = items.length; i < length; i++)
				items[i].list = this;
		}

		this.items.insert(items, index);
		this.resetOrdinals();

		if(this.itemsRendered) {
			this.fragments.push({ index: index, items: items });
			this.itemsAdded = true;
		}

		this.onContentChange();
	},

	removeItems: function(items) {
		var parents = this.getParents(items);

		this.resetOrdinals();
		Component.destroy(items);
		this.items.removeAll(items);

		this.updateCollapser(parents);

		this.adjustAutoFit();

		this.onContentChange();
	},

	setItems: function(items) {
		if(this.items == items)
			return;

		var treeState = this.getTreeState();

		this.clear();
		this.items = items;

		this.applyTreeState(treeState);
		this.resetOrdinals();

		this.onContentChange();
	},

	onContentChange: function() {
		this.fireEvent('contentChange', this);
	},
	
	clear: function() {
		Component.destroy(this.items);
		this.items = this.fragments = [];
		this.itemsRendered = this.itemsAdded = false;
	},

	findItem: function(node) {
		var index = this.findItemIndex(node);
		return index != -1 ? this.getAt(index) : null;
	},

	findItemIndex: function(node) {
		var topmost = document.body;

		while(node != null && node.nodeType == 1 && node !== topmost) {
			var index = this.getIndex(node.id);
			if(index != -1)
				return index;
			node = node.parentNode;
		}

		return -1;
	},

	getItemByTarget: function(target) {
		while(target != null) {
			var item = target.listItem;
			if(item != null)
				return item.getList() == this ? item : null;
			target = target.parentNode;
		}
		return null;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		var item = this.getCurrentItem();

		if(item != null)
			item.setTabIndex(tabIndex);

		if(DOM.get(this) == null)
			this.tabIndex = this.getCount() == 0 ? tabIndex : -1;
		else
			DOM.setTabIndex(this, this.getCount() == 0 ? tabIndex : -1);

		return tabIndex;
	},

	getFocused: function() {
		return this.isFocused;
	},

	setFocused: function(isFocused) {
		this.isFocused = isFocused;
	},

	/*
	* index: item, index; default: -1
	* direction: 'first', 'last', 'next', 'previous'; default: 'next'
	* next: boolean - focus at next menuItem from index; default: false
	*/
	focus: function(index, direction, next) {
		if(!this.enabled)
			return false;

		if(this.getCount() == 0)
			return DOM.focus(this);

		var items = this.getItems();

		index = index == null ? this.getCurrentItem() : index;

		if(index instanceof Z8.list.Item)
			index = this.getIndex(index);

		if(String.isString(index)) {
			direction = index;
			index = this.getIndex(this.getValue());
		}

		index = index != null ? index : -1;
		direction = direction || 'next';

		var step = 0;
		var count = items.length;

		do {
			if(direction == 'first') {
				index = 0;
				direction = 'next';
			} else if(direction == 'last') {
				index = count - 1;
				direction = 'previous';
			}

			if(step == 0 && next || step != 0) {
				if(direction == 'next')
					index = index != count - 1 ? index + 1 : 0;
				else if(direction == 'previous')
					index = index != 0 ? index - 1 : (count - 1);
			}

			index = Math.min(Math.max(index, 0), count - 1);

			if(index == -1)
				break;

			var item = items[index];

			if(!String.isString(item) && !item.hidden && item.isEnabled()) {
				this.selectItem(item);
				break;
			}
		} while(++step < count);

		return DOM.focus(this);
	},

	getCurrentItem: function() {
		return this.getItem(this.getValue());
	},

	getCurrentRecord: function() {
		var item = this.getCurrentItem();
		return item != null ? item.record : null;
	},

	getChecked: function(records) {
		var checked = [];
		var items = this.getItems();
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isChecked())
				checked.push(records ? item.record : item);
		}
		return checked;
	},

	getCheckedRecords: function() {
		return this.getChecked(true);
	},

	activateItem: function(item, active) {
		item.setActive(active);
		if(active) {
			this.expandParents(item);
			this.ensureVisible(item);
		}
	},

	selectItem: function(item, forceEvent) {
		var currentItem = this.getCurrentItem();

		if(currentItem != item && currentItem != null)
			this.activateItem(currentItem, false);

		if(Number.isNumber(item))
			item = this.getAt(item);

		if(item != null)
			this.activateItem(item, true);

		this.setValue(item != null && item.getValue != null ? item.getValue() : null);

		if(forceEvent || !this.confirmSelection && item != currentItem)
			this.fireEvent('select', item, currentItem, this);
	},

	setSelection: function(item) {
		if(String.isString(item))
			item = this.getItem(item);
		else if(Number.isNumber(item))
			item = this.getAt(Math.min(item, this.getCount() - 1));
		else if(item != null && item.isModel)
			item = this.getItem(item.id);

		this.selectItem(item, true);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(this.isEditing() || this.isFiltering() && key != Event.ESC)
			return;

		var item = this.getCurrentItem();

		if(key == Event.DOWN || key == Event.TAB && !event.shiftKey && this.useTAB) {
			if(this.focus(item, 'next', true))
				event.stopEvent();
		} else if(key == Event.UP || key == Event.TAB && event.shiftKey && this.useTAB) {
			if(this.focus(item, 'previous', true))
				event.stopEvent();
		} else if(key == Event.HOME) {
			if(this.focus('first'))
				event.stopEvent();
		} else if(key == Event.END) {
			if(this.focus('last'))
				event.stopEvent();
		} else if(key == Event.F && event.ctrlKey && this.filter != null) {
			this.showQuickFilter(true);
			event.stopEvent();
		} else if(key == Event.ESC && this.filterVisible) {
			this.showQuickFilter(false);
			this.focus(item);
			event.stopEvent();
		} else if(key == Event.LEFT && item != null) {
			if(item.isExpanded()) {
				if(item.hasChildren()) {
					item.collapse(true);
					event.stopEvent();
				}
			} else if((item = this.getParent(item)) != null)
				this.focus(item);
		} else if(key == Event.RIGHT && item != null && item.hasChildren() && item.isCollapsed()) {
			item.collapse(false);
			event.stopEvent();
		} else if(key == Event.ENTER && this.useENTER && item != null) {
			this.onItemClick(item, -1);
			event.stopEvent();
		} else if(key == Event.SPACE && item != null) {
			event.stopEvent();
			this.onItemToggle(item);
		}
	},

	onItemToggle: function(item) {
		this.checks ? item.toggleCheck() : this.onItemClick(item, -1);
	},

	ensureVisible: function(item) {
		var dom = DOM.get(item);
		if(dom == null)
			return;

		var top = dom.offsetTop;
		var bottom = top + dom.offsetHeight;
		var scrollTop = this.itemsScroller.scrollTop;
		var scrollBottom = scrollTop + this.itemsScroller.clientHeight;

		if(top < scrollTop || scrollBottom < bottom)
			DOM.scrollIntoView(item);
	},

	onScroll: function(event, target) {
		var scroller = target || this.totalsScroller || this.itemsScroller;

		if(this.headersScroller != null)
			this.headersScroller.scrollLeft = scroller.scrollLeft;

		this.itemsScroller.scrollLeft = scroller.scrollLeft;

		if(this.totalsScroller != null)
			this.totalsScroller.scrollLeft = scroller.scrollLeft;
	},

	setActive: function(active) {
		if(this.isActive() == active)
			return;

		this.callParent(active);

		this.adjustAutoFit();
	},

	onResize: function(event, target) {
		this.adjustAutoFit();
	},

	onHeaderSort: function(header) {
		if(header.field.sortable == false)
			return;

		var direction = header.getSort() == 'asc' ?  'desc' : 'asc';
		var sorter = { property: header.field.name, direction: direction }

		var callback = function(store, records, success) {
			header.setBusy(false);

			if(this.sortHeader != null)
				this.sortHeader.setSort(null);

			header.setSort(direction);

			this.sortHeader = header;
		};

		header.setBusy(true);
		this.store.sort(sorter, { fn: callback,scope: this });
	},

	updateSort: function() {
		var sorter = this.store != null ? this.store.getSorter() : null;
		sorter = sorter.length != 0 ? sorter[0] : null;

		var header = this.sortHeader;
		if(sorter == null && header == null)
			return;

		if(header != null && sorter != null && header.field.name == sorter.property && header.field.sortable !== false) {
			header.setSort(sorter.direction);
			return;
		}

		if(header != null) {
			header.setSort(null);
			this.sortHeader = null;
		}

		if(sorter != null) {
			header = this.getHeader(sorter.property);
			if(header != null) {
				header.setSort(sorter.direction);
				this.sortHeader = header;
			}
		}
	},

	updateFilter: function() {
		var quickFilter = this.store != null ? this.store.getQuickFilter() : null;

		var filters = {};
		for(var i = 0, length = quickFilter.length; i < length; i++) {
			var filter = quickFilter[i];
			filters[filter.property] = true;
		}

		var headers = this.headers;
		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			if(field != null)
				header.setFilter(filters[field.name]);
		}
	},

	onHeaderFilter: function(header, value) {
		var qiuckFilter = [];
		var filters = this.filters;

		for(var i = 0, length = filters.length; i < length; i++) {
			var filter = filters[i].getFilter();
			if(filter != null)
				qiuckFilter.push(filter);
		}

		var search = header.searchBox;
		var callback = function(store, records, success) {
			search.setBusy(false);
		};

		search.setBusy(true);
		this.store.quickFilter(qiuckFilter, { fn: callback, scope: this });
	},

	showQuickFilter: function(show, silent) {
		this.filterVisible = show;
		DOM.swapCls(this.headersScroller, show, 'filters');
		DOM.swapCls(this.filter, !show, 'display-none');

		this.adjustAutoFit();

		var callback = function(store, records, success) {
			this.focus();
		};

		if(!show) {
			var store = this.store;
			var quickFilter = store.getQuickFilter();
			if(quickFilter.length != 0)
				store.quickFilter([], { fn: callback, scope: this });
			this.resetQuickFilter();
		} else
			this.focusQuickFilter();

		if(silent !== true)
			this.fireEvent(show ? 'quickFilterShow' : 'quickFilterHide', this);
	},

	focusQuickFilter: function() {
		var filters = this.filters;

		for(var i = 0, length = filters.length; i < length; i++) {
			if(filters[i].focus())
				return;
		}
	},

	resetQuickFilter: function() {
		var filters = this.filters;

		for(var i = 0, length = filters.length; i < length; i++)
			filters[i].reset();
	},

	onQuickFilterFocusIn: function(header) {
		this.activeFilter = header;
	},

	onQuickFilterFocusOut: function(header) {
		this.activeFilter = null;
	},

	isFiltering: function() {
		return this.activeFilter != null;
	},

	setItemsChecked: function(checked) {
		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++)
			items[i].setChecked(checked);
	},

	checkAll: function() {
		var header = this.checkHeader;

		if(header == null)
			return false;

		header.setChecked(true);
		this.setItemsChecked(true);

		this.fireEvent('check', this, this.items, true);
		return true;
	},

	onCheckHeaderClick: function(header) {
		var checked = header.toggleCheck();
		this.setItemsChecked(checked);
		this.fireEvent('check', this, checked ? this.items : [], checked);
	},

	onItemCheck: function(item, checked) {
		var header = this.checkHeader;
		if(header.isChecked() && !checked)
			header.setChecked(false);
		this.fireEvent('check', this, [item], checked);
	},

	updateChecks: function() {
		if(this.checks && this.headers.length != 0)
			this.headers[0].setChecked(false);
	},

	onItemClick: function(item, index) {
		this.setSelection(item);
		this.processCheckboxEdit(item, index);
		this.fireEvent('itemClick', this, item, index);
	},

	onIconClick: function(item) {
		this.fireEvent('iconClick', this, item);
	},

	onItemDblClick: function(item, index) {
		this.fireEvent('itemDblClick', this, item, index);
	},

	processCheckboxEdit: function(item, index) {
		if(index == -1 || !this.editable)
			return;

		var field = this.fields[index];

		if(field.type != Type.Boolean || !field.isText || !field.editable)
			return;

		var record = item.record;

		if(record.isBusy())
			return;

		record.beginEdit();

		var name = field.name;
		record.set(name, !record.get(name));

		var callback = function(record, success) {
			if(success) {
				this.fireEvent('itemEdit', this, null, record, field);
				record.endEdit();
			} else
				record.cancelEdit();
		};

		record.update({ fn: callback, scope: this }, { values: this.store.getValues() });
	},

	onHeaderResize: function(header, change, handleType) {
		var index = this.getHeaderIndex(header);

		if(handleType == 'left')
			index--;

		header = this.headers[index];
		if(header.fixed || change == 0)
			return;

		this.setAutoFit(false);

		var headerCol = this.headerCols[index];
		var width = DOM.getPoint(headerCol, 'width');
		var itemsTableWidth = DOM.getPoint(this.itemsTable, 'width');

		var newWidth = Math.max(header.getMinWidth(), width + change);
		change = newWidth - width;

		header.setWidth(newWidth);

		if(change != 0) {
			DOM.setPoint(this.itemsTable, 'width',  itemsTableWidth + change);
			DOM.setPoint(this.itemCols[index], 'width',  newWidth);
			DOM.setPoint(this.headerCols[index], 'width',  newWidth);
			DOM.setPoint(this.totalCols[index], 'width',  newWidth);

			this.adjustScrollers();
		}
	},

	onHeaderResized: function(header) {
	},

	isTree: function() {
		var store = this.getStore();
		return store != null && store.isTree();
	},

	getParent: function(item) {
		if(item.isRoot())
			return null;

		var level = item.getLevel();
		var index = this.getIndex(item);
		var items = this.items;

		for(var i = index - 1; i >= 0; i--) {
			var item = items[i];
			if(item.getLevel() < level)
				return item;
		}

		return null;
	},

	getFirstChild: function(parent) {
		var index = this.getIndex(parent) + 1;
		var items = this.items;

		if(index >= items.length)
			return null;

		var item = items[index];
		return item.getLevel() == parent.getLevel() + 1 ? item : null;
	},

	getParents: function(items) {
		if(!this.isTree())
			return null;

		var parents = {};

		for(var i = 0, length = items.length; i < length; i++) {
			var parent = this.getParent(items[i]);
			if(parent != null)
				parents[parent.getValue()] = parent;
		}

		return Object.values(parents);
	},

	updateCollapser: function(parents) {
		if(!this.isTree())
			return;

		for(var i = 0, length = parents.length; i < length; i++) {
			var parent = parents[i];
			if(!parent.disposed)
				parent.setHasChildren(this.getFirstChild(parent) != null);
		}
	},

	expandParents: function(item) {
		if(!this.isTree())
			return;

		var parent = this.getParent(item);
		if(parent == null)
			return;

		parent.collapse(false);
		this.expandParents(parent);
	},

	onItemCollapse: function(item, collapsed) {
		var autoFit = this.autoFit;
		var hasVScrollbar = autoFit ? this.hasVScrollbar() : false;

		var index = this.getIndex(item);
		var level = item.getLevel();
		var items = this.items;

		for(var i = index + 1, length = items.length; i < length; i++) {
			item = items[i];
			if(level < item.getLevel())
				item.hide(collapsed);
			else
				break;
		}

		if(autoFit && hasVScrollbar != this.hasVScrollbar())
			this.adjustAutoFit();
	},

	getFirstEditorIndex: function(index) {
		var editors = this.editors;
		for(var i = index == null ? 0 : index, length = editors.length; i < length; i++) {
			if(editors[i] != null)
				return i;
		}
		return -1;
	},

	onItemFollowLink: function(item, index) {
		var field = this.fields[index];
		var link = field.link;
		var source = field.source;

		if(source != null || field.type == Type.File) {
			this.fireEvent('follow', item.record, field);
			return true;
		}

		return false;
	},

	onItemStartEdit: function(item, index) {
		var record = item.record;
		if(record != null && !record.isEditable())
			return false;

		if(index == null)
			index = this.lastEditedColumn;

		if(index == null)
			index = this.getFirstEditorIndex();

		if(index == -1)
			return false;

		var editor = this.editors[index];

		if(editor == null)
			return false;

		return this.editable ? this.startEdit(item, editor) : false;
	},

	isEditing: function() {
		return this.currentEditor != null;
	},

	canStartEdit: function(item, editor) {
		return item.record.isEditable();
	},

	startEdit: function(item, editor) {
		if(!this.canStartEdit(item, editor))
			return false;

		if(this.finishing) {
			this.editingQueue = { item: item, editor: editor };
			return true;
		}

		var record = item.record;
		var value = record.get(editor.name);
		var displayValue = record.get(editor.displayName);

		editor.initValue(value, displayValue);
		editor.setRecord(record);
		editor.item = item;

		this.openEditor(editor);
		this.currentEditor = editor;

		if(item != this.getCurrentItem())
			this.selectItem(item);

		return true;
	},

	openEditor: function(editor) {
		var cell = editor.item.getCell(editor.index);
		editor.appendTo(cell);

		editor.show();

		editor.on('focusOut', this.onEditorFocusOut, this);
		DOM.on(editor, 'keyDown', this.onEditorKeyDown, this);
		editor.focus(true);
	},

	closeEditor: function(editor, focus) {
		this.lastEditedColumn = editor.index;

		editor.un('focusOut', this.onEditorFocusOut, this);
		DOM.un(editor, 'keyDown', this.onEditorKeyDown, this);

		editor.hide();

		if(focus)
			this.focus();
	},

	cancelEdit: function(editor) {
		this.finishEdit(editor, null, true);
	},

	finishEdit: function(editor, next, cancel) {
		if(this.finishing)
			return;

		if(cancel || !editor.isValid()) {
			this.closeEditor(editor, true);
			this.currentEditor = null;
			return;
		}

		if(next == 'next' || next == 'previous')
			this.editingQueue = next == 'next' ? this.getNextEditor(editor.item, editor) : this.getPreviousEditor(editor.item, editor);

		var record = editor.record;
		record.beginEdit();
		record.set(editor.name, editor.getValue());

		var callback = function(record, success) {
			if(success) {
				this.fireEvent('itemEdit', this, editor, record, this.fields[editor.index]);
				record.endEdit();
			} else
				record.cancelEdit();

			this.closeEditor(editor, !success || this.editingQueue == null);
			this.currentEditor = null;
			this.finishing = false;

			if(!success)
				return;

			if(this.editingQueue != null) {
				var next = this.editingQueue;
				this.editingQueue = null;
				this.startEdit(next.item, next.editor);
			}
		};

		this.finishing = true;
		record.update({ fn: callback, scope: this }, { values: this.store.getValues() });
	},

	onEditorFocusOut: function(editor) {
		this.finishEdit(editor);
	},

	onItemEditorChange: function(editor, newValue, oldValue) {
		this.fireEvent('itemEditorChange', this, editor, newValue, oldValue);
	},

	onEditorKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.cancelEdit(this.currentEditor);
			this.focus(this.getCurrentItem());
			event.stopEvent();
		} else if(key == Event.ENTER) {
			this.finishEdit(this.currentEditor);
			event.stopEvent();
		} else if(key == Event.TAB) {
			var editor = this.currentEditor;
			this.finishEdit(editor, event.shiftKey ? 'previous' : 'next');
			event.stopEvent();
		}
	},

	getNextEditor: function(item, editor) {
		var editors = this.editors;
		var index = editor.index + 1;

		for(var i = index, length = editors.length; i < length; i++) {
			var editor = editors[i];
			if(editor != null && this.canStartEdit(item, editor))
				return { item: item, editor: editor };
		}

		var itemIndex = this.getIndex(item) + 1;
		item = this.getAt(itemIndex != this.getCount() ? itemIndex : 0); 

		for(var i = 0, length = editors.length; i < length; i++) {
			var editor = editors[i];
			if(editor != null && this.canStartEdit(item, editor))
				return { item: item, editor: editor };
		}

		return null;
	},

	getPreviousEditor: function(item, editor) {
		var editors = this.editors;
		var index = editor.index - 1;

		for(var i = index; i >= 0; i--) {
			var editor = editors[i];
			if(editor != null && this.canStartEdit(item, editor))
				return { item: item, editor: editor };
		}

		var itemIndex = this.getIndex(item) - 1;
		item = this.getAt(itemIndex != -1 ? itemIndex : this.getCount() - 1); 

		for(var i = editors.length - 1; i >= 0; i--) {
			var editor = editors[i];
			if(editor != null && this.canStartEdit(item, editor))
				return { item: item, editor: editor };
		}

		return null;
	}
});