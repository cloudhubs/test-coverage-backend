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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;

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

    private static Map<String, List<EndpointInfo>> swaggerMap;
    private static Map<String, List<EndpointInfo>> gatlingMap;
    private static Map<String, List<EndpointInfo>> seleniumMap;

    private static Map<String, List<String>> swaggerMapStrings;
    private static Map<String, List<String>> gatlingMapStrings;
    private static Map<String, List<String>> seleniumMapStrings;

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

    private static boolean fullSwaggerLock = true;
    private static boolean partialSwaggerLock = true;
    private static boolean noSwaggerLock = true;
    private static boolean fullGatlingLock = true;
    private static boolean noGatlingLock = true;
    private static boolean fullSeleniumLock = true;
    private static boolean noSeleniumLock = true;
    private static boolean totalLock = true;

    public static final String FILE_NAME = "node-coverage.json";

    public static void setSwagger(ArrayList<EndpointInfo> swagger) {
        CoverageController.swagger = swagger;
    }

    public static void setSwaggerMap(Map<String, List<EndpointInfo>> swaggerMap) {
        CoverageController.swaggerMap = swaggerMap;
    }

    @GetMapping("/getSwaggerMap")
    public Map<String, List<String>> getSwaggerMap() {
        Map<String, List<String>> newMap = new HashMap<>();

        /* fill the new map */
        for(Map.Entry<String, List<EndpointInfo>> entry : swaggerMap.entrySet()) {
            String key = entry.getKey();
            List<EndpointInfo> originalList = entry.getValue();
            List<String> newList = new ArrayList<>();

            for (EndpointInfo obj : originalList) {
                newList.add(obj.toString());
            }

            newMap.put(key, newList);
        }

        /* parse through the map and get rid of empty lists */
        newMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        return newMap;
    }


    public static void setGatling(ArrayList<EndpointInfo> gatling) {
        CoverageController.gatling = gatling;
        GATLINGCOVERAGE = 0;
    }

    @GetMapping("/getGatlingCovered")
    public int getGatlingCovered() {
        while (gatlingCoveredWaiting);

        gatlingCoveredWaiting = true;

        return GATLINGCOVERAGE;
    }

    @GetMapping("/getGatlingUncovered")
    public int getGatlingUncovered() {
        while (gatlingUncoveredWaiting);

        gatlingUncoveredWaiting = true;

        return swagger.size() - GATLINGCOVERAGE;
    }

    public static void setSelenium(ArrayList<EndpointInfo> selenium) {
        CoverageController.selenium = selenium;
        SELENIUMCOVERAGE = 0;
    }

    @GetMapping("/getSeleniumCovered")
    public int getSeleniumCovered() {
        while (seleniumCoveredWaiting);

        seleniumCoveredWaiting = true;

        return SELENIUMCOVERAGE;
    }

    @GetMapping("/getSeleniumUncovered")
    public int getSeleniumUncovered() {
        while (seleniumUncoveredWaiting);

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

        ArrayList<EndpointInfo> seleniumList = null;//new ArrayList<>(SeleniumEndpointEnumerator.listApiAnnotations(seleniumTempFile));
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
        fullSwagger = new ArrayList<>();
        partialSwagger = new ArrayList<>();
        noGatling.addAll(swaggerStr);
        noSelenium.addAll(swaggerStr);
        noSwagger.addAll(swaggerStr);

        for (EndpointInfo current : swagger) {
            String endpoint = current.getEndpoint();

            if (current.getParameters() == 0) {
                if (gatlingStr.contains(endpoint) && !seleniumStr.contains(endpoint)) {
                    GATLINGCOVERAGE++;
                    fullGatling.add(endpoint);
                    noGatling.remove(endpoint);
                    PARTIALCOVERAGE++;
                    partialSwagger.add(endpoint);
                    noSwagger.remove(endpoint);
                } else if (gatlingStr.contains(endpoint)) {
                    TOTALCOVERAGE++;
                    GATLINGCOVERAGE++;
                    fullGatling.add(endpoint);
                    noGatling.remove(endpoint);
                    fullSwagger.add(endpoint);
                    noSwagger.remove(endpoint);
                }
            } else {
                for (String g : gatlingStr) {
                    if (g.contains(current.subEndpoint()) && noGatling.contains(endpoint)) {
                        boolean sel = false;

                        for (String s : seleniumStr) {
                            if (s.contains(current.subEndpoint())) {
                                sel = true;
                            }
                        }

                        GATLINGCOVERAGE++;
                        fullGatling.add(g);
                        noGatling.remove(endpoint);
                        if (sel) {
                            fullSwagger.add(endpoint);
                            TOTALCOVERAGE++;
                        } else {
                            PARTIALCOVERAGE++;
                            partialSwagger.add(endpoint);
                        }
                        noSwagger.remove(endpoint);
                    }
                }
            }
        }

        for (EndpointInfo current : swagger) {
            String endpoint = current.getEndpoint();

            if (current.getParameters() == 0) {
                if (seleniumStr.contains(endpoint) && !noGatling.contains(endpoint)) {
                    SELENIUMCOVERAGE++;
                    fullSelenium.add(endpoint);
                    noSelenium.remove(endpoint);
                    PARTIALCOVERAGE++;
                    partialSwagger.add(endpoint);
                    noSwagger.remove(endpoint);
                } else if (seleniumStr.contains(endpoint)) {
                    SELENIUMCOVERAGE++;
                    fullSelenium.add(endpoint);
                    noSelenium.remove(endpoint);
                    fullSwagger.add(endpoint);
                    noSwagger.remove(endpoint);
                }
            } else {
                for (String s : seleniumStr) {
                    if (s.contains(current.subEndpoint()) && seleniumStr.contains(endpoint)) {
                        boolean gat = false;

                        for (String g : gatlingStr) {
                            if (g.contains(current.subEndpoint())) {
                                gat = true;
                            }
                        }

                        SELENIUMCOVERAGE++;
                        fullSelenium.add(s);
                        noSelenium.remove(endpoint);
                        if (gat) {
                            fullSwagger.add(endpoint);
                        } else {
                            PARTIALCOVERAGE++;
                            partialSwagger.add(endpoint);
                        }
                        noSwagger.remove(endpoint);
                    }
                }
            }
        }

        partialDone = true;
        fullSwaggerLock = false;
        partialSwaggerLock = false;
        noSwaggerLock = false;
        fullGatlingLock = false;
        noGatlingLock = false;
        fullSeleniumLock = false;
        noSeleniumLock = false;
        totalLock = false;
        /* return partial coverage */
        return PARTIALCOVERAGE;
    }

    @GetMapping("/getTotal")
    public int getTotalCoverage() {

        while (totalLock);

        totalLock = true;
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
        while (fullSwaggerLock);

        fullSwaggerLock = true;

        return fullSwagger;
    }

    @GetMapping("/getPartialSwagger")
    public static ArrayList<String> getPartialSwagger() {
        while (partialSwaggerLock);

        partialSwaggerLock = true;

        return partialSwagger;
    }

    @GetMapping("/getNoSwagger")
    public static ArrayList<String> getNoSwagger() {
        while (noSwaggerLock);

        noSwaggerLock = true;

        return noSwagger;
    }

    @GetMapping("/getFullGatling")
    public static ArrayList<String> getFullGatling() {
        while(fullGatlingLock);

        fullGatlingLock = true;

        return fullGatling;
    }

    @GetMapping("/getNoGatling")
    public static ArrayList<String> getNoGatling() {
        while(noGatlingLock);

        noGatlingLock = true;

        return noGatling;
    }

    @GetMapping("/getNoSelenium")
    public static ArrayList<String> getNoSelenium() {
        while(noSeleniumLock);

        noSeleniumLock = true;

        return noSelenium;
    }

    @GetMapping("/getFullSelenium")
    public static ArrayList<String> getFullSelenium() {
        while(fullSeleniumLock);

        fullSeleniumLock = true;

        return fullSelenium;
    }

    @GetMapping("/getJsonCoverage")
    public static String getJsonCoverage() throws JSONException {

        ArrayList<String> totalListing = new ArrayList<String>();

        if (swagger != null) {
            for (EndpointInfo current : swagger) {
                totalListing.add(current.getPath());
            }
        }

        JSONObject coverageJson = new JSONObject();
        JSONArray nodes = new JSONArray();

        //Need to get the results of Donnys Coverage per micro service for the loop rather than the node names

        for(String x : totalListing) {
            JSONObject node = new JSONObject();
            node.put("nodeName", x);
            node.put("coverageAmount", 0);
            nodes.put(node);
        }

        coverageJson.put("nodes", nodes);

        try (FileWriter fileWriter = new FileWriter(FILE_NAME)) {
            fileWriter.write(coverageJson.toString(4));
            fileWriter.flush();
        } catch (IOException e) {
        }

        return coverageJson.toString();
    }

    @GetMapping("/getTestMap")
    public Map<String, List<String>> getTestMap() {
        List<String> endpoints1 = new ArrayList<>();
        List<String> endpoints2 = new ArrayList<>();
        List<String> endpoints3 = new ArrayList<>();

        endpoints1.add("GET /1/1");
        endpoints1.add("POST /post/1");
        endpoints1.add("PUSH /push/1");

        endpoints2.add("DELETE /delete");
        endpoints2.add("POST /post");
        endpoints2.add("PUSH /push");

        endpoints3.add("PATCH /patch");
        endpoints3.add("DELETE /please/work/please");
        endpoints3.add("GET /getter/getter1");

        String one = "FirstMicroservice";
        String two = "SecondMicroservice";
        String three = "ThirdMicroservice";

        Map<String, List<String>> ret = new HashMap<>();

        ret.put(one, endpoints1);
        ret.put(two, endpoints2);
        ret.put(three, endpoints3);

        System.err.println(ret);

        return ret;
    }
}
