package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GetterSetterResolver extends VoidVisitorAdapter<JavaParserFacade> {

    private boolean objectCreated;
    private boolean methodCalled;
    private int fieldsAccessed;
    private boolean returnStatementPresent;

    @Override
    public void visit(final ObjectCreationExpr objectCreation, final JavaParserFacade javaParserFacade) {
        super.visit(objectCreation, javaParserFacade);
        objectCreated = true;
    }

    @Override
    public void visit(final MethodCallExpr methodCall, final JavaParserFacade javaParserFacade) {
        super.visit(methodCall, javaParserFacade);
        methodCalled = true;
    }

    @Override
    public void visit(final FieldAccessExpr fieldAccessExpr, final JavaParserFacade javaParserFacade) {
        super.visit(fieldAccessExpr, javaParserFacade);
        fieldsAccessed++;
    }

    @Override
    public void visit(final ReturnStmt returnStmt, final JavaParserFacade javaParserFacade) {
        super.visit(returnStmt, javaParserFacade);
        returnStatementPresent = true;
    }

    public boolean isValidGetter() {
        //todo: cannot set any values
        if (fieldsAccessed > 1) {
            return false; //can only access up to one field
        } else if (objectCreated) {
            return false; //cannot init any variables
        } else if (methodCalled) {
            return false; //cannot call any methods
        } else if (!returnStatementPresent) {
            return false; //must have return statement
        }
        return true;
    }

    public boolean isValidSetter() {
        //todo: cannot set any values but one which has to be what the singular arg passed
        if (methodCalled) {
            return false; //cannot call any functions
        } else if (fieldsAccessed < 1) {
            return false; //must access at least one field
        }
        return true;
    }

}
