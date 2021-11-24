Z8.define('Z8.menu.Item', {
	extend: 'ListItem',
	shortClassName: 'MenuItem',

	icon: '',

	getCls: function() {
		return ListItem.prototype.getCls.call(this).pushIf('menu');
	}
});
