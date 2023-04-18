package com.groupfour.testcoveragetool.controller;

import com.groupfour.testcoveragetool.group.swagger.SwaggerEndpointEnumerator;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.PATCH},
        allowedHeaders = "*")

@RestController
@RequestMapping("/tests/swagger")
public class SwaggerController {
    private static final String PATHS = "\"paths\": {";
    private static final String BASE_PATH = "\"basePath\": \"";
    private static final List<String> TYPES = new ArrayList<>(){
        {
            add("GET");
            add("POST");
            add("PUT");
            add("DELETE");
            add("PATCH");
        }};

    @PostMapping("/getEndPoints")
    public List<EndpointInfo> getEndPoints(@RequestParam("file") MultipartFile mFile) throws IOException, ZipException {
        List<String> endpoints = new ArrayList<>();
        File file = File.createTempFile("temp-", mFile.getOriginalFilename());
        mFile.transferTo(file);
        String basePath, s;
        int i;

        Map<String, EndpointInfo> baseEndpoints = SwaggerEndpointEnumerator.listApiAnnotations(file);
        ArrayList<EndpointInfo> allEndpoints = new ArrayList<>();
        allEndpoints.addAll(SwaggerEndpointEnumerator.listMultiEndApiAnnotations(baseEndpoints, file));
        CoverageController.setSwagger(allEndpoints);
        Map<String, List<EndpointInfo>> newEndpoints = SwaggerEndpointEnumerator.listMultiEndApiAnnotationsMap(baseEndpoints, file);
        CoverageController.setSwaggerMap(newEndpoints);
        return allEndpoints;
    }
}