package com.codebrig.jvmmechanic.bootstrap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.codebrig.jvmmechanic.bootstrap.rule.MechanicRuleGenerator;
import com.codebrig.jvmmechanic.bootstrap.scan.ExtendedJavaParserTypeSolver;
import com.codebrig.jvmmechanic.bootstrap.scan.RecursiveMethodExplorer;
import com.github.javaparser.ParseException;
import me.tomassetti.symbolsolver.javaparsermodel.JavaParserFacade;
import me.tomassetti.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JarTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JreTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class BootstrapCLI {

    @Parameter(names = "-source_package", description = "Package(s) of Java source code to be used for type solving")
    public List<String> sourcePackageList;

    @Parameter(names = "-source_directory", description = "Directory/Directories of Java source code to be used for type solving")
    public List<String> sourceDirectoryList;

    @Parameter(names = "-scan_directory", description = "Directory/directories to scan for Java source code to be used for type solving")
    public List<String> scanDirectoryList;

    @Parameter(names = "-target_function", description = "Functions to create rules for")
    public List<String> targetFunctionList;

    @Parameter(names = "-project_library", description = ".jar(s) of Java libraries to be used for type solving")
    public List<String> projectLibraryList;

    @Parameter(names = "-exclude_function", description = "Functions to exclude creating rules for")
    public List<String> excludeFunctionList;

    @Parameter(names = {"-help", "--help"}, description = "Displays help information")
    public boolean help;

    public static void main(String[] args) throws IOException, ParseException {
        BootstrapCLI cli = new BootstrapCLI();
        JCommander commander;
        try {
            commander = new JCommander(cli, args);
            commander.setProgramName("jvm_mechanic - Bootstrap CLI");

            StringBuilder sb = new StringBuilder();
            commander.usage(sb);
            if (cli.help) {
                System.out.println(sb.toString().replace("Options:", "Options:\n")); //prettier usage output
                System.exit(0);
            }
        } catch (ParameterException ex) {
            commander = new JCommander(cli);
            commander.setProgramName("jvm_mechanic - Bootstrap CLI");

            StringBuilder sb = new StringBuilder();
            commander.usage(sb);
            System.out.println(sb.toString().replace("Options:", "Options:\n")); //prettier usage output
            ex.printStackTrace();
            System.exit(-1);
        }

        if (cli.sourcePackageList == null || cli.sourcePackageList.isEmpty()) {
            //invalid
            System.out.println("Missing source code packages! Use -help to view valid arguments.");
            System.exit(-1);
        } else if ((cli.sourceDirectoryList == null || cli.sourceDirectoryList.isEmpty())
                && (cli.scanDirectoryList == null || cli.scanDirectoryList.isEmpty())) {
            //invalid
            System.out.println("Missing source code directories or scan directories! Use -help to view valid arguments.");
            System.exit(-1);
        }

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new JreTypeSolver());

        //add java libraries (.jar)
        if (cli.projectLibraryList != null && !cli.projectLibraryList.isEmpty()) {
            for (String libraryLocation : cli.projectLibraryList) {
                if (new File(libraryLocation).exists()) {
                    typeSolver.add(new JarTypeSolver(libraryLocation));
                    System.out.println("Added Java library: " + libraryLocation);
                } else {
                    System.err.println("Could not find Java library: " + libraryLocation);
                }
            }
        }

        //setup source directories directly
        if (cli.sourceDirectoryList != null && !cli.sourceDirectoryList.isEmpty()) {
            List<String> tempRemoveList = new ArrayList<>();
            List<String> tempAddList = new ArrayList<>();
            for (String sourceDirectory : cli.sourceDirectoryList) {
                File srcMainJavaDir = new File(sourceDirectory, "src/main/java");
                if (srcMainJavaDir.exists()) {
                    tempRemoveList.add(sourceDirectory);
                    sourceDirectory = srcMainJavaDir.getAbsolutePath();
                    tempAddList.add(sourceDirectory);
                }

                typeSolver.add(new ExtendedJavaParserTypeSolver(new File(sourceDirectory)));
                System.out.println("Added direct source code directory: " + sourceDirectory);
            }
            cli.sourceDirectoryList.removeAll(tempRemoveList);
            cli.sourceDirectoryList.addAll(tempAddList);
        }

        //scan for source directories
        if (cli.scanDirectoryList != null && !cli.scanDirectoryList.isEmpty()) {
            for (String scanDirectory : cli.scanDirectoryList) {
                List<File> queue = new ArrayList<>();
                findSourceDirectories(new File(scanDirectory), queue);
                if (!queue.isEmpty() && cli.sourceDirectoryList == null) {
                    cli.sourceDirectoryList = new ArrayList<>();
                }
                for (File file : queue) {
                    cli.sourceDirectoryList.add(file.getAbsolutePath());
                    typeSolver.add(new ExtendedJavaParserTypeSolver(new File(file.getAbsolutePath())));
                    System.out.println("Added scanned source code directory: " + file.getAbsolutePath());
                }
            }
        }

        if (cli.excludeFunctionList == null) {
            cli.excludeFunctionList = new ArrayList<>();
        }

        //explore methods recursively
        System.out.println("\nExploring target function method hierarchy...");
        RecursiveMethodExplorer explorer = new RecursiveMethodExplorer(
                new HashSet<>(cli.sourcePackageList),
                new HashSet<>(cli.sourceDirectoryList),
                new HashSet<>(cli.targetFunctionList),
                new HashSet<>(cli.excludeFunctionList));
        explorer.explore(JavaParserFacade.get(typeSolver));
        System.out.println("Finished exploring target function method hierarchy!");

        //don't get how output is getting out of order but here's a hack :/
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        //bad output
        Set<String> failedFunctionSet = explorer.getFailedFunctionSet();
        if (!failedFunctionSet.isEmpty()) {
            System.err.println("\nFailed to create injection rules for methods:");
            for(String failedFunction : failedFunctionSet) {
                System.err.println(failedFunction);
            }
        }

        //don't get how output is getting out of order but here's a hack :/
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        Set<String> failedConstructorSet = explorer.getFailedConstructorSet();
        if (!failedConstructorSet.isEmpty()) {
            System.err.println("\nFailed to create injection rules for constructor(s) of classes:");
            for(String failedConstructor : failedConstructorSet) {
                System.err.println(failedConstructor);
            }
        }

        //don't get how output is getting out of order but here's a hack :/
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        //good output
        Set<String> visitedFunctionSet = explorer.getVisitedFunctionSet();
        if (!visitedFunctionSet.isEmpty()) {
            System.out.println("\nMethods successfully found to inject:");
            for(String visitedFunction : visitedFunctionSet) {
                System.out.println(visitedFunction);
            }
        }

        //don't get how output is getting out of order but here's a hack :/
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        Set<String> visitedConstructorSet = explorer.getVisitedConstructorSet();
        if (!visitedConstructorSet.isEmpty()) {
            System.out.println("\nClass constructor(s) successfully found to inject:");
            for(String visitedConstructor : visitedConstructorSet) {
                System.out.println(visitedConstructor);
            }
        }

        //don't get how output is getting out of order but here's a hack :/
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        //output rules
        System.out.println("\njvm_mechanic Generated Injection Rules: ");
        MechanicRuleGenerator ruleGenerator = new MechanicRuleGenerator(explorer.getVisitedFunctionSet(), explorer.getVisitedConstructorSet());
        System.out.println(ruleGenerator.getGeneratedRules().toString());
    }

    static List<File> findSourceDirectories(File searchDirectory, List<File> queue) {
        //BFS recursive search for all src/main/java directories
        if (searchDirectory.isDirectory()) {
            File srcMainJavaDir = new File(searchDirectory, "src/main/java");
            if (srcMainJavaDir.exists()) {
                queue.add(srcMainJavaDir);
            } else {
                File[] fileArr = searchDirectory.listFiles(File::isDirectory);
                if (fileArr != null) {
                    for (File childFile : fileArr) {
                        findSourceDirectories(childFile, queue);
                    }
                }
            }
        }
        return queue;
    }

}
