package ucsal.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/dfs")
public class DfsController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/obterArquivo/{nomeArquivo}")
    public ResponseEntity<?> obterArquivo(@PathVariable String nomeArquivo) {
        List<ServiceInstance> instances = discoveryClient.getInstances("dfs-app-b");
        instances.addAll(discoveryClient.getInstances("dfs-app-c"));

        for (ServiceInstance instance : instances) {
            String dfsNodeUrl = instance.getUri().toString() + "/dfs/obterArquivo/" + nomeArquivo;
            try {
                ResponseEntity<byte[]> response = restTemplate.getForEntity(dfsNodeUrl, byte[].class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    return response;
                }
            } catch (Exception e) {
                continue;
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado em nenhum nó");
    }

    @PostMapping("/salvarArquivo/{nomeArquivo}")
    public ResponseEntity<?> salvarArquivo(@PathVariable String nomeArquivo, @RequestParam("arquivo") MultipartFile arquivo) {
        List<ServiceInstance> instances = discoveryClient.getInstances("dfs-app-b");
        instances.addAll(discoveryClient.getInstances("dfs-app-c"));

        if (instances.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum nó disponível para salvar o arquivo");
        }

        ServiceInstance selectedInstance = instances.get(new Random().nextInt(instances.size()));
        String dfsNodeUrl = selectedInstance.getUri().toString() + "/dfs/salvarArquivo/" + nomeArquivo;

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("arquivo", new org.springframework.core.io.ByteArrayResource(arquivo.getBytes()) {
                @Override
                public String getFilename() {
                    return arquivo.getOriginalFilename();
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(dfsNodeUrl, requestEntity, String.class);
            return response;
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao ler o arquivo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar requisição");
        }
    }
}
