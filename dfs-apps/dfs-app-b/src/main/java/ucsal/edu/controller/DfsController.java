package ucsal.edu.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

@RestController
@RequestMapping("/dfs")
public class DfsController {

    private static final Logger LOGGER = Logger.getLogger(DfsController.class.getName());

    private static final String STORAGE_DIR = "/temp/dfs-storage/";

    static {
        File storageDir = new File(STORAGE_DIR);
        if (!storageDir.exists()) {
            if (storageDir.mkdirs()) {
                LOGGER.info("Diretório de armazenamento criado: " + STORAGE_DIR);
            } else {
                LOGGER.severe("Falha ao criar o diretório de armazenamento: " + STORAGE_DIR);
            }
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/obterArquivo/{nomeArquivo}")
    public ResponseEntity<?> obterArquivo(@PathVariable String nomeArquivo) {
        File file = new File(STORAGE_DIR + nomeArquivo);
        if (file.exists()) {
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                return ResponseEntity.ok(content);
            } catch (IOException e) {
                LOGGER.severe("Erro ao ler o arquivo: " + e.getMessage());
                return ResponseEntity.status(500).body("Erro ao ler o arquivo");
            }
        } else {
            LOGGER.severe("Arquivo não encontrado: " + nomeArquivo);
            return ResponseEntity.status(404).body("Arquivo não encontrado");
        }
    }

    @PostMapping("/salvarArquivo/{nomeArquivo}")
    public ResponseEntity<?> salvarArquivo(@PathVariable String nomeArquivo, @RequestParam("arquivo") MultipartFile arquivo) {
        File file = new File(STORAGE_DIR + nomeArquivo);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(arquivo.getBytes());
            LOGGER.info("Arquivo salvo com sucesso: " + nomeArquivo);
            return ResponseEntity.ok("Arquivo salvo com sucesso");
        } catch (IOException e) {
            LOGGER.severe("Erro ao salvar o arquivo: " + e.getMessage());
            return ResponseEntity.status(500).body("Erro ao salvar o arquivo");
        }
    }
}
