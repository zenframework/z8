package org.zenframework.z8.pde.editor.document;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import org.zenframework.z8.pde.editor.Z8Editor;

public class DocumentSetupParticipant implements IDocumentSetupParticipant {
	final static public String PARTITIONING = "org.zenframework.z8.pde.editor.document.partitioning";

	public DocumentSetupParticipant() {
	}

	@Override
	public void setup(IDocument document) {
		if(document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3)document;
			IDocumentPartitioner partitioner = new FastPartitioner(Z8Editor.getPartitionScanner(), PartitionScanner.PARTITION_TYPES);
			extension3.setDocumentPartitioner(PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}
}
