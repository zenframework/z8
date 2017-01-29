package org.zenframework.z8.pde;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;

import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.workspace.Project;

public class TypeSelectDialog extends FilteredListDialog {

	public interface TypeTester {
		public int test(IType t);
	}

	public TypeSelectDialog(Shell parent, final Project p, final TypeTester tester, String title, String message) {
		super(parent);
		setTitle(title);
		setMessage(message);
		setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IType)element).getUserName();
			}
		});

		setContentProvider(new IStructuredContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				final Map<IType, Integer> types = new HashMap<IType, Integer>();
				List<Project> projects = new ArrayList<Project>();
				for(Project p1 : p.getReferencedProjects()) {
					projects.add(p1);
				}
				;
				projects.add(0, p);
				for(Project p1 : projects)
					for(IType t : p1.getTypes()) {
						if(t != null)
							if(!(t instanceof MemberNestedType)) {
								int test = tester.test(t);
								if(test > 0)
									types.put(t, test);
							}
					}
				IType[] arr = new IType[types.size()];
				types.keySet().toArray(arr);
				Arrays.sort(arr, new Comparator<IType>() {
					@Override
					public int compare(IType arg0, IType arg1) {
						int diff = types.get(arg0) - types.get(arg1);
						if(diff != 0)
							return diff;
						return arg0.getUserName().compareToIgnoreCase(arg1.getUserName());
					}
				});
				return arr;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

		});

		setInput(new Integer(0));

	}

	public TypeSelectDialog(Shell parent, final Project p, final String typeName, String title, String message) {
		this(parent, p, new TypeTester() {

			@Override
			public int test(IType t) {
				if(t.isSubtypeOf(typeName))
					return 1;
				return 0;

			}

		}, title, message);
	}

	public TypeSelectDialog(Shell parent, final Project p, final IType type, final boolean addThis, String title, String message) {
		this(parent, p, new TypeTester() {

			@Override
			public int test(IType t) {
				return t.isSubtypeOf(type) && (addThis || (type != t)) ? 1 : 0;
			}

		}, title, message);
	}

}
