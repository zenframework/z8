Z8.define('Z8.application.job.Job', {
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
});