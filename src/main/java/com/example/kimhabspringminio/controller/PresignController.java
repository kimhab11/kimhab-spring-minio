package com.example.kimhabspringminio.controller;

import com.example.kimhabspringminio.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("get-pre")
public class PresignController {
    @Autowired
    private MinioService minioService;


    /**
     @param objName: the existed name of file being preview
     @return url -> use GET for open that file
      * */
    @GetMapping("file-get/url")
    public ResponseEntity<?> getPresignedObjectUrl(@RequestParam String objName) throws Exception {
        var data = minioService.getPresignedObjectUrl(objName, 600);
        return ResponseEntity.ok(data);
    }


    /**
    @param newObjName: the name with extension of file being uploading
    @return -> use PUT then select body with binary file to upload
    * */
    @GetMapping("file-put/url")
    public ResponseEntity<?> getPresignedObjectUrlPut(@RequestParam String newObjName) throws Exception {
        var data = minioService.getPresignedPutUrl(newObjName, 60);
        return ResponseEntity.ok(data);
    }


    /**
     @param objName: the existed name of file being preview
     @return url -> use HEAD for get meta data of file
      * */
    @GetMapping("file-head/url")
    public ResponseEntity<?> getPresignedObjectUrlHead(@RequestParam String objName) throws Exception {
        var data = minioService.getMetaData(objName);
        return ResponseEntity.ok(data);
    }
}
