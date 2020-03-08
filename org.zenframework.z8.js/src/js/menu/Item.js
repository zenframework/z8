Z8.define('Z8.menu.Item', {
	extend: 'Z8.list.Item',

	icon: 'fa-circle transparent',

	getCls: function() {
		return Z8.list.Item.prototype.getCls.call(this).pushIf('menu');
	}
});
