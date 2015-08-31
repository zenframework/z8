Z8.tree.ColumnSplitter = function(tree, header)
{
	this.tree = tree;
	this.marker = this.tree.resizeMarker;
	this.proxy = this.tree.resizeProxy;
	
	Z8.tree.ColumnSplitter.superclass.constructor.call(this, header, "treeSplitters" + this.tree.body.id, 
		{
			dragElId : Ext.id(this.proxy.dom), 
			resizeFrame:false
		}
	);
	
	this.scroll = false;
	this.hw = this.tree.splitHandleWidth;
};

Ext.extend(Z8.tree.ColumnSplitter, Ext.dd.DDProxy, {

	b4StartDrag : function(x, y)
	{
		this.tree.headersDisabled = true;
		
		var height = this.tree.mainWrap.getHeight();
		
		this.marker.setHeight(height);
		this.marker.show();
		this.marker.alignTo(this.tree.getHeaderCell(this.cellIndex), 'tl-tl', [-2, 0]);

		this.proxy.setHeight(height);

		var width = this.tree.colModel.getColumnWidth(this.cellIndex);
		var minWidth = Math.max(width - this.tree.minColumnWidth, 0);

		this.resetConstraints();
		this.setXConstraint(minWidth, 1000);
		this.setYConstraint(0, 0);
		this.minX = x - minWidth;
		this.maxX = x + 1000;
		this.startPos = x;
		
		Ext.dd.DDProxy.prototype.b4StartDrag.call(this, x, y);
	},


	handleMouseDown : function(e)
	{
		var t = this.tree.findHeaderCell(e.getTarget());
		
		if(t)
		{
			var xy = this.tree.fly(t).getXY(), x = xy[0], y = xy[1];
			var exy = e.getXY(), ex = exy[0];
			var w = t.offsetWidth, adjust = false;
		 
			if((ex - x) <= this.hw)
			{
				adjust = -1;
			}
			else if((x + w) - ex <= this.hw)
			{
				adjust = 0;
			}
			
			if(adjust !== false)
			{
				var ci = this.tree.getCellIndex(t);
				
				if(adjust == -1)
				{
					if (ci + adjust < 0)
					{
						return;
					}
					
					while(this.tree.colModel.isHidden(ci + adjust))
					{
						--adjust;
					
						if(ci + adjust < 0)
						{
							return;
						}
					}
				}
				this.cellIndex = ci + adjust;
				this.split = t.dom;
				
				if(this.tree.colModel.isResizable(this.cellIndex) && !this.tree.colModel.isFixed(this.cellIndex))
				{
					Z8.tree.ColumnSplitter.superclass.handleMouseDown.apply(this, arguments);
				}
			}
			else if(this.tree.columnDrag)
			{
				this.tree.columnDrag.callHandleMouseDown(e);
			}
		}
	},

	endDrag : function(e)
	{
		this.marker.hide();
		var endX = Math.max(this.minX, e.getPageX());
		var diff = endX - this.startPos;

		var tree = this.tree;
		tree.onColumnSplitterMoved(this.cellIndex, tree.colModel.getColumnWidth(this.cellIndex) + diff);

		setTimeout(function() { tree.headersDisabled = false; }, 50);
	},

	autoOffset : function()
	{
		this.setDelta(0,0);
	}
});


Z8.tree.HeaderDragZone = function(tree, hd, hd2)
{
	this.tree = tree;
	this.ddGroup = "treeHeader" + this.tree.body.id;
	
	Z8.tree.HeaderDragZone.superclass.constructor.call(this, hd);
	
	if(hd2)
	{
		this.setHandleElId(Ext.id(hd));
		this.setOuterHandleElId(Ext.id(hd2));
	}
	
	this.scroll = false;
};

Ext.extend(Z8.tree.HeaderDragZone, Ext.dd.DragZone, {
	maxDragWidth: 120,
	
	getDragData : function(e)
	{
		var target = Ext.lib.Event.getTarget(e);
		var header = this.tree.findHeaderCell(target);

		if(header)
		{
			return { ddel : header.firstChild, header : header };
		}
		
		return false;
	},

	onInitDrag : function(e)
	{
		this.tree.headersDisabled = true;
		var clone = this.dragData.ddel.cloneNode(true);
		clone.id = Ext.id();
		clone.style.width = Math.min(this.dragData.header.offsetWidth, this.maxDragWidth) + "px";
		this.proxy.update(clone);
		return true;
	},

	afterValidDrop : function()
	{
		var tree = this.tree;
		setTimeout(function() { tree.headersDisabled = false; }, 50);
	},

	afterInvalidDrop : function()
	{
		var tree = this.tree;
		setTimeout(function() { tree.headersDisabled = false; }, 50);
	}
});

Z8.tree.HeaderDropZone = function(tree, hd, hd2)
{
	this.tree = tree;

	this.proxyTop = Ext.DomHelper.append(document.body, { cls:"col-move-top", html:"&#160;" }, true);
	this.proxyBottom = Ext.DomHelper.append(document.body, { cls:"col-move-bottom", html:"&#160;" }, true);
	this.proxyTop.hide = this.proxyBottom.hide = function() { this.setLeftTop(-100,-100); this.setStyle("visibility", "hidden"); };
	this.ddGroup = "treeHeader" + this.tree.body.id;

	Z8.tree.HeaderDropZone.superclass.constructor.call(this, tree.body.dom);
};

Ext.extend(Z8.tree.HeaderDropZone, Ext.dd.DropZone, {
	proxyOffsets : [-4, -9],
	fly: Ext.Element.fly,

	getTargetFromEvent : function(e)
	{
		var target = Ext.lib.Event.getTarget(e);
		var index = this.tree.findCellIndex(target);
		
		if(index !== false)
		{
			return this.tree.getHeaderCell(index);
		}
	},

	nextVisible : function(header)
	{
		header = header.nextSibling;
		
		while(header)
		{
			if(!this.tree.colModel.isHidden(this.tree.getCellIndex(header)))
			{
				return header;
			}
			header = header.nextSibling;
		}
		return null;
	},

	prevVisible : function(header)
	{
		header = header.prevSibling;
		
		while(header)
		{
			if(!this.tree.colModel.isHidden(this.tree.getCellIndex(header)))
			{
				return header;
			}
			header = header.prevSibling;
		}
		return null;
	},

	positionIndicator : function(h, n, e)
	{
		var x = Ext.lib.Event.getPageX(e);
		var r = Ext.lib.Dom.getRegion(n.firstChild);
		var px, pt, py = r.top + this.proxyOffsets[1];
	
		if((r.right - x) <= (r.right - r.left) / 2)
		{
			px = r.right + this.tree.borderWidth;
			pt = "after";
		}
		else
		{
			px = r.left;
			pt = "before";
		}

		if(this.tree.colModel.isFixed(this.tree.getCellIndex(n)))
		{
			return false;
		}

		px +=  this.proxyOffsets[0];
		
		this.proxyTop.setLeftTop(px, py);
		this.proxyTop.show();
		
		if(!this.bottomOffset)
		{
			this.bottomOffset = this.tree.mainHd.getHeight();
		}
		
		this.proxyBottom.setLeftTop(px, py + this.proxyTop.dom.offsetHeight + this.bottomOffset);
		this.proxyBottom.show();
		
		return pt;
	},

	onNodeEnter : function(n, dd, e, data)
	{
		if(data.header != n)
		{
			this.positionIndicator(data.header, n, e);
		}
	},

	onNodeOver : function(n, dd, e, data)
	{
		var result = false;
		if(data.header != n)
		{
			result = this.positionIndicator(data.header, n, e);
		}
		
		if(!result)
		{
			this.proxyTop.hide();
			this.proxyBottom.hide();
		}
		return result ? this.dropAllowed : this.dropNotAllowed;
	},

	onNodeOut : function(n, dd, e, data)
	{
		this.proxyTop.hide();
		this.proxyBottom.hide();
	},

	onNodeDrop : function(n, dd, e, data)
	{
		var h = data.header;
		if(h != n)
		{
			var x = Ext.lib.Event.getPageX(e);
			var r = Ext.lib.Dom.getRegion(n.firstChild);
			
			var pt = (r.right - x) <= ((r.right - r.left)/2) ? "after" : "before";
			
			var oldIndex = this.tree.getCellIndex(h);
			var newIndex = this.tree.getCellIndex(n);
			
			if(pt == "after")
			{
				newIndex++;
			}
			if(oldIndex < newIndex)
			{
				newIndex--;
			}
			
			this.tree.colModel.moveColumn(oldIndex, newIndex);
			this.tree.fireEvent("columnmove", oldIndex, newIndex);
			return true;
		}
		return false;
	}
});


Z8.tree.ColumnDragZone = function(tree, hd)
{
	Z8.tree.ColumnDragZone.superclass.constructor.call(this, tree, hd, null);
	this.proxy.el.addClass('x-tree3-col-dd');
};

Ext.extend(Z8.tree.ColumnDragZone, Z8.tree.HeaderDragZone, {
	handleMouseDown : function(e)
	{
	},

	callHandleMouseDown : function(e)
	{
		Z8.tree.ColumnDragZone.superclass.handleMouseDown.call(this, e);
	}
});