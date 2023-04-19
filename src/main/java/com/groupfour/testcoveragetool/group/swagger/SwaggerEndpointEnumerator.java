package com.groupfour.testcoveragetool.group.swagger;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.groupfour.testcoveragetool.controller.CoverageController;
import com.groupfour.testcoveragetool.controller.EndpointInfo;
import com.groupfour.testcoveragetool.group.APIType;
import com.groupfour.testcoveragetool.group.selenium.DirectoryTraverser;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SwaggerEndpointEnumerator {

    private static final String USER_CODE = "UserCode";

    private static final Set<String> VALID_ENDPOINTS = new HashSet<String>(Arrays.asList("RequestBody",
            "GetMapping",
            "PostMapping",
            "RequestMapping",
            "PutMapping",
            "DeleteMapping",
            "PatchMapping"));

    public static Map<String, EndpointInfo> listApiAnnotations(File projectFile) throws IOException, ZipException {
        Map<String, EndpointInfo> toReturn = new HashMap();

        File toDelete = new File(USER_CODE);
        if (toDelete.isDirectory()) {
            deleteDirectory(toDelete);
            toDelete.delete();
        }

        ZipFile zipFile = new ZipFile(projectFile);

        zipFile.extractAll(USER_CODE);

        /* get overarching directory */
        File projectDir = new File(USER_CODE);

        File[] allFiles = projectDir.listFiles();

        new DirectoryTraverser((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(SingleMemberAnnotationExpr n, Object arg) {
                        super.visit(n, arg);

                        Name candidate = n.getName();

                        if(VALID_ENDPOINTS.contains(candidate.toString())) {
//                            toReturn.add(getCurrentEndpoint(n));
                            toReturn.put(file.getName(), getCurrentEndpoint(n));
                        }
                    }
                }.visit(StaticJavaParser.parse(file), null);
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);

        if (projectDir.isDirectory()) {
            deleteDirectory(projectDir);
            projectDir.delete();
        }

        return toReturn;
    }

    public static ArrayList<EndpointInfo> listMultiEndApiAnnotations(Map<String, EndpointInfo> baseEndpoints, File projectFile) throws IOException, ZipException {
        ArrayList<EndpointInfo> toReturn = new ArrayList<>();

        File toDelete = new File(USER_CODE);
        if (toDelete.isDirectory()) {
            deleteDirectory(toDelete);
            toDelete.delete();
        }

        ZipFile zipFile = new ZipFile(projectFile);

        zipFile.extractAll(USER_CODE);

        File projectDir = new File(USER_CODE);

        new DirectoryTraverser((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(NormalAnnotationExpr n, Object arg) {
                        super.visit(n, arg);

                        Name candidate = n.getName();

                        if(VALID_ENDPOINTS.contains(candidate.toString())) {
                            EndpointInfo current = getCurrentEndpoint(n);
                            current.setPath(current.getPath().replace("path = ", ""));
                            current.setPath(current.getPath().replace("value = ", ""));
                            if (baseEndpoints.containsKey(file.getName())) {
                                current.setPath((baseEndpoints.get(file.getName()).getPath() + "/" + current.getPath()).replace("//", "/"));
                            }
                            toReturn.add(current);
                        }
                    }
                }.visit(StaticJavaParser.parse(file), null);
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);

        if (projectDir.isDirectory()) {
            deleteDirectory(projectDir);
            projectDir.delete();
        }

        System.out.println("Endpoints: " + toReturn.size());
        CoverageController.setSwagger(toReturn);
        return toReturn;
    }

    public static Map<String, List<EndpointInfo>> listMultiEndApiAnnotationsMap(Map<String, EndpointInfo> baseEndpoints, File projectFile) throws IOException, ZipException {
        Map<String, List<EndpointInfo>> toReturn = new HashMap<>();

        File toDelete = new File(USER_CODE);
        if (toDelete.isDirectory()) {
            deleteDirectory(toDelete);
            toDelete.delete();
        }

        ZipFile zipFile = new ZipFile(projectFile);

        zipFile.extractAll(USER_CODE);

        /* get overarching directory */
        File projectDir = new File(USER_CODE);
        File[] baseFile = projectDir.listFiles();
        for (File current : baseFile) {
//            System.err.println(current.getName());
        }

        File[] allFiles = baseFile[0].listFiles();
        for (File current : allFiles) {
//            System.err.println(current.getName());
        }
        ArrayList<EndpointInfo> currentList = new ArrayList<>();

        for(File f : allFiles) {
//            System.err.println(f.getName());
            if(f.isDirectory()) {
//                ArrayList<EndpointInfo> currentList = new ArrayList<>();
                currentList.clear();
                new DirectoryTraverser((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
                    try {
                        new VoidVisitorAdapter<Object>() {
                            @Override
                            public void visit(NormalAnnotationExpr n, Object arg) {
                                super.visit(n, arg);

                                Name candidate = n.getName();

                                if (VALID_ENDPOINTS.contains(candidate.toString())) {
                                    EndpointInfo current = getCurrentEndpoint(n);
                                    current.setPath(current.getPath().replace("path = ", ""));
                                    current.setPath(current.getPath().replace("value = ", ""));
                                    if (baseEndpoints.containsKey(file.getName())) {
                                        current.setPath((baseEndpoints.get(file.getName()).getPath() + "/" + current.getPath()).replace("//", "/"));
                                    }
                                    currentList.add(current);
                                }
                            }
                        }.visit(StaticJavaParser.parse(file), null);
                    } catch (IOException e) {
                        new RuntimeException(e);
                    }
                }).explore(projectDir);

                toReturn.put(f.getName(), (new ArrayList<>(currentList)));
            }
        }

        if (projectDir.isDirectory()) {
            deleteDirectory(projectDir);
            projectDir.delete();
        }

        System.out.println("Endpoints: " + toReturn.size());
        CoverageController.setSwaggerMap(toReturn);
        return toReturn;
    }

    public static void deleteDirectory(File file) {
        // store all the paths of files and folders present
        // inside directory
        for (File subfile : file.listFiles()) {

            // if it is a subfolder,e.g Rohan and Ritik,
            //  recursively call function to empty subfolder
            if (subfile.isDirectory()) {
                deleteDirectory(subfile);
            }

            // delete files and empty subfolders
            subfile.delete();
        }
    }

    public static EndpointInfo getCurrentEndpoint(AnnotationExpr n) {

        APIType type = APIType.UNDEFINED;

        //System.out.println(n.getName().toString());
        switch(n.getName().toString()) {
            case "GetMapping":
                type = APIType.GET;
                break;
            case "RequestBody":
            case "RequestMapping":
                type = APIType.REQUEST;
                break;
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

        String endpointPath = n.toString().substring(n.toString().indexOf("(") + 1, n.toString().length() - 1);

        return new EndpointInfo(type.toString(), endpointPath.replace("\"", ""));
    }
}
