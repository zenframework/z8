Date.Milli = 'ms';
Date.Second = 's';
Date.Minute = 'mi';
Date.Hour = 'h';
Date.Day = 'd';
Date.Month = 'mo';
Date.Year = 'y';

Date.DaysInMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
Date.MillisPerDay = 864e5;
Date.MillisPerWeek = 7 * Date.MillisPerDay;

Date.MonthNames = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'];
Date.MonthShortNames = ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек'];
Date.DayNames = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье'];
Date.DayShortNames = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

Date.isDate = function(value) {
	return toString.call(value) === '[object Date]';
};

Date.isEqual = function(date1, date2) {
	return date1 && date2 ? date1.getTime() == date2.getTime() : false;
};

Date.isEqualMonth = function(date1, date2) {
	if(date1 && date2)
		return date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth();
	return false;
};

Date.isEqualDate = function(date1, date2) {
	if(date1 && date2)
		return date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth() && date1.getDate() == date2.getDate();
	return false;
};


Date.prototype.getMonthName = function() {
	return Date.MonthNames[this.getMonth()];
};

Date.prototype.getMonthShortName = function() {
	return Date.MonthShortNames[this.getMonth()];
};

Date.prototype.getDayName = function() {
	return Date.dayNames[this.getDayOfWeek()];
};

Date.prototype.getDayShortName = function() {
	return Date.dayShortNames[this.getDayOfWeek()];
};

Date.prototype.getDayOfWeek = function() {
	var day = this.getDay();
	return day != 0 ? day - 1 : 6;
};

Date.prototype.getFirstDateOfWeek = function() {
	var dayOfWeek = this.getDayOfWeek();
	var date = new Date(this);
	return date.add(-dayOfWeek, Date.Day);
};

Date.prototype.getWeekOfYear = function() {
	var DC3 = Date.UTC(this.getFullYear(), this.getMonth(), this.getDate() + 3) / Date.MillisPerDay; // an Absolute Day Number 
	var absoluteWeekNumber = Math.floor(DC3 / 7); // an Absolute Week Number 
	var wyr = new nativeDate(absoluteWeekNumber * Date.MillisPerWeek).getUTCFullYear();
	return absoluteWeekNumber - Math.floor(Date.UTC(wyr, 0, 7) / Date.MillisPerWeek) + 1;
};

Date.prototype.isLeapYear = function() {
	var year = this.getFullYear();
	return !!((year & 3) === 0 && (year % 100 || (year % 400 === 0 && year)));
};

Date.prototype.getFirstDayOfMonth = function() {
	var day = (this.getDay() - (this.getDate() - 1)) % 7;
	day = (day < 0) ? (day + 7) : day;
	return day != 0 ? day - 1 : 6;
};

Date.prototype.getLastDayOfMonth = function() {
	return this.getLastDateOfMonth().getDay();
};

Date.prototype.getFirstDateOfMonth = function() {
	return new Date(this.getFullYear(), this.getMonth(), 1);
};

Date.prototype.getLastDateOfMonth = function() {
	return new Date(this.getFullYear(), this.getMonth(), this.getDaysInMonth());
};

Date.prototype.getFirstDateOfQuarter = function() {
	var month = this.getMonth();
	return new Date(this.getFullYear(), month < 3 ? 0 : (month < 6 ? 3 : (month < 9 ? 6 : 9)), 1);
};

Date.prototype.getFirstDateOfHalfYear = function() {
	return new Date(this.getFullYear(), this.getMonth() < 6 ? 0 : 6, 1);
};

Date.prototype.getFirstDateOfYear = function() {
	return new Date(this.getFullYear(), 0, 1);
};

Date.prototype.getDaysInMonth = function() {
	var month = this.getMonth();
	return month === 1 && this.isLeapYear() ? 29 : Date.DaysInMonth[month];
};

Date.prototype.set = function(value, part) {
	if(part == Date.Year)
		this.setFullYear(value);
	else if(part == Date.Month)
		this.setMonth(value);
	else if(part == Date.Day)
		this.setDate(value);
	else if(part == Date.Hour)
		this.setHours(value);
	else if(part == Date.Minute)
		this.setMinutes(value);
	else if(part == Date.Second)
		this.setSeconds(value);
	else if(part == Date.Milli)
		this.setMilliseconds(value);

	return this;
};

Date.prototype.add = function(value, datePart) {
	if (!datePart || value === 0)
		return this;

	switch(datePart) {
	case Date.Milli:
		this.setTime(this.getTime() + value);
		break;
	case Date.Second:
		this.setTime(this.getTime() + value * 1000);
		break;
	case Date.Minute:
		this.setTime(this.getTime() + value * 60 * 1000);
		break;
	case Date.Hour:
		this.setTime(this.getTime() + value * 60 * 60 * 1000);
		break;
	case Date.Day:
		this.setDate(this.getDate() + value);
		break;
	case Date.Month:
		var day = this.getDate();
		var month = this.getMonth();
		if(day > 28)
			day = Math.min(day, this.getFirstDateOfMonth().add(value, Date.Month).getLastDateOfMonth().getDate());
		this.setDate(day);
		this.setMonth(month + value);
		break;
	case Date.Year:
		var day = this.getDate();
		var year = this.getFullYear();
		if(day > 28)
			day = Math.min(day, this.getFirstDateOfMonth().add(value, Date.Year).getLastDateOfMonth().getDate());
		this.setDate(day);
		this.setFullYear(year + value);
		break;
	}

	return this;
};

Date.prototype.addMillisecond = function(millis) {
	return this.add(millis, Date.Milli);
};

Date.prototype.addSecond = function(seconds) {
	return this.add(seconds, Date.Second);
};

Date.prototype.addMinute = function(minutes) {
	return this.add(minutes, Date.Minute);
};

Date.prototype.addHour = function(hours) {
	return this.add(hours, Date.Hour);
};

Date.prototype.addDay = function(days) {
	return this.add(days, Date.Day);
};

Date.prototype.addMonth = function(months) {
	return this.add(months, Date.Month);
};

Date.prototype.addYear = function(years) {
	return this.add(years, Date.Year);
};

Date.prototype.truncDay = function() {
	this.setHours(0);
	this.setMinutes(0);
	this.setSeconds(0);
	this.setMilliseconds(0);
	return this;
};

Date.prototype.truncMonth = function() {
	this.setDate(1);
	this.setHours(0);
	this.setMinutes(0);
	this.setSeconds(0);
	this.setMilliseconds(0);
	return this;
};

Date.prototype.toISOString = function() {
	var zoneOffset = -this.getTimezoneOffset();

	var year = this.getFullYear();
	var month = this.getMonth() + 1;
	var day = this.getDate();
	var hours = this.getHours();
	var minutes = this.getMinutes();
	var seconds = this.getSeconds();
	var milliseconds = this.getMilliseconds();

	var offset = '';
	if(zoneOffset != 0) {
		var zoneHours = Math.floor(zoneOffset / 60);
		var zoneMinutes = zoneOffset % 60;
		offset = (zoneOffset >= 0 ? '+' : '-') +
			(zoneHours < 10 ? '0' : '') + zoneHours + ':' +
			(zoneMinutes < 10 ? '0' : '') + zoneMinutes;
	}

	return year + '-' +
		(month < 10 ? '0' : '') + month + '-' +
		(day < 10 ? '0' : '') + day + 'T' +
		(hours < 10 ? '0' : '') + hours + ':' +
		(minutes < 10 ? '0' : '') + minutes + ':' + 
		(seconds < 10 ? '0' : '') + seconds +
		(milliseconds != 0 ? '.' + (milliseconds < 100 ? (milliseconds < 10 ? '00' : '0') : '') + milliseconds : '') + offset;
};