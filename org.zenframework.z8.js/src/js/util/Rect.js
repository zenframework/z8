Z8.define('Z8.Rect', {
	shortClassName: 'Rect',

	left: 0,
	top: 0,
	right: 0,
	bottom: 0,

	constructor: function(left, top, right, bottom) {
		if(Number.isNumber(left)) {
			this.left = Math.min(left, right);
			this.top = Math.min(top, bottom);
			this.right = Math.max(left, right);
			this.bottom = Math.max(top, bottom);
		} else if (left != null) {
			var dom = DOM.get(left);
			if(dom != null) {
				var rect = dom.getBoundingClientRect();
				this.left = Ems.pixelsToEms(rect.left);
				this.top = Ems.pixelsToEms(rect.top);
				this.right = Ems.pixelsToEms(rect.right);
				this.bottom = Ems.pixelsToEms(rect.bottom);
			} else {
				var rect = left;
				this.left = rect.left;
				this.top = rect.top;
				this.right = rect.right;
				this.bottom = rect.bottom;
			}
		}

		this.width = this.right - this.left;
		this.height = this.bottom - this.top;
	},

	offset: function(cx, cy) {
		this.left += cx;
		this.right += cx;
		this.top += cy || 0;
		this.bottom += cy || 0;
		return this;
	}
});