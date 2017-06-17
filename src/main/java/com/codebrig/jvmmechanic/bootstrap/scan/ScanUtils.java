package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
class ScanUtils {

    static StringBuilder getFunctionSignature(String qualifiedClassName, MethodDeclaration methodDeclaration) {
        Node parentNode = methodDeclaration.getParentNode().orElse(null);
        if (parentNode instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration declaration = (ClassOrInterfaceDeclaration) parentNode;
            String className = getClassName(qualifiedClassName);
            if (!className.equals(declaration.getName().toString(new PrettyPrinterConfiguration().setPrintComments(false)))) {
                qualifiedClassName += "$" + declaration.getName(); //inner class
            }
        }
        StringBuilder functionSignature = new StringBuilder(qualifiedClassName).append(".");
        functionSignature.append(methodDeclaration.getName()).append("(");
        List<Parameter> parameters = methodDeclaration.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter param = parameters.get(i);
            functionSignature.append(param.getType().toString(new PrettyPrinterConfiguration().setPrintComments(false)));

            if ((i + 1) < parameters.size()) {
                functionSignature.append(",");
            }
        }
        functionSignature.append(")");
        return functionSignature;
    }

    private static String getClassName(String qualifiedClassName) {
        if (qualifiedClassName != null && qualifiedClassName.contains(".")) {
            String[] arr = qualifiedClassName.split("\\.");
            return arr[arr.length - 1];
        }
        return null;
    }

}
