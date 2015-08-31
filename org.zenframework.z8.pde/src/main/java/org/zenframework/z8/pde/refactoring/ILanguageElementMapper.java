package org.zenframework.z8.pde.refactoring;

import org.zenframework.z8.compiler.core.ILanguageElement;

public interface ILanguageElementMapper {
    ILanguageElement getRefactoredLanguageElement(ILanguageElement element);
}