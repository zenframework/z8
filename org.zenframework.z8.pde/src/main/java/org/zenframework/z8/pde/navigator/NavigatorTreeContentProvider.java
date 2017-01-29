package org.zenframework.z8.pde.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.expressions.ArrayInitializer;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Resource;

public class NavigatorTreeContentProvider implements ITreeContentProvider {
	@Override
	public Object[] getChildren(Object element) {
		List<ILanguageElement> result = new ArrayList<ILanguageElement>();

		if(element instanceof Folder) {
			Folder folder = (Folder)element;

			for(Resource resource : folder.getMembers()) {
				if(resource instanceof CompilationUnit || resource instanceof Folder && resource.getName().charAt(0) != '.') {
					result.add(resource);
				}
			}

			return result.toArray(new ILanguageElement[0]);
		}

		if(element instanceof CompilationUnit) {
			CompilationUnit compilationUnit = (CompilationUnit)element;

			if(!compilationUnit.isChanged()) {
				IType type = compilationUnit.getType();

				if(type != null) {
					return new Object[] { type };
				}

				return new Object[0];
			}

			return new String[] { "..." };
		}

		if(element instanceof IMethod) {
			return null;
		}

		if(element instanceof IMember) {
			IMember member = (IMember)element;
			IInitializer initializer = member.getInitializer();

			if(initializer != null) {
				element = (ILanguageElement)initializer;
			}
		}

		if(element instanceof IInitializer) {
			IInitializer initializer = (IInitializer)element;

			ILanguageElement rightElement = initializer.getRightElement();

			if(rightElement instanceof ArrayInitializer) {
				element = rightElement;
			}
		}

		if(element instanceof ArrayInitializer) {
			ArrayInitializer arrayInitializer = (ArrayInitializer)element;
			return arrayInitializer.getElements();
		}

		if(element instanceof IType) {
			IType type = (IType)element;

			IMember[] members = type.getAllMembers();

			List<IInitializer> initializers = new ArrayList<IInitializer>();
			initializers.addAll(Arrays.asList(type.getInitializers()));

			for(IMember member : members) {
				if(type.isEnum() && member instanceof IMethod) {
					continue;
				}

				for(IInitializer initializer : initializers) {
					if(initializer.getParent() == member) {
						initializers.remove(initializer);
						break;
					}
				}

				result.add(member);
			}

			result.addAll(initializers);
		}

		return result.toArray(new Object[0]);
	}

	@Override
	public Object getParent(Object object) {
		if(object instanceof ILanguageElement) {
			ILanguageElement element = (ILanguageElement)object;
			return element.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object object) {
		Object[] children = getChildren(object);
		return children != null && children.length != 0;
	}

	@Override
	public Object[] getElements(Object object) {
		return getChildren(object);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
