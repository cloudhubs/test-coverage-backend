package com.groupfour.testcoveragetool.controller;

import java.util.Objects;

public class EndpointInfo {

    private String method;
    private String path;

    public EndpointInfo(String method, String path) {
        this.method = method;
        this.path = path;
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

    public void setPath(String path) {
        this.path = path;
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
