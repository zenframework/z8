package org.zenframework.z8.pde.refactoring;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.TokenException;
import org.zenframework.z8.pde.Plugin;
import org.zenframework.z8.pde.debug.util.CharOperation;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public final class LanguageConventions {
    private final static char DOT = '.';
    private final static Lexer LEXER = new Lexer();

    private LanguageConventions() {}

    private static synchronized char[] scannedIdentifier(String source) {
        if(source == null) {
            return null;
        }

        String trimmed = source.trim();

        if(!trimmed.equals(source)) {
            return null;
        }

        LEXER.setSource(source.toCharArray());

        IToken token, nextToken;

        try {
            token = LEXER.nextToken();
            nextToken = LEXER.nextToken();
        }
        catch(TokenException e) {
            return null;
        }

        if(token.getId() == IToken.IDENTIFIER && nextToken.getId() == IToken.EOF) {
            String identifier = token.getRawText();

            if(identifier.length() == source.length())
                return identifier.toCharArray();
        }

        return null;
    }

    public static IStatus validateCompilationUnitName(String name) {
        if(name == null) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_unit_nullName, null);
        }

        if(!name.endsWith(".bl")) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_unit_notBlName, null);
        }

        String identifier;

        int index;

        index = name.lastIndexOf('.');

        if(index == -1) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_unit_notBlName, null);
        }

        identifier = name.substring(0, index);

        IStatus status = validateIdentifier(identifier);

        if(!status.isOK()) {
            return status;
        }

        status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);

        if(!status.isOK()) {
            return status;
        }

        return Status.OK_STATUS;
    }

    public static IStatus validateFieldName(String name) {
        return validateIdentifier(name);
    }

    public static IStatus validateIdentifier(String id) {
        if(scannedIdentifier(id) != null) {
            return Status.OK_STATUS;
        }
        else {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.bind(
                    RefactoringMessages.convention_illegalIdentifier, id), null);
        }
    }

    public static IStatus validateImportDeclaration(String name) {
        if(name == null || name.length() == 0) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_import_nullImport, null);
        }
        return validateFolderName(name);
    }

    public static IStatus validateTypeName(String name) {
        if(name == null) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_type_nullName, null);
        }

        String trimmed = name.trim();

        if(!name.equals(trimmed)) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_type_nameWithBlanks, null);
        }

        int index = name.lastIndexOf('.');

        char[] scannedID;

        if(index == -1) {
            scannedID = scannedIdentifier(name);
        }
        else {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_type_nameWithBlanks, null);
        }

        if(scannedID != null) {
            IStatus status = ResourcesPlugin.getWorkspace().validateName(new String(scannedID), IResource.FILE);

            if(!status.isOK()) {
                return status;
            }

            if((scannedID.length > 0 && Character.isLowerCase(scannedID[0]))) {
                return new Status(IStatus.WARNING, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_type_lowercaseName,
                        null);
            }
            return Status.OK_STATUS;
        }
        else {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.bind(
                    RefactoringMessages.convention_type_invalidName, name), null);
        }
    }

    public static IStatus validateMethodName(String name) {
        return validateIdentifier(name);
    }

    public static IStatus validateFolderName(String name) {
        if(name == null) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_folder_nullName, null);
        }

        int length = name.length();

        if(length == 0) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_folder_emptyName, null);
        }

        if(name.charAt(0) == DOT || name.charAt(length - 1) == DOT) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_folder_dotName, null);
        }

        if(CharOperation.isWhitespace(name.charAt(0)) || CharOperation.isWhitespace(name.charAt(name.length() - 1))) {
            return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.convention_folder_nameWithBlanks,
                    null);
        }

        int dot = 0;

        while(dot != -1 && dot < length - 1) {
            if((dot = name.indexOf(DOT, dot + 1)) != -1 && dot < length - 1 && name.charAt(dot + 1) == DOT) {
                return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1,
                        RefactoringMessages.convention_folder_consecutiveDotsName, null);
            }
        }

        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        StringTokenizer st = new StringTokenizer(name, ".");

        boolean firstToken = true;

        IStatus warningStatus = null;

        while(st.hasMoreTokens()) {
            String typeName = st.nextToken();

            typeName = typeName.trim();

            char[] scannedID = scannedIdentifier(typeName);

            if(scannedID == null) {
                return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, -1, RefactoringMessages.bind(
                        RefactoringMessages.convention_illegalIdentifier, typeName), null);
            }

            IStatus status = workspace.validateName(new String(scannedID), IResource.FOLDER);

            if(!status.isOK()) {
                return status;
            }

            if(firstToken && scannedID.length > 0 && Character.isUpperCase(scannedID[0])) {
                if(warningStatus == null) {
                    warningStatus = new Status(IStatus.WARNING, Plugin.PLUGIN_ID, -1,
                            RefactoringMessages.convention_folder_uppercaseName, null);
                }
            }

            firstToken = false;
        }

        if(warningStatus != null) {
            return warningStatus;
        }

        return Status.OK_STATUS;
    }

    public static IStatus validateTypeVariableName(String name) {
        return validateIdentifier(name);
    }
}
