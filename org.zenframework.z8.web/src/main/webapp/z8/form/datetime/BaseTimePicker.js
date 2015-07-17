Z8.form.BaseTimePicker = Ext.extend(Ext.Panel,
{
	format: 'g:i A',
	
	header: true,
	
	nowText: 'Now',
	
	doneText: 'Done',
	
	hourIncrement: 1,
	
	minIncrement: 1,
	
	hoursLabel: 'Hours',
	
	minsLabel: 'Minutes',
	
	cls: 'ux-base-time-picker',
	
	width: 210,
	
	layout: 'form',
	
	labelAlign: 'top',
	
	initComponent: function ()
	{
		this.addEvents('select');

		this.hourSlider = new Ext.slider.SingleSlider(
		{
			increment: this.hourIncrement,
			minValue: 0,
			maxValue: 23,
			fieldLabel: this.hoursLabel,
			listeners: { change: this.updateTimeValue, scope: this },
			plugins: new Ext.slider.Tip()
		});

		this.minSlider = new Ext.slider.SingleSlider(
		{
			increment: this.minIncrement,
			minValue: 0,
			maxValue: 59,
			fieldLabel: this.minsLabel,
			listeners: { change: this.updateTimeValue, scope: this },
			plugins: new Ext.slider.Tip()
		});

		this.setCurrentTime(false);

		this.items = [ this.hourSlider, this.minSlider ];

		this.bbar = 
		[
			{
				text: this.nowText,
				handler: this.setCurrentTime,
				scope: this
			},
			'->',
			{
				text: this.doneText,
				handler: this.onDone,
				scope: this
			}
		];

		Z8.form.BaseTimePicker.superclass.initComponent.call(this);
	},

	setCurrentTime: function(animate)
	{
		this.setValue(new Date(), !!animate);
	},

	onDone: function ()
	{
		this.fireEvent('select', this, this.getValue());
	},

	setValue: function(value, animate)
	{
		this.hourSlider.setValue(value.getHours(), animate);
		this.minSlider.setValue(value.getMinutes(), animate);

		this.updateTimeValue();
	},

	extractValue: function()
	{
		var v = new Date();
		v.setHours(this.hourSlider.getValue());
		v.setMinutes(this.minSlider.getValue());
		return v;
	},

	getValue: function()
	{
		return this.extractValue();
	},

	updateTimeValue: function()
	{
		var v = this.extractValue().format(this.format);

		if(this.rendered)
		{
			this.setTitle(v);
		}
	},

	afterRender: function()
	{
		Z8.fomr.BaseTimePicker.superclass.afterRender.call(this);

		this.updateTimeValue();
	},

	destroy: function()
	{
		this.purgeListeners();

		this.hourSlider = null;
		this.minSlider = null;

		Z8.form.BaseTimePicker.superclass.destroy.call(this);
	}
});

Ext.reg('basetimepicker', Z8.form.BaseTimePicker);

Z8.form.ExBaseTimePicker = Ext.extend(Z8.form.BaseTimePicker,
{
	format: 'g:i:s A',
	
	secIncrement: 1,
	
	secsLabel: 'Seconds',


	initComponent: function()
	{
		this.secSlider = new Ext.slider.SingleSlider(
		{
			increment: this.secIncrement,
			minValue: 0,
			maxValue: 59,
			fieldLabel: this.secsLabel,
			listeners: { change: this.updateTimeValue, scope: this },
			plugins: new Ext.slider.Tip()
		});

		Z8.form.ExBaseTimePicker.superclass.initComponent.call(this);
	},

	initItems: function()
	{
		Z8.form.ExBaseTimePicker.superclass.initItems.call(this);

		this.items.push(this.secSlider);
	},

	setValue: function(value, animate)
	{
		this.secSlider.setValue(value.getSeconds(), animate);
		
		Z8.form.ExBaseTimePicker.superclass.setValue.call(this, value, animate);
	},

	extractValue: function()
	{
		var v = Z8.form.ExBaseTimePicker.superclass.extractValue.call(this);
		v.setSeconds(this.secSlider.getValue());
		return v;
	},

	destroy: function()
	{
		this.secSlider = null;
		Z8.form.ExBaseTimePicker.superclass.destroy.call(this);
	}
});

Ext.reg('exbasetimepicker', Z8.form.ExBaseTimePicker);