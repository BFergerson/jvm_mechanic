package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;

import java.util.HashSet;
import java.util.Set;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class TargetFunctionVisitor extends VoidVisitorAdapter<JavaParserFacade> {

    private final String qualifiedClassName;
    private final Set<String> targetPackageSet;
    private final Set<String> targetFunctionSet;
    private final Set<String> failedFunctionSet;
    private final Set<String> visitedFunctionSet;
    private final Set<String> visitedConstructorSet;
    private final Set<String> failedConstructorSet;

    public TargetFunctionVisitor(String qualifiedClassName, Set<String> targetPackageSet, Set<String> targetFunctionSet,
                                 Set<String> failedFunctionSet, Set<String> visitedFunctionSet, Set<String> visitedConstructorSet, Set<String> failedConstructorSet) {
        this.qualifiedClassName = qualifiedClassName;
        this.targetPackageSet = targetPackageSet;
        this.targetFunctionSet = targetFunctionSet;
        this.failedFunctionSet = failedFunctionSet;
        this.visitedFunctionSet = visitedFunctionSet;
        this.visitedConstructorSet = visitedConstructorSet;
        this.failedConstructorSet = failedConstructorSet;
    }

    @Override
    public void visit(final MethodDeclaration methodDeclaration, final JavaParserFacade javaParserFacade) {
        super.visit(methodDeclaration, javaParserFacade);

        String functionSignature = Utils.getFunctionSignature(qualifiedClassName, methodDeclaration).toString();
        boolean monitor = false;
        for (String packageName : targetPackageSet) {
            if (functionSignature.startsWith(packageName)) {
                monitor = true;
                break;
            }
        }
        if (monitor && targetFunctionSet.contains(functionSignature)
                && !visitedFunctionSet.contains(functionSignature)) {
            visitedFunctionSet.add(functionSignature);
            System.out.println("Exploring method: " + functionSignature);
            methodDeclaration.accept(new MethodCallResolver(targetPackageSet, visitedFunctionSet, targetFunctionSet, failedFunctionSet, visitedConstructorSet, failedConstructorSet), javaParserFacade);
        }
    }

}
