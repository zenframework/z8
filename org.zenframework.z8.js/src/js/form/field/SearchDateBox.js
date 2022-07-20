Z8.define('Z8.form.field.SearchDateBox', {
	extend: 'Z8.form.field.SearchText',
	shortClassName: 'SearchDateBox',

	period: { 
		start: new Date(), 
		finish: new Date() 
	},
	format: Format.Datetime,
	
	constructor: function(config) {
		config = config || {};
		config.confirm = config.confirm !== false;
		config.searchIcon =  config.searchIcon || 'fa-search';
		config.clearIcon = config.clearIcon || 'fa-times';
		
		Z8.form.field.Text.prototype.constructor.call(this, config);
	},

	getFilter: function() {
		if(this.field == null)
			return null;

		var value = this.input.getAttribute("title");
		var periodDate = !Z8.isEmpty(value) ? value.split("\n"): [];
		var filters = [];
		var start = periodDate.length == 2 ? Parser.date(periodDate[0], this.format) : Parser.date(value, this.format);
		var finish = periodDate.length == 2 ? Parser.date(periodDate[1], this.format) : Parser.date(value, this.format);

		if(Date.isDate(start)) {
			start.setSeconds(00);
			start.setMilliseconds(000);
			filters.push({ property: this.field.name, operator: Operator.GE, value: start });
		}
		if(Date.isDate(finish)) {
			finish.setSeconds(59);
			finish.setMilliseconds(999);
			filters.push({ property: this.field.name, operator: Operator.LE, value: finish });
		}
		return !Z8.isEmpty(value) && !filters.isEmpty() ? filters : null;
	},

	initTriggers: function (){
		this.callParent();
		this.triggers.push({ icon: 'fa-calendar', tooltip: 'Выбрать период', handler: this.showPeriodMenu, scope: this });

		TextBox.prototype.initTriggers.call(this);
	},

	showPeriodMenu: function() {
		DOM.append(this, this.menu);
		this.menu.setAlignment(this);
		this.menu.toggle();
	},

	completeRender: function() {
		Z8.form.field.Text.prototype.completeRender.call(this);
		var periodControl = new Z8.calendar.Period({ period: this.period });
		periodControl.on('apply', this.onPeriodApply, this);
		periodControl.on('cancel', this.onPeriodCancel, this);
		
		var menu = this.menu = new Z8.menu.Menu({ items: [periodControl], useTab: false });
		menu.render();
		this.updateTrigger();
	},

	onPeriodCancel: function(control) {
		this.menu.hide();
	},

	onPeriodApply: function(control, start, finish) {
		this.menu.hide();

		if(control.period == null)
			return;

		var period = control.period;
		period.start = start;
		period.finish = finish;
		var startDate = Format.formatDate(period.start, this.format);
		var finsihDate = Format.formatDate(period.finish, this.format);
		this.setValue(this.formatPeriod(period.start, period.finish));
		this.input.setAttribute("title", startDate + "\n" + finsihDate)
		this.searchPending = this.value != this.lastSearchValue;
		this.updateTrigger();
	},

	formatPeriod: function(start, finish) {
		var onlyStartDate = start.toLocaleString(undefined, {
			month: "short", day: "numeric", 
			hour: undefined, minute: undefined, second: undefined
		});
		var onlyFinishDate = finish.toLocaleString(undefined, {
			month: "short", day: "numeric", 
			hour: undefined, minute: undefined, second: undefined
		});					

		if(Format.formatDate(start, this.format) == Format.formatDate(finish, this.format))
			return Format.formatDate(start, this.format);

		if(start.getFullYear() == finish.getFullYear()) {
			if(start.getMonth() == finish.getMonth() && start.getDate() == finish.getDate())
				return start.toLocaleTimeString() + " - " + finish.toLocaleTimeString()
			return start.getFullYear() + ", " + onlyStartDate + " - " + onlyFinishDate;
		}
		return start.getFullYear() + " - " + finish.getFullYear();
	},

	getSelection: function() {
		if(typeof this.list !== 'undefined') {
			var currentItem = this.list.getCurrentItem();
			if(currentItem != null) {
				var record = currentItem.getRecord();
				if(record != null) {
					this.setValue(Format.formatDate(record.data[this.field.name], this.format));
					this.searchPending = this.value != this.lastSearchValue;
					this.updateTrigger();
				}
			}
		}
	},
});
