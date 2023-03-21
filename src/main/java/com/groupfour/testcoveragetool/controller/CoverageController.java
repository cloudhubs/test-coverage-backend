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
import java.util.List;
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

    private static ArrayList<String> fullSwagger;
    private static ArrayList<String> partialSwagger;
    private static ArrayList<String> noSwagger;
    private static ArrayList<String> fullGatling;
    private static ArrayList<String> noGatling;
    private static ArrayList<String> noSelenium;
    private static ArrayList<String> fullSelenium;

    private static boolean totalDone = false;
    private static boolean partialDone = false;
    private static boolean gatlingCoveredWaiting = true;
    private static boolean gatlingUncoveredWaiting = true;
    private static boolean seleniumUncoveredWaiting = true;
    private static boolean seleniumCoveredWaiting = true;

    private boolean testing = false;

    public static void setSwagger(ArrayList<EndpointInfo> swagger) {
        CoverageController.swagger = swagger;
    }

    public static void setGatling(ArrayList<EndpointInfo> gatling) {
        CoverageController.gatling = gatling;
        System.err.println("Gatling reset");
        GATLINGCOVERAGE = 0;
    }

    @GetMapping("/getGatlingCovered")
    public int getGatlingCovered() {
        while (gatlingCoveredWaiting);

        gatlingCoveredWaiting = true;

        System.err.println("Gatling covered: " + GATLINGCOVERAGE);
        return GATLINGCOVERAGE;
    }

    @GetMapping("/getGatlingUncovered")
    public int getGatlingUncovered() {
        while (gatlingUncoveredWaiting);

        gatlingUncoveredWaiting = true;

        System.err.println("Gatling uncovered");
        return swagger.size() - GATLINGCOVERAGE;
    }

    public static void setSelenium(ArrayList<EndpointInfo> selenium) {
        CoverageController.selenium = selenium;
        SELENIUMCOVERAGE = 0;
    }

    @GetMapping("/getSeleniumCovered")
    public int getSeleniumCovered() {
        while (seleniumCoveredWaiting);

        System.err.println("Selenium covered");
        seleniumCoveredWaiting = true;

        return SELENIUMCOVERAGE;
    }

    @GetMapping("/getSeleniumUncovered")
    public int getSeleniumUncovered() {
        while (seleniumUncoveredWaiting);

        System.err.println("Selenium uncovered");
        seleniumUncoveredWaiting = true;

        return swagger.size() - SELENIUMCOVERAGE;
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
        ArrayList<EndpointInfo> swaggerList = new ArrayList<>();

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
    public int getPartialCoverage() {
        //if in one and not the other than increment counter\
        //can use global variable for counter to make no coverage easier
        PARTIALCOVERAGE = 0;
        GATLINGCOVERAGE = 0;
        SELENIUMCOVERAGE = 0;
        if (testing) {
            if (gatling == null) {
                gatling = new ArrayList<>();
            }
            if (selenium == null) {
                selenium = new ArrayList<>();
            }
            gatling.add(swagger.get(5));
            gatling.add(swagger.get(6));
            gatling.add(swagger.get(7));
            gatling.add(swagger.get(11));
            selenium.add(swagger.get(8));
            selenium.add(swagger.get(9));
            selenium.add(swagger.get(10));
            selenium.add(swagger.get(12));
        }

        List<String> gatlingStr = new ArrayList<>();
        List<String> swaggerStr = new ArrayList<>();
        List<String> seleniumStr = new ArrayList<>();
        if (gatling != null) {
            for (EndpointInfo current : gatling) {
                gatlingStr.add(current.getMethod() + " " + current.getPath());
            }
        }
        if (swagger != null) {
            for (EndpointInfo current : swagger) {
                swaggerStr.add(current.getMethod() + " " + current.getPath());
            }
        }
        if (selenium != null) {
            for (EndpointInfo current : selenium) {
                seleniumStr.add(current.getMethod() + " " + current.getPath());
            }
        }

        noGatling = new ArrayList<>();
        noSelenium = new ArrayList<>();
        noSwagger = new ArrayList<>();
        fullGatling = new ArrayList<>();
        fullSelenium = new ArrayList<>();
        fullSelenium = new ArrayList<>();
        partialSwagger = new ArrayList<>();
        noGatling.addAll(gatlingStr);
        noSelenium.addAll(seleniumStr);
        noSwagger.addAll(swaggerStr);

        /* check if the item is just in gatling */
        //gatling.add(new EndpointInfo(swagger.get(0).getMethod(), swagger.get(0).getPath()));
        if (gatlingStr != null) {
            for (String current : gatlingStr) {
                /* if not in selenium, increment */
                if (swaggerStr != null && swaggerStr.contains(current) && seleniumStr != null && !seleniumStr.contains(current)) {
                    GATLINGCOVERAGE++;
                    fullGatling.add(current);
                    noGatling.remove(current);
                    PARTIALCOVERAGE++;
                    partialSwagger.add(current);
                    noSwagger.remove(current);
                } else if (swagger != null && swaggerStr.contains(current)) {
                    GATLINGCOVERAGE++;
                    fullGatling.add(current);
                    noGatling.remove(current);
                    fullSwagger.add(current);
                    noSwagger.remove(current);
                }
            }
        }

        /* check if the item is just in selenium */
        if (seleniumStr != null) {
            for (String endpoint : seleniumStr) {
                /* if not in gatling, increment */
                if (swaggerStr != null && swaggerStr.contains(endpoint) && gatlingStr != null && !gatlingStr.contains(endpoint)) {
                    SELENIUMCOVERAGE++;
                    fullSelenium.add(endpoint);
                    noSelenium.remove(endpoint);
                    PARTIALCOVERAGE++;
                    partialSwagger.add(endpoint);
                    noSwagger.remove(endpoint);
                } else if (swaggerStr != null && swaggerStr.contains(endpoint)) {
                    SELENIUMCOVERAGE++;
                    fullSelenium.add(endpoint);
                    noSelenium.remove(endpoint);
                }
            }
        }

        partialDone = true;
        /* return partial coverage */
        return PARTIALCOVERAGE;
    }

    @GetMapping("/getTotal")
    public int getTotalCoverage() {
        //if in both than increment counter
        //can use global variable for counter to make no coverage easier
        TOTALCOVERAGE = 0;
//        GATLINGCOVERAGE = 0;
//        SELENIUMCOVERAGE = 0;

        if (testing) {
            if (gatling == null) {
                gatling = new ArrayList<>();
            }
            if (selenium == null) {
                selenium = new ArrayList<>();
            }
            selenium.add(swagger.get(0));
            selenium.add(swagger.get(1));
            selenium.add(swagger.get(2));
            selenium.add(swagger.get(3));

            gatling.add(swagger.get(0));
            gatling.add(swagger.get(1));
            gatling.add(swagger.get(2));
            gatling.add(swagger.get(3));
        }

        List<String> gatlingStr = new ArrayList<>();
        List<String> swaggerStr = new ArrayList<>();
        List<String> seleniumStr = new ArrayList<>();
        if (gatling != null) {
            for (EndpointInfo current : gatling) {
                gatlingStr.add(current.getMethod() + current.getPath());
            }
        }
        if (swagger != null) {
            for (EndpointInfo current : swagger) {
                swaggerStr.add(current.getMethod() + current.getPath());
            }
        }
        if (selenium != null) {
            for (EndpointInfo current : selenium) {
                seleniumStr.add(current.getMethod() + current.getPath());
            }
        }

        /* check if the item is in both selenium and gatling list */
        if (swaggerStr != null) {
            for (String endpointInfo : swaggerStr) {
                if (seleniumStr != null && seleniumStr.contains(endpointInfo) && gatlingStr != null && gatlingStr.contains(endpointInfo)) {
//                    GATLINGCOVERAGE++;
//                    SELENIUMCOVERAGE++;
                    TOTALCOVERAGE++;
                } else if (seleniumStr != null && seleniumStr.contains(endpointInfo)){
//                    SELENIUMCOVERAGE++;
                } else if (gatlingStr != null && gatlingStr.contains(endpointInfo)){
//                    GATLINGCOVERAGE++;
                }
            }
        }

        totalDone = true;
        return TOTALCOVERAGE;
    }

    @GetMapping("/getNo")
    public int getNoCoverage() {
        //get difference between swagger size and 2 counters
        while (!totalDone || !partialDone);

        seleniumCoveredWaiting = false;
        seleniumUncoveredWaiting = false;
        gatlingUncoveredWaiting = false;
        gatlingCoveredWaiting = false;
        totalDone = false;
        partialDone = false;

        return swagger.size() - TOTALCOVERAGE - PARTIALCOVERAGE;
    }

    @GetMapping("/getFullSwagger")
    public static ArrayList<String> getFullSwagger() {
        return fullSwagger;
    }

    @GetMapping("/getPartialSwagger")
    public static ArrayList<String> getPartialSwagger() {
        return partialSwagger;
    }

    @GetMapping("/getNoSwagger")
    public static ArrayList<String> getNoSwagger() {
        return noSwagger;
    }

    @GetMapping("/getFullGatling")
    public static ArrayList<String> getFullGatling() {
        return fullGatling;
    }

    @GetMapping("/getNoGatling")
    public static ArrayList<String> getNoGatling() {
        return noGatling;
    }

    @GetMapping("/getNoSelenium")
    public static ArrayList<String> getNoSelenium() {
        return noSelenium;
    }

    @GetMapping("/getFullSelenium")
    public static ArrayList<String> getFullSelenium() {
        return fullSelenium;
    }
}
