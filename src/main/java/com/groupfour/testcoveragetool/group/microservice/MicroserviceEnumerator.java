package com.groupfour.testcoveragetool.group.microservice;

import com.google.common.base.Strings;
import com.groupfour.testcoveragetool.group.selenium.DirectoryTraverser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroserviceEnumerator {

    public static final String PORTS = "ports:";
    public static final int SUBIDX = 2;

    public static Map<String, List<Integer>> listApiAnnotations(File projectFile) throws IOException {
        Map<String, List<Integer>> toReturn = new HashMap<>();

        if(projectFile.isDirectory()) {
            Map<String, List<Integer>> finalToReturn = toReturn;
            new DirectoryTraverser((level, path, file) -> path.endsWith(".scala"), (level, path, file) -> {
                System.out.println(path);
                System.out.println(Strings.repeat("=", path.length()));

                Map<String, List<Integer>> endpointsFound = getInfo(file);
                finalToReturn.putAll(endpointsFound);
            }).explore(projectFile);
            toReturn = finalToReturn;
        }
        else {
            toReturn = getInfo(projectFile);
        }

        return toReturn;
    }

    public static Map<String, List<Integer>> getInfo(File projectFile) throws IOException {
        Map<String, List<Integer>> toReturn = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(projectFile));

        String curr;
        String saveName = "";

        while ((curr = br.readLine()) != null) {
            String noTabs = curr.trim();
            String microserviceName;

            /* begin parsing file */
            /* first get the microservice name */
            if(noTabs.equals("")) {
                String tabName = br.readLine();
                String trimmedName = tabName.trim();
                int idx = trimmedName.indexOf(":");
                if(idx > 0) {
                    microserviceName = trimmedName.substring(0, idx);
                    saveName = microserviceName;
                }
            }

            /* look for port number */
            if(noTabs.contains(PORTS) && !noTabs.contains("#")) {
                String tabPorts;
                List<Integer> ports = new ArrayList<>();
                while((tabPorts = br.readLine()) != null && tabPorts.contains("- ")) {
                    String portLine = tabPorts.trim();
                    int index = portLine.indexOf(":");
                    String portName = portLine.substring(SUBIDX, index);
                    int port = Integer.parseInt(portName);
                    ports.add(port);
                }
                toReturn.put(saveName, ports);
            }
        }
        return toReturn;
    }

    @Deprecated
    public static void main(String[] args) throws IOException {
        File projectDir = new File("./../docker-compose.yml");
        Map<String, List<Integer>> results = listApiAnnotations(projectDir);
        for(String microservice: results.keySet()) {
            String value = results.get(microservice).toString();
            System.out.println(microservice + " with port(s) " + value);
        }
    }
}
