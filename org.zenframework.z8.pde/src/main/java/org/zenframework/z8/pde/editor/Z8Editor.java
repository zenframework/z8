package org.zenframework.z8.pde.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.EditorPluginAction;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ConfigurationElementSorter;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.zenframework.z8.compiler.content.Hyperlink;
import org.zenframework.z8.compiler.content.TypeHyperlink;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.compiler.workspace.ResourceListener;
import org.zenframework.z8.compiler.workspace.Workspace;
import org.zenframework.z8.pde.Z8EditorMessages;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.actions.CreateGUIDAction;
import org.zenframework.z8.pde.editor.actions.FindEntryPointPaths;
import org.zenframework.z8.pde.editor.actions.OpenDataSchemaAction;
import org.zenframework.z8.pde.editor.actions.OpenDeclarationAction;
import org.zenframework.z8.pde.editor.actions.OpenFormsEditorAction;
import org.zenframework.z8.pde.editor.actions.OpenTextHierarchyAction;
import org.zenframework.z8.pde.editor.actions.OrganizeImportsAction;
import org.zenframework.z8.pde.editor.document.CodeScanner;
import org.zenframework.z8.pde.editor.document.PartitionScanner;
import org.zenframework.z8.pde.editor.view.SourceViewer;
import org.zenframework.z8.pde.editor.view.SourceViewerConfiguration;
import org.zenframework.z8.pde.navigator.ClassesNavigator;

@SuppressWarnings("restriction")
public class Z8Editor extends TextEditor implements ResourceListener {
	public static final int PROP_CLOSE = 0x501;
	private static PartitionScanner m_partitionScanner;
	private static CodeScanner m_codeScanner;

	private ProjectionSupport fProjectionSupport;

	private ProjectionAnnotationModel projectionAnnotationModel;

	private ContentOutlinePage m_outlinePage;
	private List<Annotation> m_occurrenceAnnotations;

	private Annotation[] m_projectionAnnotations;

	private List<Annotation> m_overrideAnnotations;

	public Z8Editor() {
		super();
	}

	static public PartitionScanner getPartitionScanner() {
		if(m_partitionScanner == null) {
			m_partitionScanner = new PartitionScanner();
		}
		return m_partitionScanner;
	}

	static public RuleBasedScanner getCodeScanner() {
		if(m_codeScanner == null) {
			m_codeScanner = new CodeScanner(Plugin.getColorProvider());
		}
		return m_codeScanner;
	}

	@Override
	protected void editorSaved() {
		super.editorSaved();

		AnnotationModel model = (AnnotationModel)getAnnotationModel();
		model.removeAllAnnotations();

		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(getResource());

		if(compilationUnit != null) {
			compilationUnit.contentChanged();
		}
	}

	@Override
	protected void createActions() {
		super.createActions();

		IAction a = new TextOperationAction(Z8EditorMessages.getResourceBundle(), "ContentAssistProposal.", this, ISourceViewer.CONTENTASSIST_PROPOSALS);
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", a); //$NON-NLS-1$

		a = new TextOperationAction(Z8EditorMessages.getResourceBundle(), "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);
		a.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", a);

		a = new OpenDeclarationAction("OpenDeclaration.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.OPEN_DECLARATION);
		setAction("OpenDeclaration", a); //$NON-NLS-1$

		a = new OpenDataSchemaAction("OpenTablesEditor.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.OPEN_TABLES_EDITOR);
		setAction("OpenTablesEditor", a); //$NON-NLS-1$

		a = new FindEntryPointPaths("FindEntryPointPaths.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.FIND_ENTRYPOINT_PATHS);
		setAction("FindEntryPointPaths", a);

		a = new OpenTextHierarchyAction("OpenTextHierarchy.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.OPEN_TEXT_HIERARCHY);
		setAction("OpenTextHierarchy", a);

		a = new OrganizeImportsAction("OrganizeImports.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.ORGANIZE_IMPORTS);
		setAction("OrganizeImports", a);

		a = new OpenFormsEditorAction("OpenFormsEditor.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.OPEN_FORMS_EDITOR);
		setAction("OpenFormsEditor", a);

		a = new CreateGUIDAction("CreateGUID.", this);
		a.setActionDefinitionId(IEditorActionDefinitionIds.CREATE_GUID);
		setAction("CreateGUID", a);
	}

	@Override
	public void dispose() {
		if(m_outlinePage != null) {
			m_outlinePage.setInput(null);
		}
		if(fProjectionSupport != null) {
			fProjectionSupport.dispose();
			fProjectionSupport = null;
		}

		super.dispose();
	}

	@Override
	public void doRevertToSaved() {
		super.doRevertToSaved();
		if(m_outlinePage != null) {
			m_outlinePage.update();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		if(m_outlinePage != null) {
			m_outlinePage.update();
		}
	}

	@Override
	public void doSaveAs() {
		super.doSaveAs();
		if(m_outlinePage != null) {
			m_outlinePage.update();
		}
	}

	@Override
	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if(m_outlinePage != null) {
			m_outlinePage.setInput((FileEditorInput)input);
		}
	}

	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		addAction(menu, "ContentAssistProposal");
		addAction(menu, "ContentAssistTip");
		addAction(menu, "OpenDeclaration");
		// addAction(menu, "OpenTablesEditor");
		addAction(menu, "FindEntryPointPaths");
		addAction(menu, "OpenTextHierarchy");
		addAction(menu, "OrganizeImports");
		// addAction(menu, "OpenFormsEditor");
		addAction(menu, "CreateGUID");
		menu.add(new Separator());
		super.editorContextMenuAboutToShow(menu);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class required) {
		if(IContentOutlinePage.class.equals(required)) {
			if(m_outlinePage == null) {
				m_outlinePage = new ContentOutlinePage(getDocumentProvider(), this);
				if(getEditorInput() != null) {
					m_outlinePage.setInput((FileEditorInput)getEditorInput());
				}
			}
			return m_outlinePage;
		}
		if(required.equals(IContextProvider.class)) {
			return new IContextProvider() {

				@Override
				public int getContextChangeMask() {
					return NONE;
				}

				@Override
				public IContext getContext(Object target) {
					return null;
				}

				@Override
				public String getSearchExpression(Object target) {
					int pos = ((ITextSelection)getSelectionProvider().getSelection()).getOffset();
					IResource resource = ((FileEditorInput)getEditorInput()).getFile();
					CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

					IPosition position = compilationUnit.getHyperlinkPosition(pos);
					Hyperlink hyperlink = compilationUnit.getHyperlink(position);

					if(hyperlink != null) {
						if(hyperlink instanceof TypeHyperlink) {
							TypeHyperlink typeHyperlink = (TypeHyperlink)hyperlink;
							return typeHyperlink.getType().getUserName();
						} else
							return null;
					}
					return null;
				}

			};

		}

		if(fProjectionSupport != null) {
			Object adapter = fProjectionSupport.getAdapter(getSourceViewer(), required);
			if(adapter != null)
				return adapter;
		}

		return super.getAdapter(required);
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new SourceViewerConfiguration(this));
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = createAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());
		ISourceViewer viewer = new SourceViewer(this, parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		ProjectionViewer projectionViewer = (ProjectionViewer)getSourceViewer();
		fProjectionSupport = new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$

		fProjectionSupport.install();

		// turn projection mode on
		projectionViewer.doOperation(ProjectionViewer.TOGGLE);

		projectionAnnotationModel = projectionViewer.getProjectionAnnotationModel();

	}

	@Override
	protected void adjustHighlightRange(int offset, int length) {
		ISourceViewer viewer = getSourceViewer();

		if(viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5)viewer;
			extension.exposeModelRange(new Region(offset, length));
		}
	}

	public IResource getResource() {
		FileEditorInput input = (FileEditorInput)getEditorInput();
		return input != null ? input.getFile() : null;
	}

	@Override
	protected void handleCursorPositionChanged() {
		super.handleCursorPositionChanged();
		updateOccurrenceAnnotations();
	}

	@Override
	public void setFocus() {
		super.setFocus();
		updateClassesNavigatorSelection();
	}

	public void updateClassesNavigatorSelection() {
		IViewPart part = getSite().getPage().findView("org.zenframework.views.classes");

		if(part != null) {
			ClassesNavigator cn = (ClassesNavigator)part;

			if(cn.isLinkedWithEditor()) {
				CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(getResource());
				cn.updateSelection(compilationUnit);
			}
		}

	}

	private void updateOccurrenceAnnotations() {
		if(m_occurrenceAnnotations != null) {
			for(Annotation ann : m_occurrenceAnnotations)
				getAnnotationModel().removeAnnotation(ann);
		}
		m_occurrenceAnnotations = new ArrayList<Annotation>();
		int pos = ((TextSelection)getSelectionProvider().getSelection()).getOffset();

		IResource resource = getResource();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

		if(compilationUnit != null) {
			IPosition position = compilationUnit.getHyperlinkPosition(pos);
			Hyperlink hyperlink = compilationUnit.getHyperlink(position);
			if(hyperlink != null) {
				for(IPosition annPos : compilationUnit.getAllHyperlinkPositions(hyperlink)) {
					try {
						Annotation ann = new Annotation("org.eclipse.jdt.ui.occurrences", false, getDocument().get(annPos.getOffset(), annPos.getLength()));
						m_occurrenceAnnotations.add(ann);
						getAnnotationModel().addAnnotation(ann, new Position(annPos.getOffset(), annPos.getLength()));
					} catch(Exception e) {
					}
					;
				}
			}
		}
	}

	private void updateOverrideAnnotations() {
		if(m_overrideAnnotations != null) {
			for(Annotation ann : m_overrideAnnotations)
				getAnnotationModel().removeAnnotation(ann);
		}
		m_overrideAnnotations = new ArrayList<Annotation>();

		IResource resource = getResource();
		CompilationUnit compilationUnit = Workspace.getInstance().getCompilationUnit(resource);

		IType t1 = compilationUnit.getType();
		if(t1 == null)
			return;
		List<IType> types = new ArrayList<IType>();
		types.add(t1);
		for(IType t : t1.getNestedTypes()) {
			types.add(t);
		}

		for(IType t : types)
			for(IMethod m : t.getMethods()) {
				if(m.getBody() == null)
					continue;
				IType sType = t.getBaseType();
				while(sType != null) {
					IMethod m1 = sType.getMethod(m.getSignature());
					if(m1 != null) {
						IPosition pos = m.getNamePosition();
						boolean override = m1.getBody() != null;
						try {
							Annotation ann = new OverrideAnnotation(override, sType.getCompilationUnit().getQualifiedName() + "." + m.getSignature(), m1);
							m_overrideAnnotations.add(ann);
							getAnnotationModel().addAnnotation(ann, new Position(pos.getOffset(), pos.getLength()));
						} catch(Exception e) {
						}
						;
						break;
					}
					sType = sType.getBaseType();
				}
			}

	}

	public AnnotationModel getAnnotationModel() {
		AnnotationModel m = (AnnotationModel)getSourceViewer().getAnnotationModel();
		// m.addAnnotationModel(ProjectionSupport.PROJECTION, new
		// ProjectionAnnotationModel());
		return m;
	}

	public IDocument getDocument() {
		return getDocumentProvider().getDocument(getEditorInput());
	}

	public void beginCompoundChange() {
		((ITextViewerExtension6)getSourceViewer()).getUndoManager().beginCompoundChange();
	}

	public void endCompoundChange() {
		((ITextViewerExtension6)getSourceViewer()).getUndoManager().endCompoundChange();
	}

	@Override
	public IAction getAction(String actionID) {
		IAction action = super.getAction(actionID);
		if(action != null)
			return action;
		if(!getSite().getId().equals(""))
			return null;
		return myFindContributed(actionID);
	}

	private IAction myFindContributed(String actionID) {
		List<IConfigurationElement> actions = new ArrayList<IConfigurationElement>();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(PlatformUI.PLUGIN_ID, "editorActions"); //$NON-NLS-1$
		for(int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if("editorContribution".equals(element.getName())) {
				if(!"org.zenframework.z8.pde.Z8Editor".equals(element.getAttribute("targetID"))) //$NON-NLS-1$
					continue;

				IConfigurationElement[] children = element.getChildren("action"); //$NON-NLS-1$
				for(int j = 0; j < children.length; j++) {
					IConfigurationElement child = children[j];
					if(actionID.equals(child.getAttribute("actionID"))) //$NON-NLS-1$
						actions.add(child);
				}
			}
		}
		int actionSize = actions.size();
		if(actionSize > 0) {
			IConfigurationElement element;
			if(actionSize > 1) {
				IConfigurationElement[] actionArray = (IConfigurationElement[])actions.toArray(new IConfigurationElement[actionSize]);
				ConfigurationElementSorter sorter = new ConfigurationElementSorter() {
					/*
					 * @see
					 * org.eclipse.ui.texteditor.ConfigurationElementSorter#
					 * getConfigurationElement(java.lang.Object)
					 */
					@Override
					public IConfigurationElement getConfigurationElement(Object object) {
						return (IConfigurationElement)object;
					}
				};
				sorter.sort(actionArray);
				element = actionArray[0];
			} else
				element = (IConfigurationElement)actions.get(0);

			final String ATT_DEFINITION_ID = "definitionId";//$NON-NLS-1$
			String defId = element.getAttribute(ATT_DEFINITION_ID);
			return new EditorPluginAction(element, this, defId, IAction.AS_UNSPECIFIED);
		}

		return null;
	}

	@Override
	public void event(int type, Resource resource, Object object) {
		if(resource instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit)resource;

			updateFoldingAnnotations(cu);
			updateOverrideAnnotations();

		}
	}

	protected void updateFoldingAnnotations(CompilationUnit cu) {
		List<IPosition> poss = new ArrayList<IPosition>();

		IType t = cu.getReconciledType();

		if(t != null) {
			poss.add(t.getSourceRange());

			for(IType t1 : t.getNestedTypes()) {
				if(t1.getTypeBody() != null)
					poss.add(t1.getSourceRange());
			}

			for(IMethod m : t.getMethods())
				if(m.getBody() != null)
					poss.add(m.getSourceRange());

			if(t.getImportBlock() != null)
				poss.add(t.getImportBlock().getSourceRange());
		}

		Annotation[] annotations = new Annotation[poss.size()];
		// this will hold the new annotations along
		// with their corresponding positions
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();

		for(int i = 0; i < poss.size(); i++) {

			ProjectionAnnotation annotation = new ProjectionAnnotation();
			newAnnotations.put(annotation, new Position(poss.get(i).getOffset(), poss.get(i).getLength()));

			annotations[i] = annotation;
		}

		projectionAnnotationModel.modifyAnnotations(m_projectionAnnotations, newAnnotations, null);

		m_projectionAnnotations = annotations;
	}

	@Override
	public void close(final boolean save) {
		super.close(save);
		firePropertyChange(PROP_CLOSE);
	}

}
