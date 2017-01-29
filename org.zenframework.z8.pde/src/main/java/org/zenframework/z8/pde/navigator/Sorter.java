package org.zenframework.z8.pde.navigator;

import org.eclipse.jface.viewers.ViewerSorter;

import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.DeclaratorNestedType;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;

@SuppressWarnings("deprecation")
public class Sorter extends ViewerSorter {

	@Override
	public int category(Object o) {
		if(o instanceof Folder)
			return 1;
		if(o instanceof CompilationUnit)
			return 2;
		if(o instanceof MemberNestedType || o instanceof DeclaratorNestedType)
			return 3;
		if(o instanceof IType)
			return 2;
		if(o instanceof String)
			return 2;
		if(o instanceof IMethod)
			return 2;
		if(o instanceof IMember)
			return 1;
		return 0;
	}

}
