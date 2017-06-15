package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Sets;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import me.tomassetti.symbolsolver.model.declarations.MethodDeclaration;
import me.tomassetti.symbolsolver.model.invokations.MethodUsage;
import me.tomassetti.symbolsolver.model.resolution.SymbolReference;
import me.tomassetti.symbolsolver.model.typesystem.ReferenceTypeUsage;
import me.tomassetti.symbolsolver.model.typesystem.TypeUsage;

import java.util.Set;

/**
 * todo: this
 * todo: given abstract/interface find implementations
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MethodCallResolver extends VoidVisitorAdapter<JavaParserFacade> {

    private final RecursiveMethodExplorer methodExplorer;

    public MethodCallResolver(RecursiveMethodExplorer methodExplorer) {
        this.methodExplorer = methodExplorer;
    }

    @Override
    public void visit(final ObjectCreationExpr objectCreation, final JavaParserFacade javaParserFacade) {
        super.visit(objectCreation, javaParserFacade);

        try {
            TypeUsage typeUsage = javaParserFacade.getType(objectCreation);
            if (typeUsage instanceof ReferenceTypeUsage) {
                ReferenceTypeUsage referenceTypeUsage = (ReferenceTypeUsage) typeUsage;
                String qualifiedName = referenceTypeUsage.getTypeDeclaration().getQualifiedName();

                boolean monitor = false;
                for (String packageName : methodExplorer.getTargetPackageSet()) {
                    if (qualifiedName.startsWith(packageName)) {
                        monitor = true;
                        break;
                    }
                }
                if (monitor && !methodExplorer.getVisitedConstructorSet().contains(qualifiedName)) {
                    methodExplorer.getVisitedConstructorSet().add(qualifiedName);
                    System.out.println("Will monitor constructor(s) of class: " + qualifiedName);
                }
            }
            //visitedConstructorSet.add(o2.toString());
//            MethodUsage methodUsage = javaParserFacade.solveMethodAsUsage(objectCreation);
//            if (methodUsage == null || !(methodUsage.getDeclaration() instanceof JavaParserMethodDeclaration)) {
//                return;
//            }
        } catch(Exception ex) {
            String objectType = objectCreation.getType().toStringWithoutComments();
            if (!methodExplorer.getFailedConstructorSet().contains(objectType)) {
                methodExplorer.getFailedConstructorSet().add(objectType);
                //System.err.println("Unable monitor constructor(s): " + objectType);
            }
            //ex.printStackTrace();
        }
    }

    @Override
    public void visit(final MethodCallExpr methodCall, final JavaParserFacade javaParserFacade) {
        super.visit(methodCall, javaParserFacade);

        //if method call contains method calls, explore those first
        Set<MethodCallExpr> methodCallExprList = Sets.newIdentityHashSet();
        for (MethodCallExpr methodCallExpr : getInnerMethodCalls(methodCall, methodCallExprList)) {
            methodCallExpr.accept(this, javaParserFacade);
        }

        System.out.println("Resolving method call: " + methodCall.toStringWithoutComments());
        try {
            com.github.javaparser.ast.body.MethodDeclaration methodDeclaration;
            try {
                SymbolReference<MethodDeclaration> solve = javaParserFacade.solve(methodCall);
                if (!solve.isSolved() || !(solve.getCorrespondingDeclaration() instanceof JavaParserMethodDeclaration)) {
                    return;
                }
                JavaParserMethodDeclaration sourceMethodDeclaration = (JavaParserMethodDeclaration) solve.getCorrespondingDeclaration();
                methodDeclaration = sourceMethodDeclaration.getWrappedNode();
            } catch (Exception ex) {
                MethodUsage methodUsage = javaParserFacade.solveMethodAsUsage(methodCall);
                if (!(methodUsage.getDeclaration() instanceof JavaParserMethodDeclaration)) {
                    throw ex;
                } else {
                    methodDeclaration = ((JavaParserMethodDeclaration) methodUsage.getDeclaration()).getWrappedNode();
                }
            }

            Node tmp = methodDeclaration;
            CompilationUnit compilationUnit = null;
            while (compilationUnit == null) {
                tmp = tmp.getParentNode();
                if (tmp instanceof CompilationUnit) {
                    compilationUnit = (CompilationUnit) tmp;
                }
            }

            String qualifiedClassName = compilationUnit.getPackage().getName().toStringWithoutComments() + "." + compilationUnit.getTypes().get(0).getName();
            String functionSignature = ScanUtils.getFunctionSignature(qualifiedClassName, methodDeclaration).toString();
            if (!methodExplorer.getVisitedFunctionSet().contains(functionSignature)) {
                methodExplorer.getTargetFunctionSet().add(functionSignature);
                TargetFunctionVisitor targetFunctionVisitor = new TargetFunctionVisitor(qualifiedClassName, methodExplorer);
                methodDeclaration.accept(targetFunctionVisitor, javaParserFacade);
            }
        } catch (Exception ex) {
            String reason = ex.getMessage();
            if (reason == null) {
                reason = ex.toString();
            }
            if (ex instanceof UnsupportedOperationException) {
                if (reason.contains("CtClass")) {
                    //expected when trying to solve types with jar type solver
                    //can safely ignore as we won't inject this code with rules
                    return;
                }
            }

            if (reason.contains("JavassistClassDeclaration") && reason.contains("Method '")) {
                //todo: resolving static method calls
                String methodName = reason.substring(reason.indexOf("Method '") + 8);
                methodName = methodName.substring(0, methodName.indexOf("'"));
                reason = "Could not find (possibly static) method call: " + methodName;
            }

            String functionSignature = getFunctionSignatureFromMethodCall(methodCall, javaParserFacade).toString();
            if (!methodExplorer.getFailedFunctionSet().contains(functionSignature)) {
                methodExplorer.getFailedFunctionSet().add(methodCall.toStringWithoutComments());
//                System.err.println("Unable to resolve method call: " + methodCall.toStringWithoutComments());
//                System.err.println("Failed rule injection on method: " + functionSignature);
//                System.err.println("Reason: " + reason);
//                System.err.println("If you want to monitor this method please add manually with -target_function argument!");
            }
        }
    }



    private StringBuilder getFunctionSignatureFromMethodCall(MethodCallExpr methodCall, JavaParserFacade javaParserFacade) {
        StringBuilder functionSignature = new StringBuilder();
        if (methodCall.getScope() != null) {
            functionSignature.append(methodCall.getScope().toStringWithoutComments()).append(".");
        }
        functionSignature.append(methodCall.getName()).append("(");
//        for (Expression arg : methodCall.getArgs()) {
//            try {
//                TypeUsage type = javaParserFacade.getType(arg);
//                String s = type.describe();
//
//                System.out.println(type);
//            } catch(Exception ex) {
//                ex.printStackTrace();
//            }
//        }
        functionSignature.append(")");
        return functionSignature;
    }

    private static Set<MethodCallExpr> getInnerMethodCalls(Node node, Set<MethodCallExpr> methodCallExprList) {
        for (Node childNode : node.getChildrenNodes()) {
            if (childNode instanceof MethodCallExpr) {
                methodCallExprList.add((MethodCallExpr) childNode);
            }

            Set<MethodCallExpr> tmpMethodCallExprList = getInnerMethodCalls(childNode, methodCallExprList);
            methodCallExprList.addAll(tmpMethodCallExprList);
        }
        return methodCallExprList;
    }

}
