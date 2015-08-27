Z8.layout.MulticolumnMenuLayout = Ext.extend(Ext.layout.MenuLayout,
{
    renderItem: function(c, position, target)
    {
		if(!this.itemTpl)
		{
			this.itemTpl = Z8.layout.MulticolumnMenuLayout.prototype.itemTpl = new Ext.XTemplate(
				'<li id="{itemId}" class="{itemCls}">',
					'<tpl if="needsIcon">',
						'<img alt="{altText}" src="{icon}" class="{iconCls}"/>',
					'</tpl>',
				'</li>'
			);
		}

		target = this.container.getLayoutTargetForItem(c);
		
		if(c && !c.rendered)
		{
			if(Ext.isNumber(position))
			{
				position = target.dom.childNodes[position];
			}
			
			var a = this.getItemArgs(c);

			// The Component's positionEl is the <li> it is rendered into
			c.render(c.positionEl = position ? this.itemTpl.insertBefore(position, a, true) : this.itemTpl.append(target, a, true));

			// Link the containing <li> to the item.
			c.positionEl.menuItemId = c.getItemId();

			// If rendering a regular Component, and it needs an icon, move the Component rightwards.
			if(!a.isMenuItem && a.needsIcon)
			{
				c.positionEl.addClass('x-menu-list-item-indent');
			}
            
			this.configureItem(c);
		}
		else if(c && !this.isValidParent(c, target))
		{
			if(Ext.isNumber(position))
			{
				position = target.dom.childNodes[position];
			}
			
			target.dom.insertBefore(c.getActionEl().dom, position || null);
		}
	}
});

Ext.Container.LAYOUTS['multicolumnmenu'] = Z8.layout.MulticolumnMenuLayout;
