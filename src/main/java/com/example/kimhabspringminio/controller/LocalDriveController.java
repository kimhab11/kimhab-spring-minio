package com.example.kimhabspringminio.controller;

import com.example.kimhabspringminio.service.LocalDriveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("drive")
@Slf4j
public class LocalDriveController {
    @Autowired
    private LocalDriveService localDriveService;

    private Map<String, Object> objectMap = new HashMap<>();

    @PostMapping("files/create")
    public ResponseEntity<?> createMultiple(@RequestParam MultipartFile[] multipartFiles) {
        if (multipartFiles.length >= 1) {
            for (MultipartFile file : multipartFiles) {
                try {
                    // get byte data
                    byte[] content = file.getBytes();
                    // original name of file
                    String originalFilename = file.getOriginalFilename();
                    // save to drive D
                    localDriveService.createFile(content, originalFilename, "D:\\");
                    log.info("File uploaded successfully");

                    return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully");
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        } else {
            throw new RuntimeException("no file");
        }
        return null;
    }

    @GetMapping("file/download")
    public ResponseEntity<?> getFile(@RequestParam String fileName){

        var file = localDriveService.download(fileName);

        if (file.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            FileSystemResource fileResource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .body(fileResource);
        } else {
            // File not found, handle accordingly
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("file/list-all")
    public ResponseEntity<?> listAllFile(){
        var files =  localDriveService.listAllFiles();

        objectMap.put("size " , files.size());
        objectMap.put("data ", files);
        return ResponseEntity.ok(objectMap);
    }
}
