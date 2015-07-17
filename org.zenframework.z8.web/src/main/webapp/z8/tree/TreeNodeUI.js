Z8.tree.TreeNodeUI = Ext.extend(Ext.tree.TreeNodeUI,
{
	renderElements : function(node, attributes, targetNode, bulkRender)
	{
		var tree = node.getOwnerTree();

		if(!tree.rendered)
		{
			return;
		}

		var parameters = this.getParameters(node, attributes);

		if(bulkRender !== true && node.nextSibling && node.nextSibling.ui.getEl())
		{
			this.wrap = Ext.DomHelper.insertHtml("beforeBegin", node.nextSibling.ui.getEl(), tree.templates.row.apply(parameters));
		}
		else
		{
			if(!targetNode)
			{
				if(node.parentNode.ui.ctNode)
				{
					targetNode = node.parentNode.ui.ctNode;
				}
				else
				{
					//здесь node.parent != null.
					targetNode = node.parentNode.ui.ctNode = Ext.DomHelper.insertHtml("afterEnd", node.parentNode.ui.elNode, tree.templates.rows.apply({}));
				}
			}
			this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, tree.templates.row.apply(parameters));
		}

		this.elNode = this.wrap.childNodes[0]; // div
		this.ctNode = null; // ul
		
		this.update();
		this.updateExpandIcon();
	},

	update: function()
	{
		var tree = this.node.getOwnerTree();
		var checkbox = typeof this.node.attributes.checked == 'boolean';

		this.cellsContainer = this.elNode.firstChild.firstChild.firstChild; // ul/li/div/table/tbody/tr
		
		var td = this.cellsContainer.children[this.ecNodeIndex];
		var cs = td.childNodes; // ul/li/div/table/tbody/tr/td
		
		this.indentNode = td.firstChild.childNodes[0];
		this.ecNode = td.firstChild.childNodes[1];
		
		var index = 2;
		
		if(tree.icons)
		{
			this.iconNode =  td.firstChild.childNodes[index + 1];
			index++;
		}

		if(checkbox)
		{
			this.checkbox = td.firstChild.childNodes[index];
			// fix for IE6
			this.checkbox.defaultChecked = this.checkbox.checked;
			index++;
		}
		
		this.anchor = td.lastChild;
		this.textNode = this.anchor.firstChild;

		this.ecc = this.c1 = this.c2 = null;
	},

	getParameters: function(node, attributes)
	{
		this.indentMarkup = node.parentNode ? node.parentNode.ui.getChildIndent() : '';
	
		var tree = node.getOwnerTree();
		var store = tree.store;
		var record = store != null ? store.getById(node.id) : null;

		var cells = this.renderCells(node, record);

		var parameters =
		{
			nodeId: node.id,
			cls: attributes.cls || '',
			cells: cells,
			tstyle: 'width:' + tree.getTotalWidth() + 'px;'
		}

		return parameters;
	},

	renderCells: function(node, record)
	{
		var cells = [];

		var attributes = node.attributes;
		var checkbox = typeof attributes.checked == 'boolean';

		var tree = node.getOwnerTree();
		var columnModel = tree.getColumnModel();

		var columnCount = columnModel.getColumnCount();
		var last = columnCount - 1;
		
		this.ecNodeIndex = 0;

		for(var i = 0; i < columnCount; i++)
		{
			if(columnModel.columns[i].fixed)
			{
				this.ecNodeIndex++;
			}
			else
			{
				break;
			}
		}
		
		for(var i = 0; i < columnCount; i++)
		{
			var columnId = columnModel.getColumnId(i);
			var renderer = columnModel.getRenderer(i);
			var columnWidth = tree.getColumnWidth(i);
			var hidden = columnModel.isHidden(i);

			var markup = '';
			
			var value = record != null ? record.get(columnId) : attributes[columnId];
			var text = renderer ? renderer(value, null, record, node, i, tree.store) : value;
			
			if(i >= this.ecNodeIndex)
			{
				text = text.replace(' ', Z8.emptyString);
			}
			
			if(i == this.ecNodeIndex)
			{
				var iconMarkup = tree.icons ? ('<img src="' + (attributes.icon || this.emptyIcon || '') + '" class="x-tree-node-icon' + (attributes.icon ? ' x-tree-node-inline-icon' : "") + (attributes.iconCls ? ' ' + attributes.iconCls : '') + '" unselectable="on"/>') : '';
				var checkBoxMarkup = checkbox ? ('<input class="x-tree-node-cb" style="height:13px; margin-top:3px;" type="checkbox" ' + (attributes.checked ? 'checked="checked"/>' : '/>')) : '';

				var buf = [
						'<span class="x-tree-node-indent">', this.indentMarkup, "</span>",
						'<img src="', this.emptyIcon, '" class="x-tree-ec-icon x-tree-elbow" />',
						iconMarkup,
						checkBoxMarkup,
						'<a hidefocus="on" class="x-tree-node-anchor" tabIndex="1" unselectable="on">',
							'<span class="x-grid3-cell-inner" unselectable="on" style="border-width: 0px; padding:0px 0px 0px 3px;' + (Ext.isIE ? '' : 'line-height:20px;') + '">', Z8.isEmpty(text) ? Z8.emptyString : text, '</span>',
						'</a>'
				];
					
				markup = buf.join('');
				
			}
			else
			{
				if(i < this.ecNodeIndex)
				{
					markup = text;
				}
				else
				{
					var buf = [ '<span class="x-grid3-cell-inner x-grid3-cell-text x-unselectable" unselectable="on" style="border-width: 0px; padding:' + (Ext.isIE ? '2px' : '3px') + ' 0px ' + (Ext.isIE ? '5px' : '4px') + ' 5px;">', Z8.isEmpty(text) ? Z8.emptyString : text, '</span>' ];
					markup = buf.join('');
				}
			}
			
			var parameters = 
			{
				columnId: columnId,
				attr: '',
				cellAttr: '',
				cls: i == 0 ? 'x-grid3-cell-first ' : (i == last ? 'x-grid3-cell-last ' : ''),
				cellMarkup: markup,
				style: 'width:' + columnWidth + 'px;' + (hidden ? ' display:none;' : ''),
				innerStyle: i < this.ecNodeIndex ? 'padding-left: 18px; padding-top: 3px;' : 'padding:0px'
			};
			
			if(tree.markDirty && record != null && record.dirty && typeof record.modified[columnId] != 'undefined')
			{
				parameters.cls += ' x-grid3-dirty-cell';
			}

			cells[cells.length] = tree.templates.cell.apply(parameters);
		}

		return cells.join('');
	},

	render: function(bulkRender)
	{
		var tree = this.node.getOwnerTree();

		if(tree.rendered)
		{
			if(!this.node.isRoot || this.node.getOwnerTree().rootVisible)
			{
				Z8.tree.TreeNodeUI.superclass.render.call(this, bulkRender);
			}
			else
			{
				this.wrap = this.ctNode = this.node.ownerTree.innerCt.dom;
				this.node.expanded = true;
			}
		}
	},

	collapse: function()
	{
		if(!this.node.isRoot || this.node.getOwnerTree().rootVisible)
		{
			if(this.node.isExpandable())
			{
				Z8.tree.TreeNodeUI.superclass.collapse.call(this);
			}
			else
			{
				this.updateExpandIcon();
			}
		}
	},
	
	expand: function()
	{
		if(!this.node.isRoot || this.node.getOwnerTree().rootVisible)
		{
			if(this.node.isExpandable())
			{
				Z8.tree.TreeNodeUI.superclass.expand.call(this);
			}
			else
			{
				this.updateExpandIcon();
			}
		}
	},
	
	animExpand: function(callback)
	{
		if(!this.node.isRoot || this.node.getOwnerTree().rootVisible)
		{
			if(this.node.isExpandable())
			{
				Z8.tree.TreeNodeUI.superclass.animExpand.call(this, callback);
			}
			else
			{
				this.updateExpandIcon();
			}
		}
	},
	
	animCollapse: function(callback)
	{
		if(!this.node.isRoot || this.node.getOwnerTree().rootVisible)
		{
			if(this.node.isExpandable())
			{
				Z8.tree.TreeNodeUI.superclass.animCollapse.call(this, callback);
			}
			else
			{
				this.updateExpandIcon();
			}
		}
	},

	onSelectedChange: function(state)
	{
		if(state)
		{
			this.addClass("x-tree-selected");
		}
		else
		{
			this.removeClass("x-tree-selected");
		}
	},
	
	focus : function()
	{
		this.node.getOwnerTree().focusNode(this.node);
	},
	
	getDDHandles : function()
	{
		var tree = this.node.getOwnerTree();
		return tree.icons ? [this.iconNode, this.textNode, this.elNode] : [this.textNode, this.elNode];
	},

	getDDRepairXY : function()
	{
		var tree = this.node.getOwnerTree();
		return Ext.lib.Dom.getXY(tree.icons ? this.iconNode : this.checkbox ? this.checkbox : this.textNode);
	}
});