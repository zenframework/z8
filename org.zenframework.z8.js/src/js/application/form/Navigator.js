Z8.define('Z8.application.form.Navigator', {
	extend: 'ViewportForm',

	oneRecord: false,

	presentation: 'form',
	quickFilterWidth: 15,

	initComponent: function() {
		var store = this.store;

		if (store.sourceFilter != null)
			this.sourceFilter = store.sourceFilter;

		if(!store.isStore)
			store = this.store = new Z8.query.Store(store);

		store.on('recordChange', this.onStoreRecordChange, this);

		this.icon = store.form.icon;
		this.presentation = store.form.presentation || 'form';

		this.callParent();

		this.initFilter(User.getFilter(this.registryEntry()));
		this.initPeriod(User.getPeriod(this.registryEntry()));

		var items = this.createItems();
		var body = this.body = new Z8.Container({ cls: 'body', items: items });
		var toolbar = this.toolbar = this.createToolbar();

		var isForm = this.isFormPresentation();
		this.setTools(this.listbox, isForm);
		this.setTools(this.table, !isForm);

		this.items = [toolbar, body];
	},

	getCls: function() {
		var cls = ViewportForm.prototype.getCls.call(this);

		if(this.oneRecord)
			cls.pushIf('one-record');

		return cls.pushIf('navigator');
	},

	getStore: function() {
		return this.store;
	},

	registryEntry: function() {
		return this.store.getModelName();
	},

	getTitle: function() {
		var title = this.title;
		return (this.store.form.text + (title != null ? ' - ' + title : ''));
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

	createItems: function() {
		return this.createForm().add(this.createTable());
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		if(Viewport.sourceCode != null) {
			Viewport.sourceCode.owner = this.sourceCodeButton;
			Viewport.sourceCode.on('show', this.onSourceCodeShow, this);
			Viewport.sourceCode.on('hide', this.onSourceCodeHide, this);
		}

		if (this.sourceFilter != null) {
			var quickFilter = this.findQuickFilter(this.sourceFilter.property);
			if (quickFilter != null) {
				quickFilter.setValue(this.sourceFilter.value);
				quickFilter.lastSearchValue = ' ';
				quickFilter.updateTrigger();
			}
		}
	},

	onDestroy: function() {
		if(Viewport.sourceCode != null) {
			Viewport.sourceCode.un('show', this.onSourceCodeShow, this);
			Viewport.sourceCode.un('hide', this.onSourceCodeHide, this);
			Viewport.sourceCode.owner = null;
		}

		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.callParent();
	},

	isFormPresentation: function() {
		return this.oneRecord || this.presentation == 'form';
	},

	isTablePresentation: function() {
		return !this.oneRecord && this.presentation == 'table';
	},

	createForm: function() {
		var store = this.store;
		var config = store.form;

		var cls = this.isFormPresentation() ? '' : 'display-none';
		var form = this.form = new Z8.form.Form({ cls: cls, model: store.getModelName(), autoSave: true, controls: config.controls, colCount: config.colCount, readOnly: this.isReadOnly() });
		return [this.createListbox(), form];
	},

	createListbox: function() {
		var type = this.listboxType;
		var config = this.getListboxConfig();
		var listbox = this.listbox = type != null ? Z8.create(type, config) : new ListBox(config);
		listbox.on('select', this.onSelect, this);
		listbox.on('contentChange', this.updateToolbar, this);
		return listbox;
	},

	onStoreRecordChange: function(store, record, modified) {
		this.onRecordChange(record, modified);

		if(!this.oneRecord)
			return;

		var forms = Viewport.forms;
		var index = forms.indexOf(this);

		var form = forms[index - 1];
		if(!(form instanceof Z8.application.form.Navigator))
			return;

		var field = this.sourceLink;
		var id = record.id;

		var store = form.getStore();
		var records = store.getRecords();
		var toReload = [];

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			if(field != null && record.get(field) == id || record.id == id)
				toReload.add(record);
		}

		if(toReload.length < 10) {
			for(var i = 0, length = toReload.length; i < length; i++)
				toReload[i].reload();
		} else
			store.load();
	},

	onRecordChange: function(record, modified) {
	},

	createTable: function() {
		var type = this.tableType;
		var config = this.getTableConfig();
		return this.table = type != null ? Z8.create(type, config) : new ListBox(config);
	},

	setTools: function(listBox, set) {
		if(listBox == null)
			return;

		listBox.setAddTool(set ? this.addButton : null);
		listBox.setCopyTool(set ? this.copyButton : null);
		listBox.setRefreshTool(set ? this.refreshButton : null);
		listBox.setRemoveTool(set ? this.removeButton : null);
	},

	createToolbar: function() {
		var buttons = this.createTools();

		var sourceCode = this.sourceCodeButton = this.createSourceCodeButton();
		if(sourceCode != null)
			buttons.add({ cls: 'flex-1' }).add(sourceCode);

		return new Z8.toolbar.Toolbar({ items: buttons });
	},

	createTools: function() {
		var store = this.store;

		var buttons = [];
		var oneRecord = this.oneRecord;

		if(!oneRecord && store.hasCreateAccess()) {
			var add = this.addButton = new Z8.button.Button({ icon: 'fa-file-o', tooltip: Z8.$('Navigator.newRecord'), handler: this.addRecord, scope: this });
			buttons.push(add);
		}

		if(!oneRecord && store.hasCopyAccess()) {
			var copy = this.copyButton = new Z8.button.Button({ icon: 'fa-copy', tooltip: Z8.$('Navigator.copyRecord'), handler: this.copyRecord, scope: this });
			buttons.push(copy);
		}

		if(store.hasReadAccess()) {
			var refresh = this.refreshButton =  new Z8.button.Button({ icon: 'fa-refresh', tooltip: Z8.$('Navigator.refresh'), handler: this.refreshRecords, scope: this });
			buttons.push(refresh);
		}

		var files = this.filesButton = this.createFilesButton();
		if(files != null)
			buttons.push(files);

		if(store.hasDestroyAccess()) {
			var remove = this.removeButton = new Z8.button.Button({ danger: true, icon: 'fa-trash', tooltip: Z8.$('Navigator.deleteRecord'), handler: this.removeRecord, scope: this });
			buttons.push(remove);
		}

		if(!oneRecord) {
			var quickFilters = this.quickFilters = this.createQuickFilters();
			buttons.add(quickFilters);

			var period = this.periodButton = this.createPeriodButton();
			if(period != null)
				buttons.push(period);

			var filter = this.filterButton = this.createFilterButton();
			if(filter != null)
				buttons.push(filter);

/*
			var sort = this.sortButton = new Z8.button.Button({ enabled: false, icon: 'fa-sort', tooltip: 'Порядок сортировки', triggerTooltip: 'Настроить порядок сортировки', split: true, handler: this.toggleSortOrder, scope: this });
			buttons.push(sort);
*/

			var formTable = this.createFormTableGroup();
			if(formTable != null)
				buttons.push(formTable);

			var print = this.printButton = this.createPrintButton();
			if(print != null)
				buttons.push(print);
		}

		var reports = this.reportsButton = this.createReportsButton();
		if(reports != null)
			buttons.push(reports);

		var actions = this.actionsButton = this.createActionsButton();
		if(actions != null)
			buttons.push(actions);

		return buttons;
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
		var file = new Z8.button.File({ store: store, control: control, icon: 'fa-paperclip', tooltip: Z8.$('Navigator.files'), menu: menu });
		file.on('select', this.uploadFiles, this);
		return file;
	},

	createQuickFilters: function() {
		var quickFilters = [];
		var fields = this.getQuickFilterFields();
		for(var i = 0, length = fields.length; i < length; i++) {
			var quickFilter = this.createQuickFilter(fields[i]);
			quickFilter.on('search', this.onSearch, this);
			quickFilters.push(quickFilter);
		}
		return quickFilters;
	},

	createQuickFilter: function(field) {
		var config = {
			field: field,
			width: this.quickFilterWidth,
			label: false,
			placeholder: field.header,
			tooltip: field.header
		};
		return new Z8.form.field.SearchText(config);
	},

	findQuickFilter: function(field) {
		var quickFilters = this.quickFilters;
		if (quickFilters == null)
			return null;
		for (var i = 0; i < quickFilters.length; i++) {
			var quickFilter = quickFilters[i];
			if (quickFilter.field.name == field)
				return quickFilter;
		}
		return null;
	},

	createFilterButton: function() {
		var filter = new Z8.filter.Button({ filter: this.filter, fields: this.store.getFields() });
		filter.on('filter', this.onFilter, this);
		return filter;
	},

	createFormTableGroup: function() {
		if(Z8.isEmpty(this.getColumns()))
			return null;

		var isForm = this.isFormPresentation();

		var formButton = this.formButton = new Z8.button.Button({ icon: 'fa-wpforms', tooltip: Z8.$('Navigator.formView'), toggled: isForm });
		formButton.on('toggle', this.toggleView, this);

		var tableButton = this.tableButton = new Z8.button.Button({ icon: 'fa-table', tooltip: Z8.$('Navigator.tableView'), toggled: !isForm });
		tableButton.on('toggle', this.toggleView, this);

		return new Z8.button.Group({ items: [formButton, tableButton], radio: true });
	},

	createPrintButton: function() {
		var items = [
			new Z8.menu.Item({ text: 'Acrobat Reader (*.pdf)', icon: 'fa-file-pdf-o', format: 'pdf' }),
			new Z8.menu.Item({ text: 'Microsoft Excel (*.xls)', icon: 'fa-file-excel-o', format: 'xls' }),
/*
			new Z8.menu.Item({ text: 'Microsoft Word (*.doc)', icon: 'fa-file-word-o', format: 'doc' }),
*/
			'-',
			new Z8.menu.Item({ text: Z8.$('Navigator.settings'), icon: 'fa-print', enabled: false })
		];

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuPrint, this);

		return new Z8.button.Button({ icon: 'fa-file-pdf-o', tooltip: Z8.$('Navigator.saveAsPDF'), menu: menu, handler: this.print, scope: this, format: 'pdf' });
	},

	createPeriodButton: function() {
		if(this.period == null)
			return null;

		var period = new Z8.calendar.Button({ icon: 'fa-calendar', period: this.period });
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
			items.push(new Z8.menu.Item({ text: action.header, icon: action.icon, action: action }));
		}

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onAction, this);

		return new Z8.button.Button({ icon: 'fa-play', text: Z8.$('Navigator.actions'), tooltip: Z8.$('Navigator.actions'), menu: menu, handler: this.onMenuButtonClick, scope: this });
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
		return new Z8.button.Button({ icon: 'fa-print', tooltip: Z8.$('Navigator.printDocuments'), menu: menu, handler: this.onMenuButtonClick, scope: this });
	},

	createSourceCodeButton: function() {
		if(Viewport.sourceCode == null)
			return null;

		return new Z8.button.Button({ success: true, icon: 'fa-code', toggled: false, toggleHandler: this.toggleSourceCode, scope: this });
	},

	onSourceCodeShow: function() {
		if(this.sourceCodeButton != null)
			this.sourceCodeButton.setToggled(true, true);
	},

	onSourceCodeHide: function() {
		if(this.sourceCodeButton != null) {
			this.sourceCodeButton.setToggled(false, true);
			this.focus();
		}
	},

	getListboxConfig: function() {
		var names = this.getNames();
		var quickFilters = this.getQuickFilterFields();
		var label = this.getListboxLabel(names);
		var store = this.store;

		return {
			cls: 'navigator-listbox' + (this.isFormPresentation() ? '' : ' display-none'),
			store: store,
			query: { request: store.getModelName(), text: store.form.text },
			fields: names,
			names: names,
			label: label,
			quickFilters: quickFilters,
			filters: quickFilters.length == 0,
			editable: true,
			locks: !this.isReadOnly() && Application.listbox.locks,
			checks: Application.listbox.checks,
			totals: store.hasTotals()
		};
	},

	getTableConfig: function() {
		var store = this.store;

		return {
			cls: 'table' + (this.isTablePresentation() ? '' : ' display-none'),
			store: store,
			fields: this.getColumns(),
			locks: !this.isReadOnly() && Application.listbox.locks,
			checks: Application.listbox.checks,
			editable: true,
			readOnly: this.isReadOnly(),
			totals: store.hasTotals(),
			pagingMode: 'always'
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
				if(field != null && !field.isListbox && !field.isContainer && !field.isFieldset && !field.isGeometry)
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

	getChecked: function() {
		return this.getActiveListbox().getChecked();
	},

	getSelectedIds: function() {
		var records = [];
		var selected = this.getChecked();

		for(var i = 0, length = selected.length; i < length; i++) {
			var record = selected[i];
			if(!record.phantom)
				records.push(selected[i].id);
		}

		return records;
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

	toggleView: function(button) {
		if(button == this.formButton)
			this.toggleForm(button);
		else if(button == this.tableButton)
			this.toggleTable(button);
	},

	toggleForm: function(button) {
		this.table.hide();
		this.table.setActive(false);

		this.listbox.show();
		this.listbox.setActive(true);
		this.form.show();
		this.form.setActive(true);

		this.setTools(this.listbox, true);
		this.setTools(this.table, false);

		this.presentation = 'form';
		this.focus();
	},

	toggleTable: function(button) {
		this.listbox.hide();
		this.listbox.setActive(false);

		this.form.hide();
		this.form.setActive(false);

		this.table.show();
		this.table.setActive(true);

		this.setTools(this.table, true);
		this.setTools(this.listbox, false);

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

		search.expression = value != '' ? { property: search.field.name, operator: Operator.Contains, value: value } : null;
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
		return this.oneRecord ? this.form.focus() : this.getActiveListbox().focus();
	},

	getRecord: function() {
		return this.record;
	},

	setRecord: function(record) {
		this.record = record;
		this.form.loadRecord(record);
	},

	select: function(listbox, newRecord, oldRecord) {
		if(this.disposed)
			return false;

		this.setRecord(newRecord);
		this.updateToolbar();
		return true;
	},

	onSelect: function(listbox, newRecord, oldRecord) {
		if(this.selectTask == null)
			this.selectTask = new Z8.util.DelayedTask();

		this.selectTask.delay(50, this.select, this, listbox, newRecord, oldRecord);
	},

	onKeyDown: function(event, target) {
		ViewportForm.prototype.onKeyDown.call(this, event, target);

		switch(event.getKey()) {
		case Event.F:
			if(event.ctrlKey && this.quickFilters.length != 0) {
				this.quickFilters[0].focus();
				return event.stopEvent();
			}
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
		button.setTooltip(Z8.$('Navigator.saveAsPDF'));
		button.format = 'pdf';
		this.print();
	},

	printXLS: function() {
		var button = this.printButton;
		button.setIcon('fa-file-excel-o');
		button.setTooltip(Z8.$('Navigator.saveAsXLS'));
		button.format = 'xls';
		this.print();
	},

	printDOC: function() {
		var button = this.printButton;
		button.setIcon('fa-file-word-o');
		button.setTooltip(Z8.$('Navigator.saveAsDOC'));
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
				columns.push({ id: field.name, width: Ems.emsToPixels(header.getWidth()), minWidth: Ems.emsToPixels(header.getMinWidth()) });
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

	onAction: function(menu, item) {
		this.actionsButton.setBusy(true);

		var action = item.action;
		var records = this.getSelectedIds();

		var params = {
			request: action.request,
			action: 'action',
			name: action.name,
			records: records
		};

		var callback = function(response, success) {
			this.onActionComplete(action, this.getChecked(), response, success);
			this.actionsButton.setBusy(false);
			this.refreshRecords(this.refreshButton);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onActionComplete: function(action, records, response, success) {
	},

	onMenuReport: function(menu, item) {
		var store = this.store;
		var report = item.report;
		var record = this.getSelection();

		var params = {
			request: store.getModelName(),
			action: 'report',
			format: report.format,
			id: report.id,
			recordId: record.id,
			quickFilter: store.getQuickFilter(),
			where: store.getWhere(),
			sort: store.getSorter(),
			period: store.getPeriod()
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

		var record = item.record;
		DOM.download(record.get('path'), record.id, null, { fn: callback, scope: this });
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
