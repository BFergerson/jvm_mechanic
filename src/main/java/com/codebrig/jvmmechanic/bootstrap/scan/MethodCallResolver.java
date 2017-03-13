package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.collect.Sets;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import me.tomassetti.symbolsolver.model.invokations.MethodUsage;
import me.tomassetti.symbolsolver.model.typesystem.ReferenceTypeUsage;
import me.tomassetti.symbolsolver.model.typesystem.TypeUsage;

import java.util.Set;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MethodCallResolver extends VoidVisitorAdapter<JavaParserFacade> {

    private final Set<String> targetPackageSet;
    private final Set<String> visitedFunctionSet;
    private final Set<String> targetFunctionSet;
    private final Set<String> failedFunctionSet;
    private final Set<String> visitedConstructorSet;
    private final Set<String> failedConstructorSet;

    public MethodCallResolver(Set<String> targetPackageSet, Set<String> visitedFunctionSet, Set<String> targetFunctionSet,
                              Set<String> failedFunctionSet, Set<String> visitedConstructorSet, Set<String> failedConstructorSet) {
        this.targetPackageSet = targetPackageSet;
        this.visitedFunctionSet = visitedFunctionSet;
        this.targetFunctionSet = targetFunctionSet;
        this.failedFunctionSet = failedFunctionSet;
        this.visitedConstructorSet = visitedConstructorSet;
        this.failedConstructorSet = failedConstructorSet;
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
                for (String packageName : targetPackageSet) {
                    if (qualifiedName.startsWith(packageName)) {
                        monitor = true;
                        break;
                    }
                }
                if (monitor && !visitedConstructorSet.contains(qualifiedName)) {
                    visitedConstructorSet.add(qualifiedName);
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
            if (!failedConstructorSet.contains(objectType)) {
                failedConstructorSet.add(objectType);
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
//            SymbolReference<MethodDeclaration> solve = javaParserFacade.solve(methodCall);
//            if (!solve.isSolved() || !(solve.getCorrespondingDeclaration() instanceof JavaParserMethodDeclaration)) {
//                return;
//            }
//            JavaParserMethodDeclaration sourceMethodDeclaration = (JavaParserMethodDeclaration) solve.getCorrespondingDeclaration();
            MethodUsage methodUsage = javaParserFacade.solveMethodAsUsage(methodCall);
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
            String functionSignature = Utils.getFunctionSignature(qualifiedClassName, methodDeclaration).toString();
            if (!visitedFunctionSet.contains(functionSignature)) {
                targetFunctionSet.add(functionSignature);
                TargetFunctionVisitor targetFunctionVisitor = new TargetFunctionVisitor(qualifiedClassName, targetPackageSet, targetFunctionSet, failedFunctionSet, visitedFunctionSet, visitedConstructorSet, failedConstructorSet);
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
            if (!failedFunctionSet.contains(functionSignature)) {
                failedFunctionSet.add(methodCall.toStringWithoutComments());
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

    public Set<String> getTargetPackageSet() {
        return targetPackageSet;
    }

    public Set<String> getVisitedFunctionSet() {
        return visitedFunctionSet;
    }

    public Set<String> getTargetFunctionSet() {
        return targetFunctionSet;
    }

    public Set<String> getFailedFunctionSet() {
        return failedFunctionSet;
    }

}
