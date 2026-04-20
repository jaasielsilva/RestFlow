package com.jaasielsilva.erpcorporativo.app.service.shared;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jaasielsilva.erpcorporativo.app.exception.ValidationException;

@Service
public class SupportAttachmentStorageService {

    private static final long MAX_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "application/pdf",
            "text/plain"
    );

    private final Path rootPath;

    public SupportAttachmentStorageService() {
        this.rootPath = Paths.get(System.getProperty("java.io.tmpdir"), "restflow-support-attachments");
    }

    public StoredFile store(Long tenantId, Long ticketId, MultipartFile file) {
        validateFile(file);
        String originalName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalName);
        String storedName = UUID.randomUUID() + extension;

        Path ticketDir = rootPath.resolve(String.valueOf(tenantId)).resolve(String.valueOf(ticketId));

        try {
            Files.createDirectories(ticketDir);
            Path destination = ticketDir.resolve(storedName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredFile(originalName, storedName, destination.toString(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new ValidationException("Falha ao armazenar anexo: " + originalName);
        }
    }

    public Resource loadAsResource(String absolutePath) {
        try {
            Path path = Paths.get(absolutePath);
            if (!Files.exists(path)) {
                throw new ValidationException("Arquivo não encontrado.");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ValidationException("Arquivo indisponível para leitura.");
            }
            return resource;
        } catch (Exception ex) {
            throw new ValidationException("Falha ao carregar anexo.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Arquivo inválido.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ValidationException("Arquivo excede o limite de 10MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new ValidationException("Tipo de arquivo não permitido.");
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "anexo";
        }
        return fileName.replace("\\", "_").replace("/", "_").trim();
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        String extension = fileName.substring(idx).toLowerCase();
        return extension.length() > 10 ? "" : extension;
    }

    public record StoredFile(
            String originalName,
            String storedName,
            String absolutePath,
            String contentType,
            long size
    ) {
    }
}
