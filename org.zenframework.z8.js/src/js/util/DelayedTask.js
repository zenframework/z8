Z8.define('Z8.util.DelayedTask', {
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
});