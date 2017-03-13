package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import me.tomassetti.symbolsolver.model.invokations.MethodUsage;

import java.util.Set;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class FunctionMethodCallVisitor extends VoidVisitorAdapter<JavaParserFacade> {

    private final Set<String> targetPackageSet;
    private final Set<String> visitedFunctionSet;
    private final Set<String> targetFunctionSet;

    public FunctionMethodCallVisitor(Set<String> targetPackageSet, Set<String> visitedFunctionSet, Set<String> targetFunctionSet) {
        this.targetPackageSet = targetPackageSet;
        this.visitedFunctionSet = visitedFunctionSet;
        this.targetFunctionSet = targetFunctionSet;
    }

    @Override
    public void visit(final MethodCallExpr n, final JavaParserFacade javaParserFacade) {
        super.visit(n, javaParserFacade);

        System.out.println("Resolving method call: " + n.toStringWithoutComments());
        try {
//            SymbolReference<MethodDeclaration> solve = javaParserFacade.solve(n);
//            if (!solve.isSolved() || !(solve.getCorrespondingDeclaration() instanceof JavaParserMethodDeclaration)) {
//                return;
//            }
//            JavaParserMethodDeclaration sourceMethodDeclaration = (JavaParserMethodDeclaration) solve.getCorrespondingDeclaration();
            MethodUsage methodUsage = javaParserFacade.solveMethodAsUsage(n);
            if (methodUsage == null || !(methodUsage.getDeclaration() instanceof JavaParserMethodDeclaration)) {
                return;
            }
            JavaParserMethodDeclaration sourceMethodDeclaration = (JavaParserMethodDeclaration) methodUsage.getDeclaration();
            com.github.javaparser.ast.body.MethodDeclaration methodDeclaration = sourceMethodDeclaration.getWrappedNode();

            Node tmp = methodDeclaration;
            CompilationUnit compilationUnit = null;
            while (compilationUnit == null) {
                tmp = tmp.getParentNode();
                if (tmp instanceof CompilationUnit) {
                    compilationUnit = (CompilationUnit) tmp;
                }
            }

            String qualifiedClassName = compilationUnit.getPackage().getName().toStringWithoutComments() + "." + compilationUnit.getTypes().get(0).getName();
            String functionSignature = getFunctionSignature(qualifiedClassName, methodDeclaration).toString();
            if (!visitedFunctionSet.contains(functionSignature)) {
                targetFunctionSet.add(functionSignature);
                methodDeclaration.accept(new TargetFunctionVisitor(qualifiedClassName, targetPackageSet, targetFunctionSet), javaParserFacade);
            }
        } catch (Exception ex) {
            String reason = ex.getMessage();
            if (reason == null) {
                reason = ex.toString();
            }
            System.err.println("Unable to resolve method call: " + n.toStringWithoutComments());
            System.err.println("Reason: " + reason);
            System.err.println("If you want to monitor this method please add manually with -target_function argument!");
        }
    }

    private StringBuilder getFunctionSignature(String qualifiedClassName, com.github.javaparser.ast.body.MethodDeclaration methodDeclaration) {
        StringBuilder functionSignature = new StringBuilder(qualifiedClassName).append(".");
        functionSignature.append(methodDeclaration.getName()).append("(");
        for (Parameter param : methodDeclaration.getParameters()) {
            functionSignature.append(param.getType().toStringWithoutComments());
        }
        functionSignature.append(")");
        return functionSignature;
    }

}
