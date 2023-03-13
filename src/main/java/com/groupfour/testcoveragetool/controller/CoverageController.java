package com.groupfour.testcoveragetool.controller;

import com.groupfour.testcoveragetool.group.gatling.GatlingEndpointEnumerator;
import com.groupfour.testcoveragetool.group.selenium.SeleniumEndpointEnumerator;
import com.groupfour.testcoveragetool.group.swagger.SwaggerEndpointEnumerator;
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
@RequestMapping("/tests/coverage")
public class CoverageController {

    @PostMapping("/getCoverage")
    public String getCoverage(@RequestParam("file") MultipartFile file,
                              @RequestParam("file2") MultipartFile file2,
                              @RequestParam("file3") MultipartFile file3) throws IOException {

        File seleniumTempFile = File.createTempFile("covTemp-", file.getOriginalFilename());
        File gatlingTempFile = File.createTempFile("covTemp-", file2.getOriginalFilename());
        File swaggerTempFile = File.createTempFile("covTemp-", file3.getOriginalFilename());
        file.transferTo(seleniumTempFile);
        file2.transferTo(gatlingTempFile);
        file3.transferTo(swaggerTempFile);

        ArrayList<EndpointInfo> seleniumList = new ArrayList<>(SeleniumEndpointEnumerator.listApiAnnotations(seleniumTempFile));
        ArrayList<EndpointInfo> gatlingList = new ArrayList<>(GatlingEndpointEnumerator.listApiAnnotations(gatlingTempFile));
        ArrayList<EndpointInfo> swaggerList = new ArrayList<>(SwaggerEndpointEnumerator.listApiAnnotations(swaggerTempFile));

        //Remove duplicates from lists by changing to set and back to array list

        String toRet = "";
        int totalEndpoints = swaggerList.size();

        for(EndpointInfo e : seleniumList) {
            if(swaggerList.contains(e)) {
                swaggerList.remove(e);
            }
        }

        for(EndpointInfo e : gatlingList) {
            if(swaggerList.contains(e)) {
                swaggerList.remove(e);
            }
        }

        int coveredEndpoints = swaggerList.size();

        return toRet + (coveredEndpoints/totalEndpoints);
    }

    @GetMapping("/getPartial")
    public int getPartialCoverage(ArrayList<EndpointInfo> selenium, ArrayList<EndpointInfo> gatling, ArrayList<EndpointInfo> swagger) {
        //if in one and not the other then increment counter\
        //can use global variable for counter to make no coverage easier

        //rec counter
    }

    @GetMapping("/getTotal")
    public int getTotalCoverage(ArrayList<EndpointInfo> selenium, ArrayList<EndpointInfo> gatling, ArrayList<EndpointInfo> swagger) {
        //if in both and not then increment counter
        //can use global variable for counter to make no coverage easier

        //ret counter
    }

    @GetMapping("/getNo")
    public int getNoCoverage(ArrayList<EndpointInfo> swagger) {
        //get difference between swagger size and 2 counters
    }
}
