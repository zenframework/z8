Z8.query.Section = Ext.extend(Ext.util.Observable, {
	header: '',
	controls: [],

	constructor : function(section) {
		if(Ext.isArray(section)) {
			var sections = section;
			for(var i = 0, length = sections.length; i < length; i++) {
				var section = sections[i];
				if(section.isSection) 
					this.controls[i] = new Z8.query.Section(section);
				else
					this.controls[i] = section;
			}
		} else {
			for(var i = 0; i < controls.length; i++) {
				var control = controls[i];
				if(control.isSection) 
					this.controls[i] = new Z8.query.Section(control);
				else
					this.controls[i] = control;
			}
		}

		Z8.query.Section.superclass.constructor.call(this);
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
