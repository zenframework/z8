Z8.define('Z8.form.field.Combobox', {
	extend: 'Z8.form.field.Text',

	isCombobox: true,
	instantAutoSave: true,

	displayName: 'name',
	name: 'id',

	emptyValue: guid.Null,

	enterToOpen: true,
	enterOnce: false,

	queryDelay: 250,
	lastQuery: '',

	pagerMode: 'visible', // 'visible' || 'hidden'

	initComponent: function() {
		var store = this.store;

		if(store != null && !store.isStore)
			store = this.store = new Z8.query.Store(store);

		this.callParent();

		this.cls = DOM.parseCls(this.cls).pushIf('dropdown-combo');

		if(this.editable)
			this.queryTask = new Z8.util.DelayedTask();

		this.initStore();
	},

	isEmptyValue: function(value) {
		return value == this.emptyValue || this.callParent(value);
	},

	getStore: function() {
		return this.store;
	},

	createDropdown: function() {
		var dropdown = this.dropdown = new Z8.list.Dropdown({ store: this.getStore(), value: this.getValue(), name: this.name, fields: this.fields || this.displayName, icons: this.icons, checks: this.checks });
		dropdown.on('select', this.selectItem, this);
		dropdown.on('cancel', this.cancelDropdown, this);

		var cls = this.needsPager() ? '' : 'display-none';
		var pager = this.pager = new Z8.pager.Pager({ cls: 'display-none', store: this.getStore() });

		dropdown.on('align', this.onDropdownAlign, this);
		dropdown.on('show', this.onDropdownShow, this);
		dropdown.on('hide', this.onDropdownHide, this);
	},

	htmlMarkup: function() {
		var triggers = this.triggers;

		if(this.source != null)
			triggers.push({ icon: 'fa-pencil', tooltip: 'Редактировать \'' + this.source.text + '\'', handler: this.editSource, scope: this });

		if(!this.isRequired())
			triggers.push({ icon: 'fa-times', tooltip: 'Очистить', handler: this.clearValue, scope: this });

		triggers.push({});

		var markup = this.callParent();

		this.createDropdown();

		markup.cn.push(this.dropdown.htmlMarkup());
		markup.cn.push(this.pager.htmlMarkup());

		return markup;
	},

	subcomponents: function() {
		return this.callParent().add([this.dropdown, this.pager]);
	},

	completeRender: function() {
		this.callParent();

		this.dropdown.setAlignment(this.input);

		if(!this.editable)
			DOM.on(this, 'keyPress', this.onKeyPress, this);

		DOM.on(this.input, 'dblClick', this.onDblClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keyPress', this.onKeyPress, this);
		DOM.un(this.input, 'dblClick', this.onDblClick, this);

		this.setStore(null);

		this.callParent();
	},

	setEnabled: function(enabled) {
		if(!enabled && this.dropdown != null)
			this.dropdown.hide();

		this.callParent(enabled);
	},

	setReadOnly: function(readOnly) {
		if(!readOnly && this.dropdown != null)
			this.dropdown.hide();

		this.callParent(readOnly);
	},

	formatValue: function(value) {
		var field = this.field || {};

		switch(field.type) {
		case Type.Date:
		case Type.Datetime:
			value = String.isString(value) ? Parser.datetime(value) : value;
			return value != null ? Format.date(value, field.format) : '';
		case Type.Boolean:
			return Parser.boolean(value) ? 'да' : 'нет';
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

		this.displayValue = displayValue = this.formatValue(displayValue || '');

		this.updateDependenciesByValue(value);

		this.clearFilter();
		this.callParent(value, displayValue);
	},

	isEqual: function(v1, v2) {
		return (Z8.isEmpty(v1) || v1 == guid.Null) && (Z8.isEmpty(v2) || v2 == guid.Null) || this.callParent(v1, v2);
	},

	updateDependenciesByValue: function(value, force) {
		var store = this.getStore();
		if(store == null || this.dependencies == null)
			return;

		var record = null;
		if(value != null) {
			var record = Z8.create(store.getModel());
			record.setId(value);
		}

		this.updateDependencies(record);
	},

	onDependencyChange: function(record) {
		var value = this.dependsOnValue = record != null ? (this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id) : null;
		this.updateWhere(value);
		this.setValue(guid.Null);
	},

	updateWhere: function(value) {
		var where = this.getWhere(value);

		var store = this.getStore();
		store.setWhere(where);
		store.unload();
	},

	getWhere: function(dependencyValue) {
		var where = [];

		if(dependencyValue != null)
			where.push({ property: this.getDependencyField(), value: dependencyValue });

		var record = this.getRecord();
		var link = this.field.link;

		if(record == null || !link.isParentKey)
			return where;

		var parentKeys = link.parentKeys;
		for(var i = 0, length = parentKeys.length; i < length; i++)
			where.push({ property: parentKeys[i], operator: Operation.NotEq, value: record.id });

		return where;
	},

	getDisplayValue: function() {
		return this.displayValue;
	},

	valueToRaw: function(value) {
		return this.displayValue;
	},

	currentItem: function() {
		return this.dropdown.getItem(this.getValue());
	},

	currentIndex: function() {
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
				if(success) {
					var item = this.findItem(text);
					this.dropdown.selectItem(item);
					this.showDropdown(true);
				}
			};

			var filter = !Z8.isEmpty(text) ? [{ property: this.displayName, operator: Operation.Contains, value: text, anyMatch: true }] : [];
			this.load({ fn: callback, scope: this }, filter);
		};

		this.queryTask.delay(delay, query, this, text);
	},

	clearFilter: function() {
		if(this.lastQuery == '')
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

		while(index < count) {
			var item = items[index];
			var itemText = (item.getText(this.displayName) || '').toLowerCase();
			if(itemText.startsWith(text.toLowerCase()))
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
				item = this.currentItem();
			} else
				item = item || this.currentItem();

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

			var index = this.currentIndex();
			var item = this.findItem(key, index + 1);

			item = this.findItem(key, index + 1);

			if(item != null)
				this.setValue(item.getValue(), item.getText(this.displayName));
		}

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

	show: function() {
		this.entered = false;
		this.callParent();
	},

	openDropdown: function() {
		var callback = function(store, records, success) {
			if(success)
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
		DOM.setRight(pager, DOM.getRight(dropdown));
		DOM.setTop(pager, DOM.getTop(dropdown) + rect.height);
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

		var left = DOM.getOffsetLeft(this.input);
		DOM.setLeft(dropdown, left);
		DOM.setRight(dropdown, -0.08333333 /* Ems.pixelsToEms(1) */);

		var item = this.currentItem();
		var focusAt = keepFocus ? false : item;

		var pagerSize = this.getPagerSize();

		dropdown.setAlignmentOffset(0, pagerSize.height, 1.41666667 /* Ems.pixelsToEms(17) */);
		dropdown.show(null, null, focusAt);
	},

	hideDropdown: function() {
		this.dropdown.hide();
		this.clearFilter();
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
		if(!this.callParent(event))
			return false;

		this.cancelDropdown();
		return true;
	},

	onTriggerClick: function(trigger) {
		this.toggleDropdown();
	},

	onDblClick: function(event, target) {
		if(this.isEnabled() && !this.isReadOnly())
			this.toggleDropdown();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		var editable = this.editable;
		var dropdown = this.dropdown;
		var dropdownOpen = dropdown.isVisible();
		var currentItem = this.dropdown.currentItem();

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
		else if(key == Event.HOME && (!editable || !isInput()))
			dropdownOpen ? dropdown.focus(currentItem) : this.select('first');
		else if(key == Event.END && (!editable || !isInput()))
			dropdownOpen ? dropdown.focus(currentItem) : this.select('last');
		else if(key == Event.TAB && isTrigger()) {
			dropdownOpen ? dropdown.focus(currentItem) : this.cancelDropdown();
			return false;
		} else if(key == Event.ENTER || key == Event.SPACE && (!this.editable || isTrigger())) {
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
		Viewport.open(this.source.id);
		this.getStore().unload();
	},

	clearValue: function() {
		this.setValue(this.emptyValue, '');
	}
});