package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import me.tomassetti.symbolsolver.javaparser.Navigator;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.model.declarations.TypeDeclaration;
import me.tomassetti.symbolsolver.model.resolution.SymbolReference;
import me.tomassetti.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class ExtendedJavaParserTypeSolver extends JavaParserTypeSolver {

    private File srcDir;

    public ExtendedJavaParserTypeSolver(File srcDir) {
        super(srcDir);
        this.srcDir = srcDir;
    }

    @Override
    public SymbolReference<TypeDeclaration> tryToSolveType(String name) {
        File srcFile = new File(srcDir.getAbsolutePath() + "/" + name.replaceAll("\\.", "/") + ".java");
        if (srcFile.exists()) {
            try {
                CompilationUnit compilationUnit = JavaParser.parse(srcFile);
                Optional<com.github.javaparser.ast.body.TypeDeclaration> astTypeDeclaration = Navigator.findType(compilationUnit, simpleName(name));
                if (!astTypeDeclaration.isPresent()) {
                    return SymbolReference.unsolved(TypeDeclaration.class);
                }
                TypeDeclaration typeDeclaration = JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration.get());
                return SymbolReference.solved(typeDeclaration);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String className = name;
            if (name.contains(".")) {
                String[] nameArr = name.split("\\.");
                className = nameArr[nameArr.length - 1];
            }
            srcFile = new File(srcFile.getAbsolutePath().replace("\\" + className, ""));
            if (srcFile.exists()) {
                try {
                    CompilationUnit compilationUnit = JavaParser.parse(srcFile);
                    com.github.javaparser.ast.body.TypeDeclaration astTypeDeclaration = getInnerClassDeclaration(compilationUnit, simpleName(name));
                    if (astTypeDeclaration == null) {
                        return SymbolReference.unsolved(TypeDeclaration.class);
                    }
                    TypeDeclaration typeDeclaration = JavaParserFacade.get(this).getTypeDeclaration(astTypeDeclaration);
                    return SymbolReference.solved(typeDeclaration);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return SymbolReference.unsolved(TypeDeclaration.class);
        }
    }

    private static ClassOrInterfaceDeclaration getInnerClassDeclaration(Node node, String name) {
        for (Node childNode : node.getChildrenNodes()) {
            if (childNode instanceof ClassOrInterfaceDeclaration) {
                if (((ClassOrInterfaceDeclaration) childNode).getName().equals(name)) {
                    return (ClassOrInterfaceDeclaration) childNode;
                }
            }
            ClassOrInterfaceDeclaration declaration = getInnerClassDeclaration(childNode, name);
            if (declaration != null) {
                return declaration;
            }
        }
        return null;
    }

    private String simpleName(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return name;
        } else {
            return name.substring(index + 1);
        }
    }

}
