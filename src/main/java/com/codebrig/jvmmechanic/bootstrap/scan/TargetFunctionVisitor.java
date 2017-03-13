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
    private final Set<String> visitedFunctionSet;

    public TargetFunctionVisitor(String qualifiedClassName, Set<String> targetPackageSet, Set<String> targetFunctionSet) {
        this.qualifiedClassName = qualifiedClassName;
        this.targetPackageSet = targetPackageSet;
        this.targetFunctionSet = targetFunctionSet;
        this.visitedFunctionSet = new HashSet<>();
    }

    @Override
    public void visit(final MethodDeclaration methodDeclaration, final JavaParserFacade javaParserFacade) {
        super.visit(methodDeclaration, javaParserFacade);

        StringBuilder functionSignature = getFunctionSignature(methodDeclaration);
        if (targetFunctionSet.contains(functionSignature.toString())
                && !visitedFunctionSet.contains(functionSignature.toString())) {
            visitedFunctionSet.add(functionSignature.toString());
            System.out.println("Exploring method: " + functionSignature.toString());
            methodDeclaration.accept(new FunctionMethodCallVisitor(targetPackageSet, visitedFunctionSet, targetFunctionSet), javaParserFacade);
        }
    }

    private StringBuilder getFunctionSignature(MethodDeclaration methodDeclaration) {
        StringBuilder functionSignature = new StringBuilder(qualifiedClassName).append(".");
        functionSignature.append(methodDeclaration.getName()).append("(");
        for (Parameter param : methodDeclaration.getParameters()) {
            functionSignature.append(param.getType().toStringWithoutComments());
        }
        functionSignature.append(")");
        return functionSignature;
    }

}
