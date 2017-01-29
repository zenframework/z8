package org.zenframework.z8.pde.navigator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.ArrayInitializer;
import org.zenframework.z8.compiler.parser.expressions.BracedExpression;
import org.zenframework.z8.compiler.parser.expressions.Constant;
import org.zenframework.z8.compiler.parser.expressions.Container;
import org.zenframework.z8.compiler.parser.expressions.MapElement;
import org.zenframework.z8.compiler.parser.expressions.OperatorNew;
import org.zenframework.z8.compiler.parser.expressions.Postfix;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.expressions.Super;
import org.zenframework.z8.compiler.parser.expressions.This;
import org.zenframework.z8.compiler.parser.expressions.UnaryExpression;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.StringToken;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.parser.type.members.MemberInit;
import org.zenframework.z8.compiler.workspace.Resource;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.PluginImages;
import org.zenframework.z8.pde.refactoring.LanguageElementLabelProvider;

public class Z8LabelProvider extends LabelProvider implements ILightweightLabelDecorator {
	boolean m_showInit;

	public Z8LabelProvider() {
		this(false);
	}

	public Z8LabelProvider(boolean showInit) {
		m_showInit = showInit;
	}

	public static Image loadImage(String path) {
		try {
			Image originalImage = new Image((Device)null, FileLocator.openStream(Plugin.getDefault().getBundle(), new Path(path), true));

			ImageData originalImageData = originalImage.getImageData();

			if(originalImageData.width != 16 || originalImageData.height != 16) {
				Image scaledImage = new Image(null, originalImageData.scaledTo(16, 16));
				originalImage.dispose();
				return scaledImage;
			}

			return originalImage;
		} catch(Exception e) {
		}

		return null;
	}

	LanguageElementLabelProvider m_labelProvider = new LanguageElementLabelProvider();

	@Override
	public Image getImage(Object element) {
		if(element instanceof String) {
			return null; // m_busyImage;
		}

		return m_labelProvider.getImage(element);
	}

	@Override
	public String getText(Object object) {
		if(object instanceof String) {
			return (String)object;
		}

		assert (object instanceof ILanguageElement);

		ILanguageElement element = (ILanguageElement)object;

		String result = "";

		if(element.getParent() instanceof ArrayInitializer) {
			if(element instanceof MapElement) {
				MapElement mapElement = (MapElement)element;
				return "[" + getText(mapElement.getKey()) + "] = " + getText(mapElement.getValue());
			} else {
				ArrayInitializer array = (ArrayInitializer)element.getParent();
				result = "[" + array.getIndexOf(element) + "] = ";
			}
		}

		if(element instanceof MemberNestedType) {
			MemberNestedType nestedType = (MemberNestedType)element;
			return nestedType.getLeftName() + " = class {...}";
		} else if(element instanceof MemberInit) {
			MemberInit memberInit = (MemberInit)element;

			result = getText(memberInit.getLeftElement());

			ILanguageElement rightElement = memberInit.getRightElement();

			if(rightElement != null) {
				if(rightElement instanceof ArrayInitializer) {
					boolean isMap = false;

					IVariableType variableType = rightElement.getVariableType();

					if(variableType != null) {
						IType[] keys = variableType.getKeys();

						isMap = keys.length > 0 && keys[0] != null;
					}

					result += " : ";
					result += isMap ? "map of " : "array of ";
					result += ((ArrayInitializer)rightElement).getElements().length + " element(s)";
				} else {
					result += " = " + getText(rightElement);
				}
			}

			return result;
		} else if(element instanceof IType) {
			IType type = (IType)element;
			String userName = type.getUserName();
			return userName == null ? "" : userName;
		} else if(element instanceof IMethod) {
			IMethod method = (IMethod)element;
			return method.getSignature() + " : " + method.getVariableType().getSignature();
		} else if(element instanceof Resource) {
			Resource resource = (Resource)element;
			return resource.getName();
		} else if(element instanceof IMember) {
			IMember member = (IMember)element;

			IInitializer initializer = member.getInitializer();

			result = member.getName();

			if(!member.getDeclaringType().isEnum()) {
				IVariableType variableType = member.getVariableType();

				if(variableType != null) {
					result += " : " + variableType.getSignature();
				}
			}

			if(initializer instanceof MemberNestedType) {
				return result += " = class {...}";
			}

			if(initializer != null) {
				ILanguageElement rightElement = initializer.getRightElement();

				if(rightElement != null) {
					String initializerString = getText(rightElement);

					if(initializerString != "") {
						result += " = " + initializerString;
					}
				}
			}

			return result;
		} else if(element instanceof Constant) {
			Constant constant = (Constant)element;
			ConstantToken token = constant.getToken();

			if(token instanceof StringToken) {
				return result + '"' + token.format(false) + '"';
			}
			return result + token.format(false);
		} else if(element instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression)element;
			return result + unary.getOperatorToken().getSign() + getText(unary.getExpression());
		} else if(element instanceof BracedExpression) {
			BracedExpression braced = (BracedExpression)element;
			return result + '(' + getText(braced.getExpression()) + ')';
		} else if(element instanceof QualifiedName) {
			QualifiedName qualifiedName = (QualifiedName)element;
			return result + qualifiedName.toString();
		} else if(element instanceof OperatorNew) {
			OperatorNew operatorNew = (OperatorNew)element;
			return result + "new " + operatorNew.getVariableType().getSignature();
		} else if(element instanceof Postfix) {
			Postfix postfix = (Postfix)element;

			ILanguageElement prefix = postfix.getPrefix();

			result += getText(prefix);

			if(postfix.getPostfix() != null) {
				result += "." + getText(postfix.getPostfix());
			}
			return result;
		} else if(element instanceof Container) {
			return result + "container";
		} else if(element instanceof Super) {
			return result + "super";
		} else if(element instanceof This) {
			return result + "this";
		} else {
			Plugin.log(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Unknown language element " + element.getClass().getName(), null));
		}

		return result;
	}

	public void decorate(Resource resource, IDecoration decoration) {
		if(resource.containsError())
			decoration.addOverlay(PluginImages.DESC_OVR_ERROR, IDecoration.BOTTOM_LEFT);
		else if(resource.containsWarning())
			decoration.addOverlay(PluginImages.DESC_OVR_WARNING, IDecoration.BOTTOM_LEFT);
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if(element instanceof Resource) {
			decorate((Resource)element, decoration);
		}

		if(element instanceof IType) {
			IType type = (IType)element;

			if(type.getContainerType() == null) {
				decorate(type.getCompilationUnit(), decoration);
			}
		}
	}

}
