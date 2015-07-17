Z8.form.DateTimeField = Ext.extend(Ext.form.DateField,
{
	timeFormat: 'g:i A',

	doneText: 'Ok',
	todayBtnText: 'Сегодня',

	picker:
	{
		format: Z8.Format.Datetime,
		timeConfig:
		{
			tlp: '',
			readOnly: false,
			fieldLabel: '',
			strategyConfig:
			{
				format: Z8.Format.Time,
				incrementValue : 10,
				incrementConstant : Date.SECOND,
				alternateIncrementValue : 5,
				alternateIncrementConstant : Date.MINUTE
			}
		}
	},
		
	defaultAutoCreate: { tag: 'input', type: 'text', size: '22', autocomplete: 'off' },

	initComponent: function ()
	{
		Z8.form.DateTimeField.superclass.initComponent.call(this);

		this.dateFormat = this.dateFormat || this.format;
		this.format = this.dateFormat + ' ' + this.timeFormat;

		var pickerConfig = Ext.apply(this.picker || {}, { dateFormat: this.dateFormat, timeFormat: this.timeFormat });

		delete this.picker;
		delete this.initialConfig.picker;

		this.menu = new Z8.menu.DateTimeMenu({ picker: pickerConfig, hideOnClick: false });
	},

	onTriggerClick: function ()
	{
		Z8.form.DateTimeField.superclass.onTriggerClick.call(this);

		this.menu.picker.setValue(this.getValue() || new Date());
	}	
});

Ext.reg('datetimefield', Z8.form.DateTimeField);
