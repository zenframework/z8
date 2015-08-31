package org.zenframework.z8.pde.refactoring.rename;

import org.zenframework.z8.pde.refactoring.UserInterfaceManager;
import org.zenframework.z8.pde.refactoring.processors.RenameFolderProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameCompilationUnitProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameResourceProcessor;
import org.zenframework.z8.pde.refactoring.processors.rename.RenameTypeProcessor;

public class RenameUserInterfaceManager extends UserInterfaceManager {
    private static final UserInterfaceManager m_instance = new RenameUserInterfaceManager();

    public static UserInterfaceManager getDefault() {
        return m_instance;
    }

    private RenameUserInterfaceManager() {
        put(RenameResourceProcessor.class, RenameUserInterfaceStarter.class, RenameResourceWizard.class);
        //		put(RenameJavaProjectProcessor.class, RenameUserInterfaceStarter.class, RenameJavaProjectWizard.class);
        //		put(RenameSourceFolderProcessor.class, RenameUserInterfaceStarter.class, RenameSourceFolderWizard.class);
        put(RenameFolderProcessor.class, RenameUserInterfaceStarter.class, RenameFolderWizard.class);
        put(RenameCompilationUnitProcessor.class, RenameUserInterfaceStarter.class, RenameCompilationUnitWizard.class);
        put(RenameTypeProcessor.class, RenameUserInterfaceStarter.class, RenameTypeWizard.class);
        //		put(RenameFieldProcessor.class, RenameUserInterfaceStarter.class, RenameFieldWizard.class);
        //		put(RenameEnumConstProcessor.class, RenameUserInterfaceStarter.class, RenameEnumConstWizard.class);
        //		put(RenameTypeParameterProcessor.class, RenameUserInterfaceStarter.class, RenameTypeParameterWizard.class);
        //		put(RenameNonVirtualMethodProcessor.class, RenameMethodUserInterfaceStarter.class, RenameMethodWizard.class);
        //		put(RenameVirtualMethodProcessor.class, RenameMethodUserInterfaceStarter.class, RenameMethodWizard.class);
        //		put(RenameLocalVariableProcessor.class, RenameUserInterfaceStarter.class, RenameLocalVariableWizard.class);
    }
}
