package com.bid2buy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${file.upload.profile.dir}")
    private String profileUploadDir;
    
    @Value("${file.upload.citizenship.dir}")
    private String citizenshipUploadDir;
    
    @Value("${file.upload.auction.dir}")
    private String auctionUploadDir;
    
    public String storeProfilePicture(MultipartFile file) throws IOException {
        return storeFile(file, profileUploadDir);
    }
    
    public String storeCitizenshipCard(MultipartFile file) throws IOException {
        return storeFile(file, citizenshipUploadDir);
    }
    
    public String storeAuctionImage(MultipartFile file) throws IOException {
        return storeFile(file, auctionUploadDir);
    }
    
    private String storeFile(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        // Validate file type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name is null");
        }
        
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!isValidFileType(extension)) {
            throw new IllegalArgumentException("Invalid file type. Only JPG, PNG, and PDF are allowed.");
        }
        
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filename;
    }
    
    private boolean isValidFileType(String extension) {
        String lowerExt = extension.toLowerCase();
        return lowerExt.equals(".jpg") || lowerExt.equals(".jpeg") || 
               lowerExt.equals(".png") || lowerExt.equals(".pdf");
    }
    
    public void deleteFile(String filename, String uploadDir) {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw
            System.err.println("Error deleting file: " + filename);
        }
    }
}

