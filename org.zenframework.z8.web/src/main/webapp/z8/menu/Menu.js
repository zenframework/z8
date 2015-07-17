
/*Ext.menu.Item.prototype.itemTpl = new Ext.XTemplate(
	'<a id="{id}" class="{cls}" hidefocus="true" unselectable="on" href="{href}"',
		'<tpl if="hrefTarget">',
			' target="{hrefTarget}"',
		'</tpl>',
	'>',
	'<img alt="{altText}" src="{icon}" class="x-menu-item-icon {iconCls}"/>',
    	'<div style="padding-left:7px"><span class="x-menu-item-text">{text}</span></div>',
	'</a>'
);*/

Ext.override(Ext.menu.BaseItem, {
	
	iconClickable: true,
	
	initComponent : function()
	{
		Ext.menu.BaseItem.superclass.initComponent.call(this);
		this.addEvents(
			'click',
			'closeclick',
			'activate',
			'deactivate'
		);
		if(this.handler){
			this.on("click", this.handler, this.scope);
		}
	},
	
	onClick : function(e)
	{
		if(this.checkCloseClick(e))
		{
			if( ! this.disabled ) {
				this.fireEvent("closeclick", this, e);
				e.stopEvent();
	        } else {
	            e.stopEvent();
	        }
		}
		else
		{
			if(!this.disabled && this.fireEvent("click", this, e) !== false
					&& (this.parentMenu && this.parentMenu.fireEvent("itemclick", this, e) !== false)){
				this.handleClick(e);
			} else {
				e.stopEvent();
			}
		}
	},
	
	checkCloseClick: function(e)
	{
		if (this.iconClickable)
		{
	    	var el = e.getTarget(null, null, true);
	    	
	    	//Stupid IE9 FIX, it returns span click instead of parent div with padding
	    	if (el.hasClass('x-menu-item-text')) {
	    		if (el.findParent('.x-menu-item-closable', 10, true)) {
	    			el = el.findParent('.x-menu-item-closable', 10, true);
	    		}
	    	}
	    	
	    	if (el.hasClass('x-menu-item-closable'))
	    	{
	    		var xpos = el.getXY()[0] + el.getSize().width;
	        	var ypos = el.getXY()[1] + el.getSize().height;
	        	var height = el.getSize().height;
	        	var x = e.getXY()[0];
	        	var y = e.getXY()[1];
	        	
	        	if ( (x > (xpos - 22)) && (x < xpos) && (y > (ypos - height)) && (y < ypos) ) {
	        		return true;
	        	}
	    	}
		}

    	return false;
    }
	
});

Ext.override(Ext.menu.Item, {
	
	itemTpl: new Ext.XTemplate(
		'<a id="{id}" class="{cls}', 
			'<tpl if="closable">',
				' x-menu-item-closable',
			'</tpl>',
			'" hidefocus="true" unselectable="on" href="{href}"',
			'<tpl if="hrefTarget">',
				' target="{hrefTarget}"',
			'</tpl>',
		'>',
			'<tpl if="iconClickable">',
				'<img alt="{altText}" src="{icon}" class="x-menu-item-icon {iconCls}"/>',
			'</tpl>',
		    '<div style="padding-left:7px;',
		    '<tpl if="closable">',
			' padding-right:7px;',
			'</tpl>',
		    '" class="x-menu-item-text-wrap"><span class="x-menu-item-text">{text}</span></div>',
		'</a>'
	),
		
	getTemplateArgs: function()
	{
		return {
			id: this.id,
			cls: this.itemCls + (this.menu ?  ' x-menu-item-arrow' : '') + (this.cls ?  ' ' + this.cls : ''),
			href: this.href || '#',
			hrefTarget: this.hrefTarget,
			icon: this.icon || Ext.BLANK_IMAGE_URL,
			iconCls: this.iconCls || '',
			text: this.itemText||this.text||'&#160;',
			altText: this.altText || '',
			closable: this.closable || false,
			iconClickable: this.iconClickable
		};
	}	
	
});


Z8.menu.MulticolumnMenu = Ext.extend(Ext.menu.Menu,
{
	shadow: true,
	
	layout : 'multicolumnmenu',
	itemsPerColumn: 15,
	cls: 'x-multicolumn-menu',

	defaultOffsets: [0, -2],
	
    //private
	getLayoutTarget: function()
	{
		return this.ul;
	},

    //private
	getLayoutTargetForItem: function(item)
	{
		return this.uls[item.column != null ? item.column : 0];
	},

	initColumns: function()
	{
		var column = 0;
		var itemsInColumn = 0;
		
		if(this.items == null)
		{
			return 1;
		}

		this.items.each(function(item){
			
			if(item instanceof Ext.menu.TextItem)
			{
				if(itemsInColumn >= (this.itemsPerColumn))
				{
					column++;
					itemsInColumn = 0;
				}
			}
			else if(item instanceof Ext.menu.Item)
			{
				if(itemsInColumn == this.itemsPerColumn)
				{
					column++;
					itemsInColumn = 0;
				}
				itemsInColumn++;
			}
			
			item.column = column;
		}, this);
		
		return column + 1;
	},
	
    // private
	onRender: function(ct, position)
	{
		if(!ct)
		{
			ct = Ext.getBody();
		}

		var columnCount = this.initColumns();
		
		var rows = [];
		
		if(this.headerText != null)
		{
			var header = {tag: 'tr', cn:[ {tag: 'td', colspan: columnCount, style:' padding: 3px 15px 3px 15px;', cls:'x-menu-list x-menu-list-item z8-menu-header', html: this.headerText }]};
			rows.push(header);
		}

		var columns = [];
		
		for(var i = 0; i < columnCount; i++)
		{
			columns.push({ tag: 'td', style:'vertical-align: top;', cn: [{tag: 'ul', cls: 'x-menu-list'}] });
		}
	
		rows.push({tag: 'tr', cn: columns });
 
		var content =
		[
			{tag: 'a', cls: 'x-menu-focus', onclick: 'return false;', tabIndex: '-1'},
			{tag: 'table', cls: 'x-menu-table', border: '0', cellspacing: '0', cellpadding: '0', cn: rows}
		];
		
		var body = { tag: 'div', cls: 'z8-menu-body', cn: content };
		
		var dh = 
		{
			id: this.getId(),
			cls: 'x-menu ' + ((this.floating) ? 'x-menu-floating x-layer ' : '') + (this.cls || '') + (this.plain ? ' x-menu-plain' : '') + (this.showSeparator ? '' : ' x-menu-nosep'),
			style: this.style,
			cn: body
		};
        
		if(this.floating)
		{
			this.el = new Ext.Layer(
			{
				shadow: this.shadow,
				dh: dh,
				constrain: false,
				parentEl: ct,
				zindex: this.zIndex
			});
		}
		else
		{
			this.el = ct.createChild(dh);
		}
		
		// generic focus element
		this.focusEl = this.el.child('a.x-menu-focus');
		
		this.ul = this.el.child('table.x-menu-table');
		
		this.uls = [];
		var uls = this.el.query('ul.x-menu-list');
		
		for(var i = 0; i < uls.length; i++)
		{
			this.uls.push(Ext.get(uls[i]));
		}

		// skip Menu.onRender, calling Container.onRender directly;
		Ext.menu.Menu.superclass.onRender.call(this, ct, position);

		if(!this.keyNav)
		{
			this.keyNav = new Ext.menu.MenuNav(this);
		}
		
		for(var i = 0; i < this.uls.length; i++)
		{	
			var ul = this.uls[i];
			this.mon(ul, 'click', this.onClick, this);
			this.mon(ul, 'mouseover', this.onMouseOver, this);
			this.mon(ul, 'mouseout', this.onMouseOut, this);
		}

		if(this.enableScrolling)
		{
			this.mon(this.el, 'click', this.onScroll, this, { delegate: '.x-menu-scroller' });
			this.mon(this.el, 'mouseover', this.deactivateActive, this, { delegate: '.x-menu-scroller' });
		}
	}
});