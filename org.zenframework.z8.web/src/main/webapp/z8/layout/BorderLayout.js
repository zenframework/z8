Z8.layout.BorderLayout = Ext.extend(Ext.layout.BorderLayout,
{
	onLayout: function(ct, target)
	{
		if(this.east)
		{
			var width = this.east.panel.width;

			if(Ext.isString(width) && width.indexOf('%') != -1)
			{
				var cachedWidth = this.east.panel.cachedWidth;
				var actualWidth = this.east.panel.getWidth();
				var containerWidth = ct.getWidth();
			
				if((cachedWidth == null || cachedWidth == actualWidth) && actualWidth != 0)
				{
					this.east.panel.setWidth(this.center.isVisible() ? width : '100%');
					this.east.panel.cachedWidth = this.east.panel.getWidth();
				}
				else if(containerWidth != 0)
				{
					if(this.east.isVisible())
					{
						this.east.panel.width = Math.ceil(actualWidth * 100.0 / ct.getWidth()) + '%';
						this.east.panel.cachedWidth = actualWidth;
					}
				}
			}
		}

		Z8.layout.BorderLayout.superclass.onLayout.call(this, ct, target);
	}
	
});

Ext.Container.LAYOUTS.border = Z8.layout.BorderLayout;