Z8.define('Z8.application.form.Navigator', {
	extend: 'viewport.Form',

	presentation: 'form',

	initComponent: function() {
		var store = this.store;

		if(!store.isStore)
			store = this.store = new Z8.query.Store(store);

		this.title = store.form.text;
		this.icon = store.form.icon;
		this.presentation = store.form.presentation || 'form';

		this.callParent();

		this.initFilter(User.getFilter(this.registryEntry()));
		this.initPeriod(User.getPeriod(this.registryEntry()));
	},

	registryEntry: function() {
		return this.store.getModelName();
	},

	initFilter: function(filter) {
		this.filter = filter;
		this.store.setFilter(filter.getActive());
	},

	setFilter: function(filter) {
		this.initFilter(filter);
		User.setFilter(this.registryEntry(), filter);
		User.saveSettings();
		this.refreshRecords(this.filterButton);
	},

	initPeriod: function(period) {
		var store = this.store;
		if(store.getPeriodProperty() == null)
			return false;

		this.period = period;
		store.setPeriod(period);
		return true;
	},

	setPeriod: function(period) {
		if(this.initPeriod(period)) {
			User.setPeriod(this.registryEntry(), period);
			User.saveSettings();
			this.refreshRecords(this.periodButton);
		}
	},

	isReadOnly: function() {
		return this.store.form.readOnly;
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('navigator');

		var items = this.createForm().add(this.createTable());
		var body = this.body = new Z8.Container({ cls: 'body', items: items });
		var toolbar = this.createToolbar();

		var isForm = this.isFormPresentation();
		this.setListboxTools(this.listbox, isForm);
		this.setListboxTools(this.table, !isForm);

		this.items = [toolbar, body];

		this.updateSortState();

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		Viewport.sourceCode.owner = this.sourceCodeButton;
		Viewport.sourceCode.on('show', this.onSourceCodeShow, this);
		Viewport.sourceCode.on('hide', this.onSourceCodeHide, this);
	},

	onDestroy: function() {
		Viewport.sourceCode.un('show', this.onSourceCodeShow, this);
		Viewport.sourceCode.un('hide', this.onSourceCodeHide, this);
		Viewport.sourceCode.owner = null;

		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.callParent();
	},

	isFormPresentation: function() {
		return this.presentation == 'form';
	},

	isTablePresentation: function() {
		return this.presentation == 'table';
	},

	createForm: function() {
		var store = this.store;
		var config = store.form;

		var cls = this.isFormPresentation() ? '' : 'display-none';
		var form = this.form = new Z8.form.Form({ cls: cls, model: store.getModelName(), autoSave: true, controls: config.controls, colCount: config.colCount, readOnly: this.isReadOnly() });
		return [this.createListbox(), form];
	},

	createListbox: function() {
		var listbox = this.listbox = new Z8.form.field.Listbox(this.getListboxConfig());
		listbox.on('select', this.onSelect, this);
		listbox.on('contentChange', this.updateToolbar, this);
		return listbox;
	},

	createTable: function() {
		var isTable = this.isTablePresentation();
		var fields = this.getColumns();
		var store = this.store;
		var cls = 'table' + (isTable ? '' : ' display-none');
		var table = this.table = new Z8.form.field.Listbox({ cls: cls, store: store, fields: fields, locks: !this.isReadOnly(), editable: true, readOnly: this.isReadOnly(), totals: store.hasTotals(), pagingMode: 'always' });
		return table;
	},

	setListboxTools: function(listbox, set) {
		listbox.setAddTool(set ? this.addButton : null);
		listbox.setCopyTool(set ? this.copyButton : null);
		listbox.setRefreshTool(set ? this.refreshButton : null);
		listbox.setRemoveTool(set ? this.removeButton : null);
	},

	createToolbar: function() {
		var store = this.store;

		var buttons = [];
		var addCopyRefresh = [];

		if(store.hasCreateAccess()) {
			var add = this.addButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-file-o', tooltip: 'Новая запись', handler: this.addRecord, scope:this });
			addCopyRefresh.push(add);
		}

		if(store.hasCopyAccess()) {
			var copy = this.copyButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-copy', tooltip: 'Копировать запись', handler: this.copyRecord, scope:this });
			addCopyRefresh.push(copy);
		}

		if(store.hasReadAccess()) {
			var refresh = this.refreshButton =  new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-refresh', tooltip: 'Обновить', handler: this.refreshRecords, scope:this });
			addCopyRefresh.push(refresh);
		}

		if(addCopyRefresh.length != 0) {
			addCopyRefresh = new Z8.button.Group({ items: addCopyRefresh });
			buttons.push(addCopyRefresh);
		}

		var files = this.filesButton = this.createFilesButton();
		if(files != null)
			buttons.push(files);

		if(store.hasDestroyAccess()) {
			var remove = this.removeButton = new Z8.button.Button({ cls: 'btn-sm', danger: true, icon: 'fa-trash', tooltip: 'Удалить запись', handler: this.removeRecord, scope:this });
			buttons.push(remove);
		}

		var quickFilters = this.quickFilters = this.createQuickFilters();
		buttons.add(quickFilters);

		var period = this.periodButton = this.createPeriodButton();
		if(period != null)
			buttons.push(period);

		var filter = this.filterButton = this.createFilterButton();
		var sort = this.sortButton = new Z8.button.Button({ enabled: false, cls: 'btn-sm', icon: 'fa-sort', tooltip: 'Порядок сортировки', triggerTooltip: 'Настроить порядок сортировки', split: true, handler: this.toggleSortOrder, scope: this });
		buttons.push(filter, sort);

		var formTable = this.createFormTableGroup();
		if(formTable != null)
			buttons.push(formTable);

		var print = this.printButton = this.createPrintButton();
		buttons.push(print);

		var actions = this.actionsButton = this.createActionsButton();
		if(actions != null)
			buttons.push(actions);

		var reports = this.reportsButton = this.createReportsButton();
		if(reports != null)
			buttons.push(reports);

		var sourceCode = this.sourceCodeButton = this.createSourceCodeButton();
		if(sourceCode != null)
			buttons.push(sourceCode);

		return new Z8.toolbar.Toolbar({ items: buttons });
	},

	createFilesButton: function() {
		var store = this.store;
		var filesProperty = store.getFilesProperty();

		if(filesProperty == null || !store.hasWriteAccess())
			return null;

		var control = this.form.getControl(filesProperty);
		var store = control == null ? new Z8.data.Store({ model: 'Z8.data.file.Model' }) : control.store;
		var menu = new Z8.menu.Menu({ store: store });
		menu.on('itemClick', this.downloadFiles, this);
		var file = new Z8.button.File({ store: store, control: control, cls: 'btn-sm', icon: 'fa-paperclip', tooltip: 'Файлы', menu: menu });
		file.on('select', this.uploadFiles, this);
		return file;
	},

	createQuickFilters: function() {
		var quickFilters = [];
		var fields = this.getQuickFilterFields();
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var quickFilter = new Z8.form.field.Search({ field: field, width: 10, label: false, placeholder: field.header, tooltip: field.header });
			quickFilter.on('search', this.onSearch, this);
			quickFilters.push(quickFilter);
		}
		return quickFilters;
	},

	createFilterButton: function() {
		var filter = new Z8.filter.Button({ cls: 'btn-sm', filter: this.filter, fields: this.store.getFields() });
		filter.on('filter', this.onFilter, this);
		return filter;
	},

	createFormTableGroup: function() {
		if(Z8.isEmpty(this.getColumns()))
			return null;

		var isForm = this.isFormPresentation();

		var formButton = this.formButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-wpforms', tooltip: 'В виде формы', toggled: isForm });
		formButton.on('toggle', this.toggleForm, this);

		var tableButton = this.tableButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-table', tooltip: 'В виде таблицы', toggled: !isForm });
		tableButton.on('toggle', this.toggleTable, this);

		return new Z8.button.Group({ items: [formButton, tableButton], radio: true });
	},

	createPrintButton: function() {
		var items = [
			new Z8.menu.Item({ text: 'Acrobat Reader (*.pdf)', icon: 'fa-file-pdf-o', format: 'pdf' }),
			new Z8.menu.Item({ text: 'Microsoft Excel (*.xls)', icon: 'fa-file-excel-o', format: 'xls' }),
			new Z8.menu.Item({ text: 'Microsoft Word (*.doc)', icon: 'fa-file-word-o', format: 'doc' }),
			'-',
			new Z8.menu.Item({ text: 'Настройки', icon: 'fa-print', enabled: false })
		];

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuPrint, this);

		return new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-file-pdf-o', tooltip: 'Сохранить как PDF', menu: menu, handler: this.print, scope: this, format: 'pdf' });
	},

	createPeriodButton: function() {
		if(this.period == null)
			return null;

		var period = new Z8.calendar.Button({ cls: 'btn-sm', icon: 'fa-calendar', period: this.period });
		period.on('period', this.onPeriod, this);
		return period;
	},

	createActionsButton: function() {
		var actions = this.store.form.actions;

		if(Z8.isEmpty(actions))
			return null;

		var items = [];
		for(var i = 0, length = actions.length; i < length; i++) {
			var action = actions[i];
			items.push(new Z8.menu.Item({ text: action.text, icon: action.icon, action: action }));
		}

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuAction, this);

		return new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-play', text: 'Действия', tooltip: 'Действия', menu: menu, handler: this.onMenuButtonClick, scope: this });
	},

	createReportsButton: function() {
		var reports = this.store.form.reports;

		if(Z8.isEmpty(reports))
			return null;

		var items = [];
		for(var i = 0, length = reports.length; i < length; i++) {
			var report = reports[i];
			items.push(new Z8.menu.Item({ text: report.text, icon: report.icon, report: report }));
		}

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuReport, this);
		return new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-print', tooltip: 'Печать документов', menu: menu, handler: this.onMenuButtonClick, scope: this });
	},

	createSourceCodeButton: function() {
		var sourceCode = new Z8.button.Button({ cls: 'btn-sm float-right', text: 'Исходный код', success: true, icon: 'fa-code', tooltip: 'Как это сделано', toggled: false });
		sourceCode.on('toggle', this.toggleSourceCode, this);
		return sourceCode;
	},

	onSourceCodeShow: function() {
		this.sourceCodeButton.setToggled(true, true);
	},

	onSourceCodeHide: function() {
		this.sourceCodeButton.setToggled(false, true);
		this.focus();
	},

	getListboxConfig: function() {
		var names = this.getNames();
		var quickFilters = this.getQuickFilterFields();
		var label = this.getListboxLabel(names);

		return {
			cls: this.isFormPresentation() ? '' : 'display-none',
			store: this.store,
			fields: names,
			names: names,
			label: label,
			quickFilters: quickFilters,
			filters: quickFilters.length == 0,
			editable: true,
			locks: !this.isReadOnly()
		};
	},

	getListboxLabel: function(fields) {
		if(fields.length != 0) {
			var field = fields[0];
			return { text: field.header || field.name, icon: field.icon };
		}
		return '';
	},

	getNames: function() {
		var names = this.store.getNames();

		if(!Z8.isEmpty(names))
			return names;

		var controls = this.form.getFields();
		return controls.length == 0 ? [] : [controls[0].field];
	},

	getColumns: function() {
		var columns = this.store.getColumns();

		if(Z8.isEmpty(columns)) {
			var controls = this.form.getFields();
			for(var i = 0, length = controls.length; i < length; i++) {
				var field = controls[i].field;
				if(field != null && !field.isListbox && !field.isContainer && !field.isFieldset)
					columns.push(field);
			}
		}

		for(var i = 0, length = columns.length; i < length; i++) {
			var column = columns[i];
			column.editable = column.editable !== false && !column.readOnly;
		}

		return columns;
	},

	getQuickFilterFields: function() {
		return this.store.getQuickFilters();
	},

	getActiveListbox: function() {
		return this.isFormPresentation() ? this.listbox : this.table;
	},

	getSelection: function() {
		return this.getActiveListbox().getSelection();
	},

	addRecord: function(button) {
		this.getActiveListbox().onAddRecord(button);
	},

	copyRecord: function(button) {
		this.getActiveListbox().onCopyRecord(button);
	},

	removeRecord: function(button) {
		this.getActiveListbox().onRemoveRecord(button);
	},

	refreshRecords: function(button) {
		this.getActiveListbox().onRefresh(button);
	},

	updateToolbar: function() {
		var readOnly = this.isReadOnly();
		var record = this.getSelection();

		if(this.addButton != null)
			this.addButton.setEnabled(!readOnly);

		if(this.copyButton != null)
			this.copyButton.setEnabled(!readOnly && record != null);

		if(this.removeButton != null)
			this.removeButton.setEnabled(!readOnly && record != null && record.isDestroyable());

		if(this.reportsButton != null)
			this.reportsButton.setEnabled(record != null);

		var filesButton = this.filesButton;
		if(filesButton == null)
			return;

		var enabled = !readOnly && record != null;
		filesButton.setEnabled(enabled);

		if(enabled && filesButton.control == null)
			filesButton.store.loadData(record.getFiles());
	},

	toggleForm: function(button) {
		this.table.hide();
		this.listbox.show();
		this.form.show();

		this.setListboxTools(this.listbox, true);
		this.setListboxTools(this.table, false);

		this.presentation = 'form';
		this.focus();
	},

	toggleTable: function(button) {
		this.listbox.hide();
		this.form.hide();
		this.table.show();

		this.setListboxTools(this.table, true);
		this.setListboxTools(this.listbox, false);

		this.presentation = 'table';
		this.focus();
	},

	toggleSourceCode: function(button, toggled) {
		Viewport.showSourceCode(toggled);
	},

	onSearch: function(search, value) {
		var callback = function(records, success) {
			search.setBusy(false);
		};

		var filter = [];
		var quickFilters = this.quickFilters;

		for(var i = 0, length = quickFilters.length; i < length; i++) {
			var quickFilter = quickFilters[i];
			if(quickFilter != search && quickFilter.expression != null)
				filter.push(quickFilter.expression);
		}

		search.expression = value != '' ? { property: search.field.name, operator: Operation.Contains, value: value } : null;
		if(search.expression != null)
			filter.push(search.expression);

		search.setBusy(true);
		this.store.quickFilter(filter, { fn: callback, scope: this });
	},

	updateSortState: function() {
/*
		var sorters = this.store.getSorter();
		var icon = sorters.length != 0 ? (sorters[0].direction == 'asc' ? 'fa-sort-alpha-asc' : 'fa-sort-alpha-desc') : 'fa-sort';
		this.sortButton.setIcon(icon);
		this.sortButton.setEnabled(sorters.length != 0);
*/
	},

	toggleSortOrder: function(button) {
		var sorter = this.store.getSorter();
		if(sorter.length != 0) {
			var sorter = sorter[0];
			sorter.direction = sorter.direction == 'asc' ? 'desc' : 'asc';
			this.updateSortState();
		}
	},

	onFilter: function(button, filter, action) {
		if(action != Filter.NoAction)
			this.setFilter(filter);
		this.focus();
	},

	onPeriod: function(button, period, action) {
		if(action != Period.NoAction)
			this.setPeriod(period);
		this.focus();
	},

	focus: function() {
		return this.getActiveListbox().focus();
	},

	select: function(listbox, record) {
		if(!this.disposed) {
			this.form.loadRecord(record);
			this.updateToolbar();
		}
	},

	onSelect: function(listbox, record) {
		if(this.selectTask == null)
			this.selectTask = new Z8.util.DelayedTask();

		this.selectTask.delay(50, this.select, this, listbox, record);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.F && event.ctrlKey && this.quickFilters.length != 0) {
			this.quickFilters[0].focus();
			event.stopEvent();
		} else if(key == Event.ESC) {
			this.focus();
			Viewport.closeForm(this);
			event.stopEvent();
		}
	},

	onMenuPrint: function(menu, item) {
		var format = item.format;
		if(format == 'pdf')
			this.printPDF();
		else if(format == 'xls')
			this.printXLS();
		else if(format == 'doc')
			this.printDOC();
	},

	printPDF: function() {
		var button = this.printButton;
		button.setIcon('fa-file-pdf-o');
		button.setTooltip('Сохранить как PDF');
		button.format = 'pdf';
		this.print();
	},

	printXLS: function() {
		var button = this.printButton;
		button.setIcon('fa-file-excel-o');
		button.setTooltip('Сохранить как XLS');
		button.format = 'xls';
		this.print();
	},

	printDOC: function() {
		var button = this.printButton;
		button.setIcon('fa-file-word-o');
		button.setTooltip('Сохранить как DOC');
		button.format = 'doc';
		this.print();
	},

	print: function() {
		var button = this.printButton;
		var format = button.format;

		var columns = [];
		var headers = this.table.getHeaders();

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			if(field != null && field.type != Type.Text)
				columns.push({ id: field.name, width: Ems.emsToPixels(header.getWidth()) });
		}

		var store = this.store;

		var params = {
			request: store.getModelName(),
			action: 'export',
			format: format,
			columns: columns,
			filter: this.filter.getActive() || [],
			quickFilter: store.getQuickFilter(),
			where: store.getWhere(),
			sort: store.getSorter(),
			period: store.getPeriod()
		};

		var callback = function(response, success) {
			button.setBusy(false);
		};

		button.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });

		this.focus();
	},

	onMenuButtonClick: function(button) {
		button.toggleMenu();
	},

	onMenuAction: function(menu, item) {
		this.actionsButton.setBusy(true);

		var action = item.action;
		var record = this.getSelection();

		var params = {
			request: this.store.getModelName(),
			action: 'action',
			id: action.id,
			records: (record != null && !record.phantom) ? [record.id] : null
		};

		var callback = function(response, success) {
			this.onAction(action, response, success);
			this.actionsButton.setBusy(false);
			this.refreshRecords(this.refreshButton);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onAction: function(action, response, success) {
	},

	onMenuReport: function(menu, item) {
		var report = item.report;
		var record = this.getSelection();

		var params = {
			request: this.store.getModelName(),
			action: 'report',
			format: 'pdf',
			id: report.id,
			recordId: record.id
		};

		var callback = function(response, success) {
			this.reportsButton.setBusy(false);
		};

		this.reportsButton.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });

		this.focus();
	},

	downloadFiles: function(menu, item) {
		var callback = function(success) {
			this.filesButton.setBusy(false);
		};

		this.filesButton.setBusy(true);

		DOM.download(item.record.get('path'), null, { fn: callback, scope: this });
	},

	uploadFiles: function(button, files) {
		button.setBusy(true);

		var callback = function(record, files, success) {
			button.setBusy(false);
			if(success && this.getSelection() == record)
				button.store.loadData(files);
		};

		var record = this.getSelection();
		record.attach(record.getFilesProperty(), files, { fn: callback, scope: this });
	}
});