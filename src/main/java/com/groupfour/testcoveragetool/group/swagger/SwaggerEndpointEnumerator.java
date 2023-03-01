package com.groupfour.testcoveragetool.group.swagger;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.groupfour.testcoveragetool.controller.EndpointInfo;
import com.groupfour.testcoveragetool.group.APIType;
import com.groupfour.testcoveragetool.group.selenium.DirectoryTraverser;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SwaggerEndpointEnumerator {

    private static final String USER_CODE = "UserCode";

    private static final Set<String> VALID_ENDPOINTS = new HashSet<String>(Arrays.asList("RequestBody",
            "GetMapping",
            "PostMapping",
            "RequestMapping",
            "PutMapping",
            "DeleteMapping",
            "PatchMapping"));

    public static ArrayList<EndpointInfo> listApiAnnotations(File projectFile) throws IOException, ZipException {
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
            //System.out.println(path);
            //System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(SingleMemberAnnotationExpr n, Object arg) {
                        super.visit(n, arg);

                        Name candidate = n.getName();

                        if(VALID_ENDPOINTS.contains(candidate.toString())) {
                            toReturn.add(getCurrentEndpoint(n));
                        }
                    }
                }.visit(StaticJavaParser.parse(file), null);
                //System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);

        if (projectDir.isDirectory()) {
            deleteDirectory(toDelete);
            projectDir.delete();
        }

        // Donny Needs to Implement to get endpoints from file and return list of endpoints

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

    public static EndpointInfo getCurrentEndpoint(SingleMemberAnnotationExpr n) {

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

        //System.out.println(type + " API call found through: " + n.getName());
        String endpointPath = n.toString().substring(n.toString().indexOf("(") + 1, n.toString().length() - 1);
        //System.out.println(type + " " + endpointPath.replace("\"", ""));
        //System.out.println("\t- Call using HTTP Path extension: " + endpointPath + "\n");

        return new EndpointInfo(type.toString(), endpointPath.replace("\"", ""));
    }
}
