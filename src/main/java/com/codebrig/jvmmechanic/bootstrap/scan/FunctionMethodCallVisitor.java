package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class FunctionMethodCallVisitor extends VoidVisitorAdapter<Void> {

    public Set<String> targetFunctionSet;

    public FunctionMethodCallVisitor() {
        this.targetFunctionSet = new HashSet<>();
        targetFunctionSet.add("processExtendedTransaction(RestfulTransactionRequest)");
    }

    public FunctionMethodCallVisitor(Set<String> targetFunctionSet) {
        this.targetFunctionSet = targetFunctionSet;
    }

    @Override
    public void visit(final MethodCallExpr n, final Void arg) {
        super.visit(n, arg);

        //todo: each method call represent another method which is a target
    }

}
