package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class Utils {

    public static StringBuilder getFunctionSignature(String qualifiedClassName, MethodDeclaration methodDeclaration) {
        StringBuilder functionSignature = new StringBuilder(qualifiedClassName).append(".");
        functionSignature.append(methodDeclaration.getName()).append("(");
        for (Parameter param : methodDeclaration.getParameters()) {
            functionSignature.append(param.getType().toStringWithoutComments());
        }
        functionSignature.append(")");
        return functionSignature;
    }

}
