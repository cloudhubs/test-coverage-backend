package com.groupfour.testcoveragetool.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public List<String> getEndPoints(@RequestParam("file") MultipartFile mFile) throws IOException {
        List<String> endpoints = new ArrayList<>();
        File file = File.createTempFile("temp-", mFile.getOriginalFilename());
        mFile.transferTo(file);
        String basePath, s;
        int i;

        BufferedReader br = new BufferedReader(new FileReader(file));

        while ((s = br.readLine()) != null && !s.contains(BASE_PATH));
        basePath = s.substring(BASE_PATH.length(), s.length() - 2);
        while ((s = br.readLine()) != null && !PATHS.equals(s));

        int braceCount = 0;
        while ((i = br.read()) != -1) {
            //System.out.print((char)i);
            if (i == (int)'{') {
                braceCount++;
            } else if (i == (int)'}') {
                braceCount--;
            } else if (i == (int)'/' && braceCount == 0) {
                String current = basePath;
                while ((i = br.read()) != (int)'\"') {
                    current += (char)i;
                }
                braceCount++;
                br.readLine();
                br.read();

                String type = "";
                while ((i = br.read()) != -1 && i != (int)'\"') {
                    type += (char)i;
                }

                type = type.toUpperCase();
                if (!TYPES.contains(type)) {
                    type = "UNDEFINED";
                }
                type += " ";

                endpoints.add(type + current);
            }
        }

        return endpoints;
    }
}