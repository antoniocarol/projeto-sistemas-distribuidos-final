package ucsal.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ucsal.edu.config.CustomWebSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/dfs")
public class DfsController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CustomWebSocketHandler webSocketHandler;

    private final List<String> dfsNodes = Arrays.asList(
            "http://localhost:8083/dfs",
            "http://localhost:8084/dfs"
    );

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/obterArquivo/{nomeArquivo}")
    public ResponseEntity<?> obterArquivo(@PathVariable String nomeArquivo) {
        for (String node : dfsNodes) {
            String dfsNodeUrl = node + "/obterArquivo/" + nomeArquivo;
            try {
                ResponseEntity<byte[]> response = restTemplate.getForEntity(dfsNodeUrl, byte[].class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Origin-Server", node);
                    return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
                }
            } catch (Exception e) {
                // Continua pro próximo nó
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado em nenhum nó");
    }

    @PostMapping("/salvarArquivo/{nomeArquivo}")
    public ResponseEntity<?> salvarArquivo(@PathVariable String nomeArquivo, @RequestParam("arquivo") MultipartFile arquivo) {
        if (dfsNodes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum nó disponível para salvar o arquivo");
        }

        String selectedNode = dfsNodes.get(new Random().nextInt(dfsNodes.size()));
        String dfsNodeUrl = selectedNode + "/salvarArquivo/" + nomeArquivo;

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
            if (response.getStatusCode() == HttpStatus.OK) {
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.add("X-Saved-To-Server", selectedNode);
                // Notificar via WebSocket
                for (WebSocketSession session : webSocketHandler.getSessions()) {
                    session.sendMessage(new TextMessage("Arquivo salvo em: " + selectedNode));
                }
                return new ResponseEntity<>(response.getBody(), responseHeaders, HttpStatus.OK);
            } else {
                return response;
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao ler o arquivo");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao enviar requisição");
        }
    }
}
