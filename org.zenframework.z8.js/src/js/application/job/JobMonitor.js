Z8.define('Z8.application.job.Model', {
	extend: 'Z8.data.Model',

	local: true,
	idProperty: 'id',

	fields: [
		new Z8.data.field.String({ name: 'name' }),
		new Z8.data.field.Datetime({ name: 'start', type: Type.Datetime }),
		new Z8.data.field.String({ name: 'duration' }),
		new Z8.data.field.Integer({ name: 'percent' })
	]
});

Z8.define('Z8.application.job.JobMonitor', {
	extend: 'Z8.window.Window',

	header: 'Монитор задач',
	icon: 'fa-tv',

	fields: [
		{ name: 'name', header: 'Задача', width: 250 },
		{ name: 'start', header: 'Начало', width: 150, type: Type.Datetime },
		{ name: 'duration', header: 'Длительность', width: 100 },
		{ name: 'percent', header: '%', width: 70, renderer: Format.percent }
	],

	initComponent: function(){
		this.callParent();

		this.store = new Z8.data.Store({ model: 'Z8.application.job.Model' });
	},

	htmlMarkup: function() {
		this.cls = DOM.parseCls(this.cls).pushIf('job-monitor');

		var store = this.store;
		var taskList = this.taskList = new Z8.form.field.Listbox({ cls: 'tasks', flex: 1, colSpan: 7, store: store, fields: this.fields, icons: true, checks: false, editable: false, minHeight: Ems.unitsToEms(5) });
		taskList.on('select', this.onSelect, this);
		var textArea = this.textArea = new Z8.form.field.Html({ cls: 'messages', flex: 1, colSpan: 5, readOnly: true, minHeight: Ems.unitsToEms(5) });

		this.colCount = 12;
		this.controls = [taskList, textArea];

		return this.callParent();
	},

	focus: function() {
		this.taskList.focus();
	},

	addJob: function(job) {
		var data = { id: job.id, name: job.text, start: new Date(job.start) };
		var record = Z8.create(this.store.getModel(), data);
		record.job = job;
		this.store.add(record);

		var callback = function(job, data, success) {
			if(this.disposed)
				return;

			if(success) {
				record.beginEdit();
				record.set('percent', data.worked);
				record.set('duration', data.duration);
				record.endEdit();

				if(data.done)
					this.setIcon(job, 'fa-check');
			} else
				this.setIcon(job, 'fa-exclamation');

			this.addMessages(record, data.info.messages);
		};

		job.record = record;
		job.poll({ fn: callback, scope: this });

		this.open();
		this.taskList.select(record);
		this.setIcon(job, ['fa-circle-o-notch', 'fa-spin']);
	},

	setIcon: function(job, icon) {
		var item = this.taskList.getItem(job.record);
		item.setIcon(icon);
	},

	onSelect: function(listbox, record) {
		var messages = record != null ? record.job.info.messages.join('') : '';
		this.textArea.setValue(messages);
	},

	addMessages: function(record, newMessages) {
		var messages = record.job.info.messages;

		for(var i = 0, length = newMessages.length; i < length; i++) {
			var message = newMessages[i];
			var icon = { tag: 'i', cls: 'fa fa-fw fa-' + message.type + ' ' + message.type, html: String.htmlText() };
			var time = Format.datetime(new Date(message.time));
			var header = DOM.markup({ tag: 'p', cls: 'header', cn: [icon,  time] });
			var text = DOM.markup({ tag: 'p', cls: 'text', cn: [message.text] });
			messages.push(header, text);
		}

		var selection = this.taskList.getSelection();
		if(selection == record) {
			this.onSelect(this.taskList, record);
			this.textArea.setScrollTop(10000000);
		}
	}
});