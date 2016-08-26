package org.zenframework.z8.compiler.content;

import org.zenframework.z8.compiler.workspace.NlsUnit;
import org.zenframework.z8.compiler.workspace.Project;

public class LabelProvider {
	static public LabelEntry getEntry(Project project, final String locale, String key) {
		NlsUnit[] units = project.getNLSUnits();

		for(NlsUnit unit : units) {
			if(unit.compareLocale(locale)) {
				unit.parse();

				String value = unit.getValue(key);

				if(value != null)
					return new LabelEntry(value, unit);
			}
		}
		return null;
	}
}
