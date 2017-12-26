Z8.define('Z8.filter.Element', {
	extend: 'Z8.Container',

	toggle: function(toggle) {
		DOM.swapCls(this, toggle, 'active');
		if(!this.blockChildToggle)
			this.onChildToggle(toggle, this);
	},

	onChildChange: function(child) {
		this.container.onChildChange(child);
	},

	onChildToggle: function(toggle, child) {
		this.container.onChildToggle(toggle, child);
	},

	onContainerToggle: function(toggle, container) {
		if(this.container != container || this == container) {
			this.blockChildToggle = true;
			this.toggle(false);
			delete this.blockChildToggle;
		}
	},

	isSelected: function() {
		return false;
	},

	getSelection: function() {
		return [];
	},

	getFields: function() {
		return this.fields || (this.fields = this.container.getFields());
	}
});
