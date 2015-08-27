package org.zenframework.z8.compiler.workspace;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.text.edits.TextEdit;

import org.zenframework.z8.compiler.content.Hyperlink;
import org.zenframework.z8.compiler.content.TypeHyperlink;
import org.zenframework.z8.compiler.core.CodeGenerator;
import org.zenframework.z8.compiler.core.ILanguageElement;
import org.zenframework.z8.compiler.core.IPosition;
import org.zenframework.z8.compiler.core.IType;
import org.zenframework.z8.compiler.core.IVariableType;
import org.zenframework.z8.compiler.error.IBuildMessageConsumer;
import org.zenframework.z8.compiler.file.File;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.parser.grammar.Parser;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.ConstantToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.StringToken;
import org.zenframework.z8.compiler.parser.type.ImportBlock;
import org.zenframework.z8.compiler.parser.type.Primary;
import org.zenframework.z8.compiler.util.Set;

public class CompilationUnit extends Resource {
    private Object lockObject = new Object();

    private IType type;

    private int uniqueId = -1;

    private Set<IType> importedTypes;
    private Set<String> unresolvedTypeNames;

    private Set<CompilationUnit> contributors;
    private Set<CompilationUnit> consumers;

    private int contentHashCode = 0;

    private boolean updatingDependencies = false;
    private boolean collectingDependencies = false;

    private boolean cleaned = true;

    private boolean changed = true;

    private long sourceTimeStamp = 0;
    private long targetTimeStamp = 0;

    private Map<IPosition, Hyperlink> hyperlinks;

    private HashMap<Integer, IVariableType> contentProposals;

    private List<ConstantToken> nlsStrings;

    private StartupCodeLines startupCodeLines;

    private int targetLinesOffset;
    private Map<Integer, Integer> sourceToTargetLineNumbers = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> targetToSourceLineNumbers = new HashMap<Integer, Integer>();

    public CompilationUnit(Resource parent, IResource resource) {
        super(parent, resource);
        parent.addMember(this);

        startupCodeLines = new StartupCodeLines();
    }

    protected Object getLockObject() {
        return lockObject;
    }

    public IType getType() {
        return type;
    }

    public IType getReconciledType() {
        if(isCleaned()) {
            getProject().reconcile(getResource(), null, null);
        }

        return getType();
    }

    @Override
    public CompilationUnit getCompilationUnit() {
        return this;
    }

    public StartupCodeLines getStartupCodeLines() {
        return startupCodeLines;
    }

    public IPath getPackagePath() {
        return getPath().removeLastSegments(1);
    }

    public String getPackage() {
        return getPackagePath().toString().replace(IPath.SEPARATOR, '.');
    }

    public String getQualifiedName() {
        return getPath().removeFileExtension().toString().replace(IPath.SEPARATOR, '.');
    }

    public String getSimpleName() {
        return getPath().removeFileExtension().lastSegment();
    }

    public String createUniqueName() {
        uniqueId++;
        return "__" + uniqueId;
    }

    public boolean containsNativeType() {
        return type != null && type.isNative();
    }

    public void build(IBuildMessageConsumer messageConsumer) {
        if(!isChanged()) {
            reportMessages();
        }
        else {
            resolveType();
        }
    }

    protected void reconcile(char[] content) {
        assert (Project.isReconciling());

        if(content != null && new String(content).hashCode() != contentHashCode || isCleaned() || isChanged()
                || containsError()) {
            setChanged(true);
            updateDependencies();
            doResolveType(content);
        }
        else {
            reportMessages();
        }
    }

    public IType resolveType() {
        return doResolveType(null);
    }

    private IType doResolveType(char[] content) {
        synchronized(getLockObject()) {
            if(isChanged() || isCleaned()) {
                cleanup();

                setChanged(false);
                setCleaned(false);

                Parser parser = new Parser(this);

                parser.parse(content);
                contentHashCode = parser.hashCode();

                type = parser.getType();

                if(type != null) {
                    type.setImportBlock(parser.getImport());
                    type.resolveType(this);

                    getWorkspace().runCompilationLoop(this);
                }
                else {
                    fireResourceEvent(ResourceListener.RESOURCE_CHANGED, this, null);
                    reportMessages();
                }
            }

            return type;
        }
    }

    protected void resolveTypes() {
        assert (type != null);
        type.resolveTypes(this, type);
    }

    protected void resolveStructure() {
        assert (type != null);
        type.resolveStructure(this, type);
    }

    protected void checkSemantics() {
        assert (type != null);
        type.checkSemantics(this, type, null, null, null);
    }

    protected void resolveNestedTypes() {
        assert (type != null);
        type.resolveNestedTypes(this, type);
    }

    public IType resolveType(String simpleName) {
        Project thisProject = getProject();

        CompilationUnit compilationUnit = resolveToCompilationUnit(thisProject, simpleName);

        if(compilationUnit == null) {
            Project[] projects = thisProject.getReferencedProjects();

            for(Project project : projects) {
                compilationUnit = resolveToCompilationUnit(project, simpleName);

                if(compilationUnit != null) {
                    break;
                }
            }
        }

        if(compilationUnit != null) {
            return compilationUnit.resolveType();
        }

        return null;
    }

    private CompilationUnit resolveToCompilationUnit(Project project, String simpleName) {
        CompilationUnit compilationUnit = project.findCompilationUnit(simpleName);

        ImportBlock importBlock = type != null ? type.getImportBlock() : null;

        if(compilationUnit == null && importBlock != null) {
            compilationUnit = importBlock.getImportedUnit(simpleName);
        }

        if(compilationUnit == null) {
            compilationUnit = getFolder().getCompilationUnit(simpleName + ".bl");
        }

        return compilationUnit;
    }

    protected void checkImportUsage() {
        getType().checkImportUsage(this);
    }

    public void addUnresolvedType(String typeName) {
        synchronized(getLockObject()) {
            if(unresolvedTypeNames == null) {
                unresolvedTypeNames = new Set<String>();
            }

            unresolvedTypeNames.add(typeName);
        }
    }

    public String[] getUnresolvedTypes() {
        synchronized(lockObject) {
            if(unresolvedTypeNames == null) {
                return new String[0];
            }

            String[] result = unresolvedTypeNames.toArray(new String[unresolvedTypeNames.size()]);

            unresolvedTypeNames = null;

            return result;
        }
    }

    public void importType(IType type) {
        if(importedTypes == null) {
            importedTypes = new Set<IType>();
        }

        if(type != this.type && !type.getUserName().equals(Primary.Void)) {
            importedTypes.add(type);
        }
    }

    public IType[] getImportedTypes() {
        if(importedTypes == null) {
            return new IType[0];
        }

        return importedTypes.toArray(new IType[importedTypes.size()]);
    }

    public IPath getOutputPath() {
        IPath outputPath = getProject().getOutputPath();
        return outputPath != null ? outputPath.append(getPath().removeFileExtension().addFileExtension("java")) : null;
    }

    protected void generateCode() {
        startupCodeLines.generate(type);

        if(!Project.isBuilding() || containsError() || type.isNative())
            return;

        IPath outputPath = getOutputPath();

        if (outputPath == null)
            return;

        CodeGenerator codeGenerator = new CodeGenerator(this);

        sourceToTargetLineNumbers.clear();
        targetToSourceLineNumbers.clear();
        type.getCode(codeGenerator);

        IPath folder = outputPath.removeLastSegments(1);

        try {
            String oldContent = "";

            try {
                oldContent = new String(File.fromPath(outputPath).read());
            }
            catch(FileException e) {}
            catch(UnsupportedEncodingException e) {}

            String newContent = codeGenerator.toString();

            if(!newContent.equals(oldContent)) {
                File.fromPath(folder).makeDirectories();
                File.fromPath(outputPath).write(newContent);
                Project.filesWritten++;
            }
            else {
                Project.filesSkipped++;
            }

            File.rename(outputPath, outputPath);

            targetTimeStamp = File.fromPath(outputPath).getTimeStamp();
            sourceTimeStamp = File.fromPath(getAbsolutePath()).getTimeStamp();
        }
        catch(FileException e) {
            error(e);
        }
    }

    public void addHyperlink(IPosition position, CompilationUnit compilationUnit, IPosition targetPosition) {
        addHyperlink(position, new Hyperlink(compilationUnit, targetPosition));
    }

    public void addHyperlink(IPosition position, IType type) {
        addHyperlink(position, new TypeHyperlink(type));
    }

    protected void addHyperlink(IPosition position, Hyperlink hyperlink) {
        if(hyperlinks == null) {
            hyperlinks = new HashMap<IPosition, Hyperlink>();
        }

        hyperlinks.put(position, hyperlink);
    }

    public IPosition getHyperlinkPosition(int offset) {
        if(hyperlinks != null) {
            for(IPosition position : hyperlinks.keySet()) {
                if(offset >= position.getOffset() && offset <= position.getOffset() + position.getLength()) {
                    return position;
                }
            }
        }
        return null;
    }

    public Hyperlink getHyperlink(IPosition position) {
        if(hyperlinks != null) {
            return hyperlinks.get(position);
        }
        return null;
    }

    private CompilationUnit[] getContributors() {
        if(contributors == null) {
            return new CompilationUnit[0];
        }

        return contributors.toArray(new CompilationUnit[contributors.size()]);
    }

    public void getDependencies(Set<CompilationUnit> result) {
        if(!collectingDependencies) {
            collectingDependencies = true;

            CompilationUnit[] contributors = getContributors();

            for(CompilationUnit contributor : contributors) {
                if(result.get(contributor) == null) {
                    result.add(contributor);
                    contributor.getDependencies(result);
                }
            }

            collectingDependencies = false;
        }
    }

    private CompilationUnit[] getConsumers() {
        if(consumers == null) {
            return new CompilationUnit[0];
        }

        return consumers.toArray(new CompilationUnit[consumers.size()]);
    }

    public void addContributor(CompilationUnit contributor) {
        if(contributor != this && contributor != null) {
            if(contributors == null) {
                contributors = new Set<CompilationUnit>();
            }

            contributors.add(contributor);
            contributor.addConsumer(this);
        }
    }

    private void addConsumer(CompilationUnit consumer) {
        if(consumer != this) {
            if(consumers == null) {
                consumers = new Set<CompilationUnit>();
            }
            consumers.add(consumer);
        }
    }

    private void removeConsumer(CompilationUnit consumer) {
        if(consumers != null) {
            consumers.remove(consumer);

            if(consumers.size() == 0) {
                consumers = null;
            }
        }
    }

    public boolean isCleaned() {
        return cleaned;
    }

    public void setCleaned(boolean cleaned) {
        this.cleaned = cleaned;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void contentChanged() {
        if(Project.isIdle()) {
            setChanged(true);
        }
        else {
            System.out.println("CompilationUnit.contentChanged() called during building(reconciling) process: " + getName());
        }
    }

    protected boolean isSynchronized() {
        return getResource().isSynchronized(IResource.DEPTH_ZERO);
    }

    protected boolean hasToUpdateDependencies() {
        if(isChanged() || containsError()) {
            return true;
        }

        if(Project.isBuilding()) {
            if(!containsNativeType()) {
                try {
                    IPath outputPath = getOutputPath();
                    
                    File sourceFile = File.fromPath(getAbsolutePath());
                    File targetFile = outputPath != null ? File.fromPath(outputPath) : null;
                    return sourceFile.getTimeStamp() != sourceTimeStamp || targetFile == null || !targetFile.exists()
                            || targetFile.getTimeStamp() != targetTimeStamp;
                }
                catch(FileException e) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void updateDependencies() {
        if(updatingDependencies) {
            return;
        }

        if(hasToUpdateDependencies()) {
            updatingDependencies = true;

            setChanged(true);

            CompilationUnit[] contributors = getContributors();

            for(CompilationUnit contributor : contributors) {
                contributor.removeConsumer(this);
            }

            this.contributors = null;

            CompilationUnit[] consumers = getConsumers();

            for(CompilationUnit consumer : consumers) {
                consumer.setChanged(true);
                consumer.updateDependencies();
            }

            updatingDependencies = false;
        }
    }

    protected void cleanup() {
        cleanup(true);
    }

    protected void cleanup(boolean clearMessages) {
        if(clearMessages) {
            clearMessages();
        }

        setChanged(false);
        setCleaned(true);

        type = null;

        uniqueId = -1;

        hyperlinks = null;
        contentProposals = null;
        importedTypes = null;
        unresolvedTypeNames = null;

        nlsStrings = null;
    }

    public void addContentProposal(IPosition position, IVariableType type) {
        if(contentProposals == null) {
            contentProposals = new HashMap<Integer, IVariableType>(4);
        }
        contentProposals.put(new Integer(position.getOffset() + position.getLength() + 1), type);
    }

    public IVariableType getContentProposal(int offset) {
        if(contentProposals != null) {
            return contentProposals.get(new Integer(offset));
        }
        return null;
    }

    boolean useAutoOrganizeImports = false;

    protected void organizeImports() {
        if(!useAutoOrganizeImports)
            return;

        ImportBlock importBlock = getType().getImportBlock();

        if(containsParseError() || !Project.isBuilding() || unresolvedTypeNames == null
                && (importBlock == null || !importBlock.hasUnusedNames()))
            return;

        String imports = "";

        if(unresolvedTypeNames != null) {
            for(String typeName : unresolvedTypeNames) {
                CompilationUnit[] compilationUnits = getProject().lookupCompilationUnits(typeName);

                if(compilationUnits.length == 0)
                    continue;

                String qualifiedName = compilationUnits[0].getQualifiedName();

                if(importBlock == null || importBlock.getImportedUnit(typeName) == null) {
                    imports += "import " + qualifiedName + ";\n";
                }
            }
        }

        if(importBlock != null) {
            List<String> qualifiedNames = importBlock.getResolvedNames();

            for(String name : qualifiedNames) {
                imports += "import " + name + ";\n";
            }
        }

        try {
            IPosition position = getType().getSourceRange();

            File file = new File(getAbsolutePath());
            char[] content = file.read();

            file.write(imports + (imports.length() == 0 ? "" : "\n"));
            file.append(new String(content, position.getOffset(), position.getLength()));
        }
        catch(FileException e) {
            error(e);
        }
        catch(UnsupportedEncodingException e) {
            error(e);
        }
    }

    public ConstantToken[] getNLSStrings() {
        if(nlsStrings == null) {
            return new StringToken[0];
        }
        return nlsStrings.toArray(new StringToken[nlsStrings.size()]);
    }

    public void addNLSString(ConstantToken token) {
        if(nlsStrings == null) {
            nlsStrings = new ArrayList<ConstantToken>();
        }
        nlsStrings.add(token);
    }

    public IPosition[] getAllHyperlinkPositions(Hyperlink h) {
        List<IPosition> positions = new ArrayList<IPosition>();

        for(IPosition pos : hyperlinks.keySet())
            if(h.equals(hyperlinks.get(pos)))
                positions.add(pos);

        return positions.toArray(new IPosition[positions.size()]);
    }

    @Override
    public ILanguageElement getElementAt(int offset) {
        if(type != null) {
            return type.getElementAt(offset);
        }

        return null;
    }

    @Override
    public ILanguageElement getElementAt(int offset, int length) {
        if(type != null) {
            return type.getElementAt(offset, length);
        }

        return null;
    }

    @Override
    public ILanguageElement getElementAt(IPosition position) {
        if(type != null) {
            return type.getElementAt(position);
        }

        return null;
    }

    @Override
    public void replaceTypeName(TextEdit parent, IType type, String newTypeName) {
        if(this.type != null) {
            this.type.replaceTypeName(parent, type, newTypeName);
        }
    }

    @Override
    public void replaceImport(TextEdit parent, IPath oldImport, IPath newImport) {
        if(type != null) {
            type.replaceImport(parent, oldImport, newImport);
        }
    }

    public void setTargetLinesOffset(int targetLinesOffset) {
        this.targetLinesOffset = targetLinesOffset;
    }

    public int getSourceLineNumber(int targetLineNumber) {
        Integer lineNumber = targetToSourceLineNumbers.get(targetLineNumber - targetLinesOffset);
        return lineNumber != null ? lineNumber : -1;
    }

    public int getTargetLineNumber(int sourceLineNumber) {
        Integer lineNumber = sourceToTargetLineNumbers.get(sourceLineNumber);
        return lineNumber != null ? lineNumber + targetLinesOffset : -1;
    }

    public void setLineNumbers(int sourceLineNumber, int targetLineNumber) {
        sourceToTargetLineNumbers.put(sourceLineNumber, targetLineNumber);
        targetToSourceLineNumbers.put(targetLineNumber, sourceLineNumber);
    }
}
