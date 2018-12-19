Z8.define('Z8.calendar.Calendar', {
	extend: 'Z8.Component',

	time: true,

	initComponent: function() {
		this.callParent();

		this.date = this.date || new Date();
	},

	getDate: function() {
		return this.date;
	},

	htmlMarkup: function() {
		return Z8.calendar.Markup.get(this);
	},

	completeRender: function() {
		this.callParent();

		this.days = this.queryNodes('.calendar>.days>.day');
		this.selectedDay = this.selectNode('.calendar>.days>.day.selected');

		this.month = this.selectNode('.calendar>.header>.month');
		this.year = this.selectNode('.calendar>.header>.year');
		this.hour = this.selectNode('.calendar>.time>.hour');
		this.minute = this.selectNode('.calendar>.time>.minute');
		this.previousMonth = this.selectNode('.calendar>.header>.previous-month');
		this.nextMonth = this.selectNode('.calendar>.header>.next-month');

		this.yearDropdown = this.selectNode('.calendar>.header>.years');
		this.previousYear = this.selectNode('.calendar>.header>.years>.previous-year');
		this.nextYear = this.selectNode('.calendar>.header>.years>.next-year');
		this.years = this.queryNodes('.calendar>.header>.years>.year');
		this.selectedYear = this.selectNode('.calendar>.header>.years>.year.selected');

		this.monthDropdown = this.selectNode('.calendar>.header>.months');
		this.months = this.queryNodes('.calendar>.header>.months>.month');
		this.selectedMonth = this.selectNode('.calendar>.header>.months>.month.selected');

		this.hourDropdown = this.selectNode('.calendar>.time>.hours');
		this.hours = this.queryNodes('.calendar>.time>.hours>.hour');
		this.selectedHour = this.selectNode('.calendar>.time>.hours>.hour.selected');

		this.minuteDropdown = this.selectNode('.calendar>.time>.minutes');
		this.minutes = this.queryNodes('.calendar>.time>.minutes>.minute');
		this.selectedMinute = this.selectNode('.calendar>.time>.minutes>.minute.selected');

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.days = this.selectedDay = null;
		this.month = this.year = this.hour = this.minute = this.previousMonth = this.nextMonth = null;
		this.yearDropdown = this.previousYear = this.nextYear = this.years = this.selectedYear = null;
		this.monthDropdown = this.months = this.selectedMonth = null;
		this.hourDropdown = this.hours = this.selectedHour = null;
		this.minuteDropdown = this.minutes = this.selectedMinute = null;

		this.callParent();
	},

	currentYear: function() {
		return DOM.getIntAttribute(this.year, 'year');
	},

	currentMonth: function() {
		return DOM.getIntAttribute(this.month, 'month');
	},

	createDate: function(year, month) {
		var date = new Date(this.date);

		year = year != null ? year : this.currentYear();
		month = month != null ? month : this.currentMonth();
		var day = Math.min(date.getDate(), new Date(year, month).getDaysInMonth());

		date.setFullYear(year, month, day);
		return date;
	},

	set: function(value, datePart) {
		var date = !Date.isDate(value) ? new Date(this.date).set(value, datePart) : value;

		if(Date.isEqual(date, this.date) && date.getFullYear() == this.currentYear() && date.getMonth() == this.currentMonth())
			return;

		this.date = date;
		this.init(date, true);
	},

	init: function(date, force) {
		this.initDays(date, force);
		this.initMonths(date, force);

		var year = date.getFullYear();
		DOM.setValue(this.year, year);
		DOM.setAttribute(this.year, 'year', year);

		this.initHours(date.getHours(), force);
		this.initMinutes(date.getMinutes(), force);
	},

	initYears: function(year, force) {
		var first = year - year % 12;
		var years = this.years;

		this.select(null, 'selectedYear');

		for(var i = 0, length = years.length; i < length; i++) {
			var node = years[i];
			DOM.setValue(node, first + i);
			DOM.setAttribute(node, 'year', first + i);
			year == first + i ? this.select(node, 'selectedYear') : this.deselect(node, 'selectedYear');
		}

		if(this.selectedYear == null)
			this.select(years[0], 'selectedYear');
	},

	initMonths: function(date, force) {
		var month = date.getMonth();

		DOM.setValue(this.month, date.getMonthName());
		DOM.setAttribute(this.month, 'month', month);

		var months = this.months;
		for(var i = 0, length = months.length; i < length; i++)
			month == i ? this.select(months[i], 'selectedMonth') : this.deselect(months[i], 'selectedMonth');
	},

	initDays: function(date, force) {
		if(!force && date.getFullYear() == this.currentYear() && date.getMonth() == this.currentMonth())
			return;

		var today = new Date();
		var firstDate = date.getFirstDateOfMonth();
		var dayOfWeek = firstDate.getFirstDayOfMonth();
		var currentMonth = firstDate.getMonth();
		var firstDayIndex = dayOfWeek == 0 ? 7 : dayOfWeek;
		var dt = firstDate.add(-firstDayIndex, Date.Day);

		var days = this.days;

		this.select(null, 'selectedDay');

		for(var i = 0; i < 42; i++) {
			var day = days[i];
			var month = dt.getMonth();

			var selected = Date.isEqualDate(dt, this.date);

			selected ? this.select(day, 'selectedDay') : this.deselect(day, 'selectedDay');

			DOM.swapCls(day, month < currentMonth, 'previous-month');
			DOM.swapCls(day, month > currentMonth, 'next-month');
			DOM.swapCls(day, Date.isEqualDate(dt, today), 'today');

			DOM.setValue(day, dt.getDate());

			DOM.setAttribute(day, 'day', dt.getDate());
			DOM.setAttribute(day, 'month', month);
			DOM.setAttribute(day, 'year', dt.getFullYear());

			dt = dt.add(1, Date.Day);
		}

		if(this.selectedDay == null)
			this.select(days[firstDayIndex], 'selectedDay');
	},

	initHours: function(hour, force) {
		if(!force && hour == this.date.getHours())
			return;

		DOM.setValue(this.hour, (hour < 10 ? '0' : '') + hour);

		var hours = this.hours;
		for(var i = 0, length = hours.length; i < length; i++)
			hour == i ? this.select(hours[i], 'selectedHour') : this.deselect(hours[i], 'selectedHour');
	},

	initMinutes: function(minute, force) {
		if(!force && minute == this.date.getMinutes())
			return;

		DOM.setValue(this.minute, (minute < 10 ? '0' : '') + minute);

		var minutes = this.minutes;
		for(var i = 0, length = minutes.length; i < length; i++) {
			var selected = i * 5 <= minute && minute < (i + 1) * 5;
			selected ? this.select(minutes[i], 'selectedMinute') : this.deselect(minutes[i], 'selectedMinute');
		}
	},

	is: function(target, attribute) {
		return target.hasAttribute(attribute);
	},

	isDropdown: function(target, attribute) {
		return target.hasAttribute('dropdown') && this.is(target, attribute);
	},

	showDropdown: function(dropdown, focusAt) {
		if(this.dropdown == dropdown)
			return;

		if(this.dropdown != null)
			this.hideDropdown(this.dropdown);

		DOM.removeCls(dropdown, 'display-none');
		this.dropdown = dropdown;

		DOM.focus(focusAt);
		DOM.on(document.body, 'mouseDown', this.monitorDropdown, this);
	},

	hideDropdown: function(dropdown) {
		DOM.un(document.body, 'mouseDown', this.monitorDropdown, this);

		DOM.addCls(this.dropdown, 'display-none');
		this.dropdown = null;

		DOM.focus(this.selectedDay);
	},

	monitorDropdown: function(event) {
		var target = event.target;

		if(!DOM.isParentOf(this.dropdown, target))
			this.hideDropdown(this.dropdown);
	},

	deselect: function(value, property) {
		DOM.removeCls(value, 'selected');
		DOM.setTabIndex(value, -1);
	},

	select: function(value, property, focus) {
		this.deselect(this[property], property);

		DOM.addCls(value, 'selected');
		DOM.setTabIndex(value, 0);

		if(focus)
			DOM.focus(value);

		this[property] = value;
	},

	selectYear: function(year) {
		this.init(this.createDate(DOM.getIntAttribute(year, 'year')));
	},

	selectMonth: function(month) {
		this.init(this.createDate(this.currentYear(), DOM.getIntAttribute(month, 'month')));
	},

	selectDay: function(day, focus) {
		this.select(day, 'selectedDay', focus);

		var date = this.date;
		date.set(DOM.getIntAttribute(day, 'year'), Date.Year);
		date.set(DOM.getIntAttribute(day, 'month'), Date.Month);
		date.set(DOM.getIntAttribute(day, 'day'), Date.Day);
	},

	selectHour: function(hour) {
		var day = this.selectedDay;

		var date = this.createDate();
		date.set(DOM.getIntAttribute(day, 'day'), Date.Day);
		date.set(DOM.getIntAttribute(hour, 'hour'), Date.Hour);

		this.date = date;
		this.initHours(date.getHours(), true);
	},

	selectMinute: function(minute) {
		var day = this.selectedDay;

		var date = this.createDate();
		date.set(DOM.getIntAttribute(day, 'day'), Date.Day);
		date.set(DOM.getIntAttribute(minute, 'minute'), Date.Minute);

		this.date = date;
		this.initMinutes(date.getMinutes(), true);
	},

	focusYear: function(year) {
		this.select(year, 'selectedYear', true);
	},

	focusMonth: function(month) {
		this.select(month, 'selectedMonth', true);
	},

	focusDay: function(day) {
		this.selectDay(day, true);
	},

	focusHour: function(hour) {
		this.select(hour, 'selectedHour', true);
	},

	focusMinute: function(minute) {
		this.select(minute, 'selectedMinute', true);
	},

	nextIndex: function(index, direction, width, height) {
		if(direction == 'left')
			return Math.max(index - 1, 0);
		else if(direction == 'right')
			return Math.min(index + 1, width * height - 1); 
		else if(direction == 'up')
			return index - width >= 0 ? index - width : index;
		else if(direction == 'down')
			return index + width < width * height ? index + width : index;
		else
			throw 'unknown direction ' + direction;
	},

	selectNext: function(target, direction) {
		if(target == this.year) {
			this.toNextYear(direction == 'up' || direction == 'right' ? 1 : -1);
		} else if(target == this.month) {
			this.toNextMonth(direction == 'up' || direction == 'right' ? 1 : -1);
		} else if(target == this.hour) {
			this.toNextHour(direction == 'up' || direction == 'right' ? 1 : -1);
		} else if(target == this.minute) {
			this.toNextMinute(direction == 'up' || direction == 'right' ? 1 : -1);
		} else if(this.is(target, 'day')) {
			var index = DOM.getIntAttribute(target, 'index');
			var next = this.nextIndex(index, direction, 7, 6);
			if(next != index)
				this.focusDay(this.days[next]);
			else
				this.toNextDay(target, direction == 'up' ? -7 : (direction == 'down' ? 7 : (direction == 'left' ? -1 : 1)));
		} else if(this.is(target, 'year')) {
			var index = DOM.getIntAttribute(target, 'index');
			var next = this.nextIndex(index, direction, 3, 4);
			if(next != index)
				this.focusYear(this.years[next]);
			else
				this.scrollToNextYear(target, direction == 'up' ? -3 : (direction == 'down' ? 3 : (direction == 'left' ? -1 : 1)));
		} else if(this.is(target, 'month')) {
			var index = DOM.getIntAttribute(target, 'index');
			var next = this.nextIndex(index, direction, 3, 4);
			if(next != index)
				this.focusMonth(this.months[next]);
		} else if(this.is(target, 'hour')) {
			var index = DOM.getIntAttribute(target, 'index');
			var next = this.nextIndex(index, direction, 6, 4);
			if(next != index)
				this.focusHour(this.hours[next]);
		} else if(this.is(target, 'minute')) {
			var index = DOM.getIntAttribute(target, 'index');
			var next = this.nextIndex(index, direction, 4, 3);
			if(next != index)
				this.focusMinute(this.minutes[next]);
		}
	},

	toNextYear: function(years) {
		var date = this.createDate().add(years, Date.Year);
		this.init(date);
	},

	toNextMonth: function(months) {
		var date = this.createDate().add(months, Date.Month);
		this.init(date);
	},

	toNextDay: function(day, days) {
		var year = DOM.getIntAttribute(day, 'year');
		var month = DOM.getIntAttribute(day, 'month');
		var date = this.createDate(year, month).add(days, Date.Day);
		this.set(date);
		this.focusDay(this.selectedDay);
	},

	toNextHour: function(hours) {
		var date = this.createDate().add(hours, Date.Hour);
		this.set(date);
	},

	toNextMinute: function(minutes) {
		var date = this.createDate().add(minutes, Date.Minute);
		this.set(date);
	},

	scrollYears: function(years) {
		var year = DOM.getIntAttribute(this.years[0], 'year');
		this.initYears(year + 12 * years);
	},

	scrollToNextYear: function(year, years) {
		var index = DOM.getIntAttribute(year, 'index') + years;
		this.initYears(DOM.getIntAttribute(year, 'year') + years);
		index += index < 0 ? 12 : - 12;
		this.select(this.years[index], 'selectedYear', true);
	},

	onDayClick: function(day) {
		this.selectDay(day);
		this.fireEvent('dayClick', new Date(this.date), this);
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(this.is(target, 'dropdown')) {
			if(this.is(target, 'year'))
				this.selectYear(target);
			else if(this.is(target, 'month'))
				this.selectMonth(target);
			else if(this.is(target, 'hour'))
				this.selectHour(target);
			if(this.isDropdown(target, 'minute'))
				this.selectMinute(target);
			this.hideDropdown(target);
		} else if(target == this.year) {
			this.initYears(this.currentYear());
			this.showDropdown(this.yearDropdown, this.selectedYear);
		} else if(target == this.month)
			this.showDropdown(this.monthDropdown, this.selectedMonth);
		else if(target == this.hour)
			this.showDropdown(this.hourDropdown, this.selectedHour);
		else if(target == this.minute)
			this.showDropdown(this.minuteDropdown, this.selectedMinute);
		else if(target == this.previousMonth)
			this.toNextMonth(-1);
		else if(target == this.nextMonth)
			this.toNextMonth(1);
		else if(target == this.previousYear)
			this.scrollYears(-1);
		else if(target == this.nextYear)
			this.scrollYears(1);
		else if(this.is(target, 'day'))
			this.onDayClick(target);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();
		var dropdown = this.dropdown;

		if(key == Event.ESC && dropdown != null)
			this.hideDropdown(dropdown);
		else if(key == Event.ENTER || key == Event.SPACE)
			this.onClick(event, target);
		else if(key == Event.LEFT)
			this.selectNext(target, 'left');
		else if(key == Event.RIGHT)
			this.selectNext(target, 'right');
		else if(key == Event.UP)
			this.selectNext(target, 'up');
		else if(key == Event.DOWN)
			this.selectNext(target, 'down');
		else if(key == Event.TAB && dropdown != null) {
			if(dropdown == this.yearDropdown) {
				if(target == this.nextYear && !event.shiftKey)
					DOM.focus(this.previousYear);
				else if(target == this.previousYear && event.shiftKey)
					DOM.focus(this.nextYear);
				else
					return;
			} else
				this.hideDropdown(dropdown);
		} else
			return;

		event.stopEvent();
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this.selectedDay != null ? this.selectedDay : this.days[0]) : false;
	}
});