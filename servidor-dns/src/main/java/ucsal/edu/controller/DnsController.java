package ucsal.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DnsController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/resolve/{appName}")
    public List<String> resolve(@PathVariable String appName) {
        return discoveryClient.getInstances(appName)
                .stream()
                .map(serviceInstance -> serviceInstance.getHost() + ":" + serviceInstance.getPort())
                .toList();
    }
}
