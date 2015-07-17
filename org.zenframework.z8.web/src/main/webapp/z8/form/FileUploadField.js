Z8.form.FileUploadField = Ext.extend(Ext.form.TextField,
{
	/**
	 * @cfg {String} buttonText The button text to display on the upload button (defaults to
	 * 'Browse...').  Note that if you supply a value for {@link #buttonCfg}, the buttonCfg.text
	 * value will be used instead if available.
	 */
	buttonText: '...',
	/**
	 * @cfg {Boolean} buttonOnly True to display the file upload field as a button with no visible
	 * text field (defaults to false).  If true, all inherited TextField members will still be available.
	 */
	buttonOnly: false,
	/**
	 * @cfg {Number} buttonOffset The number of pixels of space reserved between the button and the text field
	 * (defaults to 3).  Note that this only applies if {@link #buttonOnly} = false.
	 */
	buttonOffset: 3,
	/**
	 * @cfg {Object} buttonCfg A standard {@link Ext.Button} config object.
	 */

	// private
	readOnly: true,

	/**
	 * @hide
	 * @method autoSize
	 */
	autoSize: Ext.emptyFn,

	initComponent: function()
	{
		Z8.form.FileUploadField.superclass.initComponent.call(this);

		this.addEvents('fileselected');
	},

	onRender: function(ct, position)
	{
		Z8.form.FileUploadField.superclass.onRender.call(this, ct, position);

		this.wrap = this.el.wrap({cls:'x-form-field-wrap x-form-file-wrap'});
		this.el.addClass('x-form-file-text');
		this.el.dom.removeAttribute('name');
		this.createFileInput();

		var btnCfg = Ext.applyIf(this.buttonCfg || {}, { text: this.buttonText });
		this.button = new Ext.Button(Ext.apply(btnCfg, { renderTo: this.wrap, cls: 'x-form-file-btn' + (btnCfg.iconCls ? ' x-btn-icon' : '') }));

		if(this.buttonOnly)
		{
			this.el.hide();
			this.wrap.setWidth(this.button.getEl().getWidth());
		}

		this.bindListeners();
		this.resizeEl = this.positionEl = this.wrap;
	},
	
	bindListeners: function()
	{
		this.fileInput.on({
			scope: this,
			mouseenter: function()
			{
				this.button.addClass(['x-btn-over','x-btn-focus'])
			},
			mouseleave: function()
			{
				this.button.removeClass(['x-btn-over','x-btn-focus','x-btn-click'])
			},
			mousedown: function()
			{
				this.button.addClass('x-btn-click')
			},
			mouseup: function()
			{
				this.button.removeClass(['x-btn-over','x-btn-focus','x-btn-click'])
			},
			change: function()
			{
				var v = this.fileInput.dom.value;
				this.setValue(v);
				this.fireEvent('fileselected', this, v);	
			}
		}); 
	},
	
	createFileInput: function()
	{
		this.fileInput = this.wrap.createChild(
		{
			id: this.getFileInputId(),
			name: this.name||this.getId(),
			cls: 'x-form-file',
			tag: 'input',
			type: 'file',
			size: 1
		});
	},
	
	reset: function()
	{
		this.fileInput.remove();
		this.createFileInput();
		this.bindListeners();
		Z8.form.FileUploadField.superclass.reset.call(this);
	},

	getFileInputId: function()
	{
		return this.id + '-file';
	},

	onResize: function(w, h)
	{
		Z8.form.FileUploadField.superclass.onResize.call(this, w, h);

		this.wrap.setWidth(w);

		if(!this.buttonOnly)
		{
			var w = this.wrap.getWidth() - this.button.getEl().getWidth() - this.buttonOffset;
			this.el.setWidth(w);
		}
	},

	onDestroy: function()
	{
		Z8.form.FileUploadField.superclass.onDestroy.call(this);
		Ext.destroy(this.fileInput, this.button, this.wrap);
	},
	
	onDisable: function()
	{
		Z8.form.FileUploadField.superclass.onDisable.call(this);
		this.doDisable(true);
	},
	
	onEnable: function()
	{
		Z8.form.FileUploadField.superclass.onEnable.call(this);
		this.doDisable(false);
	},
	
	doDisable: function(disabled)
	{
		this.fileInput.dom.disabled = disabled;
		this.button.setDisabled(disabled);
	},

	preFocus: Ext.emptyFn,

	alignErrorIcon: function()
	{
		this.errorIcon.alignTo(this.wrap, 'tl-tr', [2, 0]);
	}
});

Ext.reg('fileuploadfield', Z8.form.FileUploadField);

Ext.form.FileUploadField = Z8.form.FileUploadField;
