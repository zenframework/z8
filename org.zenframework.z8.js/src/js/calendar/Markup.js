/*
	<div class="calendar">
		<div class="header">
			<span class="previous-month"/>
			<span class="month"/>
			<span class="year"/>
			<span class="next-month">
			<div class="months">
				<div class="month"/>
			</div>
			<div class="years"/>
				<div class="year-previous/>
				<div class="year year-0"/> ... <div class="year year-11"/>  
				<div class="year-next"/>
			</div>
		</div>
		<div class="days-of-week">
			<div class="day"/> ... <div class="day"/>
		</div>
		<div class="days">
			<div class="day day-0"/> ... <div class="day day-41"/>
		</div>
		<div class="time">
			<span class="icon"/>
			<span class="hour"/>
			<span class="minute"/>
			<div class="hours">
				<div class="hour hour-0"/> ... <div class="hour hour-23"/>
			</div>
			<div class="minutes">
				<div class="minute minute-0"/> ... <div class="minute minute-11"/>
			</div>
		</div>
	</div>
*/

Z8.define('Z8.calendar.Markup', {
	statics: {
		get: function(calendar) {
			var date = calendar.date;
			var header = this.getHeader(date);
			var daysOfWeek = this.getDaysOfWeek(date);
			var days = this.getDays(date);
			var time = calendar.time ? this.getTime(date) : null;

			var cls = DOM.parseCls(calendar.cls).pushIf('calendar').join(' ');
			return { cls: cls, id: calendar.id, cn: [header, daysOfWeek, days, time] };
		},

		getHeader: function(date) {
			return { cls: 'header', cn: [
				{ tag: 'span', cls: 'fa fa-chevron-left previous-month', tabIndex: 0 },
				{ tag: 'span', month: date.getMonth(), cls: 'month', tabIndex: 0, html: date.getMonthName() },
				{ tag: 'span', year: date.getFullYear(), cls: 'year', tabIndex: 0, html: date.getFullYear() },
				{ tag: 'span', cls: 'fa fa-chevron-right next-month', tabIndex: 0 },
				this.getMonths(date),
				this.getYears(date)
			]};
		},

		getYears: function(date) {
			var years = [];
			var year = date.getFullYear();

			years.push({ cls: 'fa fa-chevron-left previous-year', tabIndex: 0 });

			for(var i = 0; i < 12; i++)
				years.push({ cls: 'year year-' + i + (i == year ? ' selected' : ''), tabIndex: year == i ? 0 : -1, index: i, year: year + i, dropdown: true, html: year + i });

			years.push({ cls: 'fa fa-chevron-right next-year', tabIndex: 0 });

			return { cls: 'years display-none', cn: years };
		},

		getMonths: function(date) {
			var months = [];
			var month = date != null ? date.getMonth() : -1;

			for(var i = 0; i < 12; i++)
				months.push({ cls: 'month month-' + i + (month == i ? ' selected' : ''), tabIndex: month == i ? 0 : -1, index: i, month: i, dropdown: true, html: Date.MonthShortNames[i] });

			return { cls: 'months display-none', cn: months };
		},

		getDaysOfWeek: function(date) {
			var daysOfWeek = [];

			for(i = 0; i < 7; i++) {
				var cls = 'day' + (i > 4 ? ' week-end' : '');
				daysOfWeek.push({ cls: cls, html: Date.DayShortNames[i] });
			}

			return { cls: 'days-of-week', cn: daysOfWeek };
		},

		getDays: function(date) {
			var firstDate = date.getFirstDateOfMonth();
			var dayOfWeek = firstDate.getFirstDayOfMonth();
			var currentMonth = firstDate.getMonth();
			var today = new Date();

			var days = [];
			var dt = firstDate.add(dayOfWeek == 0 ? -7 : -dayOfWeek, Date.Day);

			for(var i = 0; i < 42; i++) {
				var day = dt.getDate();
				var month = dt.getMonth();
				var year = dt.getFullYear();
				var selected = Date.isEqualDate(dt, date);

				var cls = ['day', 'day-' + i];

				if(i % 7 > 4)
					cls.push('week-end');
				if(month < currentMonth)
					cls.push('previous-month');
				if(month > currentMonth)
					cls.push('next-month');
				if(selected)
					cls.push('selected');
				if(Date.isEqualDate(dt, today))
					cls.push('today');

				days.push({ cls: cls.join(' '), index: i, day: day, month: month, year: year, tabIndex: selected ? 0 : -1, html: day });

				dt.add(1, Date.Day);
			}

			return { cls: 'days', cn: days };
		},

		getTime: function(date) {
			var hours = date.getHours();
			var minutes = date.getMinutes();

			return { cls: 'time', cn: [
				{ tag: 'span', cls: 'fa fa-clock-o icon' },
				{ tag: 'span', hour: date.getHours(), cls: 'hour', tabIndex: 0, html: (hours < 10 ? '0' : '') + hours },
				':',
				{ tag: 'span', minute: date.getMinutes(), cls: 'minute', tabIndex: 0, html: (minutes < 10 ? '0' : '') + minutes },
				this.getHours(),
				this.getMinutes()
			]};
		},

		getHours: function(date) {
			var hours = [];
			var hour = date != null ? date.getHours() : -1;

			for(var i = 0; i < 24; i++)
				hours.push({ cls: 'hour hour-' + i + (hour == i ? ' selected' : ''), tabIndex: hour == i ? 0 : -1, index: i, hour: i, dropdown: true, html: (i < 10 ? '0' : '') + i });

			return { cls: 'hours display-none', cn: hours };
		},

		getMinutes: function(date) {
			var minutes = [];
			var minute = date != null ? date.getMinutes() : -1;

			for(var i = 0; i < 12; i++) {
				var selected = i * 5 <= minute && minute < (i + 1) * 5;
				minutes.push({ cls: 'minute minute-' + i + (selected ? ' selected' : ''), tabIndex: selected ? 0 : -1, index: i, minute: i * 5, dropdown: true, html: (i * 5 < 10 ? '0' : '') + i * 5 });
			}
			return { cls: 'minutes display-none', cn: minutes };
		}
	}
});