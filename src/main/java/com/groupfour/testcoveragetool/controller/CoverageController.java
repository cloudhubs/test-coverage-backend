package com.groupfour.testcoveragetool.controller;

import com.groupfour.testcoveragetool.group.gatling.GatlingEndpointEnumerator;
import com.groupfour.testcoveragetool.group.selenium.SeleniumEndpointEnumerator;
import com.groupfour.testcoveragetool.group.swagger.SwaggerEndpointEnumerator;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
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

    public static int GATLINGCOVERAGE = 0;
    public static int SELENIUMCOVERAGE = 0;
    public static int PARTIALCOVERAGE = 0;
    public static int TOTALCOVERAGE = 0;

    @PostMapping("/getCoverage")
    public String getCoverage(@RequestParam("file") MultipartFile file,
                              @RequestParam("file2") MultipartFile file2,
                              @RequestParam("file3") MultipartFile file3) throws IOException, ZipException {

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
        Set<EndpointInfo> noDupesSelenium = new LinkedHashSet<>(seleniumList);
        Set<EndpointInfo> noDupesGatling = new LinkedHashSet<>(gatlingList);
        Set<EndpointInfo> noDupesSwagger = new LinkedHashSet<>(swaggerList);

        ArrayList<EndpointInfo> finalSeleniumList = new ArrayList<>(noDupesSelenium);
        ArrayList<EndpointInfo> finalGatlingList = new ArrayList<>(noDupesGatling);
        ArrayList<EndpointInfo> finalSwaggerList = new ArrayList<>(noDupesSwagger);
        
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
        //if in one and not the other than increment counter\
        //can use global variable for counter to make no coverage easier
        GATLINGCOVERAGE = 0;
        SELENIUMCOVERAGE = 0;
        PARTIALCOVERAGE = 0;

        /* check if the item is just in gatling */
        for(EndpointInfo endpoint : gatling) {
            /* if not in selenium, increment */
            if(swagger.contains(endpoint) && !selenium.contains(endpoint)) {
                GATLINGCOVERAGE++;
                PARTIALCOVERAGE++;
            } else if(swagger.contains(endpoint)) {
                GATLINGCOVERAGE++;
            }
        }

        /* check if the item is just in selenium */
        for(EndpointInfo endpoint : selenium) {
            /* if not in gatling, increment */
            if(swagger.contains(endpoint) && !gatling.contains(endpoint)) {
                SELENIUMCOVERAGE++;
                PARTIALCOVERAGE++;
            } else if(swagger.contains(endpoint)) {
                SELENIUMCOVERAGE++;
            }
        }

        /* return partial coverage */
        return PARTIALCOVERAGE;
    }

    @GetMapping("/getTotal")
    public int getTotalCoverage(ArrayList<EndpointInfo> selenium, ArrayList<EndpointInfo> gatling, ArrayList<EndpointInfo> swagger) {
        //if in both than increment counter
        //can use global variable for counter to make no coverage easier
        GATLINGCOVERAGE = 0;
        SELENIUMCOVERAGE = 0;
        TOTALCOVERAGE = 0;

        /* check if the item is in both selenium and gatling list */
        for(EndpointInfo endpointInfo : swagger) {
            if(selenium.contains(endpointInfo) && gatling.contains(endpointInfo)) {
                GATLINGCOVERAGE++;
                SELENIUMCOVERAGE++;
                TOTALCOVERAGE++;
            }
            if(selenium.contains(endpointInfo)) {
                SELENIUMCOVERAGE++;
            }
            if(gatling.contains(endpointInfo)) {
                GATLINGCOVERAGE++;
            }
        }

        return TOTALCOVERAGE;
    }

    @GetMapping("/getNo")
    public int getNoCoverage(ArrayList<EndpointInfo> swagger) {
        //get difference between swagger size and 2 counters
        return swagger.size() - TOTALCOVERAGE - PARTIALCOVERAGE;
    }
}
