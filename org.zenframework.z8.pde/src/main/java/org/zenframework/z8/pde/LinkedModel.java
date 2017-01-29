package org.zenframework.z8.pde;

import org.zenframework.z8.pde.source.InitHelper;

public class LinkedModel extends Model {

	private Model link;

	private String addition;

	public LinkedModel(Model link, String addition) {
		super(null);
		this.link = link;
		this.addition = addition;
	}

	@Override
	public InitHelper getHelper() {
		InitHelper h = link.getHelper().add(addition);
		h.context = h.context.getCompilationUnit().getReconciledType();
		return h;
	}

	public Model getLink() {
		return link;
	}

	@Override
	protected IDocProv getDocProv() {
		return link.getDocProv();
	}

}
