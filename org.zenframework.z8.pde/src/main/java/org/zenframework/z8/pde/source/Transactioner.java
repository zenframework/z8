package org.zenframework.z8.pde.source;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;

import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;

public class Transactioner implements ResourceListener {
	private IDocument doc;
	private CompilationUnit unit;
	private List<Transaction> transactions = new ArrayList<Transaction>();

	public Transactioner(Z8Editor editor) {
		doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		unit = Workspace.getInstance().getCompilationUnit(((FileEditorInput)editor.getEditorInput()).getFile());
		unit.installResourceListener(this);
	}

	private void reset() {
		transactions.clear();
	}

	public void append(Transaction transaction) {
		for(int i = 0; i < transactions.size(); i++) {
			Transaction t = transactions.get(i);
			if(t.getOffset() < transaction.getOffset())
				transaction.shift(t.getShift());
		}
		try {
			doc.replace(transaction.getOffset(), transaction.getLength(), transaction.getWhat());
		} catch(Exception e) {
			Plugin.log(e);
		}
		transactions.add(transaction);
	}

	@Override
	public void event(int type, Resource resource, Object object) {
		reset();
	}

	public CompilationUnit getCompilationUnit() {
		return unit;
	}

	public IDocument getDocument() {
		return doc;
	}

}
