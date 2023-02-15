package controller;

import group.selenium.EndpointEnumerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/tests/selenium/")
public class SeleniumController {

    ConcurrentMap<String, Extractable> extractables = new ConcurrentHashMap<>();

    @PostMapping("/")
    public List<EndpointInfo> getAll() {
        return new ArrayList<EndpointInfo>(EndpointEnumerator.listApiAnnotations(new File("./../../SeleniumSample")));
    }


    



    @GetMapping("/{id}")
    public Extractable getByID(@PathVariable String id) {
        return extractables.get(id);
    }

    @PostMapping("/")
    public Extractable addExtractable(@RequestBody Extractable extractable) {
        extractables.put(extractable.getId(), extractable);
        return extractable;
    }
}
