/**
*
* Honest critisism is hard to take, particularly from a relative, a friend, an acquaintance, or a stranger.
*
**/

var ExpandThenCreate = 1;
var SelectAdded = 2;

Ext.tree.TreeEventModel.prototype.beforeEvent = function(e)
{
	var node = this.getNode(e);

	if(this.disabled)
	{
		e.stopEvent();
		return false;
	}
	
	return 	 node != null && node.ui != null;
};

Z8.tree.TreePanel = Ext.extend(Ext.tree.TreePanel,
{
	readAction: 'read',
	
	treePanel: true,
	
	lines: false,
	icons: true,
	useArrows: true,

	forceFit: true,
	autoFill: true,
	
	columnLines: true,
	rowLines: false,
	
	hideHeaders: false,
	
	scrollOffset: 17,
	borderWidth: 2,

	loadOnExpand: true,
	
	tdClass: 'x-grid3-cell',
	hdCls: 'x-grid3-hd',

	markDirty: true,

	cellSelectorDepth: 4,
	cellSelector : 'td.x-grid3-cell',

	rowSelectorDepth : 10,
	rowSelector : 'div.x-tree-node-el',

	rowLookupCls: 'tree-node-id-',
	
	colRe: new RegExp("x-grid3-td-([^\\s]+)", ""),

	enableHdMenu : true,

	sortClasses: ["sort-asc", "sort-desc"],

	sortFolders: true,
	sortAll: false,
	
	columnsText: 'Столбцы',
	sortAscText: 'По возрастанию',
	sortDescText: 'По убыванию',

	enableColumnHide : true,
	enableColumnMove : true,

	enableDragDrop : false,

	stopEditing : Ext.emptyFn,

	minColumnWidth: 25,

	getNodeTarget: function(e)
	{
		var target = e.getTarget('.x-tree-node-icon', 1);
		
		if(!target)
		{
			target = e.getTarget('.x-tree-node-el');
		}
		return target;
	},

	createNode: function(attributes)
	{
		if(this.baseAttrs)
		{
			Ext.applyIf(attributes, this.baseAttrs);
		}

		attributes.uiProvider = Z8.tree.TreeNodeUI;

		if(attributes.nodeType)
		{
			return new Ext.tree.TreePanel.nodeTypes[attributes.nodeType](attributes);
		}
		else
		{
			
			if(!attributes.leaf && this.loadOnExpand)
			{
				return new Ext.tree.AsyncTreeNode(attributes);
			}
			return new Ext.tree.TreeNode(attributes);
		}
	},

	initComponent: function()
	{
		Z8.tree.TreePanel.superclass.initComponent.call(this);

		if(this.handlers != null)
		{
			for(var i = 0; i < this.handlers.length; i++)
			{
				var handler = this.handlers[i];
				var scope = handler.scope;
				scope.mon(this, handler.name, handler.handler, scope);
			}
		}

		this.eventModel.getNodeTarget = this.getNodeTarget;
		
		this.setRootNode(this.createNode({ id: "" }));

		var store = this.getStore();
		this.store.query.parentId = '';

		if(store != null)
		{
			store.on('load', this.onStoreLoaded, this);
			store.on('update', this.onUpdate, this);
			store.on('datachanged', this.onDataChange, this);
		}
		
		if(this.columnLines)
		{
			this.cls = (this.cls || '') + ' x-grid-with-col-lines';
		}

		if(this.rowLines)
		{
			this.cls = (this.cls || '') + ' x-grid-with-row-lines';
		}

		this.splitHandleWidth = this.splitHandleWidth || 5;

		this.autoWidth = false;

		if(Ext.isArray(this.columns))
		{
			this.colModel = new Ext.grid.ColumnModel(this.columns);
			delete this.columns;
		}

		if(this.cm)
		{
			this.colModel = this.cm;
			delete this.cm;
		}

		for(var i = 0; i < this.colModel.getColumnCount(); i++)
		{
			var column = this.colModel.getColumnAt(i);
			
			if(column.init != null)
			{
				column.init(this);
			}
		}
		
		this.addEvents(
//			'click',				// {Node} node, {Ext.EventObject} e
			'dblclick',				// {Node} node, {Ext.EventObject} e
			'contextmenu',			// {Node} node, {Ext.EventObject} e
//			'mousedown',			// {Ext.EventObject} e
			'mouseup',				// {Ext.EventObject} e
			'mouseover',			// {Ext.EventObject} e
			'mouseout',				// {Ext.EventObject} e
			'keypress',				// {Ext.EventObject} e
			'keydown',				// {Ext.EventObject} e
			'cellmousedown',		// {Tree} this, {Node} node, {Number} column, {Ext.EventObject} e
			'rowmousedown',			// {Tree} this, {Node} node, {Ext.EventObject} e
			'headermousedown',		// {Tree} this, {Node} node, {Number} column, {Ext.EventObject} e
			'cellclick',			// {Tree} this, {Node} node, {Number} column, {Ext.EventObject} e
			'celldblclick',			// {Tree} this, {Node} node, {Number} column, {Ext.EventObject} e
			'rowclick',				// {Tree} this, {Node} node, {Ext.EventObject} ef
			'rowdblclick',			// {Tree} this, {Node} node, {Ext.EventObject} e
			'headerclick',			// {Tree} this, {Number} column, {Ext.EventObject} e
			'headerdblclick',		// {Tree} this, {Number} column, {Ext.EventObject} e
			'rowcontextmenu',		// {Tree} this, {Node} node, {Ext.EventObject} e
			'cellcontextmenu',		// {Tree} this, {Node} node, {Number} column, {Ext.EventObject} e
			'headercontextmenu',	// {Tree} this, {Number} column, {Ext.EventObject} e
			'bodyscroll',			// {Number} left, {Number} top
			'columnresize',			// {Number} index, {Number} width
			'columnmove',			// {Number} oldIndex, {Number} newIndex
			'sortchange',			// {Tree} this, {Number} colIndex
			'refresh',				// {Tree} this, {Number} colIndex
			'viewReady'
		);
		
		this.initTemplates();
		this.onStoreLoaded();
	},
	
	getSelectionModel: function()
	{
		if(!this.selModel)
		{
			this.selModel = new Z8.tree.DefaultSelectionModel();
		}
		return this.selModel;
	},

	getColumnModel: function()
	{
		return this.colModel;
	},

	getStore: function()
	{
		return this.store;
	},

	getView: function()
	{
		return this;
	},

	getRowEditor: function()
	{
		if(this.hasRowEditor == null && this.plugins != null)
		{
			this.hasRowEditor = false;
			
			for(var i = 0; i < this.plugins.length; i++)
			{
				if(this.plugins[i].isRowEditor)
				{
					this.rowEditor = this.plugins[i];
					this.hasRowEditor = true;
					break;
				}
			}
		}
		
		return this.rowEditor;
	},

	showAsTree: function()
	{
		var store = this.getStore();
		store.query.showAsTree = true;
		
		store.refresh();
	},
	
	showAsGrid: function()
	{
		var store = this.getStore();
		store.query.showAsTree = false;

		store.refresh();
	},

	stopEditing: function()
	{
		var rowEditor = this.getRowEditor();
		
		if(rowEditor != null)
		{
			rowEditor.stopEditing(true);
		}
	},

	startEditing: function(record)
	{
		var rowEditor = this.getRowEditor();
		
		if(rowEditor != null)
		{
			rowEditor.startEditing(record, true);
		}
	},

	isDirty: function()
	{
		var store = this.getStore();
		
		if(store == null)
		{
			return false;
		}
		
		if(store.isDirty())
		{
			return true;
		}
			
		var rowEditor = this.getRowEditor();
		
		if(rowEditor != null && rowEditor.isVisible())
		{
			return rowEditor.isDirty();
		}
		
		return false;
	},

	getRowIndex: function(record)
	{
		return this.getNodeById(record.id);
	},

	getSelectedNode: function()
	{
		return this.getSelectionModel().getSelectedNode();
	},

	getSelectedNodes: function()
	{
		var selModel = this.getSelectionModel();
		
		if(selModel.getSelectedNodes != null)
		{
			return selModel.getSelectedNodes();
		}
		else
		{
			var node = this.getSelectedNode();
			return node != null ? [ node ] : [];
		}
	},

	findRecord: function(el)
	{
		var node = this.findNode(el);
		return node != null ? this.getStore().getById(node.id) : null;
	},
	
	getSelectedRecord: function()
	{
		var node = this.getSelectedNode();
		return node != null ? this.getStore().getById(node.id) : null;
	},

	getSelectedRecords: function()
	{
		var records = [];

		var nodes = this.getSelectedNodes();
		
		for(var i = 0; i < nodes.length; i++)
		{
			records.push(this.getStore().getById(nodes[i].id));
		}
		return records;
	},

	select: function(node)
	{
		node.select();
	},
	
	getRecord: function(node)
	{
		return this.getStore().getById(node.id);
	},

	canAddRecord: function()
	{
		return true;
	}, 

	canDeleteRecord: function()
	{
		return this.getSelectedNodes().length == 1;
	}, 

	createRecord: function(node, startEdit, success, error, scope)
	{
		var parameters = [node, SelectAdded, true, startEdit, success, error, scope];
		var onSuccess = this.onRecordsLoaded.createDelegate(this, parameters, true);
		var onFailure = this.onRecordsLoadFailed.createDelegate(this, parameters, true);

		var params = { xaction: 'new', parentId: node.isRoot ? "" : node.id };
		
		this.getStore().query.request(onSuccess, onFailure, params, this);
	},

	addRootRecord: function(startEdit, success, error, scope)
	{
		this.doAddRecord(null, startEdit, success, error, scope);
	},

	addRecord: function(startEdit, success, error, scope)
	{
		this.doAddRecord(this.getSelectedNode(), startEdit, success, error, scope);
	},
	
	doAddRecord: function(node, startEdit, success, error, scope)
	{
		if(node == null)
		{
			node = this.root;
		}

		if(!node.loaded && !node.loading)
		{
			this.onBeforeLoad(node, ExpandThenCreate, startEdit, success, error, scope);	
		}
		else
		{
			this.createRecord(node, startEdit, success, error, scope);
		}
	},
	
	copyRecord: function(startEdit, success, error, scope)
	{
		var node = this.getSelectedNode();
		
		node = node == null ? this.root : node.parentNode;
		
		var record = this.getSelectedRecord();
		
		var parameters = [node, SelectAdded, true, startEdit, success, error, scope];
		var onSuccess = this.onRecordsLoaded.createDelegate(this, parameters, true);
		var onFailure = this.onRecordsLoadFailed.createDelegate(this, parameters, true);

		var params = { xaction: 'copy', source: record.id, data: Ext.encode(record.data), parentId: node != this.root ? parent.id : "" };
		
		this.getStore().query.request(onSuccess, onFailure, params, this);
	},

	deleteRecord: function()
	{
		var node = this.getSelectedNode();

		node.cascade(function(node)
		{
			var record = this.getRecord(node);
			this.getStore().remove(record);
			return true;
		}, this);

		node.destroy();
	},

	saveRecords: function()
	{
		this.getStore().save();
	},

	selectRecord: function(record)
	{
		if(record != null)
		{
			var node = this.getRowIndex(record);
			if(node != null)
			{
				node.select();
			}
		}
		else
		{
			this.getSelectionModel().clearSelections();
		}
	},

	hasNextToSelect: function()
	{
		return true;
	},

	hasPreviousToSelect: function()
	{
		var node = this.getSelectionModel().getSelectedNode();

		if(node == null || node == this.root)
		{
			return false;
		}

		return !node.isFirst() || node.parentNode != this.root;
	},

	selectNext: function()
	{
		return this.getSelectionModel().selectNext();
	},

	selectPrevious: function()
	{
		return this.getSelectionModel().selectPrevious();
	},

	selectFirst: function()
	{
		var node = this.root.firstChild;

		if(node != null)
		{
			node.select();
		}
	},

	selectLast: function()
	{
		throw "Z8.tree.TreePanel.selectLast() is not implemented yet.";
	},

	clearSelections: function()
	{
		this.getSelectionModel().clearSelections();
	},

	onBeforeLoad: function(node, parameter, startEdit, success, error, scope) 
	{
		if(!node.isRoot)
		{
			var query = this.getStore().query;
			var parameters = [ node, parameter, false, startEdit, success, error, scope ];
			 
			var onSuccess = this.onRecordsLoaded.createDelegate(this, parameters, true);
			var onFailure = this.onRecordsLoadFailed.createDelegate(this, parameters, true);
			var params = { xaction: this.readAction, parentId: node.id }
			
			query.request(onSuccess, onFailure, params, this);
		}

		return true; 
	},

	onRecordsLoaded: function(response, node, parameter, newRecord, startEdit, success, error, scope)
	{
		var store = this.getStore();
		var result = store.reader.readRecords(response);

		if(result.records.length > 0)
		{
			this.stopEditing();

			var records = result.records;
			var nodes = this.createNodes(records, newRecord);
			store.add(records);
			node.appendChild(nodes);

			if(parameter == SelectAdded)
			{
				if(node.leaf)
				{
					node.leaf = false;
					node.loaded = true;
				}
				
				var callback = null;

				if(startEdit)
				{
					callback = function() { this.startEditing(records[0]); }
				}
				
				node.expand(false, true, callback, this);

				nodes[0].select();
			}

			if(success != null)
			{
				success.call(scope, result.info);
			}
		}

		if(node.loadComplete != null)
		{
			if(!node.loaded)
			{
				node.loadComplete(false);
			}
		}

		if(parameter == ExpandThenCreate)
		{
			this.createRecord(node, startEdit, success, error, scope);
		}
	},

	onRecordsLoadFailed: function(info, node, parameter, newRecord, startEdit, success, error, scope)
	{
		node.loaded = false;
		node.loading = false;
		node.expanded = false;
		node.ui.afterLoad(this);
		node.collapse();
		
		if(error != null)
		{
			error.call(scope, info);
		}
	},
	
	createNodes: function(records, newRecord)
	{
		var nodes = [];

		var store = this.getStore();
		
		var tree = store.query.showAsTree == null || store.query.showAsTree;
		
		var childrenProperty = store.childrenProperty;
		
		for(var i = 0; i < records.length; i++)
		{
			var record = records[i];
			
			if(newRecord)
			{
				record.dirty = true;
				record.phantom = true;
				record.modified = record.data;
			}
			
			var attributes = { id: record.id, leaf: !tree || !record.get(childrenProperty), parameters: record.json.parameters };
			var node = this.createNode(attributes); 
			node.on('beforeload', this.onBeforeLoad, this);
			nodes.push(node);
		}
		return nodes;
	},

	onDataChange: function()
	{
		this.root.removeAll(true);
		var nodes = this.createNodes(this.getStore().data.items);
		this.root.appendChild(nodes);
		this.updateHeaderSortState();
		this.syncFocusEl(0);
	},

	onUpdate: function(store, record)
	{
		var node = this.getRowIndex(record);
		
		if(node != null)
		{
			this.refreshNode(node, record);
		}
	},

	onStoreLoaded: function(store, records, options)
	{
		this.root.loadComplete(false);
	},

	setRootNode: function(root)
	{
		var root = Z8.tree.TreePanel.superclass.setRootNode.call(this, root);
		root.ui = new Z8.tree.TreeNodeUI(root);
		return root;
	},
	
	initTemplates: function()
	{
		this.templates = this.templates || {};
		
		if(!this.templates.master)
		{
			this.templates.master = new Ext.Template(
					'<div class="x-grid3" hidefocus="true">',
						'<div class="x-grid3-viewport">',
							'<div class="x-grid3-header"><div class="x-grid3-header-inner"><div class="x-grid3-header-offset" style="{ostyle}">{header}</div></div><div class="x-clear"></div></div>',
							'<div class="x-grid3-scroller"><div class="x-grid3-body" style="{bstyle}">',
							'<ul class="x-tree-node-ct ' + (this.useArrows ? 'x-tree-arrows' : this.lines ? 'x-tree-lines' : 'x-tree-no-lines') + '"></ul>',
							'</div><a class="x-grid3-focus" tabIndex="-1"></a></div>',
						'</div>',
						'<div class="x-grid3-resize-marker">&#160;</div>',
						'<div class="x-grid3-resize-proxy">&#160;</div>',
					'</div>'
			);
		}

		if(!this.templates.header)
		{
			this.templates.header = new Ext.Template(
					'<table border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
					'<thead><tr class="x-grid3-hd-row">{cells}</tr></thead>',
					'</table>'
					);
		}

		if(!this.templates.hcell)
		{
			this.templates.hcell = new Ext.Template(
					'<td class="x-grid3-hd x-grid3-cell x-grid3-td-{id} {cls}" style="{style}" {attributes}>',
						'<div {tooltip} {attr} class="x-grid3-hd-inner x-unselectable x-grid3-hd-{id}" unselectable="on" style="{istyle}">',
							 this.enableHdMenu ? '<a class="x-grid3-hd-btn"></a>' : '',
							'{value}<img class="x-grid3-sort-icon" src="', Ext.BLANK_IMAGE_URL, '" />',
						'</div>',
					'</td>'
			);
		}

		if(!this.templates.body)
		{
			this.templates.body = new Ext.Template('{rows}');
		}

		var rowInner = [
			'<table class="x-grid3-row-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
			'<tbody>',
			'<tr>{cells}</tr>',
			'</tbody></table>'
		].join('');

		if(!this.templates.rowInner)
		{
			this.templates.rowInner = new Ext.Template(rowInner);
		}

		if(!this.templates.row)
		{
			this.templates.row = new Ext.Template(
				'<li class="x-tree-node x-grid3-row" style="border:0px;">',
					'<div ext:tree-node-id="{nodeId}" id="', this.rowLookupCls, '{nodeId}" class="x-tree-node-el x-tree-node-leaf x-unselectable {cls}" style="{tstyle}" unselectable="on">',
						rowInner,
					'</div>',
				'</li>'
				);
		}

		if(!this.templates.rows)
		{
			this.templates.rows = new Ext.Template('<ul class="x-tree-node-ct" style="display:none;"></ul>');
		}
		
		if(!this.templates.cell)
		{
			this.templates.cell = new Ext.Template(
					'<td class="x-grid3-col x-grid3-cell x-grid3-td-{columnId} {cls}" style="{style}" tabIndex="0" {cellAttr}>',
						'<div class="x-grid3-cell-inner x-unselectable x-grid3-col-{columnId}" style="{innerStyle}" unselectable="on" {attr}>',
							'{cellMarkup}',
						'</div>',
					'</td>'
					);
		}

		for(var key in this.templates)
		{
			var template = this.templates[key];
			
			if(template && typeof template.compile == 'function' && !template.compiled)
			{
				template.disableFormats = true;
				template.compile();
			}
		}
	},
	
	renderHeaders: function()
	{
		var buffer = []; 
		var columnCount = this.colModel.getColumnCount();
		var lastIndex = columnCount - 1;
			
		for(var i = 0; i < columnCount; i++)
		{
			var parameter = {
				id: this.colModel.getColumnId(i),
				value: this.colModel.getColumnHeader(i) || "",
				style: this.getColumnStyle(i, true),
				tooltip: this.getColumnTooltip(i),
				cls: i === 0 ? ' x-grid3-cell-first ' : (i == lastIndex ? ' x-grid3-cell-last ' : '')
			};

			if(this.colModel.config[i].align == 'right')
			{
				parameter.istyle = 'padding-right:16px';
			}
			else
			{
				delete parameter.istyle;
			}

			buffer[buffer.length] = this.templates.hcell.apply(parameter);
		}

		return this.templates.header.apply({
			cells: buffer.join(""), 
			tstyle:'width:' + this.getTotalWidth() + 'px;'
		});
	},

	getColumnWidth: function(index)
	{
		return this.colModel.getColumnWidth(index);
	},

	getTotalWidth: function()
	{
		return this.colModel.getTotalWidth();
	},

	getOffsetWidth: function()
	{
		return this.getTotalWidth() + this.scrollOffset;
	},

	getColumnAt: function(index)
	{
		return this.colModel.getColumnAt(index);
	},
	
	getColumnStyle: function(index, isHeader)
	{
		var column = this.getColumnAt(index);
		
		var style = !isHeader ? (column.css || '') :	'';

		style += 'width:' + this.getColumnWidth(index) + 'px;';

		if(column.hidden)
		{
			style += 'display:none;';
		}

		var align = column.align;

		if(align)
		{
			style += 'text-align:' + align + ';';
		}

		return style;
	},

	getColumnTooltip: function(index)
	{
		var tooltip = this.colModel.getColumnTooltip(index);
		
		if(tooltip)
		{
			if(Ext.QuickTips.isEnabled())
			{
				return 'ext:qtip="' + tooltip + '"';
			}
			else
			{
				return 'title="' + tooltip +	'"';
			}
		}
		return "";
	},

	getScrollState: function()
	{
		return {left: this.scroller.dom.scrollLeft, top: this.scroller.dom.scrollTop};
	},

	restoreScroll: function(state)
	{
		this.scroller.dom.scrollLeft = state.left;
		this.scroller.dom.scrollTop = state.top;
	},

	scrollToTop: function()
	{
		this.scroller.dom.scrollTop = 0;
		this.scroller.dom.scrollLeft = 0;
	},
	
	syncScroll: function()
	{
		this.syncHeaderScroll();
		this.fireEvent("bodyscroll", this.scroller.dom.scrollLeft, this.scroller.dom.scrollTop);
	},

	syncHeaderScroll: function()
	{
		this.innerHd.dom.scrollLeft = this.scroller.dom.scrollLeft;
		this.innerHd.dom.scrollLeft = this.scroller.dom.scrollLeft; // second time for IE (1/2 time first fails, other browsers ignore)
	},

	getTreeEl: function()
	{
		return this.body;
	},

	fly: function(el)
	{
		if(!this._flyweight)
		{
			this._flyweight = new Ext.Element.Flyweight(document.body);
		}
		this._flyweight.dom = el;
		return this._flyweight;
	},
	
	findCell: function(el)
	{
		if(!el)
		{
			return false;
		}
		return this.fly(el).findParent(this.cellSelector, this.cellSelectorDepth);
	},

	findCellIndex: function(el, requiredCls)
	{
		var cell = this.findCell(el);
		if(cell != null && (!requiredCls || this.fly(cell).hasClass(requiredCls)))
		{
			return this.getCellIndex(cell);
		}
		return -1;
	},

	findHeaderCell: function(el)
	{
		var cell = this.findCell(el);
		return cell != null && this.fly(cell).hasClass(this.hdCls) ? cell : null;
	},

	findHeaderIndex: function(el)
	{
		return this.findCellIndex(el, this.hdCls);
	},

	getHeaderCell: function(index)
	{
	  return this.mainHd.dom.getElementsByTagName('td')[index];
	},

	getCellIndex: function(el)
	{
		if(el != null)
		{
			var m = el.className.match(this.colRe);
			if(m && m[1])
			{
				return this.colModel.getIndexById(m[1]);
			}
		}
		return -1;
	},

	getRows: function()
	{
		return this.mainBody.dom.getElementsByTagName('li');
	},

	getRow: function(node)
	{
		return node.ui != null ? node.ui.elNode : null;
	},

	getEditorParent : function()
	{
		return this.scroller.dom;
	},

	getCell: function(row, column)
	{
		if(row)
		{	
			var table = row.firstChild;
			return table.rows[0].childNodes[column];
		}
		return null;
	},
	
	handleHdDown: function(e, t)
	{
		if(Ext.fly(t).hasClass('x-grid3-hd-btn'))
		{
			Z8.stopEvent(e);
			
			var headerCell = this.findHeaderCell(t);
			Ext.fly(headerCell).addClass('x-grid3-hd-menu-open');
			
			var index = this.getCellIndex(headerCell);
			
			this.hdCtxIndex = index;
			
			var col = this.colModel.columns[index];
			
			if (col.hideable === false)
			{
				this.btnShowHideCurrent.disable();
			}
			else
			{
				this.btnShowHideCurrent.enable();
			}
			
			var isSortable = this.colModel.isSortable(index);
			
			this.hmenu.items.get("asc").setDisabled(!isSortable);
			this.hmenu.items.get("desc").setDisabled(!isSortable);
			
			this.hmenu.on("hide", function() 
			{
				Ext.fly(headerCell).removeClass('x-grid3-hd-menu-open');
			}, this, {single:true});
			
			this.hmenu.show(t, "tl-bl?");
		}
	},

	handleHdOver: function(e, t)
	{
		var headerCell = this.findHeaderCell(t);
		
		if(headerCell && !this.headersDisabled)
		{
			this.activeHd = headerCell;
			this.activeHdIndex = this.getCellIndex(headerCell);
			
			var fly = this.fly(headerCell);

			this.activeHdRegion = fly.getRegion();

			if(!this.colModel.isMenuDisabled(this.activeHdIndex))
			{
				fly.addClass("x-grid3-hd-over");
				this.activeHdBtn = fly.child('.x-grid3-hd-btn');
				if(this.activeHdBtn)
				{
					this.activeHdBtn.dom.style.height = (headerCell.firstChild.offsetHeight-1)+'px';
				}
			}
		}
	},

	handleHdMove: function(e, t)
	{
		if(this.activeHd && !this.headersDisabled)
		{
			var region = this.activeHdRegion;
			var x = e.getPageX();
			
			if(x - region.left <= this.splitHandleWidth && this.colModel.isResizable(this.activeHdIndex - 1))
			{
				this.activeHd.style.cursor = Ext.isAir ? 'move' : Ext.isWebKit ? 'e-resize' : 'col-resize'; // col-resize not always supported
			}
			else if(region.right - x <= (!this.activeHdBtn ? this.splitHandleWidth : 2) && this.colModel.isResizable(this.activeHdIndex))
			{
				this.activeHd.style.cursor = Ext.isAir ? 'move' : Ext.isWebKit ? 'w-resize' : 'col-resize';
			}
			else
			{
				this.activeHd.style.cursor = '';
			}
		}
	},

	handleHdOut: function(e, t)
	{
		var headerCell = this.findHeaderCell(t);
		
		if(headerCell && (!Ext.isIE || !e.within(headerCell, true)))
		{
			this.activeHd = null;
			this.fly(headerCell).removeClass("x-grid3-hd-over");
			headerCell.style.cursor = '';
		}
	},

	beforeColMenuShow: function()
	{
		this.colMenu.removeAll();
		
		for(var i = 0, columnCount = this.colModel.getColumnCount(); i < columnCount; i++)
		{
			var column = this.getColumnAt(i);
			
			if(column.fixed !== true && column.hideable !== false)
			{
				this.colMenu.add(new Ext.menu.CheckItem({
					itemId: "col-" + this.colModel.getColumnId(i),
					text: column.header,
					checked: !this.colModel.isHidden(i),
					hideOnClick: false,
					disabled: column.hideable === false
				}));
			}
		}
	},

	onHeaderClick: function(grid, index)
	{
		if(this.headersDisabled || !this.colModel.isSortable(index))
		{
			return;
		}

		this.stopEditing(true);
		
		this.doSort(index);
	},
	
	doSort: function(index, sortDirection, node, deep)
	{
		var column = this.getColumnAt(index); 

		this.lastSortedColumn = column.id;
		
		if(sortDirection)
		{
			column.sortDirection = sortDirection;
		}
		else
		{
			column.sortDirection = (column.sortDirection || 'DESC').toggle('ASC', 'DESC');
		}

		var store = this.getStore();
		var tree = store.query.showAsTree == null || store.query.showAsTree;

		if(tree)
		{
			if(node)
			{
				if(deep)
				{
					node.cascade(this.sortNode, null, [column, false]);
				}
				else
				{
					node.cascade(this.sortNode, null, [column, false, true]);
				}
			}
			else
			{
				this.root.cascade(this.sortNode, null, [column, true]);
			}
			
			this.updateHeaderSortState();
		}
		else
		{
			store.sort(column.id, column.sortDirection);
		}
		
		this.fireEvent('sortchange', this, index);
//		this.syncFocusEl(0);
	},
	
	sortNode: function(column, onlyExpanded, onlyOnce)
	{
		if(!onlyExpanded || this.isExpanded())
		{
			this.sort(function(node1, node2) 
			{ 
				var leaf1 = !node1.isExpandable();
				var leaf2 = !node2.isExpandable();
				
				var leafs = leaf1 && leaf2;
				var folders = !leaf1 && !leaf2;
				var mixed = !leafs && !folders;
				
				var tree = node1.getOwnerTree();
				
				if(tree.sortAll || tree.sortFolders && folders || leafs)
				{
					var store = tree.getStore();
					var record1 = store.getById(node1.id);
					var record2 = store.getById(node2.id);

					var value1 = record1.get(column.id);
					var value2 = record2.get(column.id);
					
					var asc = column.sortDirection == 'ASC';
					
					if(value1 < value2)
					{
						return asc ? -1 : 1;
					}
					else if(value1 > value2)
					{
						return asc ? 1 : -1;
					}

					if(node1.id < node2.id)
					{
						return asc ? -1 : 1;
					}

					return asc ? 1 : -1;
				}
				else if(tree.sortFolders && mixed)
				{
					return (leaf1 && !leaf2) ? 1 : -1;
				}
				
				return 0;
			});
		}
		return !onlyOnce;
	},
	
	onBeforeExpandNode: function(node, deep, animate)
	{
		if(this.lastSortedColumn)
		{
			var index = this.colModel.getIndexById(this.lastSortedColumn);
			var column = this.getColumnAt(index); 
			this.doSort(index, column.sortDirection, node, deep);
		}
		return true;
	},
	
	onExpandNode: function(node)
	{
//		this.updateColumnWidth(0, this.getColumnWidth(0));
	},
	
	onBeforeExpandNode: function(node)
	{
		this.stopEditing();
	},

	onBeforeCollapseNode: function(node)
	{
		this.stopEditing();
	},

	updateHeaderSortState : function()
	{
		if(this.lastSortedColumn)
		{
			var index = this.colModel.getIndexById(this.lastSortedColumn);
			
			if(index != -1)
			{
				var column = this.colModel.getColumnAt(index);
				this.updateSortIcon(index, column.sortDirection);
			}
		}
		else
		{
			this.updateSortIcon(-1);
		}
	},

	updateSortIcon : function(index, dir)
	{
		var headers = this.mainHd.select('td').removeClass(this.sortClasses);
		
		if(index != -1)
		{
			headers.item(index).addClass(this.sortClasses[dir == "DESC" ? 1 : 0]);
		}
	},

	handleWheel: function(e)
	{
		Z8.stopEvent(e);
	},

	onColumnSplitterMoved: function(index, width)
	{
		this.userResized = true;

		if(this.autoFill)
		{
			this.forceFit = false;
			this.scroller.setStyle('overflow-x', 'auto');
		}
		
		this.colModel.setColumnWidth(index, width, true);

		if(this.forceFit)
		{
			this.fitColumns(true, false, index);
			this.updateAllColumnWidths();
		}
		else
		{
			this.updateColumnWidth(index, width);
			this.syncHeaderScroll();
		}

		this.fireEvent("columnresize", index, width);
	},

	handleHdMenuClick: function(item)
	{
		if(item.itemId != null)
		{
			switch(item.itemId)
			{
				case "asc":
					this.doSort(this.hdCtxIndex, "ASC");
					break;
				case "desc":
					this.doSort(this.hdCtxIndex, "DESC");
					break;
				default:
					var index = this.colModel.getIndexById(item.itemId.substr(4));
					if(index != -1)
					{
						if(item.checked && this.colModel.getColumnsBy(this.isHideableColumn, this).length <= 1)
						{
							return false;
						}
						this.colModel.setHidden(index, item.checked);
					}
			}
		}
		return true;
	},

	isHideableColumn: function(column)
	{
		return !column.hidden && !column.fixed;
	},

	findNode: function(el)
	{
		if(el)
		{
			var div = this.fly(el).findParent(this.rowSelector, this.rowSelectorDepth);
			
			if(div)
			{
				var id = Ext.fly(div).getAttribute('tree-node-id', 'ext');
				return this.getNodeById(id);
			}
		}
		
		return null;
	},

	focusRow: function(node)
	{
		this.focusCell(node, 0, false);
	},

	focusNode: function(node)
	{
		this.focusCell(node, 0, false);
	},
	
	focusCell: function(node, col, hscroll)
	{
		if(!this.focusTask)
		{
			this.focusTask = new Ext.util.DelayedTask();
		}
		this.focusTask.delay(1, this.doFocusCell, this, [node, col, hscroll]);
	},

	focus: function()
	{
		if(Ext.isGecko)
		{
			this.focusEl.focus();
		}
		else
		{
			this.focusEl.focus.defer(1, this.focusEl);
		}
	},

	doFocusCell: function(node, col, hscroll)
	{
		this.syncFocusEl(this.ensureVisible(node, col, hscroll));
		this.focus();
	},

	syncFocusEl: function(xy, hscroll)
	{
		if(this.focusEl != null)
		{
			this.focusEl.setXY(xy || this.scroller.getXY());
		}
	},

	ensureVisible : function(node, col, hscroll)
	{
		var resolved = this.resolveCell(node, col, hscroll);
		
		if(!resolved.row)
		{
			return;
		}

		var rowEl = resolved.row, 
			cellEl = resolved.cell,
			c = this.scroller.dom,
			ctop = 0,
			p = rowEl, 
			stop = this.viewport.dom;
			
		while(p && p != stop)
		{
			ctop += p.offsetTop;
			p = p.offsetParent;
		}
		
		ctop -= this.mainHd.dom.offsetHeight;
		stop = parseInt(c.scrollTop, 10);

		var cbot = ctop + rowEl.offsetHeight,
			ch = c.clientHeight,
			sbot = stop + ch;
			
		if(ctop < stop)
		{
		  c.scrollTop = ctop;
		}
		else if(cbot > sbot)
		{
			c.scrollTop = cbot-ch;
		}

		if(hscroll !== false)
		{
			var cleft = parseInt(cellEl.offsetLeft, 10);
			var cright = cleft + cellEl.offsetWidth;

			var sleft = parseInt(c.scrollLeft, 10);
			var sright = sleft + c.clientWidth;
			
			if(cleft < sleft)
			{
				c.scrollLeft = cleft;
			}
			else if(cright > sright)
			{
				c.scrollLeft = cright-c.clientWidth;
			}
		}
		
		return this.getResolvedXY(resolved);
	},

	resolveCell : function(node, col, hscroll)
	{
		col = (col !== undefined ? col : 0);

		var colCount = this.colModel.getColumnCount();
		var row = this.getRow(node);
		var cell;
		if(!(hscroll === false && col === 0))
		{
			while(col < colCount && this.colModel.isHidden(col))
			{
				col++;
			}
			cell = this.getCell(row, col);
		}

		return { node: node, row: row, cell: cell };
	},

	getResolvedXY : function(resolved)
	{
		if(!resolved.row)
		{
			return null;
		}

		return resolved.cell ? Ext.fly(resolved.cell).getXY() : [this.viewport.getX(), Ext.fly(resolved.row).getY()];
	},

	processEvent: function(name, e)
	{
		var target = e.getTarget();
		
		var headerIndex = this.findHeaderIndex(target);

		if(headerIndex != -1)
		{
			this.fireEvent('header' + name, this, headerIndex, e);
		}
		else
		{
			var node = this.findNode(target);
			var cellIndex = this.findCellIndex(target);
			
			if(node != null)
			{
				this.fireEvent('row' + name, this, node, e);
				
				if(cellIndex != -1)
				{
					this.fireEvent('cell' + name, this, node, cellIndex, e);
				}
			}
		}
	},

	onCellContextMenu: function(tree, node, cell, e)
	{
		Z8.stopEvent(e);
	},
	
	onClick: function(e)
	{
		if(e.target.className.indexOf('x-tree-ec-icon') == -1)
		{
          this.processEvent('click', e, true);
		}
	},

	onMouseDown: function(e)
	{
		if(e.target.className.indexOf('x-tree-ec-icon') == -1)
		{
			this.processEvent('mousedown', e);
		}
	},

	onContextMenu: function(e, t)
	{
		this.processEvent('contextmenu', e);
	},

	onDblClick: function(e)
	{
		this.processEvent('dblclick', e);
	},

	onColWidthChange: function(colModel, index, width)
	{
		this.updateColumnWidth(index, width);
	},

	onHeaderChange: function(colModel, index, text)
	{
		this.updateHeaders();
	},

	onHiddenChange: function(colModel, index, hidden)
	{
		this.updateColumnHidden(index, hidden);
	},

	onColumnMove: function(colModel, oldIndex, newIndex)
	{
		this.indexMap = null;
		var scrollState = this.getScrollState();
		this.refresh(true);
		this.restoreScroll(scrollState);
		this.fireEvent('columnmove', oldIndex, newIndex);
	},

	onColConfigChange: function()
	{
		delete this.lastViewWidth;
		delete this.lastSortedColumn;
		this.indexMap = null;
		this.refresh(true);
	},

	initElements: function()
	{
		this.viewport = new Ext.Element(this.body.dom.firstChild);
		this.mainWrap = new Ext.Element(this.viewport.dom.firstChild);
		this.mainHd = new Ext.Element(this.mainWrap.dom.firstChild);

		if(this.hideHeaders)
		{
			this.mainHd.setDisplayed(false);
		}

		this.innerHd = new Ext.Element(this.mainHd.dom.firstChild);
		this.scroller = new Ext.Element(this.mainWrap.dom.childNodes[1]);
		
		if(this.forceFit)
		{
			this.scroller.setStyle('overflow-x', 'hidden');
		}
		
		this.mainBody = new Ext.Element(this.scroller.dom.firstChild);
		this.innerCt = new Ext.Element(this.scroller.dom.firstChild.firstChild);
		this.focusEl = new Ext.Element(this.scroller.dom.childNodes[1]);
		this.focusEl.swallowEvent("click", true);

		this.resizeMarker = new Ext.Element(this.viewport.dom.childNodes[1]);
		this.resizeProxy = new Ext.Element(this.viewport.dom.childNodes[2]);
	},

	initUIEvents: function()
	{
		this.colModel.on("configChange", this.onColConfigChange, this);
		this.colModel.on("widthChange", this.onColWidthChange, this);
		this.colModel.on("headerChange", this.onHeaderChange, this);
		this.colModel.on("hiddenChange", this.onHiddenChange, this);
		this.colModel.on("columnMoved", this.onColumnMove, this);

		this.mon(this.body, 'mousedown', this.onMouseDown, this);
		this.mon(this.body, 'click', this.onClick, this);
		this.mon(this.body, 'dblclick', this.onDblClick, this);
		this.mon(this.body, 'contextmenu', this.onContextMenu, this);

		this.on("headerClick", this.onHeaderClick, this);
		this.on("beforeExpandNode", this.onBeforeExpandNode, this);
		this.on("expandNode", this.onExpandNode, this);
		this.on('beforeExpandNode', this.onBeforeExpandNode, this);
		this.on('beforeCollapseNode', this.onBeforeCollapseNode, this);

//		this.relayEvents(this.body, ['mousedown','mouseup','mouseover','mouseout','keypress', 'keydown']);

		this.innerHd.on("click", this.handleHdDown, this);
		
		this.mainHd.on('mouseover', this.handleHdOver, this);
		this.mainHd.on('mouseout', this.handleHdOut, this);
		this.mainHd.on('mousemove', this.handleHdMove, this);

		this.scroller.on('scroll', this.syncScroll, this);
		
		this.on('cellcontextmenu', this.onCellContextMenu, this);
	},

	createControls: function()
	{
		this.createHeaderMenu();

		if(this.enableColumnResize !== false)
		{
			this.columnSplitter = new Z8.tree.ColumnSplitter(this, this.mainHd.dom);
		}

		if(this.enableColumnMove)
		{
			this.columnDrag = new Z8.tree.ColumnDragZone(this, this.innerHd.dom);
			this.columnDrop = new Z8.tree.HeaderDropZone(this, this.mainHd.dom);
		}


		if(this.enableDragDrop || this.enableDrag)
		{
			this.dragZone = new Z8.tree.GridDragZone(this, { ddGroup : this.ddGroup || 'GridDD' });
		}
	},
	
	createHeaderMenu: function()
	{
		if(this.enableHdMenu !== false)
		{
			this.hmenu = new Z8.menu.MulticolumnMenu({id: this.id + '-hctx'});
			
			this.hmenu.add(
				{itemId:"asc", text: this.sortAscText, cls: "xg-hmenu-sort-asc"},
				{itemId:"desc", text: this.sortDescText, cls: "xg-hmenu-sort-desc"}
			);
			
			if(this.enableColumnHide !== false)
			{
				this.colMenu = new Ext.menu.Menu({id: this.id + "-hcols-menu"});
				this.colMenu.on({
					scope: this,
					beforeshow: this.beforeColMenuShow,
					itemclick: this.handleHdMenuClick
				});
				this.hmenu.add('-', {
					itemId:"columns",
					hideOnClick: false,
					text: this.columnsText,
					menu: this.colMenu,
					iconCls: 'x-cols-icon'
				});
				
				// Current Hide Menu Item
				this.btnShowHideCurrent = new Ext.menu.Item({itemId: 'columnhide', text: 'Скрыть столбец', iconCls: 'x-cols-hide-icon', handler: this.handleHdMenuHideCurrent, scope: this}); 
                this.hmenu.add(this.btnShowHideCurrent);
                
                // Hide / Show all columns
                this.btnShowHide = new Ext.menu.Item({itemId: 'showhidecolumns', text: 'Показать все столбцы', iconCls: 'x-cols-showall-icon', disabled: !this.hasHiddenColumns(), handler: this.handleHdMenuShowAll, scope: this}); 
                this.hmenu.add(this.btnShowHide);
			}
			
			this.hmenu.on("itemclick", this.handleHdMenuClick, this);
		}
	},
	
	handleHdMenuHideCurrent: function()
	{
		var colModel = this.colModel;
		if (this.hdCtxIndex)
			colModel.setHidden(this.hdCtxIndex, true);
	
		this.btnShowHide.setDisabled(false);
	},
	
	handleHdMenuShowAll: function()
	{
		var colModel = this.colModel,
			colCount = colModel.getColumnCount(),
			i;

		for (i = 0; i < colCount; i++) {
			if (colModel.isHidden(i) === true) {
				colModel.setHidden(i, false);
			}
		}
	
		this.btnShowHide.setDisabled(true);
	
		this.refresh(true);
	},
	
	hasHiddenColumns: function()
	{
		var colModel = this.colModel,
			colCount = colModel.getColumnCount(),
			i;

		for (i = 0; i < colCount; i++) {
			if (colModel.isHidden(i) === true) {
				return true;
			}
		}
	
		return false;
	},
	
	onRender: function(ct, position)
	{
		Z8.tree.TreePanel.superclass.onRender.call(this, ct, position);

		this.body.dom.innerHTML = this.templates.master.apply({
			header: this.renderHeaders(),
			ostyle: 'width:' + this.getOffsetWidth() + 'px;',
			bstyle: 'width:' + this.getTotalWidth() + 'px;'
		});

		this.initElements();
		this.initUIEvents();
		
		this.createControls();
		this.updateHeaderSortState();
	},

	layoutTree: function()
	{
		if(!this.mainBody)
		{
			return;
		}

		var size = this.body.getSize(true);

		if(!this.hideHeaders && (size.width < 20 || size.height < 20))  // display: none?
		{
			return;
		}
		
		if(this.autoHeight)
		{
			this.scroller.dom.style.overflow = 'visible';
			
			if(Ext.isWebKit)
			{
				this.scroller.dom.style.position = 'static';
			}
		}
		else
		{
			this.viewport.setSize(size.width, size.height);
			this.scroller.setSize(size.width, size.height - this.mainHd.getHeight());
			
			if(this.innerHd.dom)
			{
				this.innerHd.setStyle('width', size.width + 'px');
			}
		}

		if(this.forceFit)
		{
			if(this.lastViewWidth != size.width)
			{
				this.fitColumns(false, false);
				this.lastViewWidth = size.width;
			}
		}
		else
		{
			this.autoExpand();
			this.syncHeaderScroll();
		}
	},

	fitColumns: function(preventRefresh, onlyExpand, omitColumn)
	{
		var cm = this.colModel, i;
		var tw = cm.getTotalWidth(false);
		var aw = this.body.getWidth(true) - this.scrollOffset;

		if(aw < 20)
		{
			return;
		}
		
		var extra = aw - tw;

		if(extra === 0)
		{
			return false;
		}

		var vc = cm.getColumnCount(true);
		
		var ac = vc - (typeof omitColumn == 'number' ? 1 : 0);
		
		if(ac === 0)
		{
			ac = 1;
			omitColumn = undefined;
		}
		
		var colCount = cm.getColumnCount();
		
		var cols = [];
		
		var extraCol = 0;
		var width = 0;
		
		var w;
		
		for(i = 0; i < colCount; i++)
		{
			if(!cm.isHidden(i) && !cm.isFixed(i) && i !== omitColumn)
			{
				w = cm.getColumnWidth(i);
				cols.push(i);
				extraCol = i;
				cols.push(w);
				width += w;
			}
		}
		
		var frac = (aw - cm.getTotalWidth()) / width;
		
		while (cols.length)
		{
			w = cols.pop();
			i = cols.pop();
			cm.setColumnWidth(i, Math.max(this.minColumnWidth, Math.floor(w + w * frac)), true);
		}

		if((tw = cm.getTotalWidth(false)) > aw)
		{
			var adjustCol = ac != vc ? omitColumn : extraCol;
			cm.setColumnWidth(adjustCol, Math.max(1, cm.getColumnWidth(adjustCol) - (tw - aw)), true);
		}

		if(preventRefresh !== true)
		{
			this.updateAllColumnWidths();
		}

		return true;
	},

	autoExpand: function(preventUpdate)
	{
		var cm = this.colModel;
		
		if(!this.userResized && this.autoExpandColumn)
		{
			var tw = cm.getTotalWidth(false);
			var aw = this.body.getWidth(true) - this.scrollOffset;
			
			if(tw != aw)
			{
				var ci = cm.getIndexById(this.autoExpandColumn);
				var currentWidth = cm.getColumnWidth(ci);
				var cw = Math.min(Math.max(((aw - tw) + currentWidth), this.autoExpandMin), this.autoExpandMax);
				
				if(cw != currentWidth)
				{
					cm.setColumnWidth(ci, cw, true);
					
					if(preventUpdate !== true)
					{
						this.updateColumnWidth(ci, cw);
					}
				}
			}
		}
	},

	refreshNode: function(node, record)
	{
		var rowInner = this.templates.rowInner.apply(node.ui.getParameters(node, node.attributes));
		var row = this.getRow(node);
		row.innerHTML = rowInner;

		node.ui.update();
		node.ui.updateExpandIcon();
	},

	renderTree: function()
	{
		this.innerCt.update('');
		
		this.root.cascade(function(node)
		{
			node.rendered = false;
			node.childrenRendered = false;
			node.ui.rendered = false;
			node.ui.expanded = false;
			node.ui.wasLeaf = true;
			node.ui.c1 = '';
			node.ui.c2 = '';
			node.ui.ecc = '';
			return true;
		}, this);
			
		this.root.render();
	},
	
	refresh: function(headersToo)
	{
		this.fireEvent("beforerefresh", this);
		this.stopEditing(true);

		this.mainBody.dom.style.width = this.getTotalWidth() + 'px';
		
		this.renderTree();
		
		if(headersToo === true)
		{
			this.updateHeaders();
			this.updateHeaderSortState();
		}
		
		this.layoutTree();

		this.fireEvent("refresh", this);
	},

	updateHeaders: function()
	{
		this.innerHd.dom.firstChild.innerHTML = this.renderHeaders();
		this.innerHd.dom.firstChild.style.width = this.getOffsetWidth() + 'px';
		this.innerHd.dom.firstChild.firstChild.style.width = this.getTotalWidth() + 'px';
	},

	updateAllColumnWidths : function()
	{
		var colCount = this.colModel.getColumnCount()
		
		for(i = 0; i < colCount; i++)
		{
			this.updateColumnWidth(i, this.getColumnWidth(i));
		}
	},

	updateColumnWidth: function(index, width)
	{
		var width = this.getColumnWidth(index);
		var totalWidth = this.getTotalWidth() + 'px';
		
		this.innerHd.dom.firstChild.style.width = this.getOffsetWidth() + 'px';
		this.innerHd.dom.firstChild.firstChild.style.width = totalWidth;
		this.mainBody.dom.style.width = totalWidth;
		
		var hd = this.getHeaderCell(index);
		
		hd.style.width = width + 'px';

		var rows = this.getRows();

		for(var i = 0, length = rows.length; i < length; i++)
		{
			var li = rows[i];
			var div = li.firstChild;
			
			var table = div.firstChild;
			
			div.style.width = totalWidth;
			table.style.width = totalWidth;
			
			var td = table.rows[0].childNodes[index];
			var tdDiv = td.firstChild;

			td.style.width = width + 'px';
			tdDiv.style.width = width + 'px';

/*			if(index == 0 && Ext.isIE)
			{
				var offset = 0;
				var indent = tdDiv.childNodes[0];
				
				for(var j = 0; j < indent.childNodes.length; j++)
				{
					offset += indent.childNodes[j].clientWidth;
				}
				
				offset += tdDiv.childNodes[1].clientWidth + (this.icons ? tdDiv.childNodes[2].clientWidth : 0);
				tdDiv.childNodes[this.icons ? 3 : 2].firstChild.style.width = Math.max(20, width - offset - 10) + 'px';
			}
*/			
		}
	},

	// private
	updateColumnHidden: function(col, hidden)
	{
		var totalWidth = this.getTotalWidth() + 'px';

		this.innerHd.dom.firstChild.style.width = this.getOffsetWidth();
		this.innerHd.dom.firstChild.firstChild.style.width = totalWidth;
		this.mainBody.dom.style.width = totalWidth;
		
		var display = hidden ? 'none' : '';

		var hd = this.getHeaderCell(col);
		
		hd.style.display = display;

		var rows = this.getRows();
		
		for(var i = 0, length = rows.length; i < length; i++)
		{
			var li = rows[i];
			var div = li.firstChild;
			var table = div.firstChild;
			
			div.style.width = totalWidth;
			table.style.width = totalWidth;
			
			table.rows[0].childNodes[col].style.display = display;
		}

		delete this.lastViewWidth;
		this.layoutTree();
	},

	afterRender: function()
	{
		Z8.tree.TreePanel.superclass.afterRender.call(this);
		
		this.tooltip = new Z8.grid.Tooltip({ grid: this });

		this.onDataChange();

		this.fireEvent('viewReady', this);
		
		this.layoutTree();
	},
	
	onResize: function()
	{
		Z8.tree.TreePanel.superclass.onResize.apply(this, arguments);
		this.layoutTree();
	},

	onDestroy: function()
	{
		if(this.rendered)
		{
			this.un("headerclick", this.onHeaderClick, this);
			this.un("beforeExpandNode", this.onBeforeExpandNode, this);
			this.un("expandNode", this.onExpandNode, this);

			this.innerHd.un("click", this.handleHdDown, this);
			
			this.mainHd.un('mouseover', this.handleHdOver, this);
			this.mainHd.un('mouseout', this.handleHdOut, this);
			this.mainHd.un('mousemove', this.handleHdMove, this);

			this.scroller.un('scroll', this.syncScroll, this);
		
			this.colModel.un("configchange", this.onColConfigChange, this);
			this.colModel.un("widthchange", this.onColWidthChange, this);
			this.colModel.un("headerchange", this.onHeaderChange, this);
			this.colModel.un("hiddenchange", this.onHiddenChange, this);
			this.colModel.un("columnmoved", this.onColumnMove, this);

			this.body.removeAllListeners();
			this.body.update('');

			if(this.colMenu)
			{
				Ext.menu.MenuMgr.unregister(this.colMenu);
				this.colMenu.destroy();
				delete this.colMenu;
			}
			
			if(this.hmenu)
			{
				Ext.menu.MenuMgr.unregister(this.hmenu);
				this.hmenu.destroy();
				delete this.hmenu;
			}
		
			if(this.enableColumnMove)
			{
				var dds = Ext.dd.DDM.ids['gridHeader' + this.body.id];
				
				if(dds)
				{
					for(var dd in dds)
					{
						if(!dds[dd].config.isTarget && dds[dd].dragElId)
						{
							var elid = dds[dd].dragElId;
							dds[dd].unreg();
							Ext.get(elid).remove();
						}
						else if(dds[dd].config.isTarget)
						{
							dds[dd].proxyTop.remove();
							dds[dd].proxyBottom.remove();
							dds[dd].unreg();
						}
						if(Ext.dd.DDM.locationCache[dd])
						{
							delete Ext.dd.DDM.locationCache[dd];
						}
					}
					delete Ext.dd.DDM.ids['gridHeader' + this.body.id];
				}
			}

			if(this.dragZone)
			{
				this.dragZone.unreg();
			}
			
			Ext.destroy(this.resizeProxy, this.resizeMarker, this.focusEl, this.innerCt, this.mainBody,
						this.scroller, this.innerHd, this.mainHd, this.mainWrap, this.viewport, this.dragZone, 
						this.columnSplitter, this.columnDrag, this.columnDrop, this.dragZone);

			Ext.destroy(this.loadMask);

			this.purgeListeners();
		}
		else if(this.getStore() && this.getStore().autoDestroy)
		{
			this.getStore().destroy();
		}
		
		Ext.destroy(this.colModel, this.selModel, this.tooltip);
		
		this.store = this.selModel = this.colModel = this.loadMask = this.tooltip = null;

		Z8.tree.TreePanel.superclass.onDestroy.call(this);
	}
});