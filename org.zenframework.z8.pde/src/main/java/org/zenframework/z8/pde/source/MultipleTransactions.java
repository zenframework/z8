package org.zenframework.z8.pde.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;
import org.zenframework.z8.pde.preferences.PreferencePageConsts;

public class MultipleTransactions {
	private Z8Editor editor;
	private IDocument document;
	private List<Transaction> transes = new ArrayList<Transaction>();

	public void add(int length, int offset, String what) {
		add(new Transaction(length, offset, what));
	}

	public void add(Transaction t) {
		if(t != null)
			transes.add(t);
	}

	public MultipleTransactions(Z8Editor e) {
		this.editor = e;
		document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
	}

	public MultipleTransactions(IDocument d) {
		document = d;
	}

	public void execute() {
		try {
			if(editor != null)
				if(!editor.validateEditorInputState())
					return;
			int size = transes.size();
			if(size == 0)
				return;
			Transaction[] arr = new Transaction[size];
			transes.toArray(arr);
			for(int i = 0; i < size; i++)
				arr[i].setHelperIndex(i);
			Arrays.sort(arr);
			if(editor != null)
				editor.beginCompoundChange();
			for(int i = size - 1; i >= 0; i--) {
				document.replace(arr[i].getOffset(), arr[i].getLength(), arr[i].getWhat());
			}
			if(editor != null)
				editor.endCompoundChange();
			transes.clear();
			if(editor != null)
				if(Plugin.getDefault().getPreferenceStore().getBoolean(PreferencePageConsts.ATTR_EDITOR_ALWAYS_SAVE))
					editor.doSave(null);
		} catch(Exception e) {
			Plugin.log(e);
		}

	}

	public List<Transaction> getTransactions() {
		return transes;
	}

	public IDocument getDocument() {
		return document;
	}

}
