Z8.define('Z8.calendar.Dropdown', {
	extend: 'Z8.calendar.Calendar',

	cls: 'dropdown-calendar display-none',
	visible: false,
	autoAlign: true,

	show: function(top, left) {
		if(this.isVisible()) {
			DOM.focus(this.selectedDay);
			return;
		}

		this.setPosition(top, left);

		this.callParent();
		this.align();

		this.fireEvent('show', this);

		DOM.focus(this.selectedDay);
	},

	hide: function() {
		if(!this.isVisible())
			return;

		this.callParent();

		this.fireEvent('hide', this);
	},

	toggle: function() {
		this.visible ? this.hide() : this.show();
	},

	onDayClick: function(day) {
		this.hide();
		this.callParent(day);
	},

	onCancel: function() {
		this.hide();
		this.fireEvent('cancel', this);
	},

	onKeyDown: function(event, target) {
		if(this.callParent(event, target))
			return;

		var key = event.getKey();
	
		if(key == Event.ESC)
			this.onCancel();
		else if(key == Event.TAB) {
			if(!event.shiftKey && target == this.minute)
				DOM.focus(this.previousMonth);
			else if(event.shiftKey && target == this.previousMonth)
				DOM.focus(this.minute);
			else
				return false;
		} else
			return false;

		event.stopEvent();
		return true;
	},

	align: function() {
		var alignment = this.alignment;

		var style = DOM.getComputedStyle(this);
		var fixed = style.position == 'fixed';

		var clipping = fixed ? document.body : this.getClipping();

		var viewport = new Rect(clipping);

		var align = new Rect(alignment || this);

		if(alignment == null) {
			align.height = align.width = 0;
			align.bottom = align.top;
			align.right = align.left;
		}

		var parent = new Rect(DOM.getParent(this));

		var offset = this.alignmentOffset || { width: 0, height: 0, margin: 1, adjustHeight: true };
		var style = DOM.getComputedStyle(this);

		var height = parseFloat(style.height) + parseInt(style.marginTop) + parseInt(style.marginBottom);
		height = Ems.pixelsToEms(height) + offset.height;
		var width = parseFloat(style.width) + parseInt(style.marginLeft) + parseInt(style.marginRight);
		width = Ems.pixelsToEms(width) + offset.width;

		var spaceLeft = align.left - viewport.left;
		var spaceAbove = align.top - viewport.top;
		var spaceRight = viewport.right - align.left;
		var spaceBelow = viewport.bottom - align.bottom;

		var above = false;
		var available = spaceBelow;

		if(spaceBelow < height) {
			if(spaceAbove >= height || spaceBelow < spaceAbove) {
				above = true;
				available = spaceAbove;
			}
		}

		var rect = new Rect();

		width += offset.margin;
		rect.left = align.left + (spaceLeft > Math.abs(align.width - width) ? align.width - width + offset.margin : 0);

		if(above) {
			rect.bottom = align.top - offset.height;
			rect.top = rect.bottom - (Math.min(available, height) - offset.height) + (available < height ? offset.margin : 0);
		} else {
			rect.top = align.top + align.height;
			rect.bottom = rect.top + (Math.min(available, height) - offset.height) - (available < height ? offset.margin : 0);
		}

		rect.left = rect.left - (fixed ? 0 : parent.left);
		rect.top = rect.top - (fixed ? 0 : parent.top);
		rect.bottom = (fixed ? viewport.bottom : parent.bottom) - rect.bottom;

		DOM.setLeft(this, Ems.emsToPixels(rect.left) + 'px');
		DOM.setTop(this, Ems.emsToPixels(rect.top) + 'px');
		if(alignment != null && offset.adjustHeight)
			DOM.setBottom(this, Ems.emsToPixels(rect.bottom) + 'px');
		DOM.swapCls(this, above, 'pull-up', 'pull-down');

		this.fireEvent('align', this);
	},
});