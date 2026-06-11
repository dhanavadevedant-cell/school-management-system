package com.example.demo.controller;

import com.example.demo.service.ResourceService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<com.example.demo.entity.Resource>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    @GetMapping("/search")
    public ResponseEntity<List<com.example.demo.entity.Resource>> searchResources(@RequestParam String subject) {
        return ResponseEntity.ok(resourceService.searchBySubject(subject));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResource(
            @RequestParam("title") String title,
            @RequestParam("subject") String subject,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {
        
        String username = jwt.getClaimAsString("preferred_username");

        try {
            com.example.demo.entity.Resource savedFile = resourceService.uploadResource(title, subject, username, file);
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload asset payload: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadResource(@PathVariable Long id) {
        try {
            Path fileStoragePath = resourceService.getFilePath(id);
            Resource fileSystemPayload = new UrlResource(fileStoragePath.toUri());

            if (!fileSystemPayload.exists()) {
                return ResponseEntity.notFound().build();
            }

            String internalFileName = fileStoragePath.getFileName().toString();
            String naturalFileName = internalFileName.substring(internalFileName.indexOf("_") + 1);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + naturalFileName + "\"")
                    .body(fileSystemPayload);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error gathering asset context streams.");
        }
    }
}