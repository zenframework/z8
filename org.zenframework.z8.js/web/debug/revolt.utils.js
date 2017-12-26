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
});