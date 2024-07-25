package com.example.kimhabspringminio.controller;

import com.example.kimhabspringminio.model.FileMinioRes;
import com.example.kimhabspringminio.service.MinioService;
import io.minio.messages.Item;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("minio/file")
@Slf4j
@Setter
@Getter
public class FileMinioController {
    @Value("${minio.bucket}")
    private String bucket;
    @Autowired
    private MinioService minioService;

    private Map<String, Object> stringObjectMap = new HashMap<>();
    private final Map<String, String> mimeTypeMap = new HashMap<>();
    private String getContentTypeFromExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "application/octet-stream"; // Default MIME type if no extension is found
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        return mimeTypeMap.getOrDefault(extension, "application/octet-stream");
    }


    @PostMapping("upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile[] files) {
        if (Arrays.stream(files).count() > 0) {
            for (MultipartFile file: files){
                try {
                    log.info("MultipartFile: {}", file);
                    minioService.uploadFile(bucket, file.getOriginalFilename(), file.getInputStream(), file.getSize(), file.getContentType());
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file: " + e.getMessage());
                }
            }
            return ResponseEntity.ok("File uploaded successfully! ");
        }
        throw new IllegalArgumentException();
    }

    @GetMapping("download")
    public ResponseEntity<?> downloadFile(@RequestParam String fileName) {
        try {
            InputStream fileStream = minioService.getFile(bucket, fileName.trim());
            InputStreamResource resource = new InputStreamResource(fileStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("preview")
   // @GetMapping("preview/{fileName:.+}")
    public ResponseEntity<?> previewFile(@RequestParam(value = "fileName") String fileName) {
        try {
            InputStream fileStream = minioService.getFile(bucket, fileName.trim());
            InputStreamResource resource = new InputStreamResource(fileStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE); // dynamic content
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * @param prefix path of file
    * */
    @GetMapping("all-info")
    public ResponseEntity<?> lisAllFileName(@RequestParam String prefix) {
        var allFile = minioService.listAllFileInfo(bucket, prefix);

        List<FileMinioRes> data = new ArrayList<>();
        for (Item item: allFile){
            FileMinioRes minioRes = new FileMinioRes(item);

            data.add(minioRes);
        }
        stringObjectMap.put("msg: ", "successfully");
        stringObjectMap.put("size: ", data.size());
        stringObjectMap.put("data:", data);
        return ResponseEntity.ok(stringObjectMap);
    }

    @GetMapping("info")
    public ResponseEntity<FileMinioRes> getFileInfo(@RequestParam String name){
        var data = minioService.getFileInfo(name, bucket);
        FileMinioRes fileMinioRes = new FileMinioRes(data);
        return ResponseEntity.ok(fileMinioRes);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteFile(@RequestParam String name) throws Exception {
        minioService.removeObject(bucket, name);
        return ResponseEntity.ok("deleted");
    }

}
