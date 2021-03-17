package org.zenframework.z8.compiler.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.zenframework.z8.compiler.content.Hyperlink;
import org.zenframework.z8.compiler.content.HyperlinkKind;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;

public class DocsGenerator {
	private Project[] projects;
	private IPath outputPath;
	private IPath templatePath;

	public DocsGenerator(Project[] projects, IPath outputPath, IPath templatePath) {
		this.projects = projects;
		this.outputPath = outputPath;
		this.templatePath = templatePath;
	}

	private class Link implements Comparable<Link> {
		public IPosition position;
		public Hyperlink hyperlink;

		public Link(IPosition position, Hyperlink hyperlink) {
			this.position = position;
			this.hyperlink = hyperlink;
		}

		@Override
		public int compareTo(Link link) {
			return position.getOffset() - link.position.getOffset();
		}
	}

	public void run() throws FileException {
		int compilationUnits = 0;
		int projectCount = 0;

		System.out.println("Generating sources: " +  outputPath + " ...");

		for(Project project : projects) {
			compilationUnits += run(project);
			projectCount++;
		}

		System.out.println("Sources: project(s) " +  projectCount + ", files " + compilationUnits);
	}

	public int run(Project project) throws FileException {
		File.fromPath(outputPath).makeDirectories();

		String template = new String(File.fromPath(templatePath).read());

		CompilationUnit[] compilationUnits = project.getCompilationUnits();

		for(CompilationUnit compilationUnit : compilationUnits) {
			File file = File.fromPath(compilationUnit.getAbsolutePath());
			String content = new String(file.read());

			Map<IPosition, Hyperlink> hyperlinks = compilationUnit.getHyperlinks();

			if(hyperlinks == null)
				continue;

			List<DocsGenerator.Link> links = new ArrayList<DocsGenerator.Link>();

			for(Map.Entry<IPosition, Hyperlink> entry : hyperlinks.entrySet()) {
				Link link = new Link(entry.getKey(), entry.getValue());
				links.add(link);
			}

			Collections.sort(links);

			String result = "";
			int start = 0;

			for(Link link : links) {
				IPosition from = link.position;
				Hyperlink hyperlink = link.hyperlink;
				HyperlinkKind kind = hyperlink.getKind();
				IPosition to = hyperlink.getPosition();

				boolean self = compilationUnit == hyperlink.getCompilationUnit() && from.getOffset() == to.getOffset(); 
				String url = hyperlink.getCompilationUnit().getQualifiedName();

				int offset = from.getOffset();

				if(offset <= start)
					continue;

				int length = from.getLength();

				result += decorate(content.substring(start, offset));
				result += (self ? "<span id='sc" + offset : "<a href='" + url + "#sc" + to.getOffset()) + "'" + getHtmlClass(kind) + ">";
				result += decorate(content.substring(offset, offset + length));
				result += self ? "</span>" : "</a>";

				start = offset + length;
			}

			result += decorate(content.substring(start));

			String name = compilationUnit.getQualifiedName();
			result = template.replace("{0}", name).replace("{1}", result);

			File.fromPath(outputPath.append(name)).write(result);
		}

		return compilationUnits.length;
	}

	private String decorate(String text) {
		return text.replaceAll("<", "&lt;").
				// decorate keywords
				replaceAll("(\\b(public|protected|private|virtual|auto|class|enum|extends|import|static|final|this|super|container|new|records|return|break|if|else|for|while|do|try|catch|finally|throw)\\b)", "<span class='keyword'>$1</span>").
				// decorate comments
				replaceAll("(/\\*+((([^\\*])+)|([\\*]+(?!/)))[*]+/|//.*)", "<span class='comment'>$1</span>");
	}

	private String getHtmlClass(HyperlinkKind kind) {
		switch(kind) {
		case Type: return " class='type'";
		case Enum: return " class='enum'";
		case Attribute: return " class='attribute'";
		case Literal: return " class='literal'";
		case Record: return " class='record'";
		case Local: return " class='local'";
		case Member: return " class='member'";
		case Method: return " class='method'";
		case StaticMember: return " class='static member'";
		case StaticMethod: return " class='static method'";
		default: return "";
		}
	}
}
