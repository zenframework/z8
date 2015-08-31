/**
 * 	
 *	@param {string}		str		The string to be wrapped.
 *	@param {number}		width	The column width (a number, default: 75)
 *	@param {string} 	brk		The character(s) to be inserted at every break. (default: ‘\n’)
 *	@param {boolean}	cut		The cut: a Boolean value (false by default). Разрывать слова.
 *
 *	@return string
 */
Ext.util.Format.wordWrap = function(str, width, brk, cut)
{
	brk = brk || '\n';
    width = width || 75;
    cut = cut || false;
 
    if (!str) { return str; }
 
    var regex = '.{1,' +width+ '}(\\s|$)' + (cut ? '|.{' +width+ '}|.+$' : '|\\S+?(\\s|$)');
 
    return str.match( RegExp(regex, 'g') ).join( brk );
};

Ext.util.Format.date = function(value, format)
{
	if(!value)
	{
		return "";
	}

	if(!(value instanceof Date))
	{
		value = new Date(Date.parse(value));
	}

	return value.dateFormat(format || Z8.Format.Date);
};

Ext.util.Format.number = function(v, format)
{
	if (!format)
	{
		return v;
	}
  
	v = Ext.num(v, NaN);
	
	if (isNaN(v))
	{
		return '';
	}
	
	var comma = ',',
		dec   = '.',
		i18n  = false,
		neg   = v < 0;

	v = Math.abs(v);
	if (format.substr(format.length - 2) == '/i') {
		format = format.substr(0, format.length - 2);
		i18n   = true;
		comma  = '.';
		dec    = ',';
	}

	var hasComma = format.indexOf(comma) != -1,
		psplit   = (i18n ? format.replace(/[^\d\,]/g, '') : format.replace(/[^\d\.]/g, '')).split(dec);

	if (1 < psplit.length) {
		v = v.toFixed(psplit[1].length);
	} else if(2 < psplit.length) {
		throw ('NumberFormatException: invalid format, formats should have no more than 1 period: ' + format);
	} else {
		v = v.toFixed(0);
	}

	var fnum = v.toString();

	psplit = fnum.split('.');

	if (hasComma) {
		var cnum = psplit[0], 
			parr = [], 
			j    = cnum.length, 
			m    = Math.floor(j / 3),
			n    = cnum.length % 3 || 3,
			i;

		for (i = 0; i < j; i += n) {
			if (i != 0) {
				n = 3;
			}
			
			parr[parr.length] = cnum.substr(i, n);
			m -= 1;
		}
		fnum = parr.join(Z8.integerSeparator);
		if (psplit[1]) {
			fnum += Z8.decimalSeparator + psplit[1];
		}
	} else {
		if (psplit[1]) {
			fnum = psplit[0] + Z8.decimalSeparator + psplit[1];
		}
	}

	return (neg ? '-' : '') + format.replace(/[\d,?\.?]+/, fnum);
};
