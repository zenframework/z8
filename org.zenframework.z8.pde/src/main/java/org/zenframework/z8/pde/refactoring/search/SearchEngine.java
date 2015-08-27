package org.zenframework.z8.pde.refactoring.search;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.zenframework.z8.compiler.core.IToken;
import org.zenframework.z8.compiler.file.FileException;
import org.zenframework.z8.compiler.parser.grammar.lexer.Lexer;
import org.zenframework.z8.compiler.parser.grammar.lexer.token.TokenException;
import org.zenframework.z8.compiler.workspace.CompilationUnit;
import org.zenframework.z8.pde.build.ReconcileMessageConsumer;
import org.zenframework.z8.pde.refactoring.messages.RefactoringMessages;

public class SearchEngine {
    static public CompilationUnit[] search(String pattern, CompilationUnit[] sources, IProgressMonitor monitor)
            throws CoreException {
        return search(new String[] { pattern }, sources, monitor);
    }

    static public CompilationUnit[] search(String[] patterns, CompilationUnit[] sources, IProgressMonitor monitor)
            throws CoreException {
        if(monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        if(monitor != null) {
            monitor.beginTask(RefactoringMessages.engine_searching, 100 * sources.length);
        }

        long millis = System.currentTimeMillis();

        List<CompilationUnit> result = new ArrayList<CompilationUnit>();
        Set<String> patternSet = new HashSet<String>(Arrays.asList(patterns));

        try {
            System.out.println("Initializing search engine");

            for(CompilationUnit compilationUnit : sources) {
                try {
                    if(monitor != null && monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }

                    Lexer lexer = new Lexer(compilationUnit, null);

                    IToken token = null;

                    do {
                        token = lexer.nextToken();

                        if(token.getId() == IToken.IDENTIFIER && patternSet.contains(token.getRawText())) {
                            result.add(compilationUnit);

                            char[] content = lexer.getContent();
                            ReconcileMessageConsumer consumer = new ReconcileMessageConsumer();
                            compilationUnit.getProject().reconcile(compilationUnit.getResource(), content, consumer);
                            break;
                        }

                    } while(token.getId() != IToken.NOTHING);
                } catch(TokenException e) {
                    compilationUnit.error(e.getPosition(), e.getMessage());
                } catch(FileException e) {
                    compilationUnit.error(e);
                } catch (UnsupportedEncodingException e) {
                    compilationUnit.error(e);
                } finally {
                    if(monitor != null) {
                        monitor.worked(1);
                    }
                }
            }
        }
        finally {
            if(monitor != null)
                monitor.done();
        }

        System.out.println("Search engine initialized in " + (System.currentTimeMillis() - millis) / 1000.0 + " seconds");

        return result.toArray(new CompilationUnit[result.size()]);
    }
}
