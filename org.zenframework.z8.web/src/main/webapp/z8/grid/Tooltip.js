Z8.grid.Tooltip = Ext.extend(Ext.ToolTip,
{
	maxWidth: 500,
	delegate: '.x-grid3-cell-inner',
	dismissDelay: 0,
	
	floating: { shadow: 'drop', shim: false, useDisplay: true, constrain: true },
	
	constructor: function(config)
	{
		this.grid = config.grid;
		this.view = this.grid.getView();
		this.target = this.view.mainBody;
		this.renderTo = document.body;

		Z8.grid.Tooltip.superclass.constructor.call(this, config);
	},
	
	initComponent: function()
	{
		Z8.grid.Tooltip.superclass.initComponent.call(this);
		
		this.on('beforeshow', this.handleBeforeShow, this);
		this.on('hide', this.handleHide, this);
	},
	
	handleBeforeShow: function(tooltip)
	{
		var view = this.view;
		var cell = view.findCell(tooltip.triggerElement);
		
		var txt = cell.innerText ? cell.innerText : cell.textContent;
		
		if(cell == null || txt == null || txt == ' ' || txt.length == 2 && txt.charCodeAt(0) == 160 && txt.charCodeAt(1) == 10)
		{
			return false;
		}
		
		tooltip.body.dom.innerHTML = Z8.Format.nl2br(cell.innerHTML);

		this.dqTask = new Ext.util.DelayedTask(this.delayedExpand, this);
		this.dqTask.delay(1000);
		
		return true;
	},

	onDestroy: function()
	{
		this.handleHide();
		this.view = this.grid = this.target = null;
		
		Z8.grid.Tooltip.superclass.onDestroy.call(this);
	},
	
	handleHide: function()
	{
		if(this.dqTask != null)
		{
			this.dqTask.cancel();
			this.dqTask = null;
		}
	},
	
	delayedExpand: function()
	{
		if(this.isVisible() && this.view != null && this.view.el != null && this.view.cm != null)
		{
			var view = this.view;
			
			var grid = this.grid;
			var store = grid.store;
			
			var cell = view.findCell(this.triggerElement);
			
			if(cell != null)
			{
				var cellIndex = view.getCellIndex(cell);
				var record = view.findRecord(this.triggerElement);
				
				if(record != null)
				{
					var column = grid.getColumnModel().columns[cellIndex];
					
					var queryId = column.editWith;
					var recordId = record.get(column.linkId);
					
					if(queryId != null && recordId != null)
					{
						var failure = function() {};
						Z8.Ajax.request(queryId, this.showExpanded, failure, { xaction: 'readRecord', recordId: recordId }, this);
					}
					else
					{
						this.getEl().constrainXY();
						this.syncShadow();
					}
				}
			}
		}
	},
	
	showExpanded: function(result)
	{
		if(this.isVisible() && result.data.length != 0)
		{
			this.setWidth(500);
			
			var html = '<div style="padding:2px 5px 5px 5px;"><table border="0" cellspacing="0" cellpadding="0" class="x-grid3-row-table"><thead>';

			for(var i = 0; i < result.fields.length; i++)
			{
				var field = new Z8.query.Field(result.fields[i]);
				
				if(field.header == null || !field.visible)
				{
					continue;
				}
				
				var data = result.data[i];

				var value = data[field.id];
				
				var renderer = null;

				if(field.serverType == Z8.ServerTypes.Date)
				{
					value = Date.parseDate(value, Z8.Format.Date);
					renderer = Ext.util.Format.dateRenderer();
				}
				else if(field.serverType == Z8.ServerTypes.Datetime)
				{
					value = Date.parseDate(value, Z8.Format.Datetime);
					renderer = Ext.util.Format.dateRenderer(Z8.Format.Datetime);
				}
				else if(field.serverType == Z8.ServerTypes.Float)
				{
					value = new Ext.data.Field(field).convert(value);
					renderer = Ext.util.Format.numberRenderer(Z8.Format.Float);
				}
				else if(field.serverType == Z8.ServerTypes.Integer)
				{
					value = new Ext.data.Field(field).convert(value);
					renderer = Ext.util.Format.numberRenderer(Z8.Format.Integer);
				}
				else if(field.serverType == Z8.ServerTypes.Boolean)
				{
					value = value ? Z8.Format.TrueText : Z8.Format.FalseText;
				}
				
				
				if(renderer != null)
				{
					value = renderer(value);
				}
				
				html +=
					'<tr class="x-grid3-row">' +
					'<td style="padding-top: 3px;" class="x-grid3-col x-grid3-cell"><b>' + field.header + ':</b></td>' +
					'<td style="padding-top: 3px; padding-left: 20px;" class="x-grid3-col x-grid3-cell">' + Ext.util.Format.htmlEncode(value) + '</td>' +
					'</tr>';
			}

			html += '</thead></table></div>';

			this.body.dom.innerHTML = html;

            this.getEl().constrainXY();
			this.syncShadow();
		}
	}
});

Ext.Shadow.prototype.realign = function(l, t, w, h)
{
	if(!this.el)
	{
		return;
	}
	
	var a = this.adjusts,
		d = this.el.dom,
		s = d.style,
		iea = 0,
		sw = Math.max(0, w + a.w),
		sh = Math.max(0, h + a.h),
		sws = sw + "px",
		shs = sh + "px",
		cn,
		sww;

	s.left = (l + a.l) + "px";
	s.top = (t + a.t) + "px";
    
	if(s.width != sws || s.height != shs)
	{
		s.width = sws;
		s.height = shs;

		if(!Ext.isIE)
		{
			cn = d.childNodes;
			sww = Math.max(0, (sw - 12)) + "px";
			cn[0].childNodes[1].style.width = sww;
			cn[1].childNodes[1].style.width = sww;
			cn[2].childNodes[1].style.width = sww;
			cn[1].style.height = Math.max(0, (sh - 12)) + "px";
		}
	}
};
