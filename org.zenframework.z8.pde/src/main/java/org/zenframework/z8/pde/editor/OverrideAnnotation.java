package org.zenframework.z8.pde.editor;

import org.eclipse.jface.text.source.Annotation;

import org.zenframework.z8.compiler.core.ISource;
import org.zenframework.z8.pde.navigator.actions.OpenSourceAction;

public class OverrideAnnotation extends Annotation {
	private boolean override;
	private ISource source;

	public boolean isOverride() {
		return override;
	}

	public OverrideAnnotation(boolean isOverride, String what, ISource s) {
		super("org.zenframework.z8.pde.OverrideAnnotation", false, (isOverride ? "overrides" : "implements") + " " + what);
		override = isOverride;
		source = s;
	}

	public void open() {
		new OpenSourceAction(source).run();
	}
}
