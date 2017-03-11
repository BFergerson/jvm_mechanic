package com.codebrig.jvmmechanic.bootstrap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.codebrig.jvmmechanic.bootstrap.scan.RecursiveMethodExplorer;
import me.tomassetti.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import me.tomassetti.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
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

    @Parameter(names = {"-help", "--help"}, description = "Displays help information")
    public boolean help;

    public static void main(String[] args) {
        BootstrapCLI cli = new BootstrapCLI();
        JCommander commander = null;
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

        //scan for source directories
        if (cli.scanDirectoryList != null && !cli.scanDirectoryList.isEmpty()) {
            for (String scanDirectory : cli.scanDirectoryList) {
                List<File> queue = new ArrayList<>();
                findSourceDirectories(new File(scanDirectory), queue);
                for (File file : queue) {
                    cli.sourceDirectoryList.add(file.getAbsolutePath());
                    typeSolver.add(new JavaParserTypeSolver(new File(file.getAbsolutePath())));
                    System.out.println("Added scanned source code directory: " + file.getAbsolutePath());
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

                typeSolver.add(new JavaParserTypeSolver(new File(sourceDirectory)));
                System.out.println("Added direct source code directory: " + sourceDirectory);
            }
            cli.sourceDirectoryList.removeAll(tempRemoveList);
            cli.sourceDirectoryList.addAll(tempAddList);
        }

        //follow path stuff
        RecursiveMethodExplorer explorer = new RecursiveMethodExplorer(
                new HashSet<>(cli.sourcePackageList), new HashSet<>(cli.sourceDirectoryList));
        explorer.explore();
    }

    static List<File> findSourceDirectories(File searchDirectory, List<File> queue) {
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        //BFS recursive search for all src/main/java directories
        if (searchDirectory.isDirectory()) {
            File srcMainJavaDir = new File(searchDirectory, "src/main/java");
            if (srcMainJavaDir.exists()) {
                queue.add(srcMainJavaDir);
            } else {
                File[] fileArr = searchDirectory.listFiles(filter);
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
