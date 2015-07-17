Z8.Dataview = Ext.extend(Ext.DataView, {
	
	focusedClass : "x-view-focused",
	focusEl : true,
	selectedIndex : false,
	focusedIndex : false,
	hoveredIndex : false,
	
	initComponent:function() {
		// create config object
		var config = {};

		// build config
        this.buildConfig(config);

		// apply config
        Ext.apply(this, Ext.apply(this.initialConfig, config));

        Z8.Dataview.superclass.initComponent.apply(this, arguments);
        
        this.addEvents("beforehover", "hover", "hoverout", "enterkey");
    },
	
	buildConfig:function(config) {
        this.buildStore(config);
		this.buildTpl(config);
    }, // eo function buildConfig
	
	buildStore: function(config) {
		config.store = this.store || new Ext.data.ArrayStore({
			fields: this.buildFields(),
			data: this.buildData()
		});
    }, // eo function createStore
	
	buildFields: function() {
        return this.fields || [];
    }, // eo function buildFields
	
	buildData: function() {
        return this.data || [];
    }, // eo function buildData
	
	buildTpl: function(config) {
		config.tpl = this.tpl || new Ext.XTemplate();
	}, // eo function buildTpl
	
	afterRender : function() {
		Ext.DataView.superclass.afterRender.call(this);

		this.el.on({
			'click' : this.onClick,
			'dblclick' : this.onDblClick,
			scope : this
		});

		if (this.overClass) {
			this.el.on({
				"mouseover" : this.onMouseOver,
				"mouseout" : this.onMouseOut,
				scope : this
			});
		}

		if (this.store) {
			this.setStore(this.store, true);
		}

		this.el.addClass("x-view");

		if (this.singleSelect || this.multiSelect) {
			if (this.focusEl === true) {
				this.focusEl = this.el.parent().parent().createChild({
					tag : "a",
					href : "#",
					cls : "x-view-focus",
					tabIndex : "-1"
				});
				this.focusEl.insertBefore(this.el.parent());
				this.focusEl.swallowEvent('click', true);
				this.renderedFocusEl = true;
			} else if (this.focusEl) {
				this.focusEl = Ext.get(this.focusEl);
			}

			if (this.focusEl) {
				this.keyNav = new Ext.KeyNav(this.focusEl, {
					"up" : this.onUpKey,
					"down" : this.onDownKey,
					"enter" : this.onEnterKey,
					scope : this,
					forceKeyDown : true
				}, this);
			}
		}
	},
	
	onEnterKey: function(e)
	{
		if (this.focusedIndex !== false) {
			var item = this.getNode(this.focusedIndex);
			e.preventDefault();
			this.fireEvent("enterkey", this, item, e);
		}
	},
	
	onUpKey: function(e)
	{
		if (this.nonUniform) {
			this.moveNonUniform(-1, e, true);
		} else {
			this.moveOnColumn(-1, e, true);
		}
	},
	
	onDownKey: function(e)
	{
		if (this.nonUniform) {
			this.moveNonUniform(1, e, true);
		} else {
			this.moveOnColumn(1, e, true);
		}
	},
	
	onClick : function(e) {
		var item = e.getTarget(this.itemSelector, this.el);
		if (item) {
			var index = this.indexOf(item);
			if (this.onItemClick(item, index, e) !== false) {
				this.fireEvent("click", this, index, item, e);
				this.retainFocus();
			}
		} else {
			if (this.fireEvent("containerclick", this, e) !== false) {
				this.clearSelections();
				this.retainFocus();
			}
		}
	},
	
	onMouseOver : function(e) {
		var item = e.getTarget(this.itemSelector, this.el);
		if (!item) {
			return;
		}
		var index = this.indexOf(item);
		if (this.fireEvent("beforehover", this, index, item, e) !== false) {
			this.hover(index);
			this.fireEvent("hover", this, index, item, e);
		}
	},
	
	onMouseOut : function(e) {
		var index = this.hoveredIndex;
		var item = this.getNode(this.hoveredIndex);
		this.hover(false);
		this.fireEvent("hoverout", this, index, item, e);
	},
	
	hover : function(node) {
		if (this.overClass) {
			if (this.hoveredIndex !== false) {
				Ext.fly(this.getNode(this.hoveredIndex)).removeClass(this.overClass);
			}
			if (node !== false) {
				node = this.getNode(node);
				Ext.fly(node).addClass(this.overClass);
			}
		}
		this.hoveredIndex = (node !== false) ? this.indexOf(this.getNode(node)): false;
	},
	
	deselect : function(node, suppressEvent, scrollIntoView) {
		node = this.getNode(node);
		if (this.isSelected(node))
		{
			this.selected.removeElement(node);

			Ext.fly(node).removeClass(this.selectedClass);

			if (scrollIntoView) {
				Ext.get(node).scrollIntoView(this.el, true, true);
			}
			if (!suppressEvent) {
				this.fireEvent("selectionchange", this, this.selected.elements, [node]);
			}
		}
	},

	select : function(nodeInfo, keepExisting, suppressEvent, scrollIntoView)
	{
		if (Ext.isArray(nodeInfo)) {
			if (!keepExisting) {
				this.clearSelections();
			}
			for (var i = 0, len = nodeInfo.length; i < len; i++) {

				if (!this.getNode(nodeInfo[i])) {
					continue;
				}

				this.select(nodeInfo[i], true, true);
				
				if ((i == nodeInfo.length - 1) && scrollIntoView) {
					Ext.get(this.getNode(nodeInfo[i])).scrollIntoView(this.el, true, true);
				}
			}
			if (!suppressEvent) {
				this.fireEvent("selectionchange", this, this.selected.elements);
			}
		} else {
			var node = this.getNode(nodeInfo);
			if (!keepExisting) {
				this.clearSelections();
			}
			if (node && !this.isSelected(node)) {
				if (this.fireEvent("beforeselect", this, node,
						this.selected.elements) !== false) {
					Ext.fly(node).addClass(this.selectedClass);
					this.selected.add(node);
					// ANDRIE
					// this.last = node.viewIndex;
					this.focus(node.viewIndex);
					if (scrollIntoView) {
						Ext.get(node).scrollIntoView(this.el, true, true);
					}
					// END
					if (!suppressEvent) {
						this.fireEvent("selectionchange", this,
								this.selected.elements);
					}
				}
			}
		}
	},
	
	focus : function(node, scrollIntoView)
	{
		if (this.focusedClass) {
			if (this.focusedIndex !== false) {
				//Ext.fly(this.getNode(this.focusedIndex)).removeClass(this.focusedClass);
			}
			if (node !== false) {
				node = this.getNode(node);
				if(node)
					Ext.fly(node).addClass(this.focusedClass);
			}
		}
		if (node !== false) {
			this.focusedIndex = this.indexOf(this.getNode(node));
			if (scrollIntoView) {
				Ext.get(node).scrollIntoView(this.el, true, true);
			}
		} else {
			this.focusedIndex = false;
		}
	},
	
	retainFocus : function() {
		if (this.focusEl) {
			this.doRetainFocus();
		}
	},
	
	doRetainFocus : function() {
		this.focusEl.focus();
	},
	
	moveOnColumn : function(delta, e, scrollIntoView, page)
	{
		var index = (this.focusedIndex === false) ? this.selectedIndex : this.focusedIndex;
		if (index === false) {
			this.moveFirst(e, scrollIntoView);
			return;
		}
		var node = Ext.get(this.getNode(index));
		var itemsPerPage = 1;
		if (page) {
			var itemsPerHeight = Math.floor(this.el.getHeight() / node.getHeight());
			var itemsPerWidth = Math.floor(this.el.getWidth() / node.getWidth());
			itemsPerPage = itemsPerHeight * itemsPerWidth - 1;
		}
		var newIndex = index + delta * itemsPerPage;
		if ((newIndex < 0) || (newIndex > this.store.getCount() - 1)) {
			if (this.hscroll) {
				return;
			} else {
				if (newIndex < 0) {
					this.moveFirst(e, scrollIntoView);
					return;
				} else {
					this.moveLast(e, scrollIntoView);
					return;
				}
			}
		}
		var newNode = Ext.get(this.getNode(newIndex));

		var sameColumn = (node.getX() == newNode.getX());

		while (!sameColumn && (newIndex != 0) && (newIndex != this.store.getCount() - 1)) {
			newIndex = newIndex + delta;
			newNode = Ext.get(this.getNode(newIndex));
			sameColumn = (node.getX() == newNode.getX());
		}
		if (sameColumn) {
			this.moveCommon(newIndex, e, scrollIntoView);
		}
	},
	
	moveCommon : function(newIndex, e, scrollIntoView) {
		if ((!e.ctrlKey && !e.shiftKey && ((this.singleSelect && !this.multiSelect) || (this.multiSelect && !this.simpleSelect)))
				|| (e.shiftKey && !this.multiSelect)) {
			this.select(newIndex, false, false, scrollIntoView);
			this.selectedIndex = newIndex;
		} else if (e.shiftKey && this.multiSelect) {
			var oldIndex = this.selectedIndex;
			this.selectRange(this.selectedIndex || 0, newIndex, false, scrollIntoView);
			this.selectedIndex = oldIndex;
		} else {
			this.focus(newIndex, scrollIntoView);
		}
	},
	
	moveFirst : function(e, scrollIntoView) {
		this.moveCommon(0, e, scrollIntoView);
	},
	
	moveLast : function(e, scrollIntoView) {
		this.moveCommon(this.store.getCount() - 1, e, scrollIntoView);
	},
	
	indexOf : function(node)
    {
    	node = this.getNode(node);
    	if (node)
    	{
    		if(Ext.isNumber(node.viewIndex)){
    			return node.viewIndex;
    		}
    		return this.all.indexOf(node);
    	} else {
    		//return false;
    	}
    }
});