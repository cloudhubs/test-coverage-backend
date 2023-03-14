package com.groupfour.testcoveragetool.controller;

import com.groupfour.testcoveragetool.group.gatling.GatlingEndpointEnumerator;
import com.groupfour.testcoveragetool.group.selenium.SeleniumEndpointEnumerator;
import com.groupfour.testcoveragetool.group.swagger.SwaggerEndpointEnumerator;
import net.lingala.zip4j.exception.ZipException;
import org.checkerframework.checker.units.qual.A;
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

    private static int GATLINGCOVERAGE = 0;
    private static int SELENIUMCOVERAGE = 0;
    private static int PARTIALCOVERAGE = 0;
    private static int TOTALCOVERAGE = 0;

    private static ArrayList<EndpointInfo> swagger;
    private static ArrayList<EndpointInfo> gatling;
    private static ArrayList<EndpointInfo> selenium;

    private boolean testing = true;

    public static void setSwagger(ArrayList<EndpointInfo> swagger) {
        CoverageController.swagger = swagger;
    }

    public static void setGatling(ArrayList<EndpointInfo> gatling) {
        CoverageController.gatling = gatling;
    }

    public static void setSelenium(ArrayList<EndpointInfo> selenium) {
        CoverageController.selenium = selenium;
    }

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

        System.out.println("size: " + finalSwaggerList.size());
        for (EndpointInfo info : finalSwaggerList) {
            System.out.println(info.getPath());
        }

//        getPartialCoverage(finalSeleniumList, finalGatlingList, finalSwaggerList);
//        getTotalCoverage(finalSeleniumList, finalGatlingList, finalSwaggerList);
//        getNoCoverage(finalSwaggerList);

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
    public int getPartialCoverage() {
        //if in one and not the other than increment counter\
        //can use global variable for counter to make no coverage easier
        GATLINGCOVERAGE = 0;
        SELENIUMCOVERAGE = 0;
        PARTIALCOVERAGE = 0;
        if (testing) {
            if (gatling == null) {
                gatling = new ArrayList<>();
            }
            if (selenium == null) {
                selenium = new ArrayList<>();
            }
            selenium.clear();
            gatling.clear();
            gatling.add(swagger.get(5));
            gatling.add(swagger.get(6));
            gatling.add(swagger.get(7));
            gatling.add(swagger.get(11));
            selenium.add(swagger.get(8));
            selenium.add(swagger.get(9));
            selenium.add(swagger.get(10));
            selenium.add(swagger.get(12));
        }

        /* check if the item is just in gatling */
        if (gatling != null) {
            for (EndpointInfo endpoint : gatling) {
                /* if not in selenium, increment */
                if (swagger != null && swagger.contains(endpoint) && selenium != null && !selenium.contains(endpoint)) {
                    GATLINGCOVERAGE++;
                    PARTIALCOVERAGE++;
                } else if (swagger != null && swagger.contains(endpoint)) {
                    GATLINGCOVERAGE++;
                }
            }
        }

        /* check if the item is just in selenium */
        if (selenium != null) {
            for (EndpointInfo endpoint : selenium) {
                /* if not in gatling, increment */
                if (swagger != null && swagger.contains(endpoint) && gatling != null && !gatling.contains(endpoint)) {
                    SELENIUMCOVERAGE++;
                    PARTIALCOVERAGE++;
                } else if (swagger != null && swagger.contains(endpoint)) {
                    SELENIUMCOVERAGE++;
                }
            }
        }

        /* return partial coverage */
        return PARTIALCOVERAGE;
    }

    @GetMapping("/getTotal")
    public int getTotalCoverage() {
        //if in both than increment counter
        //can use global variable for counter to make no coverage easier
        GATLINGCOVERAGE = 0;
        SELENIUMCOVERAGE = 0;
        TOTALCOVERAGE = 0;

        if (testing) {
            if (gatling == null) {
                gatling = new ArrayList<>();
            }
            if (selenium == null) {
                selenium = new ArrayList<>();
            }
            selenium.clear();
            gatling.clear();
            selenium.add(swagger.get(0));
            selenium.add(swagger.get(1));
            selenium.add(swagger.get(2));
            selenium.add(swagger.get(3));

            gatling.add(swagger.get(0));
            gatling.add(swagger.get(1));
            gatling.add(swagger.get(2));
            gatling.add(swagger.get(3));
        }

        /* check if the item is in both selenium and gatling list */
        if (swagger != null) {
            for (EndpointInfo endpointInfo : swagger) {
                if (selenium != null && selenium.contains(endpointInfo) && gatling != null && gatling.contains(endpointInfo)) {
                    GATLINGCOVERAGE++;
                    SELENIUMCOVERAGE++;
                    TOTALCOVERAGE++;
                }
                if (selenium != null && selenium.contains(endpointInfo)){
                    SELENIUMCOVERAGE++;
                }
                if (gatling != null && gatling.contains(endpointInfo)){
                    GATLINGCOVERAGE++;
                }
            }
        }

        return TOTALCOVERAGE;
    }

    @GetMapping("/getNo")
    public int getNoCoverage() {
        //get difference between swagger size and 2 counters
        return swagger.size() - TOTALCOVERAGE - PARTIALCOVERAGE;
    }
}
