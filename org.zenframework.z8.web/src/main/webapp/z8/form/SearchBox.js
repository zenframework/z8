Z8.form.SearchBox = Ext.extend(Ext.form.TriggerField,
{
	isTriggerField: true,

	autoAddRecords: false,

	defaultAutoCreate: { tag: "input", type: "text", size: "24", autocomplete: "off" },

	listClass: '',
	listEmptyText: '',
	selectedClass: 'x-combo-selected',
	triggerClass: 'x-form-arrow-trigger',
	shadow: 'sides',
	listAlign: 'tl-bl?',

	defaultWidth: 370,
	defaultColumnWidth: 145,
	defaultListHeight: 300,

	triggerAction: 'all', // 'query'

	minChars: 0,

	typeAhead: true,
	typeAheadDelay: 250,
	queryDelay: 500,

	selectOnFocus: false,

	fieldId: null,
	/**
	 * @cfg {String} queryParam Name of the query ({@link Ext.data.Store#baseParam baseParam} name for the store)
	 * as it will be passed on the querystring (defaults to <tt>'query'</tt>)
	 */
	queryParam: Z8.lookupProperty,
	/**
	 * @cfg {String} allQuery The text query to send to the server to return all records for the list
	 * with no filtering (defaults to '')
	 */
	allQuery: '',

	loadingText: 'Loading...',

	mode: 'remote',

	forceSelection: true,

	clearFilterOnReset: true,

	submitValue: false,

	initComponent: function()
	{
		Z8.form.SearchBox.superclass.initComponent.call(this);
	
		this.addEvents(
			'expand',			// Fires when the dropdown list is expanded, param {Ext.form.ComboBox} combo This combo box
			'collapse',			// Fires when the dropdown list is collapsed, param {Ext.form.ComboBox} combo This combo box
			'beforeselect',		// Fires before a list item is selected. Return false to cancel the selection.
									// param {Ext.form.ComboBox} combo This combo box
									// param {Ext.data.Record} record The data record returned from the underlying store
			'select',			// Fires when a list item is selected
									// param {Ext.form.ComboBox} combo This combo box
									// param {Ext.data.Record} record The data record returned from the underlying store
			'beforequery'		// Fires before all queries are processed. Return false to cancel the query or set the queryEvent's cancel property to true.
									// param {Object} queryEvent An object that has these properties:
										// combo : Ext.form.ComboBox This combo box
										// query : String The query
										// forceAll : Boolean True to force "all" query
										// cancel : Boolean Set to true to cancel the query
		);
		
		this.selectOnFocus = false;
	},

	reset: function()
	{
		this.lastQuery = null;
	},

	onException: function()
	{
		this.reset();
	},
	
	onRender: function(ct, position)
	{
		this.submitValue = false;

		Z8.form.SearchBox.superclass.onRender.call(this, ct, position);
		
		if(Ext.isGecko)
		{
			this.el.dom.setAttribute('autocomplete', 'off');
		}

		if(this.store != null)
		{
			this.initDropDown();
		}
	},

	getParentZIndex: function()
	{
		var zindex;
		
		if(this.ownerCt)
		{
			this.findParentBy(function(ct)
			{
				zindex = parseInt(ct.getPositionEl().getStyle('z-index'), 10);
				return !!zindex;
			});
		}
		return zindex;
	},
	
	getZIndex: function(listParent)
	{
		var zindex = this.getParentZIndex();

		if(zindex == null)
		{
    		listParent = listParent || Ext.getDom(this.getListParent() || Ext.getBody());
	    	zindex = parseInt(Ext.fly(listParent).getStyle('z-index'), 10);
		}
		
		return (zindex || 12000) + 5;
	},

	initDropDown: function()
	{
		if(!this.list)
		{
			var cls = 'x-combo-list';
			var listParent = Ext.getDom(this.getListParent() || Ext.getBody());

			this.list = new Ext.Layer({
				parentEl: listParent,
				shadow: this.shadow,
				cls: [cls, this.listClass].join(' '),
				constrain:false,
				zindex: this.getZIndex()
			});

			this.list.swallowEvent('mousewheel');
			
			if(this.syncFont !== false)
			{
				this.list.setStyle('font-size', this.el.getStyle('font-size'));
			}
			
			this.innerList = this.list.createChild({cls:cls+'-inner'});
			this.innerList.setWidth(this.calculateGridWidth());

			this.footer = this.list.createChild({cls:cls+'-ft'});

			var config =
			{
				editable: false,
				multiselect: false,
				showStatus: false,
				renderTo: this.innerList,
				width: this.calculateGridWidth(),
				height: this.defaultListHeight
			};
			
			this.view = createGrid(this.query, config); 

			this.mon(this.view, 'keydown', this.onViewKeyDown, this);
			
			if(this.view instanceof Z8.tree.TreePanel)
			{
				this.mon(this.view.getSelectionModel(), 'selectionchange', this.onTreeNodeClick, this);
			}
			else
			{
				this.mon(this.view, 'rowclick', this.onViewRowClick, this);
			}

			this.resizer = new Ext.Resizable(this.list, { pinned:false, handles:'w e s sw se' });
			this.mon(this.resizer, 'resize', this.resize, this);
		}
	},

	calculateGridWidth: function()
	{
		var columns = this.query.getColumns();
		
		var width = 0;
		for(var i = 0; i < columns.length; i++)
		{
			var column = columns[i];
			
			if(!column.hidden && column.visible)
			{
				width += Z8.Grid.fieldWidth(column.columnWidth || column.width, column);
			}
		}
		
		return Math.max(width + 20, this.defaultWidth);
	},
	
	resize: function(resizer, width, height)
	{
		this.list.setSize(width, height);

		width -= this.list.getFrameWidth('lr');
		height -= this.list.getFrameWidth('tb') + this.footer.getHeight();
	
		this.innerList.setWidth(width);
		this.view.setWidth(width);

		this.innerList.setHeight(height);
		this.view.setHeight(height);
	},

	getListParent: function() 
	{
		return document.body;
	},

	getStore: function()
	{
		return this.store;
	},

	bindStore: function()
	{
		if(this.store)
		{
			this.store.on('beforeload', this.onBeforeLoad, this);
			this.store.on('load', this.onLoad, this);
			this.store.on('exception', this.onException, this);
		}
	},

	unbindStore: function()
	{
		if(this.store)
		{
			this.store.un('beforeload', this.onBeforeLoad, this);
			this.store.un('load', this.onLoad, this);
			this.store.un('exception', this.onException, this);
		}

		this.store = null;
	},

	onViewKeyDown: function(e)
	{
		var key = e.getKey();

		if(this.isExpanded())
		{
			if(key === e.UP)
			{
				if(!this.view.hasPreviousToSelect())
				{
					this.el.dom.focus();
					this.el.dom.select();
				}
			}
			if(key === e.DOWN)
			{
				if(!this.view.hasNextToSelect())
				{
					this.el.dom.focus();
					this.el.dom.select();
				}
			}
			else if(key === e.ENTER)
			{
				this.onRowSelected();
				Z8.stopEvent(e);
			}
			else if(key === e.ESC)
			{
				this.collapseDropDown();
			}
		}
	},

	initEvents: function()
	{
		Z8.form.SearchBox.superclass.initEvents.call(this);

		this.keyNav = new Ext.KeyNav(this.el,
		{
			"down": function(e)
			{
				if(this.isExpanded())
				{
					this.view.focus();
					
					if(this.view.getSelectedRecord() != null)
					{
						this.view.selectNext();
					}
					else
					{
						this.view.selectFirst();
					}
				}
				else
				{
					this.onTriggerExpand(this.getRawValue(), true);
				}
			},

			"up": function(e)
			{
				if(this.isExpanded())
				{
					this.view.focus();
					if(this.view.getSelectedRecord() != null)
					{
						this.view.selectPrevious();
					}
					else
					{
						this.view.selectFirst();
					}
				}
				else
				{
					this.onTriggerExpand(this.getRawValue(), true);
				}
			},
			
			"enter": function(e)
			{
				this.onRowSelected();
				this.collapseDropDown();
				Z8.stopEvent(e);
			},

			"esc": function(e)
			{
				this.collapseDropDown();
			},

			"tab": function(e)
			{
				if(this.forceSelection === true)
				{
					this.collapseDropDown();
				}
				else
				{
					this.onRowSelected();
				}
				return true;
			},

			scope: this,

			doRelay: function(e, h, hname)
			{
				if(hname == 'down' || hname == 'up' || this.scope.isExpanded())
				{
					// this MUST be called before ComboBox#fireKey()
					var relay = Ext.KeyNav.prototype.doRelay.apply(this, arguments);

					if(!Ext.isIE && Ext.EventManager.useKeydown)
					{
						// call Combo#fireKey() for browsers which use keydown event (except IE)
						this.scope.fireKey(e);
					}
					return relay;
				}
				return true;
			},

			forceKeyDown: true,
			defaultEventAction: 'stopEvent'
		});

		this.dqTask = new Ext.util.DelayedTask(this.initQuery, this);
	
		if(this.typeAhead)
		{
			this.taTask = new Ext.util.DelayedTask(this.onTypeAhead, this);
		}
	
		if(!this.enableKeyEvents)
		{
			this.mon(this.el, 'keyup', this.onKeyUp, this);
		}
		
		var me = this;
		this.el.dom.onpaste = function() { me.onPaste(); };
	},

	onDestroy: function()
	{
		if(this.el != null && this.el.dom != null)
		{
			this.el.dom.onpaste = null;
		}
		
		this.destroyDropDown();
		Z8.form.SearchBox.superclass.onDestroy.call(this);
	},
	
	destroyDropDown: function()
	{
		if (this.dqTask)
		{
			this.dqTask.cancel();
		}

		this.unbindStore();
		
		Ext.destroy(
			this.resizer,
			this.view,
			this.list
		);
		
		this.resizer = this.view = this.list = null;
	},
	
	fireKey: function(e)
	{
		if (!this.isExpanded())
		{
			Z8.form.SearchBox.superclass.fireKey.call(this, e);
		}
	},

	onBeforeLoad: function()
	{
//		this.innerList.update(this.loadingText ? '<div class="loading-indicator">'+this.loadingText+'</div>' : '');
	},

	selectAll: function()
	{
		if(this.editable)
		{
			this.el.dom.select();
		}
	},
	
	onLoad: function(store, records, options)
	{
		var rawValue = this.getRawValue();
		var lookup = options != null ? options.params.lookup : '';
		
		if(lookup != null && lookup != rawValue)
		{
			return;
		}
		
		var expand = options != null && options.params.expand;

		var startTypeAhead = false;
		
		if(this.valueId != null)
		{
			if(this.selectByValue())
			{
				if(Z8.isEmpty(rawValue))
				{
					this.onSelect(this.view.getSelectedRecord(), false);
					this.selectAll();
				}
			}
			else
			{
				if(Z8.isEmpty(this.valueId))
				{
					this.view.clearSelections();
					this.onTypeAhead(expand);
					startTypeAhead = true;
				}
			}
		}
		else
		{
			this.onTypeAhead(expand);
			startTypeAhead = true;
		}

		if(expand && !startTypeAhead)
		{
			this.expandDropDown();
		}
	},

	onTypeAhead: function(forceExpand)
	{
		var currentValue = this.getRawValue();
		
		if(this.store.getCount() > 0)
		{
			if(Z8.isEmpty(currentValue))
			{
				this.reset();
				this.onSelect(this.createEmptyRecord(), false, true);
			}
			else
			{
				var record = this.store.getAt(0);
				var newValue = record.get(this.fieldId);

				var len = newValue.length;
				var selStart = currentValue.length;
			
				if(this.autoAddRecords)
				{
					if(selStart <= len && newValue.substring(0, selStart).toLowerCase() == currentValue.toLowerCase())
					{
						this.doSelectRecord(record, newValue, currentValue);
					}
					else
					{
						this.view.clearSelections();
						this.set(null, currentValue);
						this.onSelect(null, false, true);
					}
				}
				else
				{
					this.doSelectRecord(record, newValue, currentValue);
				}
			}
		}
		
		if(this.store.getCount() == 0 && this.autoAddRecords)
		{
			this.view.clearSelections();
			this.set(null, currentValue);
			this.onSelect(null, false, true);
			this.collapseDropDown();
		}
		else if(forceExpand)
		{
			this.expandDropDown();
		}
	},

	createEmptyRecord: function()
	{
		var record = new Ext.data.Record();
		
		var fields = this.query != null ? this.query.getFields() : [];
		
		for(var i = 0; i < fields.length; i++)
		{
			record.data[fields[i].id] = '';
		}
		
		return record;
	},
	
	doSelectRecord: function(record, newValue, currentValue)
	{
		var length = newValue.length;
		var selStart = currentValue.length;

		if(selStart <= length)
		{
			this.view.selectFirst();

			this.setRawValue(newValue);
			this.selectText(selStart, length);
		
			this.set(record);
			
			this.expandDropDown();
		}
	},
	
	assertValue: function()
	{
		this.setRawValue(this.value);
	},

	onSelect: function(record, collapse, skipSetRecord)
	{
		if(record != null && this.query != null && !this.query.parentsSelectable && record.data[this.store.childrenProperty] > 0)
		{
			return;
		}
		
		if(this.store != null && this.fireEvent('beforeselect', this, record) !== false)
		{
			if(!skipSetRecord)
			{
				this.setRecord(record);
			}
			
			if(collapse)
			{
				this.collapseDropDown();
			}
			
			this.fireEvent('select', this, record);
		}
	},

	getValue: function()
	{
		if(this.queryId != null)
		{
			return {
				queryId: this.queryId, 
				fieldId: this.fieldId,
				recordId: this.valueId,
				value: this.value,
				toString: function() { return this.recordId; }
			};
		}
		
		return Ext.isDefined(this.value) ? this.value : '';
	},

	setValue: function(value)
	{
		if(Ext.isObject(value) && value.queryId != null)
		{
			this.destroyDropDown();
			
			this.reset();
			this.setRawValue('');
			
			this.queryId = value.queryId;
			this.fieldId = value.fieldId;
			this.valueId = value.recordId;
			
			this.onTriggerExpand(null, false);
		}
		else
		{
			Z8.form.SearchBox.superclass.setValue.call(this, this.formatValue(value));
		}

		this.value = value;
		
		return this;
			
	},

	setRawValue: function(value)
	{
		Z8.form.SearchBox.superclass.setRawValue.call(this, this.formatValue(value));
	},
	
	clearValue: function()
	{
		this.setRawValue('');
		this.applyEmptyText();
		this.value = '';
	},

	setHostRecord: function(record, editor)
	{
		if(this.linkId != null)
		{
			this.valueId = record != null ? record.get(this.linkId) : '';
			
			this.hostRecord = record;
			this.hostEditor = editor;
			
			if(record != null)
			{
				var field = record.store.fields.get(this.fieldId);
				var filters = field.filter;
				
				if(filters != null)
				{
					this.filters = [];
					
					for(var i = 0; i < filters.length; i++)
					{
						var filter = filters[i];
						this.filters.push({ id: filter.value, value: record.get(filter.value), fields: filter.fields });
					}
				}
			}
		}
	},

	updateFilter: function(field, record)
	{
		if(this.filters != null)
		{
			for(var i = 0; i < this.filters.length; i++)
			{
				var filter = this.filters[i];
				
				if(filter.id == field.linkedVia)
				{
					var value = record != null ? record.get(filter.id) : '';
				
					if(value != null && filter.value != value)
					{
						filter.value = value;
						this.setRecord(null);
						this.onSelect(this.createEmptyRecord(), false, false);
					}
				}
			}
		}
	},
	
	setRecord: function(record, depth)
	{
		this.set(record, null, depth);
		this.setValue(this.value);
	},

	set: function(record, value, depth)
	{
		if(record == null && depth != null && depth < this.depth)
		{
			return;
		}

		if(value == null)
		{
			value = Z8.isEmpty(this.valueId) ? this.getRawValue() : '';
		}
		
		this.valueId = record != null && record.id.indexOf('record') == -1 ? record.id : '';
		this.value = record != null ? record.get(this.fieldId) : value;
		this.setLinks(record);
	},

	setLinks: function(record)
	{
		this.links = {};
			
		if(record != null && this.hostRecord != null)
		{
			var store = this.hostRecord.store;
			var ids = store.fields.keys;
		
			for(var i = 0; i < ids.length; i++)
			{
				var id = ids[i];
			
				var field = store.fields.get(id);
			
				if(field.link != null)
				{
					var value = record.data[field.id];
				
					if(value != null)
					{
						this.links[id] = value;
						this.links[field.linkedVia] = value;
					}
				}
			}
		}
	},
	
	reset: function()
	{
		this.set(null);
	},

	formatValue: function(date)
	{
		return Ext.isDate(date) ? date.dateFormat(Z8.Format.Date) : date;
	},
	
	findRecord: function(value)
	{
		var record;
	
		if(this.store.getCount() > 0)
		{
			this.store.each(function(r)
			{
				if(r.data[this.fieldId] == value)
				{
					record = r;
					return false;
				}
			});
		}
		return record;
	},

	onTreeNodeClick: function(model, node, last, byClick)
	{
		var record = this.view.getSelectedRecord();
		
		if(record)
		{
			this.onSelect(record, byClick);
		}
	},

	onViewRowClick: function(view, index, event)
	{
		this.onRowSelected();
	},

	onRowSelected: function()
	{
		var isExpanded = this.view != null && this.isExpanded();
		
		var record = isExpanded ? this.view.getSelectedRecord() : null;

		if(record || (Z8.isEmpty(this.valueId) && !this.autoAddRecords))
		{
			if(record == null)
			{
				this.setRawValue('');
				this.setValue('');
			}
			
			this.onSelect(record, true);
		}
		else
		{
			this.collapseDropDown();
		}
	},

	isExpanded: function()
	{
		return this.list && this.list.isVisible();
	},

	selectByValue: function()
	{
		var id = this.valueId;
		
		if(id != null)
		{
			var record = this.store.getById(id);

			if(record != null)
			{
				this.view.selectRecord(record);
				return true;
			}
		}
		
		return false;
	},

	onPaste: function()
	{
		var rawValue = this.getRawValue();
		this.set(null, rawValue);
		
		var isEmpty = Z8.isEmpty(rawValue);
		
		if(isEmpty)
		{
			this.onSelect(this.createEmptyRecord(), false, false);
		}

		this.dqTask.delay(this.queryDelay, this.initQuery, this, [rawValue, !isEmpty]);
	},
	
	onKeyUp: function(e)
	{
		var k = e.getKey();
		
		if(this.editable !== false && this.readOnly !== true && e.ctrlKey === false && e.altKey === false && (k == e.BACKSPACE || k == e.DELETE || !e.isSpecialKey()))
		{
			this.onPaste();
		}
		
		Z8.form.SearchBox.superclass.onKeyUp.call(this, e);
	},

	validateBlur: function()
	{
		return !this.list || !this.list.isVisible();
	},

	initQuery: function(rawValue, forceExpand)
	{
		this.onTriggerExpand(this.getRawValue(), forceExpand);
	},

	beforeBlur: function()
	{
		this.assertValue();
	},

	postBlur: function()
	{
		Z8.form.SearchBox.superclass.postBlur.call(this);
		this.collapseDropDown();
	},

	doLookup: function(lookup, forceExpand)
	{
		if(this.parentQuery != null && this.query.recordId != this.parentQuery.recordId)
		{
			this.query.recordId = this.parentQuery.recordId;
		}
		
		var baseParams = this.store.baseParams || {};
		baseParams.filter = this.getFilters();
		this.store.baseParams = baseParams;
		
		this.store.load({ params: this.getParams(lookup, forceExpand) });
	},

	getParams: function(lookup, forceExpand)
	{
		var params = {};
		
		params[Z8.lookupProperty] = lookup;
		params[Z8.lookupIdProperty] = this.fieldId;

		params[Z8.startProperty] = 0;
		params[Z8.limitProperty] = Z8.defaultPageCount;

		if(this.hostEditor != null)
		{
			this.hostEditor.save();
		}
		
		params.record = this.hostRecord != null ? this.hostRecord.encode() : null;
		
		params.expand = forceExpand || !Z8.isEmpty(lookup);
		params.filter = this.getFilters();

		return params;
	},

	getFilters: function()
	{
		var filters = [];

		if(this.filters != null)
		{
			for(var i = 0; i < this.filters.length; i++)
			{
				var filter = this.filters[i];
				filters.push({ field: filter.fields, value: filter.value });
			}
		}
		
		return Ext.encode(filters);
	},
	
	collapseDropDown: function(skipSelect)
	{
		if(!this.isExpanded())
		{
			return;
		}

		this.list.hide();
		Ext.getDoc().un('mousewheel', this.collapseIf, this);
		Ext.getDoc().un('mousedown', this.collapseIf, this);
		this.fireEvent('collapse', this);
		this.el.focus();
		
		if(!skipSelect)
		{
			this.el.dom.select();
		}
	},

	within: function(e)
	{
		if(e.within(this.wrap) || e.within(this.list))
		{
			return true;
		}
		
		if(this.view != null)
		{
			return this.view.hmenu != null && e.within(this.view.hmenu.getEl()) || 
				this.view.colMenu != null && e.within(this.view.colMenu.getEl());
		}
		
		return false;
	},
	
	collapseIf: function(e)
	{
		if(!this.isDestroyed && !this.within(e))
		{
			this.collapseDropDown();
		}
	},

	expandDropDown: function()
	{
		if(this.isExpanded())
		{
			return;
		}

		this.list.alignTo.apply(this.list, [this.el].concat(this.listAlign));

		this.list.setZIndex(this.getZIndex());
		this.list.show();

		if(Ext.isGecko2)
		{
			this.innerList.setOverflow('auto'); // necessary for FF 2.0/Mac
		}
		this.mon(Ext.getDoc(), {
			scope: this,
			mousewheel: this.collapseIf,
			mousedown: this.collapseIf
		});
		this.fireEvent('expand', this);
	},

	onTriggerExpand: function(lookup, forceExpand)
	{
		if(this.readOnly || this.disabled || this.creatingStore)
		{
			return;
		}

		if(this.store == null)
		{
			this.creatingStore = true;
			
			if(this.parentQuery != null)
			{
				var onSuccess = this.createStore.createDelegate(this, [this.parentQuery.master, this.parentQuery.recordId, lookup, forceExpand], true);
				var onError = this.onError;
				var params = this.getParams(lookup, forceExpand);
				params.fieldId = this.fieldId;
			
				this.parentQuery.request(onSuccess, onError, params, this);
			}
			else
			{
				var onSuccess = this.createStore.createDelegate(this, [null, null, lookup, forceExpand], true);
				var onError = Ext.onError;
				var params = this.getParams(lookup, forceExpand);
				params.filterBy = this.valueId;
				
				Z8.Ajax.request(this.queryId, onSuccess, onError, params, this);
			}
		}
		else
		{
			this.doLookup(lookup, forceExpand);
		}
	}, 

	onError: function()
	{
		this.creatingStore = false;
	},
	
	createStore: function(query, parent, recordId, lookup, forceExpand)
	{
		this.creatingStore = false;
		
		this.query = query;
		this.query.parent = parent;
		
		if(!Ext.isEmpty(recordId))
		{
			this.query.recordId = recordId;
		}
		
		this.store = query.getStore();
		
		this.bindStore();
		
		this.initDropDown();
		
		this.onLoad(null, null, { params: { expand: forceExpand }});
	},
	
	onTriggerClick: function()
	{
		if(this.isExpanded())
		{
			this.onRowSelected();
		}
		else
		{
			this.el.focus();
			this.onTriggerExpand(null/*this.getRawValue()*/, true);
		}
	}
});

Ext.reg('z8.combo', Z8.form.SearchBox);
