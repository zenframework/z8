Z8.define('Z8.form.field.Combobox', {
	extend: 'TextBox',
	shortClassName: 'ComboBox',

	statics: {
		ExpandIconCls: 'fa-caret-down',
		ClearIconCls: 'fa-times'
	},

	isComboBox: true,
	instantAutoSave: true,

	autocomplete: 'off',

	displayName: 'name',
	name: 'id',

	emptyValue: guid.Null,

	enterToOpen: true,
	enterOnce: false,

	queryDelay: 250,
	lastQuery: '',

	pagerMode: 'hidden', // 'visible' || 'hidden'
	clearTrigger: true,

	initComponent: function() {
		var store = this.store;

		if(store != null && !store.isStore)
			store = this.store = new Z8.query.Store(store);

		TextBox.prototype.initComponent.call(this);

		if(this.editor)
			this.queryTask = new Z8.util.DelayedTask();

		this.initStore();
	},

	getCls: function() {
		return TextBox.prototype.getCls.call(this).pushIf('combobox');
	},

	isEmptyValue: function(value) {
		return value == this.emptyValue || TextBox.prototype.isEmptyValue.call(this, value);
	},

	getStore: function() {
		return this.store;
	},

	createDropdown: function() {
		var dropdown = this.dropdown = new Z8.list.Dropdown({ store: this.getStore(), value: this.getValue(), name: this.name, fields: this.fields || this.displayName, hideHeaders: this.hideHeaders, icons: this.icons, checks: this.checks });
		dropdown.on('select', this.selectItem, this);
		dropdown.on('cancel', this.cancelDropdown, this);

		var cls = this.needsPager() ? '' : 'display-none';
		var pager = this.pager = new Z8.pager.Pager({ visible: false, store: this.getStore() });

		dropdown.on('align', this.onDropdownAlign, this);
		dropdown.on('show', this.onDropdownShow, this);
		dropdown.on('hide', this.onDropdownHide, this);
	},

	initTriggers: function() {
		this.triggers = !Z8.isEmpty(this.triggers) ? this.triggers : { icon: ComboBox.ExpandIconCls };

		var triggers = [];
		if(this.source != null)
			triggers.push({ icon: 'fa-pencil', tooltip: Z8.$('ComboBox.edit') + this.source.text + '\'', handler: this.editSource, scope: this });

		if(!this.isRequired() && this.clearTrigger !== false)
			triggers.push({ icon: ComboBox.ClearIconCls, tooltip: Z8.$('ComboBox.clear'), handler: this.clearValue, scope: this });

		this.triggers = triggers.add(this.triggers);

		TextBox.prototype.initTriggers.call(this);
	},

	htmlMarkup: function() {
		var markup = TextBox.prototype.htmlMarkup.call(this);

		this.createDropdown();

		markup.cn.push(this.dropdown.htmlMarkup());
		markup.cn.push(this.pager.htmlMarkup());

		return markup;
	},

	subcomponents: function() {
		return TextBox.prototype.subcomponents.call(this).add([this.dropdown, this.pager]);
	},

	completeRender: function() {
		TextBox.prototype.completeRender.call(this);

		this.hidePager();
		this.dropdown.setAlignment(this.getBox());

		if(!this.editor)
			DOM.on(this, 'keyPress', this.onKeyPress, this);

		DOM.on(this.input, 'dblClick', this.onDblClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keyPress', this.onKeyPress, this);
		DOM.un(this.input, 'dblClick', this.onDblClick, this);

		this.setStore(null);

		TextBox.prototype.onDestroy.call(this);
	},

	setEnabled: function(enabled) {
		if(!enabled && this.dropdown != null)
			this.dropdown.hide();

		TextBox.prototype.setEnabled.call(this, enabled);
	},

	setReadOnly: function(readOnly) {
		if(readOnly && this.dropdown != null)
			this.dropdown.hide();

		TextBox.prototype.setReadOnly.call(this, readOnly);
	},

	getFilterOperator: function(type) {
		switch(type) {
		case Type.Integer:
		case Type.Float:
			return Operator.Eq;
		case Type.Date:
		case Type.Datetime:
		case Type.Boolean:
			return null;
		case Type.String:
		default:
			return Operator.Contains;
		}
	},

	getType: function() {
		return this.field != null ? this.field.type : Type.String;
	},

	formatValue: function(value) {
		var field = this.field || {};

		if(String.isString(value))
			return value;

		switch(field.type) {
		case Type.Date:
		case Type.Datetime:
			return value != null ? Format.date(value, field.format) : '';
		case Type.Boolean:
			return value ? Z8.$('ComboBox.true') : Z8.$('ComboBox.false');
		case Type.Integer:
			return value !== null ? Format.integer(value, field.format) : '';
		case Type.Float:
			return value !== null ? Format.float(value, field.format) : '';
		default:
			return value;
		}
	},

	getSelectedRecord: function(value) {
		var store = this.getStore();
		return store != null ? store.getById(value || this.getValue()) : null;
	},

	setValue: function(value, displayValue) {
		if(displayValue === undefined) {
			var record = this.getSelectedRecord(value);
			displayValue = record != null ? record.get(this.displayName) : null;
		}

		this.entered = true;

		this.displayValue = displayValue = this.isEmptyValue(value) ? '' : this.formatValue(displayValue);

		this.clearFilter();
		TextBox.prototype.setValue.call(this, value, displayValue);
	},

	setRecord: function(record) {
		TextBox.prototype.setRecord.call(this, record);

		this.updateWhere(this.dependsOnValue, record);

		var store = this.getStore();
		if(store != null)
			store.setValues(record);

		this.innerUpdateDependencies(record);
	},

	isEqual: function(v1, v2) {
		return (Z8.isEmpty(v1) || v1 == guid.Null) && (Z8.isEmpty(v2) || v2 == guid.Null) || TextBox.prototype.isEqual.call(this, v1, v2);
	},

	innerUpdateDependencies: function(record) {
		var store = this.getStore();
		if(store == null || this.dependencies == null)
			return;

		var value = record != null ? record.get(this.name) : null;

		var tempRecord = null;

		if(value != null) {
			var selectedRecord = store.getById(value);

			var data = {};
			data[store.getIdProperty()] = value;
			var tempRecord = Z8.create(store.getModel(), data);

			if(selectedRecord != null)
				Z8.apply(tempRecord.data, selectedRecord.data);
			if(record != null)
				Z8.apply(tempRecord.data, record.data);
		}

		this.initializing = true;
		this.updateDependencies(tempRecord);
		this.initializing = false;
	},

	onDependencyChange: function(record, control) {
		var value = this.dependsOnValue = record != null ? (this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id) : null;
		this.updateWhere(value, record);

		if(control.initializing || control.isListBox)
			this.suspendCheckChange++;

		this.setValue(guid.Null);

		if(control.initializing || control.isListBox)
			this.suspendCheckChange--;
	},

	updateWhere: function(value, record) {
		var where = this.getWhere(value, record);

		var store = this.getStore();
		store.setWhere(where);
		store.unload();
	},

	getWhere: function(dependencyValue, record) {
		var where = [];

		if(dependencyValue != null)
			where.push({ property: this.getDependencyField(), value: dependencyValue });

		var field = this.field;

		if(field == null)
			return where;

		var link = field.link;

		if(record == null || link == null || !link.isParentKey)
			return where;

		var parentKeys = link.parentKeys;
		for(var i = 0, length = parentKeys.length; i < length; i++)
			where.push({ property: parentKeys[i], operator: Operator.NotEq, value: record.id });

		return where;
	},

	getDisplayValue: function() {
		return this.displayValue;
	},

	valueToRaw: function(value) {
		return this.isEmptyValue(value) ? '' : this.formatValue(this.displayValue);
	},

	getCurrentItem: function() {
		return this.dropdown.getItem(this.getValue());
	},

	getCurrentIndex: function() {
		return this.dropdown.getIndex(this.getValue());
	},

	isLoaded: function() {
		return this.dropdown.isLoaded();
	},

	initStore: function() {
		var store = this.store;
		this.store = null;
		this.setStore(store);
	},

	setStore: function(store) {
		if(this.store == store)
			return;

		var currentStore = this.store;

		if(currentStore != null) {
			currentStore.un('beforeLoad', this.beforeLoadCallback, this);
			currentStore.un('load', this.loadCallback, this);
			currentStore.dispose();
		}

		this.store = store;

		if(store != null) {
			store.use();

			this.beforeLoadCallback = this.beforeLoadCallback || function() {
				this.getTrigger().setBusy(true);
			};
			store.on('beforeLoad', this.beforeLoadCallback, this);

			this.loadCallback = this.loadCallback || function() {
				this.getTrigger().setBusy(false);
			};
			store.on('load', this.loadCallback, this);
		}

		if(this.pager != null)
			this.pager.setStore(store);

		if(this.dropdown != null)
			this.dropdown.setStore(store);
	},

	load: function(callback, filter) {
		this.dropdown.load(callback, filter);
	},

	filter: function() {
		var text = this.getRawValue() || '';

		if(text == this.lastQuery && text != '')
			return;

		var store = this.dropdown.getStore();

		if(store == null)
			return;

		var delay = store.getRemoteFilter() ? this.queryDelay : 0;

		var query = function(text) {
			if(store.isLoading()) {
				this.queryTask.delay(delay, query, this, text);
				return;
			}

			this.lastQuery = text;

			var callback = function(store, records, success) {
				if(success && !this.disposed) {
					var item = this.findItem(text);
					this.dropdown.selectItem(item);
					this.showDropdown(true);
				}
			};

			this.load({ fn: callback, scope: this }, this.getFilter(text));
		};

		this.queryTask.delay(delay, query, this, text);
	},

	getFilter: function(text) {
		if(Z8.isEmpty(text))
			return [];

		var fields = this.fields || [{ name: this.displayName, type: Type.String }];
		var expressions = [];

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var operator = this.getFilterOperator(field.type);
			if(operator != null)
				expressions.add({ property: field.name, operator: operator, value: text, anyMatch: true });
		}
		return { logical: 'or', expressions: expressions };
	},

	clearFilter: function() {
		if(this.lastQuery.isEmpty())
			return;

		this.queryTask.cancel();
		this.lastQuery = '';

		this.dropdown.clearFilter();
	},

	findItem: function(text, start) {
		if(Z8.isEmpty(text))
			return null;

		var items = this.dropdown.getItems();

		start = start || 0;
		var index = start;
		var count = items.length;

		text = text.toLowerCase();

		while(index < count) {
			var item = items[index];
			var value = this.formatValue(item.getText(this.displayName)).toLowerCase();
			if(value.startsWith(text))
				return item;

			index = index < count -1 ? index + 1 : 0;

			if(index == start)
				return null;
		}

		return null;
	},

	// direction: first, last, next, previous
	select: function(item, direction) {
		var callback = function(store, records, success) {
			if(!success)
				return;

			if(String.isString(item)) {
				direction = item;
				item = this.getCurrentItem();
			} else
				item = item || this.getCurrentItem();

			var index = item != null ? this.dropdown.getIndex(item.getValue()) : -1;

			var items = this.dropdown.getItems();
			var count = items.length;

			var startIndex = index;

			do {
				if(direction == 'next')
					index = index != count - 1 ? index + 1 : 0;
				else if(direction == 'previous')
					index = index != 0 ? index - 1 : (count - 1);
				else if(direction == 'first')
					index = 0;
				else if(direction == 'last')
					index = count - 1;

				index = Math.min(Math.max(index, 0), count - 1);

				if(index == -1 || index == startIndex)
					return;

				item = items[index];
			} while(item.isHidden());

			this.setValue(item.getValue(), item.getText(this.displayName));
		};

		if(!this.isLoaded())
			this.load({ fn: callback, scope: this });
		else
			callback.call(this, this.store, this.store.getRecords(), true);
	},

	selectByKey: function(key) {
		var callback = function(store, records, success) {
			if(!success)
				return;

			var index = this.getCurrentIndex();
			var item = this.findItem(key, index + 1);

			item = this.findItem(key, index + 1);

			if(item != null)
				this.setValue(item.getValue(), item.getText(this.displayName));
		};

		if(!this.isLoaded())
			this.load({ fn: callback, scope: this });
		else
			callback.call(this, this.store, this.store.getRecords(), true);
	},

	selectItem: function(item) {
		var value = item != null ? item.getValue() : this.emptyValue;
		var displayValue = item != null ? item.getText(this.displayName) : '';
		this.setValue(value, displayValue);
		this.hideDropdown();
	},

	show: function(show) {
		this.entered = false;
		TextBox.prototype.show.call(this, show);
	},

	openDropdown: function() {
		var callback = function(store, records, success) {
			if(success && !this.disposed)
				this.showDropdown();
		};

		if(!this.isLoaded())
			this.load({ fn: callback, scope: this });
		else
			this.showDropdown();
	},

	needsPager: function() {
		return this.pagerMode == 'visible';
	},

	realignPager: function() {
		if(!this.needsPager())
			return;

		var dropdown = this.dropdown;
		var rect = new Rect(this.dropdown);

		var pager = DOM.get(this.pager);
		DOM.setLeft(pager, DOM.getLeft(dropdown));
		DOM.setTop(pager, DOM.getTop(dropdown) + rect.height);
		DOM.setWidth(pager, rect.width);
	},

	getPagerSize: function() {
		if(!this.needsPager())
			return { width: 0, height: 0 };

		var pager = this.pager;

		var wasVisible = this.pager.isVisible();

		if(!wasVisible)
			this.showPager();

		pager.size = pager.getSize();

		if(!wasVisible)
			this.hidePager();

		return pager.size;
	},

	showDropdown: function(keepFocus) {
		var dropdown = this.dropdown;

		var width = DOM.getOffsetWidth(this/*.input*/);
		DOM.setWidth(dropdown, width);

		var item = this.getCurrentItem();
		var focusAt = keepFocus ? false : item;

		var pagerSize = this.getPagerSize();

		dropdown.setAlignmentOffset(0, pagerSize.height, 1.41666667 /* Ems.pixelsToEms(17) */, true);
		dropdown.show(null, null, focusAt);

		DOM.addCls(this, 'open');
	},

	hideDropdown: function() {
		this.dropdown.hide();
		this.clearFilter();

		DOM.removeCls(this, 'open');
	},

	cancelDropdown: function() {
		this.setRawValue(this.displayValue);
		this.hideDropdown();
	},

	toggleDropdown: function() {
		if(this.dropdown.isVisible())
			this.cancelDropdown();
		else
			this.openDropdown();
	},

	showPager: function() {
		if(!this.needsPager())
			this.hidePager();
		else
			this.pager.show();
	},

	hidePager: function() {
		this.pager.hide();
	},

	onDropdownAlign: function(dropdown) {
		this.realignPager();
		this.showPager();
	},

	onDropdownShow: function() {
		this.getTrigger().rotateIcon(180);
	},

	onDropdownHide: function() {
		this.hidePager();
		this.getTrigger().rotateIcon(0);
		DOM.focus(this.input);
	},

	onFocusOut: function(event) {
		if(!TextBox.prototype.onFocusOut.call(this, event))
			return false;

		this.cancelDropdown();
		return true;
	},

	onTriggerClick: function(trigger) {
		if(this.isEnabled() && !this.isReadOnly())
			this.toggleDropdown();
	},

	onInputClick: function() {
		if(this.isEnabled() && !this.isReadOnly())
			this.toggleDropdown();
	},

	onDblClick: function(event, target) {
		if(this.isEnabled() && !this.isReadOnly())
			this.toggleDropdown();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		var editor = this.editor;
		var dropdown = this.dropdown;
		var dropdownOpen = dropdown.isVisible();
		var currentItem = this.dropdown.getCurrentItem();

		var me = this;
		var isTrigger = function() {
			return DOM.isParentOf(me.triggers, target);
		};

		var isInput = function() {
			return DOM.isParentOf(me.input, target);
		};

		if(key == Event.DOWN)
			dropdownOpen ? dropdown.focus(currentItem) : this.select('next');
		else if(key == Event.UP)
			dropdownOpen ? dropdown.focus(currentItem) : this.select('previous');
		else if(key == Event.HOME && (!editor || !isInput()))
			dropdownOpen ? dropdown.focus(currentItem) : this.select('first');
		else if(key == Event.END && (!editor || !isInput()))
			dropdownOpen ? dropdown.focus(currentItem) : this.select('last');
		else if(key == Event.TAB && isTrigger()) {
			dropdownOpen ? dropdown.focus(currentItem) : this.cancelDropdown();
			return false;
		} else if(key == Event.ENTER || key == Event.SPACE && (!editor || isTrigger())) {
			if(dropdownOpen)
				currentItem || !this.isRequired() ? this.selectItem(currentItem) : this.cancelDropdown();
			else if(this.enterToOpen && (!this.enterOnce || !this.entered)) {
				this.onTriggerClick(event, target);
				this.entered = true;
			} else if(event.ctrlKey)
				this.onTriggerClick(event, target);
			else
				return false;
		} else if(key == Event.ESC && (dropdownOpen || this.lastQuery != ''))
			this.cancelDropdown();
		else
			return false;

		return true;
	},

	onInput: function(event, target) {
		this.filter();
	},

	onKeyPress: function(event, target) {
		this.selectByKey(String.fromCharCode(event.getKey()));
	},

	editSource: function() {
		var source = this.source;
		if (source == null)
			return;
		var link = source.link;
		var value = link != null ? this.record.get(link) : this.value;
		var filter, sourceFilter;
		if (value != null && value != guid.Null) {
			filter = [{ property: 'recordId', value: value }];
			sourceFilter = {
				property: !Z8.isEmpty(source.field) ? source.field : this.displayName.lastAfter('.'),
				value: this.displayValue
			};
		}
		Viewport.open({ filter: filter, sourceFilter: sourceFilter, request: source.request });
		this.getStore().unload();
	},

	clearValue: function() {
		this.setValue(this.emptyValue, '');
	}
});
