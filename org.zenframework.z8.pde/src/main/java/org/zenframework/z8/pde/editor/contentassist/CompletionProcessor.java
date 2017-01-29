package org.zenframework.z8.pde.editor.contentassist;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.part.FileEditorInput;

import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.view.SourceViewer;
import org.zenframework.z8.pde.refactoring.LanguageElementImageProvider;

public class CompletionProcessor implements IContentAssistProcessor {
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {
		protected int fInstallOffset;

		@Override
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		@Override
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset = offset;
		}

		@Override
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected final static String[] fgProposals = { "binary", "bool", "break", "catch", "class", "container", "continue", "date", "datetime", "do", "decimal", "else", "extends", "false", "finally", "for", "guid", "if", "int", "native", "new", "null", "private", "protected",
			"public", "return", "static", "super", "this", "throw", "true", "try", "void", "while" };

	protected IContextInformationValidator fValidator = new Validator();

	// private ICompletionProposal[] proposals;

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer v, int documentOffset) {
		SourceViewer viewer = (SourceViewer)v;
		FileEditorInput fileInput = (FileEditorInput)viewer.getEditor().getEditorInput();
		String str = getCharsAfterDot(v, documentOffset);
		if(str == null)
			return null;
		int len = str.length();
		ICompletionProposal[] proposals;
		int offset = documentOffset - len;
		IFile resource = fileInput.getFile();

		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

		IVariableType type = compilationUnit.getContentProposal(offset);

		if(type == null) {
			return null;
		}

		proposals = getProposals(type, offset);

		List<ICompletionProposal> show = new ArrayList<ICompletionProposal>(3);
		if(proposals == null)
			return null;
		for(int i = 0; i < proposals.length; i++) {
			String prStr = proposals[i].getDisplayString();
			if(prStr.length() >= len && str.compareToIgnoreCase(prStr.substring(0, len)) == 0) {
				show.add(proposals[i]);
				((CompletionProposal)proposals[i]).setReplacementLength(len);
			}
		}
		if(show.size() > 0) {
			ICompletionProposal[] result = new ICompletionProposal[show.size()];
			show.toArray(result);
			return result;
		}
		return null;
	}

	private String getCharsAfterDot(ITextViewer v, int off) {
		int i = 0;
		int of = off - 1;
		char[] chars = new char[128]; // ������ �������
		try {
			char c = v.getDocument().getChar(of);
			while(c != '.' && c != '(') {
				if(i > 127 || of == 0)
					return null;
				chars[i] = c;
				i++;
				of--;
				c = v.getDocument().getChar(of);
			}
		} catch(Exception e) {
			Plugin.log(e);
		}
		char[] result = new char[i];
		for(int j = 0; j < i; j++) {
			result[j] = chars[i - j - 1];
		}
		return new String(result);
	}

	private ICompletionProposal[] getProposals(IVariableType variableType, int documentOffset) {
		LanguageElementImageProvider leip = new LanguageElementImageProvider();

		IMember[] members = getMembers(variableType);

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		for(IMember member : members) {
			if(!member.isPublic())
				continue;

			IType container = member.getDeclaringType();
			int membersCount = 0;

			if(member instanceof IMethod) {
				IMethod method = (IMethod)member;
				String text = method.getUserName() + " " + method.getVariableType().getSignature() + " - " + variableType.getSignature();
				IContextInformation info = new ContextInformation(text, text);
				int mv = method.getParametersCount() > 0 ? 1 : 2;
				ICompletionProposal proposal = new CompletionProposal(method.getName() + "()", documentOffset, 0, method.getName().length() + mv, leip.getImageLabel(member, LanguageElementImageProvider.SMALL_ICONS), text, info, text);
				proposals.add(proposal);
			} else {
				String text = member.getUserName() + " - " + container.getUserName();
				IContextInformation info = new ContextInformation(text, text);
				ICompletionProposal proposal = new CompletionProposal(member.getName(), documentOffset, 0, member.getName().length(), leip.getImageLabel(member, LanguageElementImageProvider.SMALL_ICONS), text, info, text);
				proposals.add(membersCount, proposal);
				membersCount++;
			}
		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private IMember[] getMembers(IVariableType variableType) {
		IMember[] members = variableType.getAllMembers();

		Arrays.sort(members, new MemberComparator());

		return members;
	}

	class MemberComparator implements Comparator<IMember> {
		@Override
		public int compare(IMember left, IMember right) {
			return left.getUserName().compareToIgnoreCase(right.getUserName());
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		IContextInformation[] result = new IContextInformation[5];
		for(int i = 0; i < result.length; i++)
			result[i] = new ContextInformation(MessageFormat.format(Z8EditorMessages.getString("CompletionProcessor.ContextInfo.display.pattern"), new Object[] { new Integer(i), new Integer(documentOffset) }), //$NON-NLS-1$
					MessageFormat.format(Z8EditorMessages.getString("CompletionProcessor.ContextInfo.value.pattern"), new Object[] { new Integer(i), new Integer(documentOffset - 5), new Integer(documentOffset + 5) })); //$NON-NLS-1$
		return result;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '(' };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '#' };
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
}
