Z8.define('Z8.pager.Pager', {
	extend: 'Z8.Container',

	initComponent: function() {
		this.callParent();

		var store = this.store;
		this.store = null;
		this.setStore(store);
	},

	setStore: function(store) {
		var currentStore = this.store;

		if(store == currentStore)
			return;

		if(currentStore != null) {
			currentStore.un('load', this.updateControls, this);
			currentStore.un('add', this.updateControls, this);
			currentStore.un('remove', this.updateControls, this);
			currentStore.un('beforeCount', this.onBeforeCount, this);
			currentStore.un('count', this.onCount, this);
			currentStore.dispose();
		}

		this.store = store;

		if(store != null) {
			store.use();
			store.on('load', this.updateControls, this);
			store.on('add', this.updateControls, this);
			store.on('remove', this.updateControls, this);
			store.on('count', this.updateControls, this);
		}

		this.updateControls();
	},

	htmlMarkup: function() {
		var first = this.first = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-double-left', cls: 'btn-tool', tooltip: 'To the First Page', handler: this.onFirst, scope: this });
		var previous = this.previous = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-left', cls: 'btn-tool', tooltip: 'To the Previous Page', handler: this.onPrevious, scope: this });

		var pageNumber = this.pageNumber = new Z8.form.field.Integer({ tabIndex: -1, inputCls: 'input-sm', tooltip: 'To the Page' });
		var pageTotals = this.pageTotals = new Z8.Component({ cls: 'totals', html: this.pageTotalsText() });

		var next = this.next = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-right', cls: 'btn-tool', tooltip: 'To the Next Page', handler: this.onNext, scope: this });
		var last = this.last = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-double-right', cls: 'btn-tool', tooltip: 'To the Last Page', handler: this.onLast, scope: this });

		var paging = this.paging = new Z8.Container({ cls: 'paging', items: [first, previous, pageNumber, pageTotals, next, last] });
		var totals = this.totals = new Z8.Component({ cls: 'totals', html: this.totalsText() });

		this.items = [paging, totals];

		this.cls = DOM.parseCls(this.cls).pushIf('pager');

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		this.updateControls();

		DOM.on(this.pageNumber, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this.pageNumber, 'keyDown', this.onKeyDown, this);
		this.setStore(null);
		this.callParent();
	},

	getPage: function() {
		return this.store != null ? this.store.getPage() : 0;
	},

	count: function() {
		return this.store != null ? this.store.getCount() : 0;
	},

	totalCount: function() {
		return this.store != null ? this.store.getTotalCount() : 0;
	},

	pageSize: function() {
		return this.store != null ? this.store.getLimit() : 0;
	},

	pageCount: function() {
		var total = this.totalCount();
		var pageSize = this.pageSize();
		return pageSize != 0 ? Math.ceil(total / pageSize) : 1;
	},

	pageTotalsText: function() {
		var count = this.pageCount();
		return count != 0 ? 'из ' + Format.integer(count) : '';
	},

	totalsText: function() {
		var total = this.totalCount();

		if(total == 0)
			return 'Нет записей';

		var page = this.getPage();
		var pageSize = this.pageSize();
		var first = page * pageSize + 1;
		var last = Math.min(first + this.count() - 1, total);
		return Format.integer(first) + ' - ' + Format.integer(last) + ' из ' + Format.integer(total);
	},

	load: function(page, button) {
		var callback = function(store, records, success) {
			button.setBusy(false);
			this.updateControls();
		};

		button.setBusy(true);
		this.store.loadPage(page, { fn: callback, scope: this });
	},

	onFirst: function() {
		this.load(0, this.first);
	},

	onPrevious: function() {
		this.load(Math.max(this.getPage() - 1, 0), this.previous);
	},

	onNext: function() {
		this.load(Math.min(this.getPage() + 1, this.pageCount() - 1), this.next)
	},

	onLast: function() {
		this.load(Math.min(this.pageCount() - 1), this.last)
	},

	onBeforeCount: function(store) {
	},

	onCount: function(store) {
		this.updateControls();
	},

	updateControls: function() {
		if(this.dom == null)
			return;

		var page = this.getPage();

		var pageCount = this.pageCount();

		if(pageCount > 1) {
			this.first.setEnabled(pageCount != 0 && page != 0);
			this.previous.setEnabled(pageCount != 0 && page != 0);
			this.next.setEnabled(pageCount != 0 && page != pageCount - 1);
			this.last.setEnabled(pageCount != 0 && page != pageCount - 1);

			//this.pageNumber.setEnabled(false);
			this.pageNumber.setValue(pageCount != 0 ? page + 1 : '');
			this.pageTotals.setText(this.pageTotalsText());

			this.paging.show();
		} else
			this.paging.hide();

		this.totals.setText(this.totalsText());
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER) {
			var pageNumber = this.pageNumber;
			var page = pageNumber.getValue();
			page = Math.max(Math.min(page, this.pageCount()), 1) - 1;
			this.load(page, page < this.getPage() ? this.previous : this.next);
			event.stopEvent();
		}
	}
});
