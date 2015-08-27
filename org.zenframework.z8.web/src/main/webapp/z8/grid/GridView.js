Ext.grid.GridView.prototype.handleHdMenuHideCurrent = function() {
	
	var colModel = this.cm;
	if (this.hdCtxIndex)
		colModel.setHidden(this.hdCtxIndex, true);
	
//	this.btnShowHide.setDisabled(false);
};

Ext.grid.GridView.prototype.handleHdMenuShowAll = function(item) {
	
	var colModel = this.cm,
		colCount = colModel.getColumnCount(),
		i;

	for (i = 0; i < colCount; i++)
	{
		var column = colModel.columns[i];
		
		if(column.hidden && column.hideable !== false)
		{
			colModel.setHidden(i, false);
		}
	}
	
//	this.btnShowHide.setDisabled(true);
	
	this.refresh(true);
};

Ext.grid.GridView.prototype.hasHiddenColumns = function() {
	
	var colModel = this.cm;

	for(var i = 0; i < colModel.getColumnCount(); i++)
	{
		var column = colModel.columns[i];
		
		if(colModel.hidden && column.hideable !== false)
		{
			return true;
		}
	}
	
	return false;
};

Ext.grid.GridView.prototype.handleHdMenuClickDefault = function(item) {
    var colModel = this.cm,
    	itemId   = item.getItemId(),
    	index    = colModel.getIndexById(itemId.substr(4));

    if (index != -1) {
    	if (item.checked && colModel.getColumnsBy(this.isHideableColumn, this).length <= 1) {
    		this.onDenyColumnHide();
    		return;
    	}
    	colModel.setHidden(index, item.checked);
    	
//    	this.btnShowHide.setDisabled( ! this.hasHiddenColumns());
    }
};

Ext.grid.GridView.prototype.handleHdDown = function(e, target) {
    if (Ext.fly(target).hasClass('x-grid3-hd-btn')) {
		e.stopEvent();
            
		var colModel  = this.cm,
			header    = this.findHeaderCell(target),
			index     = this.getCellIndex(header),
			sortable  = colModel.isSortable(index),
			menu      = this.hmenu,
			menuItems = menu.items,
			menuCls   = this.headerMenuOpenCls;
            
		this.hdCtxIndex = index;
		
		var col = colModel.columns[index];
			
//		if (col.hideable === false)
//		{
//			this.btnShowHideCurrent.disable();
//		}
//		else
//		{
//			this.btnShowHideCurrent.enable();
//		}
            
		Ext.fly(header).addClass(menuCls);
		menuItems.get('asc').setDisabled(!sortable);
		menuItems.get('desc').setDisabled(!sortable);
            
		menu.on('hide', function() {
			Ext.fly(header).removeClass(menuCls);
		}, this, {single:true});
            
		menu.show(target, 'tl-bl?');
	}
};

Ext.grid.GridView.prototype.afterRenderUI = function() {
		
	var grid = this.grid;
        
	this.initElements();

        
	Ext.fly(this.innerHd).on('click', this.handleHdDown, this);

	this.mainHd.on({
		scope    : this,
		mouseover: this.handleHdOver,
		mouseout : this.handleHdOut,
		mousemove: this.handleHdMove,
		contextmenu: this.handleHdContextDown
	});

	this.scroller.on('scroll', this.syncScroll,  this);
        
	if (grid.enableColumnResize !== false) {
            this.splitZone = new Ext.grid.GridView.SplitDragZone(grid, this.mainHd.dom);
        }

        if (grid.enableColumnMove) {
            this.columnDrag = new Ext.grid.GridView.ColumnDragZone(grid, this.innerHd);
            this.columnDrop = new Ext.grid.HeaderDropZone(grid, this.mainHd.dom);
        }

        if (grid.enableHdMenu !== false) {
            this.hmenu = new Z8.menu.MulticolumnMenu({id: grid.id + '-hctx'});
            this.hmenu.add(
                {itemId:'asc',  text: this.sortAscText,  cls: 'xg-hmenu-sort-asc'},
                {itemId:'desc', text: this.sortDescText, cls: 'xg-hmenu-sort-desc'}
            );

            if (grid.enableColumnHide !== false)
            {
            	this.colMenu = new Z8.menu.MulticolumnMenu({items: this.colMenuItems(), id:grid.id + '-hcols-menu'});
            	
                this.colMenu.on({
                    scope     : this,
                   // beforeshow: this.beforeColMenuShow,
                    itemclick : this.handleHdMenuClick
                });
                
                this.hmenu.add('-', {
                    itemId:'columns',
                    hideOnClick: false,
                    text: this.columnsText,
                    menu: this.colMenu,
                    iconCls: 'x-cols-icon'
                });
                
                // Current Hide Menu Item
                this.btnShowHideCurrent = new Ext.menu.Item({itemId: 'columnhide', text: 'Скрыть столбец', iconCls: 'x-cols-hide-icon', handler: this.handleHdMenuHideCurrent, scope: this}); 
                this.hmenu.add(this.btnShowHideCurrent);
                
                // Hide / Show all columns
                this.btnShowHide = new Ext.menu.Item({itemId: 'showhidecolumns', text: 'Показать все столбцы', iconCls: 'x-cols-showall-icon', disabled: false/*!this.hasHiddenColumns()*/, handler: this.handleHdMenuShowAll, scope: this});
                this.hmenu.add(this.btnShowHide);
            }

            this.hmenu.on('itemclick', this.handleHdMenuClick, this);
        }

        if (grid.trackMouseOver) {
            this.mainBody.on({
                scope    : this,
                mouseover: this.onRowOver,
                mouseout : this.onRowOut
            });
        }

        if (grid.enableDragDrop || grid.enableDrag) {
            this.dragZone = new Ext.grid.GridDragZone(grid, {
                ddGroup : grid.ddGroup || 'GridDD'
            });
        }

        this.updateHeaderSortState();
};

Ext.grid.GridView.prototype.colMenuItems = function() {
    var colModel = this.cm,
        colCount = colModel.getColumnCount(),
        colMenu  = this.colMenu,
        colMenuItems = [],
        i;

    for (i = 0; i < colCount; i++) {
        if (colModel.config[i].hideable !== false) {
        	colMenuItems.push(new Ext.menu.CheckItem({
                text       : colModel.getColumnHeader(i),
                itemId     : 'col-' + colModel.getColumnId(i),
                checked    : !colModel.isHidden(i),
                disabled   : colModel.config[i].hideable === false,
                hideOnClick: false
            }));
        }
    }
    
    return colMenuItems;
};

Ext.grid.GridView.prototype.handleHdContextDown = function(e, target) {
	
    if (Ext.fly(target).hasClass('x-grid3-hd-inner') )
    {
        e.stopEvent();
        
        var colModel  = this.cm,
            header    = this.findHeaderCell(target),
            index     = this.getCellIndex(header),
            sortable  = colModel.isSortable(index),
            menu      = this.hmenu,
            menuItems = menu.items,
            menuCls   = this.headerMenuOpenCls;
        
        this.hdCtxIndex = index;
        
        Ext.fly(header).addClass(menuCls);
        menuItems.get('asc').setDisabled(!sortable);
        menuItems.get('desc').setDisabled(!sortable);
        
        menu.on('hide', function() {
            Ext.fly(header).removeClass(menuCls);
        }, this, {single:true});
        
        menu.show(target, 'tl-bl?');
    }
};


Z8.grid.GridView = Ext.extend(Ext.grid.GroupingView,
{
	colors: [ '#cc0000', '#00cc00', '#0000cc', '#cccc00', '#00cccc', '#cc00cc', '#238100', '#810023' ],

	splitHandleWidth: 10,
	
	autoFill: true,
	forceFit: true,

	columnsText: 'Столбцы',
	sortAscText: 'По возрастанию',
	sortDescText: 'По убыванию',
 
	groupByText : 'Группировать по столбцу',
	showGroupsText : 'Отображать группировку',
	emptyGroupText : 'Пусто',
	
	totalsHeight: 20,
	
	init: function(grid)
	{
		this.showTotals = grid.getStore().query.showTotals;
		
		Ext.grid.GroupingView.superclass.doRender = this.doRenderFn;
		Ext.grid.GroupingView.superclass.refreshRow = this.refreshRowFn;

		Z8.grid.GridView.superclass.init.call(this, grid);
	},
	
	initTemplates : function()
	{
		Z8.grid.GridView.superclass.initTemplates.call(this);

		this.startGroup = new Ext.XTemplate(
			'<div id="{groupId}" level="{level}" class="x-grid-group {cls}">',
				'<div class="x-grid-group-offset" style="padding-left: {padding}px"><div id="{groupId}-hd" level="{level}" class="x-grid-group-hd" style="{style}"><div class="x-grid-group-title">', this.groupTextTpl ,'</div></div></div>',
				'<div id="{groupId}-bd" class="x-grid-group-body">'
		);
		this.startGroup.compile();

		this.cellTpl = new Ext.Template(
			'<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
				'<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>',
					'<a', (Ext.isIE ? ' href="#"' : ''), ' ext:qtip="{tip}" hidefocus="on" class="x-grid3-cell-anchor" tabIndex="1" unselectable="on">{value}</a>',
				'</div>',
			'</td>'
		);
		
		var totalsMarkup = [
			'<div class="z8-grid3-totals" style="{ostyle}; overflow-x:hidden; overflow-y:hidden;">',
				'<div>{totals}</div>',
			'</div>',
			'<div style="overflow-x:auto;overflow-y:hidden;">',
				'<div>&#160;</div>',
			'</div>'].join('');

		this.masterTpl = new Ext.Template(
			'<div class="x-grid3" hidefocus="true">',
				'<div class="x-grid3-viewport">',
					'<div class="x-grid3-header">',
						'<div class="x-grid3-header-inner">',
							'<div class="x-grid3-header-offset" style="{ostyle}">{header}</div>',
						'</div>',
						'<div class="x-clear"></div>',
					'</div>',
					'<div class="x-grid3-scroller">',
						'<div class="x-grid3-body" style="{bstyle}">{body}</div>',
						'<a class="x-grid3-focus" tabIndex="-1"></a>',
					'</div>',
					this.showTotals ? totalsMarkup : '',
				'</div>',
				'<div class="x-grid3-resize-marker">&#160;</div>',
				'<div class="x-grid3-resize-proxy">&#160;</div>',
			'</div>'
		);

		this.totalsTpl = new Ext.Template(
			'<table border="0" cellspacing="0" cellpadding="0" style="{rowTableStyle}">',
			'<thead>',
				'<tr class="z8-grid3-totals-row">{cells}</tr>',
			'</thead>',
			'</table>'
		);

		var headerCellTpl = new Ext.Template(
			'<td class="x-grid3-hd x-grid3-cell x-grid3-td-{id} {css}" style="{style}">',
				'<div {tooltip} {attr} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">', 
					this.grid.enableHdMenu ? '<a class="x-grid3-hd-btn"></a>' : '',
					'<img alt="" class="x-grid3-sort-icon" src="', Ext.BLANK_IMAGE_URL, '" />',
					'{value}',
				'</div>',
			'</td>'
		);

		var totalsCellTpl = new Ext.Template(
			'<td class="z8-grid3-totals x-grid3-cell x-grid3-td-{id} {css}" style="{style}">',
				'<div {attr} class="z8-grid3-totals-inner z8-grid3-totals-{id}" unselectable="on" style="{istyle}">', 
					'{value}',
				'</div>',
			'</td>'
		);

		var rowBodyText = [
			'<tr class="x-grid3-row-body-tr" style="{bodyStyle}">',
				'<td colspan="{cols}" class="x-grid3-body-cell" tabIndex="0" hidefocus="on">',
					'<div class="x-grid3-row-body">{body}</div>',
				'</td>',
			'</tr>'].join('');
		
		var innerText = [
			'<table class="x-grid3-row-table" border="0" cellspacing="0" cellpadding="0" style="{rowTableStyle}">',
			'<tbody>',
				'<tr>{cells}</tr>',
				this.enableRowBody ? rowBodyText : '',
			'</tbody>',
			'</table>'].join('');
		
		Ext.apply(this.templates,
		{
			hcell: headerCellTpl,
			scell: totalsCellTpl,
			cell: this.cellTpl,
			body: this.bodyTpl,
			header: this.headerTpl,
			totals: this.totalsTpl,
			master: this.masterTpl,
			row: new Ext.Template('<div class="x-grid3-row {alt}" style="{rowStyle}">', innerText, '</div>'),
			rowInner: new Ext.Template(innerText)
		});

		for(var name in this.templates)
		{
			var template = this.templates[name];

			if(template && Ext.isFunction(template.compile) && !template.compiled)
			{
				template.disableFormats = true;
				template.compile();
			}
		}

		this.colRe = new RegExp('x-grid3-td-([^\\s]+)', '');
	},

	renderUI : function()
	{
		var templates = this.templates;

		var m = templates.master.apply({
			body  : templates.body.apply({rows:'&#160;'}),
			header: this.renderHeaders(),
			totals: this.showTotals ? this.renderTotals() : '',
			ostyle: 'width:' + this.getOffsetWidth() + ';',
			bstyle: 'width:' + this.getTotalWidth()  + ';'
		});
		
		return m;
	},
	
	afterRenderUI: function()
	{
		Z8.grid.GridView.superclass.afterRenderUI.call(this);
	
		if(this.showTotals)
		{
			this.viewport = this.mainWrap;
			
			this.totals = new Ext.Element(this.viewport.dom.childNodes[2]);
			this.hScroller = new Ext.Element(this.viewport.dom.childNodes[3]);
			this.hScrollerInner = new Ext.Element(this.hScroller.dom.firstChild);
	
			this.hScroller.on('scroll', this.syncHeaderScroll, this);
		}
		
		this.tooltip = new Z8.grid.Tooltip({ grid: this.grid });
	},	
 
	onDestroy: function()
	{
		if(this.tooltip != null)
		{
			this.tooltip.destroy();
			delete this.tooltip;
		}

		Z8.grid.GridView.superclass.onDestroy.call(this);
	},
	
	
	onLoad: function()
	{
	},
	
	onLayout: function(viewportWidth, viewportHeight)
	{
		this.updateScrollerWidth();
		this.updateGroupWidths();
	},

	scrollToTop: function()
	{
		Z8.grid.GridView.superclass.scrollToTop.call(this);
		
		if(this.showTotals)
		{
			this.totals.dom.scrollLeft = 0;
		}
	},

	syncHeaderScroll: function()
	{
		if(this.showTotals)
		{
			this.updateScrollerWidth();
	
			var scrollLeft = this.hScroller.dom.scrollLeft;
			this.scroller.dom.scrollLeft = scrollLeft;
			this.innerHd.scrollLeft = scrollLeft;
			this.totals.dom.scrollLeft = this.scroller.dom.scrollLeft;
		}

		Z8.grid.GridView.superclass.syncHeaderScroll.call(this);
	},

	updateScrollerWidth: function()
	{
		if(this.showTotals)
		{
			var totalWidth = this.cm.getTotalWidth();
	 
			this.hScrollerInner.setWidth(totalWidth + this.getScrollOffset());
			
			var viewportWidth = this.grid.getGridEl().getWidth();
			var viewportHeight = this.grid.getGridEl().getHeight();

			this.totalsHeight = this.totals.getHeight();
				
			this.totals.setWidth(viewportWidth);
			this.hScroller.setWidth(viewportWidth);

			var hScrollerHeight = totalWidth <= viewportWidth ? 0 : (Ext.getScrollBarWidth() - (Ext.isChrome ? 1 : 0));
			
			this.hScroller.setHeight(hScrollerHeight);
			this.scroller.setHeight(viewportHeight - this.mainHd.getHeight() - this.totalsHeight - hScrollerHeight);
		}
	},

	onColumnWidthUpdated: function(column, columnWidth, totalWidth)
	{
		if(this.showTotals)
		{
			var columnWidth = this.getColumnWidth(column);
			var totalsCell = this.getTotalsCell(column);
			totalsCell.style.width = columnWidth;
		}
	},
	
	onAllColumnWidthsUpdated: function(widths, totalWidth)
	{
		if(this.showTotals)
		{
			var colCount = this.cm.getColumnCount();

			for(var i = 0; i < colCount; i++)
			{
				this.onColumnWidthUpdated(i, widths[i], totalWidth);
			}
		}
	},

	onColumnHiddenUpdated: function(column, hidden, totalWidth)
	{
		if(this.showTotals)
		{
			var totalsCell = this.getTotalsCell(column);
			totalsCell.style.display = hidden ? 'none' : '';
			this.updateScrollerWidth();
		}
	},

	updateHeaderWidth: function(updateMain)
	{
		Z8.grid.GridView.superclass.updateHeaderWidth.call(this, updateMain);

		if(this.showTotals)
		{
			var div = this.totals.dom.firstChild;
			var totalsCells = div.firstChild;
			var totalWidth = this.getTotalWidth();
	
			div.style.width = this.getOffsetWidth();
			totalsCells.style.width = totalWidth;
		}
	},

	updateHeaders : function()
	{
		Z8.grid.GridView.superclass.updateHeaders.call(this);

		if(this.showTotals)
		{
			this.totals.dom.firstChild.innerHTML = this.renderTotals();
		}
	},

	getTotalsCell: function(index)
	{
		return this.totals.dom.getElementsByTagName('td')[index];
	},

	getTotalsData: function()
	{
		return this.grid.getStore().getTotalsData();
	},

	doRenderFn: function(columns, records, store, startRow, colCount, stripe)
	{
		var templates = this.templates,
			rowTemplate = templates.row,
			last = colCount - 1,
			rowBuffer = [],
			meta = {},
			alt;

		//build up each row's HTML
		for(var j = 0; j < records.length; j++)
		{
			var rowParams = {};

			var record = records[j];
			var colBuffer = [];
		
			var rowIndex = j + startRow;

			rowParams.rowTableStyle = 'width:' + this.getTotalWidth() + ';';

			var style = record.data.style;
			
			if(style != null)
			{
				rowParams.rowTableStyle += 'background-color:' + style.background + ';' +
					'color:' + style.color + ';';
			}

			//build up each column's HTML
			for(var i = 0; i < colCount; i++)
			{
				var column = columns[i];
				
				var cellTemplate = column.cellTemplate != null ? column.cellTemplate : this.templates.cell;
				
				meta.id	= column.id;
				meta.css   = i === 0 ? 'x-grid3-cell-first ' : (i == last ? 'x-grid3-cell-last ' : '');
				meta.attr  = meta.cellAttr = '';
				meta.style = column.style;
				meta.value = column.renderer.call(column.scope, record.data[column.name], meta, record, rowIndex, i, store);

				if(Ext.isEmpty(meta.value))
				{
					meta.value = Z8.emptyString;
					meta.tip = '';
				}
				else
				{
					meta.tip = Ext.util.Format.htmlEncode(meta.value);
				}

				if(this.markDirty && record.dirty && record.modified[column.name] != null)
				{
					meta.css += ' x-grid3-dirty-cell';
				}

				colBuffer[colBuffer.length] = cellTemplate.apply(meta);
			}

			alt = [];
			//set up row striping and row dirtiness CSS classes
			if(stripe && ((rowIndex + 1) % 2 === 0))
			{
				alt[0] = 'x-grid3-row-alt';
			}

			if(record.dirty) 
			{
				alt[1] = ' x-grid3-dirty-row';
			}

			rowParams.cols = colCount;

			if(this.getRowClass)
			{
				alt[2] = this.getRowClass(record, rowIndex, rowParams, store);
			}

			rowParams.alt   = alt.join(' ');
			rowParams.cells = colBuffer.join('');

			rowBuffer[rowBuffer.length] = rowTemplate.apply(rowParams);
		}

		return rowBuffer.join('');
	},

	onRowOver: function(e, target)
	{
		var rowIndex = this.findRowIndex(target);

		if(rowIndex !== false)
		{
			var row = this.getRow(rowIndex);
			
			if(row != null)
			{
				this.addRowClass(rowIndex, this.rowOverCls);
			
				row.firstChild.style.color = '';
				row.firstChild.style.backgroundColor = '';
			}
		}
	},

	onRowOut: function(e, target)
	{
		var rowIndex = this.findRowIndex(target);

		if(rowIndex !== false)
		{
			var row = this.getRow(rowIndex);
			
			if(row != null && !e.within(row, true))
			{
				this.removeRowClass(rowIndex, this.rowOverCls);
				
				if(!Ext.fly(row).hasClass(this.selectedRowClass))
				{
					var record = this.grid.getRecord(rowIndex);
					var style = record.data.style;
					
					if(!Z8.isEmpty(style))
					{
						row.firstChild.style.color = style.color;
						row.firstChild.style.backgroundColor = style.background;
					}
				}
			}
		}
	},

	onRowSelect: function(rowIndex)
	{
		this.addRowClass(rowIndex, this.selectedRowClass);

		var row = this.getRow(rowIndex);
		
		if(row != null)
		{
			row.firstChild.style.color = '';
			row.firstChild.style.backgroundColor = '';
		}
	},

	onRowDeselect: function(rowIndex)
	{
		this.removeRowClass(rowIndex, this.selectedRowClass);

		var row = this.getRow(rowIndex);

		if(row != null && !Ext.fly(row).hasClass(this.rowOverCls))
		{
			var record = this.grid.getRecord(rowIndex);
			var style = record.data.style;
			
			if(!Z8.isEmpty(style))
			{
				row.firstChild.style.color = style.color;
				row.firstChild.style.backgroundColor = style.background;
			}
		}
	},

	getPrefix: function(group)
	{
		if(group.parent != null)
		{
			return this.getPrefix(group.parent) + '-' + group.field + '-' + group.value;
		}
		
		return this.grid.getGridEl().id + '-' + group.field + '-' + group.value;
	},
	
	constructId: function(group)
	{
		return Ext.util.Format.htmlEncode(this.getPrefix(group));
	},

	getColumnData: function()
	{
		var columns  = [],
			colModel = this.cm,
			colCount = colModel.getColumnCount(),
			fields   = this.ds.fields,
			i, name;
		
		for(i = 0; i < colCount; i++)
		{
			name = colModel.getDataIndex(i);
			
			columns[i] = {
				name	: name,
				renderer: colModel.getRenderer(i),
				scope   : colModel.getRendererScope(i),
				id	  : colModel.getColumnId(i),
				style   : this.getColumnStyle(i),
				cellTemplate: colModel.columns[i].getCellTemplate != null ? colModel.columns[i].getCellTemplate() : this.templates.cell
			};
		}
		
		return columns;
	},

	
	renderTotals: function()
	{
		var colModel = this.cm;
		var colCount = colModel.getColumnCount();
		var last = colCount - 1;
		var cells = [];
		
		var totalsData = this.getTotalsData();
		
		for(var i = 0; i < colCount; i++)
		{
			var cssCls =  '';

			if(i == 0)
			{
				cssCls = 'z8-grid3-totals-cell-first ';
			}
			else if(i == last)
			{
				cssCls = 'z8-grid3-totals-cell-last ';
			}
			
			var value = '';
			
			if(totalsData != null)
			{
				value = totalsData[colModel.getDataIndex(i)];
				
				if(value == null)
				{
					value = '';
				}
			}
			
			if(String(value) != '')
			{
				var column = colModel.columns[i];
				value = column.renderer.call(column.scope, value);
			}
			
			var properties =
			{
				id: colModel.getColumnId(i),
				value: value == '' ? '&#160;' : value,
				style: this.getColumnStyle(i, true),
				css: cssCls
			};
			
			cells[i] = this.templates.scell.apply(properties);
		}
		
		return this.templates.totals.apply({
			cells : cells.join(''),
			rowTableStyle: String.format("width: {0};", this.getTotalWidth())
		});
	},

    afterRender: function()
    {
		Z8.grid.GridView.superclass.afterRender.call(this);
		
		if(this.autoFill)
		{
			this.scroller.setStyle('overflow-x', this.showTotals ? 'hidden' : 'auto');
		}
		
		if(this.grid.deferRowRender)
		{
            this.updateGroupWidths();
        }
    },
	
	updateGroupWidths : function()
	{
        if(!this.canGroup() || !this.hasRows()){
            return;
        }
        var tw = Math.max(this.cm.getTotalWidth(), this.el.dom.offsetWidth-this.getScrollOffset()) +'px';
        var gs = this.getGroups();
        for(var i = 0, len = gs.length; i < len; i++)
        {
            gs[i].firstChild.style.width = tw;
        }
    },
	
	onColumnSplitterMoved : function(cellIndex, width)
	{
		if(this.autoFill)
		{
			this.forceFit = false;
			this.scroller.setStyle('overflow-x', this.showTotals ? 'hidden' : 'auto');
		}
		
		Z8.grid.GridView.superclass.onColumnSplitterMoved.call(this, cellIndex, width);
	},

	fitColumns: function(preventRefresh, onlyExpand, omitColumn)
	{
		var grid = this.grid;
		var colModel = this.cm;
		var totalColWidth = colModel.getTotalWidth(false);
		var gridWidth = this.getGridInnerWidth();
		var extraWidth = gridWidth - totalColWidth;
		var columns = [];
		var width = 0;

		if(gridWidth < 20 || extraWidth === 0)
		{
			return false;
		}

		var visibleColCount = colModel.getColumnCount(true);
		var totalColCount = colModel.getColumnCount(false);
		var adjCount = visibleColCount - (Ext.isNumber(omitColumn) ? 1 : 0);

		if(adjCount === 0)
		{
			adjCount = 1;
			omitColumn = undefined;
		}

		for(var i = 0; i < totalColCount; i++)
		{
			var column = colModel.columns[i];
			
			if(!colModel.isFixed(i) && column.stretch && i !== omitColumn)
			{
				var colWidth = colModel.getColumnWidth(i);
				columns.push(i, colWidth);

				if(!colModel.isHidden(i))
				{
					width += colWidth;
				}
			}
		}

		if(width != 0)
		{
			var fraction = (gridWidth - colModel.getTotalWidth()) / width;
	
			while(columns.length)
			{
				var colWidth = columns.pop();
	            var i = columns.pop();
	
				colModel.setColumnWidth(i, Math.max(grid.minColumnWidth, Math.floor(colWidth + colWidth * fraction)), true);
			}
	
			if(preventRefresh !== true)
			{
				this.updateAllColumnWidths();
			}
		}
		
		return true;
	},
	
	onGroupByClick: function()
	{
		var grid = this.grid;

		var field = this.cm.getDataIndex(this.hdCtxIndex);
		var groupFields = grid.store.getGroupFields();
		
		if(groupFields.indexOf(field) != -1)
		{
			return;
		}
		
		var fields = [];
		
		for(var i = 0; i < groupFields.length; i++)
		{
			fields.push(groupFields[i]);
		}
		fields.push(field);

		this.enableGrouping = true;
		grid.store.groupBy(fields);
		this.beforeMenuShow();
	},

	refreshRow : function(record)
	{
		this.isUpdating = true;
		Ext.grid.GroupingView.superclass.refreshRow.apply(this, arguments);
		this.isUpdating = false;
	},

	refreshRowFn: function(record)
	{
		var store = this.ds,
			colCount = this.cm.getColumnCount(),
			columns = this.getColumnData(),
			last = colCount - 1,
			cls = ['x-grid3-row'],
			colBuffer = [],
			rowIndex, column, meta, css, i;

		if(Ext.isNumber(record))
		{
			rowIndex = record;
			record   = store.getAt(rowIndex);
		}
		else
		{
			rowIndex = store.indexOf(record);
		}
		
		//the record could not be found
		if(!record || rowIndex < 0)
		{
			return;
		}

		var row = this.getRow(rowIndex);
		
		if(row == null)
		{
			return;
		}
		
		var rowParams = {};

		rowParams.rowTableStyle = 'width:' + this.getTotalWidth() + ';';

		var style = record.data.style;

		if(style != null)
		{
			rowParams.rowTableStyle += 'background-color:' + style.background + ';' +
				'color:' + style.color + ';';
		}
		
		//builds each column in this row
		for (i = 0; i < colCount; i++)
		{
			column = columns[i];

			var cellTpl = column.cellTemplate != null ? column.cellTemplate : this.templates.cell;

			if(i == 0)
			{
				css = 'x-grid3-cell-first';
			}
			else
			{
				css = (i == last) ? 'x-grid3-cell-last ' : '';
			}

			meta =
			{
				id: column.id,
				style: column.style,
				css: css,
				attr: "",
				cellAttr: ""
			};

			// Need to set this after, because we pass meta to the renderer
			meta.value = column.renderer.call(column.scope, record.data[column.name], meta, record, rowIndex, i, store);

			if(Ext.isEmpty(meta.value))
			{
				meta.value = Z8.emptyString;
				meta.tip = '';
			}
			else
			{
				meta.tip = Ext.util.Format.htmlEncode(meta.value);
			}

			if(this.markDirty && record.dirty && record.modified[column.name] != null)
			{
				meta.css += ' x-grid3-dirty-cell';
			}

			colBuffer[i] = cellTpl.apply(meta);
		}

		row.className = '';

		if(this.grid.stripeRows && ((rowIndex + 1) % 2 === 0))
		{
			cls.push('x-grid3-row-alt');
		}

		if(this.getRowClass)
		{
			rowParams.cols = colCount;
			cls.push(this.getRowClass(record, rowIndex, rowParams, store));
		}

		this.fly(row).addClass(cls).setStyle(rowParams.rowStyle);
		rowParams.cells = colBuffer.join('');
		row.innerHTML = this.templates.rowInner.apply(rowParams);

		this.fireEvent('rowupdated', this, rowIndex, record);
	},

	getGroupText: function(group)
	{
		var colIndex = this.cm.findColumnIndex(group.field);
		var column = this.cm.config[colIndex];
		
		var groupRenderer = column.groupRenderer || column.renderer;
		var prefix = this.showGroupName ? (column.groupName || column.header) + ': ' : '';
		var text = groupRenderer != null ? groupRenderer.call(column.scope, group.value, {}, null, 0, colIndex, this.ds) : String(v);

		if(text === '' || text === '&#160;')
		{
			text = column.emptyGroupText || this.emptyGroupText;
		}

		return prefix + text;
	},

	findGroupById: function(groups, id)
	{
		for(var i = 0; i < groups.length; i++)
		{
			var group = groups[i];
			
			var groupId = this.constructId(group);
			
			if(groupId == id)
			{
				return group;
			}
			else if(!Z8.isEmpty(group.groups))
			{
				var result = this.findGroupById(group.groups, id);
				
				if(result != null)
				{
					return result;
				}
			}
		}
		
		return null;
	},
	
	renderGroup: function(group, columns, store, colCount, stripe)
	{
		group.groupId = Ext.util.Format.htmlEncode(this.constructId(group));
		group.text = this.getGroupText(group);
		group.cls = ''; //this.state[gid] ? '' : 'x-grid-group-collapsed',
		group.padding = group.level * 10;
		group.color = ' color:' + this.colors[group.level % this.colors.length] + ';';
		group.style = group.color;
		
		for(var i = 0; i < group.records.length; i++)
		{
			var record = group.records[i];
			record.groups.push(group.groupId);
		}
			
		var buffer = [];
		buffer[buffer.length] = this.doGroupStart(group);
		
		if(!Z8.isEmpty(group.groups))
		{
			for(var i = 0; i < group.groups.length; i++)
			{
				buffer[buffer.length] = this.renderGroup(group.groups[i], columns, store, colCount, stripe);
			}
		}
		else
		{
			buffer[buffer.length] = this.doRenderFn.call(this, columns, group.records, store, group.start, colCount, stripe);
		}

		buffer[buffer.length] = this.doGroupEnd(group);
		
		return buffer.join('');
	},

	doRender: function(columns, records, store, startRow, colCount, stripe)
	{
		if(records.length < 1)
		{
			return '';
		}

		if(!this.canGroup() || this.isUpdating)
		{
			return Ext.grid.GroupingView.superclass.doRender.apply(this, arguments);
		}

		var groups = store.getGroups();
		
		var buffer = [];
		
		for(var i = 0; i < groups.length; i++)
		{
			var group = groups[i];
			
			for(var j = 0; j < group.records.length; j++)
			{
				group.records[j].groups = [];
			}

			buffer[buffer.length] = this.renderGroup(group, columns, store, colCount, stripe);
		}
		
		return buffer.join('');
	},

	getGroupFields: function()
	{
		return this.ds.getGroupFields();
	},
	
	getAllGroups: function()
	{
		var result = [];
		
		if(!this.canGroup())
		{
			return result;
		}
		
		var groups = this.getGroups();

		for(var i = 0; i < groups.length; i++)
		{
			this.collectGroups(result, groups[i], 1);
		}
		
		return result;
	},

	collectGroups: function(result, group, level)
	{
		result[result.length] = group;

		var nodes = group.childNodes[1].childNodes;

		if(level != this.getGroupFields().length)
		{
			for(var i = 0; i < nodes.length; i++)
			{
				this.collectGroups(result, nodes[i], level + 1);
			}
		}
	},

	getRows: function()
	{
		if(!this.canGroup())
		{
			return Ext.grid.GroupingView.superclass.getRows.call(this);
		}

		var rows = [];
		var groups = this.getGroups();

		for(var i = 0; i < groups.length; i++)
		{
			this.collectRows(rows, groups[i], 1);
		}
		
		return rows;
	},
	
	collectRows: function(rows, group, level)
	{
		var nodes = group.childNodes[1];
		
		if(nodes == null)
		{
			return;
		}
		
		nodes = nodes.childNodes;

		if(level == this.getGroupFields().length)
		{
			for(var i = 0; i < nodes.length; i++)
			{
				rows[rows.length] = nodes[i];
			}
		}
		else
		{
			for(var i = 0; i < nodes.length; i++)
			{
				this.collectRows(rows, nodes[i], level + 1);
			}
		}
	},

	getColumnModel: function()
	{
		return this.cm;
	},

	getSummaryData: function(store, group)
	{
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
					var result = reader.extractValues(data, fields.items, fields.length);
					result[Z8.totalProperty] = data.total;
					return result;
				}
			}
		}
		return null;
	},
	
	doGroupStart: function(group)
	{
		var summary = this.getSummaryData(this.ds, group);
		group.count = summary != null ? summary[Z8.totalProperty] : group.records.length;
		return this.startGroup.apply(group);
	},

	toggleGroup: function(group, expanded)
	{
		group = Ext.fly(group).hasClass('x-grid-group-offset') ? group.parentNode : group;
		Z8.grid.GridView.superclass.toggleGroup.call(this, group, expanded);
	},
	
	findRecord: function(el)
	{
		var index = this.findRowIndex(el);
		return index != -1 ? this.grid.getStore().getAt(index) : null;
	},

	onRemove: function(ds, record, index, isUpdate)
	{
		Z8.grid.GridView.superclass.onRemove.apply(this, arguments);

		if(record.groups != null)
		{
			for(var i = record.groups.length - 1; i >= 0; i--)
			{
				var groupNode = document.getElementById(record.groups[i]);

				if(groupNode != null && groupNode.childNodes[1].childNodes.length < 1)
				{
					Ext.removeNode(groupNode);
				}
				else
				{
					break;
				}
			}
		}
	},

	handleHdMove: function(e)
	{
		var header = this.findHeaderCell(this.activeHdRef);
		
		if (header && !this.headersDisabled)
		{
			var handleWidth = this.splitHandleWidth || 5;
			var activeRegion = this.activeHdRegion;
			var headerStyle = header.style;
			var colModel = this.cm;
			var cursor = '';
			var pageX = e.getPageX();
				
			if(this.grid.enableColumnResize !== false)
			{
				var activeHeaderIndex = this.activeHdIndex;
				var previousVisible = this.getPreviousVisible(activeHeaderIndex);
				var currentResizable = colModel.isResizable(activeHeaderIndex);
				var previousResizable = previousVisible && colModel.isResizable(previousVisible);
				var inLeftResizer = pageX - activeRegion.left <= handleWidth;
				var inRightResizer = activeRegion.right - pageX <= (!this.activeHdBtn ? handleWidth : 2);
				
				if(inLeftResizer && previousResizable)
				{
					cursor = Ext.isAir ? 'move' : Ext.isWebKit ? 'e-resize' : 'col-resize'; 
				}
				else if(inRightResizer && currentResizable)
				{
					cursor = Ext.isAir ? 'move' : Ext.isWebKit ? 'w-resize' : 'col-resize';
				}
			}
			
			headerStyle.cursor = cursor;
			header.firstChild.style.cursor = cursor;
		}
	}
});