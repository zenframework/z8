package org.zenframework.z8.pde.refactoring;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariable;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.pde.ImageDescriptorRegistry;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.messages.LanguageElementLabels;

public class BindingLabelProvider extends LabelProvider {
    private static int getAdornmentFlags(ILanguageElement element, int flags) {
        int adornments = 0;

        if(element instanceof IMember) {
            IMember member = (IMember)element;

            if(member.isFinal())
                adornments |= LanguageElementImageDescriptor.FINAL;
            if(member.isStatic())
                adornments |= LanguageElementImageDescriptor.STATIC;
        }

        return adornments;
    }

    private static ImageDescriptor getBaseImageDescriptor(ILanguageElement element, int flags) {
        if(element instanceof IType) {
            return getImageDescriptor((IType)element, flags);
        }
        if(element instanceof IMethod) {
            return getImageDescriptor((IMethod)element);
        }
        else if(element instanceof IMember) {
            return getImageDescriptor((IMember)element);
        }
        return PluginImages.DESC_OBJS_UNKNOWN;
    }

    private static ImageDescriptor getImageDescriptor(IMethod method) {
        if(method.isPublic()) {
            return PluginImages.DESC_MISC_PUBLIC;
        }

        if(method.isProtected()) {
            return PluginImages.DESC_MISC_PROTECTED;
        }

        if(method.isPrivate()) {
            return PluginImages.DESC_MISC_PRIVATE;
        }

        return PluginImages.DESC_MISC_DEFAULT;
    }

    private static ImageDescriptor getImageDescriptor(IMember member) {
        if(member.isPublic() || member.getDeclaringType().isEnum()) {
            return PluginImages.DESC_FIELD_PUBLIC;
        }

        if(member.isProtected()) {
            return PluginImages.DESC_FIELD_PROTECTED;
        }

        if(member.isPrivate()) {
            return PluginImages.DESC_FIELD_PRIVATE;
        }

        return PluginImages.DESC_FIELD_DEFAULT;
    }

    private static void getLabel(IVariable variable, long flags, StringBuffer buffer) {
        boolean isMember = variable instanceof IMember;
        IMember member = (IMember)variable;

        if((flags & LanguageElementLabels.F_PRE_TYPE_SIGNATURE) != 0) {
            if(!(isMember && member.getDeclaringType().isEnum())) {
                getLabel(variable.getVariableType(), (flags & LanguageElementLabels.T_TYPE_PARAMETERS), buffer);
                buffer.append(' ');
            }
        }

        if((flags & LanguageElementLabels.F_FULLY_QUALIFIED) != 0 && isMember) {
            getLabel(member.getDeclaringType(), LanguageElementLabels.T_FULLY_QUALIFIED | flags, buffer);
            buffer.append('.');
        }

        buffer.append(variable.getName());
    }

    private static void getLabel(IVariableType variableType, long flags, StringBuffer buffer) {}

    private static void getLabel(IMethod method, long flags, StringBuffer buffer) {
        if(((flags & LanguageElementLabels.M_PRE_RETURNTYPE) != 0)) {
            getLabel(method.getVariableType(), (flags & LanguageElementLabels.T_TYPE_PARAMETERS), buffer);
            buffer.append(' ');
        }

        if((flags & LanguageElementLabels.M_FULLY_QUALIFIED) != 0) {
            getLabel(method.getDeclaringType(), LanguageElementLabels.T_FULLY_QUALIFIED | flags, buffer);
            buffer.append('.');
        }

        buffer.append(method.getName());

        buffer.append('(');

        IVariable[] arguments = method.getParameters();

        if((flags & LanguageElementLabels.M_PARAMETER_TYPES) != 0) {
            for(int index = 0; index < arguments.length; index++) {
                if(index > 0) {
                    buffer.append(LanguageElementLabels.COMMA_STRING);
                }

                IVariable argument = arguments[index];
                IVariableType variableType = argument.getVariableType();
                getLabel(variableType, (flags & LanguageElementLabels.T_TYPE_PARAMETERS), buffer);

                if((flags & LanguageElementLabels.M_PARAMETER_NAMES) != 0) {
                    buffer.append(' ');
                    buffer.append(argument.getName());
                }
            }
        }
        else {
            if(arguments.length > 0) {
                buffer.append(LanguageElementLabels.ELLIPSIS_STRING);
            }
        }

        buffer.append(')');
    }

    private static ImageDescriptor getImageDescriptor(IType type, int flags) {
        if(type.isEnum()) {
            return PluginImages.DESC_OBJS_ENUM;
        }

        if((flags & LanguageElementImageProvider.LIGHT_TYPE_ICONS) != 0) {
            return PluginImages.DESC_OBJS_CLASSALT;
        }

        return PluginImages.DESC_OBJS_CLASS_DEFAULT;
    }

    private static void getLabel(IType type, long flags, StringBuffer buffer) {
        if((flags & LanguageElementLabels.T_FULLY_QUALIFIED) != 0 && !type.isPrimary()) {
            buffer.append(type.getQualifiedUserName());
        }
        else {
            buffer.append(type.getUserName());
        }
    }

    public static String getLabel(ILanguageElement element, long flags) {
        StringBuffer buffer = new StringBuffer(60);

        if(element instanceof IType) {
            getLabel((IType)element, flags, buffer);
        }
        else if(element instanceof IMember) {
            getLabel((IMethod)element, flags, buffer);
        }
        else if(element instanceof IMember) {
            getLabel((IMember)element, flags, buffer);
        }
        return buffer.toString();
    }

    public static ImageDescriptor getImageDescriptor(ILanguageElement element, int imageFlags) {
        ImageDescriptor baseImage = getBaseImageDescriptor(element, imageFlags);

        if(baseImage != null) {
            int adornmentFlags = getAdornmentFlags(element, imageFlags);
            Point size = ((imageFlags & LanguageElementImageProvider.SMALL_ICONS) != 0) ? LanguageElementImageProvider.SMALL_SIZE
                    : LanguageElementImageProvider.BIG_SIZE;
            return new LanguageElementImageDescriptor(baseImage, adornmentFlags, size);
        }
        return null;
    }

    public static final long DEFAULT_TEXTFLAGS = LanguageElementLabels.ALL_DEFAULT;
    public static final int DEFAULT_IMAGEFLAGS = LanguageElementImageProvider.OVERLAY_ICONS;

    final private long fTextFlags;
    final private int fImageFlags;

    private ImageDescriptorRegistry fRegistry;

    public BindingLabelProvider() {
        this(DEFAULT_TEXTFLAGS, DEFAULT_IMAGEFLAGS);
    }

    public BindingLabelProvider(final long textFlags, final int imageFlags) {
        fImageFlags = imageFlags;
        fTextFlags = textFlags;
        fRegistry = null;
    }

    @Override
    public Image getImage(Object element) {
        if(element instanceof ILanguageElement) {
            ImageDescriptor baseImage = getImageDescriptor((ILanguageElement)element, fImageFlags);

            if(baseImage != null) {
                return getRegistry().get(baseImage);
            }
        }
        return super.getImage(element);
    }

    private ImageDescriptorRegistry getRegistry() {
        if(fRegistry == null) {
            fRegistry = Plugin.getImageDescriptorRegistry();
        }

        return fRegistry;
    }

    @Override
    public String getText(Object element) {
        if(element instanceof ILanguageElement) {
            return getLabel((ILanguageElement)element, fTextFlags);
        }
        return super.getText(element);
    }
}
