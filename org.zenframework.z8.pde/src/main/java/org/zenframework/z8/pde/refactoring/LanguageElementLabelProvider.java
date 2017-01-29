package org.zenframework.z8.pde.refactoring;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;

public class LanguageElementLabelProvider extends LabelProvider {
	public final static int SHOW_RETURN_TYPE = 0x001;
	public final static int SHOW_PARAMETERS = 0x002;
	public final static int SHOW_OVERLAY_ICONS = 0x010;
	public final static int SHOW_TYPE = 0x020;
	public final static int SHOW_ROOT = 0x040;
	public final static int SHOW_SMALL_ICONS = 0x100;
	public final static int SHOW_VARIABLE = 0x200;
	public final static int SHOW_QUALIFIED = 0x400;
	public final static int SHOW_POST_QUALIFIED = 0x800;
	public final static int SHOW_BASICS = 0x000;

	public final static int SHOW_DEFAULT = new Integer(SHOW_PARAMETERS | SHOW_OVERLAY_ICONS).intValue();

	private LanguageElementImageProvider m_imageLabelProvider;

	private int m_flags;
	private int m_imageFlags;
	private long m_textFlags;

	public LanguageElementLabelProvider() {
		this(SHOW_DEFAULT);
	}

	public LanguageElementLabelProvider(int flags) {
		m_imageLabelProvider = new LanguageElementImageProvider();
		m_flags = flags;
		updateImageProviderFlags();
		updateTextProviderFlags();
	}

	private boolean getFlag(int flag) {
		return (m_flags & flag) != 0;
	}

	public void turnOn(int flags) {
		m_flags |= flags;
		updateImageProviderFlags();
		updateTextProviderFlags();
	}

	public void turnOff(int flags) {
		m_flags &= (~flags);
		updateImageProviderFlags();
		updateTextProviderFlags();
	}

	private void updateImageProviderFlags() {
		m_imageFlags = 0;

		if(getFlag(SHOW_OVERLAY_ICONS)) {
			m_imageFlags |= LanguageElementImageProvider.OVERLAY_ICONS;
		}
		if(getFlag(SHOW_SMALL_ICONS)) {
			m_imageFlags |= LanguageElementImageProvider.SMALL_ICONS;
		}
	}

	private void updateTextProviderFlags() {
		m_textFlags = LanguageElementLabels.T_TYPE_PARAMETERS;

		if(getFlag(SHOW_RETURN_TYPE)) {
			m_textFlags |= LanguageElementLabels.M_APP_RETURNTYPE;
		}
		if(getFlag(SHOW_PARAMETERS)) {
			m_textFlags |= LanguageElementLabels.M_PARAMETER_TYPES;
		}
		if(getFlag(SHOW_TYPE)) {
			m_textFlags |= LanguageElementLabels.F_APP_TYPE_SIGNATURE;
		}
		if(getFlag(SHOW_ROOT)) {
			m_textFlags |= LanguageElementLabels.APPEND_ROOT_PATH;
		}
		if(getFlag(SHOW_VARIABLE)) {
			m_textFlags |= LanguageElementLabels.ROOT_VARIABLE;
		}
		if(getFlag(SHOW_QUALIFIED)) {
			m_textFlags |= (LanguageElementLabels.F_FULLY_QUALIFIED | LanguageElementLabels.M_FULLY_QUALIFIED | LanguageElementLabels.I_FULLY_QUALIFIED | LanguageElementLabels.T_FULLY_QUALIFIED | LanguageElementLabels.D_QUALIFIED | LanguageElementLabels.CF_QUALIFIED
					| LanguageElementLabels.CU_QUALIFIED);
		}
		if(getFlag(SHOW_POST_QUALIFIED)) {
			m_textFlags |= (LanguageElementLabels.F_POST_QUALIFIED | LanguageElementLabels.M_POST_QUALIFIED | LanguageElementLabels.I_POST_QUALIFIED | LanguageElementLabels.T_POST_QUALIFIED | LanguageElementLabels.D_POST_QUALIFIED | LanguageElementLabels.CF_POST_QUALIFIED
					| LanguageElementLabels.CU_POST_QUALIFIED);
		}
	}

	@Override
	public Image getImage(Object element) {
		return m_imageLabelProvider.getImageLabel(element, m_imageFlags);
	}

	@Override
	public String getText(Object element) {
		String text = LanguageElementLabels.getTextLabel(element, m_textFlags);

		if(text.length() > 0) {
			return text;
		}

		return text;
	}

	@Override
	public void dispose() {
		m_imageLabelProvider.dispose();
	}
}
