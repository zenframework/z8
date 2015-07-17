Z8.layout.FormLayout = Ext.extend(Ext.layout.TableLayout,
{
	rendered: false,
	monitorResize: true,

	elementSelector: 'x-form-element',
	
	fieldTemplate: (function()
	{
		var t =
			'<table class="z8-form-field x-form-item {itemCls}" style="width:100%" border="0" cellspacing="0" cellpadding="0">' +
			'<thead>';
/*		
		t += 
			'<tr><td class="x-form-item-label" style="{labelStyle}">{label}{labelSeparator}</td></tr>' +
			'<tr><td><div class="x-form-element" id="x-form-el-{id}" style="{elementStyle}"></div></td></tr>';
*/		
		t += 
			'<tr>' + 
				'<td class="x-form-item-label" style="vertical-align: {labelAlign}; text-align: right;{labelCellStyle}"><div style="{labelStyle}">{label}{labelSeparator}</div></td>' +
				'<td class="x-form-element" id="x-form-el-{id}" style="position: static; vertical-align: middle; text-align: left; {elementStyle}"></td>' +
			'</tr>';

		t +=
			'</thead></table>';
 
		var template = new Ext.Template(t);
		template.disableFormats = true;
		return template.compile();
	})(),
	
	fieldNoLabelTemplate: (function()
	{
		var t =
			'<table class="x-form-item {itemCls}" width="100%" border="0" cellspacing="0" cellpadding="0">' +
			'<thead>';
/*		
		t += 
			'<tr><td class="x-form-item-label" style="{labelStyle}">{label}{labelSeparator}</td></tr>' +
			'<tr><td><div class="x-form-element" id="x-form-el-{id}" style="{elementStyle}"></div></td></tr>';
*/		
		t += 
			'<tr>' + 
				'<td class="x-form-element" id="x-form-el-{id}" style="{elementStyle}"></td>' +
			'</tr>';

		t +=
			'</thead></table>';
 
		var template = new Ext.Template(t);
		template.disableFormats = true;
		return template.compile();
	})(),

	setContainer : function(ct)
	{
		Z8.layout.FormLayout.superclass.setContainer.call(this, ct);
		this.elementStyle = "padding-left:0;";
	},
	
	getTemplateArgs: function(field)
	{
		var noLabelSep = !field.fieldLabel || field.hideLabel;

		var width = field.labelWidth != null && field.labelWidth != 0 ? field.labelWidth : 100;

		var align = field.labelAlign;
		
		if(align == null)
		{
			align = field instanceof Ext.Panel ? 'top' : 'center';
		}
		
		return {
			id: field.id,
			label: field.fieldLabel,
			itemCls: (field.itemCls || this.container.itemCls || '') + (field.hideLabel ? ' x-hide-label' : ''),
			clearCls: field.clearCls || 'x-form-clear-left',
			labelStyle: 'width: ' + width + 'px;',
			labelCellStyle: 'padding-left: 7px; padding-right: 7px; width:' + width + 'px;',
			elementStyle: this.elementStyle || '',
			labelSeparator: noLabelSep ? '' : (Ext.isDefined(field.labelSeparator) ? field.labelSeparator : this.labelSeparator),
			labelAlign: align	
		};
	},

	renderItem: function(c, position, target)
	{
		if(c && c.hidden)
		{
			return;
		}
		
		if(!this.table)
		{
			this.table = target.createChild(
				Ext.apply({tag:'table', cls:'x-table-layout', cellspacing: 0, cn: {tag: 'tbody'}}, this.tableAttrs), null, true);
		}
		
		var validParent = c.rendered && this.isValidParent(c, target);
		
		if(c && (c.isFormField || c.fieldLabel) && !validParent)
		{
			var args = this.getTemplateArgs(c);
			
			var template = c.showLabel ? this.fieldTemplate : this.fieldNoLabelTemplate;
				
			var cell = this.getNextCell(c);
			
			c.itemCt = template.append(cell, args, true);

			if(!c.getItemCt)
			{
				c.getItemCt = function() { return c.itemCt; };
				c.customItemCt = true;
			}

			var elementCell = c.getItemCt().child('td.x-form-element');
			
			if(!c.rendered)
			{
				c.render(elementCell);
			}
			else if(!validParent)
			{
				elementCell.dom.insertBefore(c.getPositionEl().dom, null);
			}
			
			if(c.resizeEl != null)
			{
				c.resizeEl.setStyle('position', 'relative');
				c.resizeEl.setStyle('float', 'left');
			}
			else
			{
				var r = c.getResizeEl();
				r.setStyle('position', 'relative');
				r.setStyle('float', 'left');
			}
			
			this.configureItem(c);
		}
		else if(!validParent)
		{
			Z8.layout.FormLayout.superclass.renderItem.call(this, c, position, target);
        }
	},
	
	isValidParent: function(c, target)
	{
		var el = c.getPositionEl();
		var up = el.up('table.x-table-layout', 10);
		return up != null && up.dom.parentNode === (target.dom || target);
	}
});

Ext.Container.LAYOUTS['z8.form'] = Z8.layout.FormLayout;