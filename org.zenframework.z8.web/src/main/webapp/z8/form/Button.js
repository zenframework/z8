/*Ext.Button.prototype.template = new Ext.Template(
	'<div id="{id}" class="x-btn x-btn-plain {cls}" style="{style}" unselectable="on">',
		'<div class="x-btn-mc {btnCls}" style="width:auto; height:auto;">',
			'<em class="{menuCls}" unselectable="on"><button type="{type}"></button></em>',
		'</div>',
	'</div>');
*/
Z8.Button = Ext.extend(Ext.SplitButton,
{
	plain: true,
	autoWidth: false,
	vertical: false,
	split: false,
	
	initComponent: function()
	{
		Z8.Button.superclass.initComponent.call(this);
		
		if(this.iconCls != null)
		{
			this.overflowIconCls = this.iconCls + '-small'; 
		}
		
		if(!this.plain)
		{
			if(this.vertical)
			{
				this.template = new Ext.Template(
					'<table id="{id}" cellspacing="0" class="x-btn {cls}" style="{style}"><tbody class="{btnCls}">',
					Ext.isIE ? '' : '<tr><td class="x-btn-tl"><i>&#160;</i></td><td class="x-btn-tc"></td><td class="x-btn-tr"><i>&#160;</i></td></tr>',
					'<tr><td ' + (Ext.isIE ? '' : 'colspan=3 ') + 'class="x-btn-mc"><em class="{menuCls}" unselectable="on"><button type="{type}"></button></em></td></tr>',
					Ext.isIE ? '' : '<tr><td class="x-btn-bl"><i>&#160;</i></td><td class="x-btn-bc"></td><td class="x-btn-br"><i>&#160;</i></td></tr>',
					'</tbody></table>');
			}
			else
			{
				this.template = new Ext.Template(
					'<table id="{id}" cellspacing="0" class="x-btn {cls}" style="{style}"><tbody class="{btnCls}">',
					Ext.isIE ? '' : '<tr><td class="x-btn-tl"><i>&#160;</i></td><td class="x-btn-tc"></td><td class="x-btn-tr"><i>&#160;</i></td></tr>',
					'<tr><td class="x-btn-ml"><i>&#160;</i></td><td class="x-btn-mc"><em class="{menuCls}" unselectable="on"><button type="{type}"></button></em></td><td class="x-btn-mr"><i>&#160;</i></td></tr>',
					Ext.isIE ? '' : '<tr><td class="x-btn-bl"><i>&#160;</i></td><td class="x-btn-bc"></td><td class="x-btn-br"><i>&#160;</i></td></tr>',
					'</tbody></table>');
			}
		}
		else
		{
			this.template = new Ext.Template(
				'<div id="{id}" class="x-btn x-btn-plain {cls}" style="{style}" unselectable="on">',
					'<div class="x-btn-mc {btnCls}" style="width:auto; height:auto;">',
						'<em class="{menuCls}" unselectable="on"><button type="{type}"></button></em>',
					'</div>',
				'</div>');
		}
		
		this.template.compile();

		this.addEvents('autoSize');
	},

	getTemplateArgs: function()
	{
		return {
			type: this.type,
			btnCls: 'x-btn-' + this.scale + ' x-btn-icon-' + this.scale + '-' + this.iconAlign,
			menuCls: this.getMenuClass(), 
			cls: this.cls,
			id: this.id
		};
	},
	
	getMenuClass: function()
	{
		return this.split ? Z8.Button.superclass.getMenuClass.call(this) : Ext.SplitButton.superclass.getMenuClass.call(this);
	},

	isClickOnArrow: function(e)
	{
		return this.split ? Z8.Button.superclass.isClickOnArrow.call(this, e) : false;
	},
	
	onClick: function(event, target)
	{
		this.split ? Z8.Button.superclass.onClick.call(this, event, target) : Ext.SplitButton.superclass.onClick.call(this, event, target);
	},
	
	initButtonEl: function(btn, btnEl)
	{
		this.initializing = true;
		Z8.Button.superclass.initButtonEl.call(this, btn, btnEl);
		this.initializing = false;
	},

	setDisabled: function(disabled)
	{
		Z8.Button.superclass.setDisabled.call(this, disabled);
		this.wasDisabled = this.disabled;
	},
	
	setBusy: function(busy)
	{
		if(this.busy != busy)
		{
			if(busy)
			{
				this.cachedIconCls = this.iconCls;
				var wasDisabled = this.disabled;
	
				this.setIconClass('silk-loading');
				this.setDisabled(true);
				
				this.wasDisabled = wasDisabled;
			}
			else
			{
				this.setIconClass(this.cachedIconCls);
				this.setDisabled(this.wasDisabled != null ? this.wasDisabled : false);
			}
		}		
	},
	
	setIconClass: function(icon)
	{
		Z8.Button.superclass.setIconClass.call(this, icon);
		this.overflowIconCls = !Z8.isEmpty(icon) ? icon + '-small' : null;
	},
	
	setText: function(text)
	{
		Z8.Button.superclass.setText.call(this, text);
		
		if(this.el && !this.initializing)
		{
			this.fireEvent('autoSize', this);
		}

		return this;
	},

	toggleMenu: function()
	{
		if(this.menu != null)
		{
			!this.menu.isVisible() && !this.ignoreNextClick ? this.showMenu() : this.hideMenu();
		}
	}
});

Z8.SplitButton = Ext.extend(Z8.Button,
{
	split: true,

	setSplit: function(state)
	{
		this.split = state;
		
		var arrowBtnEl = this.el.child(this.arrowSelector);
		arrowBtnEl.addClass(this.getMenuClass());
		this.fireEvent('autoSize', this);
	}
});
