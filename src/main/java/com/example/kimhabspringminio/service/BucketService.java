package com.example.kimhabspringminio.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BucketService {
    @Autowired
    private MinioClient minioClient;

    public List<Bucket> listBuckets() throws Exception {
        try {
            var allBk = minioClient.listBuckets();
            log.info("all buckets: {}", allBk.stream().map(Bucket::name).collect(Collectors.toList()));
            return minioClient.listBuckets();
        } catch (MinioException e) {
            throw new Exception("Error occurred while listing buckets: " + e.getMessage());
        }
    }

    public void createBucket(String bucketName) throws Exception {
        try {
            var bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            boolean bkExist = minioClient.bucketExists(bucketExistsArgs);
            log.info("bucket Exist: {}", bkExist);

            if (!bkExist) {
                var makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                minioClient.makeBucket(makeBucketArgs);
                log.info("create bk: {}", bucketName);
            } else {
                throw new Exception("Bucket already exists");
            }
        } catch (MinioException e) {
            throw new Exception("Error occurred while creating bucket: " + e.getMessage());
        }
    }

    public void removeBucket(String bucketName) throws Exception {
        try {
            //  bk exist arg
            var bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();

            // remove bk arg
            var rmExistsArgs = RemoveBucketArgs.builder().bucket(bucketName).build();

            if (minioClient.bucketExists(bucketExistsArgs)) {

                // start remove bk
                minioClient.removeBucket(rmExistsArgs);
                log.info("remove bk: {}", bucketName);
            } else {
                throw new Exception("Bucket does not exist");
            }
        } catch (MinioException e) {
            throw new Exception("Error occurred while removing bucket: " + e.getMessage());
        }
    }

}
