package ucsal.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/perfil")
public class IspController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/obterArquivo/{nomeArquivo}")
    public ResponseEntity<?> obterArquivo(@PathVariable String nomeArquivo) {
        List<ServiceInstance> instances = discoveryClient.getInstances("profile-app");
        if (instances.isEmpty()) {
            return ResponseEntity.status(404).body("Serviço Profile não encontrado");
        }

        ServiceInstance serviceInstance = instances.get(0);
        String profileAppUrl = serviceInstance.getUri().toString() + "/profile/obterArquivo/" + nomeArquivo;
        return restTemplate.getForEntity(profileAppUrl, byte[].class);
    }

    @PostMapping("/salvarArquivo/{nomeArquivo}")
    public ResponseEntity<?> salvarArquivo(@PathVariable String nomeArquivo, @RequestParam("arquivo") MultipartFile arquivo) {
        List<ServiceInstance> instances = discoveryClient.getInstances("profile-app");
        if (instances.isEmpty()) {
            return ResponseEntity.status(404).body("Serviço Profile não encontrado");
        }

        ServiceInstance serviceInstance = instances.get(0);
        String profileAppUrl = serviceInstance.getUri().toString() + "/profile/salvarArquivo/" + nomeArquivo;

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("arquivo", new org.springframework.core.io.ByteArrayResource(arquivo.getBytes()) {
                @Override
                public String getFilename() {
                    return arquivo.getOriginalFilename();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(profileAppUrl, requestEntity, String.class);
    }
}
