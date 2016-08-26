Ext.util.JSON.encodeDate = function(date) {
	return Ext.isDate(date) ? '"' + Z8.Format.isoDate(date) + '"' : ''; 
};
