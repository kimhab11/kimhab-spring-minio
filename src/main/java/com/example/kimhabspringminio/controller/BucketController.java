package com.example.kimhabspringminio.controller;

import com.example.kimhabspringminio.model.BucketRes;
import com.example.kimhabspringminio.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("bk")
public class BucketController {
    private Map<String, Object> objRes = new HashMap<>();
    @Autowired
    private BucketService bucketService;

    @GetMapping("all")
    public ResponseEntity<?> getAll() throws Exception {
        var data = bucketService.listBuckets();
        List<BucketRes> bucketResList = new ArrayList<>();
        data.forEach(bucket -> bucketResList.add(new BucketRes(bucket)));

        objRes.put("data: ", bucketResList);
        return ResponseEntity.ok(objRes);
    }

    @PostMapping("create")
    public ResponseEntity<?> create(String name) throws Exception {
         bucketService.createBucket(name);
        objRes.put("data: ", "create bk: "+name);
        return ResponseEntity.ok(objRes);
    }

    @PostMapping("remove")
    public ResponseEntity<?> remove(@RequestParam String name) throws Exception {
        bucketService.removeBucket(name);
        objRes.put("data: ", "removed: "+name);
        return ResponseEntity.ok(objRes);
    }


}
