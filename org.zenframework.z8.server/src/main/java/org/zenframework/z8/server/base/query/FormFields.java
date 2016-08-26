package org.zenframework.z8.server.base.query;

import org.zenframework.z8.server.base.form.Control;
import org.zenframework.z8.server.runtime.RCollection;

public class FormFields extends RCollection<Control.CLASS<? extends Control>> {
	private static final long serialVersionUID = 4286261046290787388L;

	Query owner = null;

	public FormFields(Query owner) {
		super(/* true */);

		this.owner = owner;
	}
}
