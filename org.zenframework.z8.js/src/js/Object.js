Z8.define('Z8.Object', {
	mixinId: 'z8-object',

	isDisposed: function() {
		return this.disposed;
	},

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