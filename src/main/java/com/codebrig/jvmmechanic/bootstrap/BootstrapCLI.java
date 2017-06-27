package com.codebrig.jvmmechanic.bootstrap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.codebrig.jvmmechanic.bootstrap.rule.MechanicRuleGenerator;
import com.codebrig.jvmmechanic.bootstrap.scan.RecursiveMethodExplorer;
import com.github.javaparser.ParseException;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo: this
 * todo: import static methods support
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class BootstrapCLI {

    @Parameter(names = "-source_package", description = "Package(s) of Java source code to be used for type solving (Default: accept all)")
    private List<String> sourcePackageList;

    @Parameter(names = "-exclude_package", description = "Package(s) of Java source code to be excluded")
    private List<String> excludePackageList;

    @Parameter(names = "-source_directory", description = "Directory/Directories of Java source code to be used for type solving")
    private List<String> sourceDirectoryList;

    @Parameter(names = "-scan_directory", description = "Directory/directories to scan for Java source code to be used for type solving")
    private List<String> scanDirectoryList;

    @Parameter(names = "-target_function", description = "Functions to create rules for")
    private List<String> targetFunctionList;

    @Parameter(names = "-project_library", description = ".jar(s) of Java libraries to be used for type solving")
    private List<String> projectLibraryList;

    @Parameter(names = "-exclude_function", description = "Functions to exclude creating rules for")
    private List<String> excludeFunctionList;

    @Parameter(names = "-enter_function", description = "Functions to use as enter function")
    private List<String> enterFunctionList;

    @Parameter(names = {"-include_getters"}, description = "Whether or not to include monitoring getter methods")
    private boolean includeGetters = false;

    @Parameter(names = {"-include_setters"}, description = "Whether or not to include monitoring setter methods")
    private boolean includeSetters = false;

    @Parameter(names = {"-use_complete_events"}, description = "Whether or not to use complete work events as opposed to begin/end work events")
    private boolean useCompleteEvents = true;

    @Parameter(names = {"-help", "--help"}, description = "Displays help information")
    private boolean help;

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

        if ((cli.sourceDirectoryList == null || cli.sourceDirectoryList.isEmpty())
                && (cli.scanDirectoryList == null || cli.scanDirectoryList.isEmpty())) {
            //invalid
            System.out.println("Missing source code directories or scan directories! Use -help to view valid arguments.");
            System.exit(-1);
        }

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());

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
                File srcTestJavaDir = new File(sourceDirectory, "src/test/java");
                if (srcMainJavaDir.exists()) {
                    tempRemoveList.add(sourceDirectory);
                    sourceDirectory = srcMainJavaDir.getAbsolutePath();
                    tempAddList.add(sourceDirectory);
                }
                if (srcTestJavaDir.exists()) {
                    typeSolver.add(new JavaParserTypeSolver(srcTestJavaDir));
                    System.out.println("Added direct source code directory (testing classes): " + srcTestJavaDir.getAbsolutePath());
                }

                typeSolver.add(new JavaParserTypeSolver(new File(sourceDirectory)));
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
                    typeSolver.add(new JavaParserTypeSolver(new File(file.getAbsolutePath())));
                    System.out.println("Added scanned source code directory: " + file.getAbsolutePath());
                }
            }
        }

        if (cli.sourcePackageList == null) {
            cli.sourcePackageList = new ArrayList<>();
        }
        if (cli.excludePackageList == null) {
            cli.excludePackageList = new ArrayList<>();
        }
        if (cli.sourceDirectoryList == null) {
            cli.sourceDirectoryList = new ArrayList<>();
        }
        if (cli.excludeFunctionList == null) {
            cli.excludeFunctionList = new ArrayList<>();
        }
        if (cli.enterFunctionList == null) {
            cli.enterFunctionList = new ArrayList<>();
        }

        if (cli.targetFunctionList == null) {
            cli.targetFunctionList = new ArrayList<>();
        }

        //explore methods recursively
        System.out.println("\nExploring target function method hierarchy...");
        RecursiveMethodExplorer explorer = new RecursiveMethodExplorer(
                new HashSet<>(cli.sourcePackageList),
                new HashSet<>(cli.excludePackageList),
                new HashSet<>(cli.sourceDirectoryList),
                new HashSet<>(cli.targetFunctionList),
                new HashSet<>(cli.excludeFunctionList),
                cli.includeGetters, cli.includeSetters);
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
            for (String failedFunction : failedFunctionSet) {
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
            for (String failedConstructor : failedConstructorSet) {
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
            for (String visitedFunction : visitedFunctionSet) {
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
            for (String visitedConstructor : visitedConstructorSet) {
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
        MechanicRuleGenerator ruleGenerator = new MechanicRuleGenerator(
                explorer.getVisitedFunctionSet(),
                explorer.getVisitedConstructorSet(),
                new HashSet<>(cli.enterFunctionList),
                cli.useCompleteEvents);
        System.out.println(ruleGenerator.getGeneratedRules().toString());
    }

    private static List<File> findSourceDirectories(File searchDirectory, List<File> queue) {
        //BFS recursive search for all src/main/java directories
        if (searchDirectory.isDirectory()) {
            File srcMainJavaDir = new File(searchDirectory, "src/main/java");
            File srcTestJavaDir = new File(searchDirectory, "src/test/java");
            if (srcTestJavaDir.exists()) {
                queue.add(srcTestJavaDir);
            }
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
