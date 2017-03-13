package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class TargetFunctionVisitor extends VoidVisitorAdapter<JavaParserFacade> {

    private final String qualifiedClassName;
    private final RecursiveMethodExplorer methodExplorer;

    public TargetFunctionVisitor(String qualifiedClassName, RecursiveMethodExplorer methodExplorer) {
        this.qualifiedClassName = qualifiedClassName;
        this.methodExplorer = methodExplorer;
    }

    @Override
    public void visit(final MethodDeclaration methodDeclaration, final JavaParserFacade javaParserFacade) {
        super.visit(methodDeclaration, javaParserFacade);

        String functionSignature = Utils.getFunctionSignature(qualifiedClassName, methodDeclaration).toString();
        boolean monitor = false;
        for (String packageName : methodExplorer.getTargetPackageSet()) {
            if (functionSignature.startsWith(packageName)) {
                monitor = true;
                break;
            }
        }
        if (monitor && methodExplorer.getTargetFunctionSet().contains(functionSignature)
                && !methodExplorer.getVisitedFunctionSet().contains(functionSignature)) {
            methodExplorer.getVisitedFunctionSet().add(functionSignature);
            System.out.println("Exploring method: " + functionSignature);
            methodDeclaration.accept(new MethodCallResolver(methodExplorer), javaParserFacade);
        }
    }

}
