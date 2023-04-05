package com.groupfour.testcoveragetool.group.selenium;

import java.io.*;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.groupfour.testcoveragetool.controller.CoverageController;
import com.groupfour.testcoveragetool.controller.EndpointInfo;
import com.groupfour.testcoveragetool.controller.TimeBounds;
import com.groupfour.testcoveragetool.group.APIType;
import com.groupfour.testcoveragetool.group.swagger.SwaggerEndpointEnumerator;
import net.lingala.zip4j.core.ZipFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import net.lingala.zip4j.exception.ZipException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class SeleniumEndpointEnumerator {

    public static final String USER_TESTS = "UserTests";
    public static final int SUBIDX = 2;

    public static Set<String> validEndpoints = new HashSet<String>(Arrays.asList("GetMapping",
                                                                                "PostMapping",
                                                                                "PutMapping",
                                                                                "PatchMapping",
                                                                                //"RequestMapping",
                                                                                "DeleteMapping"));

    public static void listClasses(File projectDir) throws IOException {

        JavaParser x = new JavaParser();

        new DirectoryTraverser((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                        super.visit(n, arg);
                        System.out.println(" * " + n.getName());

                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }

    public static ArrayList<TimeBounds> seleniumTestRunner(File projectFile) throws IOException, ZipException {

        File toDelete = new File(USER_TESTS);
        if (toDelete.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(toDelete);
            toDelete.delete();
        }

        //zip extract
        ZipFile zipFile = new ZipFile(projectFile);
        zipFile.extractAll(USER_TESTS);
        File projectDir = new File(USER_TESTS);

        //toReturn = getTestNames(projectDir);
        List<File> testFiles = getTestFiles(projectDir);    //Getting the test files that will be run
        List<String> testFileNames = new ArrayList<>();
        for (File file : testFiles) {
            testFileNames.add(file.getName());
        }

        //clean up?
        if (projectDir.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(projectDir);
            toDelete.delete();
        }


        //This will contain the list of endpoints after the tests are run and the logs are extracted
        //ArrayList<EndpointInfo> toReturn = new ArrayList<>();

        //Selenium WebDriver instance
        System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");     //I still dont know what this does
        WebDriver driver = new ChromeDriver();
        //final String baseUrl = "http://google.com";       //I have no clue if we will need this

        //loop through each test and run it
        /*for (String test : testFileNames) {
            System.out.println("Running test case: " + test);

            //import test and execute its test method
            try {
                Class<?> testClass = Class.forName(testCase.replace(".java", ""));
                testClass.getMethod("test", WebDriver.class, String.class)
                        .invoke(null, driver, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/         //This did not work but I might need



        // Get a reference to the Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ArrayList<TimeBounds> toReturn = new ArrayList<>();

        // Loop through the files and compile each one
        for (File file : testFiles) {

            Date start = new Date();

            int compilationResult = compiler.run(null, null, null, file.getPath());
            if (compilationResult == 0) {
                // If compilation succeeds, run the program
                try {
                    Runtime.getRuntime().exec("java " + file.getName().replace(".java", ""));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Compilation failed for file: " + file.getName());
            }

            Date stop = new Date();

            TimeBounds tb = new TimeBounds(start, stop);
            toReturn.add(tb);
        }

        //  NEED TO ADD IN THE LOGGER AND THEN ADD THE ENDPOINTS GATHERED TO toReturn

        // Quit the Selenium WebDriver instance
        driver.quit();


        //CoverageController.setSelenium(toReturn);
        return toReturn;
    }

    public static ArrayList<File> getTestFiles(File projectFile) throws IOException {
        ArrayList<File> toReturn = new ArrayList<File>();

        try (Stream<Path> walkStream = Files.walk(Paths.get(projectFile.getAbsolutePath()))) {
            walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
                if (f.getFileName().toString().endsWith(".java") && !f.getFileName().toString().startsWith(".")) {
                    System.out.println(f.toFile().getAbsolutePath());
                    toReturn.add(f.toFile());
                }
            });
        }

        System.out.println(toReturn);
        return toReturn;
    }

    public static EndpointInfo printApiInformation(MethodDeclaration n) {

        APIType type = APIType.UNDEFINED;

        //System.out.println(n.getName().toString());
        switch(n.getName().toString()) {
            case "GetMapping":
                type = APIType.GET;
                break;
            case "RequestBody":
//            case "RequestMapping":
//                type = APIType.REQUEST;
//                break;
            case "PostMapping":
                type = APIType.POST;
                break;
            case "PutMapping":
                type = APIType.PUT;
                break;
            case "DeleteMapping":
                type = APIType.DELETE;
                break;
            case "PatchMapping":
                type = APIType.PATCH;
                break;
        }

        String endpointPath = "";

        if(type != APIType.UNDEFINED) {
            System.out.println(type + " API call found through: " + n.getName());
            endpointPath = n.toString().substring(n.toString().indexOf("(") + 1, n.toString().length() - 1);
            System.out.println("\t- Call using HTTP Path extension: " + endpointPath + "\n");
        }
        else {
            System.out.println("*ISSUE*: " + n.getName() + " API call found which is undefined in the scope");
        }

        return new EndpointInfo(type.toString(), endpointPath);
    }

    @Deprecated
    public static void main(String[] args) throws IOException, ZipException {
        File projectDir = new File("./../../SeleniumSample");
        //listClasses(projectDir);
        seleniumTestRunner(projectDir);
    }
}
