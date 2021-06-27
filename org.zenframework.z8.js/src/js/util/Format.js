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
		Number: '0,000.' + String.repeat('#', 12),
		TrueText: Z8.$('Format.true'),
		FalseText: Z8.$('Format.false'),

		ThousandSeparator: ' ',
		DecimalSeparator: ',',

		charWidth: null,
		measures: {},

		list: function() {
			var result = '';

			for(var i = 0, length = arguments.length; i < length; i++) {
				var arg = arguments[i];

				if(!Z8.isEmpty(arg))
					result += (result.isEmpty() ? '' : ', ') + arg;
			}
			return result;
		},

		placeholder: function(value, placeholder) {
			return Z8.isEmpty(value) ? placeholder : value;
		},

		nl2br: function(value) {
			return value != null ? value.replace(/\n/g, '<br>') : '';
		},

		br2nl: function(value) {
			return value != null ? value.replace(/<br>/g, '\n') : '';
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
			if(String.isEmpty(value))
				return '';

			dateFormat = dateFormat || Format.Datetime;
			timeFormat = timeFormat || Format.Time;

			var today = new Date();
			var yesterday = new Date().add(-1, Date.Day);

			var isToday = Date.isEqualDate(value, today);
			var isYesterday = Date.isEqualDate(value, yesterday);

			if(isToday)
				return Z8.$('Format.today') + Format.date(value, timeFormat);

			if(isYesterday)
				return Z8.$('Format.yesterday') + Format.date(value, timeFormat);

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
			length = (Format.date(new Date(2000, 8, 20, 22, 22, 22, 222), format).length + 3) * Format.getCharWidth();
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
						trimPart = 'trailingZeroes=new RegExp(Format.DecimalSeparator.replace(/([-.*+?\\^${}()|\\[\\]\\/\\\\])/g, "\\\\$1") + "*0{0,' + length + '}$");';
					}
				}

				var code = [
					'var neg,absVal,fnum,parts' + (hasComma ? ',thousandSeparator,thousands=[],j,n,i' : '') + (extraChars ? ',format="' + format + '"' : '') + ',trailingZeroes;' + 'return function(v){' + 'if(typeof v!=="number"&&isNaN(v=parseFloat(v)))return"";' + 'neg=v<0;',
					'absVal=Math.abs(v.round(' + precision + '));',
					'fnum=String(absVal);',
					trimPart
				];
				if(hasComma) {
					if(precision) {
						code[code.length] = 'parts=fnum.split(".");';
						code[code.length] = 'fnum=parts[0];';
					}
					code[code.length] = 'if(absVal>=1000) {';
					code[code.length] = 'thousandSeparator=Format.ThousandSeparator;' + 'thousands.length=0;' + 'j=fnum.length;' + 'n=fnum.length%3||3;' + 'for(i=0;i<j;i+=n){' + 'if(i!==0){' + 'n=3;' + '}' + 'thousands[thousands.length]=fnum.substr(i,n);' + '}' + 'fnum=thousands.join(thousandSeparator);' + '}';
					if (precision)
						code[code.length] = 'fnum += parts[1] ? Format.DecimalSeparator+parts[1] : "";';
				} else if(precision)
					code[code.length] = 'if(Format.DecimalSeparator!=="."){' + 'parts=fnum.split(".");' + 'fnum=parts[0]+(parts[1] ? Format.DecimalSeparator+parts[1] : "");' + '}';

				code[code.length] = 'if(neg&&fnum!=="' + (precision ? '0.' + String.repeat('0', precision) : '0') + '") { fnum="-"+fnum; }';

				if(trimTrailingZeroes)
					code[code.length] = 'if(parts[1]) { fnum=fnum.replace(trailingZeroes,""); }';

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
			M: 'this.getMonthShortName()',
			m: 'String.padLeft(this.getMonth() + 1, 2, "0")',
			n: '(this.getMonth() + 1)',
			t: 'this.getDaysInMonth()',

			W: 'String.padLeft(this.getWeekOfYear(), 2, "0")',
			w: 'this.getWeekOfYear()',

			z: 'this.getDayOfYear()',
			N: 'this.getDayOfWeek() + 1',
			l: 'this.getDayName()',
			D: 'this.getDayShortName()',
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
});
