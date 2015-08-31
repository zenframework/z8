package org.zenframework.z8.pde.refactoring.rename;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import org.zenframework.z8.compiler.workspace.Project;
import org.zenframework.z8.pde.refactoring.wordIterator.WordIterator;

public class RenamingNameSuggestor {
    /*
     * ADDITIONAL OPTIONS
     * ----------------------------------------------------------------
     * 
     * There are two additional flags which may be set in this class to allow
     * better matching of special cases:
     * 
     * a) Special treatment of leading "I"s in type names, i.e. interface names
     * like "IJavaElement". If the corresponding flag is set, leading "I"s are
     * stripped from type names if the second char is also uppercase to allow
     * exact matching of variable names like "javaElement" for type
     * "IJavaElement". Note that embedded matching already matches cases like
     * this.
     * 
     * b) Special treatment of all-uppercase type names or all-uppercase type
     * name camel-case hunks, i.e. names like "AST" or "PersonalURL". If the
     * corresponding flag is set, the type name hunks will be transformed such
     * that variables like "fAst", "ast", "personalUrl", or "url" are found as
     * well. The target name will be transformed too if it is an all-uppercase
     * type name camel-case hunk as well.
     * 
     * NOTE that in exact or embedded mode, the whole type name must be
     * all-uppercase to allow matching custom-lowercased variable names, i.e.
     * there are no attempts to "guess" which hunk of the new name should be
     * lowercased to match a partly lowercased variable name. In suffix mode,
     * hunks of the new type which are at the same position as in the old type
     * will be lowercased if necessary.
     * 
     * c) Support for (english) plural forms. If the corresponding flag is set,
     * the suggestor will try to match variables which have plural forms of the
     * type name, for example "handies" for "Handy" or "phones" for
     * "MobilePhone". The target name will be transformed as well, i.e.
     * conversion like "fHandies" -> "fPhones" are supported.
     * 
     */
    public static final int STRATEGY_EXACT = 1;
    public static final int STRATEGY_EMBEDDED = 2;
    public static final int STRATEGY_SUFFIX = 3;
    private static final String PLURAL_S = "s"; //$NON-NLS-1$
    private static final String PLURAL_IES = "ies"; //$NON-NLS-1$
    private static final String SINGULAR_Y = "y"; //$NON-NLS-1$
    private int fStrategy;
    private String[] fFieldPrefixes;
    private String[] fFieldSuffixes;
    private String[] fStaticFieldPrefixes;
    private String[] fStaticFieldSuffixes;
    private String[] fLocalPrefixes;
    private String[] fLocalSuffixes;
    private String[] fArgumentPrefixes;
    private String[] fArgumentSuffixes;
    private boolean fExtendedInterfaceNameMatching;
    private boolean fExtendedAllUpperCaseHunkMatching;
    private boolean fExtendedPluralMatching;

    public RenamingNameSuggestor() {
        this(STRATEGY_SUFFIX);
    }

    public RenamingNameSuggestor(int strategy) {
        fStrategy = strategy;
        fExtendedInterfaceNameMatching = true;
        fExtendedAllUpperCaseHunkMatching = true;
        fExtendedPluralMatching = true;
        resetPrefixes();
    }

    public String suggestNewFieldName(Project project, String oldFieldName, boolean isStatic, String oldTypeName,
            String newTypeName) {
        initializePrefixesAndSuffixes(project);

        if(isStatic)
            return suggestNewVariableName(fStaticFieldPrefixes, fStaticFieldSuffixes, oldFieldName, oldTypeName, newTypeName);
        else
            return suggestNewVariableName(fFieldPrefixes, fFieldSuffixes, oldFieldName, oldTypeName, newTypeName);
    }

    public String suggestNewLocalName(Project project, String oldLocalName, boolean isArgument, String oldTypeName,
            String newTypeName) {
        initializePrefixesAndSuffixes(project);
        if(isArgument)
            return suggestNewVariableName(fArgumentPrefixes, fArgumentSuffixes, oldLocalName, oldTypeName, newTypeName);
        else
            return suggestNewVariableName(fLocalPrefixes, fLocalSuffixes, oldLocalName, oldTypeName, newTypeName);
    }

    public String suggestNewMethodName(String oldMethodName, String oldTypeName, String newTypeName) {
        resetPrefixes();
        return match(oldTypeName, newTypeName, oldMethodName);
    }

    public String suggestNewVariableName(String[] prefixes, String[] suffixes, String oldVariableName, String oldTypeName,
            String newTypeName) {
        String usedPrefix = findLongestPrefix(oldVariableName, prefixes);
        String usedSuffix = findLongestSuffix(oldVariableName, suffixes);
        String strippedVariableName = oldVariableName.substring(usedPrefix.length(),
                oldVariableName.length() - usedSuffix.length());
        String newVariableName = match(oldTypeName, newTypeName, strippedVariableName);
        return (newVariableName != null) ? usedPrefix + newVariableName + usedSuffix : null;
    }

    // -------------------------------------- Match methods
    private String match(String oldTypeName, String newTypeName, String strippedVariableName) {
        String oldType = oldTypeName;
        String newType = newTypeName;
        if(fExtendedInterfaceNameMatching && isInterfaceName(oldType) && isInterfaceName(newType)) {
            oldType = getInterfaceName(oldType);
            newType = getInterfaceName(newType);
        }
        String newVariableName = matchDirect(oldType, newType, strippedVariableName);
        if(fExtendedPluralMatching && newVariableName == null && canPluralize(oldType))
            newVariableName = matchDirect(pluralize(oldType), pluralize(newType), strippedVariableName);
        return newVariableName;
    }

    private String matchDirect(String oldType, String newType, String strippedVariableName) {
        /*
         * Use all strategies applied by the user. Always start with exact
         * matching.
         * 
         * Note that suffix matching may not match the whole type name if the
         * new type name has a smaller camel case chunk count.
         */
        String newVariableName = exactMatch(oldType, newType, strippedVariableName);
        if(newVariableName == null && fStrategy >= STRATEGY_EMBEDDED)
            newVariableName = embeddedMatch(oldType, newType, strippedVariableName);
        if(newVariableName == null && fStrategy >= STRATEGY_SUFFIX)
            newVariableName = suffixMatch(oldType, newType, strippedVariableName);
        return newVariableName;
    }

    private String exactMatch(String oldTypeName, String newTypeName, String strippedVariableName) {
        String newName = exactDirectMatch(oldTypeName, newTypeName, strippedVariableName);
        if(newName != null)
            return newName;
        if(fExtendedAllUpperCaseHunkMatching && isUpperCaseCamelCaseHunk(oldTypeName)) {
            String oldTN = getFirstUpperRestLowerCased(oldTypeName);
            String newTN = isUpperCaseCamelCaseHunk(newTypeName) ? getFirstUpperRestLowerCased(newTypeName) : newTypeName;
            newName = exactDirectMatch(oldTN, newTN, strippedVariableName);
        }
        return newName;
    }

    private String exactDirectMatch(String oldTypeName, String newTypeName, String strippedVariableName) {
        if(strippedVariableName.equals(oldTypeName))
            return newTypeName;
        if(strippedVariableName.equals(getLowerCased(oldTypeName)))
            return getLowerCased(newTypeName);
        return null;
    }

    private String embeddedMatch(String oldTypeName, String newTypeName, String strippedVariableName) {
        // possibility of a match?
        String lowerCaseVariable = strippedVariableName.toLowerCase();
        String lowerCaseOldTypeName = oldTypeName.toLowerCase();
        int presumedIndex = lowerCaseVariable.indexOf(lowerCaseOldTypeName);
        while(presumedIndex != -1) {
            // it may be there
            String presumedTypeName = strippedVariableName.substring(presumedIndex, presumedIndex + oldTypeName.length());
            String prefix = strippedVariableName.substring(0, presumedIndex);
            String suffix = strippedVariableName.substring(presumedIndex + oldTypeName.length());
            // can match at all? (depends on suffix)
            if(startsNewHunk(suffix)) {
                String name = exactMatch(oldTypeName, newTypeName, presumedTypeName);
                if(name != null)
                    return prefix + name + suffix;
            }
            // did not match -> find next occurrence
            presumedIndex = lowerCaseVariable.indexOf(lowerCaseOldTypeName, presumedIndex + 1);
        }
        return null;
    }

    private String suffixMatch(String oldType, String newType, String strippedVariableName) {
        // get an array of all camel-cased elements from both types + the
        // variable
        String[] suffixesOld = getSuffixes(oldType);
        String[] suffixesNew = getSuffixes(newType);
        String[] suffixesVar = getSuffixes(strippedVariableName);
        // get an equal-sized array of the last n camel-cased elements
        int min = Math.min(suffixesOld.length, suffixesNew.length);
        String[] suffixesOldEqual = new String[min];
        String[] suffixesNewEqual = new String[min];
        System.arraycopy(suffixesOld, suffixesOld.length - min, suffixesOldEqual, 0, min);
        System.arraycopy(suffixesNew, suffixesNew.length - min, suffixesNewEqual, 0, min);
        // find endIndex. endIndex is the index of the last hunk of the old type
        // name in the variable name.
        int endIndex = -1;
        for(int j = suffixesVar.length - 1; j >= 0; j--) {
            String newHunkName = exactMatch(suffixesOldEqual[suffixesOldEqual.length - 1],
                    suffixesNewEqual[suffixesNewEqual.length - 1], suffixesVar[j]);
            if(newHunkName != null) {
                endIndex = j;
                break;
            }
        }
        if(endIndex == -1)
            return null; // last hunk not found -> no match
        int stepBack = 0;
        int lastSuffixMatched = -1;
        int hunkInVarName = -1;
        for(int i = suffixesOldEqual.length - 1; i >= 0; i--) {
            hunkInVarName = endIndex - stepBack;
            stepBack++;
            if(hunkInVarName < 0) {
                // we have reached the beginning of the variable name
                break;
            }
            // try to match this hunk:
            String newHunkName = exactMatch(suffixesOldEqual[i], suffixesNewEqual[i], suffixesVar[hunkInVarName]);
            if(newHunkName == null)
                break; // only match complete suffixes
            suffixesVar[hunkInVarName] = newHunkName;
            lastSuffixMatched = i;
        }
        if(lastSuffixMatched == 0) {
            // we have matched ALL type hunks in the variable name,
            // insert any new prefixes of the new type name
            int newPrefixes = suffixesNew.length - suffixesNewEqual.length;
            if(newPrefixes > 0) {
                // Propagate lowercased start to the front
                if(Character.isLowerCase(suffixesVar[hunkInVarName].charAt(0))
                        && Character.isUpperCase(suffixesOldEqual[lastSuffixMatched].charAt(0))) {
                    suffixesVar[hunkInVarName] = getUpperCased(suffixesVar[hunkInVarName]);
                    suffixesNew[0] = getLowerCased(suffixesNew[0]);
                }
                String[] newVariableName = new String[suffixesVar.length + newPrefixes];
                System.arraycopy(suffixesVar, 0, newVariableName, 0, hunkInVarName); // hunks
                                                                                     // before
                                                                                     // type
                                                                                     // name
                                                                                     // in
                                                                                     // variable
                                                                                     // name
                System.arraycopy(suffixesNew, 0, newVariableName, hunkInVarName, newPrefixes); // new
                                                                                               // hunks
                                                                                               // in
                                                                                               // new
                                                                                               // type
                                                                                               // name
                System.arraycopy(suffixesVar, hunkInVarName, newVariableName, hunkInVarName + newPrefixes,
                        suffixesVar.length - hunkInVarName); // matched
                                                             // +
                                                             // rest
                                                             // hunks
                suffixesVar = newVariableName;
            }
        }
        String varName = concat(suffixesVar);
        if(varName.equals(strippedVariableName))
            return null; // no "silly suggestions"
        else
            return varName;
    }

    // ---------------- Helper methods
    /**
     * True if the string is the beginning of a new camel case hunk. False if it
     * is not.
     */
    private boolean startsNewHunk(String string) {
        if(string.length() == 0)
            return true;
        return isLegalChar(string.charAt(0));
    }

    /**
     * True if hunk is longer than 1 character and all letters in the hunk are
     * uppercase. False if not.
     */
    private boolean isUpperCaseCamelCaseHunk(String hunk) {
        if(hunk.length() < 2)
            return false;
        for(int i = 0; i < hunk.length(); i++) {
            if(!isLegalChar(hunk.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * False if the character is a letter and it is lowercase. True in all other
     * cases.
     */
    private boolean isLegalChar(char c) {
        if(Character.isLetter(c))
            return Character.isUpperCase(c);
        return true;
    }

    private String[] getSuffixes(String typeName) {
        List<String> suffixes = new ArrayList<String>();
        WordIterator iterator = new WordIterator();

        iterator.setText(typeName);

        int lastmatch = 0;
        int match;

        while((match = iterator.next()) != BreakIterator.DONE) {
            suffixes.add(typeName.substring(lastmatch, match));
            lastmatch = match;
        }

        return (String[])suffixes.toArray(new String[0]);
    }

    private String concat(String[] suffixesNewEqual) {
        StringBuffer returner = new StringBuffer();
        for(int j = 0; j < suffixesNewEqual.length; j++) {
            returner.append(suffixesNewEqual[j]);
        }
        return returner.toString();
    }

    private String getLowerCased(String name) {
        if(name.length() > 1)
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        else
            return name.toLowerCase();
    }

    private String getUpperCased(String name) {
        if(name.length() > 1)
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        else
            return name.toLowerCase();
    }

    private String getFirstUpperRestLowerCased(String name) {
        if(name.length() > 1)
            return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
        else
            return name.toLowerCase();
    }

    private boolean isInterfaceName(String typeName) {
        return ((typeName.length() >= 2) && typeName.charAt(0) == 'I' && Character.isUpperCase(typeName.charAt(1)));
    }

    private String getInterfaceName(String typeName) {
        return typeName.substring(1);
    }

    private String findLongestPrefix(String name, String[] prefixes) {
        String usedPrefix = ""; //$NON-NLS-1$
        int bestLen = 0;
        for(int i = 0; i < prefixes.length; i++) {
            if(name.startsWith(prefixes[i])) {
                if(prefixes[i].length() > bestLen) {
                    bestLen = prefixes[i].length();
                    usedPrefix = prefixes[i];
                }
            }
        }
        return usedPrefix;
    }

    private String findLongestSuffix(String name, String[] suffixes) {
        String usedPrefix = ""; //$NON-NLS-1$
        int bestLen = 0;
        for(int i = 0; i < suffixes.length; i++) {
            if(name.endsWith(suffixes[i])) {
                if(suffixes[i].length() > bestLen) {
                    bestLen = suffixes[i].length();
                    usedPrefix = suffixes[i];
                }
            }
        }
        return usedPrefix;
    }

    private boolean canPluralize(String typeName) {
        return !typeName.endsWith(PLURAL_S);
    }

    private String pluralize(String typeName) {
        if(typeName.endsWith(SINGULAR_Y))
            typeName = typeName.substring(0, typeName.length() - 1).concat(PLURAL_IES);
        else if(!typeName.endsWith(PLURAL_S))
            typeName = typeName.concat(PLURAL_S);
        return typeName;
    }

    private void resetPrefixes() {
        String[] empty = new String[0];
        fFieldPrefixes = empty;
        fFieldSuffixes = empty;
        fStaticFieldPrefixes = empty;
        fStaticFieldSuffixes = empty;
        fLocalPrefixes = empty;
        fLocalSuffixes = empty;
        fArgumentPrefixes = empty;
        fArgumentSuffixes = empty;
    }

    private void initializePrefixesAndSuffixes(Project project) {
        fFieldPrefixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_FIELD_PREFIXES*/);
        fFieldSuffixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_FIELD_SUFFIXES*/);
        fStaticFieldPrefixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES*/);
        fStaticFieldSuffixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES*/);
        fLocalPrefixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_LOCAL_PREFIXES*/);
        fLocalSuffixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_LOCAL_SUFFIXES*/);
        fArgumentPrefixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_ARGUMENT_PREFIXES*/);
        fArgumentSuffixes = readCommaSeparatedPreference(project, null/*JavaCore.CODEASSIST_ARGUMENT_SUFFIXES*/);
    }

    private String[] readCommaSeparatedPreference(Project project, String option) {
        return new String[0];
        //		String list = project.getOption(option, true);
        //		return list == null ? new String[0] : list.split(",");
    }
}
