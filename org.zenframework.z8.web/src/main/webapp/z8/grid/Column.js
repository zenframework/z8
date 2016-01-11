Ext.grid.DateColumn.prototype.groupRenderer = null;

Ext.grid.Column.prototype.getCellTemplate = function()
{
	if(this.anchor)
	{
		return new Ext.Template(
			'<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
				'<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>',
					'<a', (Ext.isIE ? ' href="#"' : ''), ' hidefocus="on" onclick="return false;" class="x-grid3-cell-anchor" tabIndex="1" unselectable="on">{value}</a>',
				'</div>',
			'</td>'
		);
	}
	else
	{
		return new Ext.Template(
			'<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
				'<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>',
			'</td>'
		);
	}
}

Z8.grid.StatusColumn = Ext.extend(Ext.grid.Column, 
{
	constructor: function(config)
	{
		Z8.grid.StatusColumn.superclass.constructor.call(this, config);
		
		this.width = 22;
		this.fixed = true;
		this.menuDisabled = true;
		this.hideable = false;
		this.noEditor = true;
		this.sortable = false;
		this.groupable = false;
		this.filterable = false;
	},
	
	getCellTemplate: function()
	{
		return new Ext.Template(
			'<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
				'<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>',
			'</td>'
		);
	},
	
	getEditor: function()
	{
		return null;
	}
});

Z8.grid.LockStatusColumn = Ext.extend(Z8.grid.StatusColumn, 
{
	renderer: function(value, metadata, record, rowIndex, colIndex, store)
	{
		var cls = '';
		if(store.isRecordLocked(record))
		{
			cls = 'silk-lock';
		}

		return '<div unselectable="on" style="margin-left:' + (colIndex == 0 ? -15 : -3) + 'px; margin-top:-2px; width:22px; height: 15px" class="' + cls + '">' + Z8.emptyString + '</div>';
	}
});

Z8.grid.AttachmentsStatusColumn = Ext.extend(Z8.grid.StatusColumn, 
{
	renderer: function(value, metadata, record, rowIndex, colIndex, store)
	{
		var cls = '';
		if(store.filesProperty != null) {
			var data = record.data[store.filesProperty];
			if(!Z8.isEmpty(data) && data != '[]')
				cls = 'silk-attach';
		}

		return '<div unselectable="on" style="margin-left:' + (colIndex == 0 ? -18 : -6) + 'px; margin-top:-3px; width:22px; height: 16px" class="' + cls + '">' + Z8.emptyString + '</div>';
	}
});

Z8.grid.ButtonColumn = Ext.extend(Z8.grid.StatusColumn, 
{
	init: function(grid)
	{
		this.grid = grid; 
		this.grid.on('render', this.onRender, this);
	},

	onRender: function()
	{
		var view = this.grid.getView();
		view.mainBody.on('mousedown', this.onMouseDown, this);
		view.mainBody.on('mouseover', this.onMouseOver, this);
	},

	onMouseDown: function(e, target)
	{
//		if(Ext.fly(target).hasClass('z8-grid-status'))
//		{
			Z8.stopEvent(e);
//		}
	},

	onMouseOver: function(e, target)
	{
		var rowIndex = this.grid.findNode(target);

		if(rowIndex != null)
		{	
			var colModel = this.grid.getColumnModel();

			var overIndex = this.grid.findCellIndex(target);
			var myIndex = colModel.getIndexById(this.id);
			var isOver = overIndex == myIndex;
		
			var row = this.grid.getRow(rowIndex);
			var col = this.grid.getCell(row, myIndex);

			var rowChanged = this.activeRow != row;
			var colChanged = this.wasOver != isOver;
			
			col.style.backgroundPosition = 'center';
			
			if(rowChanged)
			{
				if(this.activeCol != null)
				{
					var element = Ext.fly(this.activeCol);
					element.removeClass(['silk-cross-circle', 'silk-cross-circle-inactive']);
				}

				var element = Ext.fly(col);
				element.addClass(isOver ? 'silk-cross-circle' : 'silk-cross-circle-inactive');
			}
			else if(colChanged)
			{
				if(this.activeCol != null)
				{
					var element = Ext.fly(this.activeCol);
					if(isOver)
					{
						element.removeClass('silk-cross-circle-inactive');
						element.addClass('silk-cross-circle');
					}
					else
					{
						element.removeClass('silk-cross-circle');
						element.addClass('silk-cross-circle-inactive');
					}
				}
			}
			
			this.wasOver = isOver;
			this.activeCol = col;
			this.activeRow = row;
		}
	},
	
	renderer: function(value, metadata, record, rowIndex, colIndex, store)
	{
		return '';
	}
});

Z8.grid.DecoratedColumn = Ext.extend(Ext.grid.NumberColumn, 
{
    constructor: function(cfg){
        Z8.grid.DecoratedColumn.superclass.constructor.call(this, cfg);
        this.renderer = this.myRenderer;
        this.scope = this;
    },

	myRenderer: function(value, metadata, record, rowIndex, colIndex, store)
	{
		var result = '';
		var cls = 'x-grid-eq';
		var color = 'black';

		if(value != 0) {
			cls = value < 0 ? 'x-grid-down' : 'x-grid-up';
			color = value < 0 ? 'red' : 'yellow';
		}
		
		result = '<div class="' + cls + '">';
		result += Ext.util.Format.number(value, this.format);
		result += '</div>';
		
		return result;
	}
});

Ext.grid.Column.types.lockStatusColumn = Z8.grid.LockStatusColumn;
Ext.grid.Column.types.attachmentsStatusColumn = Z8.grid.AttachmentsStatusColumn;
Ext.grid.Column.types.buttonColumn = Z8.grid.ButtonColumn;
Ext.grid.Column.types.decoratedColumn = Z8.grid.DecoratedColumn;
