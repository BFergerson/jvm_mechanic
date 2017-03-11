package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.typesystem.TypeUsage;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class TargetFunctionVisitor extends VoidVisitorAdapter<JavaParserFacade> {

    private final Set<String> targetPackageSet;
    private final Set<String> targetFunctionSet;

    public TargetFunctionVisitor() {
        this.targetFunctionSet = new HashSet<>();
        this.targetPackageSet = new HashSet<>();

    }

    public TargetFunctionVisitor(Set<String> targetPackageSet, Set<String> targetFunctionSet) {
        this.targetPackageSet = targetPackageSet;
        this.targetFunctionSet = targetFunctionSet;
    }

    @Override
    public void visit(final MethodDeclaration methodCallExpr, final JavaParserFacade javaParserFacade) {
        super.visit(methodCallExpr, javaParserFacade);

        //make function signature
        StringBuilder functionSignature = new StringBuilder();
        functionSignature.append(methodCallExpr.getName()).append("(");
        for (Parameter param : methodCallExpr.getParameters()) {
            functionSignature.append(param.getType().toStringWithoutComments());
        }
        functionSignature.append(")");

        if (targetFunctionSet.contains(functionSignature.toString())) {
            //todo: each target needs to have their method calls extracted and visited
            TypeUsage typeUsage = javaParserFacade.getType(methodCallExpr);
            System.out.println(typeUsage);
        }
    }

}
