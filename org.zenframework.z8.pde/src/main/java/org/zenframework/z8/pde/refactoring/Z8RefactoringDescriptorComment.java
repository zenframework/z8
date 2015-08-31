package org.zenframework.z8.pde.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;
import org.zenframework.z8.pde.refactoring.messages.Messages;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;
import org.zenframework.z8.pde.refactoring.rename.IDelegateUpdating;
import org.zenframework.z8.pde.refactoring.rename.INameUpdating;

public class Z8RefactoringDescriptorComment {
    private static final String ELEMENT_DELIMITER = RefactoringMessages.RefactoringDescriptorComment_element_delimiter;
    private static final String LINE_DELIMITER = System.getProperty("line.separator", "\n");

    public static String createCompositeSetting(final String caption, final String[] settings) {
        StringBuffer buffer = new StringBuffer(128);
        buffer.append(caption);

        for(int index = 0; index < settings.length; index++) {
            if(settings[index] != null && !"".equals(settings[index])) {
                buffer.append(LINE_DELIMITER);
                buffer.append(ELEMENT_DELIMITER);
                buffer.append(settings[index]);
            }
            else {
                buffer.append(LINE_DELIMITER);
                buffer.append(ELEMENT_DELIMITER);
                buffer.append(RefactoringMessages.RefactoringDescriptor_not_available);
            }
        }
        return buffer.toString();
    }

    private String m_header;
    private List<String> m_settings = new ArrayList<String>(6);

    public Z8RefactoringDescriptorComment(Object object, String header) {
        m_header = header;
        initializeInferredSettings(object);
    }

    public void addSetting(int index, String setting) {
        m_settings.add(index, setting);
    }

    public void addSetting(String setting) {
        m_settings.add(setting);
    }

    public String asString() {
        StringBuffer buffer = new StringBuffer(256);
        buffer.append(m_header);

        for(String setting : m_settings) {
            buffer.append(LINE_DELIMITER);
            buffer.append(Messages.format(RefactoringMessages.RefactoringDescriptor_inferred_setting_pattern, setting));
        }
        return buffer.toString();
    }

    public int getCount() {
        return m_settings.size();
    }

    private void initializeInferredSettings(Object object) {
        if(object instanceof INameUpdating) {
            INameUpdating updating = (INameUpdating)object;
            m_settings.add(Messages
                    .format(RefactoringMessages.RefactoringDescriptor_original_element_pattern, LanguageElementLabels
                            .getTextLabel(updating.getElements()[0], LanguageElementLabels.ALL_FULLY_QUALIFIED)));

            try {
                Object element = updating.getNewElement();

                if(element != null)
                    m_settings.add(Messages.format(RefactoringMessages.RefactoringDescriptor_renamed_element_pattern,
                            LanguageElementLabels.getTextLabel(element, LanguageElementLabels.ALL_FULLY_QUALIFIED)));
                else {
                    String oldLabel = LanguageElementLabels.getTextLabel(updating.getElements()[0],
                            LanguageElementLabels.ALL_FULLY_QUALIFIED);
                    String newName = updating.getCurrentElementName();

                    if(newName.length() < oldLabel.length()) {
                        String newLabel = oldLabel.substring(0, oldLabel.length() - newName.length());
                        m_settings.add(Messages.format(RefactoringMessages.RefactoringDescriptor_renamed_element_pattern,
                                newLabel + updating.getNewElementName()));
                    }
                }
            }
            catch(CoreException exception) {
                Plugin.log(exception);
            }
        }
        else if(object instanceof RefactoringProcessor) {
            RefactoringProcessor processor = (RefactoringProcessor)object;
            Object[] elements = processor.getElements();

            if(elements != null) {
                if(elements.length == 1 && elements[0] != null) {
                    m_settings.add(Messages.format(RefactoringMessages.RefactoringDescriptor_original_element_pattern,
                            LanguageElementLabels.getTextLabel(elements[0], LanguageElementLabels.ALL_FULLY_QUALIFIED)));
                }
                else if(elements.length > 1) {
                    StringBuffer buffer = new StringBuffer(128);
                    buffer.append(RefactoringMessages.RefactoringDescriptor_original_elements);

                    for(int index = 0; index < elements.length; index++) {
                        if(elements[index] != null) {
                            buffer.append(LINE_DELIMITER);
                            buffer.append(ELEMENT_DELIMITER);
                            buffer.append(LanguageElementLabels.getTextLabel(elements[index],
                                    LanguageElementLabels.ALL_FULLY_QUALIFIED));
                        }
                        else {
                            buffer.append(LINE_DELIMITER);
                            buffer.append(ELEMENT_DELIMITER);
                            buffer.append(RefactoringMessages.RefactoringDescriptor_not_available);
                        }
                    }

                    m_settings.add(buffer.toString());
                }
            }
        }

        if(object instanceof IReferenceUpdating) {
            IReferenceUpdating updating = (IReferenceUpdating)object;
            if(updating.canEnableUpdateReferences() && updating.getUpdateReferences()) {
                m_settings.add(RefactoringMessages.RefactoringDescriptor_update_references);
            }
        }

        if(object instanceof ITextUpdating) {
            ITextUpdating updating = (ITextUpdating)object;

            if(updating.canEnableTextUpdating()) {
                m_settings.add(RefactoringMessages.RefactoringDescriptor_textual_occurrences);
            }
        }

        if(object instanceof IDelegateUpdating) {
            IDelegateUpdating updating = (IDelegateUpdating)object;

            if(updating.canEnableDelegateUpdating() && updating.getDelegateUpdating()) {
                if(updating.getDeprecateDelegates()) {
                    m_settings.add(RefactoringMessages.RefactoringDescriptor_keep_original_deprecated);
                }
                else {
                    m_settings.add(RefactoringMessages.RefactoringDescriptor_keep_original);
                }
            }
        }
    }

    public void removeSetting(int index) {
        m_settings.remove(index);
    }
}
