package org.zenframework.z8.pde.editor.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import org.zenframework.z8.compiler.content.TypeHyperlink;
import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.compiler.workspace.ResourceVisitor;
import org.zenframework.z8.pde.source.InitHelper;

public class FindEntryPointPathsHyperlink implements IHyperlink {

	private IRegion region;

	private IType to;

	private List<String> msgs = new ArrayList<String>(2);

	private Stack<IType> classes = new Stack<IType>();

	private Stack<IMember> m_members = new Stack<IMember>();

	private CompilationUnit cUnit;

	private final static String SEP = " -> ";

	public FindEntryPointPathsHyperlink(IPosition position, TypeHyperlink hyp) {
		this.region = new Region(position.getOffset(), position.getLength());
		this.to = hyp.getType();
		cUnit = hyp.getCompilationUnit();
	}

	private void createMsgs() {
		classes.clear();
		msgs.clear();

		List<Project> projects = new ArrayList<Project>();
		projects.add(cUnit.getProject());
		for(Project p : cUnit.getProject().getReferencedProjects())
			projects.add(p);
		for(Project p : projects)
			p.iterate(new ResourceVisitor() {
				@Override
				public boolean visit(CompilationUnit compilationUnit) {
					IType candidate = compilationUnit.getReconciledType();
					if(candidate != null)
						if(candidate.getAttribute(IAttribute.Entry) != null)
							findPath(candidate);
					return true;
				}
			});
	}

	private void findPath(IType from) {
		if(from.equals(to)) {
			String msg;
			int size = classes.size();
			if(size > 0) {
				msg = "[" + classes.get(0).getCompilationUnit().getPath() + "] " + classes.get(0).getUserName() + SEP;
				for(int i = 1; i < size; i++)
					msg = msg + m_members.get(i - 1).getVariableType().getSignature() + " " + m_members.get(i - 1).getName() + SEP;
				msg = msg + to.getUserName() + " " + m_members.get(size - 1).getName() + ";";
			} else
				msg = "[" + from.getCompilationUnit().getPath() + "] " + from.getUserName() + " - ����� �����;";
			msgs.add(msg);
			return;
		}
		if(!from.isSubtypeOf("Desktop"))
			return;
		if(classes.contains(from))
			return;
		classes.push(from);
		for(IMember member : from.getAllMembers()) {
			if(member instanceof IMethod)
				continue;
			m_members.push(member);
			findPath(new InitHelper(from, member.getName()).getType());
			m_members.pop();
		}
		classes.pop();
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return region;
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return "Path to Desktop Hyperlink";
	}

	@Override
	public void open() {
		createMsgs();
		String msg = "";
		for(String m : msgs)
			msg = msg + m + "\r\n";
		if(msg.equals(""))
			msg = "����� �� �������.";
		MessageDialog.openInformation(null, "����� ����� �� ����� ����� �� ������ " + to.getUserName() + ".", msg);
	}
}
