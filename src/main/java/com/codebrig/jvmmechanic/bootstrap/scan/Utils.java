package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;

import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class Utils {

    public static StringBuilder getFunctionSignature(String qualifiedClassName, MethodDeclaration methodDeclaration) {
        StringBuilder functionSignature = new StringBuilder(qualifiedClassName).append(".");
        functionSignature.append(methodDeclaration.getName()).append("(");
        List<Parameter> parameters = methodDeclaration.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter param = parameters.get(i);
            functionSignature.append(param.getType().toStringWithoutComments());

            if ((i + 1) < parameters.size()) {
                functionSignature.append(",");
            }
        }
        functionSignature.append(")");
        return functionSignature;
    }

}
