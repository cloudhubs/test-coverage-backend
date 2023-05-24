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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import java.net.URLClassLoader;
import java.net.URL;

import net.lingala.zip4j.exception.ZipException;
import org.apache.maven.shared.invoker.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.openqa.selenium.WebDriver;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
//import org.openqa.selenium.chrome.ChromeDriver;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class SeleniumEndpointEnumerator {

    public static final String USER_TESTS = "SelTests";
    public static final int SUBIDX = 2;

    public static  List<String> TESTNAMES;

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

    /*
    public static ArrayList<TimeBounds> seleniumTestRunner(File projectFile) throws IOException, ZipException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {

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
        List<File> nonModuleFiles = new ArrayList<File>();

        //get all of the files not in the Modules directory
        for (File file : testFiles) {
            if(!file.getParentFile().getName().equals("Modules")) {
                testFileNames.add(file.getName());
                nonModuleFiles.add(file);
            }
        }

        //compile dependencies in the modules directory
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] moduleFiles = new File(projectDir, "Modules").list((dir, name) -> name.endsWith(".java"));

        for(String s:moduleFiles) {
            File f = new File(projectDir, "Modules/" + s);
            int result = compiler.run(null, null, null, "-d", projectDir.getAbsolutePath(), f.getAbsolutePath());

            if(result != 0) {
                System.out.println("Compilaiton failed");
            }
        }


        String classpath = projectDir.getAbsolutePath() + "/Modules";

        for(String s:testFileNames) {
            String className = s.substring(projectDir.getAbsolutePath().length() + 1);
            className = className.substring(0, className.length() - ".java".length());
            Class<?> myClass = Class.forName(className);
            Method method = null;
            try {
                method = myClass.getMethod("run");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            method.invoke(null);
        }



        //clean up?
        if (projectDir.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(projectDir);
            toDelete.delete();
        }
        
        


        //This will contain the list of endpoints after the tests are run and the logs are extracted
        //ArrayList<EndpointInfo> toReturn = new ArrayList<>();

        //Selenium WebDriver instance
        //System.setProperty("webdriver.chrome.driver", "./src/main/java/com/groupfour/testcoveragetool/group/selenium/chromedriver.exe");     //I still dont know what this does
//        WebDriver driver = new ChromeDriver();
        //final String baseUrl = "http://google.com";       //I have no clue if we will need this

        //loop through each test and run it
        for (String test : testFileNames) {
            System.out.println("Running test case: " + test);

            //import test and execute its test method
            try {
                Class<?> testClass = Class.forName(testCase.replace(".java", ""));
                testClass.getMethod("test", WebDriver.class, String.class)
                        .invoke(null, driver, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }       //This did not work but I might need




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
//        driver.quit();


        //CoverageController.setSelenium(toReturn);
        //return toReturn;

        return null;
    } */


    public static ArrayList<TimeBounds> seleniumTestRunner(File projectZip) {
        File toDelete = new File(USER_TESTS);
        if (toDelete.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(toDelete);
            toDelete.delete();
        }

        Date start = new Date();
        File unzipped = unzip(projectZip);
        ArrayList<TimeBounds> toReturn = new ArrayList<>();
        runJUnitTests(unzipped.getAbsolutePath());
        Date stop = new Date();

        //clean up?
        if (projectZip.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(projectZip);
            toDelete.delete();
        }

        toReturn.add(new TimeBounds(start, stop));
        return toReturn;
    }

    private static File unzip(File zip) {
        //zip extract
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zip);
            zipFile.extractAll(USER_TESTS);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        File unzipped = new File(USER_TESTS, zip.getName().replace(".zip", ""));

        return unzipped;
    }

    private static void runJUnitTests(String projectPath) {
        System.setProperty("M2_HOME", "C:\\Maven\\apache-maven-3.6.3");
        InvocationRequest request = new DefaultInvocationRequest();

        request.setPomFile(new File(projectPath + "/pom.xml"));
        request.setGoals(Collections.singletonList("test -Dmaven.test.skip=false -B -Dtest=com.example.**"));
//        request.setWaitForCompletion(true);



        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getProperty("M2_HOME")));

        try {
            Date start = new Date();
            System.out.println("Start: " + start.toInstant().toEpochMilli());
            InvocationResult result = invoker.execute(request);
            Date stop = new Date();
            System.out.println("Stop: " + stop.toInstant().toEpochMilli());
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
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

    public static File getJar(File projectFile) throws IOException {
        final File[] jar = {null};
        try (Stream<Path> walkStream = Files.walk(Paths.get(projectFile.getAbsolutePath()))) {
            walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
                if (f.getFileName().toString().endsWith(".jar") && !f.getFileName().toString().startsWith(".")) {
                    jar[0] = f.toFile();
                }
            });
        }

        return jar[0];
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
    public static void main(String[] args) throws IOException, ZipException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        File projectDir = new File("./src/main/java/com/groupfour/testcoveragetool/group/selenium/Selenium_TrainTicket.zip");
        //listClasses(projectDir);
        //seleniumTestRunner(projectDir);
        seleniumTestRunner(projectDir);
    }


}


