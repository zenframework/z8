Z8.Toolbar = Ext.extend(Ext.Container,
{
	cls: 'z8-header-bar',
	
	layoutConfig: { align: 'stretch', pack: 'start' },

	rightWidth: null,
	
	constructor: function(config)
	{
		var vertical = config.vertical === true;
		var layout = vertical ? 'vbox' : 'hbox';
		
		var cls = config.cls || this.cls;
		
		config.layout = layout;

		if(Z8.isEmpty(config.items))
		{
			config.items = [];
		}
		
		if(!Ext.isArray(config.items))
		{
			config.items = [config.items];
		}
		
		var initial = config.items;
		var items = [];
		var right = [];
		
		for(var i = 0; i < initial.length; i++)
		{
			var item = initial[i];
			item.align == 'right' ? right.push(item) : items.push(item);
			item.on('autoSize', this.onAutoSize, this);
		}
		
		this.left = new Ext.Container({ flex: 1, layout: layout, layoutConfig: { overflowHandler: 'HorizontalMenu', align: 'stretch' }, items: items });
		items = [ this.left ];
		
		if(!Z8.isEmpty(right))
		{
			var rightWidth = this.rightWidth ? this.rightWidth : right.length * 80;
			
			this.right = new Ext.Container({
				cls: cls + '-right',
				layout: layout,
				layoutConfig: {align: 'stretch', pack: 'end'},
				items: right,
				style: {width: rightWidth + 'px'}
			});
			items.push(this.right);
		}
		
		config.cls = config.cls != null ? config.cls : '';
		
		if(!vertical)
		{
			var leftCorner = new Ext.Container({ cls: cls + '-left-border' });
			var rightCorner = new Ext.Container({ cls: cls + '-right-border' });

			items = new Ext.Container({ cls: cls, flex: 1, layout: layout, layoutConfig: { align: 'stretch' }, items: items });

			config.cls = 'z8-bar ' + cls;
			
			config.items = [ leftCorner, items, rightCorner ];
		}
		else
		{
			config.cls = 'z8-bar z8-vertical-toolbar';
			config.items = items;
		}
		
		Z8.Toolbar.superclass.constructor.call(this, config);
	},

	onAutoSize: function()
	{
		this.doLayout(false, true);
	},

	initComponent: function()
	{
		Z8.Toolbar.superclass.initComponent.call(this);
	}
});

Z8.Toolbar.Separator = Ext.extend(Ext.Container, 
{
	cls: 'z8-toolbar-separator',
	html: Z8.emptyString
});

Z8.Toolbar.TextItem = Ext.extend(Ext.Container,
{
	constructor: function(config)
	{
		if(Ext.isString(config) || config == null)
		{
			config =  { text : config || '' };
		}
		
		Z8.Toolbar.TextItem.superclass.constructor.call(this, config);
	},

	initComponent: function()
	{
		Z8.Toolbar.TextItem.superclass.initComponent.call(this);
		this.addEvents('autoSize');
	},

	onRender: function(ct, position)
	{
		var cell = { tag: 'td', cls: 'x-text-item' + (this.iconCls != null ? ' x-text-icon' + this.iconCls : ''), html: this.text }; 
		var row = { tag: 'tr', cn: cell };
		this.autoEl = { tag: 'table', cellpadding: '0', cellspacing: '0', cn: row };

		Z8.Toolbar.TextItem.superclass.onRender.call(this, ct, position);
	},
	
	setText: function(text)
	{
		this.text = text;
		
		if(this.rendered)
		{
			var cell = this.el.query('td.x-text-item', true)[0];
			cell.innerHTML = text;
			this.fireEvent('autoSize', this);
		}
	},
	
	setIconClass: function(iconCls)
	{
		var old = this.iconCls;
		this.iconCls = iconCls;
	
		if(this.rendered)
		{
			var cell = Ext.fly(this.el.query('td.x-text-item', true)[0]);
			cell.addClass('x-text-icon');
			cell.replaceClass(old, this.iconCls);
		}
	}
});