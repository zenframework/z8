Ext.util.JSON.encodeDate = function(date)
{
	var format = /*date.isDatetime ?*/ Z8.Format.Datetime /*: Z8.Format.Date*/;
	return date.format('"' + format + '"'); 
};
