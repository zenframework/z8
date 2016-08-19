Z8.query.Section = Ext.extend(Ext.util.Observable, {
	header: '',
	controls: [],

	constructor : function(section) {
		if(Ext.isArray(section)) {
			var sections = section;
			for(var i = 0, length = sections.length; i < length; i++) {
				var section = sections[i];
				this.addControl(section);
			}
		} else if(section.isSection) {
			var controls = section.controls;
			for(var i = 0, length = controls.length; i < length; i++) {
				var control = controls[i];
				this.addControl(control);
			}
		} else
			this.addControl(section);

		Z8.query.Section.superclass.constructor.call(this);
	},

	addControl: function(control) {
		if(control.isSection) 
			this.controls.push(new Z8.query.Section(control));
		else
			this.controls.push(control);
	},
	
	getAllColumns: function() {
		var columns = [];

		for(var i = 0; i < this.controls.length; i++) {
			var control = this.controls[i];

			if(control.isSection) 
				columns = columns.concat(control.getAllColumns());
			else
				columns.push(control);
		}

		return columns;
	}
});
