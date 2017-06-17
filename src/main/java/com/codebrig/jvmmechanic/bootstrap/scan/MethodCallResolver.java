package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.model.declarations.MethodDeclaration;
import com.github.javaparser.symbolsolver.model.methods.MethodUsage;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceType;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Given a method call expression this resolver attempts to determine the
 * invoked method's source code declaration.
 *
 * todo: given abstract/interface find implementations
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MethodCallResolver extends VoidVisitorAdapter<JavaParserFacade> {

    private final RecursiveMethodExplorer methodExplorer;

    MethodCallResolver(RecursiveMethodExplorer methodExplorer) {
        this.methodExplorer = methodExplorer;
    }

    @Override
    public void visit(final ObjectCreationExpr objectCreation, final JavaParserFacade javaParserFacade) {
        super.visit(objectCreation, javaParserFacade);

        try {
            Type typeUsage = javaParserFacade.getType(objectCreation);
            if (typeUsage instanceof ReferenceType) {
                ReferenceType referenceTypeUsage = (ReferenceType) typeUsage;
                String qualifiedName = referenceTypeUsage.getTypeDeclaration().getQualifiedName();

                boolean monitor = methodExplorer.getTargetPackageSet().isEmpty();
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
        } catch(Exception ex) {
            String objectType = objectCreation.getType().toString(new PrettyPrinterConfiguration().setPrintComments(false));
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

        System.out.println("Resolving method call: " + methodCall.toString(new PrettyPrinterConfiguration().setPrintComments(false)));
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
            while (compilationUnit == null && tmp != null) {
                tmp = tmp.getParentNode().orElse(null);
                if (tmp instanceof CompilationUnit) {
                    compilationUnit = (CompilationUnit) tmp;
                }
            }

            String packageName = "";
            if (compilationUnit.getPackageDeclaration().isPresent()) {
                packageName = compilationUnit.getPackageDeclaration().get().getName().toString(new PrettyPrinterConfiguration().setPrintComments(false));
            }
            String qualifiedClassName = packageName + "." + compilationUnit.getTypes().get(0).getName();
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
                methodExplorer.getFailedFunctionSet().add(methodCall.toString(new PrettyPrinterConfiguration().setPrintComments(false)));
//                System.err.println("Unable to resolve method call: " + methodCall.toString(new PrettyPrinterConfiguration().setPrintComments(false)));
//                System.err.println("Failed rule injection on method: " + functionSignature);
//                System.err.println("Reason: " + reason);
//                System.err.println("If you want to monitor this method please add manually with -target_function argument!");
            }
        }
    }

    private StringBuilder getFunctionSignatureFromMethodCall(MethodCallExpr methodCall, JavaParserFacade javaParserFacade) {
        StringBuilder functionSignature = new StringBuilder();
        if (methodCall.getScope() != null) {
            functionSignature.append(methodCall.getScope().orElse(null).toString(new PrettyPrinterConfiguration().setPrintComments(false))).append(".");
        }
        functionSignature.append(methodCall.getName()).append("(");
        boolean endsWithComma = false;
        for (Expression arg : methodCall.getArguments()) {
            try {
                Type type = javaParserFacade.getType(arg);
                functionSignature.append(type.describe()).append(",");
                endsWithComma = true;
            } catch(Exception ex) {
                //ex.printStackTrace();
            }
        }
        if (endsWithComma) {
            functionSignature.substring(0, functionSignature.length() - 2);
        }
        functionSignature.append(")");
        return functionSignature;
    }

    private static Set<MethodCallExpr> getInnerMethodCalls(Node node, Set<MethodCallExpr> methodCallExprList) {
        for (Node childNode : node.getChildNodes()) {
            if (childNode instanceof MethodCallExpr) {
                methodCallExprList.add((MethodCallExpr) childNode);
            }

            Set<MethodCallExpr> tmpMethodCallExprList = getInnerMethodCalls(childNode, methodCallExprList);
            methodCallExprList.addAll(tmpMethodCallExprList);
        }
        return methodCallExprList;
    }

}
