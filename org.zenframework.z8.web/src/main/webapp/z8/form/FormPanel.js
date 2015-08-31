Z8.form.Form = Ext.extend(Ext.Panel, 
{
	layout : 'z8.form',
	padding: '0px 0px 0px 0px',
	border: true,
	autoScroll: true,
	bodyStyle: 'background-color: #e0e0e0;',
	cls: 'z8-form',

	focusDelay: 100,
	needLayout: true,
	
	constructor: function(config)
	{
		Ext.apply(this, config);
		
		config.autoScroll = true;
		config.layoutConfig = config.layoutConfig || {};
		config.layoutConfig.columns = this.query.columns;		

		config.items = this.query.section != null ? this.createFields(this.query.section) : [];
		
		Z8.form.Form.superclass.constructor.call(this, config);
	},

	createFields: function(section)
	{
		var fields = [];
		
		for(var i = 0; i < section.controls.length; i++)
		{
			fields[i] = this.createControl(section.controls[i]);
		}
		
		return fields;
	},

	createControl: function(control)
	{
		if(control.isSection)
		{
			return group = 
			{ 
					xtype:'fieldset',
					fieldId: control.id,
					title: control.header || 'Group title',
					layout: 'z8.form', 
					collapsible: true,
					colspan: control.colspan,
					layoutConfig: { columns: control.columns || 4, tableAtts: { border: 1 } },		
					border: true, 
					hidable: control.hidable,
					items: this.createFields(control)
			}
		}
		
		var field = this.query.getFieldById(control.id);
		var formField = Z8.Form.newField(field);
		
		formField.parentQuery = this.query;
	
		return formField;
	},
	
	initComponent: function()
	{
		Z8.form.Form.superclass.initComponent.call(this);

		var editors = this.getEditors();

		this.evaluator = new Z8.evaluations.Evaluator(editors, this);
		
		for(var i = 0; i < editors.length; i++)
		{
			editors[i].on('afterrender', this.onEditorRendered, this);
		}
	},
	
	add: function(component)
	{
		this.needLayout = true;
		
		Z8.form.Form.superclass.add.call(this, component);
	},
	
	onResize: function(adjustedWidth, adjustedHeight, width, height)
	{
		this.needLayout = true;
		
		Z8.form.Form.superclass.onResize.call(this, adjustedWidth, adjustedHeight, width, height);
	},
	
	isElementVisible: function(editor)
	{
		if(!editor.isVisible())
		{
			return false;
		}
		
		if(editor.ownerCt == null)
		{
			return true;
		}
		
		return this.isElementVisible(editor.ownerCt);
	},
	
	onLayout: function(shallow, forceLayout)
	{
		if(!this.needLayout)
		{
			return;
		}

		this.needLayout = false;
		
		var editors = this.getEditors();
		
		for(var i = 0; i < editors.length; i++)
		{
			var editor = editors[i];
			
			if(editor.rendered && this.isElementVisible(editor))
			{
				if(editor.stretch)
				{
					editor.setWidth(editor.minWidth);
				}
			}
		}
		
		for(var i = 0; i < editors.length; i++)
		{
			var editor = editors[i];
		
			if(editor.rendered && this.isElementVisible(editor))
			{
				if(editor.stretch)
				{
					var td = editor.getEl().findParent('td.x-form-element');
					if(td != null)
					{
						editor.newWidth = td.clientWidth;
					}
				}
			}
		}

		for(var i = 0; i < editors.length; i++)
		{
			var editor = editors[i];
			
			if(editor.rendered && this.isElementVisible(editor))
			{
				if(editor.newWidth != null)
				{
					editor.setSize(editor.newWidth, Math.min(editor.getHeight(), 300));
					delete editor.newWidth;
				}
			}
		}
		
		this.updateFieldsVisibility();
	},
	
	setRecord: function(record)
	{
		if(this.saving)
		{
			this.updateItem(this, record);
			return;
		}
		
		record = record != null && record.store != null ? record : null;
		
		this.record = record;
		
		var disabled = record == null;
		
		if(record != null)
		{
			var store = record.store;
			disabled = store.isRecordLocked(record);
		}
		
		this.saving = true;
		
		try
		{
			this.initItem(this, record, disabled);
		}
		finally
		{
			this.saving = false;
		}
		
		if(!this.needLayout)
		{
			this.needLayout = this.updateFieldsVisibility();
			this.onLayout();
		}
	},

	initItem: function(item, record, disabled)
	{
		if(item.items != null)
		{
			item.items.each(function(subitem, index, length)
			{
				this.initItem(subitem, record, disabled);
			}, this);
		}
		else
		{
			if(item.reset != null)
			{
				item.reset();
			}

			if(item.setDisabled != null)
			{
				if(!(item instanceof Ext.form.Checkbox) || !item.readOnly)
				{
					item.setDisabled(disabled);
				}
			}
			
			if(item.setHostRecord != null)
			{
				item.setHostRecord(record);
			}
			
			if(item.setValue != null)
			{
				item.setValue(record != null ? record.get(item.fieldId) : '');
			}

			this.updateDirtyState(item, record);
		}
	},

	updateItem: function(item, record)
	{
		if(item.items != null)
		{
			item.items.each(function(subitem, index, length)
			{
				this.updateItem(subitem, record);
			}, this);
		}
		else
		{
			this.updateDirtyState(item, record);
		}
	},

	onEditorRendered: function(editor)
	{
		editor.enableKeyEvents = true;
		
		if(editor.rendered)
		{
			editor.on('change', this.onFieldChanged, this);
			editor.on('check', this.onFieldChanged, this);
			editor.el.on('keyup', this.onFieldChanged, this);
			editor.on('select', editor.isTriggerField? this.onTriggerSelect : this.onFieldChanged, this);
			editor.on('focus', this.onFocus, this);
		}
	},

	onFocus: function(editor)
	{
		if(editor.selectText != null)
		{
			this.selectText.defer(this.focusDelay, this, [editor]);
		}
	},
	
	selectText: function(editor)
	{
		editor.selectText();
	},
	
	getEditors: function()
	{
		return this.doGetEditors(this);
	},
	
	doGetEditors: function(item)
	{
		if(item.items != null && item.store == null)
		{
			var result = [];
			for(var i = 0; i < item.items.getCount(); i++)
			{
				result = result.concat(this.doGetEditors(item.items.get(i)));
			}
			return result;
		}
		
		return [item];
	},
	
	findItem: function(item, id)
	{
		if(item.items != null)
		{
			for(var i = 0; i < item.items.getCount(); i++)
			{
				var result = this.findItem(item.items.get(i), id);
				
				if(result != null)
				{
					return result;
				}
			}
		}
		else if(item.fieldId == id)
		{
			return item;
		}
		
		return null;
	},
	
	getEditorById: function(fieldId)
	{
		var fields = this.items.items;
		
		for(var i = 0; i < fields.length; i++)
		{
			var result = this.findItem(fields[i], fieldId);
			
			if(result != null)
			{
				return result;
			}
		}
		
		return null;
	},
	
	onTriggerSelect: function(field, record)
	{
		if(record != null && this.record.get(field.linkId) == record.id)
		{
			return;
		}

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
		
		var editors = this.getEditors();
		
		for(var i = 0; i < editors.length; i++)
		{
			var editor = editors[i];
			
			if(editor.updateFilter != null)
			{
				editor.updateFilter(field, record);
			}
		}

		this.evaluator.update(field, triggers);

		this.saveRecord();
		
		this.needLayout = this.updateFieldsVisibility();
		this.onLayout();
	},
	
	updateFieldsVisibility: function(fieldsToShow, item)
	{
		var needLayout = false;

		if(fieldsToShow == null)
		{
			var editors = this.getEditors();
			var fieldsToShow = [];
		
			for(var i = 0; i < editors.length; i++)
			{
				var editor = editors[i];
			
				if(editor.fieldsToShow != null)
				{
					var value = this.record != null ? this.record.get(editor.linkedVia) : null;
					fieldsToShow = fieldsToShow.concat(editor.fieldsToShow[value] || []);
				}
			}
		}

		if(item != null && item.hidable)
		{
			var show = fieldsToShow.indexOf(item.fieldId) != -1;
			needLayout = show && !item.isVisible();

			item.setVisible(show);
		}
		
		var subitems = item == null ? this.items : item.items;

		if(subitems != null)
		{
			for(var i = 0; i < subitems.getCount(); i++)
			{
				needLayout = needLayout || this.updateFieldsVisibility(fieldsToShow, subitems.get(i));
			}
		}
		
		return needLayout;
	},
	
	onFieldChanged: function()
	{
		this.saveRecord();
	},

	updateDirtyState: function(item, record)
	{
		var dirty = record != null && record.dirty && record.modified[item.fieldId] != null;
		var element = item.getEl();
		
		if(element != null)
		{
			if(item instanceof Ext.form.Checkbox)
			{
				element = element.parent();
			}
				
			dirty ? element.addClass('x-grid3-dirty-cell') : element.removeClass('x-grid3-dirty-cell');
		}
	},
	
	saveRecords : function()
	{
		this.saveRecord();
		this.record.store.save();
	},
	
	saveRecord : function()
	{
		if(this.saving)
		{
			return;
		}
		
		this.saving = true;
		
		try
		{
			var changes = {};
			
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
	
			var fields = this.getEditors();
			
			for(var i = 0; i < fields.length; i++)
			{
				var editor = fields[i];
				
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

				var dataIndex = editor.fieldId;
	
				if(!Ext.isEmpty(dataIndex))
				{
					changeFn(changes, editor.getValue(), this.record, dataIndex);
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
			}
		}
		finally
		{
			this.saving = false;
		}
	}
});
