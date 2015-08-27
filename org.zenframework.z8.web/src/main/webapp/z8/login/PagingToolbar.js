Z8.PagingToolbar = Ext.extend(Z8.Toolbar, 
{
	pageSize: Z8.defaultPageCount,
	
	displayInfo: true, 
	displayMsg: 'записи {0} - {1} из {2}',
	oneRecordDisplayMsg: 'запись {0} из {1}',

	emptyMsg: "0 записей",	

	beforePageText: 'страница',
	beforeRecordText: 'запись',
	afterText : 'из {0}',

	firstText : 'Первая страница',
	previousText : 'Предыдущая страница',
	nextText : 'Следующая страница',
	lastText : 'Последняя страница',
	
	rightWidth: 150,

	constructor: function(config)
	{
		config.cls = 'x-small-editor z8-statusbar';
		
		this.first = new Z8.Button({tooltip: this.firstText, overflowText: this.firstText, iconCls: 'icon-first-page', disabled: true, handler: this.moveFirst, scope: this });	
		this.previous = new Z8.Button({tooltip: this.previousText, overflowText: this.previousText, iconCls: 'icon-prev-page', disabled: true, handler: this.movePrevious, scope: this });

		this.before = new Z8.Toolbar.TextItem({ text: this.beforePageText, cls: 'before-page-text' });

		this.inputItem = new Ext.form.NumberField({ cls: 'x-tbar-page-number', margins: { top: 3, right: 0, left: 0, bottom: 3 }, grow: true, allowDecimals: false, allowNegative: false, enableKeyEvents: true, selectOnFocus: true, submitValue: false });
		this.inputItem.on('keydown', this.onPagingKeyDown, this);
		this.inputItem.on('blur', this.onPagingBlur, this);
		
		this.after = new Z8.Toolbar.TextItem({ text: String.format(this.afterText, 1), cls: 'after-page-text' });
	
		this.next = new Z8.Button({ tooltip: this.nextText, overflowText: this.nextText, iconCls: 'icon-next-page', disabled: true, handler: this.moveNext, scope: this });
		this.last = new Z8.Button({ tooltip: this.lastText, overflowText: this.lastText, iconCls: 'icon-last-page', disabled: true, handler: this.moveLast, scope: this });

		this.displayItem = new Z8.Toolbar.TextItem({ align: 'right' });

		config.items = [this.first, this.previous, this.before, this.inputItem, this.after, this.next, this.last, this.displayItem];

		Z8.PagingToolbar.superclass.constructor.call(this, config);
	},
	
	initComponent: function()
	{
		Z8.PagingToolbar.superclass.initComponent.call(this);

		this.addEvents('change', 'beforechange');

		this.cursor = 0;

        this.on('afterlayout', this.onFirstLayout, this, {single: true});
		this.bindStore(this.store, true);
	},

	setPageMode: function()
	{
		this.oneRecordMode = false;
		this.before.setText(this.beforePageText);
		this.updateControls();
	},

	setOneRecordMode: function(record)
	{
		this.oneRecordMode = true;
		this.record = this.store.indexOf(record);
		
		this.before.setText(this.beforeRecordText);
		
		this.updateControls();
	},
	
	onFirstLayout: function()
	{
		if(this.dsLoaded)
		{
			this.onLoad.apply(this, this.dsLoaded);
		}
	},

	onAfterLayout: function()
	{
		this.updateControls();
	},

	updateInfo: function()
	{
		if(this.displayItem)
		{
			var count = this.store.getCount();

			var msg = this.emptyMsg;

			if(count != 0)
			{
				var data = this.getPageData();

				if(this.oneRecordMode)
				{
					msg = String.format(this.oneRecordDisplayMsg, data.record, data.records);
				}
				else
				{
					msg = String.format(this.displayMsg, data.start + 1, data.start + count, data.records);
				}
			}

			this.displayItem.setText(msg);
		}
	},

	onLoad: function(store, r, o)
	{
		if(!this.rendered)
		{
			this.dsLoaded = [store, r, o];
			return;
		}
	
		this.updateControls();
	
		this.fireEvent('change', this, this.getPageData());
	},

	updateControls: function()
	{
		var data = this.getPageData();
		
		this.before.setText(this.oneRecordMode ? this.beforeRecordText : this.beforePageText);
		this.after.setText(String.format(this.afterText, this.oneRecordMode ? data.records : data.pages));

		this.inputItem.setValue(''); // в chrome почему-то нужно так
		this.inputItem.setValue(this.oneRecordMode ? (data.records == 0 ? '' : data.record) : data.page);
		
		this.first.setDisabled(this.oneRecordMode ? data.record == 1 : data.page == 1);
		this.previous.setDisabled(this.oneRecordMode ? data.record == 1 : data.page == 1);
		this.next.setDisabled(this.oneRecordMode ? data.record >= data.records : data.page == data.pages);
		this.last.setDisabled(this.oneRecordMode ? data.record >= data.records : data.page == data.pages);

		this.updateInfo();
		
		if (this.oneRecordMode) 
		{
			this.first.setTooltip('Первая запись');
			this.previous.setTooltip('Предыдущая запись');
			this.next.setTooltip('Следующая запись');
			this.last.setTooltip('Последняя запись');
		}
	},
	
	getPageData: function()
	{
		return this.store.getPageData();
	},

	goToRecord: function(record)
	{
		this.store.goToRecord(record);
	},

	goToPage: function(page)
	{
		this.store.goToPage(page);
	},

	readNumber: function(data)
	{
		var value = this.inputItem.getValue();
		var number;
		
		if(!value || isNaN(number = parseInt(value, 10)))
		{
			this.inputItem.setValue(this.oneRecordMode ? data.record : data.page);
			return false;
		}
		
		return number;
	},

	onPagingFocus: function()
	{
		this.inputItem.select();
	},

	onPagingBlur: function(e)
	{
		var data = this.getPageData();
		this.inputItem.setValue(this.oneRecordMode ? data.record : data.page);
	},

	onPagingKeyDown: function(field, e)
	{
		var k = e.getKey();
		var data = this.getPageData();
		
		if(k == e.RETURN)
		{
			e.stopEvent();
			var number = this.readNumber(data);
			
			if(number !== false)
			{
				if(this.oneRecordMode)
				{
					number = Math.min(Math.max(1, number), data.records);
					this.goToRecord(number);
				}
				else
				{
					number = Math.min(Math.max(1, number), data.pages);
					this.goToPage(number);
				}
			}
		}
		else if(k == e.HOME || k == e.END)
		{
			e.stopEvent();
			number = k == e.HOME ? 1 : (this.oneRecordMode ? data.records : data.pages);
			field.setValue(number);
		}
		else if(k == e.UP || k == e.PAGEUP || k == e.DOWN || k == e.PAGEDOWN)
		{
			e.stopEvent();
			
			if((number = this.readNumber(data)))
			{
				var increment = e.shiftKey ? 10 : 1;

				if(k == e.DOWN || k == e.PAGEDOWN)
				{
					increment *= -1;
				}

				number += increment;

				if(number >= 1 & number <= (this.oneRecordMode ? data.records : data.pages))
				{
					field.setValue(number);
				}
			}
		}
	},

	moveFirst: function()
	{
		if(this.oneRecordMode)
		{
			this.store.firstRecord();
		}
		else
		{
			this.store.firstPage();
		}
	},

	movePrevious: function()
	{
		if(this.oneRecordMode)
		{
			this.store.previousRecord();
		}
		else
		{
			this.store.previousPage();
		}
	},

	moveNext: function()
	{
		if(this.oneRecordMode)
		{
			this.store.nextRecord();
		}
		else
		{
			this.store.nextPage();
		}
	},

	moveLast: function()
	{
		if(this.oneRecordMode)
		{
			this.store.lastRecord();
		}
		else
		{
			this.store.lastPage();
		}
	},

	onCurrentChange: function(store, recordIndex)
	{
		this.updateControls();
	},
	
	bindStore: function(store, initial)
	{
		var doLoad;
		
		if(!initial && this.store)
		{
			if(store !== this.store && this.store.autoDestroy)
			{
				this.store.destroy();
			}
			else
			{
				this.store.un('load', this.onLoad, this);
				this.store.un('currentChange', this.onCurrentChange, this);
			}

			if(!store)
			{
				this.store = null;
			}
		}

		if(store)
		{
			store = Ext.StoreMgr.lookup(store);
			store.on('load', this.onLoad, this);
			store.on('currentChange', this.onCurrentChange, this);
			doLoad = true;
		}

		this.store = store;
		
		if(doLoad)
		{
			this.onLoad(store, null, {});
		}
	},

	unbind: function(store)
	{
		this.bindStore(null);
	},

	bind: function(store)
	{
		this.bindStore(store);
	},

	onDestroy: function()
	{
		this.unbind();
		Z8.PagingToolbar.superclass.onDestroy.call(this);
	}
});	
