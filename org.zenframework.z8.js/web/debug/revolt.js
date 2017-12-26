Number.isNumber = function(value) {
	return typeof value == 'number';
};

Number.compare = function(value1, value2) {
	return value1 - value2;
};

Number.prototype.round = function(digits) {
	if(digits == null)
		return Math.round(this);
	var power = Math.pow(10, digits);
	return Math.round(this * power) / power;
};
String.ZeroWidthChar = String.fromCharCode(8203);
String.SpacerChar = '&#160;';

String.isString = function(value) {
	return typeof value == 'string';
};

String.htmlText = function(text) {
	return text == null || text.length == 0 ? String.ZeroWidthChar : text;
};


String.padLeft = function(string, size, ch) {
	var result = String(string);
	ch = ch || ' ';
	while(result.length < size)
		result = ch + result;
	return result;
};

String.padRight = function(string, size, ch) {
	var result = String(string);
	ch = ch || ' ';
	while(result.length < size)
		result += ch;
	return result;
};

String.repeat = function(pattern, count, separator) {
	var result = '';
	for(var i = 0; i < count; i++)
		result += pattern;
	return result;
},

String.prototype.startsWith = function(string) {
	var length = this.length;
	if(string == null || length < string.length)
		return false;

	return this.substr(0, string.length) == string;
};

String.prototype.endsWith = function(string) {
	var length = this.length;
	if(string == null || length < string.length)
		return false;

	return this.substr(length - string.length, string.length) == string;
};
Array.prototype.pushIf = function() {
	for(var i = 0, length = arguments.length; i < length; i++) {
		var element = arguments[i];
		if(this.indexOf(element) == -1)
			this.push(element);
	}
	return this;
};

Array.prototype.insert = function(element, index) {
	element = Array.isArray(element) ? element : [element];
	var args = [index != null ? index : this.length, 0].concat(element);
	this.splice.apply(this, args);
	return this;
};

Array.prototype.add = function(element) {
	return this.insert(element, null);
};

Array.prototype.remove = function() {
	var removed = [];
	for(var i = 0, length = arguments.length; i < length; i++) {
		var element = arguments[i];
		var index = this.indexOf(element);
		if(this.indexOf(element) != -1) {
			removed.push(element);
			this.splice(index, 1);
		}
	}
	return removed;
};

Array.prototype.removeAll = function(array) {
	if(array == null)
		return [];

	var array = Array.isArray(array) ? array : [array];
	return this.remove.apply(this, array);
};

Array.prototype.removeAt = function(index) {
	return this.splice(index, 1);
};

Array.prototype.first = function() {
	var length = this.length;
	return length > 0 ? this[0] : null;
};

Array.prototype.last = function() {
	var length = this.length;
	return length > 0 ? this[length - 1] : null;
};
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
};JSON.encode = JSON.stringify;
JSON.decode = JSON.parse;
var Z8 = {
	classes: {},

	apply: function(object, config) {
		for(var name in config)
			object[name] = config[name];
		return object;
	},

	addMembers: function(prototype, config) {
		for(var name in config) {
			var member = config[name];
			Z8.addMember(prototype, member, name)
		}
	},

	addMember: function(prototype, member, name) {
		if(member == undefined)
			return;

		if(typeof member == 'function' && member.$owner == null) {
			var previous = prototype[name];
			if(previous != null)
				member.$previous = previous;

			member.$owner = prototype;
		}

		prototype[name] = member;
	},

	addCallParent: function(prototype) {
		var callParent = function() {
			return this.callParent.caller.$previous.apply(this, arguments);
		}

		Z8.addMember(prototype, callParent, 'callParent');
	},

	newConstructor: function() {
		return function constructor() {
			return this.constructor.apply(this, arguments);
		}
	},

	define: function(className, config) {
		var cls = Z8.newConstructor();

		var single = config.single;
		delete config.single;

		var extend = config.extend;
		delete config.extend;

		var mixins = config.mixins;
		delete config.mixins;

		var statics = config.statics;
		delete config.statics;

		var shortName = config.shortClassName;
		delete config.shortClassName;

		var baseCls = null;

		if(extend != null) {
			baseCls = Z8.classes[extend];
			if(baseCls == null)
				throw 'Superclass not found: "' + extend + '"'; 
		} else
			baseCls = function(param) { return Z8.addMembers(this, param); };

		var basePrototype = baseCls.prototype;
		var prototype = cls.prototype = Object.create(basePrototype);
		prototype.self = prototype;
		prototype.$className = className;
		cls.superclass = prototype.superclass = basePrototype;

		Z8.addMembers(prototype, config);
		Z8.addCallParent(prototype);
		Z8.mixin(prototype, mixins);
		Z8.apply(cls, statics);

		if(!single) {
			Z8.namespace(className, cls);
			Z8.namespace(shortName, cls);
		}

		return cls;
	},

	mixin: function(prototype, mixins) {
		if(mixins == null)
			return;

		mixins = Array.isArray(mixins) ? mixins : [mixins];

		for(var i = 0, length = mixins.length; i < length; i++) {
			var mixin = Z8.classes[mixins[i]];

			if(mixin == null)
				throw 'Mixin not found: "' + mixins[i] + '"';

			mixin = mixin.prototype;
			var mixinId = mixin.mixinId;

			if(mixinId == null)
				throw 'Mixed in class must have mixinId: "' + mixin.$className + '"';

			for(var name in mixin) {
				if(name != 'mixinId' && prototype[name] === undefined)
					prototype[name] = mixin[name];
			}

			if(prototype.mixins == null)
				prototype.mixins = {};

			prototype.mixins[mixinId] = mixin;
		}
	},

	namespace: function(name, cls) {
		if(name == null)
			return;

		var root = window;
		var parts = name.split('.');
		var length = parts.length - 1;
		var last = parts[length];

		for(var i = 0; i < length; i++) {
			var part = parts[i];
			var newRoot = root[part];
			if(newRoot == null)
				newRoot = root[part] = {};
			root = newRoot;
		}

		root[last] = cls;
		Z8.classes[name] = cls;

		return cls;
	},

	create: function(cls, config) {
		if(String.isString(cls)) {
			var name = cls;
			cls = Z8.classes[name];
			if(cls == null)
				throw 'Class not found: "' + name + '"';
		}

		var object = Object.create(cls.prototype);
		cls.call(object, config);
		return object;
	},

	callback: function(callback/*, scope, args */) {
		if(callback == null)
			return;

		var args = [];
		var scope = null;

		if(typeof callback == 'object') {
			scope = callback.scope;
			callback = callback.fn || callback.callback;
			args = Array.prototype.slice.call(arguments, 1);
		} else {
			scope = arguments[1];
			args = Array.prototype.slice.call(arguments, 2);
		}

		if(callback == null)
			return;

		callback.apply(scope, args);
	},

	isEmpty: function(value) {
		if(value == null || value === '')
			return true;
		if(Array.isArray(value))
			return value.length == 0;
		return false;
	}
};Z8.apply(Event.prototype, {
	stopEvent: function() {
		this.stopPropagation();
		this.preventDefault();
		this.cancelled = true;
	},

	getCharCode: function() {
		return this.charCode || this.keyCode;
	},

	getKey: function() {
		return this.keyCode || this.charCode;
	}
});

Z8.apply(Event, {
	BACKSPACE: 8, TAB: 9, NUM_CENTER: 12, ENTER: 13, RETURN: 13, SHIFT: 16, CTRL: 17, ALT: 18, PAUSE: 19, CAPS_LOCK: 20, ESC: 27, SPACE: 32,
	PAGE_UP: 33, PAGE_DOWN: 34, END: 35, HOME: 36, LEFT: 37, UP: 38, RIGHT: 39, DOWN: 40,
	PRINT_SCREEN: 44, INSERT: 45, DELETE: 46, 
	ZERO: 48, ONE: 49, TWO: 50, THREE: 51, FOUR: 52, FIVE: 53, SIX: 54, SEVEN: 55, EIGHT: 56, NINE: 57,
	A: 65, B: 66, C: 67, D: 68, E: 69, F: 70, G: 71, H: 72, I: 73, J: 74, K: 75, L: 76, M: 77, N: 78, O: 79, P: 80, Q: 81, R: 82, S: 83, T: 84, U: 85, V: 86, W: 87, X: 88, Y: 89, Z: 90,
	CONTEXT_MENU: 93,
	NUM_ZERO: 96, NUM_ONE: 97, NUM_TWO: 98, NUM_THREE: 99, NUM_FOUR: 100, NUM_FIVE: 101, NUM_SIX: 102, NUM_SEVEN: 103, NUM_EIGHT: 104, NUM_NINE: 105, NUM_MULTIPLY: 106, NUM_PLUS: 107, NUM_MINUS: 109, NUM_PERIOD: 110, NUM_DIVISION: 111,
	F1: 112, F2: 113, F3: 114, F4: 115, F5: 116, F6: 117, F7: 118, F8: 119, F9: 120, F10: 121, F11: 122, F12: 123
});Z8.define('Z8.dom.Dom', {
	shortClassName: 'DOM',

	statics: {
		emptyTags: {
			br: true,
			frame: true,
			hr: true,
			img: true,
			input: true,
			link: true,
			meta: true,
			col: true
		},

		readyListeners: [],

		isDom: function(dom) {
			return dom != null && dom.addEventListener != null;
		},

		get: function(dom) {
			if(dom == null)
				return null;

			if(dom.isComponent || dom.dom != null)
				return dom.dom;

			return DOM.isDom(dom) ? dom : null;
		},

		isParentOf: function(parents, child) {
			if(parents == null || child == null)
				return false;

			parents = Array.isArray(parents) ? parents.slice(0) : [parents];

			for(var i = 0, count = 0, length = parents.length; i < length; i++) {
				var dom = parents[i] = DOM.get(parents[i]);
				if(dom != null)
					count++;
			}

			if(count == 0)
				return false;

			var topmost = document.body;

			while(child != null && child.nodeType == 1 && child !== topmost) {
				for(var i = 0, length = parents.length; i < length; i++) {
					if(child == parents[i])
						return true;
				}

				child = child.parentNode;
			}

			return false;
		},

		getCookie: function(name) {
			var matches = document.cookie.match(name + '=([^;|*]*);*');
			return matches != null ? decodeURIComponent(matches[1] || '') : '';
		},

		setCookie: function(name, value, days) {
			document.cookie = name + '=' + encodeURIComponent(value) + ';expires=' + new Date().addDay(days || 2).toUTCString() + ';path=/';
		},

		selectNode: function(dom, selector) {
			if(arguments.length == 1) {
				selector = dom;
				dom = document.body;
			}
			if((dom = DOM.get(dom)) == null)
				return null;
			return dom.querySelector(selector);
		},

		query: function(dom, selector) {
			if(arguments.length == 1) {
				selector = dom;
				dom = document.body;
			}
			if((dom = DOM.get(dom)) == null)
				return null;
			return dom.querySelectorAll(selector);
		},

		markup: function(markup) {
			var buffer = '';

			if(String.isString(markup) || Number.isNumber(markup)) {
				buffer += markup;
				return buffer;
			}

			if(Array.isArray(markup)) {
				for(var i = 0, length = markup.length; i < length; i++) {
					var item = markup[i];
					if(item != null)
						buffer += DOM.markup(item);
				}
				return buffer;
			}

			var tag = markup.tag || 'div';
			buffer += '<' + tag;

			var value;

			for(var attribute in markup) {
				value = markup[attribute];
				if(attribute != 'tag' && attribute != 'children' && attribute != 'cn' && attribute != 'html' && attribute != 'container')
					buffer += ' ' + (attribute == 'cls' ? 'class' : attribute) + (value != null ? '="' + value + '"' :'');
			}

			if(DOM.emptyTags[tag] == null) {
				buffer += '>';

				if((value = markup.html) != null)
					buffer += value;

				if((value = markup.cn || markup.children) != null)
					buffer += DOM.markup(value);

				buffer += '</' + tag + '>';
			} else
				buffer += '/>';

			return buffer;
		},

		append: function(container, child) {
			if((container = DOM.get(container || document.body)) == null || child == null)
				return null;

			if(DOM.isDom(child))
				return container.appendChild(child);

			container.insertAdjacentHTML('beforeend', DOM.markup(child));
			return container.lastChild;
		},

		insertBefore: function(before, dom) {
			if((before = DOM.get(before)) == null || dom == null)
				return null;

			if(DOM.isDom(dom))
				return before.insertAdjacentElement('beforebegin', dom);

			before.insertAdjacentHTML('beforebegin', DOM.markup(dom));
			return before.previousSibling;
		},

		remove: function(dom, delay) {
			if((dom = DOM.get(dom)) == null)
				return null;

			if(delay != null) {
				var remove = function(dom) {
					dom.parentElement.removeChild(dom);
				}
				new Z8.util.DelayedTask().delay(delay, remove, this, dom);
			} else
				dom.parentElement.removeChild(dom);
		},

		getProperty: function(dom, property) {
			dom = DOM.get(dom);
			return dom != null ? dom[property] : null;
		},

		setProperty: function(dom, property, value, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			if(delay != null) {
				var setValue = function(dom, property, value) {
					dom[property] = value;
				};
				new Z8.util.DelayedTask().delay(delay, setValue, this, dom, property, value);
			} else
				dom[property] = value;
		},

		getStyle: function(dom, property) {
			dom = DOM.get(dom);
			return dom != null ? dom.style[property] : null;
		},

		setStyle: function(dom, property, value, delay) {
			if(value == null || (dom = DOM.get(dom)) == null)
				return;

			if(delay != null) {
				var setValue = function(dom, property, value) {
					dom.style[property] = value;
				};
				new Z8.util.DelayedTask().delay(delay, setValue, this, dom, property, value);
			} else
				dom.style[property] = value;
		},

		getParent: function(dom) {
			return DOM.getProperty(dom, 'parentNode');
		},

		getComputedStyle: function(dom) {
			dom = DOM.get(dom);
			return dom != null ? window.getComputedStyle(dom) : null;
		},

		getClipping: function(dom) {
			if((dom = DOM.get(dom)) == null)
				return null;

			var topmost = document.body;
			dom = dom.parentNode;

			while(dom != null && dom.nodeType == 1) {
				if(dom == topmost)
					return topmost;

				var style = window.getComputedStyle(dom);
				var overflowY = style.overflowY;

				if(overflowY == 'hidden' || overflowY == 'auto')
					return dom;

				dom = dom.parentNode;
			}

			return null;
		},

		getAttribute: function(dom, attribute) {
			dom = DOM.get(dom);
			return dom != null ? dom.getAttribute(attribute) : null;
		},

		getIntAttribute: function(dom, attribute) {
			var value = DOM.getAttribute(dom, attribute);
			return value != null ? Parser.integer(value) : null;
		},

		setAttribute: function(dom, attribute, value) {
			if((dom = DOM.get(dom)) != null )
				dom.setAttribute(attribute, value);
		},

		focus: function(dom, select) {
			if((dom = DOM.get(dom)) == null || dom.focus == null)
				return false;

			dom.focus();
	
			if(select && dom.select != null)
				dom.select();

			return true;
		},

		scroll: function(dom, left, top) {
			if((dom = DOM.get(dom)) == null || dom.focus == null)
				return;

			dom.scrollLeft = left;
			dom.scrollTop = top;
		},

		parseCls: function(cls) {
			return (String.isString(cls) ? cls.toLowerCase().split(' ') : cls) || [];
		},

		getCls: function(dom) {
			DOM.getProperty(dom, 'className');
		},

		setCls: function(dom, cls, delay) {
			DOM.setProperty(dom, 'className', Array.isArray(cls) ? cls.join(' ') : cls, delay);
		},

		removeCls: function(dom, cls, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var classes = DOM.parseCls(dom.className);
			var index = classes.indexOf(cls);
			if(index == -1)
				return;

			classes.splice(index, 1);
			DOM.setCls(dom, classes);
		},

		addCls: function(dom, cls, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var classes = DOM.parseCls(dom.className);
			if(classes.indexOf(cls) != -1)
				return;

			classes.push(cls);
			DOM.setCls(dom, classes, delay);
		},

		swapCls: function(dom, condition, trueCls, falseCls, delay) {
			if((dom = DOM.get(dom)) == null)
				return;

			var index = -1;
			var classes = DOM.parseCls(dom.className);

			var clsToAdd = condition ? trueCls : falseCls;
			var clsToRemove = condition ? falseCls : trueCls;

			if(clsToAdd != null) {
				if(classes.indexOf(clsToAdd) == -1)
					classes.push(clsToAdd);
				else
					clsToAdd = null;
			}

			if(clsToRemove != null) {
				index = classes.indexOf(clsToRemove);
				if(index != -1 )
					classes.splice(index, 1);
				else
					clsToRemove = null;
			}

			if(clsToAdd != null || clsToRemove != null)
				DOM.setCls(dom, classes, delay);

			return classes;
		},

		getValue: function(dom) {
			if((dom = DOM.get(dom)) == null)
				return null;
			var tag = dom.tagName;
			return tag == 'INPUT' || tag == 'TEXTAREA' ? (dom.type == 'checkbox' ? dom.checked : dom.value) : dom.textContent;
		},

		setValue: function(dom, value, delay) {
			if((dom = DOM.get(dom)) != null) {
				var tag = dom.tagName;
				var property = tag == 'INPUT' || tag == 'TEXTAREA' ? (dom.type == 'checkbox' ? 'checked' : 'value') : 'textContent';
				DOM.setProperty(dom, property, value || '', delay);
			}
		},

		setTitle: function(dom, title, delay) {
			DOM.setProperty(dom, 'title', title, delay);
		},

		setInnerHTML: function(dom, innerHTML, delay) {
			DOM.setProperty(dom, 'innerHTML', innerHTML, delay);
		},

		setReadOnly: function(dom, readOnly, delay) {
			DOM.setProperty(dom, 'readOnly', readOnly, delay);
		},

		setDisabled: function(dom, disabled, delay) {
			DOM.setProperty(dom, 'disabled', disabled, delay);
		},

		setTabIndex: function(dom, tabIndex, delay) {
			DOM.setProperty(dom, 'tabIndex', tabIndex, delay);
		},

		setDisplay: function(dom, display, delay) {
			DOM.setStyle(dom, 'display', display, delay);
		},

		setHidden: function(dom, hidden, delay) {
			DOM.setStyle(dom, 'visibility', hidden ? 'hidden' : 'visible', delay);
		},

		getOffsetTop: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetTop'));
		},

		getOffsetLeft: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetLeft'));
		},

		getOffsetWidth: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetWidth'));
		},

		getOffsetHeight: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'offsetHeight'));
		},

		getClientWidth: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'clientWidth'));
		},

		getClientHeight: function(dom) {
			return Ems.pixelsToEms(DOM.getProperty(dom, 'clientHeight'));
		},

		getPoint: function(dom, point) {
			var point = DOM.getStyle(dom, point);
			return point != null ? parseFloat(point) : null;
		},

		setPoint: function(dom, point, value, delay) {
			if(value != null)
				DOM.setStyle(dom, point, String.isString(value) ? value : (value + 'em'), delay);
		},

		getLeft: function(dom) {
			return DOM.getPoint(dom, 'left');
		},

		setLeft: function(dom, left, delay) {
			DOM.setPoint(dom, 'left', left, delay);
		},

		getRight: function(dom) {
			return DOM.getPoint(dom, 'right');
		},

		setRight: function(dom, right, delay) {
			DOM.setPoint(dom, 'right', right, delay);
		},

		getTop: function(dom) {
			return DOM.getPoint(dom, 'top');
		},

		setTop: function(dom, top, delay) {
			DOM.setPoint(dom, 'top', top, delay);
		},

		getBottom: function(dom) {
			return DOM.getPoint(dom, 'bottom');
		},

		setBottom: function(dom, bottom, delay) {
			DOM.setPoint(dom, 'bottom', bottom, delay);
		},

		getWidth: function(dom) {
			return DOM.getPoint(dom, 'width');
		},

		setWidth: function(dom, width, delay) {
			DOM.setPoint(dom, 'width', width, delay);
		},

		getHeight: function(dom) {
			return DOM.getPoint(dom, 'height');
		},

		setHeight: function(dom, height, delay) {
			DOM.setPoint(dom, 'height', height, delay);
		},

		setCssText: function(dom, cssText, delay) {
			DOM.setStyle(dom, 'cssText', cssText, delay);
		},

		rotate: function(dom, degree) {
			if(dom == null)
				return;

			var cls = 'fa-rotate-' + degree;

			if(degree != 0) {
				DOM.addCls(dom, 'fa-transform-transition');
				DOM.addCls(dom, cls);
				dom.rotationCls = cls;
			} else {
				DOM.removeCls(dom, dom.rotationCls);
				delete dom.rotationCls;
			}
		},

		substitutes: {
			'click': 'touchend',
			'mousedown': 'touchstart',
		},

		on: function(dom, event, fn, scope, capture) {
			if((dom = DOM.get(dom)) == null)
				return;

			event = event.toLowerCase();

			var eventsData = dom.eventsData;

			if(eventsData == null)
				eventsData = dom.eventsData = eventsData = {};

			var listeners = eventsData[event] || [];

			var callback = function(event) {
				var target = event.currentTarget;
				var eventsData = target.eventsData;
				if(eventsData == null)
					return;
				var listeners = eventsData[event.type];
				if(listeners != null) {
					listeners = [].concat(listeners); // original listeners array can be modified inside the loop
					for(var i = 0, length = listeners.length; i < length; i++) {
						Z8.callback(listeners[i], event, event.target);
						if(event.cancelled)
							return;
					}
				}
			};

			if(fn == null)
				throw 'DOM.on: fn is null';

			var listener = { fn: fn, scope: scope, callback: callback, capture: capture };
			listeners.push(listener);
			eventsData[event] = listeners;

			if(listeners.length == 1)
				dom.addEventListener(event, callback, capture);
		},

		un: function(dom, event, fn, scope) {
			if((dom = DOM.get(dom)) == null)
				return;

			event = event.toLowerCase();

			var eventsData = dom.eventsData;
			if(eventsData == null)
				return;

			var listeners = eventsData[event];

			if(listeners == null)
				return;

			for(var i = 0, length = listeners.length; i < length; i++) {
				var listener = listeners[i];
				if(listener.fn == fn && listener.scope == scope) {
					listeners.removeAt(i);
					if(listeners.length == 0) {
						dom.removeEventListener(event, listener.callback, listener.capture);
						delete eventsData[event];
					}
					break;
				}
			}

			if(Object.keys(eventsData).length == 0)
				delete dom.eventsData;
		},

		download: function(url, serverId, callback) {
			var config = { tag: 'iframe', html: '<head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"></head>', src: '', hidden: true };
			var frame = DOM.append(document.body, config);

			frame.src = encodeURI(url.replace(/\\/g, '/')) + '?&session=' + Application.session + (serverId != null ? '&serverId=' + serverId : '');

			new Z8.util.DelayedTask().delay(500, DOM.downloadCallback, this, url, frame, callback);
		},

		downloadCallback: function(url, frame, callback) {
			var document = frame.contentDocument;
			var readyState = document.readyState;

			if(readyState == 'complete') {
				var response = document.body.innerHTML;
				var success = Z8.isEmpty(response);
				if(!success) {
					response = response.charAt(0) == '{' ? JSON.decode(response) : { info: { messages: [{ text: '\'' + url + '\' - файл не найден', type: 'error' }] }};
					Application.message(response.info.messages);
				}
				Z8.callback(callback, success);
				DOM.remove(frame, 10000);
			} else
				new Z8.util.DelayedTask().delay(500, DOM.downloadCallback, this, url, frame, callback);
		},

		onReady: function(callback, scope) {
			DOM.readyListeners.push({ fn: callback, scope: scope });
		},

		onContextMenu: function(event, target) {
			if(target.tagName != 'INPUT' && target.tagName != 'PRE')
				event.stopEvent();
		},

		onReadyEvent: function(callback, scope) {
			var listeners = DOM.readyListeners;
			for(var i = 0, length = listeners.length; i < length; i++)
				Z8.callback(listeners[i]);

			delete DOM.readyListeners;
			DOM.un(window, 'load', DOM.onReadyEvent);

			if(DOM.onContextMenu != null)
				DOM.on(window, 'contextmenu', DOM.onContextMenu);

			var agent = window.navigator.userAgent;
			if(agent.toLowerCase().indexOf('gecko/') != -1)
				DOM.addCls(document.body, 'gecko');
		}
	}
});

DOM.on(window, 'load', DOM.onReadyEvent);Z8.define('Z8.util.Ems', {
	shortClassName: 'Ems',

	statics: {
		Base: parseFloat(DOM.getComputedStyle(document.body).fontSize),

		UnitHeight: 4,
		UnitSpacing: .71428571,

		unitsToEms: function(units) {
			return units ? units * Ems.UnitHeight + (units - 1) * Ems.UnitSpacing : 0;
		},

		pixelsToEms: function(pixels) {
			return pixels ? (pixels / Ems.Base).round(8) : 0;
		},

		emsToPixels: function(ems) {
			return Math.ceil(ems * Ems.Base);
		}

	}
});Z8.define('Z8.Rect', {
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
});Z8.define('guid', {
	statics: {
		Null: '00000000-0000-0000-0000-000000000000'
	}
});

Z8.define('Z8.server.type', {
	shortClassName: 'Type',

	statics: {
		Guid: 'guid',
		Integer: 'int',
		Float: 'float',
		Boolean: 'boolean',
		Date: 'date',
		Datetime: 'datetime',
		Datespan: 'datespan',
		String: 'string',
		Binary: 'binary',
		Text: 'text',
		File: 'file',
		Files: 'attachments',
		Json: 'json'
	}
});

Z8.define('Z8.RecordLock', {
	shortClassName: 'RecordLock',

	statics: {
		None: 0,
		Full: 1,
		Edit: 2,
		Destroy: 3
	}
});

Z8.define('Z8.Operation', {
	shortClassName: 'Operation',

	statics: {
		Eq: 'eq',
		NotEq: 'notEq',
		Contains: 'contains',
		NotContains: 'notContains',
		BeginsWith: 'beginsWith',
		NotBeginsWith: 'notBeginsWith',
		EndsWith: 'endsWith',
		NotEndsWith: 'notEndsWith',
		IsEmpty: 'isEmpty',
		IsNotEmpty: 'isNotEmpty',

		GT: 'gt',
		GE: 'ge',
		LT: 'lt',
		LE: 'le',

		Today: 'today',
		Tomorrow: 'tomorrow',
		Yesterday: 'yesterday',

		LastWeek: 'lastWeek',
		ThisWeek: 'thisWeek',
		NextWeek: 'nextWeek',

		LastMonth: 'lastMonth',
		ThisMonth: 'thisMonth',
		NextMonth: 'nextMonth',

		LastYear: 'lastYear',
		ThisYear: 'thisYear',
		NextYear: 'nextYear',

		LastDays: 'lastDays',
		NextDays: 'nextDays',

		LastHours: 'lastHours',
		NextHours: 'nextHours',

		isTrue: 'isTrue',
		isFalse: 'isFalse'
	}
});
Z8.define('Z8.util.Format', {
	shortClassName: 'Format',

	statics: {

		Date: 'd.m.Y',
		ShortDate: 'd.m.y',
		LongDate: 'j F Y',
		Time: 'H:i',
		LongTime: 'H:i:s',
		Datetime: 'd.m.Y H:i',
		LongDatetime: 'j F Y H:i',
		Integer: '0,000',
		Float: '0,000.00',
		TrueText: 'да',
		FalseText: 'нет',

		ThousandSeparator: ' ',
		DecimalSeparator: ',',

		charWidth: null,
		measures: {},

		list: function() {
			var result = '';

			for(var i = 0, length = arguments.length; i < length; i++) {
				var arg = arguments[i];

				if(!Z8.isEmpty(arg))
					result += (result == '' ? '' : ', ') + arg;
			}
			return result;
		},

		placeholder: function(value, placeholder) {
			return Z8.isEmpty(value) ? placeholder : value;
		},

		nl2br: function(value) {
			var result = null;

			if(Array.isArray(value)) {
				var result = [];
				for(var i = 0; i < value.length; i++) {
					var text = value[i];
					result.push((text.text || text).replace(/\n/g, '<br>'));
				}
				result = result.join('<br>');
			} else if(value != null)
				result = (value.text || value).replace(/\n/g, '<br>');

			return result;
		},

		toHtmlString: function(value) {
			return Format.nl2br(value.text || value).replace(/ /g, string.Empty);
		},

		htmlEncode: function(value) {
			if(value == null || !String.isString(value))
				return null;

			return value.
				replace(/&/g, '&amp;').
				replace(/"/g, '&quot;').
				replace(/'/g, '&#39;').
				replace(/</g, '&lt;').
				replace(/>/g, '&gt;');
		},

		htmlDecode: function(value) {
			if(value == null)
				return null;

			return value.
				replace(/&quot;/g, '"').
				replace(/&#39;/g, '\'').
				replace(/&lt;/g, '<').
				replace(/&gt;/g, '>').
				replace(/&amp;/g, '&');
		},

		integer: function(value, format) {
			return Format.number(value, format != null ? format : Format.Integer);
		},

		float: function(value, format) {
			return Format.number(value, format != null ? format : Format.Float);
		},

		percent: function(value, format) {
			var value = Format.number(value, format != null ? format : Format.Integer);
			return value != null ? value + '%' : value;
		},

		date: function(value, format) {
			return Format.formatDate(value, format != null ? format : Format.Date);
		},

		datetime: function(value, format) {
			return Format.formatDate(value, format != null ? format : Format.Datetime);
		},

		dateOrTime: function(value, dateFormat, timeFormat) {
			if(value == null || value == '')
				return '';

			dateFormat = dateFormat || Format.Datetime;
			timeFormat = timeFormat || Format.Time;

			var today = new Date();
			var yesterday = new Date().add(-1, Date.Day);

			var isToday = Date.isEqualDate(value, today);
			var isYesterday = Date.isEqualDate(value, yesterday);

			if(isToday)
				return 'сегодня ' + Format.date(value, timeFormat);

			if(isYesterday)
				return 'вчера ' + Format.date(value, timeFormat);

			return Format.date(value, dateFormat);
		},

		fileSize: function(size) {
			var KB = 1024, MB = 1048576, GB = 1073741824;

			if(size < KB) {
				var value = Format.integer(size, '0,0');
				return value != '' ? value + ' B' : '';
			} else if(size < MB) {
				var value = Format.float(size / KB, '0,0.##');
				return value != '' ? value + ' KB' : '';
			} else if(size < GB) {
				var value = Format.float(size / MB, '0,0.##');
				return value != '' ? value + ' MB' : '';
			} else {
				var value = Format.float(size / GB, '0,0.##');
				return value != '' ? value + ' GB' : '';
			}
		},

		getCharWidth: function() {
			if(Format.charWidth == null) {
				var sample = "abcdefghABCDEFGH1234567890";
				var config = { html: sample, style: 'position: fixed; top: -10000;' };
				var div = DOM.append(document.body, config);
				Format.charWidth = new Rect(div).width / sample.length;
				DOM.remove(div);
			}
			return Format.charWidth;
		},

		measureDate: function(format) {
			var length = Format.measures[format];
			if(length != null)
				return length;
			length = Format.date(new Date(2000, 8, 20, 22, 22, 22, 222), format).length * Format.getCharWidth();
			Format.measures[format] = length;
			return length;
		},

		number: function(value, format) {
			if(format == null)
				return value;

			if(isNaN(value))
				return '';

			var formatFn = Format.formats[format];

			if(formatFn == null) {
				var originalFormat = format;

				var precision = 0;
				var trimPart = '';
				var hasComma = format.indexOf(',') !== -1;
				var splitFormat = format.replace(/[^\d\.#]/g, '').split('.');
				var extraChars = format.replace(/[\d,\.#]+/, '');

				if (splitFormat.length > 2)
					throw 'Invalid number format, should have no more than 1 decimal';

				if (splitFormat.length == 2) {
					var precision = splitFormat[1].length;
					var trimTrailingZeroes = splitFormat[1].match(/#+$/);
					if (trimTrailingZeroes) {
						var length = trimTrailingZeroes[0].length;
						trimPart = 'trailingZeroes=new RegExp(Format.DecimalSeparator.replace(/([-.*+?\\^${}()|\\[\\]\\/\\\\])/g, "\\\\$1") + "*0{0,' + length + '}$")';
					}
				}

				var code = [
					'var neg,absVal,fnum,parts' + (hasComma ? ',thousandSeparator,thousands=[],j,n,i' : '') + (extraChars ? ',format="' + format + '"' : '') + ',trailingZeroes;' + 'return function(v){' + 'if(typeof v!=="number"&&isNaN(v=parseFloat(v)))return"";' + 'neg=v<0;',
					'absVal=Math.abs(v);',
					'fnum=absVal.toFixed(' + precision + ');',
					trimPart,
					';'
				];
				if(hasComma) {
					if(precision) {
						code[code.length] = 'parts=fnum.split(".");';
						code[code.length] = 'fnum=parts[0];';
					}
					code[code.length] = 'if(absVal>=1000) {';
					code[code.length] = 'thousandSeparator=Format.ThousandSeparator;' + 'thousands.length=0;' + 'j=fnum.length;' + 'n=fnum.length%3||3;' + 'for(i=0;i<j;i+=n){' + 'if(i!==0){' + 'n=3;' + '}' + 'thousands[thousands.length]=fnum.substr(i,n);' + '}' + 'fnum=thousands.join(thousandSeparator);' + '}';
					if (precision)
						code[code.length] = 'fnum += Format.DecimalSeparator+parts[1];';
				} else if(precision)
					code[code.length] = 'if(Format.DecimalSeparator!=="."){' + 'parts=fnum.split(".");' + 'fnum=parts[0]+Format.DecimalSeparator+parts[1];' + '}';

				code[code.length] = 'if(neg&&fnum!=="' + (precision ? '0.' + String.repeat('0', precision) : '0') + '") { fnum="-"+fnum; }';

				if(trimTrailingZeroes)
					code[code.length] = 'fnum=fnum.replace(trailingZeroes,"");';

				code[code.length] = 'return ';

				if(extraChars)
					code[code.length] = 'format.replace(/[\\d,\\.#]+/, fnum);';
				else
					code[code.length] = 'fnum;';

				code[code.length] = '};';

				formatFn = Format.formats[originalFormat] = Function.prototype.constructor.call(Function.prototype, code.join(''))();
			}
			return formatFn(value);
		},

		formatDate: function(date, format) {
			var formats = Format.formats;
			if(!Date.isDate(date))
				return '';
			if(formats[format] == null)
				Format.createFormat(format);

			return formats[format].call(date);
		},

		formats: {},
		formatCodes: {
			Y: 'String.padLeft(this.getFullYear(), 4, "0")',
			y: '("" + this.getFullYear()).substring(2, 4)',

			F: 'this.getMonthName()',
			M: 'this.getShortMonthName()',
			m: 'String.padLeft(this.getMonth() + 1, 2, "0")',
			n: '(this.getMonth() + 1)',
			t: 'this.getDaysInMonth()',

			W: 'String.padLeft(this.getWeekOfYear(), 2, "0")',
			w: 'this.getWeekOfYear()',

			z: 'this.getDayOfYear()',
			N: 'this.getDayOfWeek() + 1',
			l: 'this.getDayName()',
			D: 'this.getShortDayName()',
			d: 'String.padLeft(this.getDate(), 2, "0")',
			j: 'this.getDate()',

			g: '((this.getHours() % 12) ? this.getHours() % 12 : 12)',
			G: 'this.getHours()',
			h: 'String.padLeft((this.getHours() % 12) ? this.getHours() % 12 : 12, 2, "0")',
			H: 'String.padLeft(this.getHours(), 2, "0")',

			L: '(this.isLeapYear() ? 1 : 0)',
			o: '(this.getFullYear() + (this.getWeekOfYear() == 1 && this.getMonth() > 0 ? 1 : (this.getWeekOfYear() >= 52 && this.getMonth() < 11 ? -1 : 0)))',
			a: '(this.getHours() < 12 ? "am" : "pm")',
			A: '(this.getHours() < 12 ? "AM" : "PM")',
			i: 'String.padLeft(this.getMinutes(), 2, "0")',
			s: 'String.padLeft(this.getSeconds(), 2, "0")',
			S: 'String.padLeft(this.getMilliseconds(), 3, "0")',
			O: '((this.getTimezoneOffset() > 0 ? "-" : "+") + String.padLeft(Math.floor(Math.abs(this.getTimezoneOffset()) / 60), 2, "0") + String.PadLeft(Math.abs(this.getTimezoneOffset() % 60), 2, "0"))',
			P: '((this.getTimezoneOffset() > 0 ? "-" : "+") + String.padLeft(Math.floor(Math.abs(this.getTimezoneOffset()) / 60), 2, "0") + ":" + String.PadLeft(Math.abs(this.getTimezoneOffset() % 60), 2, "0"))',
			T: 'this.toString().replace(/^.* (?:\((.*)\)|([A-Z]{1,5})(?:[\-+][0-9]{4})?(?: -?\d+)?)$/, "$1$2").replace(/[^A-Z]/g, "")',
			Z: '(this.getTimezoneOffset() * -60)',
			c: function() {
				var format = 'Y-m-dTH:i:sP';
				var code = [];
				for(var i = 0, length = format.length; i < length; i++) {
					var ch = format.charAt(i);
					code.push(ch === 'T' ? '\'T\'' : Format.getFormatCode(ch));
				}
				return code.join(' + ');
			},
			C: function() {
				return 'this.toISOString()';
			},
			U: 'Math.round(this.getTime() / 1000)'
		},

		createFormat: function(format) {
			var code = [];
			var special = false;
			var ch = '';

			for(var i = 0, length = format.length; i < length; i++) {
				var ch = format.charAt(i);
				if(!special && ch === '\\')
					special = true;
				else if(special) {
					special = false;
					code.push('\'' + Format.escape(ch) + '\'');
				} else
					code.push(ch == '\n' ? '\'\\n\'' : Format.getFormatCode(ch));
			}

			code = 'return ' + code.join('+');
			Format.formats[format] = Function.prototype.constructor.call(Function.prototype, code);
		},

		escape: function(string) {
			return string.replace(/('|\\)/g, '\\$1');
		},

		getFormatCode: function(ch) {
			var formatCode = Format.formatCodes[ch];
			if(formatCode != null) {
				formatCode = typeof formatCode == 'function' ? formatCode() : formatCode;
				Format.formatCodes[ch] = formatCode;
			}
			return formatCode || ('\'' + Format.escape(ch) + '\'');
		}
	}
});Z8.define('Z8.util.Parser', {
	shortClassName: 'Parser',

	statics: {
		datetime: function(value, format) {
			if(Z8.isEmpty(value))
				return null;

			if(String.isString(value))
				value = value.replace(/^(\d\d?)([\.\-//])(\d\d?)([\.\-//])/, '$3$2$1$4');

			value = new Date(value);
			return isNaN(value) ? null : value;
		},

		date: function(value, format) {
			return Z8.util.Parser.datetime(value, format);
		},

		integer: function(value, radix) {
			if(Number.isNumber(value))
				return value.round();

			var value = parseInt(value.replace(/\s/g, ''), radix);
			return isNaN(value) ? null : value;
		},

		float: function(value) {
			if(Number.isNumber(value))
				return value;

			var value = parseFloat(value.replace(/,/g, '.').replace(/\s/g, ''));
			return isNaN(value) ? null : value;
		},

		boolean: function(value) {
			return String.isString(value) ? value === 'true' : value;
		}
	}
});Z8.define('Z8.util.MD5', {
	shortClassName: 'MD5',

	statics: {
		/* 
		 * hex output format. 0 - lowercase; 1 - uppercase
		 */
		hexcase: 0,
		/* 
		 * base-64 pad character. '=' for strict RFC compliance
		 */
		b64pad: '',

		hex: function(s) {
			return MD5.rstr2hex(MD5.rstr(MD5.str2rstr_utf8(s)));
		},

		b64: function(s) {
			return MD5.rstr2b64(MD5.rstr(MD5.str2rstr_utf8(s)));
		},

		any: function(s, e) {
			return MD5.rstr2any(MD5.rstr(MD5.str2rstr_utf8(s)), e);
		},

		hex_hmac: function(k, d) {
			return MD5.rstr2hex(MD5.rstr_hmac(MD5.str2rstr_utf8(k), MD5.str2rstr_utf8(d)));
		},

		b64_hmac: function (k, d) {
			return MD5.rstr2b64(MD5.rstr_hmac(MD5.str2rstr_utf8(k), MD5.str2rstr_utf8(d)));
		},

		any_hmac: function(k, d, e) {
			return MD5.rstr2any(MD5.rstr_hmac(MD5.str2rstr_utf8(k), MD5.str2rstr_utf8(d)), e);
		},

		vm_test: function() {
			return MD5.hex('abc').toLowerCase() == '900150983cd24fb0d6963f7d28e17f72';
		},

		/*
		 * Calculate the MD5 of a raw string
		 */
		rstr: function(s) {
			return MD5.binl2rstr(MD5.binl(MD5.rstr2binl(s), s.length * 8));
		},

		/*
		 * Calculate the HMAC-MD5, of a key and some data (raw strings)
		 */
		rstr_hmac: function (key, data) {
			var bkey = MD5.rstr2binl(key);
			if(bkey.length > 16)
				bkey = MD5.binl(bkey, key.length * 8);

			var ipad = Array(16), opad = Array(16);
			for(var i = 0; i < 16; i++) {
				ipad[i] = bkey[i] ^ 0x36363636;
				opad[i] = bkey[i] ^ 0x5C5C5C5C;
			}

			var hash = MD5.binl(ipad.concat(MD5.rstr2binl(data)), 512 + data.length * 8);
			return MD5.binl2rstr(MD5.binl(opad.concat(hash), 512 + 128));
		},

		/*
		 * Convert a raw string to a hex string
		 */
		rstr2hex: function(input) {
			var hex_tab = MD5.hexcase ? '0123456789ABCDEF' : '0123456789abcdef';
			var output = '';
			for(var i = 0; i < input.length; i++) {
				var x = input.charCodeAt(i);
				output += hex_tab.charAt((x >>> 4) & 0x0F) + hex_tab.charAt(x & 0x0F);
			}
			return output;
		},

		/*
		 * Convert a raw string to a base-64 string
		 */
		rstr2b64: function(input) {
			var tab = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
			var output = '';
			var len = input.length;
			for(var i = 0; i < len; i += 3) {
				var triplet = (input.charCodeAt(i) << 16)
					| (i + 1 < len ? input.charCodeAt(i+1) << 8 : 0)
					| (i + 2 < len ? input.charCodeAt(i+2) : 0);

				for(var j = 0; j < 4; j++) {
					if(i * 8 + j * 6 > input.length * 8)
						output += b64pad;
					else
						output += tab.charAt((triplet >>> 6*(3-j)) & 0x3F);
				}
			}
			return output;
		},

		/*
		 * Convert a raw string to an arbitrary string encoding
		 */
		rstr2any: function(input, encoding) {
			var divisor = encoding.length;
			var i, j, q, x, quotient;

			/* Convert to an array of 16-bit big-endian values, forming the dividend */
			var dividend = Array(Math.ceil(input.length / 2));
			for(i = 0; i < dividend.length; i++)
				dividend[i] = (input.charCodeAt(i * 2) << 8) | input.charCodeAt(i * 2 + 1);

			/*
			 * Repeatedly perform a long division. The binary array forms the dividend,
			 * the length of the encoding is the divisor. Once computed, the quotient
			 * forms the dividend for the next step. All remainders are stored for later
			 * use.
			 */
			var full_length = Math.ceil(input.length * 8 / (Math.log(encoding.length) / Math.log(2)));
			var remainders = Array(full_length);
			for(j = 0; j < full_length; j++) {
				quotient = Array();
				x = 0;
				for(i = 0; i < dividend.length; i++) {
					x = (x << 16) + dividend[i];
					q = Math.floor(x / divisor);
					x -= q * divisor;
					if(quotient.length > 0 || q > 0)
						quotient[quotient.length] = q;
				}
				remainders[j] = x;
				dividend = quotient;
			}

			/* Convert the remainders to the output string */
			var output = '';
			for(i = remainders.length - 1; i >= 0; i--)
				output += encoding.charAt(remainders[i]);

			return output;
		},

		/*
		 * Encode a string as utf-8.
		 * For efficiency, this assumes the input is valid utf-16.
		 */
		str2rstr_utf8: function(input) {
			var output = '';
			var i = -1;
			var x, y;

			while(++i < input.length) {
				/* Decode utf-16 surrogate pairs */
				x = input.charCodeAt(i);
				y = i + 1 < input.length ? input.charCodeAt(i + 1) : 0;
				if(0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF) {
					x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
					i++;
				}

				/* Encode output as utf-8 */
				if(x <= 0x7F)
					output += String.fromCharCode(x);
				else if(x <= 0x7FF)
					output += String.fromCharCode(0xC0 | ((x >>> 6 ) & 0x1F), 0x80 | ( x & 0x3F));
				else if(x <= 0xFFFF)
					output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F), 0x80 | ((x >>> 6 ) & 0x3F), 0x80 | ( x & 0x3F));
				else if(x <= 0x1FFFFF)
					output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07), 0x80 | ((x >>> 12) & 0x3F), 0x80 | ((x >>> 6 ) & 0x3F), 0x80 | ( x & 0x3F));
			}
			return output;
		},

		/*
		 * Encode a string as utf-16
		 */
		str2rstr_utf16le: function(input) {
			var output = '';
			for(var i = 0; i < input.length; i++)
				output += String.fromCharCode(input.charCodeAt(i) & 0xFF, (input.charCodeAt(i) >>> 8) & 0xFF);
			return output;
		},

		str2rstr_utf16be: function (input) {
			var output = '';
			for(var i = 0; i < input.length; i++)
				output += String.fromCharCode((input.charCodeAt(i) >>> 8) & 0xFF, input.charCodeAt(i) & 0xFF);
			return output;
		},

		/*
		 * Convert a raw string to an array of little-endian words
		 * Characters >255 have their high-byte silently ignored.
		 */
		rstr2binl: function(input) {
			var output = Array(input.length >> 2);
			for(var i = 0; i < output.length; i++)
				output[i] = 0;
			for(var i = 0; i < input.length * 8; i += 8)
				output[i>>5] |= (input.charCodeAt(i / 8) & 0xFF) << (i%32);
			return output;
		},

		/*
		 * Convert an array of little-endian words to a string
		 */
		binl2rstr: function(input) {
			var output = '';
			for(var i = 0; i < input.length * 32; i += 8)
				output += String.fromCharCode((input[i>>5] >>> (i % 32)) & 0xFF);
			return output;
		},

		/*
		 * Calculate the MD5 of an array of little-endian words, and a bit length.
		 */
		binl: function(x, len) {
			/* append padding */
			x[len >> 5] |= 0x80 << ((len) % 32);
			x[(((len + 64) >>> 9) << 4) + 14] = len;

			var a =	1732584193;
			var b = -271733879;
			var c = -1732584194;
			var d =	271733878;

			var md5_ff = MD5.md5_ff;
			var md5_gg = MD5.md5_gg;
			var md5_hh = MD5.md5_hh;
			var md5_ii = MD5.md5_ii;
			var safe_add = MD5.safe_add;

			for(var i = 0; i < x.length; i += 16) {
				var olda = a;
				var oldb = b;
				var oldc = c;
				var oldd = d;

				a = md5_ff(a, b, c, d, x[i+ 0], 7 , -680876936);
				d = md5_ff(d, a, b, c, x[i+ 1], 12, -389564586);
				c = md5_ff(c, d, a, b, x[i+ 2], 17,	606105819);
				b = md5_ff(b, c, d, a, x[i+ 3], 22, -1044525330);
				a = md5_ff(a, b, c, d, x[i+ 4], 7 , -176418897);
				d = md5_ff(d, a, b, c, x[i+ 5], 12,	1200080426);
				c = md5_ff(c, d, a, b, x[i+ 6], 17, -1473231341);
				b = md5_ff(b, c, d, a, x[i+ 7], 22, -45705983);
				a = md5_ff(a, b, c, d, x[i+ 8], 7 ,	1770035416);
				d = md5_ff(d, a, b, c, x[i+ 9], 12, -1958414417);
				c = md5_ff(c, d, a, b, x[i+10], 17, -42063);
				b = md5_ff(b, c, d, a, x[i+11], 22, -1990404162);
				a = md5_ff(a, b, c, d, x[i+12], 7 ,	1804603682);
				d = md5_ff(d, a, b, c, x[i+13], 12, -40341101);
				c = md5_ff(c, d, a, b, x[i+14], 17, -1502002290);
				b = md5_ff(b, c, d, a, x[i+15], 22,	1236535329);

				a = md5_gg(a, b, c, d, x[i+ 1], 5 , -165796510);
				d = md5_gg(d, a, b, c, x[i+ 6], 9 , -1069501632);
				c = md5_gg(c, d, a, b, x[i+11], 14,	643717713);
				b = md5_gg(b, c, d, a, x[i+ 0], 20, -373897302);
				a = md5_gg(a, b, c, d, x[i+ 5], 5 , -701558691);
				d = md5_gg(d, a, b, c, x[i+10], 9 ,	38016083);
				c = md5_gg(c, d, a, b, x[i+15], 14, -660478335);
				b = md5_gg(b, c, d, a, x[i+ 4], 20, -405537848);
				a = md5_gg(a, b, c, d, x[i+ 9], 5 ,	568446438);
				d = md5_gg(d, a, b, c, x[i+14], 9 , -1019803690);
				c = md5_gg(c, d, a, b, x[i+ 3], 14, -187363961);
				b = md5_gg(b, c, d, a, x[i+ 8], 20,	1163531501);
				a = md5_gg(a, b, c, d, x[i+13], 5 , -1444681467);
				d = md5_gg(d, a, b, c, x[i+ 2], 9 , -51403784);
				c = md5_gg(c, d, a, b, x[i+ 7], 14,	1735328473);
				b = md5_gg(b, c, d, a, x[i+12], 20, -1926607734);

				a = md5_hh(a, b, c, d, x[i+ 5], 4 , -378558);
				d = md5_hh(d, a, b, c, x[i+ 8], 11, -2022574463);
				c = md5_hh(c, d, a, b, x[i+11], 16,	1839030562);
				b = md5_hh(b, c, d, a, x[i+14], 23, -35309556);
				a = md5_hh(a, b, c, d, x[i+ 1], 4 , -1530992060);
				d = md5_hh(d, a, b, c, x[i+ 4], 11,	1272893353);
				c = md5_hh(c, d, a, b, x[i+ 7], 16, -155497632);
				b = md5_hh(b, c, d, a, x[i+10], 23, -1094730640);
				a = md5_hh(a, b, c, d, x[i+13], 4 ,	681279174);
				d = md5_hh(d, a, b, c, x[i+ 0], 11, -358537222);
				c = md5_hh(c, d, a, b, x[i+ 3], 16, -722521979);
				b = md5_hh(b, c, d, a, x[i+ 6], 23,	76029189);
				a = md5_hh(a, b, c, d, x[i+ 9], 4 , -640364487);
				d = md5_hh(d, a, b, c, x[i+12], 11, -421815835);
				c = md5_hh(c, d, a, b, x[i+15], 16,	530742520);
				b = md5_hh(b, c, d, a, x[i+ 2], 23, -995338651);
		
				a = md5_ii(a, b, c, d, x[i+ 0], 6 , -198630844);
				d = md5_ii(d, a, b, c, x[i+ 7], 10,	1126891415);
				c = md5_ii(c, d, a, b, x[i+14], 15, -1416354905);
				b = md5_ii(b, c, d, a, x[i+ 5], 21, -57434055);
				a = md5_ii(a, b, c, d, x[i+12], 6 ,	1700485571);
				d = md5_ii(d, a, b, c, x[i+ 3], 10, -1894986606);
				c = md5_ii(c, d, a, b, x[i+10], 15, -1051523);
				b = md5_ii(b, c, d, a, x[i+ 1], 21, -2054922799);
				a = md5_ii(a, b, c, d, x[i+ 8], 6 ,	1873313359);
				d = md5_ii(d, a, b, c, x[i+15], 10, -30611744);
				c = md5_ii(c, d, a, b, x[i+ 6], 15, -1560198380);
				b = md5_ii(b, c, d, a, x[i+13], 21,	1309151649);
				a = md5_ii(a, b, c, d, x[i+ 4], 6 , -145523070);
				d = md5_ii(d, a, b, c, x[i+11], 10, -1120210379);
				c = md5_ii(c, d, a, b, x[i+ 2], 15,	718787259);
				b = md5_ii(b, c, d, a, x[i+ 9], 21, -343485551);

				a = safe_add(a, olda);
				b = safe_add(b, oldb);
				c = safe_add(c, oldc);
				d = safe_add(d, oldd);
			}
			return Array(a, b, c, d);
		},

		/*
		 * These functions implement the four basic operations the algorithm uses.
		 */
		md5_cmn: function(q, a, b, x, s, t) {
			var safe_add = MD5.safe_add;
			return safe_add(MD5.bit_rol(safe_add(safe_add(a, q), safe_add(x, t)), s),b);
		},

		md5_ff: function(a, b, c, d, x, s, t) {
			return MD5.md5_cmn((b & c) | ((~b) & d), a, b, x, s, t);
		},

		md5_gg: function(a, b, c, d, x, s, t) {
			return MD5.md5_cmn((b & d) | (c & (~d)), a, b, x, s, t);
		},

		md5_hh: function(a, b, c, d, x, s, t) {
			return MD5.md5_cmn(b ^ c ^ d, a, b, x, s, t);
		},

		md5_ii: function(a, b, c, d, x, s, t) {
			return MD5.md5_cmn(c ^ (b | (~d)), a, b, x, s, t);
		},

		/*
		 * Add integers, wrapping at 2^32. This uses 16-bit operations internally
		 * to work around bugs in some JS interpreters.
		 */
		safe_add: function(x, y) {
			var lsw = (x & 0xFFFF) + (y & 0xFFFF);
			var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
			return (msw << 16) | (lsw & 0xFFFF);
		},

		/*
		 * Bitwise rotate a 32-bit number to the left.
		 */
		bit_rol: function(num, cnt) {
			return (num << cnt) | (num >>> (32 - cnt));
		}
	}
});Z8.define('Z8.util.DelayedTask', {
	delay: function(interval, fn, scope/*, params */) {
		this.cancel();

		this.fn = fn;
		this.scope = scope;
		this.params = Array.prototype.slice.call(arguments, 3);

		if(interval == 0) {
			this.interval = 0;
			this.timerFn(this);
		} else
			this.interval = setInterval(this.timerFn, interval, this);
	},

	cancel: function() {
		if(this.interval != null) {
			clearInterval(this.interval);
			this.interval = null;
		}
	},

	timerFn: function(me) {
		if(me.interval == null)
			return;

		me.cancel();
		me.fn.apply(me.scope, me.params);
	}
});Z8.define('Z8.Object', {
	dispose: function() {
		this.listeners = null;
		this.disposed = true;
	},

	on: function(event, callback, scope) {
		var listeners = this.listeners = this.listeners || {};
		event = event.toLowerCase();
		var eventListeners = listeners[event] || [];
		eventListeners.push({ fn: callback, scope: scope });
		listeners[event] = eventListeners;
	},

	un: function(event, callback, scope) {
		event = event.toLowerCase();

		var listeners = this.listeners;
		if(listeners == null || (listeners = listeners[event]) == null)
			return;

		for(var i = 0, length = listeners.length; i < length; i++) {
			var listener = listeners[i];
			if(listener.scope == scope) {
				listeners.removeAt(i);
				return;
			}
		}
	},

	fireEvent: function(event) {
		var listeners = this.listeners;
		if(listeners == null || (listeners = listeners[event.toLowerCase()]) == null)
			return;

		listeners = [].concat(listeners); // original listeners array can be modified inside the loop

		var args = Array.prototype.slice.call(arguments, 1);

		for(var i = 0, length = listeners.length; i < length; i++) {
			var listener = listeners[i];
			if(listener.fn != null)
				listener.fn.apply(listener.scope, args);
		}
	}
});Z8.define('Z8.data.HttpRequest', {
	extend: 'Z8.Object',
	shortClassName: 'HttpRequest',

	isUpload: false,
	isGet: false,

	defaultUrl: 'request.json',

	statics: {
		Timeout: 0,

		status: {
			AccessDenied: 401
		},

		state: {
			Unitialized: 0,
			Loading: 1,
			Loaded: 2,
			Interactive: 3,
			Complete: 4
		},

		contentType: {
			FormUrlEncoded: 'application/x-www-form-urlencoded; charset=UTF-8',
			FormMultipart: 'multipart/form-data'
		},

		url: function() {
			var location = window.location;
			var path = location.pathname;
			var length = path.length;

			if(length != 0) {
				var index = path.lastIndexOf('/');
				if(index != -1)
					path = path.substr(0, index);
			}
			return location.origin + path;
		},

		hostUrl: function() {
			return window.location.origin;
		},

		send: function(params, callback) {
			new HttpRequest().send(params, callback);
		},

		get: function(url, callback) {
			new HttpRequest().get(url, callback);
		},

		upload: function(params, files, callback) {
			new HttpRequest().upload(params, files, callback);
		}
	},

	constructor: function(config) {
		this.callParent(config);

		var xhr = this.xhr = new XMLHttpRequest();
		xhr.timeout = 0;

		DOM.on(xhr, 'load', this.onLoad, this);
		DOM.on(xhr, 'error', this.onError, this);
		DOM.on(xhr, 'abort', this.onError, this);
		DOM.on(xhr, 'loadEnd', this.onLoadEnd, this);
	},

	send: function(data, callback) {
		this.data = data;
		this.callback = callback;

		var xhr = this.xhr;
		xhr.open('POST', HttpRequest.url() + '/' + this.defaultUrl, true);
		xhr.setRequestHeader('Content-Type', HttpRequest.contentType.FormUrlEncoded);
		xhr.send(this.encodeData(data));
	},

	get:function(url, callback) {
		this.callback = callback;
		this.isGet = true;

		var xhr = this.xhr;
		xhr.open('GET', HttpRequest.url() + '/' + url, true);
		xhr.setRequestHeader('Content-Type', HttpRequest.contentType.FormUrlEncoded);
		xhr.send();
	},

	upload: function(data, files, callback) {
		this.data = data;
		this.files = files;
		this.callback = callback;
		this.isUpload = true;

		var xhr = this.xhr;
		xhr.open('POST', HttpRequest.url() + '/request.json', true);
		xhr.send(this.encodeFormData(data, files));
	},

	onLoad: function() {
		this.isLoaded = true;

		var xhr = this.xhr;

		if(this.isGet) {
			Z8.callback(this.callback, { text: xhr.responseText }, xhr.status == 200);
			return;
		}

		var response = {};

		try {
			response =  JSON.decode(xhr.responseText);
		} catch(exception) {
			var messages = [{ time: new Date(), type: 'error', text: exception.message }];
			response.info = { messages: messages };

			Z8.callback(this.callback, response, false);
			this.processResponse(response);
			return;
		}

		if(response.success) {
			if(response.retry == null) {
				Z8.callback(this.callback, response, true);
				this.processResponse(response);
			} else
				HttpRequest.send({ retry: response.retry, server: response.server }, this.callback);
		} else if(!this.relogin(response.status)) {
			response.info = response.info || {};
			Z8.callback(this.callback, response, false);
			this.processResponse(response);
		}
	},

	relogin: function(status) {
		if(!this.isLogin && status == HttpRequest.status.AccessDenied) {
			Application.login({ fn: this.onRelogin, scope: this });
			return true;
		}
		return false;
	},

	onRelogin: function() {
		this.isUpload ? HttpRequest.upload(this.data, this.files, this.callback) : HttpRequest.send(this.data, this.callback);
	},

	onError: function(event) {
		if(this.isLoaded)
			throw 'HttpRequest.onError: this.isLoaded == true;';

		var messages = [{ time: new Date(), type: 'error', text: 'Communication failure' }];
		var response = { info: { messages: messages } };
		Z8.callback(this.callback, response, false);
		this.processResponse(response);
	},

	onLoadEnd: function() {
		var xhr = this.xhr;

		DOM.un(xhr, 'load', this.onLoad, this);
		DOM.un(xhr, 'timeout', this.onError, this);
		DOM.un(xhr, 'error', this.onError, this);
		DOM.un(xhr, 'abort', this.onError, this);
		DOM.un(xhr, 'loadEnd', this.onLoadEnd, this);

		this.xhr = null;
	},

	encodeData: function(data) {
		var result = [];

		var isLogin = this.isLogin = data.login != null;

		if(isLogin)
			data.request = 'login';
		else
			data.session = Application.session;

		for(var name in data) {
			var value = data[name];
			if(Z8.isEmpty(value))
				continue;
			if(!String.isString(value))
				value = JSON.encode(value);
			result.push(encodeURIComponent(name) + '=' + encodeURIComponent(value));
		}

		return result.join('&');
	},

	encodeFormData: function(data, files) {
		data.session = Application.session;

		var formData = new FormData();
		for(var name in data) {
			var value = data[name];
			if(!Z8.isEmpty(value))
				formData.append(name, String.isString(value) ? value : JSON.encode(value));
		}

		for(var i = 0, length = files.length; i < length; i++) {
			var file = files[i];
			formData.append(file.name, file);
		}

		return formData;
	},

	processResponse: function(response) {
		var info = response.info;
		Application.message(info.messages);

		var files = info.files;
		if(files == null)
			return;

		for(var i = 0, length = files.length; i < length; i++)
			DOM.download(HttpRequest.hostUrl() + '/' + files[i].path, info.serverId);
	}
});Z8.define('Z8.data.Model', {
	extend: 'Z8.Object',
	shortClassName: 'Model',

	isModel: true,

	local: false,
	totals: false,

	destroyed: false,
	phantom: false,

	idProperty: 'recordId',
	lockProperty: 'lock',
	periodProperty: null,
	filesProperty: null,

	fields: null,
	ordinals: null,
	modified: null,
	original: null,

	busy: 0,

	statics: {
		activeSaves: 0,

		idSeed: 0,
		nextId: function() {
			Model.idSeed++;
			return 'r-' + Model.idSeed;
		},

		getFieldNames: function(fields) {
			var result = [];

			if(fields == null)
				return result;

			for(var i = 0, length = fields.length; i < length; i++)
				result.push(fields[i].name);

			return result;
		}
	},

	constructor: function(data) {
		var myData = this.data = {};
		this.editing = 0;

		for(var name in data) {
			var value = data[name];
			var field = this.getField(name);
			value = field != null ? field.convert(value, this) : value;
			myData[name] = value;
		}

		var idProperty = this.idProperty;
		var id = this.id = data != null ? data[idProperty] : null;
		if(id == null) {
			id = this.id = Model.nextId();
			myData[idProperty] = id;
			this.phantom = true;
		}

		var parentIdProperty = this.parentIdProperty;
		if(parentIdProperty == null)
			return;

		var parentId = this.parentId = data != null ? data[parentIdProperty] : null;
		if(parentId == null) {
			parentId = this.parentId = guid.Null;
			myData[parentIdProperty] = parentId;
		}
	},

	dispose: function() {
		this.store = null;
		this.callParent();
	},

	getName: function() {
		return this.name || this.$className;
	},

	getSourceCodeLocation: function() {
		return this.sourceCode || this.name || this.$className;
	},

	isLocal: function() {
		return this.local === true;
	},

	isRemote: function() {
		return this.local !== true;
	},

	hasTotalCount: function() {
		return this.totalCount !== false;
	},

	hasTotals: function() {
		return this.totals === true;
	},

	getAccess: function() {
		return this.access;
	},

	getIdProperty: function() {
		return this.idProperty;
	},

	getParentIdProperty: function() {
		return this.parentIdProperty;
	},

	getPeriodProperty: function() {
		return this.periodProperty;
	},

	getFilesProperty: function() {
		return this.filesProperty;
	},

	isEqual: function(left, right) {
		if(left instanceof Date && right instanceof Date)
			return Date.isEqual(left, right);
		return left == right;
	},

	getFields: function() {
		return this.fields;
	},

	getField: function(name) {
		return this.fields[this.getOrdinals()[name]];
	},

	getLinks: function() {
		return this.links;
	},

	getNames: function() {
		return this.names;
	},

	getColumns: function() {
		return this.columns;
	},

	getQuickFilters: function() {
		return this.quickFilters;
	},

	getRequestFields: function() {
		return this.requestFields;
	},

	getValueFor: function() {
		return this.valueFor;
	},

	getValueFrom: function() {
		return this.valueFrom;
	},

	getLink: function() {
		return this.link;
	},

	getQuery: function() {
		return this.query;
	},

	getId: function() {
		return this.id;
	},

	setId: function(id) {
		this.set(this.idProperty, id);
	},

	getLock: function() {
		var lockProperty = this.lockProperty;
		return lockProperty == null ? RecordLock.None : this.get(lockProperty);
	},

	getFiles: function() {
		var filesProperty = this.filesProperty;
		return filesProperty != null ? this.get(filesProperty) : null;
	},

	isEditable: function() {
		var lock = this.getLock();
		return lock != RecordLock.Edit && lock != RecordLock.Full;
	},

	isDestroyable: function() {
		var lock = this.getLock();
		return lock != RecordLock.Destroy && lock != RecordLock.Full;
	},

	getOrdinals: function() {
		if(this.ordinals != null)
			return this.ordinals;

		var ordinals = this.self.ordinals = {};
		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++)
			ordinals[fields[i].name] = i;

		return ordinals;
	},

	isPhantom: function() {
		return this.phantom;
	},

	isDirty: function() {
		return this.modified != null;
	},

	beginEdit: function() {
		this.editing++;
	},

	cancelEdit: function() {
		this.editing = 0;

		var data = this.data;
		var modified = this.modified;

		for(name in modified)
			data[name] = modified[name];

		this.modified = this.original = null;
	},

	endEdit: function() {
		var editing = this.editing = Math.max(0, this.editing - 1);

		if(editing != 0)
			return;

		var modified = this.modified;

		if(modified != null && Object.keys(modified).length != 0) {
			var store = this.store;
			if(store != null)
				store.onRecordChanged(this, modified);
			this.fireEvent('change', this, modified);
		}
	},

	getModifiedFields: function() {
		var fields = {};
		var modified = this.modified;
		var data = this.data;

		for(var name in modified)
			fields[name] = data[name];

		return fields;
	},

	get: function(name) {
		var value = this.data[name];
		return value !== undefined ? value : null;
	},

	set: function(name, value, silent, reset) {
		var data = this.data;
		var currentValue = data[name];
		var field = this.getField(name);

		var modified = this.modified;
		var original = this.original;

		var comparator = this;

		if(field != null) {
			value = field.convert(value, this);
			comparator = field;
		}

		if(!reset && comparator.isEqual(currentValue, value))
			return;

		if(silent) {
			data[name] = value;
			return;
		}

		if(original == null || !original.hasOwnProperty(name)) {
			original = this.original = this.original || {};
			modified = this.modified = this.modified || {};
			original[name] = currentValue;
			modified[name] = currentValue;
		} else if(original != null) {
			var originalValue = original[name];
			if(!reset && comparator.isEqual(originalValue, value)) {
				delete modified[name];
				if(Object.keys(modified).length == 0) {
					this.modified = null;
					this.original = null;
				}
			} else
				modified[name] = currentValue;
		}

		var idProperty = this.getIdProperty();
		var parentIdProperty = this.getParentIdProperty();

		if(idProperty == name)
			this.id = value;
		else if(parentIdProperty == name)
			this.parentId = value;

		data[name] = value;

		if(!this.editing) {
			var changes = {};
			changes[name] = currentValue;

			var store = this.store;
			if(store != null)
				store.onRecordChanged(this, changes);

			this.fireEvent('change', this, changes);
		}
	},

	setStore: function(store) {
		this.store = store;
	},

	getServerData: function(action) {
		var phantom = this.isPhantom();

		var data = {};

		if(!this.destroyed) {
			if(!phantom)
				Z8.apply(data, this.getModifiedFields());
			else
				Z8.apply(data, this.data);
		}

		data[this.idProperty] = phantom ? guid.Null : this.id;
		return data;
	},

	startOperation: function() {
		this.busy++;
		Z8.data.Model.activeSaves++;
	},

	finishOperation: function() {
		this.busy--;
		Z8.data.Model.activeSaves--;
	},

	isBusy: function() {
		return this.busy != 0;
	},

	reload: function(callback, options) {
		if(this.getAccess().read === false)
			throw 'Model ' + this.getName() + ' does not have read record privilege';

		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(this.isPhantom())
			throw 'phantom record can not be reloaded';

		this.executeAction('read', callback, Z8.apply(options || {}, { recordId: this.id }));
	},

	create: function(callback, options) {
		if(this.getAccess().create === false)
			throw 'Model ' + this.getName() + ' does not have create record privilege';

		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(!this.isPhantom())
			throw 'new record must be a phantom';

		this.executeAction('create', callback, options);
	},

	update: function(callback, options) {
		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(this.isPhantom())
			throw 'phantom record can not be updated';

		if(!this.isDirty())
			Z8.callback(callback, this, true);
		else
			this.executeAction('update', callback, options);
	},

	copy: function(record, callback, options) {
		if(this.getAccess().copy === false)
			throw 'Model ' + this.getName() + ' does not have copy record privilege';

		if(this.destroyed)
			throw 'record ' + this.id + ' is already destroyed';

		if(!this.isPhantom())
			throw 'copy of the record ' + this.id + ' must be a phantom';

		this.executeAction('copy', callback, Z8.apply(options || {}, { recordId: record.id }));
	},

	destroy: function(callback, options) {
		if(this.getAccess().destroy === false)
			throw 'Model ' + this.getName() + ' does not have destroy record privilege';

		if(this.isPhantom()) {
			var store = this.store;
			if(store != null)
				store.remove(this);
			Z8.callback(callback, this, true);
		} else
			this.executeAction('destroy', callback, options);
	},

	executeAction: function(action, callback, options) {
		if(this.isRemote()) {
			var batch = new Z8.data.Batch({ model: this, singleMode: true });
			batch.execute(action, [this], callback, options);
		} else
			Z8.callback(callback, this, true);
	},

	onAction: function(action, data) {
		if(action != 'destroy') {
			this.phantom = false;
			this.beginEdit();
			for(var name in data)
				this.set(name, data[name]);
			this.endEdit();
		} else {
			var store = this.store;
			if(store != null)
				store.remove(this);
			this.destroyed  = true;
		}
	},

	afterAction: function(action) {
		this.modified = this.original = null;
	},

	attach: function(name, files, callback) {
		if(this.getAccess().update === false)
			throw 'Model ' + this.getName() + ' does not have update record privilege';

		var filesToUpload = [];

		for(var i = 0, length = files.length; i < length; i++) {
			var file = files[i];
			if(Application.checkFileSize(file))
				filesToUpload.push(file);
		}

		if(filesToUpload.length == 0) {
			Z8.callback(callback, this, [], false);
			return;
		}

		var data = { request: this.getName(), action: 'attach', recordId: this.id, field: name };

		var requestCallback = function(response, success) {
			if(success) {
				this.set(name, response.data, true);
				Z8.callback(callback, this, this.get(name), true);
			} else
				Z8.callback(callback, this, [], false);
		};

		HttpRequest.upload(data, filesToUpload, { fn: requestCallback, scope: this });
	},

	detach: function(name, files, callback) {
		if(this.getAccess().update === false)
			throw 'Model ' + this.getName() + ' does not have update record privilege';

		var data = { request: this.getName(), action: 'detach', recordId: this.id, field: name, data: files };

		var requestCallback = function(response, success) {
			if(success) {
				this.set(name, response.data, true);
				Z8.callback(callback, this, this.get(name), true);
			} else
				Z8.callback(callback, this, [], false);
		};

		HttpRequest.send(data, { fn: requestCallback, scope: this });
	}
});
Z8.define('Z8.data.Store', {
	extend: 'Z8.Object',
	shortClassName: 'Store',

	isStore: true,

	filters: [],
	quickFilters: [],
	where: [],
	period: [],
	sorter: [],

	remoteFilter: true,
	remoteSort: true,

	page: 0,
	limit: 0,
	totalCount: 0,

	loadCount: 0,
	loading: 0,
	loadIndex: 0,
	transaction: 0,

	autoDispose: true,

	constructor: function(config) {
		var data = config.data;
		delete config.data;

		this.callParent(config);

		this.useCount = 0;

		this.inserted = [];
		this.positions = [];
		this.removed = [];

		this.setWhere(this.where);
		this.setFilter(this.filters);
		this.setQuickFilter(this.quickFilters);
		this.setPeriod(this.period);
		this.setSorter(this.sorter);

		this.initRecords(data);

		if(this.getCount() != 0) {
			this.loadTotalCount();
			this.loadTotals();
		}
	},

	use: function() {
		this.useCount++;
	},

	dispose: function() {
		if(this.useCount == 0)
			return;

		this.useCount--;

		if(this.useCount != 0)
			return;

		var records = this.records;
		for(var i = 0, length = records.length; i < length; i++)
			records[i].dispose();

		delete this.records;

		this.callParent();
	},

	getModel: function() {
		if(String.isString(this.model))
			this.model = Z8.classes[this.model];
		return this.model;
	},

	getModelName: function() {
		return this.isRemote() ? this.getModel().prototype.getName() : null;
	},

	getSourceCodeLocation: function() {
		return this.isRemote() ? this.getModel().prototype.getSourceCodeLocation() : null;
	},

	isLocal: function() {
		return this.getModel().prototype.isLocal();
	},

	isTree: function() {
		return this.getParentIdProperty() != null;
	},

	isRemote: function() {
		return this.getModel().prototype.isRemote();
	},

	hasTotals: function() {
		return this.getModel().prototype.hasTotals();
	},

	hasTotalCount: function() {
		return this.getModel().prototype.hasTotalCount();
	},

	getAccess: function() {
		return this.getModel().prototype.getAccess();
	},

	hasReadAccess: function() {
		return this.getAccess().read !== false;
	},

	hasWriteAccess: function() {
		return this.getAccess().write !== false;
	},

	hasCreateAccess: function() {
		return this.getAccess().create !== false;
	},

	hasCopyAccess: function() {
		return this.getAccess().copy !== false;
	},

	hasDestroyAccess: function() {
		return this.getAccess().destroy !== false;
	},

	getIdProperty: function() {
		return this.getModel().prototype.getIdProperty();
	},

	getParentIdProperty: function() {
		return this.getModel().prototype.getParentIdProperty();
	},

	getPeriodProperty: function() {
		return this.getModel().prototype.getPeriodProperty();
	},

	getFilesProperty: function() {
		return this.getModel().prototype.getFilesProperty();
	},

	getValueForFields: function() {
		return this.getModel().prototype.getValueFor();
	},

	getValueFromFields: function() {
		return this.getModel().prototype.getValueFrom();
	},

	getRecords: function() {
		return this.records;
	},

	getRange: function(start, end) {
		return this.records.slice(start, end + 1);
	},

	getFields: function() {
		return this.getModel().prototype.getFields();
	},

	getRequestFields: function() {
		return this.getModel().prototype.getRequestFields();
	},

	getLink: function() {
		return this.getModel().prototype.getLink();
	},

	getQuery: function() {
		return this.getModel().prototype.getQuery();
	},

	getField: function(name) {
		return this.getModel().prototype.getField(name);
	},

	getNames: function() {
		return this.getModel().prototype.getNames();
	},

	getColumns: function() {
		return this.getModel().prototype.getColumns();
	},

	getQuickFilters: function() {
		return this.getModel().prototype.getQuickFilters();
	},

	getCount: function() {
		return this.records.length;
	},

	getTotalCount: function() {
		return this.isRemote() ? this.totalCount : this.getCount();
	},

	getTotals: function() {
		return this.totals;
	},

	getRemoteFilter: function() {
		return this.isRemote() && this.remoteFilter;
	},

	getRemoteSort: function() {
		return this.isRemote() && this.remoteSort;
	},

	isEmpty: function() {
		return this.getCount() == 0;
	},

	getAt: function(index) {
		return this.records[index];
	},

	indexOf: function(record) {
		return record != null ? this.getOrdinals()[record.id] : null;
	},

	getById: function(id) {
		return this.records[this.getOrdinals()[id]];
	},

	add: function(records) {
		this.insert(records, this.getCount());
	},

	attach: function(records) {
		this.setStore(records, this);
	},

	detach: function(records) {
		this.setStore(records, null);
	},

	beginTransaction: function() {
		this.transaction++;
	},

	commit: function() {
		var transaction = this.transaction = Math.max(this.transaction - 1, 0);

		if(transaction != 0)
			return;

		var removed = this.removed;
		var hasRemoved = !Z8.isEmpty(removed);

		var inserted = this.inserted;
		var hasInserted = !Z8.isEmpty(inserted);

		if(!hasRemoved && !hasInserted)
			return;

		var records = this.records;

		this.ordinals = null;

		if(hasRemoved) {
			records.removeAll(removed);
			this.detach(removed);
			this.totalCount -= removed.length;
			this.fireEvent('remove', this, removed);
		}

		if(hasInserted) {
			var added = [];
			var positions = this.positions;
			for(var i = 0, length = inserted.length; i < length; i++) {
				var insert = inserted[i];
				records.insert(insert, positions[i]);
				added = added.concat(insert);
			}

			this.attach(added);
			this.totalCount += added.length;

			this.treefyRecords();

			var ranges = this.getIndexRanges(added);

			if(Array.isArray(ranges)) {
				for(var i = 0, length = ranges.length; i < length; i++) {
					var range = ranges[i];
					var start = range[0];
					var end = range[range.length - 1];
					this.fireEvent('add', this, this.getRange(start, end), start);
				}
			} else
				this.fireEvent('add', this, this.getRange(ranges, ranges + added.length - 1), ranges);
		}

		this.inserted = [];
		this.positions = [];
		this.removed = [];

		this.loadTotals();
	},

	rollback: function() {
		var transaction = this.transaction = Math.max(this.transaction - 1, 0);

		if(transaction != 0)
			return;

		this.inserted = [];
		this.positions = [];
		this.removed = [];
	},

	setStore: function(records, store) {
		records = Array.isArray(records) ? records : [records];
		for(var i = 0, length = records.length; i < length; i++)
			records[i].setStore(store);
	},

	insert: function(records, index) {
		if(records == null || records.length == 0)
			return;

		this.inserted.push(records);
		this.positions.add(index);

		if(this.transaction == 0)
			this.commit();
	},

	remove: function(records) {
		if(records == null)
			return;

		records = Array.isArray(records) ? records : [records];
		this.removed.add(records);

		if(this.transaction == 0)
			this.commit();
	},

	removeAll: function() {
		this.removed = [].concat(this.records);

		if(this.transaction == 0)
			this.commit();
	},

	removeAt: function(index) {
		if(index < 0 || index > this.getCount())
			return;

		var record = this.records[index];
		this.remove([record]);
	},

	getIndexRanges: function(records) {
		var ordinals = this.getOrdinals();
		var index = [];

		var length = records.length;

		if(length == 1)
			return ordinals[records[0].id];

		for(var i = 0; i < length; i++)
			index.push(ordinals[records[i].id]);

		index.sort(Number.compare);

		var previous = index[0];
		var range = [index[0]];
		var ranges = [range];

		for(var i = 1, length = index.length; i < length; i++) {
			var next = index[i];
			if(previous + 1 != next) {
				range = [next];
				ranges.push(range);
			} else
				range.push(next);

			previous = next;
		}

		if(ranges.length == 1)
			return ranges[0][0];

		return ranges;
	},

	getOrdinals: function() {
		if(this.ordinals != null)
			return this.ordinals;

		var ordinals = this.ordinals = {};
		var records = this.records;
		for(var i = 0, length = records.length; i < length; i++)
			ordinals[records[i].id] = i;

		return ordinals;
	},

	getValues: function() {
		return this.values;
	},

	setValues: function(values) {
		this.values = values;
	},

	getWhere: function() {
		return this.where;
	},

	setWhere: function(where) {
		where = where || [];
		return (this.where = Array.isArray(where) ? where : [where]);
	},

	getFilter: function() {
		return this.filters;
	},

	setFilter: function(filters) {
		filters = filters || [];
		return (this.filters = Array.isArray(filters) ? filters : [filters]);
	},

	getQuickFilter: function() {
		return this.quickFilters;
	},

	setQuickFilter: function(quickFilters) {
		quickFilters = quickFilters || [];
		return (this.quickFilters = Array.isArray(quickFilters) ? quickFilters : [quickFilters]);
	},

	getPeriod: function() {
		return this.period;
	},

	setPeriod: function(period) {
		return this.period = period;
	},

	getSorter: function() {
		return this.sorter;
	},

	setSorter: function(sorter) {
		sorter = sorter || [];
		return (this.sorter = Array.isArray(sorter) ? sorter : [sorter]);
	},

	filter: function(filters, options) {
		this.setFilter(filters);

		if(!this.getRemoteFilter()) {
			this.filterRecords();
			var records = this.records;
			this.fireEvent('load', this, records, true);
			Z8.callback(options, this, records, true);
		} else
			this.load(options);
	},

	quickFilter: function(quickFilters, options) {
		this.setQuickFilter(quickFilters);

		if(!this.getRemoteFilter()) {
			this.filterRecords();
			var records = this.records;
			this.fireEvent('load', this, records, true);
			Z8.callback(options, this, records, true);
		} else
			this.load(options);
	},

	sort: function(sorter, options) {
		this.setSorter(sorter);

		if(!this.getRemoteSort()) {
			this.sortRecords();
			var records = this.getRecords();
			this.fireEvent('load', this, records, true);
			Z8.callback(options, this, records, true);
		} else
			this.load(options);
	},

	initRecords: function(data) {
		var hasData = data != undefined;

		data = data || [];

		var records = this.records = [];
		this.ordinals = null;

		if(hasData) {
			var model = this.getModel();
			for(var i = 0, length = data.length; i < length; i++) {
				var record = Z8.create(model, data[i]);
				this.attach(record);
				records.push(record);
			}

			if(this.isLocal() && this.getSorter().length != 0)
				this.sortRecords();

			this.loadCount++;
		}

		return records;
	},

	isLoaded: function() {
		return this.loadCount != 0 || this.isLocal();
	},

	isLoading: function() {
		return this.loading != 0;
	},

	getLoadParams: function() {
		return {
			action: 'read',
			request: this.getModelName(),
			link: this.getLink(),
			query: this.getQuery(),
			fields: Model.getFieldNames(this.getRequestFields()),
			where: this.getWhere(),
			filter: this.getFilter(),
			quickFilter: this.getQuickFilter(),
			sort: this.getSorter(),
			period: this.getPeriod(),
			values: this.getValues(),
			start: this.page * this.limit,
			limit: this.limit // not to send limit if unlimited, e.g. limit == 0
		};
	},

	load: function(options) {
		this.fireEvent('beforeLoad', this);

		var params = this.getLoadParams();

		var callback = function(response, success) {
			var records = success ? this.initRecords(response.data || []) : [];
			this.fireEvent('load', this, records, success);
			Z8.callback(options, this, records, success);

			if(success) {
				this.loadTotalCount();
				this.loadTotals();
			}
		};

		this.loadIndex++;
		this.sendLoadRequest(params, { fn: callback, scope: this });
	},

	loadTotalCount: function(options) {
		if(!this.hasTotalCount())
			return;

		var count = this.getCount();

		if(count == 0 || !this.isRemote()) {
			this.calcTotalCount(count);
			this.fireEvent('count', this, true);
			Z8.callback(options, this, true);
			return;
		}

		var params = this.getLoadParams();
		params.count = true;

		var callback = function(response, success) {
			if(success) {
				this.calcTotalCount(response.total || 0);
				this.fireEvent('count', this, true);
			}

			Z8.callback(options, this, success);
		};

		this.fireEvent('beforeCount', this);
		this.sendLoadRequest(params, { fn: callback, scope: this });
	},

	calcTotalCount: function(totalCount) {
		this.totalCount = totalCount;
		var limit = this.limit || 0;
		this.page = limit != 0 && totalCount != 0 ? Math.min(this.page || 0, Math.floor(totalCount / limit) + (totalCount % limit == 0 ? 0 : 1) - 1) : 0;
	},

	loadTotals: function(options) {
		if(!this.hasTotals())
			return;

		var count = this.getCount();

		if(count == 0 || !this.isRemote()) {
			var totals = this.totals = this.calcTotals();
			this.fireEvent('totals', this, totals, true);
			Z8.callback(options, this, totals, true);
			return;
		}

		var params = this.getLoadParams();
		params.totals = true;

		var callback = function(response, success) {
			var totals = this.totals = Z8.create(this.getModel(), success ? response.data : {});
			this.fireEvent('totals', this, totals, success);
			Z8.callback(options, this, totals, success);
		};

		this.sendLoadRequest(params, { fn: callback, scope: this });
	},

	sendLoadRequest: function(params, callback) {
		this.loading++;
		var loadIndex = this.loadIndex;

		var sendCallback = function(response, success) {
			this.loading--;
			if(loadIndex != this.loadIndex)
				return;
			Z8.callback(callback, response, success);
		};

		HttpRequest.send(params, { fn: sendCallback, scope: this });
	},

	loadData: function(data) {
		var records = this.initRecords(data);
		this.fireEvent('load', this, records, true);
	},

	loadPage: function(page, options) {
		this.page = page;
		this.load(options);
	},

	unload: function() {
		this.loadCount = 0;
	},

	onRecordChanged: function(record, modified) {
		var idProperty = this.getIdProperty();
		var parentIdProperty = this.getParentIdProperty();

		if(idProperty in modified)
			this.onIdChanged(record, modified[idProperty]);

		if(parentIdProperty != null && parentIdProperty in modified)
			this.onParentIdChanged(record, modified[parentIdProperty]);

		this.loadTotals();
	},

	onIdChanged: function(record, oldId) {
		var ordinals = this.ordinals;
		if(ordinals != null) {
			index = ordinals[oldId];
			ordinals[record.id] = index;
			delete ordinals[oldId];
		}
		this.fireEvent('idChange', this, record, oldId);
	},

	onParentIdChanged: function(record, oldId) {
		this.treefyRecords();
		this.fireEvent('load', this, this.getRecords(), true);
	},

	sortRecords: function() {
		var sorters = this.getSorter();

		if(sorters.length == 0)
			return;

		var fields = [];
		for(var i = 0, length = sorters.length; i < length; i++)
			fields.push(this.getField(sorters[i].property));

		var sortFn = function(left, right) {
			for(var i = 0, length = sorters.length; i < length; i++) {
				var sorter = sorters[i];
				var property = sorter.property;
				var field = fields[i];
				left = left.get(property);
				right = right.get(property);
				var result = fields[i].compare(left, right);
				if(result != 0)
					return sorter.direction == 'asc' ? result : -result;
			}
			return 0;
		};

		this.records.sort(sortFn);
		this.ordinals = null;
	},

	filterRecords: function() {
		var filters = this.filters.concat(this.where).concat(this.quickFilters).concat(this.period);

		if(this.recordsCache != null) {
			this.records = this.recordsCache;
			this.recordsCache = null;
			this.ordinals = null;
		}

		if(filters.length == 0)
			return;

		var cache = this.recordsCache = this.records;
		var records = this.records = [];

		for(var i = 0, recordCount = cache.length; i < recordCount; i++) {
			var record = cache[i];
			for(var j = 0, filterCount = filters.length; j < filterCount; j++) {
				if(!Operator.applyFilter(record, filters[j]))
					break;
			}

			if(j == filterCount)
				records.push(record);
		}

		this.ordinals = null;
	},

	treefyRecords: function() {
		this.sortRecords();

		if(!this.isTree())
			return;

		var map = {};
		var roots = [];

		var records = this.getRecords();

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			var parentId = record.parentId;

			if(parentId != guid.Null) {
				var children = map[parentId];

				if(children == null)
					map[parentId] = children = [];

				children.push(record);
			} else
				roots.push(record);
		}

		var populate = function(records, level) {
			for(var i = 0, length = records.length; i < length; i++) {
				var record = records[i];
				var children = map[record.id];
				var data = record.data;
				data.hasChildren = children != null;
				data.level = level;
				result.push(record);
				if(children != null)
					populate(children, level + 1);
			}
		};

		var result = [];
		populate(roots, 0);

		this.records = result;
		this.ordinals = null;
	},

	calcTotals: function() {
		return this.totals = Z8.create(this.getModel(), {});
	}
});Z8.define('Z8.data.Batch', {
	extend: 'Z8.Object',

	model: null,
	store: null,
	singleMode: false,

	getModel: function() {
		if(this.store != null)
			this.model = this.store.getModel();

		var model = this.model;

		if(String.isString(model))
			model = this.model = Z8.classes[this.model];

		if(typeof model == 'function')
			model = this.model = model.prototype;

		return model;
	},

	getServerData: function(records, action) {
		var data = [];
		for(var i = 0, length = records.length; i < length; i++)
			data.push(records[i].getServerData(action));
		return data;
	},

	create: function(records, callback, options) {
		this.execute('create', records, callback, options);
	},

	update: function(records, callback, options) {
		this.execute('update', records, callback, options);
	},

	destroy: function(records, callback, options) {
		this.execute('destroy', records, callback, options);
	},

	execute: function(action, records, callback, options) {
		var singleMode = this.singleMode;

		if(singleMode && Array.isArray(records) && records.length != 1)
			throw 'Multiple records can not be saved in single mode';

		records = Array.isArray(records) ? records : [records];

		var model = this.getModel();
		var data = this.getServerData(records, action);

		var params = {
			request: model.getName(),
			action: action,
			data: data,
			fields: Model.getFieldNames(model.getRequestFields()),
			link: model.getLink(),
			query: model.getQuery()
		};

		Z8.apply(params, options);

		this.beginTransaction();

		var requestCallback = function(response, success) {
			if(success) {
				var data = response.data || [];

				for(var i = 0, length = records.length; i < length; i++)
					records[i].onAction(action, data[i]);

				this.commit();

				Z8.callback(callback, singleMode ? records[0] : records, true);

				for(var i = 0, length = records.length; i < length; i++)
					records[i].afterAction(action);
			} else {
				this.rollback();
				Z8.callback(callback, singleMode ? records[0] : records, false);
			}
		};

		HttpRequest.send(params, { fn: requestCallback, scope: this });
	},

	beginTransaction: function() {
		if(this.store != null)
			this.store.beginTransaction();
	},

	commit: function() {
		if(this.store != null)
			this.store.commit();
	},

	rollback: function() {
		if(this.store != null)
			this.store.rollback();
	}
});Z8.define('Z8.data.Action', {
	extend: 'Z8.Object',
/*
	text: '', 
	id: guid.Null, 
	target: null,
	global: false,
*/
	statics: {
		actionsQueue: []
	},

	target: 'ru.ivk.postfactor.desktops.Actions',

	execute: function(options) {
		this.options = options;
		this.getParameters(options);
	},

	getParameters: function(options) {
		var okCallback = function(parameters) {
			var records = options.records || [];
			this.fireEvent('beforeaction', this, options);
			this.executeAction(records, parameters);
		};

		var cancelCallback = function() {
			Z8.callback(this.options, this, null, false);
		};

		if(this.form != null) {
			var records = options.records || [];
			var form = {};
			var type = this.form;

			if(typeof this.form == 'object') {
				type = form.type;
				delete form.type;
				form = Z8.apply({}, this.form);
			}

			form.records = records;
			var form = Z8.create(type, form);
			form.on('ok', okCallback, this);
			form.on('cancel', cancelCallback, this);
			form.show();
		} else
			okCallback.call(this, null);
	},

	executeAction: function(records, parameters) {
		parameters = Z8.apply(this.options.parameters || {}, parameters);

		var params = {
			xaction: 'command',
			request: this.target,
			command: this.command || this.id, 
			parameters: parameters, 
			data: this.global ? [] :  this.getRecordIds(records)
		};

		var callback = function(response, success) {
			Z8.data.Action.actionsQueue.shift();
			Z8.callback(this.options, this, response, success);
			this.fireEvent('action', this, success);
		};

		Z8.data.Action.actionsQueue.push(this);

		this.delayedRequest(params, callback);
	},

	delayedRequest: function(params, callback) {
		if(Z8.data.Model.activeSaves != 0 || Z8.data.Action.actionsQueue[0] != this) {
			this.task = this.task || new Z8.util.DelayedTask();
			this.task.delay(100, this.delayedRequest, this, params, callback);
		} else
			HttpRequest.send(params, { fn: callback, scope: this });
	},

	getRecordIds: function(records) {
		var result = [];

		for(var i = 0; i < records.length; i++)
			result.push(records[i].id);

		return result;
	}
});Z8.define('Z8.data.Filter', {
	shortClassName: 'Filter',

	statics: {
		NoAction: 0,
		Apply: 1,
		Clear: 2
	},

	active: false,
	current: null,

	constructor: function(config) {
		this.callParent(config);
		this.filters = this.filters || {};
	},

	isActive: function() {
		return this.active && !this.isEmpty();
	},

	isEmpty: function() {
		return this.current == null;
	},

	setActive: function(active) {
		this.active = active;
	},

	getActive: function() {
		return this.active ? this.getCurrent() : null;
	},

	getNames: function() {
		return Object.keys(this.filters);
	},

	get: function(name) {
		return this.filters[name];
	},

	getCurrent: function() {
		return this.get(this.current);
	},

	setCurrent: function(current) {
		this.current = current;
	},

	add: function(name, filter) {
		this.filters[name] = filter;
	},

	remove: function(name) {
		delete this.filters[name];
		if(this.current == name)
			this.current = null;
	},

	clear: function() {
		this.filters = {};
		this.current = null;
	},

	toJson: function() {
		return { active: this.active, current: this.current, filters: this.filters };
	},

	toStoreData: function() {
		var records = [];

		var filters = this.filters;

		for(var name in filters)
			records.push({ name: name, filter: filters[name] });

		return records;
	}
});Z8.define('Z8.data.Period', {
	shortClassName: 'Period',

	statics: {
		NoAction: 0,
		Apply: 1,
		Clear: 2
	},

	constructor: function(config) {
		this.callParent(config);
		this.start = Parser.date(this.start);
		this.finish = Parser.date(this.finish);
	},

	isActive: function() {
		return this.active;
	},

	setActive: function(active) {
		this.active = active;
	},

	getStart: function() {
		return this.active ? this.start : null;
	},

	setStart: function(start) {
		this.start = Parser.date(start);
	},

	getFinish: function() {
		return this.active ? this.finish : null;
	},

	setFinish: function(finish) {
		this.finish = Parser.date(finish);
	},

	getActive: function(property) {
		return this.active ? this.get() : null;
	},

	get: function(property) {
		var filter = [];
		if(this.start != null)
			filter.push({ property: property, operator: Operation.GE, value: this.start });
		if(this.finish != null)
			filter.push({ property: property, operator: Operation.LE, value: this.finish });
		return filter;
	},

	getText: function() {
		var start = this.start;
		var finish = this.finish;

		if(start == null && finish == null)
			return null;

		if(start != null && finish != null) {
			if(Date.isEqualDate(start, finish))
				return Format.date(start, Format.Date);

			var startFormat = Format.Date;
	
			if(start.getYear() == finish.getYear())
				startFormat = start.getMonth() == finish.getMonth() ? 'd' : 'd.m';

			return Format.date(start, startFormat) + ' - ' + Format.date(finish, Format.Date);
		}

		return (start != null ? 'после' : 'до') + ' ' + Format.date(start || finish, Format.Date);
	},

	toJson: function() {
		return { active: this.active, start: this.start, finish: this.finish };
	}
});Z8.define('Z8.data.field.Field', {
	isField: true,

	name: null,
	type: Type.String,

	persist: true,

	constructor: function(config) {
		this.callParent(config);
	},

	compare: function(left, right) {
		if((left == null || left === '') && (right == null || right === ''))
			return 0;

		return (left == right) ? 0 : ((left < right) ? -1 : 1);
	},

	isEqual: function(left, right) {
		return this.compare(left, right) == 0;
	},

	getType: function() {
		return this.type;
	},

	serialize: function(value, record) {
		return value;
	},

	convert: function(value, record) {
		return value;
	}
});Z8.define('Z8.data.field.String', {
	extend: 'Z8.data.field.Field',

	type: Type.String
});
Z8.define('Z8.data.field.Boolean', {
	extend: 'Z8.data.field.Field',

	type: Type.Boolean,

	convert: function(value) {
		return typeof value != 'boolean' ? value == 'true' : value;
	}
});
Z8.define('Z8.data.field.Datetime', {
	extend: 'Z8.data.field.Field',

	type: Type.Datetime,
	format: Format.Datetime,

	compare: function(left, right) {
		if(Date.isDate(left) && Date.isDate(right))
			return left.getTime() - right.getTime();
		return left == right ? 0 : -1;
	},

	convert: function(value) {
		if(Date.isDate(value))
			return value;
		return Z8.isEmpty(value) ? null : new Date(value);
	},

	serialize: function(value) {
		return Date.isDate(value) ? value.toISOString() : '';
	}
});
Z8.define('Z8.data.field.Date', {
	extend: 'Z8.data.field.Datetime',

	type: Type.Date,
	format: Format.Date
});
Z8.define('Z8.data.field.Guid', {
	extend: 'Z8.data.field.String',

	type: Type.Guid
});
Z8.define('Z8.data.field.Float', {
	extend: 'Z8.data.field.Field',

	type: Type.Integer,

	convert: function(value) {
		if(Number.isNumber(value))
			return value;

		if(value == null || value == '')
			return 0;

		value = parseFloat(value);
		return isNaN(value) ? 0 : value;
	}
});
Z8.define('Z8.data.field.Integer', {
	extend: 'Z8.data.field.Float',

	type: Type.Integer,

	convert: function(value) {
		value = this.callParent(value);
		return value > 0 ? Math.floor(value) : Math.ceil(value);
	}
});
Z8.define('Z8.data.field.Json', {
	extend: 'Z8.data.field.String',

	convert: function(value) {
		if(!String.isString(value))
			return value;
		try {
			return value != '' ? JSON.decode(value) : null;
		} catch(e) {
			return null;
		}
	},

	serialize: function(value) {
		return value != null ? JSON.encode(value) : null;
	},

	compare: function(left, right) {
		left = String.isString(left) ? left : JSON.encode(left);
		right = String.isString(right) ? right : JSON.encode(right);
		return left == right ? 0 : -1;
	}
});
Z8.define('Z8.data.field.Files', {
	extend: 'Z8.data.field.Json',

	type: Type.Files,

	convert: function(value) {
		return this.callParent(value) || [];
	}
});
Z8.define('Z8.Component', {
	extend: 'Z8.Object',
	shortClassName: 'Component',

	isComponent: true,

	enabled: true,
	tabIndex: -1,

	statics: {
		idSeed: 0,

		nextId: function() {
			this.idSeed++;
			return 'id' + this.idSeed;
		},

		destroy: function(component) {
			if(component == null)
				return;

			var components = Array.isArray(component) ? component : [component];

			for(var i = 0, length = components.length; i < length; i++) {
				component = components[i];
				if(component == null || component.disposed)
					continue;
				if(component.isComponent)
					components[i].destroy();
				else if(DOM.isDom(component))
					DOM.remove(component);
				else if(component.dom != null) {
					DOM.remove(component.dom);
					component.dom = null;
				}
			}
		}
	},

	constructor: function(config) {
		this.callParent(config);
		this.initComponent();
	},

	initComponent: function() {
		if(this.id == null)
			this.id = Z8.Component.nextId();

		this.enabledLock = !this.enabled;
	},

	getId: function() {
		return this.id;
	},

	getDom: function() {
		return this.dom;
	},

	getTitle: function() {
		return this.title;
	},

	setTitle: function(title) {
		this.title = title;
	},

	isEnabled: function() {
		return this.enabled;
	},

	setEnabled: function(enabled) {
		this.enabled = enabled;
		this.setTabIndex(enabled ? this.tabIndex: -1);
	},

	getTabIndex: function() {
		return this.enabled ? this.tabIndex : -1;
	},

	setTabIndex: function(tabIndex) {
		if(tabIndex != -1)
			this.tabIndex = tabIndex;
		return tabIndex;
	},

	setText: function(text) {
		DOM.setInnerHTML(this, text);
	},

	isVisible: function() {
		return this.visible;
	},

	show: function() {
		this.visible = true;
		DOM.removeCls(this, 'display-none');
	},

	hide: function() {
		this.visible = false;
		DOM.addCls(this, 'display-none');
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this) : false;
	},

	selectNode: function(selector) {
		return DOM.selectNode(this, selector);
	},

	queryNodes: function(selector) {
		return DOM.query(this, selector);
	},

	appendTo: function(element) {
		var dom = this.dom;

		if(dom == null)
			this.render(element);
		else
			DOM.append(element, dom);
	},

	render: function(dom) {
		if(this.dom == null) {
			var markup = this.htmlMarkup();
			this.dom = DOM.append(dom, markup);
			this.renderDone();
		}
	},

	getWidth: function() {
		var dom = this.dom;
		return dom != null ? Ems.pixelsToEms(dom.offsetWidth) : 0;
	},

	getHeight: function() {
		var dom = this.dom;
		return dom != null ? Ems.pixelsToEms(dom.offsetHeight) : 0;
	},

	getMinWidth: function() {
		return this.minWidth || this.width || 0;
	},

	getMinHeight: function() {
		return this.minHeight || this.height || 0;
	},

	getBoxMinWidth: function() {
		return this.getMinWidth();
	},

	getBoxMinHeight: function() {
		return this.getMinHeight();
	},

	getSize: function() {
		var dom = this.dom;
		return { width: this.getWidth(), height: this.getHeight() };
	},

	updateSize: function() {
		var width = this.width;
		if(width > 0 || String.isString(width))
			DOM.setPoint(this, 'width', width);

		var height = this.height;
		if(height > 0 || String.isString(height))
			DOM.setPoint(this, 'height', height);

		var minWidth = this.getMinWidth();
		if(minWidth > 0 || String.isString(minWidth))
			DOM.setPoint(this, 'minWidth', minWidth);

		var minHeight = this.getMinHeight();
		if(minHeight > 0 || String.isString(minHeight))
			DOM.setPoint(this, 'minHeight', minHeight);
	},

	completeRender: function() {
		this.updateSize();

		var subcomponents = this.subcomponents();

		for(var i = 0, length = subcomponents.length; i < length; i++) {
			var component = subcomponents[i];
			if(component == null || component.dom != null)
				continue;
			if(component.id != null)
				component.dom = this.selectNode('#' + component.id);
			if(component.isComponent)
				component.completeRender();
		}
	},

	afterRender: function() {
		var subcomponents = this.subcomponents();

		for(var i = 0, length = subcomponents.length; i < length; i++) {
			var component = subcomponents[i];
			if(component == null || component.afterRenderCalled)
				continue;
			if(component.isComponent) {
				component.afterRender();
				component.afterRenderCalled = true;
			}
		}

		if(this.renderOptions != null)
			Z8.callback(this.renderOptions);
	},

	renderDone: function() {
		this.completeRender();
		this.afterRender();
	},

	destroy: function() {
		this.destroying = true;
		this.onDestroy();
		this.destroying = false;

		this.dispose();
	},

	onDestroy: function() {
		Component.destroy(this.subcomponents());
		DOM.remove(this);
		this.alignment = this.dom = null;
	},

	subcomponents: function() {
		return [];
	},

	htmlMarkup: function() {
		return { id: this.getId(), cls: DOM.parseCls(this.cls).join(' '), tabIndex: this.getTabIndex(), html: this.html || '' };
	},

	setPosition: function(left, top) {
		DOM.setPoint(this, 'left', left);
		DOM.setPoint(this, 'top', top);
	},

	setAlignment: function(alignment) {
		this.alignment = alignment;
	},

	setAlignmentOffset: function(horizontal, vertical, margin) {
		this.alignmentOffset = { width: horizontal, height: vertical, margin: margin };
	},

	getClipping: function() {
		if(this.clipping == null)
			this.clipping = DOM.getClipping(this);
		return this.clipping;
	},

	align: function() {
		var alignment = this.alignment;

		var clipping = this.getClipping();

		var viewport = new Rect(clipping);
		var align = new Rect(alignment || this);
		if(alignment == null) {
			align.height = align.width = 0;
			align.bottom = align.top
			align.right = align.left;
		}

		var parent = new Rect(DOM.getParent(this));

		var offset = this.alignmentOffset || { width: 0, height: 0, margin: 5 };
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
		rect.left = align.left - (spaceRight < width ? width - spaceRight : 0);

		if(!above) {
			rect.top = align.top + align.height;
			rect.bottom = rect.top + (Math.min(available, height) - offset.height) - (available < height ? offset.margin : 0);
		} else {
			rect.bottom = align.top - offset.height;
			rect.top = rect.bottom - (Math.min(available, height) - offset.height) + (available < height ? offset.margin : 0);
		}

		rect.left = rect.left - parent.left;
		rect.top = rect.top - parent.top;
		rect.bottom = parent.bottom - rect.bottom;

		DOM.setLeft(this, rect.left);
		DOM.setTop(this, rect.top);
		if(alignment != null)
			DOM.setBottom(this, rect.bottom);
		DOM.swapCls(this, above, 'pull-up', 'pull-down');

		this.fireEvent('align', this);
	}
});Z8.define('Z8.Container', {
	extend: 'Z8.Component',

	isContainer: true,

	constructor: function(config) {
		config = config || {};
		var items = config.items = config.items || [];

		config.minHeight = false;

		for(var i = 0, length = items.length; i < length; i++)
			items[i].container = this;

		this.callParent(config);
	},

	subcomponents: function() {
		return this.items;
	},

	htmlMarkup: function() {
		var markup = [];

		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			item.container = this;
			markup.push(item.htmlMarkup != null ? item.htmlMarkup() : item);
		}

		var cls = this.cls = DOM.parseCls(this.cls);
		return { id: this.getId(), cls: cls.join(' '), tabIndex: this.getTabIndex(), cn: markup };
	},

	onDestroy: function() {
		Component.destroy(this.items);
		this.callParent();
	},

	getCount: function() {
		return this.items.length;
	},

	indexOf: function(item) {
		return this.items.indexOf(item);
	},

	add: function(component, index) {
		var items = this.items;
		var result = component;

		var components = Array.isArray(component) ? component : [component];

		for(var i = 0, length = components.length; i < length; i++) {
			component = components[i];

			if(this.indexOf(component) != -1)
				continue;

			var container = component.container;

			if(container != null && container != this)
				container.items.remove(component);

			component.container = this;

			var before = index != null ? items[index] : null;

			items.insert(component, index);

			if(index != null)
				index++;

			if(this.getDom() == null)
				continue;

			var dom = component.dom;

			if(dom == null) {
				var markup = component.isComponent != null ? component.htmlMarkup() : component;
				component.dom = before != null ? DOM.insertBefore(before, markup) : DOM.append(this, markup);
				if(component.isComponent)
					component.renderDone();
			} else
				before != null ? DOM.insertBefore(before, dom) : DOM.append(this, dom);
		}

		return result;
	},

	remove: function(component) {
		var items = this.items;

		if(Array.isArray(component))
			component = items.removeAll(component);
		else if(Number.isNumber(component))
			component = items.removeAt(component);
		else
			component = items.remove(component);

		Component.destroy(component);
	},

	removeAll: function() {
		Component.destroy(this.items);
		this.items = [];
	},

	focus: function() {
		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.focus != null && item.focus())
				return true;
		}
		return this.callParent();
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isComponent && !item.enabledLock)
				item.setEnabled(enabled);
		}
	}
});Z8.define('Z8.button.Button', {
	extend: 'Z8.Component',

	tabIndex: 0,

	text: '',
	icon: null,

	primary: false,
	danger: false,
	success: false,
	info: false,

	split: false,
	trigger: null,
	menu: null,

	toggle: false,
	toggled: null,
	vertical: false,

	busy: false,

	tooltip: '',
	triggerTooltip: '',

	iconTag: 'i',

	initComponent: function() {
		this.callParent();
	},

	htmlMarkup: function() {
		this.setIcon(this.icon).join(' ');

		this.toggle = this.toggle || this.toggled != null;
		this.split = this.split || this.menu != null;

		var iconCls = DOM.parseCls(this.iconCls).join(' ');

		var icon = { tag: this.iconTag, icon: this.getId(), cls: iconCls, html: String.htmlText() };
		var text = { tag: 'span', cls: 'text', html: String.htmlText(this.text) };
		var title = this.tooltip || '';

		var button = { tag: 'a', anchor: this.getId(), cls: this.getButtonCls().join(' '), tabIndex: this.getTabIndex(), title: title, cn: [icon, text] };

		if(!this.split) {
			button.id = this.getId(); 
			return button;
		}

		var trigger = this.trigger = new Z8.button.Trigger({ cls: this.cls, primary: this.primary, danger: this.danger, success: this.success, info: this.info, tooltip: this.triggerTooltip, icon: this.triggerIcon, tabIndex: this.getTabIndex(), enabled: this.enabled });

		var cn = [button, trigger.htmlMarkup()];

		var menu = this.menu;

		if(menu != null) {
			menu.owner = trigger;
			cn.push(menu.htmlMarkup());
		}

		return { tag: 'div', id: this.getId(), cls: 'btn-group' + (this.vertical ? '-vertical' : ''), cn: cn };
	},

	subcomponents: function() {
		return this.split ? (this.menu != null ? [this.trigger, this.menu] : [this.trigger]) : [];
	},

	completeRender: function() {
		this.callParent();

		this.button = this.selectNode('a[anchor=' + this.getId() + ']') || this.getDom();
		this.icon = this.selectNode(this.iconTag + '[icon=' + this.getId() + ']');
		this.textElement = this.selectNode('.text');

		if(!this.disableEvents) {
			DOM.on(this, 'click', this.onClick, this);
			DOM.on(this, 'keyDown', this.onKeyDown, this);
		}

		var menu = this.menu;

		if(menu != null) {
			menu.setAlignment(this);
			menu.on('show', this.onMenuShow, this);
			menu.on('hide', this.onMenuHide, this);
		}
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.button = this.icon = this.textElement = null;

		this.callParent();
	},

	setEnabled: function(enabled) {
		this.wasEnabled = enabled;

		DOM.swapCls(this.button, !enabled, 'disabled');

		if(this.trigger != null)
			this.trigger.setEnabled(enabled);

		this.callParent(enabled);
	},

	getButtonTypeCls: function() {
		if(this.primary)
			return 'btn-primary';
		else if(this.danger)
			return 'btn-danger';
		else if(this.success)
			return 'btn-success';
		else if(this.info)
			return 'btn-info';
		return 'btn-default';
	},

	getButtonCls: function() {
		var cls = DOM.parseCls(this.cls).pushIf('btn', this.getButtonTypeCls());

		if(!this.isEnabled())
			cls.pushIf('disabled');

		if(this.toggle && this.toggled)
			cls.pushIf('active');

		return cls;
	},

	getIconCls: function(cls) {
		var iconCls = DOM.parseCls(cls).pushIf('fa');
		if(Z8.isEmpty(cls))
			iconCls.pushIf('no-icon');
		if(Z8.isEmpty(this.text))
			iconCls.pushIf('no-text');
		return iconCls;
	},

	setIcon: function(cls) {
		cls = this.iconCls = this.getIconCls(cls);
		DOM.setCls(this.icon, this.iconCls);
		return cls;
	},

	setPrimary: function(primary) {
		this.primary = primary;
		DOM.swapCls(this.button, primary, 'btn-primary', 'btn-default');

		if(this.trigger != null)
			this.trigger.setPrimary(primary);
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		DOM.setTabIndex(this.button, tabIndex);

		if(this.trigger != null)
			this.trigger.setTabIndex(tabIndex);
	},

	getText: function() {
		return this.text;
	},

	setText: function(text) {
		this.text = text || '';
		var noText = Z8.isEmpty(text);
		DOM.setValue(this.textElement, String.htmlText(text));
		this.iconCls = DOM.swapCls(this.icon, noText, 'no-text') || this.iconCls;
	},

	setTooltip: function(tooltip) {
		this.tooltip = tooltip || '';
		DOM.setAttribute(this.button, 'title', tooltip);
	},

	isRadio: function() {
		var container = this.container;
		return container != null && container.radio;
	},

	setToggled: function(toggled, silent) {
		this.toggle = true;
		this.toggled = toggled;

		DOM.swapCls(this.button, toggled, 'active');

		if(silent)
			return;

		if(this.isRadio())
			this.container.onRadioToggle(this, toggled);

		this.fireEvent('toggle', this, toggled);
	},

	isBusy: function() {
		return this.busy;
	},

	setBusy: function(busy) {
		if(this.busy != busy) {
			this.busy = busy;

			if(busy) {
				this.cachedIconCls = this.iconCls;
				var wasEnabled = this.isEnabled()
				this.setEnabled(false);
				this.wasEnabled = wasEnabled;
				this.setIcon(['fa-circle-o-notch', 'fa-spin']);
			} else {
				this.setEnabled(this.wasEnabled);
				this.setIcon(this.cachedIconCls);
			}

			DOM.swapCls(this, busy, 'z-index-10');
		}
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this.button) : false;
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled())
			return;

		var button = DOM.isParentOf(this.button, target);
		var trigger = !button && DOM.isParentOf(this.trigger, target);

		if(button && this.toggle) {
			this.setToggled(!this.isRadio() ? !this.toggled : true, false);
			return;
		}

		if(trigger && this.menu != null) {
			this.toggleMenu();
			return;
		}

		if(!button && !trigger)
			return;

		var handler = button ? this.handler : this.triggerHandler;
		Z8.callback(handler, this.scope, this);

		this.fireEvent(button ? 'click' : 'triggerClick', this);
	},

	onKeyDown: function(event, target) {
		if(!this.isEnabled())
			return;

		var key = event.getKey();

		if(key == Event.ENTER || key == Event.SPACE)
			this.onClick(event, DOM.isParentOf(this.menu, target) ? this.trigger.button.getDom() : target);
		else if((key == Event.DOWN || key == Event.UP || key == Event.HOME || key == Event.END) && this.menu != null)
			this.onClick(event, this.trigger.button);
	},

	toggleMenu: function() {
		this.menu.toggle();
	},

	onMenuShow: function() {
		DOM.addCls(this, 'open');
		this.trigger.rotateIcon(180);
	},

	onMenuHide: function() {
		DOM.removeCls(this, 'open');
		this.trigger.rotateIcon(0);

		DOM.focus(this.trigger);
	},

	rotateIcon: function(degree) {
		DOM.rotate(this.icon, degree);
	}
});
Z8.define('Z8.button.Trigger', {
	extend: 'Z8.button.Button',

	tabIndex: -1,

	constructor: function(config) {
		config = config || {};
		config.cls = DOM.parseCls(config.cls).concat(['btn-trigger']);
		config.icon = config.icon || 'fa-caret-down';
		config.iconTag = config.iconTag || 'span';
		config.text = '';
		config.disableEvents = true;

		this.callParent(config);
	}
});Z8.define('Z8.button.Tool', {
	extend: 'Z8.button.Button',

	tabIndex: -1,

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('btn-sm');
		this.callParent();
	}
});Z8.define('Z8.button.File', {
	extend: 'Z8.button.Button',

	initComponent: function() {
		this.callParent();

		this.on('click', this.processClick, this);
	},

	completeRender: function() {
		this.callParent();

		var fileInput = this.fileInput = DOM.append(this, { tag: 'input', type: 'file', multiple: true });
		DOM.on(fileInput, 'change', this.onFileInputChange, this);
	},

	onDestroy: function() {
		DOM.un(this.fileInput, 'change', this.onFileInputChange, this);

		delete this.fileInput;

		this.callParent();
	},

	processClick: function() {
		this.fileInput.value = null;
		this.fileInput.click();
	},

	onClick: function(event, target) {
		if(target != this.fileInput)
			this.callParent(event, target);
	},

	onFileInputChange: function() {
		var files = this.fileInput.files;
		if(files.length != null)
			this.fireEvent('select', this, files);
	}
});Z8.define('Z8.button.Group', {
	extend: 'Z8.Container',

	vertical: false,
	radio: false,

	constructor: function(config) {
		config = config || {};
		config.items = config.items || [];

		this.callParent(config);

		this.cls = DOM.parseCls(this.cls).pushIf('btn-group' + (this.vertical ? '-vertical' : ''));
	},

	getToggled: function() {
		return this.toggled;
	},

	onRadioToggle:function(button, toggled) {
		var items = this.items;

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item != button && item.toggled)
				item.setToggled(false, true);
		}

		this.toggled = button;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		var items = this.items;

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isComponent)
				item.setTabIndex(tabIndex);
		}
	},

	focus: function() {
		if(this.enabled) {
			var toggled = this.getToggled();
			return toggled != null && toggled.focus() ? true : this.callParent();
		}
		return false;
	}
});
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
});Z8.define('Z8.calendar.Calendar', {
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
		date.setFullYear(year != null ? year : this.currentYear());
		date.setMonth(month != null ? month : this.currentMonth());
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
});Z8.define('Z8.calendar.Dropdown', {
	extend: 'Z8.calendar.Calendar',

	cls: 'dropdown display-none',

	initComponent: function() {
		this.callParent();
		this.visible = false;
	},

	show: function(top, left) {
		if(this.visible) {
			DOM.focus(this.selectedDay);
			return;
		}

		this.setPosition(top, left);

		DOM.removeCls(this, 'display-none');
		this.align();

		this.fireEvent('show', this);

		this.visible = true;

		DOM.focus(this.selectedDay);
	},

	hide: function() {
		if(!this.visible)
			return;

		this.visible = false;

		DOM.addCls(this, 'display-none');
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
	}
});Z8.define('Z8.calendar.Period', {
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
});Z8.define('Z8.calendar.Button', {
	extend: 'Z8.button.Button',

	icon: 'fa-calendar',
	toggle: true,

	tooltip: 'Период',
	triggerTooltip: 'Выбрать период',

	period: null,
	property: null,

	htmlMarkup: function() {
		this.filterItems = [];

		this.on('toggle', this.onToggle, this);

		var periodControl = new Z8.calendar.Period({ period: this.period });
		periodControl.on('apply', this.onPeriodApply, this);
		periodControl.on('cancel', this.onPeriodCancel, this);

		var menu = this.menu = new Z8.menu.Menu({ items: [periodControl], useTAB: false });
		menu.on('itemClick', this.onMenuItemClick, this);

		this.init();

		return this.callParent();
	},

	init: function() {
		var period = this.period;
		this.setText(period.getText());
		this.setToggled(period.isActive(), true);
	},

	onPeriodApply: function(control, start, finish) {
		this.menu.hide();

		var period = this.period;
		period.setStart(start);
		period.setFinish(finish);
		period.setActive(true);

		this.init();

		this.fireEvent('period', this, period, Period.Activate);
	},

	onPeriodCancel: function(control) {
		this.menu.hide();
		this.fireEvent('period', this, this.period, Period.NoAction);
	},

	onToggle: function(button, toggled) {
		var period = this.period;
		period.setActive(toggled);
		this.fireEvent('period', this, period, toggled ? Period.Apply : Period.Clear);
	}
});Z8.define('Z8.list.Item', {
	extend: 'Z8.Component',

	/*
	* config:
	*     active: false, 
	*     icon: '',
	*     fields: [] | '',
	*     checked: false,
	*
	* private:
	*     list: false,
	*     icon: null,
	*     item: null,
	*     handlers: null
	*/

	collapsed: false,

	hidden: 0,

	initComponent: function() {
		this.callParent();

		var record = this.record; 

		if(record != null) {
			this.level = record.get('level');
			this.children = record.get('hasChildren');

			var icon = record.get('icon');
			this.icon = icon != null ? icon : this.icon;

			if(record.on != null)
				record.on('change', this.onRecordChange, this);
		}
	},

	isReadOnly: function() {
		var record = this.record;
		return record != null ? record.getLock() != RecordLock.None : false;
	},

	onRecordChange: function(record, modified) {
		var fields = this.list.getFields();
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var name = field.name;
			if(modified.hasOwnProperty(name))
				this.setText(i, this.formatText(field, record.get(name)));
		}

		if(this.list.locks)
			DOM.swapCls(this.lockIcon, this.isReadOnly(), 'fa-lock', '');
	},

	columnsMarkup: function() {
		var record = this.record;

		var columns = [];
		if(this.list.checks) {
			var check = { tag: 'i', cls: 'fa ' + (this.checked ? 'fa-check-square' : 'fa-square-o')};
			var text = { cls: 'text', cn: [check] };
			columns.push({ tag: 'td', cls: 'check column', cn: [text] });
		}

		if(this.list.locks) {
			var lock = { tag: 'i', cls: 'fa ' + (this.isReadOnly() ? 'fa-lock' : '')};
			var text = { cls: 'text', cn: [lock] };
			columns.push({ tag: 'td', cls: 'lock column', cn: [text] });
		}

		var icons = [];

		if(this.icon != null) {
			var iconCls = this.setIcon(this.icon);
			var icon = { tag: 'i', cls: iconCls.join(' '), html: String.htmlText() };
			icons.push(icon);
		}

		return columns.concat(this.fieldsMarkup(icons, record));
	},

	fieldsMarkup: function(icons, record) {
		var columns = [];

		var cls = 'column';

		if(record != null) {
			var fields = this.list.getFields();

			var hasChildren = this.hasChildren();

			if(hasChildren != null) {
				var level = this.getLevel();
				var collapserIcon = { tag: 'i', cls: (hasChildren ? 'fa fa-chevron-down ' : '') + 'icon', html: String.htmlText() };
				var collapser = { tag: 'span', cls: 'collapser tree-level-' + (hasChildren ? level : level + 1), cn: [collapserIcon] };
				icons.insert(collapser, 0);
			}

			for(var i = 0, length = fields.length; i < length; i++) {
				var field = fields[i];
				var title = null;
				var text = this.formatText(field, record.get(field.name));

				var type = field.type;

				if(String.isString(text)) {
					title = type != Type.Text ? Format.htmlEncode(text) : '';
					text = String.htmlText(text);
				}

				text = { tag: 'div', cls: 'text', cn: i == 0 ? icons.concat([text]) : [text] };

				columns.push({ tag: 'td', cls: cls + (type != null ? ' ' + type : ''), field: i, cn: [text], title: title });
			}
		} else {
			var text = String.htmlText(this.text);
			text = { tag: 'div', cls: 'text', cn: icons.concat([text]) };
			columns.push({ tag: 'td', cls: cls, cn: [text], title: this.text || '' });
		}

		return columns;
	},

	formatText: function(field, value) {
		if(field.renderer != null)
			return field.renderer.call(field, value);

		switch(field.type) {
		case Type.Date:
			return Format.date(value, field.format);
		case Type.Datetime:
			return Format.datetime(value, field.format);
		case Type.Integer:
			return Format.integer(value, field.format);
		case Type.Float:
			return Format.float(value, field.format);
		case Type.Boolean:
			return { tag: 'i', cls: value ? 'fa fa-check-square' : 'fa fa-square-o' };
		default:
			return value || '';
		}
	},

	htmlMarkup: function() {
		var cls = ['item'];

		if(!this.enabled)
			cls.push(['disabled']);

		if(this.active)
			cls.pushIf('active');

		return { tag: 'tr', id: this.getId(), cls: cls.join(' '), tabIndex: this.getTabIndex(), cn: this.columnsMarkup() };
	},

	completeRender: function() {
		this.callParent();

		this.collapser = this.selectNode('.item .collapser .icon');
		this.iconElement = this.selectNode('.item .icon');
		this.checkIcon = this.selectNode('.item>.column.check>.text>.fa');
		this.checkElement = this.selectNode('.item>.column.check');
		this.lockIcon = this.selectNode('.item>.column.lock>.text>.fa');
		this.cells = this.queryNodes('.item>.column:not(.check):not(.lock)');

		DOM.on(this, 'mouseDown', this.onMouseDown, this);
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'dblClick', this.onDblClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'mouseDown', this.onMouseDown, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'dblClick', this.onDblClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		if(this.record != null && this.record.un != null)
			this.record.un('change', this.onRecordChange, this);

		this.collapser = this.iconElement = this.checkIcon = this.checkElement = this.lockIcon = this.cells = null;

		this.callParent();
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);
		DOM.setTabIndex(this, tabIndex);
		return tabIndex;
	},

	setActive: function(active) {
		this.active = active;
		DOM.swapCls(this, active, 'active');
	},

	focus: function() {
		return this.enabled ? DOM.focus(this) : false;
	},

	toggleCheck: function() {
		var checked = !this.checked;
		this.setChecked(checked);
		this.list.onItemCheck(this, checked);
	},

	isChecked: function() {
		return this.checked;
	},

	setChecked: function(checked) {
		this.checked = checked;
		DOM.swapCls(this.checkIcon, checked, 'fa-check-square', 'fa-square-o');
	},

	setIcon: function(icon) {
		this.icon = icon;
		var cls = this.iconCls = DOM.parseCls(icon).pushIf('fa', 'fa-fw', 'icon');
		DOM.setCls(this.iconElement, this.iconCls);
		return cls;
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	getValue: function() {
		var record = this.record;
		return record != null ? record.id : this.getId();
	},

	getText: function(field) {
		var record = this.record;
		return record != null ? record.get(field) || '' : this.text;
	},

	hasChildren: function() {
		return this.children;
	},

	isRoot: function() {
		return this.level == 0;
	},

	isCollapsed: function() {
		return this.collapsed === true;
	},

	isExpanded: function() {
		return this.children && !this.collapsed;
	},

	getLevel: function() {
		return this.level;
	},

	hide: function(hide) {
		if(this.hidden == 0 && hide)
			DOM.addCls(this, 'display-none');
		else if(this.hidden == 1 && !hide)
			DOM.removeCls(this, 'display-none');

		this.hidden = Math.max(this.hidden + (hide ? 1 : -1), 0);
	},

	collapse: function(collapsed) {
		if(this.collapsed != collapsed) {
			this.collapsed = collapsed;
			DOM.rotate(this.collapser, collapsed ? -90 : 0);
			this.list.onItemCollapse(this, collapsed);
		}
	},

	setText: function(index, text) {
		var cell = this.cells[index];
		if(String.isString(text)) {
			DOM.setValue(cell.firstChild.lastChild || cell.firstChild, String.htmlText(text));
			DOM.setAttribute(cell, 'title', text);
		} else {
			DOM.setInnerHTML(cell.firstChild, DOM.markup(text));
			DOM.setAttribute(cell, 'title', '');
		}
	},

	onMouseDown: function(event, target) {
		var dom = DOM.get(this);

		if(DOM.selectNode(dom.parentNode, 'tr:focus') != dom || !this.isEnabled())
			return;

		if(target == this.collapser || this.list.checks && DOM.isParentOf(this.checkElement, target))
			return;

		var index = this.findCellIndex(target);
		if(this.startEdit(index))
			event.stopEvent();
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled())
			return;

		if(target == this.collapser)
			this.collapse(!this.collapsed);
		else if(this.list.checks && DOM.isParentOf(this.checkElement, target)) {
			this.toggleCheck();
			this.list.onItemClick(this, -1);
		} else {
			var index = this.findCellIndex(target);
			this.list.onItemClick(this, index);
		}
	},

	onDblClick: function(event, target) {
		event.stopEvent();

		if(!this.enabled)
			return;

		var index = this.findCellIndex(target);
		var field = index != -1 ? this.list.getFields()[index] : null;

		if(field == null || !DOM.isParentOf(field.editor, target))
			this.list.onItemDblClick(this, index);
	},

	onKeyDown: function(event, target) {
		var list = this.list;
		if(list.isEditing())
			return;

		var key = event.getKey();

		if(key == Event.ENTER) {
			if(this.useENTER) {
				list.onItemClick(this, -1);
				event.stopEvent();
			}
		} else if(key == Event.SPACE) {
			if(list.checks) {
				event.stopEvent();
				this.toggleCheck();
			} else {
				list.onItemClick(this, -1);
				event.stopEvent();
			}
		}
	},

	startEdit: function(index) {
		if(!this.active || index == -1)
			return false;
		return this.list.onItemStartEdit(this, index);
	},

	getCell: function(index) {
		return this.cells[index];
	},

	findCell: function(target) {
		var index = this.findCellIndex(target);
		return index != -1 ? this.cells[index] : null;
	},

	findCellIndex: function(target) {
		var cells = this.cells;

		for(var i = 0, length = cells.length; i < length; i++) {
			var cell = cells[i];
			if(DOM.isParentOf(cell, target))
				return i;
		}

		return -1;
	}
});
Z8.define('Z8.list.Divider', {
	extend: 'Z8.Component',

	cls: 'divider',
	enabled: false
});Z8.define('Z8.list.HeaderBase', {
	extend: 'Z8.Component',
	shortClassName: 'HeaderBase',

	statics: {
		Numeric: Ems.pixelsToEms(70),
		Date: Ems.pixelsToEms(70),
		Datetime: Ems.pixelsToEms(130),

		Icon: Ems.pixelsToEms(24),

		Min: Ems.pixelsToEms(110),
		Stretch: Ems.pixelsToEms(5000)
	},

	initComponent: function() {
		this.callParent();
	},

	getSort: function() {},
	setSort: function() {},
	getFilter: function() {},
	setFilter: function() {}
});
Z8.define('Z8.list.Total', {
	extend: 'Z8.Component',

	htmlMarkup: function() {
		var text = this.formatText(this.getTotal());
		text = { cls: 'text', html: text, title: text } ;
		var cls = this.cls = DOM.parseCls(this.cls).pushIf('column').pushIf(this.field.type);
		return { tag: 'td', id: this.getId(), cls: cls.join(' '), cn: [text] };
	},

	completeRender: function() {
		this.callParent();
		this.textElement = this.selectNode('.text');
	},

	onDestroy: function() {
		this.textElement = null;
		this.callParent();
	},

	getTotal: function() {
		var totals = this.list.getStore().getTotals();
		return totals != null ? totals.get(this.field.name) : null;
	},

	getText: function(field) {
		return this.text;
	},

	setText: function(text) {
		this.text = text;
		var text = String.htmlText(this.formatText(text));
		DOM.setValue(this.textElement, text);
		DOM.setTitle(this.textElement, text);
	},

	formatText: function(value) {
		var field = this.field;

		if(field.renderer != null)
			return field.renderer.call(field, value);

		switch(field.type) {
		case Type.Date:
			return Format.date(value, field.format);
		case Type.Datetime:
			return Format.datetime(value, field.format);
		case Type.Integer:
			return Format.integer(value, field.format);
		case Type.Float:
			return Format.float(value, field.format);
		default:
			return '';
		}
	}
});
Z8.define('Z8.list.Header', {
	extend: 'Z8.list.HeaderBase',

	/*
	* config:
	* icon: '',
	* field: {},
	*
	*/

	initComponent: function() {
		this.callParent();

		var field = this.field;
		this.text = field.header;
		this.icon = field.icon;
		this.width = field != null ? field.width : null;
		this.sortDirection = field.sortDirection;
	},

	getWidth: function() {
		var defaultWidth = this.getDefaultWidth();
		var width = this.width;
		return width != null && width != 0 ? Math.max(width, this.getMinWidth()) : defaultWidth;
	},

	getMinWidth: function() {
		var field = this.field;
		var type = field.type;
		var format = field.format;

		switch(type) {
		case Type.Integer:
		case Type.Float:
			return HeaderBase.Numeric;
		case Type.Date:
			return Format.measureDate(format || Format.Date) + 1;
		case Type.Datetime:
			return Format.measureDate(format || Format.Datetime) + 1;
		default:
			return HeaderBase.Min;
		}
	},

	getDefaultWidth: function() {
		switch(this.field.type) {
		case Type.Integer:
		case Type.Float:
		case Type.Date:
		case Type.Datetime:
			return this.getMinWidth();
		default:
			return HeaderBase.Stretch;
		}
	},

	setWidth: function(width) {
		return this.width = width;
	},

	htmlMarkup: function() {
		if(this.icon != null) {
			var cls = this.setIcon(this.icon);
			var icon = { tag: 'i', cls: cls.join(' '), html: String.htmlText() };
		}

		cls = this.setSort(this.sortDirection);
		var sort = { tag: 'i', cls: cls.join(' '), html: String.htmlText() };

		cls = this.setFilter(false);
		var filter = { tag: 'i', cls: cls.join(' '), html: String.htmlText() };

		var text = String.htmlText(this.text);
		text = { cls: 'text', title: text, cn: icon != null ? [icon, text] : [text] } ;

		var cls = this.getCls().join(' ');

		var leftHandle = { cls: 'resize-handle-left' };
		var rightHandle = { cls: 'resize-handle-right' };
		return { tag: 'td', id: this.getId(), cls: cls, tabIndex: this.getTabIndex(), cn: [leftHandle, text, sort, filter, rightHandle], title: this.text };
	},

	completeRender: function() {
		this.callParent();

		this.textElement = this.selectNode('.text');
		this.iconElement = this.selectNode('.icon');
		this.sortElement = this.selectNode('.sort');
		this.filterElement = this.selectNode('.filter');

		this.resizeHandleLeft = this.selectNode('.resize-handle-left');
		this.resizeHandleRight = this.selectNode('.resize-handle-right');

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'mouseDown', this.onMouseDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'mouseDown', this.onMouseDown, this);

		this.textElement = this.iconElement = this.sortElement = this.filterElement = null;
		this.resizeHandleLeft = this.resizeHandleRight = null;

		this.callParent();
	},

	getCls: function() {
		var cls = this.cls = DOM.parseCls(this.cls).pushIf('column');
		if(this.sortDirection != null)
			cls.pushIf('sort');
		if(this.filtered)
			cls.pushIf('filter');
		return cls;
	},

	setBusy: function(busy) {
	},

	setIcon: function(icon) {
		this.icon = icon;
		var cls = this.iconCls = DOM.parseCls(icon).pushIf('fa', 'fa-fw', 'icon');
		DOM.setCls(this.iconElement, this.iconCls);
		return cls;
	},

	getSortIcon: function() {
		var direction = this.sortDirection;

		if(direction == null)
			return 'display-none';

		var icon = 'fa-sort-';

		switch(this.field.type) {
		case Type.Date:
		case Type.Datetime:
		case Type.Integer:
		case Type.Float:
			icon += 'numeric-';
			break;
		default:
			icon += 'alpha-';
		}
		return icon + direction;
	},

	getSort: function() {
		return this.sortDirection;
	},

	setSort: function(direction) {
		this.sortDirection = direction;
		var cls = this.sortCls = DOM.parseCls(this.getSortIcon()).pushIf('fa', 'fa-fw', 'sort');
		DOM.setCls(this.sortElement, cls);
		DOM.swapCls(this, direction != null, 'sort');
		return cls;
	},

	getFilterIcon: function() {
		return this.filtered ? 'fa-filter' : 'display-none';
	},

	setFilter: function(filtered) {
		this.filtered = filtered;
		var cls = this.filterCls = DOM.parseCls(this.getFilterIcon()).pushIf('fa', 'fa-fw', 'filter');
		DOM.setCls(this.filterElement, cls);
		DOM.swapCls(this, filtered, 'filter');
		return cls; 
	},

	getText: function(field) {
		return this.text;
	},

	setText: function(index, text) {
		this.text = text;
		text = String.htmlText(text);
		DOM.setValue(this.textElement, text);
		DOM.setTitle(this.textElement, text);
	},

	onMouseDown: function(event, target) {
		if(target == this.resizeHandleLeft || target == this.resizeHandleRight) {
			event.stopEvent();

			this.lastPageX = event.pageX;
			this.currentHandle = target;

			var resizer = this.resizer = DOM.append(document.body, { cls: 'column-resizer-mask' });

			DOM.on(resizer, 'mouseMove', this.onMouseMove, this);
			DOM.on(resizer, 'mouseUp', this.onMouseUp, this);
		}
	},

	onMouseMove: function(event, target) {
		event.stopEvent();

		var change = Ems.pixelsToEms(event.pageX - this.lastPageX);
		this.list.onHeaderResize(this, change, this.currentHandle == this.resizeHandleLeft ? 'left' : 'right');
		this.lastPageX = event.pageX;
	},

	onMouseUp: function(event, target) {
		var resizer = this.resizer;
		DOM.un(resizer, 'mouseMove', this.onMouseMove, this);
		DOM.un(resizer, 'mouseUp', this.onMouseUp, this);
		DOM.remove(this.resizer);
		this.resizer = null;

		this.list.onHeaderResized(this);
	},

	onClick: function(event, target) {
		event.stopEvent();
		this.list.onHeaderSort(this);
	}
});
Z8.define('Z8.list.HeaderIcon', {
	extend: 'Z8.list.HeaderBase',

	fixed: true,

	initComponent: function() {
		this.callParent();
		var field = this.field;
		this.icon = field != null ? field.icon : this.icon;
		this.title = (field != null ? field.header : this.title) || '';
	},

	getWidth: function() {
		return this.width || HeaderBase.Icon;
	},

	getMinWidth: function() {
		return this.getWidth();
	},

	htmlMarkup: function() {
		if(this.icon != null) {
			var cls = DOM.parseCls(this.icon).pushIf('fa').join(' ');
			var icon = { tag: 'i', cls: cls };
		} else
			var icon = { tag: 'span', html: this.title.substr(0, 2) };

		cls = DOM.parseCls(this.cls).pushIf('column', 'icon').join(' ');
		return { tag: 'td', id: this.getId(), cls: cls, tabIndex: this.getTabIndex(), cn: [icon], title: this.title };
	},

	completeRender: function() {
		this.callParent();
		this.icon = this.selectNode('.column>.fa');
	},

	onDestroy: function() {
		this.icon = null;
		this.callParent();
	}
});
Z8.define('Z8.list.HeaderFilter', {
	extend: 'Z8.Component',

	/*
	* config:
	* field: {},
	*
	* private:
	*
	*/

	initComponent: function() {
		this.callParent();
	},

	subcomponents: function() {
		return [this.searchBox];
	},

	htmlMarkup: function() {
		var field = this.field || {};

		var searchBox = null;

		switch(field.type) {
		case Type.String:
		case Type.Text:
			searchBox = new Z8.form.field.Search({ field: field, placeholder: field.header });
			searchBox.on('search', this.onSearch, this);
			searchBox.on('focusIn', this.onFocusIn, this);
			searchBox.on('focusOut', this.onFocusOut, this);
			break;
		}

		this.searchBox = searchBox;

		var cls = DOM.parseCls(this.cls).pushIf('column').join(' ');
		return { tag: 'td', id: this.getId(), cls: cls, tabIndex: this.getTabIndex(), cn: searchBox != null ? [searchBox.htmlMarkup()] : [] };
	},

	focus: function() {
		return this.searchBox != null ? this.searchBox.focus() : false;
	},

	reset: function() {
		var searchBox = this.searchBox;
		if(searchBox != null)
			searchBox.reset();
	},

	onSearch: function(control, value) {
		this.list.onHeaderFilter(this, value);
	},

	getFilter: function() {
		var searchBox = this.searchBox;
		return searchBox == null ? null : searchBox.getFilter();
	},

	onFocusIn: function(search) {
		this.fireEvent('focusIn', this);
	},

	onFocusOut: function(serach) {
		this.fireEvent('focusOut', this);
	}
});
Z8.define('Z8.list.HeaderCheck', {
	extend: 'Z8.list.HeaderIcon',

	cls: 'check',

	isChecked: function() {
		return this.checked;
	},

	setChecked: function(checked) {
		this.checked = checked;
		DOM.swapCls(this.icon, checked, 'fa-check-square', 'fa-square-o');
		return checked;
	},

	toggleCheck: function() {
		return this.setChecked(!this.checked);
	},

	htmlMarkup: function() {
		this.icon = this.checked ? 'fa-check-square' : 'fa-square-o';
		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'click', this.onClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		this.callParent();
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(this.enabled)
			this.list.onCheckHeaderClick(this);
	}
});
Z8.define('Z8.list.List', {
	extend: 'Z8.Component',

	checks: true,
	autoFit: true,

	headers: null,
	items: null,
	totals: false,

	headerType: null, // default 'Z8.list.Header',
	itemType: null,   // default 'Z8.list.Item',

	store: null,
	name: 'id',
	fields: 'name',

	visible: true,

	editable: false,

	useTAB: false,
	useENTER: true,

	confirmSelection: false,
	autoSelectFirst: true,

	itemsRendered: false,
	manualItemsRendering: false,

	ordinals: null,
	filterVisible: false,
	lastEditedColumn: null,

	tabIndex: 0,

	constructor: function(config) {
		this.callParent(config);
	},

	initComponent: function() {
		this.callParent();

		var items = this.items = this.items || [];
		this.fragments = [];

		this.fields = this.createFields(this.fields || 'name');
		this.headers = this.createHeaders();
		this.filters = this.createQuickFilters();
		this.totals = this.createTotals();

		this.initItems();
		this.initStore();
		this.editors = this.initEditors();
	},

	initItems: function() {
		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++)
			items[i].list = this;
	},

	initStore: function() {
		var store = this.store;
		this.store = null;
		this.setStore(store);
	},

	setStore: function(store) {
		if(this.store == store)
			return;

		if(this.store != null) {
			this.store.un('load', this.onStoreLoad, this);
			this.store.un('add', this.onStoreAdd, this);
			this.store.un('remove', this.onStoreRemove, this);
			this.store.un('totals', this.onStoreTotals, this);
			this.store.un('idChange', this.onStoreIdChange, this);
			this.store.dispose();
		}

		this.store = store;

		if(store == null)
			return;

		store.use();

		store.on('load', this.onStoreLoad, this);
		store.on('add', this.onStoreAdd, this);
		store.on('remove', this.onStoreRemove, this);
		store.on('totals', this.onStoreTotals, this);
		store.on('idChange', this.onStoreIdChange, this);

		if(store.isLoaded())
			this.setRecords(store.getRecords());
	},

	onStoreLoad: function(store, records, success) {
		if(success) {
			this.setRecords(records);
			this.updateSort();
			this.updateFilter();
			this.updateChecks();
		}
	},

	onStoreAdd: function(store, records, index) {
		this.addRecords(records, index);
	},

	onStoreRemove: function(store, records) {
		this.removeRecords(records);
	},

	onStoreTotals: function(store, totals, success) {
		if(success)
			this.setTotals(totals);
	},

	onStoreIdChange: function(store, record, oldId) {
		if(this.getValue() == oldId)
			this.setValue(record.id);
		this.resetOrdinals();
	},

	initEditors: function() {
		var editors = [];

		if(this.store == null)
			return editors;

		var fields = this.fields;

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var type = field.type;

			if(!field.editable || type == null || type == Type.Boolean)
				continue;

			var config = Z8.apply({}, field);
			config.label = false;
			config.type = type != Type.Text ? type : Type.String;
			config.enterOnce = true;
			config.height = null;

			var editor = field.editor;

			if(editor == null)
				editor = field.editor = Z8.form.Helper.createControl(config);

			editor.on('change', this.onItemEditorChange, this);
			editor.index = i;
			editors.push(editor);
		}

		return editors;
	},

	setEditable: function(editable) {
		this.editable = editable;
	},

	headersMarkup: function() {
		var headerCells = [];
		var filterCells = [];

		var headers = this.headers;
		var filters = this.filters;
		var hasFilters = this.hasQuickFilters();

		for(var i = 0, length = headers.length; i < length; i++) {
			headerCells.push(headers[i].htmlMarkup());
			if(hasFilters)
				filterCells.push(filters[i].htmlMarkup());
		}

		headerCells.push({ tag: 'td', cls: 'extra', style: 'width: 0;' });

		var result = [{ tag: 'tr', cls: 'header', cn: headerCells }];

		if(hasFilters)
			result.push({ tag: 'tr', cls: 'filter display-none', cn: filterCells });

		return result;
	},

	itemsMarkup: function(items) {
		var markup = [];

		var items = items || this.items;
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			item = item == '-' ? new Z8.list.Divider() : item;
			markup.push(item.htmlMarkup());
		}

		this.itemsRendered = true;

		return markup;
	},

	totalsMarkup: function() {
		var cells = [];

		var totals = this.totals;

		for(var i = 0, length = totals.length; i < length; i++)
			cells.push(totals[i].htmlMarkup());

		cells.push({ tag: 'td', cls: 'extra', style: 'width: 0;' });

		return [{ tag: 'tr', cls: 'total', cn: cells }];
	},

	tableMarkup: function(type) {
		var hasHeaders = this.headers.length != 0;

		var width = hasHeaders ? this.getTotalWidth() : 0;
		var style = width != 0 ? 'width: ' + width + 'em;' : '';
		var colGroup = hasHeaders ? { tag: 'colGroup', cn: this.colGroupMarkup(type) } : null;

		var rows = [];
		if(type == 'headers')
			rows = this.headersMarkup();
		else if(type == 'totals')
			rows = this.totalsMarkup();
		else if(type == 'items' && !this.manualItemsRendering)
			rows = this.itemsMarkup();

		var tbody = { tag: 'tbody', cn: rows };
		return { tag: 'table', style: style, cn: colGroup != null ? [colGroup, tbody] : [tbody] };
	},

	colGroupMarkup: function(type) {
		var headers = this.headers;

		var markup = [];

		for(var i = 0, length = headers.length; i < length; i++)
			markup.push({ tag: 'col', style: 'width: ' + headers[i].getWidth() + 'em;' });

		if(type != 'items')
			markup.push({ tag: 'col', cls: 'extra', style: 'width: 0;' });

		return markup;
	},

	htmlMarkup: function() {
		var hasHeaders = this.headers.length != 0;
		var hasTotals = this.totals.length != 0;

		var cls = DOM.parseCls(this.cls).pushIf('list');
		if(hasHeaders)
			cls.pushIf('columns');

		if(hasTotals)
			cls.pushIf('totals');

		if(this.autoFit)
			cls.pushIf('auto-fit');

		var cn = [];

		if(hasHeaders) {
			var headers = this.tableMarkup('headers');
			var headersScroller = { cls: 'scroller headers', cn: [headers] };
			cn.push(headersScroller);
		}

		var items = this.tableMarkup('items');
		var itemsScroller = { cls: 'scroller items', cn: [items] };
		cn.push(itemsScroller);

		if(hasTotals) {
			var totals = this.tableMarkup('totals');
			var totalsScroller = { cls: 'scroller totals', cn: [totals] };
			cn.push(totalsScroller);
		}

		return { id: this.getId(), tabIndex: this.getTabIndex(), cls: cls.join(' '), cn: cn };
	},

	subcomponents: function() {
		return this.headers.concat(this.filters).concat(this.itemsRendered ? this.items : []).concat(this.totals);
	},

	completeRender: function() {
		this.callParent();

		if(this.itemsTable == null) {
			var itemsScroller = this.itemsScroller = this.selectNode('.scroller.items');
			var headersScroller = this.headersScroller = this.selectNode('.scroller.headers');
			var totalsScroller = this.totalsScroller = this.selectNode('.scroller.totals');

			this.headersTable = this.selectNode('.scroller.headers>table');
			this.headerCols = this.queryNodes('.scroller.headers>table>colgroup>col') || [];
			this.filter = this.selectNode('.scroller.headers tr.filter');

			this.totalsTable = this.selectNode('.scroller.totals>table');
			this.totalCols = this.queryNodes('.scroller.totals>table>colgroup>col') || [];

			this.itemsTable = this.selectNode('.scroller.items>table');
			this.itemsTableBody = this.selectNode('.scroller.items>table>tbody');
			this.itemCols = this.queryNodes('.scroller.items>table>colgroup>col');

			DOM.on(this, 'keyDown', this.onKeyDown, this);

			if(headersScroller != null) {
				DOM.on(window, 'resize', this.onResize, this);
				DOM.on(totalsScroller || itemsScroller, 'scroll', this.onScroll, this);
			}
		}
	},

	afterRender: function() {
		this.callParent();

		if(!this.manualItemsRendering) {
			var item = this.currentItem() || (this.autoSelectFirst ? 0 : null);
			this.selectItem(item, this.autoSelectFirst);
		}

		this.adjustAutoFit();
	},

	getTotalWidth: function() {
		var width = 0;

		var headers = this.headers;
		for(var i = 0, length = headers.length; i < length; i++)
			width += headers[i].getWidth();

		return width;
	},

	setAutoFit: function(autoFit, silent) {
		this.autoFit = autoFit;
		DOM.swapCls(this, autoFit, 'auto-fit');
		this.adjustAutoFit();

		if(!this.silent)
			this.fireEvent('autoFit', this, autoFit);
	},

	hasVScrollbar: function() {
		return this.itemsScroller.clientHeight < this.itemsTable.clientHeight;
	},

	hasHScrollbar: function() {
		return this.itemsScroller.clientWidth < this.itemsTable.clientWidth;
	},

	getScrollbarWidth: function() {
		var scroller = this.itemsScroller;
		var scrollbarWidth = new Rect(scroller).width - DOM.getClientWidth(scroller);
		return Math.max(scrollbarWidth, 0);
	},

	adjustAutoFit: function() {
		var headers = this.headers;
		var itemsScroller = this.itemsScroller;

		if(this.headers.length == 0 || itemsScroller == null)
			return;

		if(!this.autoFit) {
			this.adjustScrollers()
			return;
		}

		var fixedWidth = 0;
		var flexibleWidth = 0;
		var totalWidth = 0;

		var headerCols = this.headerCols;
		var totalCols = this.totalCols;
		var itemCols = this.itemCols;

		var flexible = [];

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var width = header.getWidth();
			totalWidth += width;
			if(!header.fixed) {
				flexibleWidth += width;
				flexible.push(i);
			} else
				fixedWidth += width;
		}

		var clientWidth = new Rect(itemsScroller).width - this.getScrollbarWidth();

		if(clientWidth - fixedWidth <= 0 || totalWidth == fixedWidth)
			return;

		do {
			var continueAdjusting = false;
			var ratio = (clientWidth - fixedWidth) / flexibleWidth;
			flexibleWidth = 0;
			for(var i = 0, length = flexible.length; i < length; i++) {
				var index = flexible[i];
				var header = headers[index];
				var width = header.getWidth();
				var minWidth = header.getMinWidth();

				if(width == minWidth && header.adjusted)
					continue;

				width = Math.max(minWidth, (i != flexible.length - 1) ? header.getWidth() * ratio : (clientWidth - fixedWidth - flexibleWidth));

				if(width == minWidth) {
					continueAdjusting = true;
					fixedWidth += width;
				} else
					flexibleWidth += width;

				header.adjusted = true;

				header.setWidth(width);
			}
		} while(continueAdjusting);

		for(var i = 0, length = flexible.length; i < length; i++) {
			var index = flexible[i];
			var header = headers[index];
			var width = header.getWidth();
			DOM.setPoint(headerCols[index], 'width',  width);
			DOM.setPoint(itemCols[index], 'width',  width);
			DOM.setPoint(totalCols[index], 'width',  width);

			header.adjusted = false;
		}

		var width = fixedWidth + flexibleWidth;
		DOM.setPoint(this.itemsTable, 'width',  width);

		this.adjustScrollers(width);
	},

	adjustScrollers: function(width) {
		var headers = this.headers;
		var itemsScroller = this.itemsScroller;

		if(headers.length == 0 || itemsScroller == null)
			return;

		var headerCols = this.headerCols;
		var totalCols = this.totalCols;

		width = width || Ems.pixelsToEms(parseFloat(DOM.getComputedStyle(this.itemsTable).width));
		var scrollbarWidth = this.getScrollbarWidth();

		DOM.setPoint(headerCols[headerCols.length - 1], 'width',  scrollbarWidth);
		DOM.setPoint(totalCols[totalCols.length - 1], 'width',  scrollbarWidth);

		DOM.setPoint(this.headersTable, 'width',  width + scrollbarWidth);
		DOM.setPoint(this.totalsTable, 'width',  width + scrollbarWidth);

		this.onScroll();
	},

	renderItems: function() {
		if(this.itemsRendered && !this.itemsAdded || this.getDom() == null)
			return false;

		var items = this.getItems();

		if(!this.itemsRendered) {
			DOM.append(this.itemsTableBody, this.itemsMarkup());
			this.renderDone();
			return true;
		} 

		if(!this.itemsAdded)
			throw 'List: invalid render state';

		var fragments = this.fragments;

		for(var i = 0, length = fragments.length; i < length; i++) {
			var fragment = fragments[i];
			var before = DOM.get(this.itemsTableBody).childNodes[fragment.index];
			if(before != null)
				DOM.insertBefore(before, this.itemsMarkup(fragment.items));
			else
				DOM.append(this.itemsTableBody, this.itemsMarkup(fragment.items));
		}

		this.fragments = [];
		this.itemsAdded = false;

		this.renderDone();
		return true;
	},

	onDestroy: function() {
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		DOM.un(this.totalsScroller || this.itemsScroller, 'scroll', this.onScroll, this);
		DOM.un(window, 'resize', this.onResize, this);

		this.setStore(null);

		Component.destroy(this.editors);

		this.callParent();

		this.itemsScroller = this.headersScroller = this.totalsScroller = null;
		this.headersTable = this.headerCols = this.filter = this.totalsTable = null;
		this.totalCols = this.itemsTable = this.itemsTableBody = this.itemCols = null;
		this.items = this.fields = this.fragment = this.headers = this.checkHeader = null;
		this.filters = this.totals = this.editors = null;
	},

	getStore: function() {
		return this.store;
	},

	isLoaded: function() {
		return this.store != null ? this.store.isLoaded() : (this.getCount() != 0);
	},

	load: function(callback, filter) {
		var store = this.store;

		if(store == null) {
			this.setItems(this.getItems());
			return;
		}

		filter == null ? store.load(callback) : store.filter(filter, callback);
	},

	clearFilter: function() {
		var store = this.getStore();
		store.setFilter([]);
		store.unload();
	},

	createHeader: function(field, cls) {
		var type = this.headerType || (field.type != Type.Boolean ? 'Z8.list.Header' : 'Z8.list.HeaderIcon');
		return Z8.create(type, { list: this, field: field, cls: cls });
	},

	createTotal: function(field, cls) {
		var type = this.totalType || 'Z8.list.Total';
		return Z8.create(type, { list: this, field: field, cls: cls });
	},

	hasQuickFilters: function() {
		return this.filters.length != 0;
	},

	createQuickFilter: function(field, cls) {
		var filter = new Z8.list.HeaderFilter({ list: this, field: field, cls: cls });
		filter.on('focusIn', this.onQuickFilterFocusIn, this);
		filter.on('focusOut', this.onQuickFilterFocusOut, this);
		return filter;
	},

	createHeaders: function() {
		var headers = [];
		var fields = this.fields;

		if(fields.length == 0 || this.headers == false)
			return headers;

		if(this.checks) {
			var header = this.checkHeader = new Z8.list.HeaderCheck({ list: this });
			headers.push(header);
		}

		if(this.locks) {
			header = new Z8.list.HeaderIcon({ list: this, cls: 'lock', icon: 'fa-lock' });
			headers.push(header);
		}

		var store = this.store;
		var sorter = store != null && store.getSorter != null ? store.getSorter() : [];
		sorter = sorter.length != 0 ? sorter[0] : null;

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var isSortHeader = sorter != null && field.name == sorter.property && field.sortable !== false;
			field.sortDirection = isSortHeader ? sorter.direction : null;
			var header = this.createHeader(fields[i], cls);
			if(isSortHeader)
				this.sortHeader = header;
			headers.push(header);
		}

		return headers;
	},

	createTotals: function() {
		var totals = [];

		if(!this.totals)
			return totals;

		if(this.checks) {
			var total = new Z8.list.HeaderIcon({ list: this, title: 'Σ' });
			totals.push(total);
		}

		if(this.locks) {
			var total = new Z8.list.HeaderIcon({ list: this, title: this.checks ? '' : 'Σ' });
			totals.push(total);
		}

		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var total = this.createTotal(fields[i], cls);
			totals.push(total);
		}

		return totals;
	},

	createQuickFilters: function() {
		var filters = [];
		var headers = this.headers;

		if(headers.length == 0 || this.filters === false)
			return filters;

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			var cls = i != 0 ? (i == length - 1 ? 'last': null) : 'first';
			var filter = this.createQuickFilter(field, cls);
			filters.push(filter);
		}

		return filters;
	},

	createItem: function(record) {
		var config = { list: this, record: record, name: this.name || 'id', icon: this.icons ? '' : null, useENTER: this.useENTER };
		return this.itemType != null ? Z8.create(this.itemType, config) : new  Z8.list.Item(config); 
	},

	createItems: function(records) {
		var items = [];

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			var item = this.createItem(record); 
			items.push(item);
		}

		return items;
	},

	createFields: function(fields) {
		if(fields == null)
			return [];

		if(String.isString(fields))
			return [{ name: fields, header: fields }];

		if(Array.isArray(fields)) {
			var result = [];
			for(var i = 0, length = fields.length; i < length; i++) {
				var field = fields[i];
				result.push(String.isString(field) ? { name: field } : field);
			}
			return result;
		}

		return [fields];
	},

	setFields: function(fields) {
/*
		this.fields = this.createFields(fields);
		this.setRecords();
*/
	},

	setRecords: function(records) {
		var items = this.createItems(records || this.store.getRecords());

		this.setItems(items);

		if(this.visible)
			this.renderItems();
	},

	addRecords: function(records, index) {
		var items = this.createItems(records || this.store.geRecords());
		this.addItems(items, index, true);

		if(this.visible)
			this.renderItems();
	},

	removeRecords: function(records) {
		var items = [];
		for(i = 0, length = records.length; i < length; i++)
			items.push(this.getItem(records[i].id));

		this.removeItems(items);
	},

	setTotals: function(record) {
		var totals = this.totals;

		if(totals.length == 0)
			return;

		var start = 0 + (this.checks ? 1 : 0) + (this.locks ? 1 : 0);

		for(var i = start, length = totals.length; i < length; i++) {
			var total = totals[i];
			totals[i].setText(record.get(total.field.name));
		}
	},

	getItems: function() {
		return this.items || [];
	},

	getCount: function() {
		return this.getItems().length;
	},

	getAt: function(index) {
		var items = this.getItems();
		return index < items.length ? items[index] : null;
	},

	getItem: function(value) {
		var index = this.getIndex(value); 
		return index != -1 ? this.getAt(index) : null;
	},

	getIndex: function(value) {
		var ordinals = this.getOrdinals();

		if(value instanceof Z8.list.Item) {
			var index = ordinals[value.getValue()];
			return index != null ? index : -1;
		}

		index = Number.isNumber(value) ? value : ordinals[value];
		return index != null ? index : -1;
	},

	getOrdinals: function() {
		if(this.ordinals != null)
			return this.ordinals;

		var ordinals = this.ordinals = {};

		var items = this.items;

		if(items == null)
			return ordinals;

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isComponent) {
				var value = item.getValue != null ? item.getValue() : null;
				if(value != null)
					ordinals[value] = i;
				ordinals[item.getId()] = i;
			}
		}

		return ordinals;
	},

	resetOrdinals: function() {
		this.ordinals = null;
	},

	getFields: function() {
		return this.fields;
	},

	getHeaders: function() {
		return this.headers;
	},

	getHeader: function(name) {
		var headers = this.headers;
		var start = 0 + (this.checks ? 1 : 0) + (this.locks ? 1 : 0);
		for(var i = start, length = headers.length; i < length; i++) {
			var header = headers[i];
			if(header.field.name == name)
				return header;
		}
		return null;
	},

	getHeaderIndex: function(header) {
		var headers = this.headers;
		for(var i = 0, length = headers.length; i < length; i++) {
			if(header == headers[i])
				return i;
		}
		return -1;
	},

	getValue: function() {
		return this.value;
	},

	setValue: function(value) {
		this.value = value;
	},

	addItems: function(items, index, attached) {
		if(!attached) {
			for(var i = 0, length = items.length; i < length; i++)
				items[i].list = this;
		}

		this.items.insert(items, index);
		this.resetOrdinals();

		if(this.itemsRendered) {
			this.fragments.push({ index: index, items: items });
			this.itemsAdded = true;
		}

		this.onContentChange();
	},

	removeItems: function(items) {
		this.resetOrdinals();

		Component.destroy(items);

		this.items.removeAll(items);

		this.adjustAutoFit();

		this.onContentChange();
	},

	setItems: function(items) {
		if(this.items == items)
			return;

		this.clear();
		this.items = items;

		this.resetOrdinals();

		this.onContentChange();
	},

	onContentChange: function() {
		this.fireEvent('contentChange', this);
		this.setTabIndex(this.getTabIndex());
	},
	
	clear: function() {
		Component.destroy(this.items);
		this.items = this.fragments = [];
		this.itemsRendered = this.itemsAdded = false;
	},

	findItem: function(node) {
		var index = this.findItemIndex(node);
		return index != -1 ? this.getItem(index) : null;
	},

	findItemIndex: function(node) {
		var topmost = document.body;

		while(node != null && node.nodeType == 1 && node !== topmost) {
			var index = this.getIndex(node.id);
			if(index != -1)
				return index;
			node = node.parentNode;
		}

		return -1;
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		var item = this.currentItem();

		if(item != null)
			item.setTabIndex(tabIndex);

		if(DOM.get(this) == null)
			this.tabIndex = this.getCount() == 0 ? tabIndex : -1;
		else
			DOM.setTabIndex(this, this.getCount() == 0 ? tabIndex : -1);

		return tabIndex;
	},

	/*
	* index: item, index; default: -1
	* direction: 'first', 'last', 'next', 'previous'; default: 'next'
	* next: boolean - focus at next menuItem from index; default: false
	*/
	focus: function(index, direction, next) {
		if(!this.enabled)
			return false;

		if(this.getCount() == 0)
			return DOM.focus(this);

		var items = this.getItems();

		index = index == null ? this.currentItem() : index;

		if(index instanceof Z8.list.Item)
			index = this.getIndex(index);

		if(String.isString(index)) {
			direction = index;
			index = this.getIndex(this.getValue());
		}

		index = index != null ? index : -1;
		direction = direction || 'next';

		var step = 0;
		var count = items.length;

		do {
			if(direction == 'first') {
				index = 0;
				direction = 'next';
			} else if(direction == 'last') {
				index = count - 1;
				direction = 'previous';
			}

			if(step == 0 && next || step != 0) {
				if(direction == 'next')
					index = index != count - 1 ? index + 1 : 0;
				else if(direction == 'previous')
					index = index != 0 ? index - 1 : (count - 1);
			}

			index = Math.min(Math.max(index, 0), count - 1);

			if(index == -1)
				return false;

			var item = items[index];
			if(!String.isString(item) && !item.hidden && this.focusItem(item))
				return true;
		} while(++step < count);

		return DOM.focus(this);
	},

	focusItem: function(item) {
		if(item.focus != null && item.focus()) {
			this.selectItem(item);
			return true;
		}
		return false;
	},

	currentItem: function() {
		return this.getItem(this.getValue());
	},

	getChecked: function(records) {
		var checked = [];
		var items = this.getItems();
		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item.isChecked())
				checked.push(records ? item.record : item);
		}
		return checked;
	},

	getCheckedRecords: function() {
		return this.getChecked(true);
	},

	activateItem: function(item, active) {
		item.setActive(active);
		item.setTabIndex(active ? 0 : -1);
	},

	selectItem: function(item, forceEvent) {
		var currentItem = this.currentItem();

		var wasFocused = false;
		if(currentItem != item && currentItem != null) {
			wasFocused = this.selectNode(':focus') == DOM.get(currentItem);
			this.activateItem(currentItem, false);
		}

		if(Number.isNumber(item))
			item = this.getAt(item);

		if(item != null) {
			this.activateItem(item, true);
			if(wasFocused)
				item.focus();
		}

		this.setValue(item != null && item.getValue != null ? item.getValue() : null);

		if(forceEvent || !this.confirmSelection && item != currentItem)
			this.fireEvent('select', item, this);
	},

	setSelection: function(item) {
		this.selectItem(item, true);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(this.isEditing() || this.isFiltering() && key != Event.ESC)
			return;

		if(key == Event.DOWN || key == Event.TAB && !event.shiftKey && this.useTAB) {
			if(this.focus(this.findItemIndex(target), 'next', true))
				event.stopEvent();
		} else if(key == Event.UP || key == Event.TAB && event.shiftKey && this.useTAB) {
			if(this.focus(this.findItemIndex(target), 'previous', true))
				event.stopEvent();
		} else if(key == Event.HOME) {
			if(this.focus('first'))
				event.stopEvent();
		} else if(key == Event.END) {
			if(this.focus('last'))
				event.stopEvent();
		} else if(key == Event.F && event.ctrlKey && this.filter != null) {
			this.showQuickFilter(true);
			event.stopEvent();
		} else if(key == Event.ESC && this.filterVisible) {
			this.showQuickFilter(false);
			this.focus(this.currentItem());
			event.stopEvent();
		} else if(key == Event.LEFT) {
			var item = this.findItem(target);
			if(item == null)
				return;

			if(item.isExpanded()) {
				if(item.hasChildren()) {
					item.collapse(true);
					event.stopEvent();
				}
			} else if(!item.isRoot()) {
				item = this.getParent(item);
				if(item != null)
					this.focus(item);
			}
		} else if(key == Event.RIGHT) {
			var item = this.findItem(target);
			if(item != null && item.hasChildren() && item.isCollapsed()) {
				item.collapse(false);
				event.stopEvent();
			}
		}
	},

	onScroll: function(event, target) {
		var scroller = this.totalsScroller || this.itemsScroller;
		this.headersScroller.scrollLeft = scroller.scrollLeft;
		this.itemsScroller.scrollLeft = scroller.scrollLeft;
	},

	onResize: function(event, target) {
		this.adjustAutoFit();
	},

	onHeaderSort: function(header) {
		if(header.field.sortable == false)
			return;

		var direction = header.getSort() == 'asc' ?  'desc' : 'asc';
		var sorter = { property: header.field.name, direction: direction }

		var callback = function(store, records, success) {
			header.setBusy(false);

			if(this.sortHeader != null)
				this.sortHeader.setSort(null);

			header.setSort(direction);

			this.sortHeader = header;
		};

		header.setBusy(true);
		this.store.sort(sorter, { fn: callback,scope: this });
	},

	updateSort: function() {
		var sorter = this.store != null ? this.store.getSorter() : null;
		sorter = sorter.length != 0 ? sorter[0] : null;

		var header = this.sortHeader;
		if(sorter == null && header == null)
			return;

		if(header != null && sorter != null && header.field.name == sorter.property && header.field.sortable !== false) {
			header.setSort(sorter.direction);
			return;
		}

		if(header != null) {
			header.setSort(null);
			this.sortHeader = null;
		}

		if(sorter != null) {
			header = this.getHeader(sorter.property);
			if(header != null) {
				header.setSort(sorter.direction);
				this.sortHeader = header;
			}
		}
	},

	updateFilter: function() {
		var quickFilter = this.store != null ? this.store.getQuickFilter() : null;

		var filters = {};
		for(var i = 0, length = quickFilter.length; i < length; i++) {
			var filter = quickFilter[i];
			filters[filter.property] = true;
		}

		var headers = this.headers;
		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			if(field != null)
				header.setFilter(filters[field.name]);
		}
	},

	onHeaderFilter: function(header, value) {
		var qiuckFilter = [];
		var filters = this.filters;

		for(var i = 0, length = filters.length; i < length; i++) {
			var filter = filters[i].getFilter();
			if(filter != null)
				qiuckFilter.push(filter);
		}

		var search = header.searchBox;
		var callback = function(store, records, success) {
			search.setBusy(false);
		};

		search.setBusy(true);
		this.store.quickFilter(qiuckFilter, { fn: callback, scope: this });
	},

	showQuickFilter: function(show, silent) {
		this.filterVisible = show;
		DOM.swapCls(this.headersScroller, show, 'filters');
		DOM.swapCls(this.filter, !show, 'display-none');

		this.adjustAutoFit();

		if(!show) {
			var store = this.store;
			var quickFilter = store.getQuickFilter();
			if(quickFilter.length != 0)
				store.quickFilter([]);
			this.resetQuickFilter();
		} else
			this.focusQuickFilter();

		if(silent !== true)
			this.fireEvent(show ? 'quickFilterShow' : 'quickFilterHide', this);
	},

	focusQuickFilter: function() {
		var filters = this.filters;

		for(var i = 0, length = filters.length; i < length; i++) {
			if(filters[i].focus())
				return;
		}
	},

	resetQuickFilter: function() {
		var filters = this.filters;

		for(var i = 0, length = filters.length; i < length; i++)
			filters[i].reset();
	},

	onQuickFilterFocusIn: function(header) {
		this.activeFilter = header;
	},

	onQuickFilterFocusOut: function(header) {
		this.activeFilter = null;
	},

	isFiltering: function() {
		return this.activeFilter != null;
	},

	setItemsChecked: function(checked) {
		var items = this.items;
		for(var i = 0, length = items.length; i < length; i++)
			items[i].setChecked(checked);
	},

	checkAll: function() {
		var header = this.checkHeader;

		if(header == null)
			return false;

		header.setChecked(true);
		this.setItemsChecked(true);

		this.fireEvent('check', this, this.items);
		return true;
	},

	onCheckHeaderClick: function(header) {
		var checked = header.toggleCheck();
		this.setItemsChecked(checked);
		this.fireEvent('check', this, checked ? this.items : []);
	},

	onItemCheck: function(item, checked) {
		var header = this.checkHeader;
		if(header.isChecked() && !checked)
			header.setChecked(false);
		this.fireEvent('check', this, [item]);
	},

	updateChecks: function() {
		if(this.checks && this.headers.length != 0)
			this.headers[0].setChecked(false);
	},

	onItemClick: function(item, index) {
		this.setSelection(item);
		this.fireEvent('itemClick', this, item, index);
	},

	onItemDblClick: function(item, index) {
		this.fireEvent('itemDblClick', this, item, index);
	},

	onHeaderResize: function(header, change, handleType) {
		var index = this.getHeaderIndex(header);

		if(handleType == 'left')
			index--;

		header = this.headers[index];
		if(header.fixed || change == 0)
			return;

		this.setAutoFit(false);

		var headerCol = this.headerCols[index];
		var width = DOM.getPoint(headerCol, 'width');
		var itemsTableWidth = DOM.getPoint(this.itemsTable, 'width');

		var newWidth = Math.max(header.getMinWidth(), width + change);
		change = newWidth - width;

		header.setWidth(newWidth);

		if(change != 0) {
			DOM.setPoint(this.itemsTable, 'width',  itemsTableWidth + change);
			DOM.setPoint(this.itemCols[index], 'width',  newWidth);
			DOM.setPoint(this.headerCols[index], 'width',  newWidth);
			DOM.setPoint(this.totalCols[index], 'width',  newWidth);

			this.adjustScrollers();
		}
	},

	onHeaderResized: function(header) {
	},

	getParent: function(item) {
		if(item.isRoot())
			return null;

		var level = item.getLevel();
		var index = this.getIndex(item);
		var items = this.items;

		for(var i = index - 1; i >= 0; i--) {
			var item = items[i];
			if(item.getLevel() < level)
				return item;
		}

		return null;
	},

	onItemCollapse: function(item, collapsed) {
		var autoFit = this.autoFit;
		var hasVScrollbar = autoFit ? this.hasVScrollbar() : false;

		var index = this.getIndex(item);
		var level = item.getLevel();
		var items = this.items;

		for(var i = index + 1, length = items.length; i < length; i++) {
			item = items[i];
			if(level < item.getLevel())
				item.hide(collapsed);
			else
				break;
		}

		if(autoFit && hasVScrollbar != this.hasVScrollbar())
			this.adjustAutoFit();
	},

	getFirstEditorIndex: function(index) {
		var fields = this.fields;
		for(var i = index == null ? 0 : index, length = fields.length; i < length; i++) {
			var editor = fields[i].editor;
			if(editor != null)
				return i;
		}
		return -1;
	},

	onItemStartEdit: function(item, index) {
		var record = item.record;
		if(record != null && !record.isEditable())
			return false;

		if(index == null)
			index = this.lastEditedColumn;

		if(index == null)
			index = this.getFirstEditorIndex();

		if(index == -1)
			return false;

		var field = this.fields[index];
		var editor = field.editor;

		if(editor == null)
			return false;

		return this.editable ? this.startEdit(item, editor) : false;
	},

	isEditing: function() {
		return this.currentEditor != null;
	},

	startEdit: function(item, editor) {
		var record = item.record;

		if(!record.isEditable())
			return false;

		if(this.finishing) {
			this.editingQueue = { item: item, editor: editor };
			return true;
		}

		var value = record.get(editor.name) || null;
		var displayValue = record.get(editor.displayName);

		editor.initValue(value, displayValue);
		editor.setRecord(record);
		editor.item = item;

		this.openEditor(editor);
		this.currentEditor = editor;

		if(item != this.currentItem())
			this.selectItem(item);

		return true;
	},

	openEditor: function(editor) {
		var cell = editor.item.getCell(editor.index);
		editor.appendTo(cell);

		DOM.addCls(cell, 'overflow-visible');
		DOM.addCls(cell.firstChild, 'display-none');

		editor.show();

		editor.on('focusOut', this.onEditorFocusOut, this);
		DOM.on(editor, 'keyDown', this.onEditorKeyDown, this);
		editor.focus(true);
	},

	closeEditor: function(editor, focus) {
		this.lastEditedColumn = editor.index;

		editor.un('focusOut', this.onEditorFocusOut, this);
		DOM.un(editor, 'keyDown', this.onEditorKeyDown, this);

		var item = editor.item;
		if(!item.disposed) {
			var cell = editor.item.getCell(editor.index);
			DOM.removeCls(cell, 'overflow-visible');
			DOM.removeCls(cell.firstChild, 'display-none');
		}

		editor.hide();

		if(focus)
			this.focus();
	},

	cancelEdit: function(editor) {
		this.finishEdit(editor, null, true);
	},

	finishEdit: function(editor, next, cancel) {
		if(this.finishing)
			return;

		if(cancel || !editor.isValid()) {
			this.closeEditor(editor, true);
			this.currentEditor = null;
			return;
		}

		if(next == 'next' || next == 'previous')
			this.editingQueue = next == 'next' ? this.getNextEditor(editor.item, editor) : this.getPreviousEditor(editor.item, editor);

		var record = editor.record;
		record.beginEdit();
		record.set(editor.name, editor.getValue());

		var callback = function(record, success) {
			success ? record.endEdit() : record.cancelEdit();

			if(success)
				this.fireEvent('itemEdit', this, editor, record, this.fields[editor.index]);

			this.closeEditor(editor, !success || this.editingQueue == null);
			this.currentEditor = null;
			this.finishing = false;

			if(!success)
				return;

			if(this.editingQueue != null) {
				var next = this.editingQueue;
				this.editingQueue = null;
				this.startEdit(next.item, next.editor);
			}
		};

		this.finishing = true;
		record.update({ fn: callback, scope: this }, { values: this.store.getValues() });
	},

	onEditorFocusOut: function(editor) {
		this.finishEdit(editor);
	},

	onItemEditorChange: function(editor, newValue, oldValue) {
		this.fireEvent('itemEditorChange', this, editor, newValue, oldValue);
	},

	onEditorKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.cancelEdit(this.currentEditor);
			this.focus(this.currentItem());
			event.stopEvent();
		} else if(key == Event.ENTER) {
			this.finishEdit(this.currentEditor);
			event.stopEvent();
		} else if(key == Event.TAB) {
			var editor = this.currentEditor;
			this.finishEdit(editor, event.shiftKey ? 'previous' : 'next');
			event.stopEvent();
		}
	},

	getNextEditor: function(item, editor) {
		var fields = this.fields;
		var index = editor.index + 1;

		for(var i = index, length = fields.length; i < length; i++) {
			editor = fields[i].editor;
			if(editor != null)
				return { item: item, editor: editor };
		}

		var itemIndex = this.getIndex(item) + 1;
		item = this.getAt(itemIndex != this.getCount() ? itemIndex : 0); 

		for(var i = 0, length = fields.length; i < length; i++) {
			editor = fields[i].editor;
			if(editor != null)
				return { item: item, editor: editor };
		}

		return null;
	},

	getPreviousEditor: function(item, editor) {
		var fields = this.fields;
		var index = editor.index - 1;

		for(var i = index; i >= 0; i--) {
			editor = fields[i].editor;
			if(editor != null)
				return { item: item, editor: editor };
		}

		var itemIndex = this.getIndex(item) - 1;
		item = this.getAt(itemIndex != -1 ? itemIndex : this.getCount() - 1); 

		for(var i = fields.length - 1; i >= 0; i--) {
			editor = fields[i].editor;
			if(editor != null)
				return { item: item, editor: editor };
		}

		return null;
	}
});Z8.define('Z8.list.Dropdown', {
	extend: 'Z8.list.List',

	visible: false,

	cls: 'dropdown-list display-none',
	manualItemsRendering: true,

	confirmSelection: true,
	autoSelectFirst: false,

	render: function(container) {
		if(!this.inRender) {
			this.callParent(container);
			this.renderItems();
		}
	},

	renderItems: function() {
		var justRendered = this.callParent();
		this.needAlign = this.needAlign || justRendered;

		if(this.visible && this.needAlign && !this.inShow) {
			this.inRender = true;
			this.show(null, null, false);
			delete this.inRender;
		}

		return justRendered;
	},
	
	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);

		var items = this.getItems();

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(!String.isString(item))
				item.setTabIndex(tabIndex);
		}

		return tabIndex;
	},

	show: function(left, top, focusAt) {
		this.inShow = true;

		this.render();

		if(this.visible && !this.needAlign) {
			if(focusAt !== false)
				this.focus(focusAt);
			delete this.inShow;
			return;
		}

		this.needAlign = false;

		this.setPosition(left, top);

		DOM.setBottom(this, 'auto');
		DOM.removeCls(this, 'display-none');

		var wasVisible = this.visible;
		this.visible = true;

		this.align();
		this.adjustAutoFit();

		if(!wasVisible)
			this.fireEvent('show', this);

		if(focusAt !== false)
			this.focus(focusAt);

		delete this.inShow;
	},

	hide: function() {
		if(!this.visible)
			return;

		this.visible = false;

		DOM.addCls(this, 'display-none');

		this.selectItem(null);

		this.fireEvent('hide', this);
	},

	toggle: function() {
		this.visible ? this.hide() : this.show();
	},

	setSelection: function(item) {
		this.hide();
		this.callParent(item);
	},

	onCancel: function() {
		this.hide();
		this.fireEvent('cancel', this);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		var ul = this.getDom();

		if(key == Event.ESC || key == Event.TAB && target == ul) {
			this.onCancel();
			event.stopEvent();
			return;
		}

		this.callParent(event, target);
	}
});Z8.define('Z8.form.field.Field', {
	extend: 'Z8.Object',

	mixinId: 'field',

	autoSave: false,
	updatingDependencies: 0,
	suspendCheckChange: 0,

	valid: true,

	initField: function() {
		this.initValue();
	},

	initValue: function(value, displayValue) {
		value = value !== undefined ?  value : this.value;

		this.suspendCheckChange++;
		this.setValue(value, displayValue);
		this.suspendCheckChange--;

		this.initialValue = this.originalValue = this.lastValue = this.getValue();
		this.originalDisplayValue = displayValue;
	},

	getName: function() {
		return this.name;
	},

	getRecord: function() {
		return this.record;
	},

	getRecordId: function() {
		return this.record != null ? this.record.id : null;
	},

	setRecord: function(record) {
		this.record = record;
	},

	getValue: function() {
		return this.value;
	},

	getDisplayValue: function() {
		return this.getValue();
	},

	setValue: function(value, displayValue) {
		this.value = value;
		this.validate();
		this.checkChange();
	},

	reset: function() {
		this.setValue(this.originalValue);
	},

	isEqual: function(value1, value2) {
		return String(value1 || null) === String(value2 || null);
	},

	checkChange: function() {
		if(this.suspendCheckChange <= 0) {
			var newValue = this.getValue();
			var oldValue = this.lastValue;
			if(!this.disposed && this.didValueChange(newValue, oldValue)) {
				this.lastValue = newValue;
				this.fireEvent('change', this, newValue, oldValue);
			}
		}
	},

	didValueChange: function(newValue, oldValue) {
		return !this.isEqual(newValue, oldValue);
	},

	setAutoSave: function(autoSave) {
		if(this.autoSave != autoSave) {
			this.autoSave = autoSave;
			if(!this.isCombobox && !this.isCheckbox)
				this.suspendCheckChange += autoSave ? 1 : -1;
		}
	},

	initEvents: function() {
		DOM.on(this, 'focus', this.onFocusIn, this, true);
		DOM.on(this, 'blur', this.onFocusOut, this, true);
	},

	clearEvents: function() {
		DOM.un(this, 'focus', this.onFocusIn, this, true);
		DOM.un(this, 'blur', this.onFocusOut, this, true);
	},

	onFocusIn: function(event) {
		DOM.addCls(this, 'focus');
		this.fireEvent('focusIn', this);
	},

	onFocusOut: function(event) {
		var dom = DOM.get(this);
		var target = event.relatedTarget;

		if(dom == target || DOM.isParentOf(dom, target))
			return false;

		if(this.autoSave) {
			if(this.isValid()) {
				this.suspendCheckChange--;
				this.setValue(this.getValue(), this.getDisplayValue());
				this.suspendCheckChange++;
			} else
				this.initValue(this.originalValue, this.originalDisplayValue);
		}

		DOM.removeCls(this, 'focus');
		this.fireEvent('focusOut', this);
		return true;
	},

	isValid: function() {
		return this.valid;
	},

	setValid: function(valid) {
		this.valid = valid;
	},

	validate: function() {
	},

	isDependent: function() {
		var field = this.field;
		return field != null && field.dependency != null || this.dependsOn != null;
	},

	getDependencyField: function() {
		return this.field.dependency;
	},

	hasDependsOnField: function() {
		return this.field != null && this.field.dependsOn != null;
	},

	getDependsOnField: function() {
		return this.field.dependsOn;
	},

	getDependsOnValue: function() {
		return this.dependsOnValue;
	},

	setDependsOnValue: function(value) {
		this.dependsOnValue = value;
	},

	hasDependsOnValue: function() {
		return !Z8.isEmpty(this.dependsOnValue) && this.dependsOnValue != guid.Null;
	},

	addDependency: function(field) {
		if(this.dependencies == null)
			this.dependencies = [];

		field.dependsOn = this;
		this.dependencies.push(field);
	},

	updateDependencies: function(record) {
		if(this.dependencies == null || this.updatingDependencies != 0 || this.disposed)
			return;

		this.updatingDependencies++;

		var dependencies = this.dependencies;
		for(var i = 0, length = dependencies.length; i < length; i++) {
			var dependency = dependencies[i];
			if(dependency.updatingDependencies == 0)
				dependency.onDependencyChange(record);
		}

		this.updatingDependencies--;
	},

	onDependencyChange: function(value) {
	}
});Z8.define('Z8.form.field.Control', {
	extend: 'Z8.Component',

	mixins: ['Z8.form.field.Field'],

	tabIndex: 0,

	label: null, // string | { text: String, align: 'left' | 'right' | 'top', icon: String, iconAlign: 'left' | 'right', textAlign: 'left' | 'right' | 'center' };

	readOnly: false,
	readOnlyLock: false,

	initComponent: function() {
		this.callParent();
		this.initField();
		this.readOnlyLock = this.isReadOnly();
		this.enabledLock = !this.isEnabled();
	},

	getBoxMinHeight: function() {
		var minHeight = this.getMinHeight();
		return minHeight != 0 ? minHeight + Ems.UnitSpacing : 0;
	},

	subcomponents: function() {
		return [this.labelTextControl, this.tools];
	},

	htmlMarkup: function() {
		var label = this.label = String.isString(this.label) ? { text: this.label } : this.label;

		var controlMarkup = this.controlMarkup();

		var cls = this.cls = DOM.parseCls(this.cls).pushIf('control-group');

		if(!this.isEnabled())
			cls.pushIf('disabled');
		if(this.isReadOnly())
			cls.pushIf('readonly');
		if(!label)
			cls.pushIf('label-none');
		if(this.flex)
			cls.pushIf('flexed');
		if(!this.isValid())
			cls.pushIf('invalid');

		if(label) {
			var align = label.align;
			var icon = label.icon != null ? { tag: 'i', cls: this.getIconCls(label.icon).join(' ') } : null;
			var labelText = label.text;
			var isControl = labelText != null && typeof labelText == 'object';
			var control = this.labelTextControl = isControl ? labelText : null;
			var title = label.title || (isControl ? null : labelText);
			var text = isControl ? (control.htmlMarkup != null ? control.htmlMarkup() : control) : String.htmlText(labelText);

			var cn = [{ cls: 'text' + (control != null ? ' has-control' : ''), cn: icon != null ? [icon, text] : [text] }];

			if(label.tools != null) {
				var tools = this.tools = label.tools;
				cn.push(tools.htmlMarkup());
			}

			var style = label.width != null ? 'min-width:' + label.width + 'px;width:' + label.width + 'px;' : '';
			label = { tag: 'span', name: 'label', cls: this.getLabelCls().join(' '), title: title || '', style: style, cn: cn };

			align == 'right' ? controlMarkup.add(label) : controlMarkup.insert(label, 0);

			if(align != null)
				cls.pushIf('label-' + align);
		}

		return { id: this.getId(), cls: cls.join(' '), cn: controlMarkup };
	},

	completeRender: function() {
		this.callParent();

		var label = this.label = this.selectNode('span[name=label]');
		if(label != null) {
			this.labelText = DOM.selectNode(label, '.text');
			this.labelIcon = DOM.selectNode(label, '.text>.icon');
		}

		DOM.on(label, 'mouseDown', this.onLabelMouseDown, this);

		this.mixins.field.initEvents.call(this);
	},

	onDestroy: function() {
		this.mixins.field.clearEvents.call(this);

		DOM.un(this.label, 'mousedown',this.onLabelMouseDown, this);

		this.label = this.labelText = this.labelIcon = null;

		this.callParent();
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		DOM.swapCls(this, !enabled, 'disabled');
		DOM.swapCls(this.label, !enabled, 'disabled');

		this.updateTools();
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		this.readOnly = readOnly;

		DOM.swapCls(this, readOnly, 'readonly');
		DOM.swapCls(this.label, readOnly, 'readonly');

		this.updateTools();
	},

	updateTools: function() {
		DOM.swapCls(this.tools, this.isReadOnly() || !this.isEnabled(), 'display-none');
	},

	getRawValue: function() {
	},

	setRawValue: function() {
	},

	setValue: function(value, displayValue) {
		this.mixins.field.setValue.call(this, value, displayValue);
		value = this.valueToRaw(value);
		this.setRawValue(value);
	},

	isEqualValues: function(value1, value2) {
		return String(value1) == String(value2);
	},

	valueToRaw: function(value) {
		return value;
	},

	rawToValue: function(value) {
		return value;
	},

	isRequired: function() {
		return this.required;
	},

	isEmptyValue: function(value) {
		return Z8.isEmpty(value);
	},

	setValid: function(valid) {
		this.mixins.field.setValid.call(this, valid);
		DOM.swapCls(this, !valid, 'invalid');
		if(this.validationCallback != null)
			this.validationCallback(this, valid);
	},

	validate: function() {
		var value = this.getValue();
		this.setValid(!this.isEmptyValue(value) || !this.isRequired());
	},

	getIconCls: function(cls) {
		return cls != null ? DOM.parseCls(cls).pushIf('fa', 'fa-fw', 'icon') : null;
	},

	getLabelCls: function() {
		var label = this.label;

		if(label == null)
			return null;

		var cls = DOM.parseCls(this.labelCls).pushIf('label');

		if(!this.isEnabled())
			cls.pushIf('disabled');
		if(this.isReadOnly())
			cls.pushIf('readonly');
		if(label.textAlign != null)
			cls.push('text-' + label.textAlign);
		if(label.tools != null)
			cls.push('tools');
		if(Z8.isEmpty(label.text))
			cls.pushIf('empty');

		return cls;
	},

	setIcon: function(cls) {
		cls = this.getIconCls(cls);
		DOM.setCls(this.labelIcon, cls);
	},

	setLabel: function(label) {
		DOM.setValue(this.labelText, label);
	},

	onLabelMouseDown: function(event, target) {
		if(target == DOM.get(this.label) || target == this.labelText) {
			if(this.labelTextControl == null)
				event.stopEvent();
			this.focus();
		}
	}
});Z8.define('Z8.form.field.Html', {
	extend: 'Z8.form.field.Control',

	initComponent: function() {
		this.callParent();
	},

	controlMarkup: function() {
		var cls = DOM.parseCls(this.cls).pushIf('control', 'html-text');
		return [{ tag: 'div', cls: cls.join(' '), tabIndex: this.getTabIndex(), html: this.getValue() }];
	},

	completeRender: function() {
		this.callParent();
		this.div = this.selectNode('.html-text');
	},

	onDestroy: function() {
		this.div = null;
		this.callParent();
	},

	setValue: function(value, displayValue) {
		this.callParent(value, displayValue);
		DOM.setInnerHTML(this.div, value);
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this.div, !enabled, 'disabled');
		this.callParent(enabled);
	},

	setScrollTop: function(scrollTop) {
		this.div.scrollTop = scrollTop;
	},

	setScrollLeft: function(scrollLeft) {
		this.div.scrollLeft = scrollLeft;
	}
});Z8.define('Z8.form.field.Document', {
	extend: 'Z8.form.field.Control',

	isDocument: true,
	label: '',

	tag: 'div',
	height: Ems.unitsToEms(3),

	isValid: function() {
		return true;
	},

	validate: function() {},

	controlMarkup: function() {
		return [{ tag: this.tag, cls: 'control' }];
	},

	completeRender: function() {
		this.callParent();
		this.document = this.selectNode('.control');
		this.setValue(this.getValue());
	},

	onDestroy: function() {
		this.document = null;
		this.callParent();
	},

	getSource: function(value) {
		return value != null ? encodeURI(value.replace(/\\/g, '/')) + '?&session=' + Application.session : null;
	},

	setSource: function(source) {
		throw "Z8.form.field.Document.setSource is not implemented";
	},

	setValue: function(value) {
		this.callParent(value);
		this.setSource(this.getSource(value));
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this) : false;
	}
});Z8.define('Z8.form.field.Image', {
	extend: 'Z8.form.field.Document',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('image');
	},

	setSource: function(source) {
		DOM.setStyle(this.document, 'backgroundImage', source != null ? 'url(' + source + ')' : 'none');
	}
});Z8.define('Z8.form.field.File', {
	extend: 'Z8.form.field.Document',

	tag: 'iframe',

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('file');
	},

	setSource: function(source) {
		DOM.setProperty(this.document, 'src', source != null ? source + '&preview=true' : '');
	}
});Z8.define('Z8.form.field.Text', {
	extend: 'Z8.form.field.Control',

	triggers: null,

	placeholder: '',

	tag: 'input',

	editable: true,

	initComponent: function() {
		this.triggers = this.triggers || [];
		this.callParent();
	},

	controlMarkup: function() {
		var value = this.valueToRaw(this.getValue());
		var enabled = this.isEnabled();
		var readOnly = this.isReadOnly();

		var tag = this.getInputTag();
		var inputCls = this.getInputCls().join(' ');
		value = Format.htmlEncode(value);
		var input = { tag: tag, name: 'input', cls: inputCls, tabIndex: this.getTabIndex(), spellcheck: false, type: this.password ? 'password' : 'text', title: this.tooltip || '', placeholder: this.placeholder, value: tag == 'input' ? value : null, html: tag != 'input' ? value : null };

		if(!enabled)
			input.disabled = null;

		if(readOnly)
			input.readOnly = null;

		var result = [input];

		var triggers = this.triggers;

		if(!Z8.isEmpty(triggers)) {
			triggers = Array.isArray(triggers) ? triggers : [triggers];
			this.triggers = [];

			this.cls = DOM.parseCls(this.cls).pushIf('trigger-' + triggers.length);

			for(var i = 0, length = triggers.length; i < length; i++) {
				var trigger = triggers[i];
				var cls = DOM.parseCls(trigger.cls).pushIf('trigger-' + (length - i));
				if(!enabled || readOnly)
					cls.pushIf('hidden');
				trigger = new Z8.button.Trigger({ primary: true, tooltip: trigger.tooltip, icon: trigger.icon, handler: trigger.handler, scope: trigger.scope, cls: cls });
				result.push(trigger.htmlMarkup());

				this.triggers.push(trigger);
			}
		}

		return result;
	},

	subcomponents: function() {
		return this.callParent().concat(this.triggers);
	},

	completeRender: function() {
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		var input = this.input = this.selectNode(this.getInputTag() + '[name=input]');

		if(this.editable)
			DOM.on(input, 'input', this.onInput, this);

		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this.input, 'input', this.onInput, this);
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.input = null;

		this.callParent();
	},

	swapTriggersCls: function(condition, trueCls, falseCls) {
		var triggers = this.triggers;
		for(var i = 0, length = triggers.length; i < length; i++)
			DOM.swapCls(triggers[i], condition, trueCls, falseCls);
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this.input, !enabled, 'disabled');
		DOM.setDisabled(this.input, !enabled);
		this.swapTriggersCls(!enabled || this.isReadOnly(), 'hidden');
		this.callParent(enabled);
	},

	setReadOnly: function(readOnly) {
		if(this.isReadOnly() != readOnly) {
			DOM.setReadOnly(this.input, readOnly);
			this.swapTriggersCls(!this.isEnabled() || readOnly, 'hidden');
		}
		this.callParent(readOnly);
	},

	getRawValue: function(value) {
		return DOM.getValue(this.input);
	},

	setRawValue: function(value) {
		DOM.setValue(this.input, value);
		DOM.setAttribute(this.input, 'title', value);
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);
		DOM.setTabIndex(this.input, tabIndex);
		return tabIndex;
	},

	getInputTag: function() {
		return this.editable ? this.tag : 'div';
	},

	getInputCls: function() {
		var cls = DOM.parseCls(this.inputCls).pushIf('control');
		if(!this.isEnabled())
			cls.pushIf('disabled');
		return cls;
	},

	focus: function(select) {
		return this.isEnabled() ? DOM.focus(this.input, select) : false;
	},

	getTrigger: function(index) {
		var triggers = this.triggers;
		return triggers[index || triggers.length - 1];
	},

	onClick: function(event, target) {
		event.stopEvent();

		var triggers = this.triggers;

		for(var i = 0, length = triggers.length; i < length; i++) {
			var trigger = triggers[i];
			if(DOM.isParentOf(trigger, target) && trigger.isEnabled()) {
				var handler = trigger.handler;
				if(handler != null)
					handler.call(trigger.scope, trigger);
				else
					this.onTriggerClick(trigger);
				return;
			}
		}
	},

	onTriggerClick: function(trigger) {
	},

	onKeyEvent: function(event, target) {
	},

	onKeyDown: function(event, target) {
		if(!this.isEnabled() || this.isReadOnly())
			return;

		if(this.onKeyEvent(event, target))
			event.stopEvent();
	},

	onInput: function(event, target) {
		var rawValue = this.getRawValue();
		var value = this.rawToValue(rawValue);
		this.mixins.field.setValue.call(this, value);
	}
});Z8.define('Z8.form.field.Number', {
	extend: 'Z8.form.field.Text',

	increment: 1,

	getInputCls: function() {
		return this.callParent().pushIf('number');
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		var increment = 0;

		if(key == Event.UP && event.ctrlKey || key == Event.PAGE_UP)
			increment = 10 * this.increment;
		else if(key == Event.UP && event.shiftKey)
			increment = 100 * this.increment;
		else if(key == Event.DOWN && event.ctrlKey || key == Event.PAGE_DOWN)
			increment = -10 * this.increment;
		else if(key == Event.DOWN && event.shiftKey)
			increment = -100 * this.increment;
		else if(key == Event.UP)
			increment = this.increment;
		else if(key == Event.DOWN)
			increment = -this.increment;

		if(increment != 0) {
			this.setValue((this.getValue() || 0) + increment);
			return true;
		}

		return Event.A <= key && key <= Event.Z && !event.ctrlKey && !event.altKey;
	}
});Z8.define('Z8.form.field.Integer', {
	extend: 'Z8.form.field.Number',

	radix: 10,

	rawToValue: function(value) {
		return Parser.integer(value, this.radix);
	},

	valueToRaw: function(value) {
		return Format.integer(value);
	}
});Z8.define('Z8.form.field.Float', {
	extend: 'Z8.form.field.Number',

	rawToValue: function(value) {
		return Parser.float(value);
	},

	valueToRaw: function(value) {
		return Format.float(value);
	}
});Z8.define('Z8.form.field.Checkbox', {
	extend: 'Z8.form.field.Control',

	isCheckbox: true,

	initComponent: function() {
		this.callParent();
		this.cls = DOM.parseCls(this.cls).pushIf('checkbox');
	},

	isValid: function() {
		return true;
	},

	validate: function() {},

	controlMarkup: function() {
		var label = this.label;

		if(label != null) {
			label.align = label.align || 'right';
			label.icon = null;
		}

		return [{ tag: 'div', name: 'icon', cls: this.iconCls.join(' '), html: String.htmlText('') }];
	},

	htmlMarkup: function() {
		var markup = this.callParent();
		markup.tabIndex = this.getTabIndex();
		return markup;
	},

	completeRender: function() {
		this.icon = this.selectNode('div[name=icon]');

		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.icon = null;

		this.callParent();
	},

	isEqualValues: function(value1, value2) {
		return value1 && value2 || !(value1 || value2);
	},

	setValue: function(value) {
		this.callParent(value);

		this.iconCls = [value ? 'fa-check-square' : 'fa-square-o', 'fa', 'control'];
		DOM.swapCls(this.icon, value, 'fa-check-square', 'fa-square-o');
	},

	setTabIndex: function(tabIndex) {
		tabIndex = this.callParent(tabIndex);
		DOM.setTabIndex(this, tabIndex);
		return tabIndex;
	},

	onClick: function(event, target) {
		event.stopEvent();

		if(!this.isEnabled() || this.isReadOnly())
			return;

		this.setValue(!this.getValue());
	},

	onKeyDown: function(event, target) {
		if(!this.isEnabled() || this.isReadOnly())
			return;

		var key = event.getKey();

		if(key == Event.SPACE)
			this.onClick(event, target);
	},

	focus: function() {
		return this.isEnabled() ? DOM.focus(this) : false;
	}
});Z8.define('Z8.form.field.TextArea', {
	extend: 'Z8.form.field.Text',

	trigger: false,
	tag: 'textarea'
});Z8.define('Z8.form.field.Combobox', {
	extend: 'Z8.form.field.Text',

	isCombobox: true,

	displayName: 'name',
	name: 'id',

	emptyValue: guid.Null,

	enterToOpen: true,
	enterOnce: false,

	queryDelay: 250,
	lastQuery: '',

	pagerMode: 'visible', // 'visible' || 'hidden'

	initComponent: function() {
		var store = this.store;

		if(store != null && !store.isStore)
			store = this.store = new Z8.query.Store(store);

		this.callParent();

		this.cls = DOM.parseCls(this.cls).pushIf('dropdown-combo');

		if(this.editable)
			this.queryTask = new Z8.util.DelayedTask();

		this.initStore();
	},

	isEmptyValue: function(value) {
		return value == this.emptyValue || this.callParent(value);
	},

	getStore: function() {
		return this.store;
	},

	createDropdown: function() {
		var dropdown = this.dropdown = new Z8.list.Dropdown({ store: this.getStore(), value: this.getValue(), name: this.name, fields: this.fields || this.displayName, icons: this.icons, checks: this.checks });
		dropdown.on('select', this.selectItem, this);
		dropdown.on('cancel', this.cancelDropdown, this);

		var cls = this.needsPager() ? '' : 'display-none';
		var pager = this.pager = new Z8.pager.Pager({ cls: 'display-none', store: this.getStore() });

		dropdown.on('align', this.onDropdownAlign, this);
		dropdown.on('show', this.onDropdownShow, this);
		dropdown.on('hide', this.onDropdownHide, this);
	},

	htmlMarkup: function() {
		var triggers = this.triggers;

		if(this.source != null)
			triggers.push({ icon: 'fa-pencil', tooltip: 'Редактировать \'' + this.source.text + '\'', handler: this.editSource, scope: this });

		if(!this.isRequired())
			triggers.push({ icon: 'fa-times', tooltip: 'Очистить', handler: this.clearValue, scope: this });

		triggers.push({});

		var markup = this.callParent();

		this.createDropdown();

		markup.cn.push(this.dropdown.htmlMarkup());
		markup.cn.push(this.pager.htmlMarkup());

		return markup;
	},

	subcomponents: function() {
		return this.callParent().add([this.dropdown, this.pager]);
	},

	completeRender: function() {
		this.callParent();

		this.dropdown.setAlignment(this.input);

		if(!this.editable)
			DOM.on(this, 'keyPress', this.onKeyPress, this);

		DOM.on(this.input, 'dblClick', this.onDblClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keyPress', this.onKeyPress, this);
		DOM.un(this.input, 'dblClick', this.onDblClick, this);

		this.setStore(null);

		this.callParent();
	},

	setEnabled: function(enabled) {
		if(!enabled && this.dropdown != null)
			this.dropdown.hide();

		this.callParent(enabled);
	},

	setReadOnly: function(readOnly) {
		if(!readOnly && this.dropdown != null)
			this.dropdown.hide();

		this.callParent(readOnly);
	},

	formatValue: function(value) {
		var field = this.field || {};

		switch(field.type) {
		case Type.Date:
		case Type.Datetime:
			value = String.isString(value) ? Parser.datetime(value) : value;
			return value != null ? Format.date(value, field.format) : '';
		case Type.Boolean:
			return Parser.boolean(value) ? 'да' : 'нет';
		default:
			return value;
		}
	},

	getSelectedRecord: function(value) {
		var store = this.getStore();
		return store != null ? store.getById(value || this.getValue()) : null;
	},

	setValue: function(value, displayValue) {
		if(displayValue == undefined) {
			var record = this.getSelectedRecord(value);
			displayValue = record != null ? record.get(this.displayName) : null;
		}

		this.entered = true;

		this.displayValue = displayValue = this.formatValue(displayValue || '');

		this.clearFilter();
		this.callParent(value, displayValue);
	},

	onDependencyChange: function(record) {
		var value = this.dependsOnValue = record != null ? (this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id) : null;
		this.updateWhere(value);
		this.setValue(this.emptyValue);
	},

	setRecord: function(record) {
		this.callParent(record);

		if(record != null)
			this.updateWhere(this.dependsOn != null ? record.get(this.dependsOn.name) : null);
	},

	updateWhere: function(value) {
		var where = this.getWhere(value);

		var store = this.getStore();
		store.setWhere(where);
		store.unload();
	},

	getWhere: function(dependencyValue) {
		var where = [];

		if(dependencyValue != null)
			where.push({ property: this.getDependencyField(), value: dependencyValue });

		var record = this.getRecord();
		var link = this.field.link;

		if(record == null || !link.isParentKey)
			return where;

		var parentKeys = link.parentKeys;
		for(var i = 0, length = parentKeys.length; i < length; i++)
			where.push({ property: parentKeys[i], operator: Operation.NotEq, value: record.id });

		return where;
	},

	getDisplayValue: function() {
		return this.displayValue;
	},

	valueToRaw: function(value) {
		return this.displayValue;
	},

	currentItem: function() {
		return this.dropdown.getItem(this.getValue());
	},

	currentIndex: function() {
		return this.dropdown.getIndex(this.getValue());
	},

	isLoaded: function() {
		return this.dropdown.isLoaded();
	},

	initStore: function() {
		var store = this.store;
		this.store = null;
		this.setStore(store);
	},

	setStore: function(store) {
		if(this.store == store)
			return;

		var currentStore = this.store;

		if(currentStore != null) {
			currentStore.un('beforeLoad', this.beforeLoadCallback, this);
			currentStore.un('load', this.loadCallback, this);
			currentStore.dispose();
		}

		this.store = store;

		if(store != null) {
			store.use();

			this.beforeLoadCallback = this.beforeLoadCallback || function() {
				this.getTrigger().setBusy(true);
			};
			store.on('beforeLoad', this.beforeLoadCallback, this);

			this.loadCallback = this.loadCallback || function() {
				this.getTrigger().setBusy(false);
			};
			store.on('load', this.loadCallback, this);
		}

		if(this.pager != null)
			this.pager.setStore(store);

		if(this.dropdown != null)
			this.dropdown.setStore(store);
	},

	load: function(callback, filter) {
		this.dropdown.load(callback, filter);
	},

	filter: function() {
		var text = this.getRawValue() || '';

		if(text == this.lastQuery && text != '')
			return;

		var store = this.dropdown.getStore();

		if(store == null)
			return;

		var delay = store.getRemoteFilter() ? this.queryDelay : 0;

		var query = function(text) {
			if(store.isLoading()) {
				this.queryTask.delay(delay, query, this, text);
				return;
			}

			this.lastQuery = text;

			var callback = function(store, records, success) {
				if(success) {
					var item = this.findItem(text);
					this.dropdown.selectItem(item);
					this.showDropdown(true);
				}
			};

			var filter = !Z8.isEmpty(text) ? [{ property: this.displayName, operator: Operation.Contains, value: text, anyMatch: true }] : [];
			this.load({ fn: callback, scope: this }, filter);
		};

		this.queryTask.delay(delay, query, this, text);
	},

	clearFilter: function() {
		if(this.lastQuery == '')
			return;

		this.queryTask.cancel();
		this.lastQuery = '';

		this.dropdown.clearFilter();
	},

	findItem: function(text, start) {
		if(Z8.isEmpty(text))
			return null;

		var items = this.dropdown.getItems();

		start = start || 0;
		var index = start;
		var count = items.length;

		while(index < count) {
			var item = items[index];
			var itemText = (item.getText(this.displayName) || '').toLowerCase();
			if(itemText.startsWith(text.toLowerCase()))
				return item;

			index = index < count -1 ? index + 1 : 0;

			if(index == start)
				return null;
		}

		return null;
	},

	// direction: first, last, next, previous
	select: function(item, direction) {
		var callback = function(store, records, success) {
			if(!success)
				return;

			if(String.isString(item)) {
				direction = item;
				item = this.currentItem();
			} else
				item = item || this.currentItem();

			var index = item != null ? this.dropdown.getIndex(item.getValue()) : -1;

			var items = this.dropdown.getItems();
			var count = items.length;

			if(direction == 'next')
				index = index != count - 1 ? index + 1 : 0;
			else if(direction == 'previous')
				index = index != 0 ? index - 1 : (count - 1);
			else if(direction == 'first')
				index = 0;
			else if(direction == 'last')
				index = count - 1;

			index = Math.min(Math.max(index, 0), count - 1);

			if(index == -1)
				return;

			item = items[index];
			this.setValue(item.getValue(), item.getText(this.displayName));
		};

		if(!this.isLoaded())
			this.load({ fn: callback, scope: this });
		else
			callback.call(this, this.store, this.store.getRecords(), true);
	},

	selectByKey: function(key) {
		var callback = function(store, records, success) {
			if(!success)
				return;

			var index = this.currentIndex();
			var item = this.findItem(key, index + 1);

			item = this.findItem(key, index + 1);

			if(item != null)
				this.setValue(item.getValue(), item.getText(this.displayName));
		}

		if(!this.isLoaded())
			this.load({ fn: callback, scope: this });
		else
			callback.call(this, this.store, this.store.getRecords(), true);
	},

	selectItem: function(item) {
		var value = item != null ? item.getValue() : this.emptyValue;
		var displayValue = item != null ? item.getText(this.displayName) : '';
		this.setValue(value, displayValue);
		this.hideDropdown();
	},

	show: function() {
		this.entered = false;
		this.callParent();
	},

	openDropdown: function() {
		var callback = function(store, records, success) {
			if(success)
				this.showDropdown();
		};

		if(!this.isLoaded())
			this.load({ fn: callback, scope: this });
		else
			this.showDropdown();
	},

	needsPager: function() {
		return this.pagerMode == 'visible';
	},

	realignPager: function() {
		if(!this.needsPager())
			return;

		var dropdown = this.dropdown;
		var rect = new Rect(this.dropdown);

		var pager = DOM.get(this.pager);
		DOM.setLeft(pager, DOM.getLeft(dropdown));
		DOM.setRight(pager, DOM.getRight(dropdown));
		DOM.setTop(pager, DOM.getTop(dropdown) + rect.height);
	},

	getPagerSize: function() {
		if(!this.needsPager())
			return { width: 0, height: 0 };

		var pager = this.pager;

		var wasVisible = this.pager.isVisible();

		if(!wasVisible)
			this.showPager();

		pager.size = pager.getSize();

		if(!wasVisible)
			this.hidePager();

		return pager.size;
	},

	showDropdown: function(keepFocus) {
		var dropdown = this.dropdown;

		var left = DOM.getOffsetLeft(this.input);
		DOM.setLeft(dropdown, left);
		DOM.setRight(dropdown, Ems.pixelsToEms(-1));

		var item = this.currentItem();
		var focusAt = keepFocus ? false : item;

		var pagerSize = this.getPagerSize();

		dropdown.setAlignmentOffset(0, pagerSize.height, Ems.pixelsToEms(17));
		dropdown.show(null, null, focusAt);
	},

	hideDropdown: function() {
		this.dropdown.hide();
		this.clearFilter();
	},

	cancelDropdown: function() {
		this.setRawValue(this.displayValue);
		this.hideDropdown();
	},

	toggleDropdown: function() {
		if(this.dropdown.visible)
			this.cancelDropdown();
		else
			this.openDropdown();
	},

	showPager: function() {
		if(!this.needsPager())
			this.hidePager();
		else
			this.pager.show();
	},

	hidePager: function() {
		this.pager.hide();
	},

	onDropdownAlign: function(dropdown) {
		this.realignPager();
		this.showPager();
	},

	onDropdownShow: function() {
		this.getTrigger().rotateIcon(180);
	},

	onDropdownHide: function() {
		this.hidePager();
		this.getTrigger().rotateIcon(0);
		DOM.focus(this.input);
	},

	onFocusOut: function(event) {
		if(!this.callParent(event))
			return false;

		this.cancelDropdown();
		return true;
	},

	onTriggerClick: function(trigger) {
		this.toggleDropdown();
	},

	onDblClick: function(event, target) {
		if(this.isEnabled() && !this.isReadOnly())
			this.toggleDropdown();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		var editable = this.editable;
		var dropdown = this.dropdown;
		var dropdownOpen = dropdown.visible;
		var currentItem = this.dropdown.currentItem();

		var me = this;
		var isTrigger = function() {
			return DOM.isParentOf(me.triggers, target);
		};

		var isInput = function() {
			return DOM.isParentOf(me.input, target);
		};

		if(key == Event.DOWN)
			dropdownOpen ? dropdown.focus(currentItem) : this.select('next');
		else if(key == Event.UP)
			dropdownOpen ? dropdown.focus(currentItem) : this.select('previous');
		else if(key == Event.HOME && (!editable || !isInput()))
			dropdownOpen ? dropdown.focus(currentItem) : this.select('first');
		else if(key == Event.END && (!editable || !isInput()))
			dropdownOpen ? dropdown.focus(currentItem) : this.select('last');
		else if(key == Event.TAB && isTrigger()) {
			dropdownOpen ? dropdown.focus(currentItem) : this.cancelDropdown();
			return false;
		} else if(key == Event.ENTER || key == Event.SPACE && (!this.editable || isTrigger())) {
			if(dropdownOpen)
				currentItem || !this.isRequired() ? this.selectItem(currentItem) : this.cancelDropdown();
			else if(this.enterToOpen && (!this.enterOnce || !this.entered)) {
				this.onTriggerClick(event, target);
				this.entered = true;
			} else if(event.ctrlKey)
				this.onTriggerClick(event, target);
			else
				return false;
		} else if(key == Event.ESC && (dropdownOpen || this.lastQuery != ''))
			this.cancelDropdown();
		else
			return false;

		return true;
	},

	onInput: function(event, target) {
		this.filter();
	},

	onKeyPress: function(event, target) {
		this.selectByKey(String.fromCharCode(event.getKey()));
	},

	editSource: function() {
		Viewport.open(this.source.id);
		this.getStore().unload();
	},

	clearValue: function() {
		this.setValue(this.emptyValue, '');
	}
});Z8.define('Z8.form.field.Listbox', {
	extend: 'Z8.form.field.Control',

	tabIndex: -1,

	isListbox: true,

	tools: false,
	locks: false,
	totals: false,

	editable: false,

	pagerMode: 'visible', // 'visible' || 'hidden'

	initComponent: function() {
		var store = this.store;

		if(store != null && !store.isStore)
			store = this.store = new Z8.query.Store(store);

		this.callParent();
		this.setValue(this.value);
	},

	isEmptyValue: function(value) {
		return this.store.getCount() == 0;
	},

	getValue: function() {
		return this.mixins.field.getValue.call(this);
	},

	setValue: function(value) {
		var list = this.list;

		if(list != null) {
			var item = list.getItem(value);
			item != null ? list.selectItem(item) : (value = guid.Null);
		}

		if(value != this.getValue())
			this.mixins.field.setValue.call(this, value);
	},

	isEditable: function() {
		return this.editable && !this.isReadOnly();
	},

	setEnabled: function(enabled) {
		this.callParent(enabled);

		var list = this.list;
		if(list != null)
			list.setEditable(this.editable && enabled && !this.isReadOnly());
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly);

		var list = this.list;
		if(list != null)
			list.setEditable(this.editable && !readOnly && this.isEnabled());
	},

	hasLink: function() {
		return this.getLink() != null;
	},

	getLink: function() {
		var query = this.query;
		return query != null && query.link != null ? this.query.link.name : null;
	},

	getItem: function(record) {
		var record = record.isModel ? record.id : record;
		return this.list.getItem(record);
	},

	getModel: function() {
		return this.store.getModel();
	},

	getFields: function() {
		return this.list.getFields();
	},

	getControl: function(name) {
		var controls = this.controls;

		if(controls == null)
			return null;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.name == name || control.displayName == name)
				return control;
		}

		return null;
	},

	getHeaders: function() {
		return this.list.getHeaders();
	},

	needsValues: function() {
		var values = this.getValues();
		if(values == null)
			return false;

		for(var name in values) {
			var value = values[name];
			if(Z8.isEmpty(value) || value == guid.Null)
				return true;
		}
		return false;
	},

	getValues: function() {
		var record = this.record;

		if(record == null)
			return null;

		var fields = record.getValueFor();

		if(fields.length == 0)
			return null;

		var values = {};
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			values[field.valueFor] = record.get(field.name);
		}

		return values;
	},

	setRecord: function(record) {
		var current = this.getRecord();

		if(current != null)
			current.un('change', this.onRecordChange, this);

		this.callParent(record);

		if(record != null)
			record.on('change', this.onRecordChange, this);

		this.afterRecordSet(record);
		this.onRecordChange(record, {});
	},

	onRecordChange: function(record, modified) {
		this.setEnabled(!this.needsValues());
	},

	getFilter: function(record) {
		if(!this.hasLink())
			return null;

		var filter = this.getDependencyWhere(this.getLink(), record.id);

		filter = Array.isArray(filter) ? filter : (filter != null ? [filter] : []);

		var parentKeys = this.query.link.parentKeys;

		if(parentKeys != null) {
			for(var i = 0, length = parentKeys.length; i < length; i++)
				filter.push({ property: parentKeys[i], value: record.id });

			return [{ logical: 'or', expressions: filter }];
		}

		return filter;
	},

	afterRecordSet: function(record) {
		if(record != null) {
			if(!this.isDependent()) {
				var filter = this.getFilter(record);
				this.store.setValues(this.getValues());
				var loadCallback = function(store, records, success) {
					this.validate();
				};
				this.store.filter(filter, { fn: loadCallback, scope: this });
			}
		} else {
			this.clear();
			this.validate();
		}
	},

	getDependencyWhere: function(property, value) {
		return [{ property: property, value: value }];
	},

	onDependencyChange: function(record) {
		var recordId = this.getRecordId();

		var dependsOnValue = null;

		var loadCallback = function(store, records, success) {
			this.setDependsOnValue(dependsOnValue);
			this.validate();
		};

		if(record != null) {
			dependsOnValue = this.hasDependsOnField() ? record.get(this.getDependsOnField()) : record.id;
			var where = this.getDependencyWhere(this.getDependencyField(), dependsOnValue);
			if(this.hasLink())
				where.push({ property: this.getLink(), value: recordId });
			this.store.setWhere(where);
			this.store.load({ fn: loadCallback, scope: this });
		} else {
			this.store.removeAll();
			loadCallback.call(this, this.store, [], true);
			this.onSelect(null);
		}
	},

	clear: function() {
		if(this.list != null)
			this.list.setRecords([]);
	},

	subcomponents: function() {
		return this.callParent().add([this.list, this.pager, this.selector, this.actions]);
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('list-box').pushIf(this.needsPager() ? 'pager-on' : '');

		var label = this.label;
		if(this.tools) {
			if(label == null)
				label = this.label = {};
			if(label.tools == null)
				label.tools = new Z8.button.Group({ items: this.createTools() });
			this.hasTools = true;
		}

		return this.callParent();
	},

	setAddTool: function(button) {
		this.addTool = button;
	},

	setCopyTool: function(button) {
		this.copyTool = button;
	},

	setEditTool: function(button) {
		this.editTool = button;
	},

	setRefreshTool: function(button) {
		this.refreshTool = button;
	},

	setRemoveTool: function(button) {
		this.removeTool = button;
	},

	setFilterTool: function(button) {
		this.filterTool = button;
	},

	setPeriodTool: function(button) {
		this.periodTool = button;
	},

	setSortTool: function(button) {
		this.sortTool = button;
	},

	setExportTool: function(button) {
		this.exportTool = button;
	},

	setAutoFitTool: function(button) {
		this.autoFitTool = button;
	},

	createTools: function() {
		var store = this.store;

		var addCopyRemove = [];

		if(store.hasCreateAccess()) {
			var add = new Z8.button.Tool({ icon: 'fa-file-o', tooltip: 'Новая запись (Insert)', handler: this.onAddRecord, scope: this });
			this.setAddTool(add);
			addCopyRemove.push(add);
		}

		if(store.hasCopyAccess()) {
			var copy = new Z8.button.Tool({ icon: 'fa-copy', tooltip: 'Копировать запись (Shift+Insert)', handler: this.onCopyRecord, scope: this });
			this.setCopyTool(copy);
			addCopyRemove.push(copy);
		}

		if(store.hasReadAccess()) {
			if(this.source != null) {
				var edit = new Z8.button.Tool({ icon: 'fa-pencil', tooltip: 'Редактировать \'' + this.source.text + '\'', handler: this.onEdit, scope: this });
				this.setEditTool(edit);
				addCopyRemove.push(edit);
			};

			var refresh = new Z8.button.Tool({ icon: 'fa-refresh', tooltip: 'Обновить (Ctrl+R)', handler: this.onRefresh, scope: this });
			addCopyRemove.push(refresh);
			this.setRefreshTool(refresh);
		}

		if(store.hasDestroyAccess()) {
			var remove = new Z8.button.Tool({ cls: 'remove', danger: true, icon: 'fa-trash', tooltip: 'Удалить запись (Delete)', handler: this.onRemoveRecord, scope: this });
			addCopyRemove.push(remove);
			this.setRemoveTool(remove);
		}

		var tools = [];

		if(addCopyRemove.length != 0) {
			addCopyRemove = new Z8.button.Group({ items: addCopyRemove });
			tools.push(addCopyRemove);
		}

		var filterSort = [];

		if(this.filter !== false) {
			var filter = new Z8.button.Tool({ icon: 'fa-filter', tooltip: 'Фильтрация (Ctrl+F)', toggled: false });
			this.setFilterTool(filter);

			filter.on('toggle', this.onQuickFilter, this);
			filterSort.push(filter);
		}

		var sort = new Z8.button.Tool({ enabled: false, cls: 'btn-sm', icon: 'fa-sort', tooltip: 'Порядок сортировки' });
		this.setSortTool(sort);
		filterSort.push(sort);

		var filterSort = new Z8.button.Group({ items: filterSort });

		tools.push(filterSort);

		var period = this.createPeriodTool();
		this.setPeriodTool(period);
		if(period != null)
			tools.push(period);

		var exportAs = this.createExportTool();
		this.setExportTool(exportAs);
		tools.push(exportAs);

		var autoFit = new Z8.button.Tool({ icon: 'fa-arrows-h', tooltip: 'Auto fit columns', toggled: true });
		autoFit.on('toggle', this.onAutoFit, this);
		this.setAutoFitTool(autoFit);
		tools.push(autoFit);

		return tools;
	},

	createPeriodTool: function() {
		if(this.store.getPeriodProperty() == null)
			return null;

		var period = new Z8.data.Period();

		period = new Z8.calendar.Button({ cls: 'btn-sm', icon: 'fa-calendar', period: period });
		period.on('period', this.onPeriod, this);
		return period;
	},

	createExportTool: function() {
		var items = [
			new Z8.menu.Item({ text: 'Acrobat Reader (*.pdf)', icon: 'fa-file-pdf-o', format: 'pdf' }),
			new Z8.menu.Item({ text: 'Microsoft Excel (*.xls)', icon: 'fa-file-excel-o', format: 'xls' }),
			new Z8.menu.Item({ text: 'Microsoft Word (*.doc)', icon: 'fa-file-word-o', format: 'doc' }),
			'-',
			new Z8.menu.Item({ text: 'Настройки', icon: 'fa-print', enabled: false })
		];

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuExportAs, this);

		return new Z8.button.Tool({ cls: 'btn-sm', icon: 'fa-file-pdf-o', tooltip: 'Сохранить как PDF', menu: menu, handler: this.exportAs, scope: this, format: 'pdf' });
	},

	createActions: function() {
		var query = this.query;

		if(query == null)
			return null;

		var actions = query.actions;

		if(Z8.isEmpty(actions))
			return null;

		var buttons = [];

		for(var i = 0, length = actions.length; i < length; i++) {
			var action = actions[i];
			var type = action.type;
			var button = new Z8.button.Button({ name: action.id, text: action.text, tooltip: action.description, icon: action.icon, action: action, primary: type == 'primary', success: type == 'success', danger: type == 'danger', handler: this.onAction, scope: this });
			buttons.push(button);
		}

		this.controls = buttons;

		return new Z8.Container({ cls: 'actions',  items: buttons });
	},

	controlMarkup: function() {
		var list = this.list = new Z8.list.List({ cls: 'control', store: this.store, items: this.items, totals: this.totals, locks: this.locks, name: this.name, fields: this.fields, editable: this.isEditable(), itemType: this.itemType, value: this.getValue(), icons: this.icons, checks: this.checks, filters: this.filters, useENTER: false });
		list.on('select', this.onSelect, this);
		list.on('contentChange', this.onContentChange, this);
		list.on('itemEditorChange', this.onItemEditorChange, this);
		list.on('itemEdit', this.onItemEdit, this);
		list.on('itemClick', this.onItemClick, this);
		list.on('itemDblClick', this.onItemDblClick, this);
		list.on('check', this.onItemCheck, this);
		list.on('quickFilterShow', this.onQuickFilterShow, this);
		list.on('quickFilterHide', this.onQuickFilterHide, this);
		list.on('autoFit', this.onListAutoFit, this);

		var cls = this.needsPager() ? '' : 'display-none';
		var pager = this.pager = new Z8.pager.Pager({ cls: cls, store: this.store });

		var markup = [list.htmlMarkup(), pager.htmlMarkup()];

		var actions = this.actions = this.createActions();
		if(actions != null)
			markup.push(actions.htmlMarkup());

		return markup;
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		var store = this.store;
		store.on('beforeLoad', this.onBeforeLoad, this);
		store.on('load', this.onLoad, this);
	},

	onDestroy: function() {
		this.setRecord(null);

		var store = this.store;
		store.un('beforeLoad', this.onBeforeLoad, this);
		store.un('load', this.onLoad, this);

		DOM.un(this, 'keyDown', this.onKeyDown, this);
		this.callParent();
	},

	setFields: function(fields) {
		this.fields = fields;
		if(this.list != null)
			this.list.setFields(fields);
	},

	onSelect: function(item) {
		var record = item != null ? item.record : null;

		this.mixins.field.setValue.call(this, record != null ? record.id : guid.Null);

		this.fireEvent('select', this, record);
		this.updateTools();

		if(this.selectTask == null)
			this.selectTask = new Z8.util.DelayedTask();

		this.selectTask.delay(50, this.updateDependencies, this, record);
	},

	onContentChange: function() {
		this.fireEvent('contentChange', this);
		this.updateTools();
		this.updatePager();
	},

	onItemEditorChange: function(list, editor, newValue, oldValue) {
		this.fireEvent('itemEditorChange', this, editor, newValue, oldValue);
	},

	onItemEdit: function(list, editor, record, field) {
		if(field != null && field.important)
			this.reloadRecord();
	},

	focus: function() {
		return this.isEnabled() ? this.list.focus() : false;
	},

	getSelection: function() {
		var item = this.list != null ? this.list.currentItem() : null;
		return item != null ? item.record : null;
	},

	getChecked: function() {
		if(this.list == null)
			return [];

		var records = this.list.getCheckedRecords();

		if(records.length != 0)
			return records;

		var selection = this.getSelection();
		return selection != null ? [selection] : [];
	},

	getDestroyable: function() {
		var destroyable = [];

		var checked = this.getChecked();

		for(var i = 0, length = checked.length; i < length; i++) {
			var record = checked[i];
			if(record.isDestroyable())
				destroyable.push(record);
		}

		return destroyable;
	},

	select: function(item) {
		var list = this.list;
		if(String.isString(item))
			item = list.getItem(item);
		else if(Number.isNumber(item))
			item = list.getAt(Math.min(item, list.getCount() - 1));
		else if(item != null && item.isModel)
			item = list.getItem(item.id);

		this.list.setSelection(item);
		return item;
	},

	startEdit: function(record, index) {
		if(record != null) {
			var list = this.list;
			var item = list.getItem(record.id);
			return list.onItemStartEdit(item, index);
		}
		return false;
	},

	getSelectorQuery: function(fields) {
		var linkedField = null;
		var displayFields = [];
		var myLink = this.getLink();

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(field.isCombobox) {
				if(!field.link.isBackward && field.link.name != myLink) {
					if(linkedField != null && field.link.name != linkedField.link.name)
						return null;
					linkedField = field;
				}
				if(field.link.name != myLink)
					displayFields.push(field);
			}
		}

		if(linkedField == null)
			return null;

		return { id: this.query.id, name: linkedField.query.name, link: linkedField.link.name, primaryKey: linkedField.link.primaryKey, fields: displayFields, label: { text: linkedField.header, icon: linkedField.icon } };
	},

	createRecordsSelector: function() {
		var query = this.query;

		if(this.query == null)
			return null;

		query = this.getSelectorQuery(this.query.columns);

		if(query == null)
			return null;

		var store = Z8.form.Helper.storeConfig({ isListbox: true, query: { id: query.id, name: query.name, fields: query.fields, primaryKey: query.primaryKey }, values: this.getValues() });
		return new Z8.form.field.Listbox({ name: query.link, fields: query.fields, label: query.label, store: store, tools: false, pagerMode: 'visible', checks: true, height: '100%' });
	},

	newRecord: function() {
		var record = Z8.create(this.getModel());

		if(this.hasLink())
			record.set(this.getLink(), this.getRecordId());

		if(this.isDependent())
			record.set(this.getDependencyField(), this.getDependsOnValue());

		return record;
	},

	createRecords: function(records) {
		var callback = function(records, success) {
			if(success) {
				this.reloadRecord();

				var store = this.store;
				store.setValues(this.getValues());
				store.insert(records, 0);
				var record = records[0];
				this.select(record);

				if(this.selector != null || !this.startEdit(record, 0))
					this.focus();
			}

			if(this.selector != null) {
				if(success)
					this.closeSelector();
				else
					this.selector.setBusy(false);
			}
		};

		var batch = new Z8.data.Batch({ model: this.getModel() });
		batch.create( records, { fn: callback, scope: this }, { values: this.getValues() });
	},

	closeSelector: function() {
		this.selector.close();
		this.selector = null;
	},

	onAddRecord: function(button) {
		this.addRecord(button);
	},

	getValueFromFields: function() {
		if(this.valueFromFields == null)
			this.valueFromFields = this.store.getValueFromFields();
		return this.valueFromFields;
	},

	reloadRecord: function(callback) {
		var record = this.record;
		if(record != null)
			record.reload(callback);
	},

	addRecord: function(button) {
		var selector = this.createRecordsSelector();

		if(selector == null) {
			var records = this.newRecord();
			this.createRecords(records);
			return;
		}

		if(button != null)
			button.setBusy(true);

		var loadCallback = function(store, records, success) {
			if(button != null)
				button.setBusy(false);

			if(!success)
				return;

			var addRecordCallback = function(dialog, success) {
				if(!success) {
					this.closeSelector();
					this.focus();
					return;
				}

				var records = [];
				var listbox = dialog.selector;
				var checked = listbox.getChecked();
				var name = listbox.name;
				var valueFromFields = this.getValueFromFields();

				for(var i = 0, length = checked.length; i < length; i++) {
					var record = this.newRecord();
					var checkedRecord = checked[i];
					record.set(name, checkedRecord.id);

					for(var j = 0, length1 = valueFromFields.length; j < length1; j++) {
						var valueFromField = valueFromFields[j];
						record.set(valueFromField.name, checkedRecord.get(valueFromField.valueFrom));
					}

					records.push(record);
				}

				this.createRecords(records);
			};

			var form = new Z8.Container({ cls: 'padding-15', items: [selector], height: '100%' });
			this.selector = new Z8.window.Window({ cls: 'air', header: this.query.text, icon: 'fa-plus-circle', autoClose: false, body: [form], selector: selector, handler: addRecordCallback, scope: this });
			this.selector.open();
		};

		selector.store.load({ fn: loadCallback, scope: this });
	},

	onCopyRecord: function(button) {
		this.copyRecord(this.getChecked()[0], button);
	},

	copyRecord: function(record, button) {
		var callback = function(record, success) {
			if(button != null)
				button.setBusy(false);
			if(success) {
				this.reloadRecord();
				this.store.insert(record, 0);
				this.select(record.id);
				if(!this.startEdit(record, 0))
					this.focus();
			}
		};

		if(button != null)
			button.setBusy(true);

		var newRecord = Z8.create(this.getModel());
		newRecord.copy(record, { fn: callback, scope: this }, { values: this.getValues() });
	},

	onBeforeLoad: function(store) {
		if(this.refreshTool != null)
			this.refreshTool.setBusy(true);
	},

	onLoad: function(store, records, success) {
		if(this.refreshTool != null)
			this.refreshTool.setBusy(false);
	},

	onRefresh: function(button) {
		button = button || this.refreshTool;

		if(button != null) {
			var callback = function(records, success) {
				button.setBusy(false);
				this.focus();
			};
			button.setBusy(true);
			this.store.load({ fn: callback, scope: this });
		} else
			this.store.load({ fn: callback, scope: this });
	},

	onRemoveRecord: function(button) {
		this.removeRecord(this.getDestroyable(), button);
	},

	removeRecord: function(records, button) {
		if(records.length == 0)
			throw 'Listbox.removeRecord: records.length == 0';

		var index = this.store.indexOf(records[0]);

		var callback = function(records, success) {
			if(button != null)
				button.setBusy(false);
			if(success) {
				this.reloadRecord();
				this.select(index);
				this.focus();
			}
		};

		if(button != null)
			button.setBusy(true);

		var batch = new Z8.data.Batch({ store: this.store });
		batch.destroy(records, { fn: callback, scope: this });
	},

	onPeriod: function(button, period, action) {
		if(action != Period.NoAction)
			this.store.setPeriod(period);
		this.onRefresh(this.periodTool);
	},
	
	getSelectedIds: function() {
		var records = [];
		var selected = this.getChecked();

		for(var i = 0, length = selected.length; i < length; i++) {
			var record = selected[i];
			if(!record.phantom)
				records.push(selected[i].id);
		}

		return records;
	},

	onMenuExportAs: function(menu, item) {
		var format = item.format;
		if(format == 'pdf')
			this.exportAsPdf();
		else if(format == 'xls')
			this.exportAsXls();
		else if(format == 'doc')
			this.exportAsDoc();
	},

	exportAsPdf: function() {
		var tool = this.exportTool;
		tool.setIcon('fa-file-pdf-o');
		tool.setTooltip('Сохранить как PDF');
		tool.format = 'pdf';
		this.exportAs();
	},

	exportAsXls: function() {
		var tool = this.exportTool;
		tool.setIcon('fa-file-excel-o');
		tool.setTooltip('Сохранить как XLS');
		tool.format = 'xls';
		this.exportAs();
	},

	exportAsDoc: function() {
		var tool = this.exportTool;
		tool.setIcon('fa-file-word-o');
		tool.setTooltip('Сохранить как DOC');
		tool.format = 'doc';
		this.exportAs();
	},

	exportAs: function() {
		var tool = this.exportTool;
		var format = tool.format;

		var columns = [];
		var table = this.table;
		var headers = this.getHeaders();

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			if(field != null && field.type != Type.Text)
				columns.push({ id: field.name, width: Ems.emsToPixels(header.getWidth()) });
		}

		var store = this.store;

		var params = {
			request: store.getModelName(),
			action: 'export',
			format: format,
			query: store.getQuery(),
			columns: columns,
			filter: store.getFilter(),
			quickFilter: store.getQuickFilter(),
			where: store.getWhere(),
			sort: store.getSorter(),
			period: store.getPeriod(),
			values: store.getValues()
		};

		var callback = function(response, success) {
			tool.setBusy(false);
		};

		tool.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });
		this.focus();
	},

	onAction: function(button) {
		button.setBusy(true);

		var action = button.action;

		if(!Z8.isEmpty(action.parameters))
			this.requestActionParameters(button);
		else
			this.runAction(button);
	},

	requestActionParameters: function(button) {
		this.runAction(button);
	},

	runAction: function(button) {
		var action = button.action;

		var record = this.record;
		var query = this.query;

		var params = {
			request: this.store.getModelName(),
			action: 'action',
			id: action.id,
			query: query.name,
			records: (record != null && !record.phantom) ? [record.id] : null,
			selection: this.getSelectedIds(),
			parameters: action.parameters
		};

		var callback = function(response, success) {
			button.setBusy(false);
			this.onActionComplete(button, record, response, success);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onActionComplete: function(button, record, response, success) {
		if(success && this.record == record) {
			var reloadCallback = function(record, success) {
				button.setBusy(false);
				if(success) {
					this.onRefresh();
					this.fireEvent('action', this, button.action);
				}
			};
			record.reload({ fn: reloadCallback, scope: this });
		}
	},

	onItemClick: function(list, item, index) {
		if(index != -1 && this.editable) {
			var field = this.fields[index];

			if(field.type != Type.Boolean || !field.isText || !field.editable)
				return;

			var record = item.record;

			if(record.isBusy())
				return;

			var name = field.name;

			record.beginEdit();

			record.set(name, !record.get(name));

			if(this.hasLink())
				record.set(this.getLink(), this.getRecordId());

			if(this.isDependent())
				record.set(this.getDependencyField(), this.getDependsOnValue());

			var callback = function(record, success) {
				success ? record.endEdit() : record.cancelEdit();
			}

			record.update({ fn: callback, scope: this }, { values: this.getValues() });
		}

		this.fireEvent('itemClick', this, item, index);
	},

	onItemDblClick: function(list, item, index) {
		this.fireEvent('itemDblClick', this, item, index);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(this.list.isEditing() || this.list.isFiltering())
			return;

		if(key == Event.ENTER || key == Event.F2) {
			if(this.startEdit(this.getSelection()))
				event.stopEvent();
		} else if(key == Event.INSERT) {
			if(!event.ctrlKey) {
				var addTool = this.addTool;
				if(addTool == null || !addTool.isEnabled())
					return;

				addTool.handler.call(addTool.scope, addTool);
				event.stopEvent();
			} else {
				var copyTool = this.copyTool;
				if(copyTool == null || !copyTool.isEnabled())
					return;

				copyTool.handler.call(copyTool.scope, copyTool);
				event.stopEvent();
			}
		} else if(key == Event.DELETE) {
			var removeTool = this.removeTool;
			if(removeTool == null || !removeTool.isEnabled())
				return;

			removeTool.handler.call(removeTool.scope, removeTool);
			event.stopEvent();
		} else if(key == Event.A && event.ctrlKey) {
			if(this.list.checkAll())
				event.stopEvent();
		} else if(key == Event.R && event.ctrlKey && !event.shiftKey) {
			this.onRefresh(this.refreshTool);
			event.stopEvent();
		}

	},

	onItemCheck: function(list, items) {
		this.updateTools();
	},

	onEdit: function(button) {
		Viewport.open(this.source.id);
	},

	onQuickFilter: function(button, toggled) {
		this.list.showQuickFilter(toggled, true);
	},

	onQuickFilterShow: function(list) {
		if(this.filterTool != null)
			this.filterTool.setToggled(true, true);
	},

	onQuickFilterHide: function(list) {
		if(this.filterTool != null)
			this.filterTool.setToggled(false, true);
	},

	onAutoFit: function(button, toggled) {
		this.list.setAutoFit(toggled, true);
		this.focus();
	},

	show: function() {
		this.callParent();
		this.list.adjustAutoFit();
	},

	onListAutoFit: function(button, toggled) {
		if(this.autoFitTool != null)
			this.autoFitTool.setToggled(toggled, true);
	},

	updateTools: function() {
		if(!this.hasTools || this.getDom() == null)
			return;

		var enabledNotReadOnly = this.isEnabled() && !this.isReadOnly();

		if(this.removeTool != null)
			this.removeTool.setEnabled(enabledNotReadOnly && !Z8.isEmpty(this.getDestroyable()));

		if(this.copyTool != null) {
			var selected = this.getChecked();
			this.copyTool.setEnabled(enabledNotReadOnly && !Z8.isEmpty(selected) && selected.length == 1);
		}

		if(this.addTool != null) {
			var record = this.getRecord();
			this.addTool.setEnabled(enabledNotReadOnly && record != null && (!this.isDependent() || this.hasDependsOnValue()));
		}
	},

	needsPager: function() {
		return this.pagerMode == 'visible';
	},

	updatePager: function() {
		var needsPager = this.needsPager();
		DOM.swapCls(this.pager, !needsPager, 'display-none');
		DOM.swapCls(this, needsPager, 'pager-on');
	}
});Z8.define('Z8.data.file.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.Guid({ name: 'id' }),
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.String({ name: 'path' }),
		new Z8.data.field.Integer({ name: 'size' }),
		new Z8.data.field.Datetime({ name: 'time' }),
		new Z8.data.field.String({ name: 'author' })
	]
});

Z8.define('Z8.form.field.Files', {
	extend: 'Z8.form.field.Listbox',

	tools: true,
	checks: true,

	fields: [
		{ name: 'name', header: 'Файл', type: Type.String, icon: 'fa-file-o' },
		{ name: 'size', header: 'Размер', type: Type.Integer, renderer: Format.fileSize },
		{ name: 'time', header: 'Дата', type: Type.Datetime, renderer: Format.dateOrTime, icon: 'fa-clock-o' },
		{ name: 'author', header: 'Автор', type: Type.String, icon: 'fa-user-o' }
	],

	initComponent: function() {
		this.store = new Z8.data.Store({ model: 'Z8.data.file.Model' });
		this.callParent();
	},

	setValue: function(value, displayValue) {
		this.callParent(value, displayValue);

		if(this.store != null)
			this.store.loadData(value);
	},

	afterRecordSet: function(record) {
	},

	createTools: function() {
		var upload = this.uploadTool = new Z8.button.Tool({ cls: 'btn-sm', icon: 'fa-upload', tooltip: 'Загрузить файл(ы)', handler: this.onUploadFile, scope: this });
		var download = this.downloadTool = new Z8.button.Tool({ cls: 'btn-sm', icon: 'fa-download', tooltip: 'Скачать файл(ы)', handler: this.onDownloadFile, scope: this });
		var remove = this.removeTool = new Z8.button.Tool({ cls: 'btn-sm remove', danger: true, icon: 'fa-trash', tooltip: 'Удалить файл(ы)', handler: this.onRemoveFile, scope: this });
		return [upload, download, remove];
	},

	completeRender: function() {
		this.callParent();

		var fileInput = this.fileInput = DOM.append(this, { tag: 'input', type: 'file', multiple: true });

		DOM.on(fileInput, 'change', this.onFileInputChange, this);

		DOM.on(this, 'dragEnter', this.onDragEnter, this);
		DOM.on(this, 'dragOver', this.onDragOver, this);
		DOM.on(this, 'drop', this.onDrop, this);

		DOM.on(window, 'dragEnter', this.onWindowDragEnter, this);
		DOM.on(window, 'dragOver', this.onWindowDragOver, this);
		DOM.on(window, 'dragLeave', this.onWindowDragLeave, this);
		DOM.on(window, 'drop', this.onWindowDrop, this);
	},

	onDestroy: function() {
		DOM.un(window, 'dragEnter', this.onWindowDragEnter, this);
		DOM.un(window, 'dragOver', this.onWindowDragOver, this);
		DOM.un(window, 'dragLeave', this.onWindowDragLeave, this);
		DOM.un(window, 'drop', this.onWindowDrop, this);

		DOM.un(this, 'dragEnter', this.onDragEnter, this);
		DOM.un(this, 'dragOver', this.onDragOver, this);
		DOM.un(this, 'drop', this.onDrop, this);

		DOM.un(this.fileInput, 'change', this.onFileInputChange, this);

		DOM.remove(this.fileInput);

		this.fileInput = null;

		this.callParent();
	},

	updateTools: function() {
		if(this.getDom() == null)
			return;

		var enabled = !this.isReadOnly() && this.isEnabled();

		this.uploadTool.setEnabled(enabled);

		enabled = enabled && this.getChecked().length != 0;
		this.downloadTool.setEnabled(enabled);
		this.removeTool.setEnabled(enabled);
	},

	onUploadFile: function(button) {
		this.fileInput.value = null;
		this.fileInput.click();
	},

	onRemoveFile: function(button) {
		var files = [];
		var selected = this.getChecked();

		for(var i = 0, length = selected.length; i < length; i++)
			files.push(selected[i].id);

		this.removeTool.setBusy(true);

		var callback = function(record, files, success) {
			this.removeTool.setBusy(false);
			if(success && this.getRecord() == record)
				this.setValue(files);
		};

		var record = this.getRecord();
		record.detach(this.name, files, { fn: callback, scope: this });
	},

	onDownloadFile: function(button) {
		var files = this.getChecked();
		var count = files.length;

		var callback = function(success) {
			if(--count == 0)
				this.downloadTool.setBusy(false);
		};

		this.downloadTool.setBusy(true);

		for(var i = 0, length = files.length; i < length; i++)
			DOM.download(HttpRequest.hostUrl() + '/' + files[i].get('path'), null, { fn: callback, scope: this });
	},

	onFileInputChange: function() {
		this.upload(this.fileInput.files);
	},

	onDragEnter: function(event) {
		var dataTransfer = event.dataTransfer;
		if(dataTransfer == null || this.isReadOnly() || !this.isEnabled())
				return;

		dataTransfer.effectAllowed = dataTransfer.dropEffect = 'copy';
		event.stopEvent();
	},

	onDragOver: function(event) {
		if(event.dataTransfer != null && !this.isReadOnly() && this.isEnabled())
			event.stopEvent();
	},

	onDrop: function(event) {
		var dataTransfer = event.dataTransfer;
		if(dataTransfer == null || this.isReadOnly() || !this.isEnabled())
			return;

		dataTransfer.effectAllowed = dataTransfer.dropEffect = 'copy';
		event.stopEvent();

		this.upload(dataTransfer.files);
	},

	onWindowDragEnter: function(event) {
	},

	onWindowDragLeave: function(event) {
	},

	onWindowDragOver: function(event) {
		var dataTransfer = event.dataTransfer;
		if(!event.dataTransfer)
			return;

		dataTransfer.effectAllowed = dataTransfer.dropEffect = 'none';
		event.stopEvent();
	},

	onWindowDrop: function(event) {
		if(event.dataTransfer != null)
			event.stopEvent();
	},

	upload: function(files) {
		if(files.length == 0)
			return;

		this.uploadTool.setBusy(true);

		var callback = function(record, files, success) {
			this.uploadTool.setBusy(false);
			if(success && this.getRecord() == record) {
				this.setValue(files);
				var file = files.last();
				this.select(file != null ? file.id : null);
				this.focus();
			}
		};

		var record = this.getRecord();
		record.attach(this.name, files, { fn: callback, scope: this });
	}
});Z8.define('Z8.form.field.Date', {
	extend: 'Z8.form.field.Text',

	triggers: [{ icon: 'fa-calendar' }],

	enterToOpen: true,
	enterOnce: false,

	format: 'd.m.Y',

	initComponent: function() {
		this.callParent();
	},

	setValue: function(value, dispayValue) {
		this.entered = true;

		value = String.isString(value) ? this.rawToValue(value) : value;
		this.callParent(value, dispayValue);
	},

	valueToRaw: function(value) {
		return Format.date(value, this.format);
	},

	rawToValue: function(value) {
		return Parser.datetime(value, this.format);
	},

	isEqual: function(value1, value2) {
		return Date.isEqual(value1, value2);
	},

	htmlMarkup: function() {
		var markup = this.callParent();

		var value = this.getValue();
		var dropdown = this.dropdown = new Z8.calendar.Dropdown({ date: value != null ? new Date(value) : null });
		dropdown.on('dayClick', this.setValue, this);
		dropdown.on('cancel', this.cancelDropdown, this);
		dropdown.on('show', this.onDropdownShow, this);
		dropdown.on('hide', this.onDropdownHide, this);

		markup.cn.push(dropdown.htmlMarkup());
		return markup;
	},

	subcomponents: function() {
		return this.callParent().add(this.dropdown);
	},

	completeRender: function() {
		this.callParent();
		this.dropdown.setAlignment(this.input);
	},

	select: function(direction) {
	},

	show: function() {
		this.entered = false;
		this.callParent();
	},

	showDropdown: function() {
		var dropdown = this.dropdown;

		var left = DOM.getOffsetLeft(this.input);
		DOM.setLeft(dropdown, left);

		this.dropdown.show();
	},

	hideDropdown: function() {
		this.dropdown.hide();
	},

	openDropdown: function() {
		var value = this.getValue();
		this.dropdown.set(value != null ? new Date(value) : null);
		this.showDropdown();
	},

	cancelDropdown: function() {
		this.hideDropdown();
	},

	toggleDropdown: function() {
		if(this.dropdown.visible)
			this.cancelDropdown();
		else
			this.openDropdown();
	},

	onDropdownShow: function() {
		this.getTrigger().rotateIcon(360);
	},

	onDropdownHide: function() {
		this.getTrigger().rotateIcon(0);
		DOM.focus(this.input);
	},

	onFocusOut: function(event) {
		if(!this.callParent(event))
			return false;

		this.cancelDropdown();
		return true;
	},

	onTriggerClick: function(trigger) {
		this.toggleDropdown();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		var editable = this.editable;
		var dropdown = this.dropdown;
		var dropdownOpen = dropdown.visible;

		var me = this;
		var isTrigger = function() {
			return DOM.isParentOf(me.trigger, target);
		};

		var isInput = function() {
			return DOM.isParentOf(me.input, target);
		};

		if(key == Event.DOWN)
			dropdownOpen ? dropdown.focus() : this.select('next');
		else if(key == Event.UP)
			dropdownOpen ? dropdown.focus() : this.select('previous');
		else if(key == Event.TAB && isTrigger()) {
			dropdownOpen ? dropdown.focus() : this.cancelDropdown();
			return false;
		} else if(key == Event.ENTER || key == Event.SPACE && isTrigger()) {
			if(dropdownOpen) {
				this.setValue(dropdown.date);
				this.cancelDropdown();
			} else if(this.enterToOpen && (!this.enterOnce || !this.entered)) {
				this.onTriggerClick(event, target);
				this.entered = true;
			} else if(event.ctrlKey)
				this.onTriggerClick(event, target);
			else
				return false;
		} else if(key == Event.ESC && dropdownOpen)
			this.cancelDropdown();
		else
			return false;

		return true;
	}
});Z8.define('Z8.form.field.Datetime', {
	extend: 'Z8.form.field.Date',

	format: 'd.m.Y H:i'
});
Z8.define('Z8.form.field.Search', {
	extend: 'Z8.form.field.Text',

	triggers: [{ icon: 'fa-search' }],

	searchPending: false,
	lastSearchValue: '',

	initComponent: function() {
		this.callParent();
	},

	isValid: function() {
		return true;
	},

	getValue: function() {
		return this.callParent() || '';
	},

	completeRender: function() {
		this.callParent();
		this.updateTrigger();
	},

	setBusy: function(busy) {
		this.getTrigger().setBusy(busy);
	},

	updateTrigger: function() {
		var trigger = this.getTrigger();

		if(trigger.isBusy())
			return;

		trigger.setIcon(this.searchPending || this.lastSearchValue == '' ? 'fa-search' : 'fa-times');
		trigger.setEnabled(this.searchPending || this.lastSearchValue != '');
	},

	onInput: function(event, target) {
		this.callParent(event, target);

		var value = this.getValue();

		this.searchPending = value != this.lastSearchValue;
		this.updateTrigger();
	},

	search: function() {
		if(!this.getTrigger().isEnabled())
			return;

		if(!this.searchPending)
			this.setValue('');

		var value = this.lastSearchValue = this.getValue();
		this.searchPending = false;
		this.updateTrigger();
		this.fireEvent('search', this, value);
	},

	clear: function() {
		if(!this.getTrigger().isEnabled())
			return false;

		this.setValue('');

		if(this.searchPending && this.lastSearchValue == '') {
			this.searchPending = false;
			this.updateTrigger();
		} else
			this.search();

		return true;
	},

	onTriggerClick: function(trigger) {
		this.search();
	},

	onKeyEvent: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER)
			this.search();
		else if(key == Event.ESC)
			return this.clear();
		else
			return false;

		return true;
	},

	reset: function() {
		this.callParent();
		this.searchPending = false;
		this.lastSearchValue = '';
		this.updateTrigger();
	},

	getFilter: function() {
		if(this.field == null)
			return null;

		var value = this.getValue();
		return !Z8.isEmpty(value) ? { property: this.field.name, operator: Operation.Contains, value: value } : null;
	}
});Z8.define('Z8.form.field.Filter', {
	extend: 'Z8.form.field.Control',

	tabIndex: -1,

	setValue: function(value) {
		if(value == this.getValue())
			return;

		this.callParent(value);

		if(this.expression != null) {
			this.expression.setExpression(value);
			this.updateTools();
		}
	},

	isEqual: function(value1, value2) {
		return JSON.encode(value1) == JSON.encode(value2);
	},

	subcomponents: function() {
		return this.callParent().add(this.expression);
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('filter-control');

		var label = this.label = this.label || {};

		var newLine = this.newLineButton = new Z8.button.Button({ tooltip: 'Новая строка', icon: 'fa-file-o', handler: this.newLine, scope: this });
		var group = this.groupButton = new Z8.button.Button({ tooltip: 'Группировать', icon: 'fa-link', handler: this.group, scope: this });
		var ungroup = this.ungroupButton = new Z8.button.Button({ tooltip: 'Разгруппировать', icon: 'fa-unlink', handler: this.ungroup, scope: this });
		var removeLine = this.removeLineButton = new Z8.button.Button({ tooltip: 'Удалить строки', cls: 'remove', icon: 'fa-trash', danger: true, handler: this.removeLine, scope: this });

		label.tools = new Z8.button.Group({ items: [newLine, group, ungroup, removeLine] });

		return this.callParent();
	},

	controlMarkup: function() {
		var expression = this.expression = new Z8.filter.Expression({ cls: 'control', fields: this.fields, expression: this.getValue() });
		expression.on('select', this.onExpressionSelect, this);
		expression.on('change', this.onExpressionChange, this);
		return [expression.htmlMarkup()];
	},

	completeRender: function() {
		this.callParent();
		this.updateTools();
	},

	focus: function() {
		return this.isEnabled() ? this.expression.focus() : false;
	},

	getSelection: function() {
		return this.expression.getSelection();
	},

	updateTools: function(selection) {
		var selection = selection || this.getSelection();
		this.newLineButton.setEnabled(this.isEnabled());
		this.removeLineButton.setEnabled(selection.length != 0);
		this.groupButton.setEnabled(selection != null && selection.length > 1);
		this.ungroupButton.setEnabled(this.getGroups(selection).length != 0);
	},

	onExpressionSelect: function(selection) {
		this.updateTools(selection);
	},

	onExpressionChange: function(value) {
		this.superclass.setValue.call(this, value);
		this.updateTools();
	},

	newLine: function() {
		var selection  = this.getSelection();
		var first = selection.length != 0 ? selection[0] : null;
		var container = first instanceof Z8.filter.Group ? first : (first != null ? first.container : this.expression);
		container.newLine();
	},

	removeLine: function() {
		var selection  = this.getSelection();
		var container = selection[0].container;
		container.removeLine(selection);
		container.focus();
	},

	group: function() {
		var selection = this.getSelection();
		var first = selection[0];
		var container = first.container;
		container.group(selection);
	},

	ungroup: function() {
		var groups = this.getGroups(this.getSelection());

		for(var i = 0, length = groups.length; i < length; i++)
			groups[i].ungroup();
	},

	getGroups: function(items) {
		var result = [];

		for(var i = 0, length = items.length; i < length; i++) {
			var item = items[i];
			if(item instanceof Z8.filter.Group)
				result.push(item);
		}

		return result;
	},

	getExpression: function() {
		return this.expression.getExpression();
	},

	getExpressionText: function() {
		return this.expression.getExpressionText();
	}
});
Z8.define('Z8.form.Fieldset', {
	extend: 'Z8.Component',
	shortClassName: 'Fieldset',

	statics: {
		MarginTop: .71428571,
		MarginBottom: .71428571,

		PaddingTop: 1.07142857,
		PaddingBottom: 0,

		Border: .07142857,

		extraHeight: function() {
			return Fieldset.MarginTop + Fieldset.MarginBottom + Fieldset.PaddingTop + Fieldset.PaddingBottom + 2 * Fieldset.Border;
		}
	},

	isFieldset: true,
	colCount: 1,

	constructor: function(config) {
		this.controls = [];
		this.readOnlyLock = config != null ? config.readOnly : false;
		this.callParent(config);
	},

	getControls: function() {
		return this.controls;
	},

	getControl: function(name) {
		return this.traverseControls(this.controls, name);
	},

	traverseControls: function(controls, name) {
		if(controls == null)
			return null;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.name == name || control.displayName == name)
				return control;
			var control = this.traverseControls(control.controls, name);
			if(control != null)
				return control;
		}

		return null;
	},

	getBoxMinHeight: function() {
		var minHeight = this.getMinHeight();
		return minHeight != 0 ? minHeight + (this.plain ? 0 : Fieldset.extraHeight()) : 0;
	},

	subcomponents: function() {
		return this.controls;
	},

	htmlMarkup: function() {
		this.setReadOnly(this.isReadOnly());

		if(this.icon != null) {
			var icon = DOM.parseCls(this.icon).pushIf('fa').join(' ');
			var icon = { tag: 'i', cls: icon };
		}

		var text = this.legend || '';
		var legend = { cls: 'legend', cn: icon != null ? [icon, text] : [text] };

		var rows = (this.plain ? [] : [legend]).concat(this.rowsMarkup());

		var cls = DOM.parseCls(this.cls).pushIf('fieldset', this.plain ? 'section' : '',
			!this.isEnabled() ? 'disabled' : '', this.isReadOnly() ? 'readonly' : '', this.flex ? 'flexed' : '').join(' ');

		return { id: this.getId(), cls: cls, cn: rows };
	},

	rowsMarkup: function() {
		var markup = [];

		var rows = this.getRows();

		var controls = this.controls;
		var rowsCount = rows.length;
		var columnWidth = Math.floor(12 / Math.max(1, this.colCount));

		var minHeight = 0;
		var unitHeight = Ems.UnitHeight;
		var unitSpacing = Ems.UnitSpacing;
		var defaultRowHeight = unitHeight + unitSpacing;

		var controlIndex = 0;

		for(var rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
			var cells = [];

			var columns = rows[rowIndex];

			var flex = 0;
			for(var columnIndex = 0, columnsCount = columns.length; columnIndex < columnsCount; columnIndex++)
				flex = Math.max(flex, controls[controlIndex].flex || 0);

			var colFlexCls = flex != 0 ? ' flexed' : '';

			var totalWidth = 0;
			var rowMinHeight = 0;

			for(var columnIndex = 0, columnsCount = columns.length; columnIndex < columnsCount; columnIndex++) {
				var cn = [];
				var control = controls[controlIndex];

				cn.push(control.htmlMarkup != null ? control.htmlMarkup() : control);

				rowMinHeight = Math.max(rowMinHeight, control.isComponent ? control.getBoxMinHeight() : 0);

				var width = Math.min(12, columns[columnIndex] * columnWidth);
				cells.push({ cls: 'col-lg-' + width + ' col-md-' + width + ' col-sm-' + width + ' cell' + colFlexCls, cn: cn });
				totalWidth += width;

				controlIndex++;
			}

			if(totalWidth < 12) {
				var padding = 12 - totalWidth;
				cells.push({ cls: 'col-lg-' + padding + ' col-md-' + padding + ' col-sm-' + padding + ' cell' + colFlexCls });
			}

			minHeight += rowMinHeight || defaultRowHeight;

			var cls = 'row' + (flex != 0 ? ' flex-' + flex : '');
			var row = { cls: cls, cn: cells };
			if(rowMinHeight != 0)
				row.style = 'min-height: ' + rowMinHeight + 'em';

			markup.push(row);
		}

		var actions = this.actions;

		if(actions != null) {
			markup.push(this.actionsMarkup());
			minHeight += defaultRowHeight;
		}

		if(this.minHeight !== false)
			this.minHeight = minHeight;

		return markup;
	},

	getRows: function() {
		var controls = this.controls;

		var row = [];
		var rows = [row];

		var colCount = this.colCount;

		var totalColSpan = 0;

		for(var i = 0, length = controls.length; i < length; i++) {
			var colSpan = controls[i].colSpan || 1;
			totalColSpan += colSpan;
			if(totalColSpan > colCount && i != 0) {
				row = [colSpan];
				totalColSpan = colSpan;
				rows.push(row);
			} else
				row.push(colSpan);
		}

		return rows[0].length != 0 ? rows : [];
	},

	actionsMarkup: function() {
		var actions = this.actions;
		var controls = this.controls;

		var markup = [];
		for(var i = 0, length = actions.length; i < length; i++) {
			var action = actions[i];
			var type = action.type;
			var button = new Z8.button.Button({ name: action.id, text: action.text, tooltip: action.description, icon: action.icon, action: action, primary: type == 'primary', success: type == 'success', danger: type == 'danger' });
			controls.push(button);
			markup.insert(button.htmlMarkup());
		}

		var cell = { cls: 'col-lg-12 col-md-12 col-sm-12 cell actions', cn: markup };
		return { cls: 'row', cn: [cell] };
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];

			if(!control.readOnlyLock && control.setReadOnly != null)
				control.setReadOnly(readOnly);
		}

		this.readOnly = readOnly;
		DOM.swapCls(this, readOnly, 'readonly');
	},

	focus: function() {
		var controls = this.controls;
		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];
			if(control.focus != null && control.focus())
				return true;
		}
		return false;
	}
});
Z8.define('Z8.form.Form', {
	extend: 'Z8.form.Fieldset',

	mixins: ['Z8.form.field.Field'],

	isForm: true,
	autoSave: false,

	fields: null,
	controls: null,

	fieldsMap: null,

	constructor: function(config) {
		config = config || {};
		if(config.name == null)
			config.cls = DOM.parseCls(config.cls).pushIf('form');
		this.callParent(config);
	},

	initComponent: function() {
		this.callParent();
		this.initControls();
		this.initFields();

		this.setReadOnly(this.readOnly);
	},

	setReadOnly: function(readOnly) {
		this.callParent(readOnly || this.record == null || !this.record.isEditable());
	},

	initControls: function() {
		var controls = this.controls;

		this.fields = [];
		this.fieldsMap = {};
		this.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], this);
	},

	getFields: function() {
		return this.fields;
	},

	getField: function(name) {
		return this.fieldsMap[name];
	},

	getQuery: function() {
		return this.query != null ? this.query.id : null;
	},

	initControl: function(control, container) {
		if(control.isForm || control instanceof Z8.form.Form) {
			control = this.initForm(control);
		} else if(control.isSection || control instanceof Z8.form.Fieldset) {
			control = this.initFieldset(control);
		} else if(control.isTabControl || control instanceof Z8.form.Tabs) {
			control = this.initTabControl(control);
		} else if(control instanceof Z8.form.field.Listbox) {
			this.addField(control);
		} else if(control.isListbox) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} else if(control instanceof Z8.form.field.Control) {
			this.addField(control);
		} else if(control.isText && control.isLink) {
			return;
		} else if(control.isText) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} else if(control.isCombobox) {
			control = Z8.form.Helper.createControl(control);
			this.addField(control);
		} // else control is a button or some other config - add it as is

		container.controls.push(control);
	},

	initForm: function(form) {
		if(form.isForm) {
			form.autoSave = true;
			form.plain = true;
			form.height = Ems.unitsToEms(form.height);
			var cls = Application.getSubclass(form.ui);
			form = cls != null ? Z8.create(cls, form) : new Z8.form.Form(form);
		}

		this.addField(form);

		var fields = form.fields;
		for(var i = 0, length = fields.length; i < length; i++)
			this.addField(fields[i]);

		return form;
	},

	initFieldset: function(fieldset) {
		var controls = fieldset.controls || [];

		if(fieldset.isSection)
			fieldset = Z8.form.Helper.createFieldset(fieldset);
		else
			fieldset.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], fieldset);

		return fieldset;
	},

	initTab: function(tab) {
		var controls = tab.controls || [];

		if(tab.isSection)
			tab = Z8.form.Helper.createTab(tab);
		else
			tab.controls = [];

		for(var i = 0, length = controls.length; i < length; i++)
			this.initControl(controls[i], tab);

		return tab;
	},

	initTabControl: function(tabControl) {
		var controls = [];
		var tabs = tabControl.tabs || [];

		for(var i = 0, length = tabs.length; i < length; i++) {
			var tab = this.initTab(tabs[i]);
			controls.push(tab);
		}

		return new Z8.form.Tabs({ name: tabControl.name, controls: controls, actions: tabControl.actions, colSpan: tabControl.colSpan, readOnly: tabControl.readOnly, flex: tabControl.flex, height: tabControl.height });
	},

	addField: function(field) {
		if(field.name == null)
			return;

		this.fields.push(field);
		this.fieldsMap[field.name] = field;
		if(field.displayName != null)
			this.fieldsMap[field.displayName] = field;
		if(field.form == null)
			field.form = this;
	},

	initAutoSave: function(field) {
		if(this.autoSave && !field.isListbox && !field.isForm && field.form == this) {
			field.setAutoSave(true);
			field.on('change', this.autoSaveCallback, this);
		}
	},

	initDependencies: function(field) {
		var dependencies = field.field != null ? field.field.dependencies : field.dependencies;

		if(dependencies == null || (!field.isCombobox && !field.isListbox && !field.isForm))
			return;

		field.dependencies = null;

		var fieldsMap = this.fieldsMap;
		for(var i = 0, length = dependencies.length; i < length; i++) {
			var dependentField = this.getField(dependencies[i]);
			if(dependentField != null)
				field.addDependency(dependentField);
		}
	},

	initFields: function() {
		var fields = this.fields;
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			this.initAutoSave(field);
			this.initDependencies(field);
		}
	},

	autoSaveCallback: function(control, newValue, oldValue) {
		if(!this.autoSave || (!control.isValid() && !control.isDependent()))
			return;

		var record = this.record;

		record.beginEdit();

		record.set(control.name, newValue);
		if(control.displayName != null)
			record.set(control.displayName, control.getDisplayValue());

		var params = {};
		var link = this.link;

		if(link != null) {
			params.link = link.name;
			var owner = link.primaryKey;
			var query = link.query.primaryKey;
			record.set(owner, record.get(owner), false, true);
			record.set(query, record.get(link.name), false, true);
		}

		var callback = function(record, success) {
			if(success) {
				this.applyRecordChange(record, control);
				if(control.isCombobox)
					control.updateDependencies(control.getSelectedRecord());
				record.endEdit();
			} else {
				control.initValue(control.originalValue, control.originalDisplayValue);
				record.cancelEdit();
			}
		};

		record.update({ fn: callback, scope: this}, params);
	},

	applyRecordChange: function(record, control) {
		var fields = this.getFields();
		var modified = record.getModifiedFields();

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var name = field.name;
			var displayName = field.displayName;

			if(field == control || !(name in modified || displayName in modified))
				continue;

			var value = record.get(name) || null;
			var displayValue = record != null ? record.get(displayName) : null;

			field.initValue(value, displayValue);
		}
	},

	isMyRecord: function(record) {
		if(!this.isDependent())
			return true;

		var query = this.getQuery();
		return record == null || query == null || query == record.getQuery();
	},

	setRecord: function(record) {
		if(!this.isMyRecord(record))
			return;

		var current = this.record;

		if(current != null)
			current.un('change', this.onRecordChange, this);

		this.record = record;

		this.updateDependencies(record);

		if(record != null)
			record.on('change', this.onRecordChange, this);
	},

	onDestroy: function() {
		var record = this.record;
		if(record != null)
			record.un('change', this.onRecordChange, this);
		this.callParent();
	},

	onRecordChange: function(record, modified) {
		this.applyRecordChange(record, null);

		for(var name in modified) {
			var control = this.getField(name);
			if(control != null && control.isCombobox)
				control.updateDependencies(control.getSelectedRecord());
		}
	},

	loadRecord: function(record) {
		var fields = this.fields;
		var current = this.record;

		this.setRecord(record);

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];

			var form = field.form;
			if(form != this && !form.isMyRecord(record))
				continue;

			if(record != null) {
				var value = record.get(field.name);
				var displayValue = field.displayName != null ? record.get(field.displayName) : null;
				field.initValue(value, displayValue);
			} else
				field.initValue(null, null);

			field.setRecord(record);
		}

		if(!this.readOnlyLock)
			this.setReadOnly(record == null || !record.isEditable());

		this.fireEvent('change', this, this.record, current);
	},

	onDependencyChange: function(record) {
		this.loadRecord(record);
	}
});
Z8.define('Z8.form.Helper', {
	statics: {
		storeConfig: function(field) {
			return Z8.query.Store.config(field);
		},

		createControl: function(field) {
			if(field == null)
				return null;

			var readOnly = field.readOnly;
			var label = field.label !== false ? { text: field.header, icon: field.icon, align: 'top' } : false;
			var config = { label: label, placeholder: field.header, name: field.name, field: field, colSpan: field.colSpan, flex: field.flex, readOnly: readOnly, editable: field.editable, enterOnce: field.enterOnce, source: field.source };

			if(field.format != null)
				config.format = field.format;

			var cls = Application.getSubclass(field.ui);

			if(field.isCombobox) {
				config.store = this.storeConfig(field);
				config.displayName = field.name;
				config.fields = field;
				config.name = field.link.name;
				config.required = field.required;
				config.checks = false;
				config.pagerMode = 'visible';
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Combobox(config);
			}

			if(field.isListbox) {
				var query = field.query;
				config.store = this.storeConfig(field);
				config.query = query;
				config.fields = query.columns;
				config.tools = true;
				config.pagerMode = 'visible';
				config.checks = true;
				config.locks = query.lockKey != null;
				config.totals = query.totals;
				config.minHeight = config.height = Ems.unitsToEms(field.height || 5);
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Listbox(config);
			}

			var type = field.type;

			if(type == Type.Boolean) {
				label.align = 'right';
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Checkbox(config);
			}

			if(type == Type.Integer)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Integer(config);

			if(type == Type.Float)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Float(config);

			if(type == Type.Date)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Date(config);

			if(type == Type.Datetime)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Datetime(config);

			if(type == Type.String)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Text(config);

			if(type == Type.Text) {
				config.minHeight = Ems.unitsToEms(field.height);
				return cls != null ? Z8.create(cls, config) : (field.html ? new Z8.form.field.Html(config) : new Z8.form.field.TextArea(config));
			}

			if(type == Type.Files) {
				config.height = Ems.unitsToEms(field.height || 3);
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Files(config);
			}

			if(type == Type.Guid)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Text(config);

			if(type == Type.Datespan)
				return cls != null ? Z8.create(cls, config) : new Z8.form.field.Text(config);

			throw 'Unknown server type "' + type + '"';
		},

		createFieldset: function(fieldset) {
			var config = {
				name: fieldset.name,
				actions: fieldset.actions,
				colCount: fieldset.colCount,
				colSpan: fieldset.colSpan,
				readOnly: fieldset.readOnly,
				legend: fieldset.legend,
				icon: fieldset.icon,
				plain: !fieldset.isFieldset,
				height: Ems.unitsToEms(fieldset.height),
				flex: fieldset.flex
			};
			var cls = Application.getSubclass(fieldset.ui);
			return cls != null ? Z8.create(cls, config) : new Z8.form.Fieldset(config);
		},

		createTab: function(tab) {
			var config = {
				name: tab.name,
				title: tab.header,
				actions: tab.actions,
				colCount: tab.colCount,
				colSpan: tab.colSpan,
				readOnly: tab.readOnly,
				icon: tab.icon,
				height: Ems.unitsToEms(tab.height),
				flex: tab.flex
			};
			var cls = Application.getSubclass(tab.ui);
			return cls != null ? Z8.create(cls, config) : new Z8.form.Tab(config);
		}
	}
});Z8.define('Z8.form.Tab', {
	extend: 'Z8.form.Fieldset',
	shortClassName: 'Tab',

	minHeight: false,
	plain: true,

	setText: function(text) {
		var tag = this.tag;
		if(tag != null)
			tag.setText(text);
	}
});Z8.define('Z8.form.Tabs', {
	extend: 'Z8.Container',
	shortClassName: 'Tabs',

	initComponent: function() {
		this.minHeight = this.height = this.height || Ems.unitsToEms(6);
		this.callParent();
	},

	htmlMarkup: function() {
		this.setReadOnly(this.isReadOnly());

		var controls = this.controls || [];

		this.cls = DOM.parseCls(this.cls).pushIf('tabs', this.flex ? 'flexed' : '');
		this.headerCls = DOM.parseCls(this.headerCls).pushIf('header');
		this.bodyCls = DOM.parseCls(this.bodyCls).pushIf('body');

		var callback = function(tag, toggled) {
			DOM.addCls(this.getActiveTab(), 'inactive');
			var active = this.activeTab = tag.tab;
			DOM.removeCls(active, 'inactive');
		};

		var tags = [];
		var items = [];

		for(var i = 0, length = controls.length; i < length; i++) {
			var tab = controls[i];
			tab.cls = DOM.parseCls(tab.cls).pushIf('tab', i != 0 ? 'inactive' : '').join(' ');

			var tag = tab.tag = new Z8.button.Button({ cls: 'tag', toggle: true, text: tab.title, icon: tab.icon, tab: tab });
			tab.icon = null;

			tag.on('toggle', callback, this);
			tags.push(tag);
		}

		var header = this.header = new Z8.button.Group({ radio: true, cls: this.headerCls.join(' '), items: tags });
		var body = new Z8.Container({ cls: this.bodyCls.join(' '), items: controls });

		this.items = [header, body];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();

		if(!Z8.isEmpty(this.controls))
			this.activateTab(this.controls[0]);
	},

	getActiveTab: function() {
		return this.activeTab;
	},

	showTab: function(tab, show) {
		DOM.swapCls(tab.tag, !show, 'display-none');
		if(!show && tab == this.getActiveTab())
			DOM.addCls(tab, 'inactive');
	},

	activateTab: function(tab) {
		tab.tag.setToggled(true);
	},

	isReadOnly: function() {
		return this.readOnly;
	},

	setReadOnly: function(readOnly) {
		var controls = this.controls;

		for(var i = 0, length = controls.length; i < length; i++) {
			var control = controls[i];

			if(!control.readOnlyLock && control.setReadOnly != null)
				control.setReadOnly(readOnly);
		}

		this.readOnly = readOnly;
		DOM.swapCls(this, readOnly, 'readonly');
	},

});Z8.define('Z8.menu.Item', {
	extend: 'Z8.list.Item',

	icon: 'fa-circle transparent',
	tabIndex: 0,

	useENTER: true
});
Z8.define('Z8.menu.Menu', {
	extend: 'Z8.list.Dropdown',

	cls: 'dropdown-menu display-none',
	itemType: 'Z8.menu.Item',
	manualItemsRendering: true,
	useTAB: true,
	headers: false,
	checks: false,

	activateItem: function() {},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'blur', this.onFocusOut, this, true);
	},

	onDestroy: function() {
		DOM.un(this, 'blur', this.onFocusOut, this, true);
		this.callParent();
	},

	onFocusOut: function(event) {
		var dom = DOM.get(this);
		var target = event.relatedTarget;

		if(dom != target && !DOM.isParentOf(dom, target) && !DOM.isParentOf(this.owner, target))
			this.onCancel();
	},

	toggle: function() {
		if(this.getCount() != 0)
			this.callParent();
	}
});Z8.define('Z8.pager.Pager', {
	extend: 'Z8.Container',

	initComponent: function() {
		this.callParent();

		var store = this.store;
		this.store = null;
		this.setStore(store);
	},

	setStore: function(store) {
		var currentStore = this.store;

		if(store == currentStore)
			return;

		if(currentStore != null) {
			currentStore.un('load', this.updateControls, this);
			currentStore.un('add', this.updateControls, this);
			currentStore.un('remove', this.updateControls, this);
			currentStore.un('beforeCount', this.onBeforeCount, this);
			currentStore.un('count', this.onCount, this);
			currentStore.dispose();
		}

		this.store = store;

		if(store != null) {
			store.use();
			store.on('load', this.updateControls, this);
			store.on('add', this.updateControls, this);
			store.on('remove', this.updateControls, this);
			store.on('count', this.updateControls, this);
		}

		this.updateControls();
	},

	htmlMarkup: function() {
		var first = this.first = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-double-left', cls: 'btn-tool', tooltip: 'To the First Page', handler: this.onFirst, scope: this });
		var previous = this.previous = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-left', cls: 'btn-tool', tooltip: 'To the Previous Page', handler: this.onPrevious, scope: this });

		var pageNumber = this.pageNumber = new Z8.form.field.Integer({ tabIndex: -1, inputCls: 'input-sm', tooltip: 'To the Page' });
		var pageTotals = this.pageTotals = new Z8.Component({ cls: 'totals', html: this.pageTotalsText() });

		var next = this.next = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-right', cls: 'btn-tool', tooltip: 'To the Next Page', handler: this.onNext, scope: this });
		var last = this.last = new Z8.button.Button({ tabIndex: -1, icon: 'fa-angle-double-right', cls: 'btn-tool', tooltip: 'To the Last Page', handler: this.onLast, scope: this });

		var paging = this.paging = new Z8.Container({ cls: 'paging', items: [first, previous, pageNumber, pageTotals, next, last] });
		var totals = this.totals = new Z8.Component({ cls: 'totals', html: this.totalsText() });

		this.items = [paging, totals];

		this.cls = DOM.parseCls(this.cls).pushIf('pager');

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		this.updateControls();

		DOM.on(this.pageNumber, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this.pageNumber, 'keyDown', this.onKeyDown, this);
		this.setStore(null);
		this.callParent();
	},

	getPage: function() {
		return this.store != null ? this.store.page : 0;
	},

	count: function() {
		return this.store != null ? this.store.getCount() : 0;
	},

	totalCount: function() {
		return this.store != null ? this.store.getTotalCount() : 0;
	},

	pageSize: function() {
		return this.store != null ? this.store.limit : 0;
	},

	pageCount: function() {
		var total = this.totalCount();
		var pageSize = this.pageSize();
		return pageSize != 0 ? Math.ceil(total / pageSize) : 1;
	},

	pageTotalsText: function() {
		var count = this.pageCount();
		return count != 0 ? 'из ' + Format.integer(count) : '';
	},

	totalsText: function() {
		var total = this.totalCount();

		if(total == 0)
			return 'Нет записей';

		var page = this.getPage();
		var pageSize = this.pageSize();
		var first = page * pageSize + 1;
		var last = Math.min(first + this.count() - 1, total);
		return Format.integer(first) + ' - ' + Format.integer(last) + ' из ' + Format.integer(total);
	},

	load: function(page, button) {
		var callback = function(store, records, success) {
			button.setBusy(false);
			this.updateControls();
		};

		button.setBusy(true);
		this.store.loadPage(page, { fn: callback, scope: this });
	},

	onFirst: function() {
		this.load(0, this.first);
	},

	onPrevious: function() {
		this.load(Math.max(this.getPage() - 1, 0), this.previous);
	},

	onNext: function() {
		this.load(Math.min(this.getPage() + 1, this.pageCount() - 1), this.next)
	},

	onLast: function() {
		this.load(Math.min(this.pageCount() - 1), this.last)
	},

	onBeforeCount: function(store) {
	},

	onCount: function(store) {
		this.updateControls();
	},

	updateControls: function() {
		if(this.dom == null)
			return;

		var page = this.getPage();

		var pageCount = this.pageCount();

		if(pageCount > 1) {
			this.first.setEnabled(pageCount != 0 && page != 0);
			this.previous.setEnabled(pageCount != 0 && page != 0);
			this.next.setEnabled(pageCount != 0 && page != pageCount - 1);
			this.last.setEnabled(pageCount != 0 && page != pageCount - 1);

			//this.pageNumber.setEnabled(false);
			this.pageNumber.setValue(pageCount != 0 ? page + 1 : '');
			this.pageTotals.setText(this.pageTotalsText());

			this.paging.show();
		} else
			this.paging.hide();

		this.totals.setText(this.totalsText());
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER) {
			var pageNumber = this.pageNumber;
			var page = pageNumber.getValue();
			page = Math.max(Math.min(page, this.pageCount()), 1) - 1;
			this.load(page, page < this.getPage() ? this.previous : this.next);
			event.stopEvent();
		}
	}
});
Z8.define('Z8.toolbar.Toolbar', {
	extend: 'Z8.Container',

	cls: 'toolbar',

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('toolbar');
		this.callParent();
	}
});Z8.define('Z8.window.Window', {
	extend: 'Z8.Container',
	shortClassName: 'Window',

	closable: true,
	autoClose: true,
	autoDestroy: true,
	isOpen: false,

	htmlMarkup: function() {
		var icon = { tag: 'i', cls: DOM.parseCls(this.icon).pushIf('icon', 'fa', 'fw-fa').join(' ') };
		var text = this.text = { cls: 'text', html: this.header };
		var dragger = { cls: 'dragger', cn: [icon, text] };

		var cn = [dragger];

		if(this.closable) {
			var close = this.closeButton = new Z8.button.Button({ cls: 'btn-tool', icon: 'fa-close', handler: this.cancel, scope: this });
			cn.push(close);
		}

		var header = this.header = new Z8.Container({ cls: 'header', items: cn });

		var body = this.body = new Z8.Container({ cls: 'body', items: this.body });

		var buttons = [];
		if(this.closable) {
			var cancel = this.cancelButton = new Z8.button.Button({ text: 'Отмена', handler: this.cancel, scope: this });
			buttons.push(cancel);
		}
		var ok = this.okButton = new Z8.button.Button({ text: 'Готово', primary: true, handler: this.ok, scope: this });
		buttons.push(ok);

		var footer = this.footer = new Z8.Container({ cls: 'footer', items: buttons });

		this.cls = DOM.parseCls(this.cls).pushIf('window');
		this.items = [header, body, footer];

		return this.callParent();
	},

	onDestroy: function() {
		this.close();
		this.callParent();
	},

	open: function() {
		if(this.isOpen)
			return;

		this.isOpen = true;

		if(this.getDom() == null) {
			var body = Viewport.getBody();
			body.items.push(this);

			this.mask = DOM.append(body, { cls: 'window-mask' }); 
			this.aligner = DOM.append(body, { cls: 'window-aligner' });
			this.render(this.aligner);
		} else {
			DOM.removeCls(this.mask, 'display-none');
			DOM.removeCls(this.aligner, 'display-none');
		}

		DOM.on(Viewport, 'mouseDown', this.monitorOuterClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		this.focus();
	},

	close: function() {
		if(!this.isOpen)
			return;

		this.isOpen = false;

		DOM.un(Viewport, 'mouseDown', this.monitorOuterClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);

		if(this.autoDestroy) {
			var body = Viewport.getBody();
			body.items.remove(this);

			DOM.remove(this.aligner);
			DOM.remove(this.mask);

			this.aligner = this.mask = null;

			if(!this.destroying)
				this.destroy();
		} else {
			DOM.addCls(this.mask, 'display-none');
			DOM.addCls(this.aligner, 'display-none');
		}
	},

	monitorOuterClick: function(event) {
		var dom = DOM.get(this);
		var target = event.target;

		if(dom != target && !DOM.isParentOf(dom, target))
			this.cancel();
	},

	ok: function() {
		this.notifyAndClose(true);
	},

	cancel: function() {
		if(this.closable)
			this.notifyAndClose(false);
	},

	setBusy: function(busy) {
		if(this.busyButton != null)
			this.busyButton.setBusy(busy);
	},

	notifyAndClose: function(success) {
		var busyButton = this.busyButton = success ? this.okButton : this.cancelButton;

		if(!this.autoClose && busyButton != null)
			busyButton.setBusy(true);

		if(this.handler != null)
			this.handler.call(this.scope, this, success);

		if(this.autoClose)
			this.close();
	},

	focus: function() {
		return this.isEnabled() ? (this.body.focus() ? true : DOM.focus(this)) : false;
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.cancel();
			event.stopEvent();
		} else if(key == Event.ENTER) {
			this.ok();
			event.stopEvent();
		}
	}
});Z8.define('Z8.filter.operator.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.String({ name: 'id' }),
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.String({ name: 'icon' }),
		new Z8.data.field.String({ name: 'type' })
	]
});

Z8.define('Z8.filter.String', {
	statics: {
		contains: function(string, what) {
			return string != null ? string.indexOf(what || '') != -1 : false;
		},

		notContains: function(string, what) {
			return string != null ? string.indexOf(what || '') == -1 : false;
		},

		eq: function(string, what) {
			return string != null ? string.toLowerCase() == (what || '').toLowerCase() : false;
		},

		notEq: function(string, what) {
			return string != null ? string.toLowerCase() != (what || '').toLowerCase() : false;
		},

		gt: function(string, what) {
			return string != null ? string > (what || '') : false;
		},

		ge: function(string, what) {
			return string != null ? string >= (what || '') : false;
		},

		lt: function(string, what) {
			return string != null ? string < (what || '') : false;
		},

		le: function(string, what) {
			return string != null ? string <= (what || '') : false;
		},

		beginsWith: function(string, what) {
			return string != null ? string.startsWith(what || '') : false;
		},

		notBeginsWith: function(string, what) {
			return string != null ? !string.startsWith(what || '') : false;
		},

		endsWith: function(string, what) {
			return string != null ? string.endsWith(what || '') : false;
		},

		notEndsWith: function(string, what) {
			return string != null ? !string.endsWith(what || '') : false;
		},

		isEmpty: function(string) { 
			return Z8.isEmpty(string);
		},

		isNotEmpty: function(string) { 
			return !Z8.isEmpty(string);
		}
	}
});

Z8.define('Z8.filter.Number', {
	statics: {
		eq: function(number, what) {
			return number == what;
		},

		notEq: function(string, what) {
			return number != what;
		},

		gt: function(string, what) {
			return number > what;
		},

		ge: function(string, what) {
			return number >= what;
		},

		lt: function(string, what) {
			return number < what;
		},

		le: function(string, what) {
			return number <= what;
		}
	}
});

Z8.define('Z8.filter.Operator', {
	shortClassName: 'Operator',

	statics: {
		data: {
			boolean: [
				{ id: Operation.isTrue, name: 'да', icon: 'fa-dot-circle-o' },
				{ id: Operation.isFalse, name: 'нет', icon: 'fa-circle-o' }
			],

			datetime: [
				{ id: Operation.GT, name: 'после', icon: 'fa-math-gt', type: Type.Datetime },
				{ id: Operation.GE, name: 'не ранее', icon: 'fa-math-ge', type: Type.Datetime },
				{ id: Operation.LT, name: 'до', icon: 'fa-math-lt', type: Type.Datetime },
				{ id: Operation.LE, name: 'не позже', icon: 'fa-math-le', type: Type.Datetime },

				{ id: Operation.Yesterday, name: 'вчера' },
				{ id: Operation.Today, name: 'сегодня' },
				{ id: Operation.Tomorrow, name: 'завтра' },

				{ id: Operation.LastWeek, name: 'прошлая неделя' },
				{ id: Operation.ThisWeek, name: 'текущая неделя' },
				{ id: Operation.NextWeek, name: 'следующая неделя' },

				{ id: Operation.LastMonth, name: 'прошлый месяц' },
				{ id: Operation.ThisMonth, name: 'текущий месяц' },
				{ id: Operation.NextMonth, name: 'следующий месяц' },

				{ id: Operation.LastYear, name: 'прошлый год' },
				{ id: Operation.ThisYear, name: 'текущий год' },
				{ id: Operation.NextYear, name: 'следующий год' },

				{ id: Operation.LastDays, name: 'последние X дней', type: Type.Integer },
				{ id: Operation.NextDays, name: 'следующие X дней', type: Type.Integer },

				{ id: Operation.LastHours, name: 'последние X часов', type: Type.Integer },
				{ id: Operation.NextHours, name: 'следующие X часов', type: Type.Integer }
			],

			date: [
				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.Date },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.Date },

				{ id: Operation.GT, name: 'после', icon: 'fa-math-gt', type: Type.Date },
				{ id: Operation.GE, name: 'не ранее', icon: 'fa-math-ge', type: Type.Date },
				{ id: Operation.LT, name: 'до', icon: 'fa-math-lt', type: Type.Date },
				{ id: Operation.LE, name: 'не позже', icon: 'fa-math-le', type: Type.Date },

				{ id: Operation.Yesterday, name: 'вчера' },
				{ id: Operation.Today, name: 'сегодня' },
				{ id: Operation.Tomorrow, name: 'завтра' },

				{ id: Operation.LastWeek, name: 'прошлая неделя' },
				{ id: Operation.ThisWeek, name: 'текущая неделя' },
				{ id: Operation.NextWeek, name: 'следующая неделя' },

				{ id: Operation.LastMonth, name: 'прошлый месяц' },
				{ id: Operation.ThisMonth, name: 'текущий месяц' },
				{ id: Operation.NextMonth, name: 'следующий месяц' },

				{ id: Operation.LastYear, name: 'прошлый год' },
				{ id: Operation.ThisYear, name: 'текущий год' },
				{ id: Operation.NextYear, name: 'следующий год' },

				{ id: Operation.LastDays, name: 'последние X дней', type: Type.Integer },
				{ id: Operation.NextDays, name: 'следующие X дней', type: Type.Integer },

				{ id: Operation.LastHours, name: 'последние X часов', type: Type.Integer },
				{ id: Operation.NextHours, name: 'следующие X часов', type: Type.Integer }
			],

			integer: [
				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.Integer },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.Integer },

				{ id: Operation.GT, name: 'больше', icon: 'fa-math-gt', type: Type.Integer },
				{ id: Operation.GE, name: 'не меньше', icon: 'fa-math-ge', type: Type.Integer },
				{ id: Operation.LT, name: 'меньше', icon: 'fa-math-lt', type: Type.Integer },
				{ id: Operation.LE, name: 'не больше', icon: 'fa-math-le', type: Type.Integer }
			],

			float: [
				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.Float },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.Float },

				{ id: Operation.GT, name: 'больше', icon: 'fa-math-gt', type: Type.Float },
				{ id: Operation.GE, name: 'не меньше', icon: 'fa-math-ge', type: Type.Float },
				{ id: Operation.LT, name: 'меньше', icon: 'fa-math-lt', type: Type.Float },
				{ id: Operation.LE, name: 'не больше', icon: 'fa-math-le', type: Type.Float }
			],

			string: [
				{ id: Operation.Contains, name: 'содержит', icon: 'fa-superset-of', type: Type.String, filter: Z8.filter.String.contains },
				{ id: Operation.NotContains, name: 'не содержит', icon: 'fa-not-superset-of', type: Type.String, filter: Z8.filter.String.notContains },

				{ id: Operation.Eq, name: 'равно', icon: 'fa-math-eq', type: Type.String, filter: Z8.filter.String.eq },
				{ id: Operation.NotEq, name: 'не равно', icon: 'fa-math-not-eq', type: Type.String, filter: Z8.filter.String.notEq },

				{ id: Operation.GT, name: 'больше', icon: 'fa-math-gt', type: Type.String },
				{ id: Operation.GE, name: 'больше или равно', icon: 'fa-math-ge', type: Type.String },
				{ id: Operation.LT, name: 'меньше', icon: 'fa-math-lt', type: Type.String },
				{ id: Operation.LE, name: 'меньше или равно', icon: 'fa-math-le', type: Type.String },

				{ id: Operation.BeginsWith, name: 'начинается с', type: Type.String },
				{ id: Operation.NotBeginsWith, name: 'не начинается с', type: Type.String },
				{ id: Operation.EndsWith, name: 'оканчивается на', type: Type.String },
				{ id: Operation.NotEndsWith, name: 'не оканчивается на', type: Type.String },

				{ id: Operation.IsEmpty, name: 'пустая строка' },
				{ id: Operation.IsNotEmpty, name: 'непустая строка' }
			]
		},

		filters: null,

		fields: ['id', 'name', 'icon', 'type'],

		getOperators: function(type) {
			switch(type) {
			case Type.String:
			case Type.Text:
			case Type.Guid:
				if(this.string == null) {
					this.string = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.string });
					this.string.use();
				}
				return this.string;
			case Type.Boolean:
				if(this.boolean == null) {
					this.boolean = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.boolean });
					this.boolean.use();
				}
				return this.boolean;
			case Type.Date:
				if(this.date == null) {
					this.date = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.date });
					this.date.use();
				}
				return this.date;
			case Type.Datetime:
				if(this.datetime == null) {
					this.datetime = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.datetime });
					this.datetime.use();
				}
				return this.datetime;
			case Type.Integer:
				if(this.integer == null) {
					this.integer = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.integer });
					this.integer.use();
				}
				return this.integer;
			case Type.Float:
				if(this.float == null) {
					this.float = new Z8.data.Store({ model: 'Z8.filter.operator.Model', data: this.data.float });
					this.float.use();
				}
				return this.float;
			default:
				throw 'Unknown type: ' + type;
			}
		},

		getFilters: function() {
			if(this.filters != null)
				return this.filters;

			var filters = this.filters = {};

			filters.string = this.getStringFilters();
			filters.integer = this.getNumberFilters();
			filters.float = this.getNumberFilters();

			return filters;
		},

		getStringFilters: function() {
			var filters = {};

			filters[Operation.Contains] = Z8.filter.String.contains;
			filters[Operation.NotContains] = Z8.filter.String.notContains;

			filters[Operation.Eq] = Z8.filter.String.eq;
			filters[Operation.NotEq] = Z8.filter.String.notEq;

			filters[Operation.GT] = Z8.filter.String.gt;
			filters[Operation.GE] = Z8.filter.String.ge;
			filters[Operation.LT] = Z8.filter.String.lt;
			filters[Operation.LE] = Z8.filter.String.le;
			filters[Operation.Eq] = Z8.filter.String.eq;

			filters[Operation.BeginsWith] = Z8.filter.String.beginsWidth;
			filters[Operation.NotBeginsWith] = Z8.filter.String.notBeginsWidth;
			filters[Operation.EndsWith] = Z8.filter.String.endsWidth;
			filters[Operation.NotEndsWith] = Z8.filter.String.notEndsWidth;

			filters[Operation.IsEmpty] = Z8.filter.String.isEmpty;
			filters[Operation.IsNotEmpty] = Z8.filter.String.isNotEmpty;

			return filters;
		},

		getNumberFilters: function() {
			var filters = {};

			filters[Operation.Eq] = Z8.filter.Number.eq;
			filters[Operation.NotEq] = Z8.filter.Number.notEq;

			filters[Operation.GT] = Z8.filter.Number.gt;
			filters[Operation.GE] = Z8.filter.Number.ge;
			filters[Operation.LT] = Z8.filter.Number.lt;
			filters[Operation.LE] = Z8.filter.Number.le;
			filters[Operation.Eq] = Z8.filter.Number.eq;

			return filters;
		},

		applyFilter: function(record, filter) {
			var property = filter.property;
			var operator = filter.operator;
			var parameter = filter.value;

			if(Z8.isEmpty(property) || Z8.isEmpty(operator))
				return true;

			var field = record.getField(property);
			if(field == null)
				return true;

			var type = field.getType();

			var filters = this.getFilters()[type];
			if(filters == null)
				return true;

			var filterFn = filters[operator];
			return filterFn != null ? filterFn(record.get(property), field.convert(parameter)) : true;
		}
	}
});Z8.define('Z8.filter.Element', {
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
Z8.define('Z8.filter.Line', {
	extend: 'Z8.filter.Element',

	cls: 'line',

	htmlMarkup: function() {
		var checkbox = this.checkbox = new Z8.form.field.Checkbox();
		checkbox.on('change', this.onChecked, this);

		var expression = this.expression || {};

		var store = this.getFields();
		var record = store.getById(expression.property);
		var fields = [{ name: 'name', header: 'Поле', icon: 'fa-tag', sortable: false, width: 200 }, { name: 'type', header: 'Тип', icon: 'fa-code', sortable: false, width: 100 }, { name: 'description', header: 'Описание', icon: 'fa-file-text-o', sortable: false, width: 400 }];
		var property = this.property = new Z8.form.field.Combobox({ store: store, value: expression.property, emptyValue: '', fields: fields, required: true, filters: false, cls: 'property', placeholder: 'Поле', icons: true });
		property.on('change', this.propertyChanged, this);

		var type = record != null ? record.get('type') : null;
		store = type != null ? Operator.getOperators(type) : null;
		record = store != null ? store.getById(expression.operator) : null;

		var operator = this.operator = new Z8.form.field.Combobox({ store: store, value: expression.operator, emptyValue: '', cls: 'operator', required: true, filters: false, placeholder: 'Операция' });
		operator.on('change', this.operatorChanged, this);

		type = record != null ? record.get('type') : null;
		var value = this.value = this.getValueEditor(type, expression.value);

		this.items = [checkbox, property, operator, value];
		return this.callParent();
	},

	focus: function() {
		return this.isEnabled() ? this.property.focus() : false;
	},

	isSelected: function() {
		return this.checkbox.getValue();
	},

	onChecked: function(checkbox, newValue, oldValue) {
		this.toggle(newValue);
	},

	toggle: function(toggle) {
		this.checkbox.initValue(toggle);
		this.callParent(toggle);
	},

	getValueEditor: function(type, value) {
		var editor = this.value;

		if(editor != null && editor.type == type)
			return editor;

		editor = this.createValueEditor(type || null, value);
		editor.on('change', this.valueChanged, this);
		editor.type = type;
		return editor;
	},

	createValueEditor: function(type, value) {
		switch(type) {
		case null:
			return new Z8.Component({ cls: 'value hidden' });
		case Type.String:
		case Type.Text:
			return new Z8.form.field.Text({ value: value, cls: 'value', placeholder: 'текст', required: true });
		case Type.Date:
			return new Z8.form.field.Date({ value: value, cls: 'value', placeholder: 'дата', required: true});
		case Type.Datetime:
			return new Z8.form.field.Datetime({ value: value, cls: 'value', placeholder: 'время', required: true});
		case Type.Integer:
			return new Z8.form.field.Integer({ value: value, cls: 'value', placeholder: '0 000', required: true});
		case Type.Float:
			return new Z8.form.field.Float({ value: value, cls: 'value', placeholder: '0 000,00', required: true});
		default:
			throw 'Unsupported type: ' + type;
		}
	},

	propertyChanged: function(combobox, newValue, oldValue) {
		var record = combobox.store.getById(newValue);
		var type = record != null ? record.get('type') : null;
		var store = type != null ? Operator.getOperators(type) : null;
		this.operator.setStore(store);
		this.operator.setValue(this.emptyValue, '');
		this.onChildChange(this);
	},

	operatorChanged: function(combobox, newValue, oldValue) {
		var store = combobox.store;
		var record = store != null? store.getById(newValue) : null;
		var type = record != null ? record.get('type') : null;
		var editor = this.getValueEditor(type);
		if(editor != this.value) {
			this.remove(this.value);
			this.value = editor;
			this.add(this.value);
		}

		this.onChildChange(this);
	},

	valueChanged: function(combobox, newValue, oldValue) {
		this.onChildChange(this);
	},

	getExpression: function() {
		var property = this.property.getValue();
		if(Z8.isEmpty(property))
			return {};

		var operator = this.operator.getValue();
		if(Z8.isEmpty(operator))
			return { property: property };

		var value = this.value instanceof Z8.form.field.Control ? this.value.getValue() : null;

		return { property: property, operator: operator, value: value || undefined };
	},

	getExpressionText: function() {
		var property = this.property.getDisplayValue();
		if(Z8.isEmpty(property))
			return null;

		var operator = this.operator.getDisplayValue();
		if(Z8.isEmpty(operator))
			return null;

		var needsValue = this.value instanceof Z8.form.field.Control;
		var value = needsValue ? this.value.getRawValue() || '' : null;

		return '\'' + property + '\' ' + operator + (needsValue ? ' \'' + value + '\'' : '');
	},
});
Z8.define('Z8.filter.Group', {
	extend: 'Z8.filter.Element',

	button: true,

	isGroup: true,
	logical: 'or',

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('group');
		this.initExpression(this.expression);
		this.callParent();
	},

	initExpression: function(expression) {
		expression = this.expression = expression || { logical: this.logical, expressions: [] };
		this.logical = expression.logical;
	},

	getExpression: function() {
		var result = { logical: this.logical, expressions: [] };

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++) {
			expression = lines[i].getExpression();
			if(expression != null)
				result.expressions.push(expression);
		}

		return result.expressions.length != 0 ? result : null;
	},

	setExpression: function(expression) {
		this.removeAll();
		this.initExpression(expression);
		this.add(this.createItems());
	},

	createItems: function() {
		var items = [];

		var expression = this.expression;

		if(this.button) {
			var menuItems = [
				new Z8.menu.Item({ text: 'Новая строка', icon: 'fa-plus', handler: this.onNewLine }),
				new Z8.menu.Item({ text: 'Разгруппировать', icon: 'fa-unlink', handler: this.onUngroup }),
				new Z8.menu.Item({ text: 'Изменить и/или', icon: 'fa-exchange', handler: this.onChangeAndOr }),
				'-'
			];

			var deleteItem = this.deleteItem = new Z8.menu.Item({ text: 'Delete', icon: 'fa-trash', handler: this.onDeleteLine })
			menuItems.push(deleteItem);

			var menu = new Z8.menu.Menu({ items: menuItems });
			var menuItemCallback = function(menu, item) {
				item.handler.call(this);
			};
			menu.on('itemClick', menuItemCallback, this);

			var button = this.button = new Z8.button.Button({ icon: 'fa-square-o', text: this.getOperatorText(), toggled: false, menu: menu, vertical: true });
			button.on('toggle', this.onToggle, this);

			items.push(button);
		}

		var expressions = expression.expressions || [];
		for(var i = 0, length = expressions.length; i < length; i++) {
			var expression = expressions[i];
			var line = expression.logical != null ? new Z8.filter.Group({ expression: expression }) : new Z8.filter.Line({ expression: expression });
			items.push(line);
		}

		return items;
	},

	htmlMarkup: function() {
		this.items = this.createItems();
		return this.callParent();
	},

	getLines: function() {
		return this.button ? this.items.slice(1) : this.items;
	},

	getCount: function() {
		return this.items.length - this.button ? 1 : 0;
	},

	focus: function() {
		if(!this.isEnabled())
			return false;

		var lines = this.getLines();
		return lines.length != 0 ? lines[0].focus() : false;
	},

	getSelection: function() {
		var result = [];

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++) {
			var line = lines[i];
			if(line.isSelected())
				result.push(line);
		}

		return result;
	},

	isSelected: function() {
		return this.button ? this.button.toggled : false;
	},

	onToggle: function(button, toggled) {
		this.toggle(toggled);
	},

	toggle: function(toggle) {
		if(this.button) {
			this.button.setToggled(toggle, true);
			this.button.setIcon(toggle ? 'fa-check-square' : 'fa-square-o');
		}

		this.callParent(toggle);
	},

	onContainerToggle: function(toggle, container) {
		this.callParent(toggle, container);

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++)
			lines[i].onContainerToggle(toggle, container);
	},

	onChildToggle:  function(toggle, child) {
		this.callParent(toggle, child);
		this.deleteItem.setEnabled(this.getSelection().length != 0);
	},

	onNewLine: function() {
		this.newLine();
	},

	onUngroup: function() {
		this.ungroup();
	},

	onChangeAndOr: function() {
		this.changeAndOr();
	},

	onDeleteLine: function() {
		this.removeLine(this.getSelection());
	},

	newLine: function() {
		var line = new Z8.filter.Line();
		this.add(line);
		line.focus();

		this.onChildChange(this);
	},

	removeLine: function(lines) {
		var lines = Array.isArray(lines) ? lines : [lines];

		for(var i = 0, length = lines.length; i < length; i++)
			this.remove(lines[i]);

		lines = this.getLines();

		if(lines.length == 0) {
			var container = this.container;
			if(container instanceof Z8.filter.Element)
				container.removeLine(this);
		} else if(lines.length == 1 && this.button)
			this.ungroup();

		this.onChildChange(this);
	},

	group: function(lines) {
		var group = this.add(new Z8.filter.Group(), this.indexOf(lines[0]));
		group.add(lines);
		this.onChildChange(this);
	},

	ungroup: function() {
		var container = this.container;
		var index = container.indexOf(this);

		var lines = this.getLines();
		this.container.add(lines, index);

		this.container.remove(this);

		this.onChildChange(this);
	},

	changeAndOr: function() {
		this.logical = this.logical == 'or' ? 'and' : 'or';
		this.button.setText(this.getOperatorText());
		this.onChildChange(this);
	},

	getOperatorText: function() {
		return this.logical == 'or' ? 'или' : 'и';
	},

	getExpressionText: function() {
		var expressions = [];

		var lines = this.getLines();
		for(var i = 0, length = lines.length; i < length; i++) {
			var line = lines[i];
			var text = line.getExpressionText();
			if(text == null)
				continue;
			var parenthesis = line.isGroup && this.logical == 'and' && line.logical == 'or';
			expressions.push({ parenthesis: parenthesis, text: text});
		}

		if(expressions.length == 0)
			return null;

		if(expressions.length == 1)
			return expressions[0].text;

		var text = '';
		var operator = this.getOperatorText();
		for(var i = 0, length = expressions.length; i < length; i++) {
			var expression = expressions[i];
			expression = expression.parenthesis ? '(' + expression.text + ')' : expression.text;
			text += (text != '' ? ' ' + operator + ' ' : '') + expression;
		}

		return text;
	}
});
Z8.define('Z8.filter.fields.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.String({ name: 'id' }),
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.String({ name: 'icon' }),
		new Z8.data.field.String({ name: 'type' }),
		new Z8.data.field.String({ name: 'description' })
	]
});

Z8.define('Z8.filter.Expression', {
	extend: 'Z8.filter.Group',

	button: false,

	logical: 'and',

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('expression');
		this.expression = this.expression || { logical: 'and', expressions: [] };
		return this.callParent();
	},

	onDestroy: function() {
		var fields = this.fields;
		if(fields != null && fields.isStore) {
			fields.dispose();
			this.fields = null;
		}
		this.callParent();
	},

	getFields: function() {
		var fields = this.fields || [];

		if(!Array.isArray(fields))
			return this.fields;

		var data = [];

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(!field.isPrimaryKey && !field.isParentKey && !field.isLink)
				data.push({ id: field.name, name: field.header || field.name, icon: field.icon, type: field.type, description: field.descripton });
		}

		var fields = this.fields = new Z8.data.Store({ model: 'Z8.filter.fields.Model', data: data });
		fields.use();
		return fields;
	},

	onChildToggle: function(toggle, child) {
		this.onContainerToggle(toggle, child.container);
		this.fireEvent('select', this.getSelection(), this);
	},

	onChildChange: function(child) {
		this.fireEvent('change', this.getExpression(), this);
	},

	getSelection: function() {
		var result = this.callParent();
		return result.length != 0 ? result : this.getSelectedLines(this.getLines());
	},

	getSelectedLines: function(lines) {
		var result = [];

		for(var i = 0, length = lines.length; i < length; i++) {
			var line = lines[i];
			if(!line.isSelected()) {
				if(line instanceof Z8.filter.Group) {
					var selection = this.getSelectedLines(line.getLines());
					if(selection.length != 0)
						return selection;
				}
			} else
				result.push(line);
		}
		return result;
	}
});
Z8.define('Z8.filter.Button', {
	extend: 'Z8.button.Button',

	toggle: true,
	icon: 'fa-filter', 

	tooltip: 'Фильтрация',
	triggerTooltip: 'Настроить фильтрацию',

	filter: null,
	fields: null,

	htmlMarkup: function() {
		this.filterItems = [];

		this.on('toggle', this.onToggle, this);

		var items = [ new Z8.menu.Item({ text: 'Настроить', icon: 'fa-filter' }) ];
		var menu = this.menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuItemClick, this);

		this.init();

		return this.callParent();
	},

	init: function() {
		var filter = this.filter;
		var current = filter.current;

		this.setText(filter.current);
		this.setToggled(filter.isActive(), true);

		var menu = this.menu;
		var filterItems = this.filterItems;
		menu.removeItems(filterItems);
		filterItems = this.filterItems = [];

		var names = filter.getNames();
		for(var i = 0, length = names.length; i < length; i++) {
			var name = names[i];
			var isCurrent = name == current;
			var item = new Z8.menu.Item({ text: name, filter: name, icon: isCurrent ? 'fa-check-square' : '' });
			if(isCurrent)
				this.currentItem = item;
			filterItems.add(item);
		}

		if(filterItems.length != 0)
			filterItems.add(new Z8.list.Divider());

		menu.addItems(filterItems, 0);
	},

	setFilter: function(filter) {
		this.filter = filter;
		this.init();
	},

	onMenuItemClick: function(menu, item) {
		var name = item.filter;

		if(name != null) {
			var filter = this.filter;
			filter.setCurrent(name);
			filter.setActive(true);

			if(this.currentItem != null)
				this.currentItem.setIcon('');

			item.setIcon('fa-check-square');
			this.currentItem = item;

			this.setText(name);
			this.setToggled(true, true);

			this.fireEvent('filter', this, filter, Filter.Apply);
		} else
			this.onSettings();
	},

	onSettings: function() {
		var callback = function(dialog, success) {
			if(success) {
				var filter = dialog.getFilter();
				this.setFilter(filter);
				this.fireEvent('filter', this, filter, Filter.Apply);
			} else
				this.fireEvent('filter', this, this.filter, Filter.NoAction);
		};

		new Z8.filter.Dialog({ cls: 'air', header: 'Настройка фильтрации', icon: 'fa-filter', fields: this.fields, filter: this.filter, handler: callback, scope: this }).open();
	},

	onToggle: function(button, toggled) {
		var filter = this.filter;

		if(!toggled || !filter.isEmpty()) {
			filter.setActive(toggled);
			this.fireEvent('filter', this, filter, toggled ? Filter.Apply : Filter.Clear);
		} else {
			this.setToggled(false, true);
			this.onSettings();
		}
	}
});Z8.define('Z8.filter.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'name',

	fields: [ 
		new Z8.data.field.String({ name: 'name', header: 'Фильтр', editable: true }),
		new Z8.data.field.Json({ name: 'filter' })
	]
});

Z8.define('Z8.filter.Editor', {
	extend: 'Z8.Container',

	cls: 'filter-editor',

	htmlMarkup: function() {
		var store = this.store = new Z8.data.Store({ model: 'Z8.filter.Model', data: this.filter.toStoreData() });
		store.use();

		var addTool = this.addTool = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-file-o', tooltip: 'Новый фильтр', handler: this.onAddFilter, scope: this });
		var copyTool = this.copyTool = new Z8.button.Tool({ icon: 'fa-copy', tooltip: 'Копировать запись (Shift+Insert)', handler: this.onCopyFilter, scope: this });
		var removeTool = this.removeTool = new Z8.button.Button({ cls: 'btn-sm remove', danger: true, icon: 'fa-trash', tooltip: 'Удалить фильтр(ы)', handler: this.onRemoveFilter, scope: this });
		var tools = new Z8.button.Group({ items: [addTool, copyTool, removeTool] });

		var label = { text: 'Фильтры', icon: 'fa-filter', tools: tools};

		var filters = this.filters = new Z8.form.field.Listbox({ store: store, cls: 'filters', fields: [store.getField('name')], label: label, editable: true });
		filters.on('select', this.onFiltersSelect, this);
		filters.on('itemEditorChange', this.onItemEditorChange, this);

		filters.setAddTool(addTool);
		filters.setRemoveTool(removeTool);
		filters.setCopyTool(copyTool);

		var expression = this.expression = new Z8.form.field.Filter({ fields: this.fields, enabled: false, label: { text: 'Выражение', icon: 'fa-list-ul' } });
		expression.on('change', this.expressionChanged, this);
		var expressionText = this.expressionText = new Z8.form.field.Html({ cls: 'text', label: { text: 'Текст', icon: 'fa-code' } });

		var container = new Z8.Container({ cls: 'filter', items: [expression, expressionText] });
	
		this.items = [filters, container];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		this.updateTools();
	},

	onDestroy: function() {
		if(this.store != null) {
			this.store.dispose();
			this.store = null;
		}

		this.callParent();
	},

	getChecked: function() {
		return this.filters.getChecked();
	},

	getSelection: function() {
		return this.filters.getSelection();
	},

	updateTools: function() {
		var selection = this.getChecked();
		this.removeTool.setEnabled(selection.length != 0);

		selection = this.getSelection();
		this.copyTool.setEnabled(selection != null);
	},

	getFilter: function() {
		var filter = this.filter;
		filter.clear();

		var records = this.store.getRecords();
		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			filter.add(record.id, record.get('filter'));
		}

		var record = this.getSelection();
		filter.setActive(record != null);
		filter.setCurrent(record != null ? record.id : null);
		return filter;
	},

	onFiltersSelect: function(listbox, record) {
		var expression = this.expression;
		var expressionText = this.expressionText;

		expression.setEnabled(record != null);
		expressionText.setEnabled(record != null);

		var value = record != null ? record.get('filter') : null;

		if(value != expression.getValue()) {
			expression.initValue(value);
			expressionText.initValue(expression.getExpressionText());
		}

		this.updateTools();
	},

	onItemEditorChange: function(listbox, editor, newValue, oldValue) {
		var editedRecord = editor.record;

		var records = this.store.getRecords();

		for(var i = 0, length = records.length; i < length; i++) {
			var record = records[i];
			if(record != editedRecord && newValue == record.get('name')) {
				editor.setValid(false);
				return;
			}
		}

		editor.setValid(true);
	},

	expressionChanged: function(control, newValue, oldValue) {
		var selected = this.getSelection();
		selected.set('filter', this.expression.getExpression());
		this.expressionText.setValue(this.expression.getExpressionText());
	},

	onAddFilter: function(button) {
		var name = this.newFilterName();

		var record = new Z8.filter.Model({ name: name, filter: null });
		this.store.add(record);

		var filters = this.filters;
		filters.select(record);

		if(!filters.startEdit(record, 0))
			filters.focus();
	},

	onCopyFilter: function(button) {
		var record = this.getSelection();
		var name = this.newFilterName(record.id);
		var filter = JSON.decode(JSON.encode(record.get('filter')));

		var record = new Z8.filter.Model({ name: name, filter: filter });
		this.store.add(record);

		var filters = this.filters;
		filters.select(record);

		if(!filters.startEdit(record, 0))
			filters.focus();
	},

	onRemoveFilter: function(button) {
		var records = this.filters.getChecked();

		if(records.length == 0)
			throw 'records.length == 0';

		var store = this.store;
		var index = store.indexOf(records[0]);
		store.remove(records);

		var filters = this.filters;
		filters.select(index);
		filters.focus();
	},

	newFilterName: function(template) {
		template = (template || 'Фильтр') + ' ';

		var store = this.store;
		var names = store.getOrdinals();

		var index = 1;
		while(true) {
			var name = template + index;
			if(names[name] == null)
				return name;
			index++;
		}
	}
});
Z8.define('Z8.filter.Dialog', {
	extend: 'Z8.window.Window',

	htmlMarkup: function() {
		var editor = this.editor = new Z8.filter.Editor({ filter: this.filter, fields: this.fields });
		this.body = [editor];
		return this.callParent();
	},

	getFilter: function() {
		return this.editor.getFilter();
	}
});
Z8.define('Z8.application.sidebar.Item', {
	extend: 'Z8.Component',

	subcomponents: function() {
		return [this.menu];
	},

	htmlMarkup: function() {
		var data = this.data;
		var icon = { tag: 'i', cls: DOM.parseCls(data.icon).pushIf('fa', 'icon').join(' '), html: String.htmlText() };
		var text = { tag: 'span', cls: 'text', html: data.text };

		var cn = [icon, text];

		var items = data.items;
		if(!Z8.isEmpty(items)) {
			var chevron = { tag: 'i', cls: 'fa fa-chevron-right chevron' };
			cn.push(chevron);

			var menu = this.menu = new Z8.application.sidebar.Menu({ data: items, parent: this });
			cn.push(menu.htmlMarkup());
		}

		return { id: this.getId(), cls: 'item', tabIndex: this.getTabIndex(), cn: cn };
	},

	completeRender: function() {
		DOM.on(this, 'mouseOver', this.onMouseOver, this);
		this.callParent();
	},

	onDestroy: function() {
		DOM.un(this, 'mouseOver', this.onMouseOver, this);
		this.callParent();
	},

	activate: function() {
		this.showMenu();
		DOM.addCls(this, 'active');
		DOM.setTabIndex(this, 0);
		this.focus();
	},

	deactivate: function() {
		DOM.removeCls(this, 'active');
		DOM.setTabIndex(this, -1);
		if(this.menu != null)
			this.menu.deactivate();
	},

	showMenu: function() {
		if(this.menu != null)
			this.menu.show();
	},

	hideMenu: function() {
		if(this.menu != null)
			this.menu.hide();
	},

	onMouseOver: function(event, target) {
		var parent = this.parent;
		parent.onBeforeActivateItem(this);
		this.activate(true);
		parent.onAfterActivateItem(this);
		event.stopEvent();
	}
});Z8.define('Z8.application.sidebar.Menu', {
	extend: 'Z8.Container',

	cls: 'submenu',

	closeOnMouseOut: true,
	closeOnBlur: true,

	htmlMarkup: function() {
		var items = this.items;
		var menuItems = this.menuItems = [];
		var data = this.data;

		for(var i = 0, length = data.length; i < length; i++) {
			var item = new Z8.application.sidebar.Item({ data: data[i], parent: this });
			items.push(item);
			menuItems.push(item);
		}

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);
		DOM.on(this, 'click', this.onClick, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		DOM.un(this, 'click', this.onClick, this);
		this.callParent();
	},

	getTopLevelParent: function() {
		var item = this;

		while(item != null) {
			var parent = item.parent;
			if(parent == null)
				return item;
			item = parent;
		}
	},

	onBeforeActivateItem: function(item) {
		this.activate(item);
	},

	onAfterActivateItem: function(item) {
	},

	deactivate: function() {
		var active = this.active;
		if(active != null) {
			active.deactivate();
			delete this.active;
		}
	},

	activate: function(item) {
		item = item || this.menuItems[0];

		if(this.active != item) {
			this.deactivate();
			this.active = item;
			if(item != null)
				item.activate();
		}
	},

	hide: function() {
		this.deactivate();
		this.callParent();
	},

	onClick: function(event, target) {
		var active = this.active;
		var dom = DOM.get(active);
		if((dom == target || DOM.isParentOf(dom, target)) && active.data.id != null)
			this.getTopLevelParent().onSelectItem(active);
	},

	onKeyDown: function(event, target) {
		var items = this.menuItems;

		var active = this.active;
		var parent = this.parent;
		var key = event.getKey();

		if(key == Event.DOWN || key == Event.UP) {
			var index = items.indexOf(active);
			index = key == Event.DOWN ? (index != -1 && index < items.length - 1 ? index + 1 : 0) :
				(index != -1 && index > 0 ? index - 1 : items.length - 1);
			this.activate(items[index]);
			event.stopEvent();
		} else if(key == Event.LEFT && parent != null) {
			var topLevelParent = this.getTopLevelParent();
			topLevelParent.closeOnMouseOut = false;
			parent.activate();
			parent.hideMenu();
			event.stopEvent();
		} else if(key == Event.RIGHT && active != null && active.menu != null) {
			active.showMenu();
			active.menu.activate();
			event.stopEvent();
		} else if(key == Event.ENTER && active != null && active.data.id != null)
			this.getTopLevelParent().onSelectItem(active);
	},

	onSelectItem: function(item) {
	}
});
Z8.define('Z8.application.sidebar.Sidebar', {
	extend: 'Z8.application.sidebar.Menu',

	cls: 'menu',
	tabIndex: 0,

	isOpen: false,

	htmlMarkup: function() {
		this.itemIndex = {};

		this.data = JSON.decode(JSON.encode(User.data));

		var handle = { tag: 'i', cls: 'handle fa fa-angle-right' };
		var logo = { cls: 'logo' };

		this.items = [handle, logo];
		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'focus', this.onFocus, this, true);
		DOM.on(this, 'blur', this.onBlur, this, true);
		DOM.on(this, 'mouseOver', this.onMouseOver, this);
		DOM.on(this, 'mouseOut', this.onMouseOut, this);
	},

	onDestroy: function() {
		DOM.un(this, 'focus', this.onFocus, this, true);
		DOM.un(this, 'blur', this.onBlur, this, true);
		DOM.un(this, 'mouseOver', this.onMouseOver, this);
		DOM.un(this, 'mouseOut', this.onMouseOut, this);
		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);
		this.callParent();
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	open: function() {
		if(this.isOpen)
			return;

		DOM.addCls(this, 'open', 10);
		this.isOpen = true;

		this.focus();

		DOM.on(document.body, 'mouseDown', this.monitorOuterClick, this);

		this.fireEvent('open', this);
	},

	close: function() {
		if(!this.isOpen)
			return;

		this.deactivate();

		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);

		DOM.removeCls(this, 'open');
		DOM.setCssText(this, '', 100);

		this.isOpen = false;

		this.fireEvent('close', this);
	},

	toggle: function() {
		this.isOpen ? this.close() : this.open();
	},

	monitorOuterClick: function(event) {
		var target = event.target;

		if(!this.isInnerElement(target))
			this.close();
	},

	onSelectItem: function(item) {
		this.close();
		Z8.callback(this.handler, this.scope, item.data);
	},

	onFocus: function(event, target) {
		this.open();
	},

	isInnerElement: function(element) {
		return DOM.isParentOf(this, element) || DOM.isParentOf(this.owner, element);
	},

	onBlur: function(event, target) {
		if(this.closeOnBlur && !DOM.isParentOf(this, event.relatedTarget))
			this.close();
	},

	onMouseOver: function(event, target) {
		if(!this.isOpen)
			this.open();
	},

	onMouseOut: function(event, target) {
		var target = event.relatedTarget;
		if(this.closeOnMouseOut && target != null && !this.isInnerElement(target))
			this.close();
		this.closeOnMouseOut = true;
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC) {
			this.close();
			event.stopEvent();
		} else
			this.callParent(event, target);
	}
});
Z8.define('Z8.application.viewport.Login', {
	extend: 'Z8.form.Fieldset',

	plain: true,

	initComponent: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('login', 'air', 'display-none');

		var header = { cls: 'header', html: 'Авторизация' };
		var login = this.loginField = new Z8.form.field.Text({ label: { text: 'Логин', icon: 'fa-user', width: 80, align: 'left' }, placeholder: 'Логин', value: 'Admin' });
		var password = this.passwordField = new Z8.form.field.Text({ label: { text: 'Пароль',  icon: 'fa-key', align: 'left', width: 80 }, placeholder: 'Пароль', password: true });
		var loginButton = this.loginButton = new Z8.button.Button({ cls: 'btn-tool', icon: 'fa-check', handler: this.login, scope: this });

		this.controls = [header, login, password, loginButton];

		this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keydown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'keydown', this.onKeyDown, this);
		this.callParent();
	},

	focus: function() {
		this.loginField.focus();
	},

	show: function() {
		if(this.visible) {
			this.focus();
			return;
		}

		this.visible = true;

		this.mask = DOM.append(Viewport.getBody(), { cls: 'window-mask login' }); 

		DOM.removeCls(this, 'display-none');
		DOM.addCls(this, 'open', 100);

		this.focus();

		this.fireEvent('show', this);
	},

	hide: function() {
		if(!this.visible)
			return;

		this.visible = false;

		DOM.remove(this.mask);
		this.mask = null;

		DOM.removeCls(this, 'open');
		DOM.addCls(this, 'display-none', 200);

		this.passwordField.setValue('');
		this.fireEvent('hide', this);
	},

	login: function(button) {
		button.setBusy(true);

		var callback = function(response, success) {
			button.setBusy(false);

			if(success) {
				Z8.callback(this.handler, this.scope, response);
				this.hide();
			} else
				this.loginField.focus();
		}

		var parameters = {
			login: this.loginField.getValue(), 
			password: MD5.hex(this.passwordField.getValue() || ''),
			experimental: true
		};

		HttpRequest.send(parameters, { fn: callback, scope: this });
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ENTER)
			this.login(this.loginButton);
	}
});Z8.define('Z8.viewport.PopupMessages', {
	extend: 'Z8.Container',

	cls: 'messages popup',

	show: function(message) {
		var messages = Array.isArray(message) ? message : [message];

		var items = this.items;
		var count = items.length;
		var news = messages.length;

		if(count + news > 5) {
			for(var i = count - 1; i >= Math.max(5 - news, 0); i--)
				items[i].remove();
		}

		var items = [];
		for(var i = Math.max(0, news - 5); i < news; i++)
			items.insert(new Z8.viewport.PopupMessage(messages[i]), 0);

		this.add(items, 0);
	}
});

Z8.define('Z8.viewport.PopupMessage', {
	extend: 'Z8.Component',

	cls: 'message',

	htmlMarkup: function() {
		var icon = { cls: 'icon fa fa-fw fa-' + this.type + ' ' + this.type, html: String.htmlText() };
		var source = { tag: 'b', html: this.source || Application.title };
		var text = { cls: 'text', cn: [source, { tag: 'br' }, Format.nl2br(this.text)] };
		var body = { cls: 'body', cn: [icon, text] };
		return { cls: 'message ' + this.type, id: this.getId(), cn: [body] };
	},

	completeRender: function() {
		this.callParent();

		DOM.on(this, 'click', this.hide, this);
		new Z8.util.DelayedTask().delay(4000, this.hide, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.hide, this);
		this.callParent();
	},

	hide: function() {
		if(!this.isHiding) {
			this.isHiding = true;
			DOM.addCls(this, 'hiding');
			new Z8.util.DelayedTask().delay(1000, this.remove, this);
		}
	},

	remove: function() {
		this.container.remove(this);
	}
});Z8.define('Z8.application.viewport.Form', {
	extend: 'Z8.Container',
	shortClassName: 'viewport.Form',

	setTitle: function(title) {
		this.callParent(title);
		this.fireEvent('title', this, title);
	},

	getIcon: function() {
		return this.icon;
	},

	setIcon: function(icon) {
		this.icon = icon;
	}
});

Z8.define('Z8.application.viewport.SourceCode', {
	extend: 'Z8.Container',

	tabIndex: 0,

	cls: 'air source-code display-none',

	history: null,

	htmlMarkup: function() {
		this.history = [];
		this.historyPosition = -1;

		var backward = this.backward = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-play fa-flip-horizontal', enabled: false, handler: this.onBackward, scope: this });
		var forward = this.forward = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-play', enabled: false, handler: this.onForward, scope: this });
		var buttons = new Z8.button.Group({ items: [backward, forward] });
		var title = this.title = new Z8.Component({ cls: 'text' });

		var toolbar = new Z8.toolbar.Toolbar({ items: [buttons, title] });
		var frame = this.frame = new Z8.Component({ cls: 'code' });

		this.items = [toolbar, frame];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'click', this.onClick, this);
		DOM.on(this, 'keyDown', this.onKeyDown, this);
	},

	onDestroy: function() {
		DOM.un(this, 'click', this.onClick, this);
		DOM.un(this, 'keyDown', this.onKeyDown, this);
		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);
		this.callParent();
	},

	setEnabled: function(enabled) {
		DOM.swapCls(this, !enabled, 'disabled');
		this.callParent(enabled);
	},

	load: function(url, callback) {
		if(url == null || this.loadLock)
			return;

		var frame = this.frame;

		var index = url.indexOf('#');
		var hash = index != -1 ? url.substring(index) : null;
		var currentUrl = index != -1 ? url.substring(0, index) : url;

		if(currentUrl == this.current) {
			this.select(hash);
			this.addToHistory(url);
			Z8.callback(callback, true);
			return;
		}

		var loadCallback = function(response, success) {
			DOM.setInnerHTML(frame, success ? response.text : '');
	
			index = currentUrl.lastIndexOf('.');
			var title = index != -1 ? (currentUrl.substring(index + 1) + ' - ' + currentUrl.substring(0, index)) : currentUrl;
			this.title.setText(title);

			this.select(hash);
			this.addToHistory(url);
			this.currentUrl = currentUrl;

			if(hash == null)
				DOM.scroll(this.frame, 0, 0);

			Z8.callback(callback, success);

			if(this.isOpen)
				this.focus();

			this.loadLock = false;
		};

		this.backward.setEnabled(false);
		this.forward.setEnabled(false);
		this.loadLock = true;

		HttpRequest.get('src/' + url, { fn: loadCallback, scope: this });
	},

	focus: function() {
		this.frame.focus();
	},

	open: function() {
		if(this.isOpen)
			return;

		this.isOpen = true;

		DOM.removeCls(this, 'display-none');
		DOM.addCls(this, 'open', 10);
		DOM.on(document.body, 'mouseDown', this.monitorOuterClick, this);

		new Z8.util.DelayedTask().delay(500, this.focus, this);

		this.fireEvent('show', this);
	},

	close: function() {
		if(!this.isOpen)
			return;

		DOM.un(document.body, 'mouseDown', this.monitorOuterClick, this);
		DOM.removeCls(this, 'open');
		DOM.addCls(this, 'display-none');

		this.fireEvent('hide', this);

		this.isOpen = false;
	},

	toggle: function() {
		this.isOpen ? this.close() : this.open();
	},

	select: function(id) {
		DOM.removeCls(this.selected, 'selected');

		if(id == null)
			return;

		var selected = this.selected = DOM.selectNode(this, id);

		var parent = selected.offsetParent;

		var left = selected.offsetLeft;
		var top = selected.offsetTop;
		var width = selected.offsetWidth;
		var height = selected.offsetHeight;
		var scrollTop = parent.scrollTop;
		var scrollLeft = parent.scrollLeft;

		var parentWidth = parent.clientWidth;
		var parentHeight = parent.clientHeight;

		var scrollRight = scrollLeft + parentWidth;
		var scrollBottom = scrollTop + parentHeight;

		if(top < scrollTop)
			scrollTop = selected.offsetTop - 10;
		else if(top + height >= scrollBottom)
			scrollTop = scrollTop + (height + top - scrollBottom) + 10;

		if(left < scrollLeft)
			scrollLeft = selected.offsetLeft - 10 < parentWidth ? 0 : (selected.offsetLeft - 10);
		else if(left + width >= scrollRight)
			scrollLeft = scrollLeft + (width + left - scrollRight) + 10;

		DOM.scroll(parent, scrollLeft, scrollTop);

		DOM.addCls(this.selected, 'selected');
	},

	addToHistory: function(url) {
		if(this.historyLock)
			return;

		if(this.history[this.historyPosition] == url)
			return;

		var position = ++this.historyPosition;
		var history = this.history = this.history.splice(0, position);
		history.push(url);

		this.updateButtons();
	},

	updateButtons: function() {
		var position = this.historyPosition;
		this.backward.setEnabled(position > 0);
		this.forward.setEnabled(position != this.history.length - 1);
	},

	onBackward: function(button) {
		this.historyLock = true;

		var url = this.history[this.historyPosition - 1];

		var callback = function(success) {
			button.setBusy(false);
			this.historyPosition--;
			this.historyLock = false;
			this.updateButtons();
		};

		button.setBusy(true);
		this.load(url, { fn: callback, scope: this });
	},

	onForward: function(button) {
		this.historyLock = true;

		var url = this.history[this.historyPosition + 1];

		var callback = function(success) {
			button.setBusy(false);
			this.historyPosition++;
			this.historyLock = false;
			this.updateButtons();
		};

		button.setBusy(true);
		this.load(url, { fn: callback, scope: this });
	},

	monitorOuterClick: function(event) {
		var target = event.target;

		if(!DOM.isParentOf(this, target) && !DOM.isParentOf(this.owner, target))
			this.close();
	},

	onClick: function(event, target) {
		var tag = target.tagName;

		if(tag == 'SPAN')  // <a class="type"><span class="keyword">class</span></a>
			target = target.parentNode;

		if(target.tagName != 'A') {
			this.select(null);
			return;
		}

		event.stopEvent();

		var url = target.outerHTML.match(/href="([^"]*)"/)[1];
		this.load(url);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.ESC && this.isOpen) {
			event.stopEvent();
			this.close();
		}
	}
});var Viewport = null;

Z8.define('Z8.application.viewport.Viewport', {
	extend: 'Z8.Container',

	cls: 'viewport', //'air',

	handlers: [],
	forms: [null],

	initComponent: function() {
		this.callParent();

		this.setTitle(Application.name);
	},

	htmlMarkup: function() {
		var startButton = this.createBreadcrumb();
		var breadcrumbs = this.breadcrumbs = new Z8.button.Group({ cls: 'breadcrumbs flex', items: [startButton] });

		var logout = this.logout = new Z8.button.Button({ tooltip: 'Выход', cls: 'btn-tool', icon: 'fa-power-off', handler: this.logout, scope: this });

		var header = this.header = new Z8.Container({ cls: 'header flex-row flex-center', items: [breadcrumbs, logout] });
		var body = this.body = new Z8.Container({ cls: 'body' });
		var popupMessages = this.popupMessages = new Z8.viewport.PopupMessages();

		var loginForm = this.loginForm = new Z8.application.viewport.Login({ handler: this.loginCallback, scope: this });
		loginForm.on('show', this.onLoginFormShow, this);
		loginForm.on('hide', this.onLoginFormHide, this);

		this.items = [header, body, loginForm, popupMessages];

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
	},

	login: function(options) {
		this.handlers.push(options);
		var login = this.loginForm;
		this.isLoggingIn ? login.focus() : login.show();
	},

	logout: function() {
		if(!this.isLoggingIn) {
			this.onLogout();
			Application.login();
		} else
			this.loginForm.focus();
	},

	createHeaderButtons: function() {
		var settings = this.settingsButton = new Z8.button.Button({ tooltip: 'Настройки', cls: 'btn-tool', icon: 'fa-cog', enabled: false, handler: this.onSettings, scope: this });
		var jobMonitor = this.jobMonitorButton = new Z8.button.Button({ tooltip: 'Монитор задач', cls: 'btn-tool', icon: 'fa-tv', handler: this.openJobMonitor, scope: this });
		var logger = this.loggerButton = new Z8.button.Button({ tooltip: 'Сообщения', cls: 'btn-tool', icon: 'fa-comment-o', enabled: false, handler: this.openLogger, scope: this });
		return [jobMonitor, logger, settings];
	},

	onLogin: function() {
		if(this.isLoggedIn) {
			this.focus();
			return;
		}

		this.isLoggedIn = true;

		var header = this.header; 
		var menuToggle = this.menuToggle = new Z8.button.Button({ tooltip: 'Развернуть/скрыть меню', cls: 'btn-tool', icon: 'fa-bars', handler: this.toggleMenu, scope: this });
		header.add(menuToggle, 0);

		var buttons = this.buttons = this.createHeaderButtons();
		header.add(buttons, header.getCount() - 1);

		var menu = this.menu = new Z8.application.sidebar.Sidebar({ owner: menuToggle, handler: this.onMenuItem, scope: this });
		menu.on('open', this.onMenuOpen, this);
		menu.on('close', this.onMenuClose, this);
		this.add(menu);

		var sourceCode = this.sourceCode = new Z8.application.viewport.SourceCode();
		this.add(sourceCode);

		this.openMenu();

		DOM.on(menuToggle, 'mouseDown', this.onMenuToggleMouseDown, this);
		DOM.on(document.body, 'keyDown', this.onKeyDown, this);
	},

	onLogout: function() {
		if(!this.isLoggedIn)
			return;

		this.isLoggedIn = false;

		DOM.un(this.menuToggle, 'mouseDown', this.onMenuToggleMouseDown, this);
		DOM.un(document.body, 'keyDown', this.onKeyDown, this);

		// this will close all open forms and clear breadcrumbs
		this.openForm(null);

		var header = this.header;
		header.remove(this.menuToggle);
		header.remove(this.buttons);

		if(this.jobMonitor != null)
			this.jobMonitor.destroy();

		var menu = this.menu;
		menu.un('open', this.onMenuOpen, this);
		menu.un('close', this.onMenuClose, this);
		this.remove(menu);

		this.remove(this.sourceCode);

		this.body.removeAll();

		this.menuToggle = this.buttons = this.menu = this.jobMonitor = this.sourceCode = null;
	},

	loginCallback: function(loginData) {
		var handlers = this.handlers;

		for(var i = 0, length = handlers.length; i < length; i++)
			Z8.callback(handlers[i], loginData);

		this.handlers = [];

		this.onLogin();
	},

	message: function(message) {
		this.popupMessages.show(message);
	},

	getBody: function() {
		return this.body;
	},

	focus: function() {
		var forms = this.forms;
		var length = forms.length;
		return length > 1 ? forms[length - 1].focus() : DOM.focus(this);
	},

	openMenu: function(button) {
		this.menu.open();
	},

	showSourceCode: function(show) {
		show ? this.sourceCode.open() : this.sourceCode.close();
	},

	initSourceCode: function(source) {
		this.sourceCode.load(source);
	},

	onMenuToggleMouseDown: function(event, target) {
		if(this.menuToggle.isEnabled())
			this.menu.closeOnBlur = false;
	},

	toggleMenu: function(button) {
		this.menu.closeOnBlur = true;
		this.menu.toggle();
	},

	enableMenu: function(enabled) {
		if(this.menu != null) {
			this.menuToggle.setEnabled(enabled);
			this.menu.setEnabled(enabled);
		}
	},

	onMenuOpen: function(menu) {
		this.menuToggle.rotateIcon(90);
	},

	onMenuClose: function(menu) {
		this.menuToggle.rotateIcon(0);
		this.focus();
	},

	onLoginFormShow: function(form) {
		this.enableMenu(false);
		this.isLoggingIn = true;
	},

	onLoginFormHide: function(form) {
		this.enableMenu(true);
		this.isLoggingIn = false;
	},

	onMenuItem: function(item) {
		this.open(item.id, true);
	},

	setTitle: function(title) {
		this.callParent(title);
		DOM.setInnerHTML(this.text, title);
	},

	onBreadcrumbClick: function(button) {
		this.openForm(button.form);
	},

	createBreadcrumb: function(form, depth) {
		var text = form != null ? form.getTitle() : Application.name;
		var icon = form != null ? form.getIcon() : null;
		return new Z8.button.Button({ cls: 'n' + (depth || 1), text: text, tooltip: text, icon: icon, form: form, handler: this.onBreadcrumbClick, scope: this });
	},

	closeForm: function(form) {
		var forms = this.forms;
		var index = forms.indexOf(form);

		if(index == -1)
			return;

		index--;
		form = index != 0 ? forms[index] : null;

		this.openForm(form);
	},

	openForm: function(form, closeOthers, header) {
		this.showSourceCode(false);
		this.initSourceCode(form != null && form.store != null ? form.store.getSourceCodeLocation() : null);

		var forms = this.forms;
		var index = forms.indexOf(form || null);

		if(index == -1 && closeOthers)
			index = 0;

		var breadcrumbs = this.breadcrumbs;
		var buttons = this.breadcrumbs.items;
		var body = this.body;

		if(index != -1) {
			for(var i = buttons.length - 1; i > index; i--) {
				var button = buttons[i];
				body.remove(button.form);
				breadcrumbs.remove(button);
				forms.removeAt(i);
			}

			button = buttons[i];
			if(button.originalText != null) {
				button.setText(button.originalText);
				button.originalText = null;
			}

			DOM.removeCls(forms[i], 'display-none');
		} 

		if(form != null && forms.indexOf(form) == -1) {
			if(header != null) {
				var button = buttons[buttons.length - 1];
				button.originalText = button.getText();
				button.setText(header);
			}
			button = this.createBreadcrumb(form, forms.length + 1);
			DOM.addCls(forms[forms.length - 1], 'display-none');
			breadcrumbs.add(button);
			body.add(form);
			forms.push(form);
		}

		this.focus();

		Application.setTitle(form != null ? form.getTitle() : null);
	},

	getJobMonitor: function() {
		if(this.jobMonitor == null)
			this.jobMonitor = new Z8.application.job.JobMonitor({ autoDestroy: false });
		return this.jobMonitor;
	},

	startJob: function(job) {
		var jobMonitor = this.getJobMonitor();
		jobMonitor.addJob(job);
	},

	open: function(params, closeOthers, header) {
		if(this.isOpeningForm)
			return;

		this.isOpeningForm = true;

		var callback = function(response, success) {
			this.menuToggle.setBusy(false);
			this.isOpeningForm = false;

			if(!success)
				return;

			if(!response.isJob) {
				response.where = params.where;
				response.filter = params.filter;
				response.period = params.period;

				var config = { cls: 'air', store: response };
				var form = Application.getSubclass(response.ui);
				form = form != null ? Z8.create(form, config) : new Z8.application.form.Navigator(config);
				this.openForm(form, closeOthers, header);
			} else {
				var job = new Z8.application.job.Job(response);
				this.startJob(job);
			}
		};

		if(String.isString(params)) {
			var filter = User.getFilter(params);
			var period = User.getPeriod(params);
			params = { request: params, filter: filter.getActive(), period: period };
		}

		this.menuToggle.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });
	},

	openJobMonitor: function() {
		this.getJobMonitor().open();
	},

	onSettings: function() {
/*
		var view1 = new forms.View1();
		this.openForm(view1, true);
*/
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();
		if(key == Event.ESC) {
			this.openMenu();
			event.stopEvent();
		}
	}
});var User = null;

Z8.define('Z8.application.User', {
	extend: 'Z8.Object',

	constructor: function(config) {
		this.callParent(config);

		try {
			var settings = this.settings;
			this.settings = Z8.isEmpty(settings) ? {} : JSON.decode(settings);
		} catch(e) {
			this.settings = {};
		}
	},

	getProperty: function(key, subkey) {
		var application = this.settings[Application.id];
		var section = application ? application[key] : null;
		return section != null ? (subkey != null ? section[subkey] : section) : null;
	},

	setProperty: function(key, subkey, value) {
		var settings = this.settings;
		var applicationId = Application.id;
		var application = settings[applicationId];

		if(application == null)
			settings[applicationId] = application = {};

		if(value != undefined) {
			var section = application[key];
			if(section == null)
				application[key] = section = {};
			section[subkey] = value;
		} else
			application[key] = subkey;
	},

	getFilter: function(key) {
		return new Z8.data.Filter(this.getProperty(key, 'filter'));
	},

	setFilter: function(key, filter) {
		this.setProperty(key, 'filter', filter != null ? filter.toJson() : undefined);
	},

	getPeriod: function(key) {
		return new Z8.data.Period(this.getProperty(key, 'period'));
	},

	setPeriod: function(key, period) {
		this.setProperty(key, 'period', period != null ? period.toJson() : undefined);
	},

	saveSettings: function() {
		var params = { request: 'settings', data: this.settings };
		HttpRequest.send(params);
	}
});Z8.define('Z8.application.Application', {
	extend: 'Z8.Object',

	id: 'Z8™ Application',
	name: 'Revolt',
	title: 'Revolt software',

	viewportCls: null,
	userCls: null,

	subclasses: {
	},

	getSubclass: function(id) {
		return this.subclasses[id];
	},

	login: function(options) {
		var callback = function(loginData) {
			this.session = loginData.session;
			this.maxUploadSize = loginData.maxUploadSize;

			var cls = this.userCls;
			User = cls == null ? new Z8.application.User(loginData.user) : Z8.create(cls, loginData.user);

			if(options != null)
				Z8.callback(options, loginData);
		};

		if(Viewport == null) {
			this.setTitle();
			var cls = this.viewportCls;
			Viewport = cls == null ? new Z8.application.viewport.Viewport() : Z8.create(cls);
			Viewport.render();
		}

		Viewport.login({ fn: callback, scope: this });
	},

	setTitle: function(title) {
		document.title = (title != null ? title + ' - ' : '') + Application.title;
	},

	message: function(message) {
		Viewport.message(message);
	},

	checkFileSize: function(file) {
		var size = file.size;
		var maxSize = Application.maxUploadSize * 1024 * 1024;
		if(maxSize != 0 && size > maxSize) {
			Application.message({ text: 'Размер файла \'' + file.name + '\' ' + 
				Z8.util.Format.fileSize(size) + ' превышает максимально допустимые ' + Format.fileSize(maxSize), type: 'error', time: new Date() });
			return false;
		}
		return true;
	}
});

var Application = new Z8.application.Application();

DOM.onReady(Application.login, Application);Z8.define('Z8.application.job.Job', {
	poll: function(options) {
		if(this.task == null) {
			this.options = options;
			this.task = new Z8.util.DelayedTask();
		}

		var params = { request: this.request, server: this.server, job: this.id };
		HttpRequest.send(params, { fn: this.pollCallback, scope: this });
	},

	pollCallback: function(response, success) {
		Z8.callback(this.options, this, response, success);

		if(success && !response.done)
			this.task.delay(250, this.poll, this);
	}
});Z8.define('Z8.application.job.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.Datetime({ name: 'start', type: Type.Datetime }),
		new Z8.data.field.String({ name: 'duration' }),
		new Z8.data.field.Integer({ name: 'percent' })
	]
});

Z8.define('Z8.application.job.JobMonitor', {
	extend: 'Z8.window.Window',

	header: 'Монитор задач',
	icon: 'fa-tv',

	fields: [
		{ name: 'name', header: 'Задача', width: 250 },
		{ name: 'start', header: 'Начало', width: 150, type: Type.Datetime },
		{ name: 'duration', header: 'Длительность', width: 100 },
		{ name: 'percent', header: '%', width: 70, renderer: Format.percent }
	],

	initComponent: function(){
		this.callParent();

		this.store = new Z8.data.Store({ model: 'Z8.application.job.Model' });
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('job-monitor', 'air');

		var store = this.store;
		var taskList = this.taskList = new Z8.form.field.Listbox({ cls: 'tasks', store: store, fields: this.fields, icons: true, checks: false, editable: false });
		taskList.on('select', this.onSelect, this);
		var textArea = this.textArea = new Z8.form.field.Html({ cls: 'messages', readOnly: true });

		this.body = [taskList, textArea];

		return this.callParent();
	},

	focus: function() {
		this.taskList.focus();
	},

	addJob: function(job) {
		var data = { id: job.id, name: job.text, start: new Date(job.start) };
		var record = Z8.create(this.store.getModel(), data);
		record.job = job;
		this.store.add(record);

		var callback = function(job, data, success) {
			if(success) {
				record.beginEdit();
				record.set('percent', data.worked);
				record.set('duration', data.duration);
				record.endEdit();

				if(data.done)
					this.setIcon(job, 'fa-check');
			} else
				this.setIcon(job, 'fa-exclamation');

			this.addMessages(record, data.info.messages);
		};

		job.record = record;
		job.poll({ fn: callback, scope: this });

		this.open();
		this.taskList.select(record);
		this.setIcon(job, ['fa-circle-o-notch', 'fa-spin']);
	},

	setIcon: function(job, icon) {
		var item = this.taskList.getItem(job.record);
		item.setIcon(icon);
	},

	onSelect: function(listbox, record) {
		var messages = record != null ? record.job.info.messages.join('') : '';
		this.textArea.setValue(messages);
	},

	addMessages: function(record, newMessages) {
		var messages = record.job.info.messages;

		for(var i = 0, length = newMessages.length; i < length; i++) {
			var message = newMessages[i];
			var icon = { tag: 'i', cls: 'fa fa-fw fa-' + message.type + ' ' + message.type, html: String.htmlText() };
			var time = Format.datetime(new Date(message.time));
			var header = DOM.markup({ tag: 'p', cls: 'header', cn: [icon,  time] });
			var text = DOM.markup({ tag: 'p', cls: 'text', cn: [message.text] });
			messages.push(header, text);
		}

		var selection = this.taskList.getSelection();
		if(selection == record) {
			this.onSelect(this.taskList, record);
			this.textArea.setScrollTop(10000000);
		}
	}
});Z8.define('Z8.query.Store', {
	extend: 'Z8.data.Store',

	statics: {
		definedModels: {},

		config: function(field) {
			var config = {};

			var isListbox = field.isListbox;

			if(!field.isCombobox && !isListbox)
				throw 'Z8.query.Store.config() accepts only listbox or combobox config';

			var query = field.query;
			var primaryKey = isListbox ? query.primaryKey : field.link.primaryKey;
			var parentKey = isListbox ? field.query.parentKey : null;
			var periodKey = isListbox ? field.query.periodKey : null;
			var lockKey = field.query.lockKey;

			var link = isListbox ? query.link : field.link;
			var fields = query.fields || query.columns || [field];
			if(isListbox && link != null)
				fields = fields.concat([link]);

			config.request = query.id;
			config.fields = fields;
			config.requestFields = fields;
			config.primaryKey = primaryKey;
			config.parentKey = parentKey;
			config.lockKey = lockKey;
			config.periodKey = periodKey;
			config.link = !isListbox ? link.name : null;
			config.query = query.name;
			config.totals = isListbox ? query.totals : false;
			config.sort = isListbox ? query.sort : null;
			config.values = field.values;
			config.access = query.access;

			return config;
		}
	},

	constructor : function(config) {
		var fields = this.getFieldsInfo(config);

		var form = { colCount: config.colCount, controls: config.controls, text: config.text, presentation: config.presentation, icon: config.icon, actions: config.actions, reports: config.reports, readOnly: config.readOnly, dependencies: config.dependencies };

		this.readOnly = config.readOnly || false;

		var request = config.request;
		var className = request + '/' + fields.hash;

		var model = Z8.define(className, {
			extend: 'Z8.data.Model',
			single: true,

			name: request,
			link: config.link,
			query: config.query,
			totalCount: config.total !== false,
			totals: config.totals === true,

			idProperty: config.primaryKey,

			access: config.access,

			parentIdProperty: config.parentKey,
			lockProperty: config.lockKey,
			periodProperty: config.periodKey,
			filesProperty: config.attachmentsKey,
			sourceCode: config.sourceCode,

			fields: fields.data,
			names: fields.names,
			columns: fields.columns,
			quickFilters: fields.quickFilters,
			requestFields: fields.request,

			links: fields.links,
			valueFor: fields.valueFor,
			valueFrom: fields.valueFrom
		});

		var storeConfig = {
			model: model,

			data: config.data,
			totalCount: config.total,
			summaryData: config.summaryData,
			totalsData: config.totalsData,
			limit: config.limit || 200,

			filters: config.filter,
			where: config.where,
			period: config.period,
			sorter: config.sort,
			values: config.values,

			params: config.params,
			form: form
		};

		this.callParent(storeConfig);
	},

	getFieldsInfo: function(config) {
		config = config || {};
		var result = {};

		var fieldsMap = {};
		result.data = this.createFields(config.fields, fieldsMap);
		result.names = this.createFields(config.nameFields, fieldsMap);
		result.columns = this.createFields(config.columns, fieldsMap);
		result.quickFilters = this.createFields(config.quickFilters, fieldsMap);
		result.request = this.createFields(config.requestFields, fieldsMap);
		result.links = this.getFieldsBy(result.data, 'isLink');
		result.valueFor = this.getFieldsBy(result.data, 'valueFor');
		result.valueFrom = this.getFieldsBy(result.data, 'valueFrom');
		result.hash = MD5.hex(Object.keys(fieldsMap).sort().join(';') + config.link + config.primaryKey);

		return result;
	},

	createFields: function(configs, fieldsMap) {
		var result = [];

		if(configs == null || configs.length == 0)
			return result;

		for(var i = 0, length = configs.length; i < length; i++) {
			var config = configs[i];
			var name = config.name;
			var field = fieldsMap[name];
			if(field == null) {
				var type = this.getFieldType(config);
				field = Z8.create(type, config);
			}
			result.push(field);
			fieldsMap[name] = field;
		}
		return result;
	},

	getFieldsBy: function(fields, property) {
		var result = [];

		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			if(field[property] != null)
				result.push(field);
		}

		return result;
	},

	getFieldType: function(config) {
		var type = config.type;

		if(type == Type.Boolean)
			return 'Z8.data.field.Boolean';
		else if(type == Type.Integer)
			return 'Z8.data.field.Integer';
		else if(type == Type.Float)
			return 'Z8.data.field.Float';
		else if(type == Type.Date)
			return 'Z8.data.field.Date';
		else if(type == Type.Datetime)
			return 'Z8.data.field.Datetime';
		else if(type == Type.Guid)
			return 'Z8.data.field.Guid';
		else if(type == Type.Files)
			return 'Z8.data.field.Files';
		else if(type == Type.Json)
			return 'Z8.data.field.Json';
		else
			return 'Z8.data.field.String';
	}
});Z8.define('Z8.application.form.Navigator', {
	extend: 'viewport.Form',

	presentation: 'form',

	initComponent: function() {
		var store = this.store;

		if(!store.isStore)
			store = this.store = new Z8.query.Store(store);

		this.title = store.form.text;
		this.icon = store.form.icon;
		this.presentation = store.form.presentation || 'form';

		this.callParent();

		this.initFilter(User.getFilter(this.registryEntry()));
		this.initPeriod(User.getPeriod(this.registryEntry()));
	},

	registryEntry: function() {
		return this.store.getModelName();
	},

	initFilter: function(filter) {
		this.filter = filter;
		this.store.setFilter(filter.getActive());
	},

	setFilter: function(filter) {
		this.initFilter(filter);
		User.setFilter(this.registryEntry(), filter);
		User.saveSettings();
		this.refreshRecords(this.filterButton);
	},

	initPeriod: function(period) {
		var store = this.store;
		if(store.getPeriodProperty() == null)
			return false;

		this.period = period;
		store.setPeriod(period);
		return true;
	},

	setPeriod: function(period) {
		if(this.initPeriod(period)) {
			User.setPeriod(this.registryEntry(), period);
			User.saveSettings();
			this.refreshRecords(this.periodButton);
		}
	},

	isReadOnly: function() {
		return this.store.form.readOnly;
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('navigator');

		var items = this.createForm().add(this.createTable());
		var body = this.body = new Z8.Container({ cls: 'body', items: items });
		var toolbar = this.createToolbar();

		var isForm = this.isFormPresentation();
		this.setListboxTools(this.listbox, isForm);
		this.setListboxTools(this.table, !isForm);

		this.items = [toolbar, body];

		this.updateSortState();

		return this.callParent();
	},

	completeRender: function() {
		this.callParent();
		DOM.on(this, 'keyDown', this.onKeyDown, this);

		Viewport.sourceCode.owner = this.sourceCodeButton;
		Viewport.sourceCode.on('show', this.onSourceCodeShow, this);
		Viewport.sourceCode.on('hide', this.onSourceCodeHide, this);
	},

	onDestroy: function() {
		Viewport.sourceCode.un('show', this.onSourceCodeShow, this);
		Viewport.sourceCode.un('hide', this.onSourceCodeHide, this);
		Viewport.sourceCode.owner = null;

		DOM.un(this, 'keyDown', this.onKeyDown, this);

		this.callParent();
	},

	isFormPresentation: function() {
		return this.presentation == 'form';
	},

	isTablePresentation: function() {
		return this.presentation == 'table';
	},

	createForm: function() {
		var store = this.store;
		var config = store.form;

		var cls = this.isFormPresentation() ? '' : 'display-none';
		var form = this.form = new Z8.form.Form({ cls: cls, model: store.getModelName(), autoSave: true, controls: config.controls, colCount: config.colCount, readOnly: this.isReadOnly() });
		return [this.createListbox(), form];
	},

	createListbox: function() {
		var listbox = this.listbox = new Z8.form.field.Listbox(this.getListboxConfig());
		listbox.on('select', this.onSelect, this);
		listbox.on('contentChange', this.updateToolbar, this);
		return listbox;
	},

	createTable: function() {
		var isTable = this.isTablePresentation();
		var fields = this.getColumns();
		var store = this.store;
		var cls = 'table' + (isTable ? '' : ' display-none');
		var table = this.table = new Z8.form.field.Listbox({ cls: cls, store: store, fields: fields, locks: !this.isReadOnly(), editable: true, readOnly: this.isReadOnly(), totals: store.hasTotals(), pagingMode: 'always' });
		return table;
	},

	setListboxTools: function(listbox, set) {
		listbox.setAddTool(set ? this.addButton : null);
		listbox.setCopyTool(set ? this.copyButton : null);
		listbox.setRefreshTool(set ? this.refreshButton : null);
		listbox.setRemoveTool(set ? this.removeButton : null);
	},

	createToolbar: function() {
		var store = this.store;

		var buttons = [];
		var addCopyRefresh = [];

		if(store.hasCreateAccess()) {
			var add = this.addButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-file-o', tooltip: 'Новая запись', handler: this.addRecord, scope:this });
			addCopyRefresh.push(add);
		}

		if(store.hasCopyAccess()) {
			var copy = this.copyButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-copy', tooltip: 'Копировать запись', handler: this.copyRecord, scope:this });
			addCopyRefresh.push(copy);
		}

		if(store.hasReadAccess()) {
			var refresh = this.refreshButton =  new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-refresh', tooltip: 'Обновить', handler: this.refreshRecords, scope:this });
			addCopyRefresh.push(refresh);
		}

		if(addCopyRefresh.length != 0) {
			addCopyRefresh = new Z8.button.Group({ items: addCopyRefresh });
			buttons.push(addCopyRefresh);
		}

		var files = this.filesButton = this.createFilesButton();
		if(files != null)
			buttons.push(files);

		if(store.hasDestroyAccess()) {
			var remove = this.removeButton = new Z8.button.Button({ cls: 'btn-sm', danger: true, icon: 'fa-trash', tooltip: 'Удалить запись', handler: this.removeRecord, scope:this });
			buttons.push(remove);
		}

		var quickFilters = this.quickFilters = this.createQuickFilters();
		buttons.add(quickFilters);

		var period = this.periodButton = this.createPeriodButton();
		if(period != null)
			buttons.push(period);

		var filter = this.filterButton = this.createFilterButton();
		var sort = this.sortButton = new Z8.button.Button({ enabled: false, cls: 'btn-sm', icon: 'fa-sort', tooltip: 'Порядок сортировки', triggerTooltip: 'Настроить порядок сортировки', split: true, handler: this.toggleSortOrder, scope: this });
		buttons.push(filter, sort);

		var formTable = this.createFormTableGroup();
		if(formTable != null)
			buttons.push(formTable);

		var print = this.printButton = this.createPrintButton();
		buttons.push(print);

		var actions = this.actionsButton = this.createActionsButton();
		if(actions != null)
			buttons.push(actions);

		var reports = this.reportsButton = this.createReportsButton();
		if(reports != null)
			buttons.push(reports);

		var sourceCode = this.sourceCodeButton = this.createSourceCodeButton();
		if(sourceCode != null)
			buttons.push(sourceCode);

		return new Z8.toolbar.Toolbar({ items: buttons });
	},

	createFilesButton: function() {
		var store = this.store;
		var filesProperty = store.getFilesProperty();

		if(filesProperty == null || !store.hasWriteAccess())
			return null;

		var control = this.form.getControl(filesProperty);
		var store = control == null ? new Z8.data.Store({ model: 'Z8.data.file.Model' }) : control.store;
		var menu = new Z8.menu.Menu({ store: store });
		menu.on('itemClick', this.downloadFiles, this);
		var file = new Z8.button.File({ store: store, control: control, cls: 'btn-sm', icon: 'fa-paperclip', tooltip: 'Файлы', menu: menu });
		file.on('select', this.uploadFiles, this);
		return file;
	},

	createQuickFilters: function() {
		var quickFilters = [];
		var fields = this.getQuickFilterFields();
		for(var i = 0, length = fields.length; i < length; i++) {
			var field = fields[i];
			var quickFilter = new Z8.form.field.Search({ field: field, width: 10, label: false, placeholder: field.header, tooltip: field.header });
			quickFilter.on('search', this.onSearch, this);
			quickFilters.push(quickFilter);
		}
		return quickFilters;
	},

	createFilterButton: function() {
		var filter = new Z8.filter.Button({ cls: 'btn-sm', filter: this.filter, fields: this.store.getFields() });
		filter.on('filter', this.onFilter, this);
		return filter;
	},

	createFormTableGroup: function() {
		if(Z8.isEmpty(this.getColumns()))
			return null;

		var isForm = this.isFormPresentation();

		var formButton = this.formButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-wpforms', tooltip: 'В виде формы', toggled: isForm });
		formButton.on('toggle', this.toggleForm, this);

		var tableButton = this.tableButton = new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-table', tooltip: 'В виде таблицы', toggled: !isForm });
		tableButton.on('toggle', this.toggleTable, this);

		return new Z8.button.Group({ items: [formButton, tableButton], radio: true });
	},

	createPrintButton: function() {
		var items = [
			new Z8.menu.Item({ text: 'Acrobat Reader (*.pdf)', icon: 'fa-file-pdf-o', format: 'pdf' }),
			new Z8.menu.Item({ text: 'Microsoft Excel (*.xls)', icon: 'fa-file-excel-o', format: 'xls' }),
			new Z8.menu.Item({ text: 'Microsoft Word (*.doc)', icon: 'fa-file-word-o', format: 'doc' }),
			'-',
			new Z8.menu.Item({ text: 'Настройки', icon: 'fa-print', enabled: false })
		];

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuPrint, this);

		return new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-file-pdf-o', tooltip: 'Сохранить как PDF', menu: menu, handler: this.print, scope: this, format: 'pdf' });
	},

	createPeriodButton: function() {
		if(this.period == null)
			return null;

		var period = new Z8.calendar.Button({ cls: 'btn-sm', icon: 'fa-calendar', period: this.period });
		period.on('period', this.onPeriod, this);
		return period;
	},

	createActionsButton: function() {
		var actions = this.store.form.actions;

		if(Z8.isEmpty(actions))
			return null;

		var items = [];
		for(var i = 0, length = actions.length; i < length; i++) {
			var action = actions[i];
			items.push(new Z8.menu.Item({ text: action.text, icon: action.icon, action: action }));
		}

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuAction, this);

		return new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-play', text: 'Действия', tooltip: 'Действия', menu: menu, handler: this.onMenuButtonClick, scope: this });
	},

	createReportsButton: function() {
		var reports = this.store.form.reports;

		if(Z8.isEmpty(reports))
			return null;

		var items = [];
		for(var i = 0, length = reports.length; i < length; i++) {
			var report = reports[i];
			items.push(new Z8.menu.Item({ text: report.text, icon: report.icon, report: report }));
		}

		var menu = new Z8.menu.Menu({ items: items });
		menu.on('itemClick', this.onMenuReport, this);
		return new Z8.button.Button({ cls: 'btn-sm', icon: 'fa-print', tooltip: 'Печать документов', menu: menu, handler: this.onMenuButtonClick, scope: this });
	},

	createSourceCodeButton: function() {
		var sourceCode = new Z8.button.Button({ cls: 'btn-sm float-right', text: 'Исходный код', success: true, icon: 'fa-code', tooltip: 'Как это сделано', toggled: false });
		sourceCode.on('toggle', this.toggleSourceCode, this);
		return sourceCode;
	},

	onSourceCodeShow: function() {
		this.sourceCodeButton.setToggled(true, true);
	},

	onSourceCodeHide: function() {
		this.sourceCodeButton.setToggled(false, true);
		this.focus();
	},

	getListboxConfig: function() {
		var names = this.getNames();
		var quickFilters = this.getQuickFilterFields();
		var label = this.getListboxLabel(names);

		return {
			cls: this.isFormPresentation() ? '' : 'display-none',
			store: this.store,
			fields: names,
			names: names,
			label: label,
			quickFilters: quickFilters,
			filters: quickFilters.length == 0,
			editable: true,
			locks: !this.isReadOnly()
		};
	},

	getListboxLabel: function(fields) {
		if(fields.length != 0) {
			var field = fields[0];
			return { text: field.header || field.name, icon: field.icon };
		}
		return '';
	},

	getNames: function() {
		var names = this.store.getNames();

		if(!Z8.isEmpty(names))
			return names;

		var controls = this.form.getFields();
		return controls.length == 0 ? [] : [controls[0].field];
	},

	getColumns: function() {
		var columns = this.store.getColumns();

		if(Z8.isEmpty(columns)) {
			var controls = this.form.getFields();
			for(var i = 0, length = controls.length; i < length; i++) {
				var field = controls[i].field;
				if(field != null && !field.isListbox && !field.isContainer && !field.isFieldset)
					columns.push(field);
			}
		}

		for(var i = 0, length = columns.length; i < length; i++) {
			var column = columns[i];
			column.editable = column.editable !== false && !column.readOnly;
		}

		return columns;
	},

	getQuickFilterFields: function() {
		return this.store.getQuickFilters();
	},

	getActiveListbox: function() {
		return this.isFormPresentation() ? this.listbox : this.table;
	},

	getSelection: function() {
		return this.getActiveListbox().getSelection();
	},

	addRecord: function(button) {
		this.getActiveListbox().onAddRecord(button);
	},

	copyRecord: function(button) {
		this.getActiveListbox().onCopyRecord(button);
	},

	removeRecord: function(button) {
		this.getActiveListbox().onRemoveRecord(button);
	},

	refreshRecords: function(button) {
		this.getActiveListbox().onRefresh(button);
	},

	updateToolbar: function() {
		var readOnly = this.isReadOnly();
		var record = this.getSelection();

		if(this.addButton != null)
			this.addButton.setEnabled(!readOnly);

		if(this.copyButton != null)
			this.copyButton.setEnabled(!readOnly && record != null);

		if(this.removeButton != null)
			this.removeButton.setEnabled(!readOnly && record != null && record.isDestroyable());

		if(this.reportsButton != null)
			this.reportsButton.setEnabled(record != null);

		var filesButton = this.filesButton;
		if(filesButton == null)
			return;

		var enabled = !readOnly && record != null;
		filesButton.setEnabled(enabled);

		if(enabled && filesButton.control == null)
			filesButton.store.loadData(record.getFiles());
	},

	toggleForm: function(button) {
		this.table.hide();
		this.listbox.show();
		this.form.show();

		this.setListboxTools(this.listbox, true);
		this.setListboxTools(this.table, false);

		this.presentation = 'form';
		this.focus();
	},

	toggleTable: function(button) {
		this.listbox.hide();
		this.form.hide();
		this.table.show();

		this.setListboxTools(this.table, true);
		this.setListboxTools(this.listbox, false);

		this.presentation = 'table';
		this.focus();
	},

	toggleSourceCode: function(button, toggled) {
		Viewport.showSourceCode(toggled);
	},

	onSearch: function(search, value) {
		var callback = function(records, success) {
			search.setBusy(false);
		};

		var filter = [];
		var quickFilters = this.quickFilters;

		for(var i = 0, length = quickFilters.length; i < length; i++) {
			var quickFilter = quickFilters[i];
			if(quickFilter != search && quickFilter.expression != null)
				filter.push(quickFilter.expression);
		}

		search.expression = value != '' ? { property: search.field.name, operator: Operation.Contains, value: value } : null;
		if(search.expression != null)
			filter.push(search.expression);

		search.setBusy(true);
		this.store.quickFilter(filter, { fn: callback, scope: this });
	},

	updateSortState: function() {
/*
		var sorters = this.store.getSorter();
		var icon = sorters.length != 0 ? (sorters[0].direction == 'asc' ? 'fa-sort-alpha-asc' : 'fa-sort-alpha-desc') : 'fa-sort';
		this.sortButton.setIcon(icon);
		this.sortButton.setEnabled(sorters.length != 0);
*/
	},

	toggleSortOrder: function(button) {
		var sorter = this.store.getSorter();
		if(sorter.length != 0) {
			var sorter = sorter[0];
			sorter.direction = sorter.direction == 'asc' ? 'desc' : 'asc';
			this.updateSortState();
		}
	},

	onFilter: function(button, filter, action) {
		if(action != Filter.NoAction)
			this.setFilter(filter);
		this.focus();
	},

	onPeriod: function(button, period, action) {
		if(action != Period.NoAction)
			this.setPeriod(period);
		this.focus();
	},

	focus: function() {
		return this.getActiveListbox().focus();
	},

	select: function(listbox, record) {
		if(!this.disposed) {
			this.form.loadRecord(record);
			this.updateToolbar();
		}
	},

	onSelect: function(listbox, record) {
		if(this.selectTask == null)
			this.selectTask = new Z8.util.DelayedTask();

		this.selectTask.delay(50, this.select, this, listbox, record);
	},

	onKeyDown: function(event, target) {
		var key = event.getKey();

		if(key == Event.F && event.ctrlKey && this.quickFilters.length != 0) {
			this.quickFilters[0].focus();
			event.stopEvent();
		} else if(key == Event.ESC) {
			this.focus();
			Viewport.closeForm(this);
			event.stopEvent();
		}
	},

	onMenuPrint: function(menu, item) {
		var format = item.format;
		if(format == 'pdf')
			this.printPDF();
		else if(format == 'xls')
			this.printXLS();
		else if(format == 'doc')
			this.printDOC();
	},

	printPDF: function() {
		var button = this.printButton;
		button.setIcon('fa-file-pdf-o');
		button.setTooltip('Сохранить как PDF');
		button.format = 'pdf';
		this.print();
	},

	printXLS: function() {
		var button = this.printButton;
		button.setIcon('fa-file-excel-o');
		button.setTooltip('Сохранить как XLS');
		button.format = 'xls';
		this.print();
	},

	printDOC: function() {
		var button = this.printButton;
		button.setIcon('fa-file-word-o');
		button.setTooltip('Сохранить как DOC');
		button.format = 'doc';
		this.print();
	},

	print: function() {
		var button = this.printButton;
		var format = button.format;

		var columns = [];
		var headers = this.table.getHeaders();

		for(var i = 0, length = headers.length; i < length; i++) {
			var header = headers[i];
			var field = header.field;
			if(field != null && field.type != Type.Text)
				columns.push({ id: field.name, width: Ems.emsToPixels(header.getWidth()) });
		}

		var store = this.store;

		var params = {
			request: store.getModelName(),
			action: 'export',
			format: format,
			columns: columns,
			filter: this.filter.getActive() || [],
			quickFilter: store.getQuickFilter(),
			where: store.getWhere(),
			sort: store.getSorter(),
			period: store.getPeriod()
		};

		var callback = function(response, success) {
			button.setBusy(false);
		};

		button.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });

		this.focus();
	},

	onMenuButtonClick: function(button) {
		button.toggleMenu();
	},

	onMenuAction: function(menu, item) {
		this.actionsButton.setBusy(true);

		var action = item.action;
		var record = this.getSelection();

		var params = {
			request: this.store.getModelName(),
			action: 'action',
			id: action.id,
			records: (record != null && !record.phantom) ? [record.id] : null
		};

		var callback = function(response, success) {
			this.onAction(action, response, success);
			this.actionsButton.setBusy(false);
			this.refreshRecords(this.refreshButton);
		};

		HttpRequest.send(params, { fn: callback, scope: this });
	},

	onAction: function(action, response, success) {
	},

	onMenuReport: function(menu, item) {
		var report = item.report;
		var record = this.getSelection();

		var params = {
			request: this.store.getModelName(),
			action: 'report',
			format: 'pdf',
			id: report.id,
			recordId: record.id
		};

		var callback = function(response, success) {
			this.reportsButton.setBusy(false);
		};

		this.reportsButton.setBusy(true);
		HttpRequest.send(params, { fn: callback, scope: this });

		this.focus();
	},

	downloadFiles: function(menu, item) {
		var callback = function(success) {
			this.filesButton.setBusy(false);
		};

		this.filesButton.setBusy(true);

		DOM.download(item.record.get('path'), null, { fn: callback, scope: this });
	},

	uploadFiles: function(button, files) {
		button.setBusy(true);

		var callback = function(record, files, success) {
			button.setBusy(false);
			if(success && this.getSelection() == record)
				button.store.loadData(files);
		};

		var record = this.getSelection();
		record.attach(record.getFilesProperty(), files, { fn: callback, scope: this });
	}
});