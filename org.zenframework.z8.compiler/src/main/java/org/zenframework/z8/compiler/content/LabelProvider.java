package org.zenframework.z8.compiler.content;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.zenframework.z8.compiler.workspace.NlsUnit;
import org.zenframework.z8.compiler.workspace.Project;

public class LabelProvider {
	static public LabelEntry getEntry(Project project, final String locale, String key) {
		return new LabelProvider().findEntry(project, locale, key, new HashSet<IPath>());
	}

	private LabelEntry findEntry(Project project, final String locale, String key, Set<IPath> visited) {
		if(visited.contains(project.getAbsolutePath()))
			return null;

		visited.add(project.getAbsolutePath());

		NlsUnit[] units = project.getNLSUnits();

		for(NlsUnit unit : units) {
			if(unit.compareLocale(locale)) {
				unit.parse();

				String value = unit.getValue(key);

				if(value != null)
					return new LabelEntry(value, unit);
			}
		}

		for(Project reference : project.getReferencedProjects()) {
			LabelEntry entry = findEntry(reference, locale, key, visited);
			if(entry != null)
				return entry;
		}

		return null;
	}
}
