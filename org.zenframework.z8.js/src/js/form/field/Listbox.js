Z8.define('Z8.form.field.Listbox', {
	extend: 'Z8.form.field.Control',

	isListbox: true,
	scrollable: true,

	tools: false,
	locks: false,
	totals: false,
	autoFit: true,

	editable: false,
	enterToEdit: true,

	pagerMode: 'visible', // 'visible' || 'hidden'

	loadPolicy: 'active', // 'active' || 'always'
	loadPending: false,

	filterVisible: false,

	initComponent: function() {
		var store = this.store;

		if(store != null && !store.isStore)
			store = this.store = new Z8.query.Store(store);

		this.callParent();
		this.setValue(this.value);
	},

	isEmptyValue: function(value) {
		return this.list.getCount() == 0;
	},

	getValue: function() {
		return this.mixins.field.getValue.call(this);
	},

	setValue: function(value) {
		var list = this.list;

		if(list != null) {
			var item = list.getItem(value);
			item != null ? list.selectItem(item) : (value = guid.Null);
		}

		if(value != this.getValue())
			this.mixins.field.setValue.call(this, value);
	},

	isActive: function() {
		return this.callParent() || this.loadPolicy == 'always';
	},

	isEditable: function() {
		return this.editable && !this.isReadOnly();
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		var list = this.list;
		if(list != null)
			list.setEditable(this.editable && enabled && !this.isReadOnly());
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly);

		var list = this.list;
		if(list != null)
			list.setEditable(this.editable && !readOnly && this.isEnabled());
	},

	hasLink: function() {
		return this.getLink() != null;
	},

	getLink: function() {
		var query = this.query;
		return query != null && query.link != null ? this.query.link.name : null;
	},

	getItem: function(record) {
		var record = record.isModel ? record.id : record;
		return this.list.getItem(record);
	},

	getModel: function() {
		return this.store.getModel();
	},

	getStore: function() {
		return this.store;
	},

	getFields: function() {
		return this.list.getFields();
	},

	getControl: function(name) {
		var controls = this.controls;

		if(controls == null)
			return null;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.name == name || control.displayName == name)
				return control;
		}

		return null;
	},

	getHeaders: function() {
		return this.list.getHeaders();
	},

	getEditors: function() {
		return this.list.getEditors();
	},

	getEditor: function(name) {
		return this.list.getEditor(name);
	},

	needsValues: function() {
		var values = this.getValues();
		if(values == null)
			return false;

		for(var name in values) {
			var value = values[name];
			if(Z8.isEmpty(value) || value == guid.Null)
				return true;
		}
		return false;
	},

	getValues: function() {
		var record = this.record;

		if(record == null)
			return null;

		var fields = record.getValueFor();

		if(fields.length == 0)
			return null;

		var values = {};
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			values[field.valueFor] = record.get(field.name);
		}

		return values;
	},

	getValueFromFields: function() {
		if(this.valueFromFields == null)
			this.valueFromFields = this.store.getValueFromFields();
		return this.valueFromFields;
	},

	attachRecordChange: function(record) {
		if(record != null)
			record.on('change', this.onRecordChange, this);
	},

	detachRecordChange: function(record) {
		if(record != null)
			record.un('change', this.onRecordChange, this);
	},

	setRecord: function(record) {
		var current = this.getRecord();

		this.detachRecordChange(current);

		this.callParent(record);

		this.attachRecordChange(record);

		this.afterRecordSet(record);
		this.updateEnabledState();
	},

	updateEnabledState: function() {
		this.setEnabled(!this.needsValues());
	},

	onRecordChange: function(record, modified) {
		this.updateEnabledState();
	},

	getFilter: function(record) {
		if(record == null || !this.hasLink())
			return null;

		var dependsOnValue = this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id;
		this.setDependsOnValue(dependsOnValue);

		if(dependsOnValue == null || dependsOnValue == guid.Null)
			return null;

		var filter = this.getDependencyWhere(this.getLink(), dependsOnValue);

		filter = Array.isArray(filter) ? filter : (filter != null ? [filter] : []);

		var parentKeys = this.query.link.parentKeys;

		if(parentKeys != null) {
			for(var i = 0, length = parentKeys.length; i < length; i++)
				filter.push({ property: parentKeys[i], value: record.id });

			return [{ logical: 'or', expressions: filter }];
		}

		return filter;
	},

	afterRecordSet: function(record) {
		if(record != null && this.isDependent())
			return;

		var filter = this.getFilter(record);

		if(filter != null) {
			this.store.setFilter(filter);
			this.store.setValues(this.getValues());
			this.reloadStore();
		} else {
			this.clear();
			this.validate();
		}
	},

	reloadStore: function() {
		var loadCallback = function(store, records, success) {
			this.validate();
			this.updateTools();
		};

		if(this.loadPending = !this.isActive()) {
			this.clear();
			return;
		}

		this.store.load({ fn: loadCallback, scope: this });
	},

	getDependencyWhere: function(property, value) {
		return [{ property: property, value: value }];
	},

	onDependencyChange: function(record, control) {
		var recordId = this.getRecordId();

		var loadCallback = function(store, records, success) {
			this.validate();
			this.updateTools();
		};

		var dependsOnValue = record != null ? (this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id) : null;
		this.setDependsOnValue(dependsOnValue);

		if(dependsOnValue != null && dependsOnValue != guid.Null) {
			var where = this.getDependencyWhere(this.getDependencyField(), dependsOnValue);
			if(this.hasLink())
				where.push({ property: this.getLink(), value: recordId });
			this.store.setWhere(where);
			this.reloadStore();
		} else {
			this.store.removeAll();
			loadCallback.call(this, this.store, [], true);
			this.onSelect(null);
		}
	},

	setActive: function(active) {
		this.callParent(active);

		if(this.list != null)
			this.list.setActive(active);

		if(active && this.loadPending)
			this.reloadStore();
	},

	clear: function() {
		if(this.list != null)
			this.list.setRecords([]);
	},

	getCls: function() {
		var cls = Z8.form.field.Control.prototype.getCls.call(this);

		if(this.needsPager())
			cls.pushIf('pager-on');

		return cls.pushIf('list-box');
	},

	subcomponents: function() {
		return this.callParent().add([this.list, this.pager, this.selectorDlg, this.actions]);
	},

	htmlMarkup: function() {
		var label = this.label;
		if(this.tools) {
			if(label == null)
				label = this.label = {};
			if(label.tools == null)
				label.tools = new Z8.button.Group({ items: this.createTools() });
		}

		return this.callParent();
	},

	setAddTool: function(button) {
		this.addTool = button;
	},

	setCopyTool: function(button) {
		this.copyTool = button;
	},

	setEditTool: function(button) {
		this.editTool = button;
	},

	setRefreshTool: function(button) {
		this.refreshTool = button;
	},

	setRemoveTool: function(button) {
		this.removeTool = button;
	},

	setFilterTool: function(button) {
		this.filterTool = button;
	},

	setPeriodTool: function(button) {
		this.periodTool = button;
	},

	setSortTool: function(button) {
		this.sortTool = button;
	},

	setExportTool: function(button) {
		this.exportTool = button;
	},

	setAutoFitTool: function(button) {
		this.autoFitTool = button;
	},

	createTools: function() {
		var store = this.store;

		var tools = [];

		if(store.hasCreateAccess() && !this.isLocked()) {
			var add = new Z8.button.Button({ icon: 'fa-file-o', tooltip: 'Новая запись (Insert)', handler: this.onAddRecord, scope: this });
			this.setAddTool(add);
			tools.push(add);
		}

		if(store.hasCopyAccess() && !this.isLocked()) {
			var copy = new Z8.button.Button({ icon: 'fa-copy', tooltip: 'Копировать запись (Shift+Insert)', handler: this.onCopyRecord, scope: this });
			this.setCopyTool(copy);
			tools.push(copy);
		}

		if(store.hasReadAccess()) {
			if(this.source != null) {
				var edit = new Z8.button.Button({ icon: 'fa-pencil', tooltip: 'Редактировать \'' + this.source.text + '\'', handler: this.onEdit, scope: this });
				this.setEditTool(edit);
				tools.push(edit);
			};

			var refresh = new Z8.button.Button({ icon: 'fa-refresh', tooltip: 'Обновить (Ctrl+R)', handler: this.onRefresh, scope: this });
			tools.push(refresh);
			this.setRefreshTool(refresh);
		}

		if(store.hasDestroyAccess() && !this.isLocked()) {
			var remove = new Z8.button.Button({ cls: 'remove', danger: true, icon: 'fa-trash', tooltip: 'Удалить запись (Delete)', handler: this.onRemoveRecord, scope: this });
			this.setRemoveTool(remove);
			tools.push(remove);
		}


		var period = this.createPeriodTool();
		this.setPeriodTool(period);
		if(period != null)
			tools.push(period);

		if(this.filter !== false) {
			var filter = new Z8.button.Button({ icon: 'fa-filter', tooltip: 'Фильтрация (Ctrl+F)', toggled: false, toggleHandler: this.onQuickFilter, scope: this });
			this.setFilterTool(filter);
			tools.push(filter);
		}
/*
		var sort = new Z8.button.Button({ enabled: false, icon: 'fa-sort', tooltip: 'Порядок сортировки' });
		this.setSortTool(sort);
		filterSort.push(sort);
*/

		var exportAs = this.createExportTool();
		this.setExportTool(exportAs);
		tools.push(exportAs);

		var autoFit = new Z8.button.Button({ cls: 'auto-fit', icon: 'fa-arrows-h', tooltip: 'Auto fit columns', toggled: this.autoFit, toggleHandler: this.onAutoFit, scope: this });
		this.setAutoFitTool(autoFit);
		tools.push(autoFit);

		return tools;
	},

	createPeriodTool: function() {
		if(this.store.getPeriodProperty() == null)
			return null;

		var period = new Z8.data.Period();

		period = new Z8.calendar.Button({ icon: 'fa-calendar', period: period });
		period.on('period', this.onPeriod, this);
		return period;
	},

	createExportTool: function() {
		var items = [
			new Z8.menu.Item({ text: 'Acrobat Reader (*.pdf)', icon: 'fa-file-pdf-o', format: 'pdf' }),
			new Z8.menu.Item({ text: 'Microsoft Excel (*.xls)', icon: 'fa-file-excel-o', format: 'xls' }),
/*
			new Z8.menu.Item({ text: 'Microsoft Word (*.doc)', icon: 'fa-file-word-o', format: 'doc' }),
*/
			'-',
			new Z8.menu.Item({ text: 'Настройки', icon: 'fa-print', enabled: false })
		];

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuExportAs, this);

		return new Z8.button.Button({ icon: 'fa-file-pdf-o', tooltip: 'Сохранить как PDF', menu: menu, handler: this.exportAs, scope: this, format: 'pdf' });
	},

	createActions: function() {
		var query = this.query;

		if(query == null)
			return null;

		var actions = query.actions;

		if(Z8.isEmpty(actions))
			return null;

		var buttons = [];

		for(var i = 0, length = actions.length; i < length; i++) {
			var action = actions[i];
			action.handler = this.onAction;
			action.scope = this;
			var button = Z8.form.Helper.createControl(action);
			buttons.push(button);
		}

		this.controls = buttons;

		return new Z8.Container({ cls: 'actions',  items: buttons });
	},

	createList: function() {
		var config = { cls: 'control', tabIndex: this.getTabIndex(), store: this.store, items: this.items, totals: this.totals, numbers: this.numbers, startCollapsed: this.startCollapsed, locks: this.locks, name: this.name, fields: this.fields, editable: this.isEditable(), itemType: this.itemType, itemConfig: this.itemConfig, value: this.getValue(), icons: this.icons, checks: this.checks, filters: this.filters, enterToSelect: false, enterToEdit: this.enterToEdit, autoFit: this.autoFit, filterVisible: this.filterVisible, autoSelectFirst: this.autoSelectFirst, hideHeaders: this.hideHeaders };
		return this.listCls != null ? Z8.create(this.listCls, config) : new Z8.list.List(config);
	},

	controlMarkup: function() {
		var list = this.list = this.createList();
		list.on('select', this.onSelect, this);
		list.on('check', this.onCheck, this);
		list.on('follow', this.onFollow, this);
		list.on('contentChange', this.onContentChange, this);
		list.on('itemEditorChange', this.onItemEditorChange, this);
		list.on('itemEdit', this.onItemEdit, this);
		list.on('iconClick', this.onIconClick, this);
		list.on('itemClick', this.onItemClick, this);
		list.on('itemDblClick', this.onItemDblClick, this);
		list.on('quickFilterShow', this.onQuickFilterShow, this);
		list.on('quickFilterHide', this.onQuickFilterHide, this);
		list.on('autoFit', this.onListAutoFit, this);

		var cls = this.needsPager() ? '' : 'display-none';
		var pager = this.pager = new Z8.pager.Pager({ cls: cls, store: this.store });

		var markup = [list.htmlMarkup(), pager.htmlMarkup()];

		var actions = this.actions = this.createActions();
		if(actions != null)
			markup.push(actions.htmlMarkup());

		return markup;
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		var store = this.store;
		store.on('beforeLoad', this.onBeforeLoad, this);
		store.on('load', this.onLoad, this);
	},

	onDestroy: function() {
		this.detachRecordChange(this.getRecord());

		var store = this.store;
		store.un('beforeLoad', this.onBeforeLoad, this);
		store.un('load', this.onLoad, this);

		DOM.un(this, 'keyDown', this.onKeyDown, this);
		this.callParent();
	},

	setFields: function(fields) {
		this.fields = fields;
		if(this.list != null)
			this.list.setFields(fields);
	},

	onSelect: function(item, oldItem) {
		var oldRecord = oldItem != null ? oldItem.record : null;
		var record = item != null ? item.record : null;

		this.mixins.field.setValue.call(this, record != null ? record.id : guid.Null);

		this.fireEvent('select', this, record, oldRecord);
		this.updateTools();

		if(this.selectTask == null)
			this.selectTask = new Z8.util.DelayedTask();

		this.selectTask.delay(50, this.updateDependencies, this, record);
	},

	onFollow: function(record, field) {
		var source = field.source;

		if(source != null) {
			var link = source.link || (field.link ? field.link.owner : null);

			var params = { 
				request: source.request,
				where: { property: 'recordId', value: link != null ? record.get(link) : record.id }
			};

			Viewport.open(params, false, { oneRecord: true, sourceLink: link, title: record.get(field.name) });
		} else if(field.type == Type.File) {
			var file = record.get(field.name);
			if(!Z8.isEmpty(file))
				DOM.download(file[0].path, file[0].id);
		}
	},

	onContentChange: function() {
		this.fireEvent('contentChange', this);
		this.updateTools();
		this.updatePager();
		this.validate();
	},

	onItemEditorChange: function(list, editor, newValue, oldValue) {
		this.fireEvent('itemEditorChange', this, editor, newValue, oldValue);
	},

	onItemEdit: function(list, editor, record, field) {
		if(field != null && field.important)
			this.reloadRecord();
		this.fireEvent('itemEdit', this, editor, record, field);
	},

	focus: function() {
		return this.isEnabled() ? this.list.focus() : false;
	},

	getSelection: function() {
		return this.list != null ? this.list.getCurrentRecord() : null;
	},

	getChecked: function() {
		if(this.list == null)
			return [];

		var records = this.list.getCheckedRecords();

		if(records.length != 0)
			return records;

		var selection = this.getSelection();
		return selection != null ? [selection] : [];
	},

	getDestroyable: function() {
		var destroyable = [];

		var checked = this.getChecked();

		for(var i = 0, length = checked.length; i < length; i++) {
			var record = checked[i];
			if(record.isDestroyable())
				destroyable.push(record);
		}

		return destroyable;
	},

	select: function(item) {
		this.list.setSelection(item);
		return item;
	},

	startEdit: function(record, index) {
		if(record != null) {
			var list = this.list;
			var item = list.getItem(record.id);
			if(!list.canStartEdit(item, index))
				return false;
			list.startEdit(item, index);
			return true;
		}
		return false;
	},

	getSelectorConfig: function() {
		var linkedField = null;
		var columns = [];
		var myLink = this.getLink();
		var fields = this.query.columns;

		if(fields == null)
			return null;

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(field.isCombobox) {
				if(!field.link.isJoined && field.link.name != myLink) {
					if(linkedField != null && field.link.name != linkedField.link.name)
						return null;
					linkedField = field;
				}
				if(field.link.name != myLink)
					columns.push(field);
			}
		}

		if(linkedField == null)
			return null;

		return { link: { name: linkedField.link.name, query: { name: linkedField.query.name, primaryKey: linkedField.link.primaryKey }}, columns: columns, multiselect: true };
	},

	createRecordsSelector: function(action) {
		var selector = this.selector;

		if(selector == null) {
			if(this.query == null)
				return null;
			selector = this.selector = this.getSelectorConfig();
		}

		if(selector == null || action == 'copy' && !selector.copy)
			return null;

		var link = selector.link;
		var query = link.query;
		var columns = selector.columns;

		var config = { isListbox: true, readOnly: true, label: false, tools: false, pagerMode: 'visible', checks: selector.multiselect, flex: 1, height: 3, values: this.getValues() };
		Z8.apply(config, { name: link.name, query: { request: this.query.request, name: query.name, fields: columns, sort: selector.sort, columns: columns, primaryKey: null } });
		return Z8.form.Helper.createControl(config);
	},

	openSelector: function(action, button, callback) {
		var selector = this.createRecordsSelector(action);

		if(selector == null) {
			var record = this.newRecord();
			Z8.callback(callback, Array.isArray(record) ? record : [record]);
			return;
		}

		if(button != null)
			button.setBusy(true);

		var loadCallback = function(store, records, success) {
			if(button != null)
				button.setBusy(false);

			if(!success)
				return;

			var okCallback = function(dialog, success) {
				if(!success) {
					this.closeSelector();
					this.focus();
					return;
				}

				var result = [];
				var listbox = dialog.selector;
				var checked = listbox.getChecked();
				var name = listbox.name;
				var valueFromFields = this.getValueFromFields();
				var primaryKey = this.selector.link.query.primaryKey;

				for(var i = 0, length = checked.length; i < length; i++) {
					var record = this.newRecord();
					var records = Array.isArray(record) ? record : [record];
					var checkedRecord = checked[i];

					for(var j = 0; j < records.length; j++) {
						record = records[j];
						record.set(name, checkedRecord.get(primaryKey));

						for(var k = 0; k < valueFromFields.length; k++) {
							var valueFromField = valueFromFields[k];
							record.set(valueFromField.name, checkedRecord.get(valueFromField.valueFrom));
						}
					}

					result.add(records);
				}

				Z8.callback(callback, result);
			};

			this.selectorDlg = new Z8.window.Window({ header: this.query.text, icon: 'fa-plus-circle', autoClose: false, controls: [selector], selector: selector, cls: this.selectorCls, handler: okCallback, scope: this });
			this.selectorDlg.open();
		};

		selector.store.load({ fn: loadCallback, scope: this });
	},

	closeSelector: function() {
		this.selectorDlg.close();
		this.selectorDlg = null;
	},

	newRecord: function() {
		var newRecord = Z8.create(this.getModel());

		if(this.hasLink()) {
			var record = this.getRecord();
			var value = record != null ? (this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id) : null;
			newRecord.set(this.getLink(), value);
		}

		if(this.isDependent())
			newRecord.set(this.getDependencyField(), this.getDependsOnValue());

		return newRecord;
	},

	onAddRecord: function(button) {
		this.addRecord(button);
	},

	addRecord: function(button) {
		var callback = function(records) {
			this.createRecords(records);
		};

		this.openSelector('create', button, { fn: callback, scope: this });
	},

	filterCreatedRecords: function(records) {
		return records;
	},

	createRecords: function(records, callback) {
		var addTool = this.addTool;

		var batchCallback = function(records, success) {
			if(success) {
				this.reloadRecord();

				records = this.filterCreatedRecords(records);

				var store = this.store;
				store.setValues(this.getValues());
				store.insert(records, 0);
				var record = records[0];
				this.select(record);

				this.fireEvent('recordCreated', this, records);

				if(this.selectorDlg != null || !this.startEdit(record, 0))
					this.focus();
			}

			var selectorDlg = this.selectorDlg;

			if(selectorDlg != null)
				success ? this.closeSelector() : selectorDlg.setBusy(false);

			if(addTool != null)
				addTool.setBusy(false);

			Z8.callback(callback, records, success);
		};

		if(addTool != null)
			addTool.setBusy(true);

		var batch = new Z8.data.Batch({ model: this.getModel() });
		batch.create( records, { fn: batchCallback, scope: this }, { values: this.getValues() });
	},

	onCopyRecord: function(button) {
		this.copyRecord(this.getChecked()[0], button);
	},

	copyRecord: function(record, button) {
		var callback = function(records) {
			this.copyRecords(record, records[0]);
		};

		this.openSelector('copy', button, { fn: callback, scope: this });
	},

	copyRecords: function(record, newRecord) {
		var copyTool = this.copyTool;

		var callback = function(record, success) {
			if(success) {
				this.reloadRecord();
				this.store.insert(record, 0);
				this.select(record.id);
				if(!this.startEdit(record, 0))
					this.focus();
			}

			var selectorDlg = this.selectorDlg;

			if(selectorDlg != null)
				success ? this.closeSelector() : selectorDlg.setBusy(false);

			if(copyTool != null)
				copyTool.setBusy(false);
		};

		if(copyTool != null)
			copyTool.setBusy(true);

		newRecord.copy(record, { fn: callback, scope: this }, { values: this.getValues() });
	},

	reloadRecord: function(callback) {
		var record = this.record;
		if(record != null)
			record.reload(callback);
	},

	onBeforeLoad: function(store) {
		if(this.refreshTool != null)
			this.refreshTool.setBusy(true);
	},

	onLoad: function(store, records, success) {
		if(this.refreshTool != null)
			this.refreshTool.setBusy(false);
	},

	onRefresh: function(button, skipFocus) {
		button = button || this.refreshTool;

		if(button != null) {
			var callback = function(records, success) {
				button.setBusy(false);
				if(!skipFocus)
					this.focus();
				this.validate();
				this.updateTools();
			};
			button.setBusy(true);
			this.store.load({ fn: callback, scope: this });
		} else
			this.store.load({ fn: callback, scope: this });
	},

	onRemoveRecord: function(button) {
		this.removeRecord(this.getDestroyable(), button);
	},

	removeRecord: function(records, button) {
		if(records.length == 0)
			throw 'Listbox.removeRecord: records.length == 0';

		var index = this.store.indexOf(records[0]);

		var callback = function(records, success) {
			if(button != null)
				button.setBusy(false);
			if(success) {
				this.reloadRecord();
				this.select(index);
				this.onRecordDestroyed(records);
				this.focus();
			}
		};

		if(button != null)
			button.setBusy(true);

		var batch = new Z8.data.Batch({ store: this.store });
		batch.destroy(records, { fn: callback, scope: this });
	},

	onRecordDestroyed: function(records) {
		this.fireEvent('recordDestroyed', this, records);
	},

	onPeriod: function(button, period, action) {
		if(action != Period.NoAction)
			this.store.setPeriod(period);
		this.onRefresh(this.periodTool);
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

	onMenuExportAs: function(menu, item) {
		var format = item.format;
		if(format == 'pdf')
			this.exportAsPdf();
		else if(format == 'xls')
			this.exportAsXls();
		else if(format == 'doc')
			this.exportAsDoc();
	},

	exportAsPdf: function() {
		var tool = this.exportTool;
		tool.setIcon('fa-file-pdf-o');
		tool.setTooltip('Сохранить как PDF');
		tool.format = 'pdf';
		this.exportAs();
	},

	exportAsXls: function() {
		var tool = this.exportTool;
		tool.setIcon('fa-file-excel-o');
		tool.setTooltip('Сохранить как XLS');
		tool.format = 'xls';
		this.exportAs();
	},

	exportAsDoc: function() {
		var tool = this.exportTool;
		tool.setIcon('fa-file-word-o');
		tool.setTooltip('Сохранить как DOC');
		tool.format = 'doc';
		this.exportAs();
	},

	exportAs: function() {
		var tool = this.exportTool;
		var format = tool.format;

		var columns = [];
		var table = this.table;
		var headers = this.getHeaders();

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
			query: store.getQuery(),
			columns: columns,
			filter: store.getFilter(),
			quickFilter: store.getQuickFilter(),
			where: store.getWhere(),
			sort: store.getSorter(),
			period: store.getPeriod(),
			values: store.getValues()
		};

		var callback = function(response, success) {
			tool.setBusy(false);
		};

		tool.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });
		this.focus();
	},

	onAction: function(button) {
		button.setBusy(true);

		var action = button.action;

		if(!Z8.isEmpty(action.parameters))
			this.requestActionParameters(button);
		else
			this.runAction(button);
	},

	requestActionParameters: function(button) {
		this.runAction(button);
	},

	runAction: function(button) {
		var action = button.action;

		var record = this.record;
		var query = this.query;

		var params = {
			request: action.request,
			action: 'action',
			name: action.name,
			query: query.name,
			records: (record != null && !record.phantom) ? [record.id] : null,
			selection: this.getSelectedIds(),
			parameters: action.parameters
		};

		var callback = function(response, success) {
			button.setBusy(false);
			this.onActionComplete(button, record, response, success);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onActionComplete: function(button, record, response, success) {
		if(success && this.record == record) {
			var reloadCallback = function(record, success) {
				button.setBusy(false);
				if(success) {
					this.onRefresh();
					this.fireEvent('action', this, button.action);
				}
			};
			record.reload({ fn: reloadCallback, scope: this });
		}
	},

	onIconClick: function(list, item) {
		this.fireEvent('iconClick', this, item);
	},

	onItemClick: function(list, item, index) {
		this.fireEvent('itemClick', this, item, index);
	},

	onItemDblClick: function(list, item, index) {
		this.fireEvent('itemDblClick', this, item, index);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(this.list.isEditing() || this.list.isFiltering())
			return;

		if(key == Event.INSERT) {
			if(!event.ctrlKey) {
				var addTool = this.addTool;
				if(addTool == null || !addTool.isEnabled())
					return;

				addTool.handler.call(addTool.scope, addTool);
				event.stopEvent();
			} else {
				var copyTool = this.copyTool;
				if(copyTool == null || !copyTool.isEnabled())
					return;

				copyTool.handler.call(copyTool.scope, copyTool);
				event.stopEvent();
			}
		} else if(key == Event.DELETE && !DOM.isParentOf(this.pager, target)) {
			var removeTool = this.removeTool;
			if(removeTool == null || !removeTool.isEnabled())
				return;

			removeTool.handler.call(removeTool.scope, removeTool);
			event.stopEvent();
		} else if(key == Event.A && event.ctrlKey) {
			if(this.list.checkAll())
				event.stopEvent();
		} else if(key == Event.R && event.ctrlKey && !event.shiftKey) {
			this.onRefresh(this.refreshTool);
			event.stopEvent();
		}
	},

	onCheck: function(list, items, checked) {
		this.updateTools();

		var records = [];
		for(var i = 0, length = items.length; i < length; i++)
			records.push(items[i].record);

		this.fireEvent('check', this, records, checked);
	},

	onEdit: function(button) {
		Viewport.open(this.source.request);
	},

	onQuickFilter: function(button, toggled) {
		this.list.showQuickFilter(toggled, true);
	},

	onQuickFilterShow: function(list) {
		if(this.filterTool != null)
			this.filterTool.setToggled(true, true);
	},

	onQuickFilterHide: function(list) {
		if(this.filterTool != null)
			this.filterTool.setToggled(false, true);
	},

	onAutoFit: function(button, toggled) {
		this.list.setAutoFit(toggled, true);
		this.focus();
	},

	show: function(show) {
		this.callParent(show);
		this.list.adjustAutoFit();
	},

	onListAutoFit: function(button, toggled) {
		if(this.autoFitTool != null)
			this.autoFitTool.setToggled(toggled, true);
	},

	updateTools: function() {
		if(!this.tools || this.getDom() == null)
			return;

		var enabledNotReadOnly = this.isEnabled() && !this.isReadOnly();

		if(this.removeTool != null)
			this.removeTool.setEnabled(enabledNotReadOnly && !Z8.isEmpty(this.getDestroyable()));

		if(this.copyTool != null) {
			var selected = this.getChecked();
			this.copyTool.setEnabled(enabledNotReadOnly && !Z8.isEmpty(selected) && selected.length == 1);
		}

		if(this.addTool != null) {
			var record = this.getRecord();
			this.addTool.setEnabled(enabledNotReadOnly && record != null && (!this.isDependent() || this.hasDependsOnValue()));
		}
	},

	needsPager: function() {
		return this.pagerMode == 'visible';
	},

	updatePager: function() {
		var needsPager = this.needsPager();
		DOM.swapCls(this.pager, !needsPager, 'display-none');
		DOM.swapCls(this, needsPager, 'pager-on');
	}
});