package group.selenium;

import java.io.File;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.metamodel.AnnotationExprMetaModel;
import com.github.javaparser.metamodel.AnnotationMemberDeclarationMetaModel;
import com.github.javaparser.symbolsolver.javaparsermodel.contexts.AnnotationDeclarationContext;
import com.google.common.base.Strings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EndpointEnumerator {

    /* This might need to change if we are searching for more REST API types */
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

    public static void listMethodCalls(File projectDir) {
        new DirectoryTraverser((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MarkerAnnotationExpr n, Object arg) {
                        super.visit(n, arg);

                        Name candidate = n.getName();

                        if(validEndpoints.contains(candidate.toString())) {
                            System.out.println(n.getName().toString());
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
    }

    @interface X { int id(); }

    @Deprecated
    public static void main(String[] args) {

        /* Find a way to make an interface for this so it is instance independant */
        File projectDir = new File("./../../SwaggerSample");
        //listClasses(projectDir);
        listMethodCalls(projectDir);
    }
}

