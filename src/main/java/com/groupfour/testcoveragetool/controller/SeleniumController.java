package com.groupfour.testcoveragetool.controller;

import com.groupfour.testcoveragetool.group.selenium.SeleniumEndpointEnumerator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH}, allowedHeaders = "*")
@RestController
@RequestMapping("/tests/selenium")
public class SeleniumController {

    ConcurrentMap<String, Extractable> extractables = new ConcurrentHashMap<>();

    @PostMapping("/getAll")
    public String getAll(@RequestParam("file") MultipartFile file) throws IOException {
    	
    	File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
        file.transferTo(tempFile);
		
        ArrayList<EndpointInfo> list =  new ArrayList<EndpointInfo>(SeleniumEndpointEnumerator.listApiAnnotations(tempFile));
        String toRet = "";
        for(EndpointInfo e:list) {
        	String path =  e.getPath().substring(1, e.getPath().length() - 1);
        	toRet += e.getMethod() + " ";
        	toRet += path + '\n';
        }
        return toRet;
    }


    /*
    @GetMapping("/{id}")
    public Extractable getByID(@PathVariable String id) {
        return extractables.get(id);
    }

    @PostMapping("/")
    public Extractable addExtractable(@RequestBody Extractable extractable) {
        extractables.put(extractable.getId(), extractable);
        return extractable;
    }
    */
}
