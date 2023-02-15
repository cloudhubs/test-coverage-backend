package com.groupfour.testcoveragetool.group.selenium;

import java.io.File;
import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.groupfour.testcoveragetool.controller.EndpointInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class EndpointEnumerator {

    enum ApiType{
        GET,
        REQUEST,
        POST,
        PUT,
        PATCH,
        DELETE,
        UNDEFINED
    }

    public static Set<String> validEndpoints = new HashSet<String>(Arrays.asList("RequestBody",
                                                                                "GetMapping",
                                                                                "PostMapping",
                                                                                "RequestMapping",
                                                                                "PutMapping",
                                                                                "DeleteMapping",
                                                                                "PatchMapping"));

    public static void listClasses(File projectDir) {

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

    public static ArrayList<EndpointInfo> listApiAnnotations(File projectDir) {

        ArrayList<EndpointInfo> toReturn = new ArrayList<>();

        new DirectoryTraverser((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(SingleMemberAnnotationExpr n, Object arg) {
                        super.visit(n, arg);

                        Name candidate = n.getName();

                        if(validEndpoints.contains(candidate.toString())) {
                            toReturn.add(printApiInformation(n));
                        }
                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);

        /* Find me */
        for(int i = 0; i < 10; i++) {
            System.out.print("");
        }

        return toReturn;
    }

    @interface X { int id(); }

    public static EndpointInfo printApiInformation(SingleMemberAnnotationExpr n) {

        ApiType type = ApiType.UNDEFINED;

        //System.out.println(n.getName().toString());
        switch(n.getName().toString()) {
            case "GetMapping":
                type = ApiType.GET;
                break;
            case "RequestBody":
            case "RequestMapping":
                type = ApiType.REQUEST;
                break;
            case "PostMapping":
                type = ApiType.POST;
                break;
            case "PutMapping":
                type = ApiType.PUT;
                break;
            case "DeleteMapping":
                type = ApiType.DELETE;
                break;
            case "PatchMapping":
                type = ApiType.PATCH;
                break;
        }

        String endpointPath = "";

        if(type != ApiType.UNDEFINED) {
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
    public static void main(String[] args) {
        File projectDir = new File("./../../SeleniumSample");
        //listClasses(projectDir);
        listApiAnnotations(projectDir);
    }
}
