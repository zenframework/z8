package org.zenframework.z8.pde.refactoring.rename;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

import org.zenframework.z8.pde.refactoring.wordIterator.WordIterator;

public class TextFieldNavigationHandler {
    public static void install(Text text) {
        if(isSubWordNavigationEnabled())
            new FocusHandler(new TextNavigable(text));
    }

    public static void install(StyledText styledText) {
        if(isSubWordNavigationEnabled())
            new FocusHandler(new StyledTextNavigable(styledText));
    }

    public static void install(Combo combo) {
        if(isSubWordNavigationEnabled())
            new FocusHandler(new ComboNavigable(combo));
    }

    private static boolean isSubWordNavigationEnabled() {
        return true;
    }

    private abstract static class WorkaroundNavigable extends Navigable {
        Point m_lastSelection;
        int m_caretPosition;

        void selectionChanged() {
            Point selection = getSelection();
            if(selection.equals(m_lastSelection)) {}
            else if(selection.x == selection.y) {
                m_caretPosition = selection.x;
            }
            else if(m_lastSelection.y == selection.y) {
                m_caretPosition = selection.x;
            }
            else {
                m_caretPosition = selection.y;
            }
            m_lastSelection = selection;
        }
    }

    private abstract static class Navigable {
        public abstract Control getControl();

        public abstract String getText();

        public abstract void setText(String text);

        public abstract Point getSelection();

        public abstract void setSelection(int start, int end);

        public abstract int getCaretPosition();
    }

    private static class TextNavigable extends WorkaroundNavigable {
        static final boolean BUG_106024_TEXT_SELECTION = "win32".equals(SWT.getPlatform())
                || "carbon".equals(SWT.getPlatform());

        private final Text m_text;

        public TextNavigable(Text text) {
            m_text = text;

            if(BUG_106024_TEXT_SELECTION) {
                m_lastSelection = getSelection();
                m_caretPosition = m_lastSelection.y;
                m_text.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        selectionChanged();
                    }
                });
                m_text.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseUp(MouseEvent e) {
                        selectionChanged();
                    }
                });
            }
        }

        @Override
        public Control getControl() {
            return m_text;
        }

        @Override
        public String getText() {
            return m_text.getText();
        }

        @Override
        public void setText(String text) {
            m_text.setText(text);
        }

        @Override
        public Point getSelection() {
            return m_text.getSelection();
        }

        @Override
        public int getCaretPosition() {
            if(BUG_106024_TEXT_SELECTION) {
                selectionChanged();
                return m_caretPosition;
            }
            else {
                return m_text.getCaretPosition();
            }
        }

        @Override
        public void setSelection(int start, int end) {
            m_text.setSelection(start, end);
        }
    }

    private static class StyledTextNavigable extends Navigable {
        private final StyledText m_styledText;

        public StyledTextNavigable(StyledText styledText) {
            m_styledText = styledText;
        }

        @Override
        public Control getControl() {
            return m_styledText;
        }

        @Override
        public String getText() {
            return m_styledText.getText();
        }

        @Override
        public void setText(String text) {
            m_styledText.setText(text);
        }

        @Override
        public Point getSelection() {
            return m_styledText.getSelection();
        }

        @Override
        public int getCaretPosition() {
            return m_styledText.getCaretOffset();
        }

        @Override
        public void setSelection(int start, int end) {
            m_styledText.setSelection(start, end);
        }
    }

    private static class ComboNavigable extends WorkaroundNavigable {
        private final Combo m_combo;

        public ComboNavigable(Combo combo) {
            m_combo = combo;
            m_lastSelection = getSelection();
            m_caretPosition = m_lastSelection.y;
            m_combo.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    selectionChanged();
                }
            });
            m_combo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    selectionChanged();
                }
            });
        }

        @Override
        public Control getControl() {
            return m_combo;
        }

        @Override
        public String getText() {
            return m_combo.getText();
        }

        @Override
        public void setText(String text) {
            m_combo.setText(text);
        }

        @Override
        public Point getSelection() {
            return m_combo.getSelection();
        }

        @Override
        public int getCaretPosition() {
            selectionChanged();
            return m_caretPosition;
        }

        @Override
        public void setSelection(int start, int end) {
            m_combo.setSelection(new Point(start, end));
        }
    }

    private static class FocusHandler implements FocusListener {
        private static final String EMPTY_TEXT = "";
        private final WordIterator m_iterator;
        private final Navigable m_navigable;
        private KeyAdapter m_keyListener;

        private FocusHandler(Navigable navigable) {
            m_iterator = new WordIterator();
            m_navigable = navigable;
            Control control = navigable.getControl();
            control.addFocusListener(this);
            if(control.isFocusControl())
                activate();
            control.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    deactivate();
                }
            });
        }

        @Override
        public void focusGained(FocusEvent e) {
            activate();
        }

        @Override
        public void focusLost(FocusEvent e) {
            deactivate();
        }

        private void activate() {
            m_navigable.getControl().addKeyListener(getKeyListener());
        }

        private void deactivate() {
            if(m_keyListener != null) {
                Control control = m_navigable.getControl();
                if(!control.isDisposed())
                    control.removeKeyListener(m_keyListener);
                m_keyListener = null;
            }
        }

        private KeyAdapter getKeyListener() {
            if(m_keyListener == null) {
                m_keyListener = new KeyAdapter() {
                    private static final String TEXT_EDITOR_CONTEXT_ID = "org.eclipse.ui.textEditorScope"; //$NON-NLS-1$
                    private final boolean IS_WORKAROUND = (m_navigable instanceof ComboNavigable)
                            || (m_navigable instanceof TextNavigable && TextNavigable.BUG_106024_TEXT_SELECTION);
                    private List<Submission> m_submissions;

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(IS_WORKAROUND) {
                            if(e.keyCode == SWT.ARROW_LEFT && e.stateMask == SWT.MOD2) {
                                int caretPosition = m_navigable.getCaretPosition();
                                if(caretPosition != 0) {
                                    Point selection = m_navigable.getSelection();
                                    if(caretPosition == selection.x)
                                        m_navigable.setSelection(selection.y, caretPosition - 1);
                                    else
                                        m_navigable.setSelection(selection.x, caretPosition - 1);
                                }
                                e.doit = false;
                                return;
                            }
                            else if(e.keyCode == SWT.ARROW_RIGHT && e.stateMask == SWT.MOD2) {
                                String text = m_navigable.getText();
                                int caretPosition = m_navigable.getCaretPosition();
                                if(caretPosition != text.length()) {
                                    Point selection = m_navigable.getSelection();
                                    if(caretPosition == selection.y)
                                        m_navigable.setSelection(selection.x, caretPosition + 1);
                                    else
                                        m_navigable.setSelection(selection.y, caretPosition + 1);
                                }
                                e.doit = false;
                                return;
                            }
                        }
                        int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
                        KeySequence keySequence = KeySequence.getInstance(SWTKeySupport
                                .convertAcceleratorToKeyStroke(accelerator));
                        getSubmissions();
                        for(Iterator<Submission> iter = getSubmissions().iterator(); iter.hasNext();) {
                            Submission submission = iter.next();
                            TriggerSequence[] triggerSequences = submission.getTriggerSequences();
                            for(int i = 0; i < triggerSequences.length; i++) {
                                if(triggerSequences[i].equals(keySequence)) {
                                    e.doit = false;
                                    submission.execute();
                                    return;
                                }
                            }
                        }
                    }

                    private List<Submission> getSubmissions() {
                        if(m_submissions != null)
                            return m_submissions;

                        m_submissions = new ArrayList<Submission>();

                        IContextService contextService = (IContextService)PlatformUI.getWorkbench().getAdapter(
                                IContextService.class);
                        ICommandService commandService = (ICommandService)PlatformUI.getWorkbench().getAdapter(
                                ICommandService.class);
                        IHandlerService handlerService = (IHandlerService)PlatformUI.getWorkbench().getAdapter(
                                IHandlerService.class);
                        IBindingService bindingService = (IBindingService)PlatformUI.getWorkbench().getAdapter(
                                IBindingService.class);

                        if(contextService == null || commandService == null || handlerService == null
                                || bindingService == null)
                            return m_submissions;

                        IContextActivation[] contextActivations;
                        contextActivations = new IContextActivation[] {
                                contextService.activateContext(IContextService.CONTEXT_ID_WINDOW),
                                contextService.activateContext(TEXT_EDITOR_CONTEXT_ID) };

                        m_submissions.add(new Submission(bindingService
                                .getActiveBindingsFor(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT)) {
                            @Override
                            public void execute() {
                                m_iterator.setText(m_navigable.getText());
                                int caretPosition = m_navigable.getCaretPosition();
                                int newCaret = m_iterator.following(caretPosition);
                                if(newCaret != BreakIterator.DONE) {
                                    Point selection = m_navigable.getSelection();
                                    if(caretPosition == selection.y)
                                        m_navigable.setSelection(selection.x, newCaret);
                                    else
                                        m_navigable.setSelection(selection.y, newCaret);
                                }
                                m_iterator.setText(EMPTY_TEXT);
                            }
                        });
                        m_submissions.add(new Submission(bindingService
                                .getActiveBindingsFor(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS)) {
                            @Override
                            public void execute() {
                                m_iterator.setText(m_navigable.getText());
                                int caretPosition = m_navigable.getCaretPosition();
                                int newCaret = m_iterator.preceding(caretPosition);
                                if(newCaret != BreakIterator.DONE) {
                                    Point selection = m_navigable.getSelection();
                                    if(caretPosition == selection.x)
                                        m_navigable.setSelection(selection.y, newCaret);
                                    else
                                        m_navigable.setSelection(selection.x, newCaret);
                                }
                                m_iterator.setText(EMPTY_TEXT);
                            }
                        });
                        m_submissions.add(new Submission(bindingService
                                .getActiveBindingsFor(ITextEditorActionDefinitionIds.WORD_NEXT)) {
                            @Override
                            public void execute() {
                                m_iterator.setText(m_navigable.getText());
                                int caretPosition = m_navigable.getCaretPosition();
                                int newCaret = m_iterator.following(caretPosition);
                                if(newCaret != BreakIterator.DONE)
                                    m_navigable.setSelection(newCaret, newCaret);
                                m_iterator.setText(EMPTY_TEXT);
                            }
                        });
                        m_submissions.add(new Submission(bindingService
                                .getActiveBindingsFor(ITextEditorActionDefinitionIds.WORD_PREVIOUS)) {
                            @Override
                            public void execute() {
                                m_iterator.setText(m_navigable.getText());
                                int caretPosition = m_navigable.getCaretPosition();
                                int newCaret = m_iterator.preceding(caretPosition);
                                if(newCaret != BreakIterator.DONE)
                                    m_navigable.setSelection(newCaret, newCaret);
                                m_iterator.setText(EMPTY_TEXT);
                            }
                        });
                        m_submissions.add(new Submission(bindingService
                                .getActiveBindingsFor(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD)) {
                            @Override
                            public void execute() {
                                Point selection = m_navigable.getSelection();
                                String text = m_navigable.getText();
                                int start;
                                int end;
                                if(selection.x != selection.y) {
                                    start = selection.x;
                                    end = selection.y;
                                }
                                else {
                                    m_iterator.setText(text);
                                    start = m_navigable.getCaretPosition();
                                    end = m_iterator.following(start);
                                    m_iterator.setText(EMPTY_TEXT);
                                    if(end == BreakIterator.DONE)
                                        return;
                                }
                                m_navigable.setText(text.substring(0, start) + text.substring(end));
                                m_navigable.setSelection(start, start);
                            }
                        });
                        m_submissions.add(new Submission(bindingService
                                .getActiveBindingsFor(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD)) {
                            @Override
                            public void execute() {
                                Point selection = m_navigable.getSelection();
                                String text = m_navigable.getText();
                                int start;
                                int end;
                                if(selection.x != selection.y) {
                                    start = selection.x;
                                    end = selection.y;
                                }
                                else {
                                    m_iterator.setText(text);
                                    end = m_navigable.getCaretPosition();
                                    start = m_iterator.preceding(end);
                                    m_iterator.setText(EMPTY_TEXT);
                                    if(start == BreakIterator.DONE)
                                        return;
                                }
                                m_navigable.setText(text.substring(0, start) + text.substring(end));
                                m_navigable.setSelection(start, start);
                            }
                        });
                        for(int i = 0; i < contextActivations.length; i++) {
                            contextService.deactivateContext(contextActivations[i]);
                        }
                        return m_submissions;
                    }
                };
            }
            return m_keyListener;
        }
    }

    private abstract static class Submission {
        private TriggerSequence[] m_triggerSequences;

        public Submission(TriggerSequence[] triggerSequences) {
            m_triggerSequences = triggerSequences;
        }

        public TriggerSequence[] getTriggerSequences() {
            return m_triggerSequences;
        }

        public abstract void execute();
    }
}
