Z8.grid.RowEditor = Ext.extend(Ext.Panel,
{
	isRowEditor: true,

	autoHide: true,
	floating: true,
	shadow: false,
	layout: 'hbox',
	cls: 'x-small-editor',
	baseCls: 'x-row-editor',
	elements: 'body',
	monitorValid: true,
	focusDelay: 100,
	errorSummary: false,

	commitChangesText: 'You need to commit or cancel your changes',
	errorText: 'Errors',

	editors: [],
	
	initComponent: function()
	{
		Z8.grid.RowEditor.superclass.initComponent.call(this);
		this.addEvents(
			/**
			 * @event beforeedit
			 * Fired before the row editor is activated.
			 * If the listener returns <tt>false</tt> the editor will not be activated.
			 * @param {Z8.grid.RowEditor} roweditor This object
			 * @param {Number} rowIndex The rowIndex of the row just edited
			 */
			'beforeedit',
			/**
			 * @event canceledit
			 * Fired when the editor is cancelled.
			 * @param {Z8.grid.RowEditor} roweditor This object
			 * @param {Boolean} forced True if the cancel button is pressed, false is the editor was invalid.
			 */
			'canceledit',
			/**
			 * @event validateedit
			 * Fired after a row is edited and passes validation.
			 * If the listener returns <tt>false</tt> changes to the record will not be set.
			 * @param {Z8.grid.RowEditor} roweditor This object
			 * @param {Object} changes Object with changes made to the record.
			 * @param {Ext.data.Record} r The Record that was edited.
			 * @param {Number} rowIndex The rowIndex of the row just edited
			 */
			'validateedit',
			/**
			 * @event afteredit
			 * Fired after a row is edited and passes validation.  This event is fired
			 * after the store's update event is fired with this edit.
			 * @param {Z8.grid.RowEditor} roweditor This object
			 * @param {Object} changes Object with changes made to the record.
			 * @param {Ext.data.Record} r The Record that was edited.
			 * @param {Number} rowIndex The rowIndex of the row just edited
			 */
			'afteredit'
		);
	},

	init: function(grid)
	{
		this.grid = grid;
		this.ownerCt = grid;

		grid.on({
			scope: this,
			keydown: this.onGridKey,
			cellmousedown: this.onOpenEditor,
			columnresize: this.verifyLayout,
			resize: this.verifyLayout,
			columnmove: this.refreshFields,
			reconfigure: this.refreshFields,
			beforedestroy : this.beforedestroy,
			destroy : this.destroy
		});

		// stopEditing without saving when a record is removed from Store.
		grid.getStore().on('remove', this.onStoreRemove, this);
		grid.getStore().on('load', this.onStoreLoad, this);
		grid.getStore().on('update', this.onStoreUpdate, this);

		grid.getColumnModel().on('hiddenchange', this.verifyLayout, this, {delay:1});
		grid.getView().on('refresh', this.stopEditing, this);
	},
	
	beforedestroy: function()
	{
		this.stopMonitoring();

		this.grid.getStore().un('remove', this.onStoreRemove, this);
		this.grid.getStore().un('load', this.onStoreLoad, this);
		this.grid.getStore().un('update', this.onStoreUpdate, this);

		this.grid.getColumnModel().un('hiddenchange', this.verifyLayout, this);
		this.grid.getView().un('refresh', this.stopEditing, this);

		this.stopEditing(false);
	
		Ext.destroy(this.tooltip);
	},

	onDestroy: function()
	{
		this.unhookOuterClicks();
		
		Z8.grid.RowEditor.superclass.onDestroy.call(this);
	},
	
	onStoreRemove: function()
	{
		this.stopEditing(false);
	},
	
	onStoreLoad: function()
	{
		this.stopEditing(false);

		for(var i = 0; i < this.editors.length; i++)
		{
			var editor = this.editors[i];
			
			if(editor.reset != null)
			{
				editor.reset();
			}
		}
	},

	onStoreUpdate: function(store, record, operation)
	{
		if(this.editing && record == this.record)
		{
			this.updateFields(record);
		}
	},
	
	refreshFields: function()
	{
		this.verifyLayout();
	},

	isDirty: function()
	{
		return !Z8.isEmpty(this.getChanges());
	},

	verifyPosition: function()
	{
		if(this.isVisible())
		{
			var grid = this.grid;
			var view = grid.getView();
			var row = view.getRow(this.rowIndex);
	
			this.setPagePosition(Ext.fly(row).getXY());
		}
	},
	
	startEditing: function(record, doFocus)
	{
		if(this.editing && this.isDirty())
		{
			this.showTooltip(this.commitChangesText);
			return;
		}
		
		var store = record.store;
		
		if(Z8.user.supervisor !== true && store.isRecordLocked(record))
		{
			Z8.showMessages(store.query.text, 'Запись заблокирована.');
			return;
		}

		if(Z8.user.supervisor !== true && store.query.writeAccess === false)
		{
			return;
		}
		
		var rowIndex = this.grid.getRowIndex(record);

		if(this.fireEvent('beforeedit', this, rowIndex) !== false)
		{
			this.editing = true;
			
			var grid = this.grid;
			var view = grid.getView();
			var row = view.getRow(rowIndex);

			this.record = record;
			this.rowIndex = rowIndex;

			if(!this.rendered)
			{
				this.render(view.getEditorParent());
			}
			
			var w = Ext.fly(row).getWidth();
			
			this.setSize(w);
			
			this.initFields();
			
			this.updateFields(record);

			this.setPagePosition(Ext.fly(row).getXY());
			this.verifyLayout(true);

			this.show();

			if(doFocus !== false)
			{
				this.doFocus.defer(this.focusDelay, this);
			}
		}
	},
	
	updateFields: function(record)
	{
			var cm = this.grid.getColumnModel();
			var count = cm.getColumnCount();
			
			for(var i = 0; i < count; i++)
			{
				var column = cm.getColumnAt(i);
				var editor = column.editor;

				if(editor == null)
				{
					continue;
				}
				
				if(editor.reset != null)
				{
					editor.reset();
				}

				if(editor.setHostRecord != null)
				{
					editor.setHostRecord(record, this);
				}
				
				var value = this.preEditValue(record, cm.getDataIndex(i));
				editor.setValue(value);
			}
	},
	
	getEditorById: function(fieldId)
	{
		var fields = this.items.items;
		
		for(var i = 0; i < fields.length; i++)
		{
			if(fields[i].fieldId == fieldId)
			{
				return fields[i];
			}
		}
		
		return null;
	},
	
	onTriggerSelect: function(field, record)
	{
		var ids = field.store.fields.keys;
		
		var triggers = [];
		
		for(var i = 0; i < ids.length; i++)
		{
			var id = ids[i];
			var editor = this.getEditorById(id);

			if(editor != null && editor != field)
			{
				editor.setRecord(record, field.autoAddRecords ? field.depth : null);
				triggers.push(editor);
			}
		}

		for(var i = 0; i < this.editors.length; i++)
		{
			var editor = this.editors[i];
			
			if(editor != field && editor.updateFilter != null)
			{
				editor.updateFilter(field, record);
			}
		}
		
		this.evaluator.update(field, triggers);
	},

	getChanges: function()
	{
		var changes = {};
		var cm = this.grid.colModel;
		
		var changeFn = function(changes, newValue, record, dataIndex)
		{
			if(dataIndex != null)
			{
				var oldValue = record.get(dataIndex);

				if(oldValue == null && (newValue == null || newValue == ''))
				{
					return;
				}

				if(String(record.get(dataIndex)) !== String(newValue))
				{
					changes[dataIndex] = newValue;
				}
			}
		};

		for(var i = 0; i < this.editors.length; i++)
		{
			var editor = this.editors[i];
			
			if(editor == null || !editor.isValid(true))
			{
				continue;
			}
			
			if(editor.linkId != null && editor.valueId != null)
			{
				changeFn(changes, editor.valueId, this.record, editor.linkId);
			}

			if(editor.links != null)
			{
				for(var id in editor.links)
				{
					changeFn(changes, editor.links[id], this.record, id);
				}
			}

			var dataIndex = editor.dataIndex;

			if(!Ext.isEmpty(dataIndex))
			{
				changeFn(changes, this.postEditValue(editor.getValue(), this.record, dataIndex), this.record, dataIndex);
			}
		}
		
		return changes;
	},
	
	stopEditing : function(saveChanges)
	{
		this.editing = false;

		if(!this.isVisible())
		{
			return;
		}

		if(saveChanges === false/* || !this.isValid()*/)
		{
			this.hide();
			this.fireEvent('canceledit', this, saveChanges === false);
			return;
		}

		this.save();
		this.hide();
	},

	save: function()
	{
		var changes = this.getChanges();
		
		for(var i = 0; i < this.editors.length; i++)
		{
			var editor = this.editors[i];
			
			if(editor.isTriggerField)
			{
				editor.collapseDropDown();
			}
			
			if(editor.menu != null)
			{
				editor.menu.hide();
			}
		}

		if(!Z8.isEmpty(changes) && this.fireEvent('validateedit', this, changes, this.record, this.rowIndex) !== false)
		{
			this.record.beginEdit();

			var visitorFn = function(name, value)
			{
				this.record.set(name, value);

				if(!Ext.isDefined(this.record.modified[name]))
				{
					this.record.modified[name] = '';
				}
			};

			Ext.iterate(changes, visitorFn, this);

			this.record.endEdit();
			this.fireEvent('afteredit', this, changes, this.record, this.rowIndex);
		}
	},
	
	verifyLayout: function(force)
	{
		if(this.el && (this.isVisible() || force === true))
		{
			var row = this.grid.getView().getRow(this.rowIndex);
			var rowEl = Ext.fly(row);
			var rowWidth = rowEl.getWidth();
			var rowHeight = rowEl.getHeight();
			
			this.setSize(rowWidth, rowHeight);
			
			// x-row-editor-bwrap - chrome
			this.el.dom.firstChild.style.height = rowHeight + 'px';
			// x-row-editor-bwrap.x-row-editor-body.x-box-inner 
			this.el.dom.firstChild.firstChild.firstChild.style.height = rowHeight + 'px';
			
			var cm = this.grid.colModel;

			var left = 0;
			var top = 0;
			var first = true;
			
			for(var i = 0, len = cm.getColumnCount(); i < len; i++)
			{
				var editor = cm.getColumnAt(i).editor;

				if(!cm.isHidden(i))
				{
					var adjust = first ? 1 : 1;
					var leftajust,
						comboajust;
					
					if (Ext.isGecko) {
						if (editor != null && editor.xtype == 'z8.combo') {
							comboajust = 1;
						} else {
							comboajust = 0;
						}
						
						leftajust = (editor == null) ? 2 : -1;
					} else {
						leftajust = 0;
						comboajust = 0;
					}
					
					
	
					var width = cm.getColumnWidth(i) - adjust - comboajust;
					
					left = left - leftajust;
					
					if(editor != null)
					{
						editor.show();
						editor.setWidth(width);
						editor.setHeight(rowHeight);
						editor.setPosition(left, editor.getPosition(true)[1]);
					}
					
					left += width + adjust;

					first = false;
				}
				else if(editor != null)
				{
					editor.hide();
				}
			}
		}
	},

	slideHide : function()
	{
		this.hide();
	},

	initFields: function()
	{
		if(this.initialized)
		{
			return;
		}
		
		var cm = this.grid.getColumnModel();
		var pm = Ext.layout.ContainerLayout.prototype.parseMargins;
		this.removeAll(false);

		this.editors = [];
		
		for(var i = 0, len = cm.getColumnCount(); i < len; i++)
		{
			var c = cm.getColumnAt(i);
			
			if(c.noEditor)
			{
				continue;
			}
			
			var ed = c.getEditor();

			if(!ed)
			{
				ed = c.displayEditor || new Ext.form.DisplayField({ hidden: true });
			}
			
			ed.parentGrid = this.grid;

			this.editors.push(ed);
			
			if(i == 0)
			{
				ed.margins = pm('0 1 2 1');
			}
			else if(i == len - 1)
			{
				ed.margins = pm('0 0 2 1');
			}
			else
			{
				if (Ext.isIE)
				{
					ed.margins = pm('0 0 2 0');
				}
				else
				{
					ed.margins = pm('0 1 2 0');
				}
			}
			
			ed.setWidth(cm.getColumnWidth(i));
			ed.column = c;
			c.editor = ed;

			ed.enableKeyEvents = true;
			
			if(ed.ownerCt !== this)
			{
				ed.on('specialkey', this.onSpecialKey, this);
				ed.on('keydown', this.onKey, this);
				ed.on('focus', this.onFocus, this);

				if(ed.isTriggerField)
				{
					ed.on('select', this.onTriggerSelect, this);
				}
			}
			
			this.insert(i, ed);

			if(!ed.rendered)
			{
				ed.render(this.body);
			}
		}

		this.evaluator = new Z8.evaluations.Evaluator(this.editors, this);
		
		this.initialized = true;
	},

	getEditors: function()
	{
		return this.editors;
	},
	
	onFocus: function(editor)
	{
		this.lastFocused = editor;

		if(this.isVisible())
		{
			this.grid.getView().ensureVisible(this.rowIndex, this.grid.colModel.getIndexById(editor.column.id), true);

			if(Ext.isIE)
			{
				this.selectText(editor);

			}
			else
			{
				this.selectText.defer(this.focusDelay, this, [editor]);
			}
		}
	},

	selectText: function(editor)
	{
		if(editor.selectText != null)
		{
			editor.selectText();
		}
	},
	
	onSpecialKey: function(f, e)
	{
		if(f.xtype == 'checkbox')
		{
			this.onKey(f, e);
		}
	},

	onKey: function(f, e)
	{
		var key = e.getKey();

		if(key == e.ENTER || key == e.F2)
		{
			if(this.lastFocused != null && this.lastFocused.isTriggerField)
			{
			    var expanded = this.lastFocused.isExpanded();
			    
	            this.lastFocused.onRowSelected();

                if(expanded)
                {			    
                    return;
	            }
			}
			
			this.lastFocused.fireEvent('blur');
			
			this.stopEditing(true);
			Z8.stopEvent(e);
		}
		else if(key == e.ESC)
		{
			this.stopEditing(false);
			Z8.stopEvent(e);
		}
	},

	onGridKey: function(e)
	{
		var key = e.getKey();

		if((key == e.ENTER || key == e.F2) && !this.isVisible())
		{
			var record = this.grid.getSelectedRecord();
			
			if(record != null)
			{
				this.startEditing(record);
				Z8.stopEvent(e);
			}
		}
		else if(key == e.ESC)
		{
			this.stopEditing(false);
			Z8.stopEvent(e);
		}
	},

	onOpenEditor: function(grid, rowIndex, cellIndex, e)
	{
		if (Ext.get(e.getTarget()).hasClass('silk-attach'))
		{
			return;
		}
		
		if(e.button != 0 || e.target.tagName == 'A')
		{
			return;	
		}
		
		if (e.altKey)
		{
			return;
		}
		
		var record = this.grid.getRecord(rowIndex);
		var selectedRecord = this.grid.getSelectedRecord();
		
		if(record == selectedRecord && !this.isVisible())
		{
			this.startEditing(record, false);
			this.doFocus.defer(this.focusDelay, this, [e.getPoint()]);
		}
		else if(this.isVisible())
		{
			this.stopEditing(true);
		}
	},

	onRender: function()
	{
		Z8.grid.RowEditor.superclass.onRender.apply(this, arguments);

		this.el.swallowEvent(['keydown', 'keyup', 'keypress']);
	},

	afterRender: function()
	{
		Z8.grid.RowEditor.superclass.afterRender.apply(this, arguments);
		
		if(this.monitorValid)
		{
			this.startMonitoring();
		}
	},

	onShow: function()
	{
		if(this.monitorValid)
		{
			this.startMonitoring();
		}

		Z8.grid.RowEditor.superclass.onShow.call(this);
	},
	
	onHide: function()
	{
		this.stopMonitoring();
		
		var view = this.grid.getView();
		
		if(view.getRow(this.rowIndex) != null)
		{
			view.focusRow(this.rowIndex);
		}

		Z8.grid.RowEditor.superclass.onHide.call(this);
	},

	onOuterClick: function(event)
	{
		if(this.isVisible() && !this.within(event))
		{
			this.stopEditing(true);
		}
	},

	// private
	preEditValue : function(r, field)
	{
		var value = r.data[field];
		return this.autoEncode && typeof value === 'string' ? Ext.util.Format.htmlDecode(value) : value;
	},

	// private
	postEditValue : function(value, record, dataIndex)
	{
		return this.autoEncode && typeof value == 'string' ? Ext.util.Format.htmlEncode(value) : value;
	},

	doFocus: function(pt)
	{
		if(this.isVisible())
		{
			var index = 0;
			var cm = this.grid.getColumnModel();

			if(pt)
			{
				index = this.getTargetColumnIndex(pt);
			}
			else if(this.lastFocused != null)
			{
				index = this.getColumnIndex(this.lastFocused);
				
				if(cm.getColumnAt(index).hidden)
				{
					index = 0;
				}
			}

			for(var i = index || 0, len = cm.getColumnCount(); i < len; i++)
			{
				var column = cm.getColumnAt(i);
				
				if(!column.hidden)
				{
					var editor = column.getEditor();
					
					if(editor != null)
					{
						editor.focus(editor.el != null && editor.el.dom != null && editor.el.dom.select != null);
						break;
					}
				}
			}
		}
	},

	getColumnIndex: function(editor)
	{
		var cm = this.grid.getColumnModel();

		for(var i = 0, len = cm.getColumnCount(); i < len; i++)
		{
			var column = cm.getColumnAt(i);
		
			if(!column.hidden && column.getEditor() == editor)
			{
				return i;
			}
		}
		return 0;		
	},

	getTargetColumnIndex: function(pt)
	{
		var grid = this.grid;
		var view = grid.getView();
		var x = pt.left;
		var columns = grid.getColumnModel().config;
		var i = 0;
		var match = false;

		for(var len = columns.length, column; column = columns[i]; i++)
		{
			if(!column.hidden)
			{
				if(Ext.fly(view.getHeaderCell(i)).getRegion().right >= x)
				{
					match = i;
					break;
				}
			}
		}

		return match;
	},

	startMonitoring : function()
	{
		if(!this.bound && this.monitorValid)
		{
			this.bound = true;
			Ext.TaskMgr.start(
			{
				run : this.bindHandler,
				interval : this.monitorPoll || 200,
				scope: this
			});
		}
	},

	stopMonitoring : function()
	{
		this.bound = false;
		if(this.tooltip)
		{
			this.tooltip.hide();
		}
	},

	isValid: function()
	{
		var valid = true;
		
		this.items.each(function(f)
		{
			if(!f.isValid(true))
			{
				valid = false;
				return false;
			}
		});
		return valid;
	},

	// private
	bindHandler : function()
	{
		if(!this.bound)
		{
			return false; // stops binding
		}
		var valid = this.isValid();
		if(!valid && this.errorSummary)
		{
			this.showTooltip(this.getErrorText().join(''));
		}
		this.fireEvent('validation', this, valid);
	},

	lastVisibleColumn : function()
	{
		var i = this.items.getCount() - 1, c;
		
		for(; i >= 0; i--)
		{
			c = this.items.items[i];
			if (!c.hidden)
			{
				return c;
			}
		}
	},

	showTooltip: function(msg){
		var t = this.tooltip;
		if(!t){
			t = this.tooltip = new Ext.ToolTip({
				maxWidth: 600,
				cls: 'errorTip',
				width: 300,
				title: this.errorText,
				autoHide: false,
				anchor: 'left',
				anchorToTarget: true,
				mouseOffset: [40,0]
			});
		}
		var v = this.grid.getView(),
			top = parseInt(this.el.dom.style.top, 10),
			scroll = v.scroller.dom.scrollTop,
			h = this.el.getHeight();

		if(top + h >= scroll){
			t.initTarget(this.lastVisibleColumn().getEl());
			if(!t.rendered){
				t.show();
				t.hide();
			}
			t.body.update(msg);
			t.doAutoWidth(20);
			t.show();
		}else if(t.rendered){
			t.hide();
		}
	},

	getErrorText: function(){
		var data = ['<ul>'];
		this.items.each(function(f){
			if(!f.isValid(true)){
				data.push('<li>', f.getActiveError(), '</li>');
			}
		});
		data.push('</ul>');
		return data;
	}
});
