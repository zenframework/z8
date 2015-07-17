Z8.evaluations.Evaluator = Ext.extend(Ext.util.Observable,
{
	editors: [],
	
	constructor: function(editors, container)
	{
		Z8.evaluations.Evaluator.superclass.constructor.call(this);
		
		this.editors = editors;
		this.container = container;
		
		for(var i = 0; i < editors.length; i++)
		{
			var editor = editors[i];

			editor.enableKeyEvents = true;
			
			if(!editor.isTriggerField) // when a trigger field changes, Z8.Evaluator.update method should be called manually
			{
				editor.on('keyup', this.onKeyup, this);
				editor.on('select', this.onKeyup, this);
			}
			
			if(editor.xtype == 'checkbox')
			{
				editor.on('check', this.onKeyup, this);
			}
		}
		
		this.initFormulas(editors);

	},
	
	getEditorById: function(fieldId)
	{
		for(var i = 0; i < this.editors.length; i++)
		{
			if(this.editors[i].fieldId == fieldId)
			{
				return this.editors[i];
			}
		}
		
		return null;
	},
	
	getEditorByLinkId: function(linkId)
	{
		for(var i = 0; i < this.editors.length; i++)
		{
			if(this.editors[i].linkId == linkId)
			{
				return this.editors[i];
			}
		}
		
		return null;
	},

	updateDependencies: function(trigger)
	{
		var dependencies = trigger.dependencies;
		
		if(dependencies != null)
		{
			for(var i = 0; i < dependencies.length; i++)
			{
				var editor = this.getEditorById(dependencies[i]);
				editor.setValue(trigger.value);			
				this.evaluateEditor(editor);
			}
		}
	},
	
	update: function(editor, triggers)
	{
		for(var i = 0; i < triggers.length; i++)
		{
			this.updateDependencies(triggers[i]);
		}
		
		if(editor.evaluations != null)
		{
			this.onKeyup(editor);
		}
		else
		{
			for(var i = 0; i < triggers.length; i++)
			{
				this.onKeyup(triggers[i]);
			}
		}
	},
	
	initFormulas: function(editors)
	{
		for(var i = 0; i < editors.length; i++)
		{
			var editor = editors[i];
			
			if(editor.evaluations != null)
			{
				var evaluations = editor.evaluations;
				
				for(var j = 0; j < evaluations.length; j++)
				{
					var evaluation = evaluations[j];
					
					var fieldId = evaluation.field;
					
					var dependentEditor = this.getEditorById(fieldId);
	
					if(dependentEditor == null)
					{
						dependentEditor = this.getEditorByLinkId(fieldId);
					}
					
					if(dependentEditor != null)
					{
						evaluation.editor = dependentEditor;
					}
				}
			}
		}
	},

	evaluateEditor: function(editor)
	{
		if(editor.evaluations != null)
		{
			editor.evaluated = true;
			this.evaluateFormulas(editor);
		}
	},
	
	onKeyup: function(editor, e)
	{
		if(e == null || e.keyCode == null ||  e.keyCode != e.TAB && e.keyCode != e.ALT && e.keyCode != e.TAB && e.keyCode != e.CONTROL && e.keyCode != e.LEFT && e.keyCode != e.RIGHT )
		{		
			this.evaluateEditor(editor);

			for(var i = 0; i < this.editors.length; i++)
			{
				this.editors[i].evaluated = false;
			}
		}
	},
	
	evaluateFormulas: function(editor)
	{
		var evaluations = editor.evaluations;
		
		for(var i = 0; i < evaluations.length; i++)
		{
			this.evaluate(evaluations[i]);
		}
	},

	evaluate: function(evaluation)
	{
		var editor = evaluation.editor;
		
		if(editor == null || editor.evaluated)
		{
			return;
		}
		
		var text = evaluation.formula;
		
		for(var i = 0; i < evaluation.fields.length; i++)
		{
			var fieldId = evaluation.fields[i];
			var ed = this.getEditorById(fieldId);
			var link = this.getEditorByLinkId(fieldId);
			
			var value = this.container.record.get(fieldId);
			
			if(ed != null)
			{
				value = ed.getValue();
			}
			else if(link != null)
			{
				value = link.valueId;
			}
			
			if(Ext.isDate(value))
			{
				value = 'new Date(' + value.valueOf() + ')';
			}
			else if(Ext.isString(value))
			{
				var field = this.container.record.fields.get(fieldId);
				
				if(field.type == Ext.data.Types.STRING)
				{
					value = '"' + value + '"';
				}
				else if(field.type == Ext.data.Types.NUMBER && Z8.isEmpty(value))
				{
					value = 0;
				}
				else
				{
					value = undefined;
				}
			}
			
			var pattern = '{' + fieldId + '}';
			var regexp = new RegExp(pattern, 'g');
			text = text.replace(regexp, value);
		}
		
		var success = false;
		
		try
		{
			var result = eval(text);
			
			if(Ext.isString(result) || !isNaN(result))
			{
				editor.setValue(result);
				success = true;
			}
		}
		catch(e)
		{
		}
		
		editor.evaluated = true;
	}
});