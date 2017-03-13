package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;

import java.io.File;
import java.io.IOException;
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

    public RecursiveMethodExplorer(Set<String> targetPackageSet, Set<String> sourceDirectorySet, Set<String> targetFunctionSet) {
        this.targetPackageSet = targetPackageSet;
        this.sourceDirectorySet = sourceDirectorySet;
        this.targetFunctionSet = targetFunctionSet;
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
                    cu.accept(new TargetFunctionVisitor(qualifiedClassName.toString(), targetPackageSet, targetFunctionSet), javaParserFacade);
                }
            }
        }
    }

}
