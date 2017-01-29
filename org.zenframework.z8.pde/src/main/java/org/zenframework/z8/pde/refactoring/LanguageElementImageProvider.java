package org.zenframework.z8.pde.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.compiler.workspace.Folder;
import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.PluginImages;

public class LanguageElementImageProvider {
	public final static int OVERLAY_ICONS = 0x1;
	public final static int SMALL_ICONS = 0x2;
	public final static int LIGHT_TYPE_ICONS = 0x4;
	public static final Point SMALL_SIZE = new Point(16, 16);
	public static final Point BIG_SIZE = new Point(22, 16);
	private static ImageDescriptor DESC_OBJ_PROJECT_CLOSED;
	private static ImageDescriptor DESC_OBJ_PROJECT;
	private static ImageDescriptor DESC_OBJ_FOLDER;

	{
		ISharedImages images = Plugin.getDefault().getWorkbench().getSharedImages();
		DESC_OBJ_PROJECT_CLOSED = images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED);
		DESC_OBJ_PROJECT = images.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
		DESC_OBJ_FOLDER = images.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
	}

	public LanguageElementImageProvider() {
	}

	public Image getImageLabel(Object element, int flags) {
		return getImageLabel(computeDescriptor(element, flags));
	}

	private Image getImageLabel(ImageDescriptor descriptor) {
		if(descriptor == null)
			return null;
		return Plugin.getImageDescriptorRegistry().get(descriptor);
	}

	private ImageDescriptor computeDescriptor(Object element, int flags) {
		if(element instanceof ILanguageElement) {
			return getImageDescriptor((ILanguageElement)element, flags);
		} else if(element instanceof IFile) {
			IFile file = (IFile)element;
			if("bl".equals(file.getFileExtension())) {
				return getCUResourceImageDescriptor(file, flags);
			}
			return getWorkbenchImageDescriptor(file, flags);
		} else if(element instanceof IAdaptable) {
			return getWorkbenchImageDescriptor((IAdaptable)element, flags);
		}
		return null;
	}

	private static boolean showOverlayIcons(int flags) {
		return (flags & OVERLAY_ICONS) != 0;
	}

	private static boolean useSmallSize(int flags) {
		return (flags & SMALL_ICONS) != 0;
	}

	private static boolean useLightIcons(int flags) {
		return (flags & LIGHT_TYPE_ICONS) != 0;
	}

	public ImageDescriptor getCUResourceImageDescriptor(IFile file, int flags) {
		Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		return new LanguageElementImageDescriptor(PluginImages.DESC_OBJS_CUNIT_RESOURCE, 0, size);
	}

	public ImageDescriptor getImageDescriptor(ILanguageElement element, int flags) {
		int adornmentFlags = computeAdornmentFlags(element, flags);
		Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;
		return new LanguageElementImageDescriptor(getBaseImageDescriptor(element, flags), adornmentFlags, size);
	}

	public ImageDescriptor getWorkbenchImageDescriptor(IAdaptable adaptable, int flags) {
		IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter)adaptable.getAdapter(IWorkbenchAdapter.class);

		if(wbAdapter == null) {
			return null;
		}
		ImageDescriptor descriptor = wbAdapter.getImageDescriptor(adaptable);

		if(descriptor == null) {
			return null;
		}

		Point size = useSmallSize(flags) ? SMALL_SIZE : BIG_SIZE;

		return new LanguageElementImageDescriptor(descriptor, 0, size);
	}

	public ImageDescriptor getBaseImageDescriptor(ILanguageElement element, int renderFlags) {
		if(element instanceof IMethod) {
			return getMethodImageDescriptor((IMethod)element);
		} else if(element instanceof IMember) {
			return getImageDescriptor((IMember)element);
		} else if(element instanceof IType) {
			return getImageDescriptor((IType)element, useLightIcons(renderFlags));
		} else if(element instanceof Project) {
			Project project = (Project)element;
			IProject iProject = (IProject)project.getResource();
			if(iProject.isOpen()) {
				IWorkbenchAdapter adapter = (IWorkbenchAdapter)iProject.getAdapter(IWorkbenchAdapter.class);

				if(adapter != null) {
					ImageDescriptor result = adapter.getImageDescriptor(project);
					if(result != null)
						return result;
				}

				return DESC_OBJ_PROJECT;
			}
			return DESC_OBJ_PROJECT_CLOSED;
		} else if(element instanceof Folder) {
			return DESC_OBJ_FOLDER;
		} else if(element instanceof CompilationUnit) {
			return PluginImages.DESC_OBJS_CUNIT;
		}

		return PluginImages.DESC_OBJS_UNKNOWN;

		/*
		 * switch(element.getElementType()) { case IJavaElement.INITIALIZER:
		 * return JavaPluginImages.DESC_MISC_PRIVATE; // 23479 case
		 * IJavaElement.LOCAL_VARIABLE: return
		 * JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE; case
		 * IJavaElement.IMPORT_DECLARATION: return
		 * JavaPluginImages.DESC_OBJS_IMPDECL; case
		 * IJavaElement.IMPORT_CONTAINER: return
		 * JavaPluginImages.DESC_OBJS_IMPCONT; case IJavaElement.JAVA_MODEL:
		 * return JavaPluginImages.DESC_OBJS_JAVA_MODEL; case
		 * IJavaElement.TYPE_PARAMETER: return
		 * JavaPluginImages.DESC_OBJS_LOCAL_VARIABLE; } Assert.isTrue(false,
		 * JavaUIMessages.JavaImageLabelprovider_assert_wrongImage); return
		 * JavaPluginImages.DESC_OBJS_GHOST;
		 */
	}

	public void dispose() {
	}

	private int computeAdornmentFlags(ILanguageElement element, int renderFlags) {
		int flags = 0;

		if(showOverlayIcons(renderFlags) && element instanceof IMember) {
			IMember member = (IMember)element;

			if(member.isFinal() || isEnumConstant(member)) {
				flags |= LanguageElementImageDescriptor.FINAL;
			}

			if(member.isStatic() || isEnumConstant(member)) {
				flags |= LanguageElementImageDescriptor.STATIC;
			}
			if(element instanceof IMethod) {
				IMethod m = (IMethod)element;
				if(m.getBody() != null) {
					IType sType = m.getDeclaringType().getBaseType();
					while(sType != null) {
						IMethod m1 = sType.getMethod(m.getSignature());
						if(m1 != null) {
							boolean override = m1.getBody() != null;
							if(override)
								flags |= LanguageElementImageDescriptor.OVERRIDES;
							else
								flags |= LanguageElementImageDescriptor.IMPLEMENTS;
							break;
						}
						sType = sType.getBaseType();
					}
				}

			}
		}
		return flags;
	}

	private static boolean isEnumConstant(IMember member) {
		return member.getDeclaringType().isEnum();
	}

	public static ImageDescriptor getMethodImageDescriptor(IMethod method) {
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

	public static ImageDescriptor getImageDescriptor(IMember member) {
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

	public static ImageDescriptor getImageDescriptor(IType type, boolean useLightIcons) {
		if(type.isEnum()) {
			if(useLightIcons) {
				return PluginImages.DESC_OBJS_ENUM_ALT;
			}

			return PluginImages.DESC_OBJS_ENUM;
		} else {
			if(useLightIcons) {
				return PluginImages.DESC_OBJS_CLASSALT;
			}
			return PluginImages.DESC_OBJS_CLASS;
		}
	}

	public static Image getDecoratedImage(ImageDescriptor baseImage, int adornments, Point size) {
		return Plugin.getImageDescriptorRegistry().get(new LanguageElementImageDescriptor(baseImage, adornments, size));
	}
}
