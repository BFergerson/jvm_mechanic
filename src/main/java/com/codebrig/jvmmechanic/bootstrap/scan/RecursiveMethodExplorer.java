package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class RecursiveMethodExplorer {

    private final Set<String> targetPackageSet;
    private final Set<String> sourceDirectorySet;
    private final Set<String> targetFunctionSet;
    private final Set<String> excludeFunctionSet;
    private final Set<String> failedFunctionSet;
    private final Set<String> visitedFunctionSet;
    private final Set<String> visitedConstructorSet;
    private final Set<String> failedConstructorSet;

    public RecursiveMethodExplorer(Set<String> targetPackageSet, Set<String> sourceDirectorySet,
                                   Set<String> targetFunctionSet, Set<String> excludeFunctionSet) {
        this.targetPackageSet = targetPackageSet;
        this.sourceDirectorySet = sourceDirectorySet;
        this.targetFunctionSet = targetFunctionSet;
        this.excludeFunctionSet = excludeFunctionSet;
        this.visitedFunctionSet = new HashSet<>();
        this.failedFunctionSet = new HashSet<>();
        this.visitedConstructorSet = new HashSet<>();
        this.failedConstructorSet = new HashSet<>();
    }

    public void explore(JavaParserFacade javaParserFacade) throws IOException, ParseException {
        for (String targetFunction : targetFunctionSet) {
            String[] targetFunctionArr = targetFunction.split("\\.");

            StringBuilder qualifiedClassName = new StringBuilder();
            StringBuilder filePath = new StringBuilder();
            for (int i = 0; i < targetFunctionArr.length - 1; i++) {
                filePath.append(targetFunctionArr[i]);
                qualifiedClassName.append(targetFunctionArr[i]);
                if ((i + 1) < targetFunctionArr.length - 1) {
                    filePath.append("\\");
                    qualifiedClassName.append(".");
                }
            }
            filePath.append(".java");

            for (String sourceDirectory : sourceDirectorySet) {
                File sourceFile = new File(sourceDirectory, filePath.toString());
                if (sourceFile.exists()) {
                    final CompilationUnit cu = JavaParser.parse(sourceFile);
                    cu.accept(new TargetFunctionVisitor(qualifiedClassName.toString(), this), javaParserFacade);
                }
            }
        }
    }

    public Set<String> getTargetPackageSet() {
        return targetPackageSet;
    }

    public Set<String> getSourceDirectorySet() {
        return sourceDirectorySet;
    }

    public Set<String> getTargetFunctionSet() {
        return targetFunctionSet;
    }

    public Set<String> getExcludeFunctionSet() {
        return excludeFunctionSet;
    }

    public Set<String> getFailedFunctionSet() {
        return failedFunctionSet;
    }

    public Set<String> getVisitedFunctionSet() {
        return visitedFunctionSet;
    }

    public Set<String> getVisitedConstructorSet() {
        return visitedConstructorSet;
    }

    public Set<String> getFailedConstructorSet() {
        return failedConstructorSet;
    }

}
