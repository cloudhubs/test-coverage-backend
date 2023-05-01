package com.groupfour.testcoveragetool.controller;

import java.util.HashSet;
import java.util.Objects;

public class EndpointInfo {

    private String method;
    private String path;
    private int parameters;

    private static final String GET = "GET ";
    private static final String POST = "POST ";
    private static final String PUT = "POST ";
    private static final String DELETE = "POST ";

    private static final char BRACKET = '{';

    public EndpointInfo(String method, String path) {
        this.method = method;
//        String beg = "";
//        if (!path.startsWith("/")) {
//            beg = "/";
//        }
//        this.path = beg + path;
        setPath(path);
        this.parameters = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == BRACKET) {
                this.parameters++;
            }
        }
    }

    public EndpointInfo(String method, String path, int parameters) {
        this.method = method;
        String beg = "";
//        if (!path.startsWith("/")) {
//            beg = "/";
//        }
        this.path = beg + path;
        this.parameters = parameters;
    }

    public void convertFromString(HashSet<String> eps) {
        
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public String getEndpoint() {
        return this.method + " " + this.path;
    }

    public void setPath(String path) {
        String beg = "";
        if (!path.startsWith("/")) {
            beg = "/";
        }
        this.path = beg + path;
        this.path = this.path.replaceAll("//", "/");
    }

    public int getParameters() {
        return parameters;
    }

    public void setParameters(int parameters) {
        this.parameters = parameters;
    }

    public String subEndpoint() {
        String ret = this.method + " " + this.path;

        int index = ret.indexOf("{");
        if (index != -1) {
            ret = ret.substring(0, index);
        }

        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointInfo that = (EndpointInfo) o;
        return method.equals(that.method) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
