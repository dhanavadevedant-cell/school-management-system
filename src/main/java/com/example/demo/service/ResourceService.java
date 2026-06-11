package com.example.demo.service;

import com.example.demo.entity.Resource;
import com.example.demo.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    
    // Directory target where actual file documents are written
    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploaded_resources";

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
        
        // Auto-create folder structural paths at boot if missing
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<Resource> searchBySubject(String subject) {
        return resourceRepository.findBySubjectContainingIgnoreCase(subject);
    }

    public Resource uploadResource(String title, String subject, String username, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file upload.");
        }

        // Use UUID prefix identifiers to safely manage duplicate file names
        String cleanFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetPath = Paths.get(uploadDir).resolve(cleanFileName);
        
        // Write file raw bytes directly to disk path location
        Files.copy(file.getInputStream(), targetPath);

        // Record deployment path details inside our PostgreSQL schema
        Resource resource = new Resource(title, subject, username, targetPath.toString(), LocalDateTime.now());
        return resourceRepository.save(resource);
    }

    public Path getFilePath(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requested resource document not tracked in registry."));
        return Paths.get(resource.getFilePath());
    }
}