package org.zenframework.z8.compiler.workspace;

import java.util.ArrayList;
import java.util.List;

public class CompilationLoop {
    private List<CompilationUnit> compilationUnits;
    private boolean isOpen = true;

    public boolean isOpen() {
        return isOpen;
    }

    public boolean run(CompilationUnit compilationUnit) {
        if(compilationUnits == null) {
            compilationUnits = new ArrayList<CompilationUnit>();
            compilationUnits.add(compilationUnit);
        }
        else {
            assert (isOpen());
            assert (compilationUnits.indexOf(compilationUnit) == -1);
            compilationUnits.add(compilationUnit);
            return false;
        }

        isOpen = true;

        for(int i = 0; i < compilationUnits.size(); i++) {
            compilationUnits.get(i).resolveTypes();
        }

        isOpen = false;

        for(CompilationUnit unit : compilationUnits) {
            unit.resolveStructure();
        }

        for(CompilationUnit unit : compilationUnits) {
            unit.checkSemantics();
        }

        for(CompilationUnit unit : compilationUnits) {
            unit.resolveNestedTypes();
        }

        for(CompilationUnit unit : compilationUnits) {
            unit.fireResourceEvent(ResourceListener.RESOURCE_CHANGED, unit, null);
            unit.checkImportUsage();
            unit.reportMessages();
            unit.generateCode();
        }

        for(CompilationUnit unit : compilationUnits) {
            unit.organizeImports();
        }

        return true;
    }

}
