package ucsal.edu.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/obterArquivo/{nomeArquivo}")
    public ResponseEntity<?> obterArquivo(@PathVariable String nomeArquivo) {
        String dfsAppUrl = "http://localhost:8082/dfs/obterArquivo/" + nomeArquivo;
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(dfsAppUrl, byte[].class);
            return response;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao obter arquivo");
        }
    }

    @PostMapping("/salvarArquivo/{nomeArquivo}")
    public ResponseEntity<?> salvarArquivo(@PathVariable String nomeArquivo, @RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo não encontrado na requisição");
        }

        String dfsAppUrl = "http://localhost:8082/dfs/salvarArquivo/" + nomeArquivo;

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

            ResponseEntity<String> response = restTemplate.postForEntity(dfsAppUrl, requestEntity, String.class);
            return response;
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao ler o arquivo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar requisição");
        }
    }
}
