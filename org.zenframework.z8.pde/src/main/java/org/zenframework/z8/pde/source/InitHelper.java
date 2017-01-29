package org.zenframework.z8.pde.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;

import org.zenframework.z8.compiler.core.IAttribute;
import org.zenframework.z8.compiler.core.IAttributed;
import org.zenframework.z8.compiler.core.IInitializer;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IMember;
import org.zenframework.z8.compiler.core.IMethod;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.parser.expressions.ArrayInitializer;
import org.zenframework.z8.compiler.parser.expressions.BinaryExpression;
import org.zenframework.z8.compiler.parser.expressions.BracedExpression;
import org.zenframework.z8.compiler.parser.expressions.Container;
import org.zenframework.z8.compiler.parser.expressions.MethodCall;
import org.zenframework.z8.compiler.parser.expressions.OperatorNew;
import org.zenframework.z8.compiler.parser.expressions.Postfix;
import org.zenframework.z8.compiler.parser.expressions.QualifiedName;
import org.zenframework.z8.compiler.parser.statements.CompoundStatement;
import org.zenframework.z8.compiler.parser.statements.JumpStatement;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.ImportElement;
import org.zenframework.z8.compiler.parser.type.MemberNestedType;
import org.zenframework.z8.compiler.parser.type.members.Member;
import org.zenframework.z8.compiler.parser.type.members.Record;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.pde.MyMultiEditor;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.editor.Z8Editor;

@SuppressWarnings("restriction")
public class InitHelper implements Cloneable {

	public IType context;
	public List<String> what;
	public List<InitHelper> findInitDeepHelpers;

	public InitHelper(IType context, List<String> what) {
		this.context = context;
		if(what == null)
			what = new ArrayList<String>();
		this.what = what;
	}

	public String toQname() {
		return toQname(what);
	}

	public static String toQname(List<String> tokens) {
		if(tokens.size() == 0)
			return "";
		String result = tokens.get(0);
		for(int i = 1; i < tokens.size(); i++)
			result = result + "." + tokens.get(i);
		return result;
	}

	/*
	 * public Init2 findInitDeep(){ if (context.isEnum()) return new
	 * Init2(context,(ILanguageElement)context.findMember(what.get(0))); boolean
	 * statical = context.findMember(what.get(0))==null; IType curr = context;
	 * while (curr!=null){ IInitializer init =
	 * curr.getInitializer(toQname(what)); if (init!=null){ if (init instanceof
	 * MemberNestedType) return new Init2(curr, (MemberNestedType)init);
	 * ILanguageElement el = init.getRightElement(); if (el!=null){ InitHelper
	 * newHelper = getNewHelper(curr, el); if (newHelper==null) return new
	 * Init2(curr, el); else return newHelper.findInitDeep(); } } for (int
	 * size=what.size()-1; size>0; size--){ if (statical && size==1){ if
	 * (curr.getBaseType()==null){ IType sType =
	 * context.getCompilationUnit().resolveType(what.get(0)); return new
	 * InitHelper(sType, what.subList(1, what.size())).findInitDeep(); } } else
	 * { List<String> list = what.subList(0, size); List<String> post =
	 * what.subList(size, what.size()); Init2 init2 = new InitHelper(curr,
	 * list).findInitDeep(); if (init2!=null){ ILanguageElement right =
	 * init2.element; if (right!=null){ if (right instanceof MemberNestedType) {
	 * MemberNestedType mnt = (MemberNestedType) right; return new
	 * InitHelper(mnt, post).findInitDeep(); } if (right instanceof OperatorNew)
	 * { OperatorNew oNew = (OperatorNew) right; return new
	 * InitHelper(oNew.getVariableType().getType(), post).findInitDeep(); }
	 * InitHelper newHelper = getNewHelper(context, right); if (newHelper==null)
	 * return null; newHelper.what.addAll(post); return
	 * newHelper.findInitDeep(); } } } }
	 * 
	 * if (curr.getMember(what.get(0))!=null) break; curr=curr.getBaseType(); }
	 * return null; }
	 */

	public IType findTypeInit() {
		if(what.size() == 0)
			return context;
		Init2 init = findInitDeep();
		if(init.element instanceof IType) {
			return (IType)init.element;
		}
		return null;
	}

	public Init2 findInitDeep() {
		/*
		 * findInitDeepHelpers = new ArrayList<InitHelper>(); Init2 init2 =
		 * findInit(); if (init2==null) return null; InitHelper helper =
		 * createHelper(init2); if (helper==null) return init2; if
		 * (helper.what.size()==0) return new Init2(init2.context,
		 * (ILanguageElement) helper.context); if (this.equals(helper)) return
		 * new Init2(context, what.subList(0, what.size()-1),
		 * getMember().getVariableType().getType(), null);
		 * findInitDeepHelpers.add(helper); Init2 result =
		 * helper.findInitDeep();
		 * findInitDeepHelpers.addAll(helper.findInitDeepHelpers); return
		 * result;
		 */
		return new Init2(context, findType());
	}

	public static IType findTypeDeep(InitHelper ih, List<InitHelper> deepHelpers) {

		Map<List<String>, Set<IInitializer>> sawInits = new HashMap<List<String>, Set<IInitializer>>();
		while(true) {
			if(ih.what.size() == 0)
				return ih.context;
			Init2 init2 = ih.findInit(sawInits);
			if(init2 == null) {
				ih.findInit(sawInits);
				return null;
			}
			ih = createHelper(init2);
			if(ih == null) {
				return null;
			}
			deepHelpers.add(ih);
		}
	}

	public IType findType() {
		findInitDeepHelpers = new ArrayList<InitHelper>();
		return findTypeDeep(this, findInitDeepHelpers);
	}

	public Init2 findRealInit() {
		Init2 init2 = findInit();
		if(init2 == null)
			return null;
		if(init2.postfix.size() == 0)
			return init2;
		InitHelper helper = createHelper(init2);
		if(helper == null)
			return init2;
		if(helper.what.size() == 0)
			return new Init2(init2.context, (ILanguageElement)helper.context);
		Init2 result = helper.findRealInit();
		return result;
	}

	public InitHelper getLastHelper() {
		findInitDeep();
		int size = findInitDeepHelpers.size();
		for(int i = size - 1; i >= 0; i--) {
			if(findInitDeepHelpers.get(i).context == context)
				return findInitDeepHelpers.get(i);
		}
		return this;

	}

	public boolean isStatical() {
		return context.findMember(what.get(0)) == null;
	}

	public Init2 findInit() {
		return findInit(new HashMap<List<String>, Set<IInitializer>>());
	}

	public Init2 findInit(Map<List<String>, Set<IInitializer>> sawInits) {
		if(context == null)
			return null;
		if(what.size() == 0)
			return null;
		if(context.isEnum()) {
			IMember member = context.findMember(what.get(0));
			if(what.size() == 1 && member != null)
				return new Init2(context, (ILanguageElement)member);
			return null;
		}
		boolean statical = isStatical();
		if(statical && what.size() == 1)
			return null;

		IType curr = context;
		do {
			IInitializer init = curr.getInitializer(toQname(what));
			if(init != null) {
				Set<IInitializer> s = sawInits.get(new ArrayList<String>());
				if(s == null) {
					s = new HashSet<IInitializer>();
					sawInits.put(new ArrayList<String>(), s);
				}
				if(s.contains(init))
					init = null;
				else {
					s.add(init);
					if(init instanceof MemberNestedType)
						return new Init2(context, (MemberNestedType)init);
					ILanguageElement el = init.getRightElement();
					if(el != null)
						return new Init2(context, el);
				}
			}
			for(int size = what.size() - 1; size > (statical ? 1 : 0); size--) {
				List<String> pre = what.subList(0, size);
				List<String> post = what.subList(size, what.size());
				init = curr.getInitializer(toQname(pre));
				if(init != null) {
					Set<IInitializer> s = sawInits.get(pre);
					if(s == null) {
						s = new HashSet<IInitializer>();
						sawInits.put(pre, s);
					}
					if(s.contains(init)) {
						init = null;
					} else
						s.add(init);
				}
				IMember currMember = curr.getMember(what.get(0));
				if(init != null || (size == 1 && currMember != null)) {
					ILanguageElement el;
					if(init != null) {
						if(init instanceof MemberNestedType)
							el = (ILanguageElement)init;
						else
							el = init.getRightElement();
					} else
						el = (ILanguageElement)currMember.getVariableType().getType();
					if(el != null) {
						if(el instanceof MemberNestedType || el instanceof OperatorNew || el instanceof IType) {
							InitHelper h = createHelper(context, el);
							if(h == null)
								return null;
							h.what.addAll(post);
							Init2 result = h.findInit(sawInits);
							if(result != null) {
								// if (h.isStatical())
								// return result;
								result.context = context;
								ArrayList<String> np = new ArrayList<String>();
								np.addAll(pre);
								np.addAll(result.prefix);
								result.prefix = np;
							}
							return result;
						} else
							return new Init2(context, null, el, post);
					}
				}
			}
			if(statical) {
				IType sType = curr.getCompilationUnit().resolveType(what.get(0));
				if(sType != null)
					return new InitHelper(sType, what.subList(1, what.size())).findInit();
			} else {
				IMember member = curr.getMember(what.get(0));
				if(member != null) {
					IType type = member.getVariableType().getType();
					if(what.size() == 1)
						return new Init2(context, (ILanguageElement)type);
					Init2 result = new InitHelper(type, what.subList(1, what.size())).findInit(sawInits);
					if(result != null) {
						result.context = context;
						result.prefix.add(0, what.get(0));
					}
					return result;
				}
			}
			curr = curr.getBaseType();
		} while(curr != null);
		return null;
	}

	public List<Init2> findArrayInits() {
		return findArrayInits(false);
	}

	private List<Init2> findArrayInits(boolean allowNonArray) {
		List<Init2> result = new ArrayList<Init2>();
		if(context.findMember(what.get(0)) == null)
			return result;
		boolean found = false;
		IType curr = context;
		do {
			IInitializer init = curr.getInitializer(toQname(what));
			if(init != null) {
				ILanguageElement right = init.getRightElement();
				if(right != null) {
					if(right instanceof ArrayInitializer) {
						ArrayInitializer arrayInit = (ArrayInitializer)right;
						ILanguageElement[] els = arrayInit.getElements();
						for(int i = els.length - 1; i >= 0; i--)
							result.add(new Init2(context, els[i]));
					} else {
						if(allowNonArray) {
							result.add(new Init2(context, right));
						} else {
							InitHelper newHelper = createHelper(context, right);
							if(newHelper != null)
								result.addAll(newHelper.findArrayInits(allowNonArray));
						}
					}
				}
			}
			if(what.size() == 1) {
				IVariableType arrayType = context.findMember(what.get(0)).getVariableType();
				if(arrayType.isAuto()) {
					IMember[] members = curr.getMembers();
					for(int i = members.length - 1; i >= 0; i--) {
						IMember member = members[i];
						IVariableType memberType = member.getVariableType();
						if(!memberType.isArray())
							if(memberType.getType().equals(arrayType.getType()) || memberType.getType().isSubtypeOf(arrayType.getType()))
								result.add(new Init2(context, (ILanguageElement)member));
					}
				}
			} else
				for(int size = what.size() - 1; size > 0 && !found; size--) {
					List<String> pre = what.subList(0, size);
					List<String> post = what.subList(size, what.size());
					init = curr.getInitializer(toQname(pre));
					IMember currMember = curr.getMember(what.get(0));
					if(init != null || (size == 1 && currMember != null)) {
						ILanguageElement right;
						if(init != null) {
							if(init instanceof MemberNestedType)
								right = (ILanguageElement)init;
							else
								right = init.getRightElement();
						} else
							right = (ILanguageElement)currMember.getVariableType().getType();
						if(right != null) {
							found = true;
							InitHelper newHelper = createHelper(context, right);
							if(newHelper != null) {
								newHelper.what.addAll(post);
								if(!newHelper.equals(this)) {
									List<Init2> addition = newHelper.findArrayInits(allowNonArray);
									if(right instanceof MemberNestedType || right instanceof OperatorNew || right instanceof IType) {
										for(Init2 i : addition) {
											// int contextUp=0;
											if(i.context == newHelper.context) {
												/*
												 * IType t = newHelper.context;
												 * while (t!=null &&
												 * t!=i.context){
												 * t=t.getContainerType();
												 * contextUp++; } if (t==null)
												 * contextUp=1/0; // this
												 * shouldn't have happened! } if
												 * (contextUp==0){
												 */
												i.context = context;
												List<String> prefix = new ArrayList<String>();
												prefix.addAll(pre);
												prefix.addAll(i.prefix);
												i.prefix = prefix;
											}
										}
									}
									result.addAll(addition);
								}
							}
						}
					}
				}
			if(curr.getMember(what.get(0)) != null)
				break;
			curr = curr.getBaseType();
		} while(curr != null && !found);
		return result;
	}

	public List<IAttributed> findAttrubuted() {
		if(context == null)
			return new ArrayList<IAttributed>();
		if(what.size() == 0) {

			List<IAttributed> result = new InitHelper(context.getBaseType()).findAttrubuted();
			result.add(0, context);
			if(context instanceof MemberNestedType) {
				MemberNestedType mnt = (MemberNestedType)context;
				if(mnt.getParent() instanceof IMember)
					result.add(0, mnt.getParent());
			}
			return result;
		}
		List<IAttributed> result = new ArrayList<IAttributed>();
		IType curr = context;
		IInitializer init = curr.getInitializer(toQname(what));
		// boolean skipSearch = false;
		if(init != null) {
			if(init instanceof MemberNestedType) {
				MemberNestedType mnt = (MemberNestedType)init;
				// if (mnt.getMember()!=null)
				// result.add(mnt.getMember());
				if(mnt.getParent() instanceof Member) {
					Member member = (Member)mnt.getParent();
					result.add(member);
				}
				result.addAll(new InitHelper(mnt).findAttrubuted());
				// return result;
				// skipSearch=true;
			} else {
				if(init.getParent() instanceof Member) {
					Member member = (Member)init.getParent();
					result.add(member);
				}
				result.add((IAttributed)init);
				ILanguageElement right = init.getRightElement();
				if(right != null && result.contains(right)) {
					InitHelper newHelper = createHelper(curr, right);
					if(newHelper != null) {
						// result.addAll(newHelper.findAttrubuted());
					}
					return result;
				}
			}
		}
		/* if (!skipSearch) */for(int size = what.size() - 1; size > 0; size--) {
			List<String> pre = what.subList(0, size);
			List<String> post = what.subList(size, what.size());
			init = curr.getInitializer(toQname(pre));
			if(init != null) {
				ILanguageElement right = init.getRightElement();
				if(init instanceof MemberNestedType) {
					MemberNestedType mnt = (MemberNestedType)init;
					result.addAll(new InitHelper(mnt, post).findAttrubuted());
					return result;
				} else if(right != null) {
					if(right instanceof OperatorNew) {
						OperatorNew oNew = (OperatorNew)right;
						result.addAll(new InitHelper(oNew.getVariableType().getType(), post).findAttrubuted());
					} else {
						InitHelper newHelper = createHelper(curr, right);
						if(newHelper != null) {
							newHelper.what.addAll(post);
							if(!newHelper.equals(this))
								result.addAll(newHelper.findAttrubuted());
						}
					}
					return result;
				}
			}
		}
		IMember member = curr.getMember(what.get(0));
		if(member != null) {
			if(what.size() == 1)
				result.add(member);
			result.addAll(new InitHelper(member.getVariableType().getType(), what.subList(1, what.size())).findAttrubuted());
		} else
			result.addAll(new InitHelper(curr.getBaseType(), what).findAttrubuted());
		return result;
	}

	public IAttribute findAttribute(String name) {
		for(IAttributed att : findAttrubuted()) {
			IAttribute result = att.getAttribute(name);
			if(result != null)
				return result;
		}
		return null;
	}

	public static List<String> toStringList(QualifiedName qName) {
		int size = qName.getTokenCount();
		List<String> result = new ArrayList<String>(size);
		for(int i = 0; i < size; i++) {
			result.add(qName.getTokens()[i].getRawText());
		}
		return result;
	}

	/*
	 * public IMember getMember(){ IType cont = context; if (what.size()==0)
	 * return null; for (int i=0; i<what.size()-1; i++){ String qName =
	 * what.get(i); IMember member = cont.findMember(qName); if (member==null)
	 * return null; cont = member.getVariableType().getType(); } return
	 * cont.findMember(what.get(what.size()-1)); }
	 */

	public IMember getMember() {
		if(what.size() == 1)
			return context.findMember(what.get(0));
		List<String> w = new ArrayList<String>(1);
		w.add(what.get(0));
		InitHelper h = new InitHelper(context, w);
		Init2 init = h.findInitDeep();
		if(init == null)
			return null;
		h = createHelper(init);
		if(h == null || h.what == null || h.what.size() > 0) {
			new InitHelper(context, w).findInitDeep();
			return null;
		}
		h.what = what.subList(1, what.size());
		return h.getMember();
	}

	private IVariableType getVariableType() {
		return getMember().getVariableType();
	}

	public boolean isArray() {
		return getVariableType().isArray();
	}

	public boolean isAuto() {
		return getVariableType().isAuto();
	}

	public IType getMemberType() {
		if(what.size() == 0)
			return context;
		return getVariableType().getType();

	}

	public IType getType() {
		if(what.size() == 0)
			return context;
		Init2 init2 = findInitDeep();
		if(init2.element instanceof IType) {
			IType result = (IType)init2.element;
			return result;
		} else if(init2.element instanceof IMember) {
			IMember mem = (IMember)init2.element;
			return mem.getVariableType().getType();
		}
		return getVariableType().getType();
	}

	@Override
	public InitHelper clone() {
		List<String> copyWhat = new ArrayList<String>(what);
		return new InitHelper(context, copyWhat);
	}

	public InitHelper add(String segment) {
		InitHelper result = clone();
		String[] addition = segment.split("\\.");
		for(int i = 0; i < addition.length; i++)
			result.what.add(addition[i]);
		return result;
	}

	public static InitHelper createHelper(IType context, ILanguageElement el) {
		return createHelper(context, new ArrayList<String>(), el, new ArrayList<String>());
	}

	public static InitHelper createHelper(IType context, List<String> prefix, ILanguageElement el, List<String> postfix) {
		List<String> pref = new ArrayList<String>();
		pref.addAll(prefix);
		InitHelper result = null;
		if(el instanceof IType) {
			IType type = (IType)el;
			result = new InitHelper(type);
		} else if(el instanceof OperatorNew) {
			OperatorNew oNew = (OperatorNew)el;
			result = new InitHelper(oNew.getVariableType().getType());
		} else if(el instanceof IMember) {
			if(context.isEnum())
				return null;
			IMember member = (IMember)el;
			List<String> resultWhat = new ArrayList<String>(1);
			resultWhat.addAll(pref);
			resultWhat.add(member.getName());
			result = new InitHelper(context, resultWhat);
		} else {
			if(el instanceof Postfix) {
				Postfix pstfx = (Postfix)el;
				if(pstfx.getPrefix() != null) {
					if(pstfx.getPrefix() instanceof Container) {
						Container container = (Container)pstfx.getPrefix();
						for(int i = 0; i < container.getNumber(); i++) {
							int size = pref.size();
							if(size > 0)
								pref.remove(size - 1);
							else
								context = context.getContainerType();
						}
						el = pstfx.getPostfix();
					} else if(pstfx.getPostfix() == null)
						el = pstfx.getPrefix();
				}

			}
			if(el instanceof Container) {
				Container container = (Container)el;
				for(int i = 0; i < container.getNumber(); i++) {
					int size = pref.size();
					if(size > 0)
						pref.remove(size - 1);
					else
						context = context.getContainerType();
				}
				result = new InitHelper(context, pref);
			}
			if(el instanceof QualifiedName) {
				QualifiedName qname = (QualifiedName)el;
				List<String> post = toStringList(qname);
				if(pref.size() > 0) {
					Init2 actualContext = new InitHelper(context, pref).findInitDeep();
					if(actualContext != null) {
						InitHelper ah = createHelper(actualContext);
						if(ah != null && ah.what.size() == 0) {
							if(ah.context.findMember(post.get(0)) == null)
								return new InitHelper(ah.context, post);
						}
					}
				}
				pref.addAll(post);
				result = new InitHelper(context, pref);
			}
			if(el instanceof MethodCall) {
				MethodCall mc = (MethodCall)el;
				InitHelper actualContext;
				if(mc.getContext() == null)
					actualContext = new InitHelper(context, pref);
				else
					actualContext = createHelper(context, prefix, mc.getContext(), new ArrayList<String>());
				IType type = actualContext.getType();
				IMethod method = type.findMethod(mc.getSignature());
				if(method.getBody() instanceof CompoundStatement) {
					CompoundStatement cs = (CompoundStatement)method.getBody();
					if(cs.getElements().size() != 1)
						return null;
					if(cs.getElements().get(0) instanceof JumpStatement) {
						JumpStatement js = (JumpStatement)cs.getElements().get(0);
						result = createHelper(actualContext.context, actualContext.what, js.getExpression(), new ArrayList<String>());
					}
				}
			}
		}
		if(result != null)
			result.what.addAll(postfix);
		return result;
	}

	public static InitHelper createHelper(Init2 init2) {
		return createHelper(init2.context, init2.prefix, init2.element, init2.postfix);
	}

	public static InitHelper parseInit2(Init2 init) {
		if(init.element instanceof IMember) {
			IMember m = (IMember)init.element;
			List<String> what = new ArrayList<String>();
			what.addAll(init.postfix);
			return new InitHelper(m.getVariableType().getType(), what);
		} else
			return createHelper(init);
	}

	public InitHelper(IType context) {
		this(context, (List<String>)null);
	}

	public InitHelper(IType classFrom, String name) {
		this(classFrom);
		what.add(name);
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof InitHelper) {
			InitHelper o = (InitHelper)other;
			return context.equals(o.context) && what.equals(o.what);
		}
		return false;
	}

	public Transaction getAddInitTransaction(String init) {
		return getAddInitTransaction(init, false);

	}

	public Transaction getAddInitTransaction(String init, boolean addContainers) {
		int size = what.size();
		int i = size;
		IType type = context;
		while(i > 0) {
			i--;
			if(i == 0)
				break;
			InitHelper helper = new InitHelper(context, what.subList(0, i));
			Init2 init2 = helper.findInitDeep();
			if(init2 == null)
				continue;
			CompilationUnit cUnit = init2.element.getCompilationUnit();
			if(!cUnit.equals(context.getCompilationUnit()))
				continue;
			if(init2.element instanceof MemberNestedType) {
				type = (MemberNestedType)init2.element;
				break;
			} else
				return null;
		}
		IPosition pos = type.getTypeBody().getPosition();
		int offset = pos.getOffset() + pos.getLength() - 1;
		String pre = "\r\n";
		String post = tabs(i);
		for(int j = i; j < size - 1; j++) {
			pre = pre + tabs(j + 1) + what.get(j) + " = class {\r\n";
			post = tabs(j + 1) + "};\r\n" + post;
		}
		String containers = "";
		if(addContainers)
			for(int j = 0; j < size - 1; j++)
				containers += "container.";
		String ins = pre + tabs(what.size()) + what.get(size - 1) + (isArray() ? " += {" : " = ") + containers + init + (isArray() ? "}" : "") + ";\r\n" + post;
		return new Transaction(offset, ins);
	}

	public Transaction getAddInitTransaction(NameGen gen, IDocument document) {
		int size = what.size();
		int i = size;
		IType type = context;
		while(i > 0) {
			i--;
			if(i == 0)
				break;
			InitHelper helper = new InitHelper(context, what.subList(0, i));
			Init2 init2 = helper.findInitDeep();
			if(init2 == null)
				continue;
			CompilationUnit cUnit = init2.element.getCompilationUnit();
			if(!cUnit.equals(context.getCompilationUnit()))
				continue;
			if(init2.element instanceof MemberNestedType) {
				type = (MemberNestedType)init2.element;
				break;
			} else
				return null;
		}
		int offset = new InitHelper(type).getNewInitPosition(document, what.get(size - 1));
		String pre = "\r\n";
		String post = tabs(i);
		for(int j = i; j < size - 1; j++) {
			pre = pre + tabs(j + 1) + what.get(j) + " = class {\r\n";
			post = tabs(j + 1) + "};\r\n" + post;
		}
		for(int j = 0; j < size - 1; j++)
			gen.nameGenAdd("container");
		String inner = what.get(size - 1) + (isArray() ? " += {" : " = ") + gen.nameGenResult() + (isArray() ? "}" : "");
		if(i == size - 1) {
			IInitializer init = type.getInitializer(what.get(i));
			if(init != null) {
				ILanguageElement element = init.getRightElement();
				if(element instanceof ArrayInitializer) {
					ArrayInitializer arrInit = (ArrayInitializer)element;
					if(arrInit.getElements().length == 0) {
						return new Transaction(arrInit.getSourceRange().getOffset() + 1, gen.nameGenResult());
					}
				}
			}
		}
		String ins = pre + tabs(what.size()) + inner + ";\r\n" + post;
		return new Transaction(offset, ins);
	}

	public Transaction getAddAttributeTransaction(String attString) {
		int size = what.size();
		int i = size;
		IType type = context;
		while(i > 0) {
			i--;
			if(i == 0)
				break;
			InitHelper helper = new InitHelper(context, what.subList(0, i));
			Init2 init2 = helper.findInitDeep();
			if(init2 == null)
				continue;
			CompilationUnit cUnit = init2.element.getCompilationUnit();
			if(!cUnit.equals(context.getCompilationUnit()))
				continue;
			if(init2.element instanceof MemberNestedType) {
				type = (MemberNestedType)init2.element;
				break;
			} else
				return null;
		}
		IPosition pos = type.getTypeBody().getPosition();
		int offset = pos.getOffset() + pos.getLength() - 1;
		String pre = "\r\n";
		String post = tabs(i);
		for(int j = i; j < size - 1; j++) {
			pre = pre + tabs(j + 1) + what.get(j) + " = class {\r\n";
			post = tabs(j + 1) + "};\r\n" + post;
		}
		String ins = pre + tabs(what.size()) + attString + "\r\n" + tabs(what.size()) + what.get(size - 1) + ";\r\n" + post;
		return new Transaction(offset, ins);
	}

	private static String tabs(int tabs) {
		String result = "";
		for(int i = 0; i < tabs; i++)
			result += "\t";
		return result;
	}

	@Override
	public int hashCode() {
		return (context == null ? 0 : context.hashCode()) + what.hashCode();
	}

	public final static char TABLES_TREE_FIELD_TABLE_SPLIT_CHAR = ':';

	private void parseJoinExpression(Init2 init, List<Init2> tableInits, List<String> lWhere, List<String> rWhere, List<IPosition> expressions) {
		ILanguageElement le = init.element;
		if(le instanceof BinaryExpression) {
			BinaryExpression b = (BinaryExpression)le;
			List<ILanguageElement> les = new ArrayList<ILanguageElement>(2);
			les.add(b.getLeftElement());
			les.add(b.getRightElement());
			for(ILanguageElement le1 : les) {
				parseJoinExpression(new Init2(init, le1), tableInits, lWhere, rWhere, expressions);
			}
		} else if(le instanceof Postfix) {
			Postfix ps = (Postfix)le;
			le = ps.getPrefix();
			if(le instanceof MethodCall) {
				MethodCall mc = (MethodCall)le;
				if(mc.getSignature().startsWith("on(")) {
					parseWhereExpression(mc.getArguments()[0], lWhere, rWhere, expressions);
					tableInits.add(0, new Init2(init, mc.getContext()));
				}
			} else if(le instanceof QualifiedName) {
				QualifiedName qname = (QualifiedName)le;
				tableInits.add(0, new Init2(init, qname));
			}
		}

	}

	private StringTree parseDataSource(InitHelper h, Map<InitHelper, StringTree> tableInits, List<String> lWhere, List<String> rWhere, List<IPosition> expressions) {
		StringTree result = h.what.size() > 0 ? new StringTree(h.toQname()) : new StringTree();
		IType type = h.findType();
		if(type.isSubtypeOf("TableBase")) {
			for(IMember m : type.getAllMembers()) {
				if(m instanceof IMethod) {
					continue;
				}
				if(m.getVariableType().isArray())
					continue;
				IType mtype = m.getVariableType().getType();
				if(mtype.isSubtypeOf("Relation")) {
					InitHelper nn = h.add(m.getName()).getLastHelper();
					StringTree st = null;
					if(!tableInits.containsKey(nn)) {
						st = parseDataSource(nn, tableInits, lWhere, rWhere, expressions);
					} else
						st = tableInits.get(nn);
					result.getChildren().put(m.getName(), st);
					tableInits.put(nn, st);
				}
			}
		} else if(type.isSubtypeOf("Query")) {
			for(IMember m : type.getAllMembers()) {
				if(m.getName().equals("model"))
					continue;
				if(m instanceof IMethod) {
					continue;
				}
				if(m.getVariableType().isArray())
					continue;
				IType mtype = m.getVariableType().getType();
				if(mtype.isSubtypeOf("Query")) {
					InitHelper nn = h.add(m.getName()).getLastHelper();
					StringTree st = null;
					if(!tableInits.containsKey(nn)) {
						st = parseDataSource(nn, tableInits, lWhere, rWhere, expressions);
					} else
						st = tableInits.get(nn);
					tableInits.put(nn, st);
					result.getChildren().put(m.getName(), st);
				}
			}
		}
		return result;
	}

	public boolean usesFromJoin() {
		return !add("fromJoin").findArrayInits(true).isEmpty() || add("tables").findArrayInits().isEmpty();
	}

	private void reduceTree(StringTree tree, List<InitHelper> neededHelpers) {
		List<String> toRemove = new ArrayList<String>();
		for(Entry<String, StringTree> e : tree.getChildren().entrySet()) {
			reduceTree(e.getValue(), neededHelpers);
			if(!neededHelpers.contains(new InitHelper(context).add(e.getValue().name())) && e.getValue().getChildren().isEmpty()) {
				toRemove.add(e.getKey());
			}
		}
		for(String st : toRemove)
			tree.getChildren().remove(st);
	}

	public StringTree getTablesTree() {
		StringTree result = new StringTree();

		InitHelper helper = add("fromJoin");
		List<Init2> inits1 = helper.findArrayInits(true);

		InitHelper helper2 = add("fields");
		List<Init2> inits2 = helper2.findArrayInits(true);

		List<String> lWhere = new ArrayList<String>();
		List<String> rWhere = new ArrayList<String>();
		List<IPosition> expressions = new ArrayList<IPosition>();
		List<Init2> inits;

		if(inits1.size() > 0) {
			// ������������ ����� ��������� fromJoin = ...
			inits = new ArrayList<Init2>();
			for(Init2 init : inits1)
				parseJoinExpression(init, inits, lWhere, rWhere, expressions);
		} else if(inits2.size() > 0) {
			inits = new ArrayList<Init2>();
			Map<InitHelper, StringTree> helpers = new HashMap<InitHelper, StringTree>();
			StringTree ss = parseDataSource(this, helpers, lWhere, rWhere, expressions);

			List<Init2> fs = add("fields").findArrayInits();
			List<InitHelper> neededHelpers = new ArrayList<InitHelper>();
			for(Init2 f : fs) {
				InitHelper ih = createHelper(f);
				if(ih == null)
					continue;
				if(ih.what.size() < 2)
					continue;
				ih.what.remove(ih.what.size() - 1);
				neededHelpers.add(ih.getLastHelper());
			}

			neededHelpers.add(add("model").getLastHelper());

			reduceTree(ss, neededHelpers);

			return ss;
			/*
			 * StringTree st = new StringTree(); for (InitHelper hhe :
			 * helpers.keySet()){ Init2 init = hhe.findInitDeep(); if
			 * (init.element instanceof IType) { IType t = (IType) init.element;
			 * if (t.isSubtypeOf("TableBase")){
			 * 
			 * } } }
			 */

		} else {
			// ----------------------------------

			helper = add("tables");
			inits = helper.findArrayInits();

			IType curr = getType();
			// ������ where �� ���� ������������ ������� � ������ ������
			while(curr != null) {
				IMethod where = curr.getMethod("where()");
				if(where != null) {
					CompoundStatement body = (CompoundStatement)where.getBody();
					if(body != null)
						for(ILanguageElement el : body.getElements()) {
							if(el instanceof JumpStatement) {
								JumpStatement js = (JumpStatement)el;
								parseWhereExpression(js.getExpression(), lWhere, rWhere, expressions);
								break;
							}
						}
				}
				curr = curr.getBaseType();
			}
		}

		// List<IType> tablesTypes = new ArrayList<IType>();
		// List<String> tablesQNames = new ArrayList<String>();
		for(int i = inits.size() - 1; i >= 0; i--) {
			Init2 tableInit = inits.get(i);
			helper = InitHelper.createHelper(tableInit);
			if(helper == null)
				continue;
			String qname = helper.toQname();
			// tablesQNames.add(qname);
			StringTree tree = new StringTree(qname);
			tree.setSource(tableInit.element);
			helper.findInitDeep();
			for(InitHelper h : helper.findInitDeepHelpers)
				if(h.context.equals(helper.context))
					tree.addValue(h.toQname());
			result.getChildren().put(qname, tree);
			if(result.root == null)
				result.root = qname;
		}

		for(int i = 0; i < lWhere.size(); i++) {
			String left = lWhere.get(i);
			String right = rWhere.get(i);
			if(right.endsWith(".recordId")) {
				String temp = left;
				left = right;
				right = temp;
			}
			if(!left.endsWith(".recordId"))
				continue;
			left = left.substring(0, left.length() - ".recordId".length());
			/*
			 * int pos=-1; for (int j=0; j<right.length(); j++) if
			 * (right.charAt(j)=='.') pos=j; if (pos==-1) continue; String
			 * parentTable = right.substring(0,pos); String childRelation =
			 * right.substring(pos+1);
			 */

			StringTree pTree = null;
			StringTree cTree = null;
			String childRelation = "";

			String[] parents = right.split("\\.");
			for(int j = 1; j < parents.length; j++) {
				List<String> l = new ArrayList<String>();
				for(int k = 0; k < j; k++)
					l.add(parents[k]);
				pTree = result.find(toQname(l));
				if(pTree == null)
					continue;
				l = new ArrayList<String>();
				for(int k = j; k < parents.length; k++)
					l.add(parents[k]);
				childRelation += toQname(l);
				break;
			}

			String[] children = left.split("\\.");
			for(int j = 1; j <= children.length; j++) {
				List<String> l = new ArrayList<String>();
				for(int k = 0; k < j; k++)
					l.add(children[k]);
				cTree = result.find(toQname(l));
				if(cTree == null)
					continue;
				left = toQname(l);
				l = new ArrayList<String>();
				if(j < children.length) {
					for(int k = j; k < children.length; k++)
						l.add(children[k]);
					childRelation += (TABLES_TREE_FIELD_TABLE_SPLIT_CHAR + toQname(l));
				}
				break;
			}
			if(pTree == null || cTree == null)
				continue;
			result.getChildren().remove(left);
			pTree.getChildren().put(childRelation, cTree);
			pTree.addWhereExpression(expressions.get(i));
			cTree.addWhereExpression(expressions.get(i));
		}

		return result;
	}

	private void parseWhereExpression(ILanguageElement le, List<String> left, List<String> right, List<IPosition> exprs) {
		if(le instanceof BracedExpression) {
			BracedExpression braced = (BracedExpression)le;
			parseWhereExpression(braced.getExpression(), left, right, exprs);
		}
		if(le instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression)le;
			ILanguageElement ll = binary.getLeftElement();
			ILanguageElement rr = binary.getRightElement();
			if(ll instanceof Postfix && rr instanceof Postfix) {
				InitHelper lh = createHelper(context, what, ll, new ArrayList<String>());
				InitHelper rh = createHelper(context, what, rr, new ArrayList<String>());
				if(lh != null && rh != null) {
					left.add(lh.getLastHelper().toQname());
					right.add(rh.getLastHelper().toQname());
					if(exprs != null)
						exprs.add(le.getSourceRange());
				}
			} else {
				parseWhereExpression(ll, left, right, exprs);
				parseWhereExpression(rr, left, right, exprs);
			}

		}
	}

	public static String see(List<String> from, List<String> see) {
		List<String> l1 = new ArrayList<String>(from);
		List<String> l2 = new ArrayList<String>(see);
		while(l1.size() > 0 && l2.size() > 0)
			if(l1.get(0).equals(l2.get(0))) {
				l1.remove(0);
				l2.remove(0);
			} else
				break;
		for(int i = 0; i < l1.size(); i++)
			l2.add(0, "container");
		if(l2.size() == 0)
			l2.add("this");
		return toQname(l2);
	}

	public String see(List<String> see) {
		return see(what, see);
	}

	public static void checkImport(MultipleTransactions tr, IType type, IType t) {
		if(t.isPrimary())
			return;
		checkImport(tr, type, t.getCompilationUnit().getQualifiedName());
	}

	public static void checkImport(MultipleTransactions tr, IType type, String imp) {
		boolean newImport = true;
		ImportBlock block = type.getImportBlock();
		if(block != null) {

			for(ImportElement element : block.getImportElements()) {
				if(imp.equalsIgnoreCase(element.getImportedUnit().getQualifiedName())) {
					newImport = false;
					break;
				}
			}
		}
		String importString = "import " + imp + ";\r\n";
		for(Transaction t : tr.getTransactions())
			if(t.getWhat().equals(importString))
				newImport = false;
		if(newImport)
			tr.add(0, 0, importString);
	}

	@Override
	public String toString() {
		return toQname();
	}

	public final static <T extends IMergeable> boolean mergeList(List<T> oldList, List<T> newList) {
		boolean changed = false;
		for(int i = 0; i < newList.size(); i++) {
			T newModel = newList.get(i);
			boolean found = false;
			for(int j = i; j < oldList.size(); j++) {
				T oldModel = oldList.get(j);
				if(newModel.equals(oldModel)) {
					oldModel.merge(newModel);
					if(j > i) {
						oldList.add(i, oldList.remove(j));
						changed = true;
					}
					found = true;
					break;
				}
			}
			if(!found) {
				oldList.add(i, newModel);
				changed = true;
			}
		}
		while(newList.size() < oldList.size()) {
			oldList.remove(newList.size());
			changed = true;
		}
		return changed;
	}

	public boolean deleteObject(MultipleTransactions mt, Shell shell) {
		if(what.isEmpty())
			return true;
		IMember mem = getMember();
		if(mem.getDeclaringType().getCompilationUnit() != context.getCompilationUnit()) {
			return true;
		}
		boolean doSwitch = false;
		IPosition p2 = mem.getSourceRange();
		if(mem.getInitializer() instanceof MemberNestedType) {
			doSwitch = true;
		}

		int offset = mem.getFirstToken().getPosition().getOffset();
		int startSearch = p2.getOffset() + p2.getLength();
		int length = startSearch - offset;
		String message = "������ �� �� ������� ������ " + toQname() + "?";
		return deleteCode(offset, length, message, doSwitch, mt, shell, true);
	}

	public static boolean deleteCode(int offset, int length, String message, boolean doSwitch, MultipleTransactions mt, Shell shell, boolean lookForEndStatement) {
		if(lookForEndStatement) {
			try {
				FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(mt.getDocument());
				int startSearch = offset + length;
				IRegion r = adapter.find(startSearch, ";", true, false, false, false);
				if(r == null)
					throw new Exception("symbol ';' not found");
				length = r.getOffset() + 1 - offset;
			} catch(Exception v) {
				Plugin.log(v);
				return true;
			}
		}
		if(doSwitch)
			message += "\r\n���������� ��� ����� �����.";
		MessageDialog d = new MessageDialog(shell, "�������� �������", null, message, MessageDialog.QUESTION, new String[] { "��", "���", "������" }, 0);
		IEditorPart part = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		int save = 0;
		if(part != null && doSwitch)
			if(part instanceof MyMultiEditor) {
				MyMultiEditor multi = (MyMultiEditor)part;
				save = multi.getActivePage();
				multi.setActivePage(0);
				((Z8Editor)multi.getEditor(0)).getSelectionProvider().setSelection(new TextSelection(offset, length));
			}
		int open = d.open();
		if(open == 0)
			mt.add(length, offset, "");
		boolean result = open != 2;
		if(part != null && doSwitch)
			if(part instanceof MyMultiEditor) {
				MyMultiEditor multi = (MyMultiEditor)part;
				multi.setActivePage(save);
			}
		return result;
	}

	public static boolean deleteLanguageElement(ILanguageElement el, String message, boolean doSwitch, MultipleTransactions mt, Shell shell) {
		int offset = el.getFirstToken().getPosition().getOffset();
		int length = el.getSourceRange().getOffset() + el.getSourceRange().getLength() - offset;
		return deleteCode(offset, length, message, doSwitch, mt, shell, true);
	}

	public static Transaction removeFromArray(IPosition pos, IDocument doc) {
		int start = pos.getOffset();
		int end = start + pos.getLength();
		FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(doc);
		try {
			IRegion e1 = adapter.find(end, ",", true, false, false, false);
			IRegion e2 = adapter.find(end, "}", true, false, false, false);
			IRegion s1 = adapter.find(start, ",", false, false, false, false);
			IRegion s2 = adapter.find(start, "{", false, false, false, false);
			int s0 = s2.getOffset();
			int e0 = e2.getOffset();
			if(s1 != null && s1.getOffset() > s0) {
				return new Transaction(end - s1.getOffset(), s1.getOffset());
			} else if(e1 != null && e1.getOffset() < e0) {
				return new Transaction(e1.getOffset() + 1 - start, start);
			} else {
				IRegion e3 = adapter.find(end, ";", true, false, false, false);
				int s3 = start - 1;
				boolean id = false;
				boolean whitespace = false;
				while(true) {
					Character c = doc.getChar(s3);
					if(!id) {
						if(Character.isJavaIdentifierPart(c))
							id = true;
					} else if(!whitespace) {
						if(!Character.isJavaIdentifierPart(c)) {
							if(Character.isWhitespace(c)) {
								whitespace = true;
							} else {
								s3++;
								break;
							}
						}
					} else if(!Character.isWhitespace(c)) {
						s3++;
						break;
					}
					s3--;
				}
				return new Transaction(e3.getOffset() + 1 - s3, s3);
			}
		} catch(Exception e) {
		}
		return null;
	}

	public int getNewMemberPosition(IDocument document) {
		IMember last = null;
		for(IMember m : context.getMembers()) {
			if(m instanceof IMethod)
				continue;
			last = m;
		}
		if(last != null) {
			FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
			IPosition p = last.getSourceRange();
			if(last.getInitializer() != null)
				p = p.union(last.getInitializer().getSourceRange());
			int position = p.getOffset() + p.getLength();
			IRegion r = null;
			try {
				r = adapter.find(position, ";", true, true, false, false);
			} catch(BadLocationException e) {
				Plugin.log(e);
			}
			;
			if(r != null)
				return r.getOffset() + 1;
		}
		return context.getTypeBody().getSourceRange().getOffset() + 1;
	}

	public int getNewRecordPosition(IDocument document) {
		IMember last = null;
		for(IMember m : context.getMembers()) {
			if(m instanceof IMethod)
				continue;
			if(!(m instanceof Record))
				continue;
			last = m;
		}
		if(last != null) {
			FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
			IPosition p = last.getSourceRange();
			if(last.getInitializer() != null)
				p = p.union(last.getInitializer().getSourceRange());
			int position = p.getOffset() + p.getLength();
			IRegion r = null;
			try {
				r = adapter.find(position, ";", true, true, false, false);
			} catch(BadLocationException e) {
				Plugin.log(e);
			}
			;
			if(r != null)
				return r.getOffset() + 1;
		}
		return -1;
	}

	public int getNewInitPosition(IDocument document, String name) {

		List<IInitializer> badInits = new ArrayList<IInitializer>();
		for(IMember m : context.getMembers()) {
			if(m instanceof IMethod)
				continue;
			if(m.getInitializer() != null)
				badInits.add(m.getInitializer());
		}
		IInitializer last = null;
		for(IInitializer m : context.getInitializers()) {
			if(!badInits.contains(m)) {
				if(name == null || (name.compareToIgnoreCase(m.getLeftName()) > 0 && (last == null || last.getLeftName().compareToIgnoreCase(m.getLeftName()) < 0))) {
					last = m;
				}
			}
		}
		if(last != null) {
			FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
			IPosition p = last.getSourceRange();
			int position = p.getOffset() + p.getLength();
			IRegion r = null;
			try {
				r = adapter.find(position, ";", true, true, false, false);
			} catch(BadLocationException e) {
				Plugin.log(e);
			}
			;
			if(r != null)
				return r.getOffset() + 1;
		}
		return getNewMemberPosition(document);
	}

	public int getNewMethodPosition(IDocument document, String name) {
		IMethod last = null;
		for(IMethod m : context.getMethods()) {
			if(name.compareToIgnoreCase(m.getName()) > 0 && (last == null || last.getName().compareToIgnoreCase(m.getName()) < 0)) {
				last = m;
			}
		}
		if(last != null) {
			IPosition p = last.getSourceRange();
			int position = p.getOffset() + p.getLength();
			return position;
		}
		return getNewInitPosition(document, null);
	}

}
