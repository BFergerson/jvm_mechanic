package com.codebrig.jvmmechanic.bootstrap.scan;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class RecursiveMethodExplorer {

    private Set<String> sourcePackageList;
    private Set<String> sourceDirectoryList;

    public RecursiveMethodExplorer(Set<String> sourcePackageList, Set<String> sourceDirectoryList) {
        this.sourcePackageList = sourcePackageList;
        this.sourceDirectoryList = sourceDirectoryList;
    }

    public void explore() {
        //parseCompilationUnit(new File("C:\\temp\\WSI\\AlchemyWSI\\src\\main\\java\\com\\kobie\\alchemy\\wsi\\service\\impl\\RestfulTransactionServicesImpl.java"));
    }

    public static void main(final String[] args) throws Exception {

    }

    public static void parseCompilationUnit(final File sourceFile) throws ParseException, IOException {
        final CompilationUnit cu = JavaParser.parse(sourceFile);
        cu.accept(new TargetFunctionVisitor(), JavaParserFacade.get(null));
    }

}
