package com.groupfour.testcoveragetool.group.gatling;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.groupfour.testcoveragetool.controller.EndpointInfo;
import com.groupfour.testcoveragetool.group.APIType;
import com.groupfour.testcoveragetool.group.selenium.DirectoryTraverser;
import com.groupfour.testcoveragetool.group.swagger.SwaggerEndpointEnumerator;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.*;
import java.util.*;

public class GatlingEndpointEnumerator {
    public static List<String> validEndpoints = new ArrayList<>() {
        {
            add(".get(\"");
            add(".post(\"");
            add(".put(\"");
            add(".httpRequest(\"");
            add(".patch(\"");
            add(".delete(\"");
        }};

    public static final String BASEURL = ".baseUrl(\"";
    public static final String USER_TESTS = "UserTests";

    public static final int SUBIDX = 2;
    public static final int GETPUTIDX = 6;
    public static final int POSTIDX = 7;
    public static final int REQUESTIDX = 14;
    public static final int PATCHIDX = 8;
    public static final int DELETEIDX = 9;

    public static void listClasses(File projectDir) throws IOException {

        JavaParser x = new JavaParser();

        new DirectoryTraverser((level, path, file) -> path.endsWith(".scala"), (level, path, file) -> {
            //System.out.println(path);
            //System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                        super.visit(n, arg);
                        //System.out.println(" * " + n.getName());

                    }
                }.visit(StaticJavaParser.parse(file), null);
                //System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }

    public static ArrayList<EndpointInfo> listApiAnnotations(File projectFile) throws IOException, ZipException {
        File toDelete = new File(USER_TESTS);
        if (toDelete.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(toDelete);
            toDelete.delete();
        }

        ZipFile zipFile = new ZipFile(projectFile);

        zipFile.extractAll(USER_TESTS);

        File projectDir = new File(USER_TESTS);

        ArrayList<EndpointInfo> toReturn = new ArrayList<>();
        toReturn = getInfo(projectDir);

        /*
            if(projectDir.isDirectory()) {
            ArrayList<EndpointInfo> finalToReturn = toReturn;
            new DirectoryTraverser((level, path, file) -> path.endsWith(".scala"), (level, path, file) -> {
                System.out.println(path);
                //System.out.println(Strings.repeat("=", path.length()));

                ArrayList<EndpointInfo> endpointsFound = getInfo(file);
                finalToReturn.addAll(endpointsFound);
            }).explore(projectDir);
            toReturn = finalToReturn;
        }
        else {
            toReturn = getInfo(projectDir);
        }
         */

        if (projectDir.isDirectory()) {
            SwaggerEndpointEnumerator.deleteDirectory(projectDir);
            toDelete.delete();
        }

        return toReturn;
    }

    @interface X { int id(); }
    
    public static ArrayList<EndpointInfo> getInfo(File projectFile) throws IOException {
        File root = new File( projectFile.getName() );
        File[] list = root.listFiles();

        ArrayList<EndpointInfo> toReturn = new ArrayList<EndpointInfo>();

        String basePath, s;

        if (list == null) return null;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                getInfo(new File(f.getAbsolutePath()));
            } else if (f.getName().endsWith(".scala")) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                while((s = br.readLine()) != null && !s.contains(BASEURL));
                basePath = s.substring(BASEURL.length() + SUBIDX, s.length() - SUBIDX);
                EndpointInfo baseEnd = new EndpointInfo("BASEURL", basePath);
                toReturn.add(printApiInformation(baseEnd));

                String curr;

                while ((curr = br.readLine()) != null) {
                    EndpointInfo endpoint = new EndpointInfo ("", "");
                    String noTabs = curr.replace("\t", "");
                    /* check for get */
                    if (noTabs.contains(validEndpoints.get(0))) {
                        endpoint.setMethod("GET");
                        endpoint.setPath(noTabs.substring(GETPUTIDX, noTabs.length() - SUBIDX - 1));
                    }

                    /* check for post */
                    else if (noTabs.contains(validEndpoints.get(1))) {
                        endpoint.setMethod("POST");
                        endpoint.setPath(noTabs.substring(POSTIDX, noTabs.length() - SUBIDX));
                    }

                    /* check for put */
                    else if (noTabs.contains(validEndpoints.get(2))) {
                        endpoint.setMethod("PUT");
                        endpoint.setPath(noTabs.substring(GETPUTIDX, noTabs.length() - SUBIDX));
                    }

                    /* check for httpRequest*/
                    else if (noTabs.contains(validEndpoints.get(3))) {
                        endpoint.setMethod("HTTPREQUEST");
                        endpoint.setPath(noTabs.substring(REQUESTIDX, noTabs.length() - SUBIDX));
                    }

                    /* check for patch */
                    else if (noTabs.contains(validEndpoints.get(4))) {
                        endpoint.setMethod("PATCH");
                        endpoint.setPath(noTabs.substring(PATCHIDX, noTabs.length() - SUBIDX));
                    }

                    /* check for delete */
                    else if (noTabs.contains(validEndpoints.get(5))) {
                        endpoint.setMethod("DELETE");
                        endpoint.setPath(noTabs.substring(DELETEIDX, noTabs.length() - SUBIDX));
                    }

                    if(!endpoint.getMethod().equals("") && !endpoint.getPath().equals("")) {
                        toReturn.add(printApiInformation(endpoint));
                    }
                }
            }
        }

        System.out.println(toReturn);
        return toReturn;
    }

    public static EndpointInfo printApiInformation(EndpointInfo e) {

        /* set the API type*/
        APIType type = switch (e.getMethod()) {
            case "BASEURL" -> APIType.BASE;
            case "GET" -> APIType.GET;
            case "POST" -> APIType.POST;
            case "PUT" -> APIType.PUT;
            case "HTTPREQUEST" -> APIType.REQUEST;
            case "PATCH" -> APIType.PATCH;
            case "DELETE" -> APIType.DELETE;
            default -> APIType.UNDEFINED;
        };

        if(type != APIType.UNDEFINED) {
            //System.out.println(type + " API call found through: " + e.getMethod());
            //System.out.println("\t- Call using HTTP Path extension: " + e.getPath() + "\n");
        }
        else {
            //System.out.println("*ISSUE*: " + e.getMethod() + " API call found which is undefined in the scope");
        }

        return new EndpointInfo(type.toString(), e.getPath());
    }

    @Deprecated
    public static void main(String[] args) throws IOException, ZipException {
        File projectDir = new File("./../ComputerDatabase.scala");
        //listClasses(projectDir);
        listApiAnnotations(projectDir);
    }
}
