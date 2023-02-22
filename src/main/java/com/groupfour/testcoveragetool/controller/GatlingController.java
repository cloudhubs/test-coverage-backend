package com.groupfour.testcoveragetool.controller;

import com.groupfour.testcoveragetool.group.gatling.GatlingEndpointEnumerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET,
                                                            RequestMethod.POST,
                                                            RequestMethod.PUT,
                                                            RequestMethod.DELETE,
                                                            RequestMethod.PATCH},
    allowedHeaders = "*")
@RestController
@RequestMapping("/tests/gatling")
public class GatlingController {

    ConcurrentMap<String, Extractable> extractables = new ConcurrentHashMap<>();

    @PostMapping("/getAll")
    public String getAll(@RequestParam("file") MultipartFile file) throws IOException {

        File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
        file.transferTo(tempFile);

        System.out.println("Got the file");
        ArrayList<EndpointInfo> list = new ArrayList<>(GatlingEndpointEnumerator.listApiAnnotations(tempFile));
        String toRet = "";
        for(EndpointInfo e:list) {
            String path =  e.getPath().substring(1, e.getPath().length() - 1);
            toRet += e.getMethod() + " ";
            toRet += path + '\n';
        }
        return toRet;
    }
}
