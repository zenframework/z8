Z8.grid.GroupSummary = Ext.extend(Ext.util.Observable,
{
	constructor: function(config)
	{
		Ext.apply(this, config);
		Z8.grid.GroupSummary.superclass.constructor.call(this);
	},
	
	init: function(grid)
	{
		this.grid = grid;
		var v = this.view = grid.getView();
		v.doGroupEnd = this.doGroupEnd.createDelegate(this);

		v.afterMethod('onColumnWidthUpdated', this.doWidth, this);
		v.afterMethod('onAllColumnWidthsUpdated', this.doAllWidths, this);
		v.afterMethod('onColumnHiddenUpdated', this.doHidden, this);
		v.afterMethod('onUpdate', this.doUpdate, this);
		v.afterMethod('onRemove', this.doRemove, this);

		if(!this.rowTpl)
		{
			this.rowTpl = new Ext.Template(
				'<div class="x-grid3-summary-row" style="{tstyle}">',
				'<table class="x-grid3-summary-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
					'<tbody><tr>{cells}</tr></tbody>',
				'</table></div>'
			);
			this.rowTpl.disableFormats = true;
		}
		
		this.rowTpl.compile();

		if(!this.cellTpl)
		{
			this.cellTpl = new Ext.Template(
				'<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}">',
				'<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on">{value}</div>',
				"</td>"
			);
			this.cellTpl.disableFormats = true;
		}
		
		this.cellTpl.compile();
	},

	toggleSummaries: function(visible)
	{
		var el = this.grid.getGridEl();
		
		if(el)
		{
			if(visible)
			{
				visible = el.hasClass('x-grid-hide-summary');
			}
			el[visible ? 'removeClass' : 'addClass']('x-grid-hide-summary');
		}
	},

	renderSummary: function(o, group)
	{
		cs = this.view.getColumnData();
		
		var cfg = this.grid.getColumnModel().config;
		var buf = [];
		
		for(var i = 0; i < cs.length; i++)
		{
			var c = cs[i];
			var cf = cfg[i];
			
			var p = {};
			p.id = c.id;
			p.style = c.style;
			p.css = i == 0 ? 'x-grid3-cell-first ' : (i == (cs.length - 1) ? 'x-grid3-cell-last ' : '');
			
			if(cf.summaryType || cf.summaryRenderer)
			{
				p.value = (cf.summaryRenderer || c.renderer)(o.data[c.name], p, o);
			}
			else
			{
				p.value = '';
			}
			
			if(Z8.isEmpty(p.value))
			{
				p.value = "&#160;";
			}
			
			buf[buf.length] = this.cellTpl.apply(p);
		}

		return this.rowTpl.apply(
		{
			tstyle: 'width:' + this.view.getTotalWidth() + ';' + (group != null ? group.color : ''), 
			cells: buf.join('')
		});
	},

	privateCalculate: function(group)
	{
		var cs = this.grid.getView().getColumnData();
		
		var data = {}, c, cfg = this.grid.getColumnModel().config, cf;
		var records = group.records;
		
		for(var j = 0; j < records.length; j++)
		{
			var record = records[j];
			
			for(var i = 0, len = cs.length; i < len; i++)
			{
				c = cs[i];
				cf = cfg[i];
				
				if(cf.summaryType)
				{
					data[c.name] = Z8.grid.GroupSummary.Calculations[cf.summaryType](data[c.name] || 0, record, c.name, data);
				}
			}
		}
		
		return data;
	},

	calculate: function(group)
	{
		return this.privateCalculate(group);
	},

	doGroupEnd: function(group)
	{
		// temporary fix until nested summaryData from server is unavailable
		var data = group.level == 0 ? this.calculate(group) : this.privateCalculate(group);
		return '</div>' + this.renderSummary({data: data}, group) + '</div>';
	},

	doWidth: function(column, width, totalWidth)
	{
		if(!this.isGrouped())
		{
			return;
		}
		
		var groups = this.view.getAllGroups();

		for(var i = 0; i < groups.length; i++)
		{
			var summary = groups[i].childNodes[2];
			summary.style.width = totalWidth;
			summary.firstChild.style.width = totalWidth;
			summary.firstChild.rows[0].childNodes[column].style.width = width;
		}
	},

	doAllWidths: function(widths, totalWidth)
	{
		if(!this.isGrouped())
		{
			return;
		}
		
		var groups = this.view.getAllGroups();

			
		for(var i = 0; i < groups.length; i++)
		{
			var summary = groups[i].childNodes[2];
			summary.style.width = totalWidth;
			summary.firstChild.style.width = totalWidth;
			
			var cells = summary.firstChild.rows[0].childNodes;
			
			for(var j = 0; j < widths.length; j++)
			{
				cells[j].style.width = widths[j];
			}
		}
	},

	doHidden: function(column, hidden, totalWidth)
	{
		if(!this.isGrouped())
		{
			return;
		}
		
		var groups = this.view.getAllGroups();

		for(var i = 0; i < groups.length; i++)
		{
			var summary = groups[i].childNodes[2];
			summary.style.width = totalWidth;
			summary.firstChild.style.width = totalWidth;
			summary.firstChild.rows[0].childNodes[column].style.display = hidden ? 'none' : '';
		}
	},
	
	isGrouped: function()
	{
		return !Ext.isEmpty(this.grid.getStore().groupFields);
	},

	getSummaryNode: function(groupId)
	{
		var group = Ext.fly(groupId, '_gsummary');
		if(group != null)
		{
			return group.down('.x-grid3-summary-row', true);
		}
		return null;
	},

	refreshSummaryById: function(groupIds)
	{
		if(!this.isGrouped() || groupIds == null)
		{
			return;
		}
		
		var store = this.grid.getStore();
		var groups = store.getGroups();
			
		for(var i = 0; i < groupIds.length; i++)
		{
			var groupId = groupIds[i];
			var groupElement = Ext.getDom(groupId);
			var group = this.view.findGroupById(groups, groupId);
			
			if(groupElement != null)
			{
				var existing = this.getSummaryNode(groupId);
		
				if(existing)
				{
					groupElement.removeChild(existing);
				}

				var data = group == null ? {} : this.calculate(group);
				var markup = this.renderSummary({data: data}, group);
				Ext.DomHelper.append(groupElement, markup);
			}
		}
	},

	doUpdate: function(store, record)
	{
		this.refreshSummaryById(record.groups);
	},

	doRemove: function(store, record, index, isUpdate)
	{
		if(!isUpdate)
		{
			this.refreshSummaryById(record.groups);
		}
	},

	showSummaryMsg: function(groupValue, msg)
	{
		var gid = this.view.getGroupId(groupValue),
			 node = this.getSummaryNode(gid);
		
		if(node)
		{
			node.innerHTML = '<div class="x-grid3-summary-msg">' + msg + '</div>';
		}
	}
});

Z8.grid.GroupSummary.Calculations =
{
	sum: function(v, record, field)
	{
		return v + (record.data[field]||0);
	},

	count: function(v, record, field, data)
	{
		return data[field + 'count'] ? ++data[field + 'count'] : (data[field + 'count'] = 1);
	},

	max: function(v, record, field, data)
	{
		return null;
/*
		var v = record.data[field];
		var max = data[field+'max'] === undefined ? (data[field+'max'] = v) : data[field+'max'];
		return v > max ? (data[field+'max'] = v) : max;
*/
	},

	min: function(v, record, field, data)
	{
		return null;
/*
		var v = record.data[field];
		var min = data[field+'min'] === undefined ? (data[field+'min'] = v) : data[field+'min'];
		return v < min ? (data[field+'min'] = v) : min;
*/
	},

	average: function(v, record, field, data)
	{
		var c = data[field + 'count'] ? ++data[field + 'count'] : (data[field + 'count'] = 1);
		var t = (data[field + 'total'] = ((data[field + 'total'] || 0) + (record.data[field] || 0)));
		return t === 0 ? 0 : t / c;
	}
};

Z8.grid.HybridSummary = Ext.extend(Z8.grid.GroupSummary,
{
	calculate : function(group)
	{
		var groupField = this.view.getGroupField();
		var data = this.getSummaryData(group);
		return data || Z8.grid.HybridSummary.superclass.calculate.call(this, group);
	},

	getSummaryData: function(group)
	{
		var store = this.grid.getStore();
		var reader = store.reader;
		var json = reader.jsonData;
		var fields = reader.recordType.prototype.fields;
			
		if(json && json.summaryData)
		{
			for(var property in json.summaryData)
			{
				var data = json.summaryData[property];
				
				if(group.comparableValue == data.groupValue)
				{
					return reader.extractValues(data, fields.items, fields.length);
				}
			}
		}
		return null;
	}
});
