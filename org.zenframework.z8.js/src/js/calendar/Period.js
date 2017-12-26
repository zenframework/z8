Z8.define('Z8.calendar.Period', {
	extend: 'Z8.Container',

	cls: 'period',

	startDate: function() {
		var start = this.start.getDate();
		var finish = this.finish.getDate();
		return start.getTime() < finish.getTime() ? start : finish;
	},

	finishDate: function() {
		var start = this.start.getDate();
		var finish = this.finish.getDate();
		return start.getTime() < finish.getTime() ? finish : start;
	},

	today: function() {
		return {
			start: new Date().truncDay(),
			finish: new Date().truncDay().add(1, Date.Day).add(-1, Date.Second)
		};
	},

	yesterday: function() {
		return {
			start: new Date().truncDay().add(-1, Date.Day),
			finish: new Date().truncDay().add(-1, Date.Second)
		};
	},

	dayBeforeYesterday: function() {
		return {
			start: new Date().truncDay().add(-2, Date.Day),
			finish: new Date().truncDay().add(-1, Date.Day).add(-1, Date.Second)
		};
	},

	last7Days: function() {
		return {
			start: new Date().truncDay().add(-7, Date.Day),
			finish: new Date()
		};
	},

	tomorrow: function() {
		return {
			start: new Date().truncDay().add(1, Date.Day),
			finish: new Date().truncDay().add(2, Date.Day).add(-1, Date.Second)
		};
	},

	thisWeek: function() {
		return {
			start: new Date().truncDay().getFirstDateOfWeek(),
			finish: new Date().truncDay().getFirstDateOfWeek().add(7, Date.Day).add(-1, Date.Second)
		};
	},

	lastWeek: function() {
		return {
			start: new Date().truncDay().getFirstDateOfWeek().add(-7, Date.Day),
			finish: new Date().truncDay().getFirstDateOfWeek().add(-1, Date.Second)
		};
	},

	nextWeek: function() {
		return {
			start: new Date().truncDay().getFirstDateOfWeek().add(7, Date.Day),
			finish: new Date().truncDay().getFirstDateOfWeek().add(14, Date.Day).add(-1, Date.Second)
		};
	},

	thisMonth: function() {
		return {
			start: new Date().getFirstDateOfMonth(),
			finish: new Date().getFirstDateOfMonth().add(1, Date.Month).add(-1, Date.Second)
		};
	},

	lastMonth: function() {
		return {
			start: new Date().getFirstDateOfMonth().add(-1, Date.Month),
			finish: new Date().getFirstDateOfMonth().add(-1, Date.Second)
		};
	},

	nextMonth: function() {
		return {
			start: new Date().getFirstDateOfMonth().add(1, Date.Month),
			finish: new Date().getFirstDateOfMonth().add(2, Date.Month).add(-1, Date.Second)
		};
	},

	thisQuarter: function() {
		return {
			start: new Date().getFirstDateOfQuarter(),
			finish: new Date().getFirstDateOfQuarter().add(3, Date.Month).add(-1, Date.Second)
		};
	},

	lastQuarter: function() {
		return {
			start: new Date().getFirstDateOfQuarter().add(-3, Date.Month),
			finish: new Date().getFirstDateOfQuarter().add(-1, Date.Second)
		};
	},

	nextQuarter: function() {
		return {
			start: new Date().getFirstDateOfQuarter().add(3, Date.Month),
			finish: new Date().getFirstDateOfQuarter().add(6, Date.Month).add(-1, Date.Second)
		};
	},

	thisHalfYear: function() {
		return {
			start: new Date().getFirstDateOfHalfYear(),
			finish: new Date().getFirstDateOfHalfYear().add(6, Date.Month).add(-1, Date.Second)
		};
	},

	lastHalfYear: function() {
		return {
			start: new Date().getFirstDateOfHalfYear().add(-6, Date.Month),
			finish: new Date().getFirstDateOfHalfYear().add(-1, Date.Second)
		};
	},

	nextHalfYear: function() {
		return {
			start: new Date().getFirstDateOfHalfYear().add(6, Date.Month),
			finish: new Date().getFirstDateOfHalfYear().add(12, Date.Month).add(-1, Date.Second)
		};
	},

	thisYear: function() {
		return {
			start: new Date().getFirstDateOfYear(),
			finish: new Date().getFirstDateOfYear().add(1, Date.Year).add(-1, Date.Second)
		};
	},

	lastYear: function() {
		return {
			start: new Date().getFirstDateOfYear().add(-1, Date.Year),
			finish: new Date().getFirstDateOfYear().add(-1, Date.Second)
		};
	},

	nextYear: function() {
		return {
			start: new Date().getFirstDateOfYear().add(1, Date.Year),
			finish: new Date().getFirstDateOfYear().add(2, Date.Year).add(-1, Date.Second)
		};
	},

	htmlMarkup: function() {
		var period = this.period;
		var last7Days = this.last7Days();

		var start = this.start = new Z8.calendar.Calendar({ cls: 'start', date: period.start || last7Days.start });
		var finish = this.finish = new Z8.calendar.Calendar({ cls: 'finish', date: period.finish || last7Days.finish });

		var today = new Z8.button.Button({ text: 'Сегодня', name: 'today', toggled: true });
		var yesterday = new Z8.button.Button({ text: 'Вчера', name: 'yesterday', toggled: false });
		var dayBeforeYesterday = new Z8.button.Button({ text: 'Позавчера', name: 'dayBeforeYesterday', toggled: false });
		var last7Days = new Z8.button.Button({ text: '7 дней', name: 'last7Days', toggled: false });
		var tomorrow = new Z8.button.Button({ text: 'Завтра', name: 'tomorrow', toggled: false });

		var separator = { cls: 'separator' };

		var thisWeek = new Z8.button.Button({ text: 'Текущая неделя', name: 'thisWeek', toggled: false });
		var lastWeek = new Z8.button.Button({ text: 'Прошлая неделя', name: 'lastWeek', toggled: false });
		var nextWeek = new Z8.button.Button({ text: 'Следующая неделя', name: 'nextWeek', toggled: false });

		var thisMonth = new Z8.button.Button({ text: 'Текущий месяц', name: 'thisMonth', toggled: false });
		var lastMonth = new Z8.button.Button({ text: 'Прошлый месяц', name: 'lastMonth', toggled: false });
		var nextMonth = new Z8.button.Button({ text: 'Следующий месяц', name: 'nextMonth', toggled: false });

		var selector1 = new Z8.button.Group({ cls: 'selector', items: [today, yesterday, dayBeforeYesterday, last7Days, tomorrow, separator, thisWeek, lastWeek, nextWeek, separator, thisMonth, lastMonth, nextMonth], radio: true, vertical: true })

		var thisQuarter = new Z8.button.Button({ text: 'Текущий квартал', name: 'thisQuarter', toggled: false });
		var lastQuarter = new Z8.button.Button({ text: 'Прошлый квартал', name: 'lastQuarter', toggled: false });
		var nextQuarter = new Z8.button.Button({ text: 'Следующий квартал', name: 'nextQuarter', toggled: false });

		var thisHalfYear = new Z8.button.Button({ text: 'Текущее полугодие', name: 'thisHalfYear', toggled: false });
		var lastHalfYear = new Z8.button.Button({ text: 'Прошлое полугодие', name: 'lastHalfYear', toggled: false });
		var nextHalfYear = new Z8.button.Button({ text: 'Следующеее полугодие', name: 'nextHalfYear', toggled: false });

		var thisYear = new Z8.button.Button({ text: 'Текущий год', name: 'thisYear', toggled: false });
		var lastYear = new Z8.button.Button({ text: 'Прошлый год', name: 'lastYear', toggled: false });
		var nextYear = new Z8.button.Button({ text: 'Следующий год', name: 'nextYear', toggled: false });

		var selector2 = new Z8.button.Group({ cls: 'selector', items: [thisQuarter, lastQuarter, nextQuarter, separator, thisHalfYear, lastHalfYear, nextHalfYear, separator, thisYear, lastYear, nextYear], radio: true, vertical: true })

		this.attachListeners(selector1.items);
		this.attachListeners(selector2.items);

		var body = new Z8.Container({ cls: 'body', items: [start, finish, selector1, selector2] });

		var cancel = new Z8.button.Button({ text: 'Закрыть', handler: this.onCancel, scope: this });
		var ok = new Z8.button.Button({ text: 'Применить', primary: true, handler: this.onApply, scope: this });
		var buttons = new Z8.Container({ cls: 'buttons', items: [cancel, ok] });

		this.items = [body, buttons];

		return this.callParent();
	},

	attachListeners: function(items) {
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isComponent)
				item.on('toggle', this.onToggle, this);
		}
	},

	onToggle: function(button, toggled) {
		if(toggled && button.name != null) {
			var period = this[button.name]();
			this.start.set(period.start);
			this.finish.set(period.finish);
		}
	},

	onCancel: function() {
		this.fireEvent('cancel', this);
	},

	onApply: function() {
		this.fireEvent('apply', this, this.startDate(), this.finishDate());
	}
});