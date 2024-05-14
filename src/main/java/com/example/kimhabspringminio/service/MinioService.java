package com.example.kimhabspringminio.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MinioService {

    @Value("${minio.bucket}")
    private String bucket;

    @Autowired
    private MinioClient minioClient;

    String getCurrentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      //  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-dd-MM HH:mm:ss");
        String time = LocalDateTime.now().format(formatter);
        log.info("prefix: {}", time);
        return time;
    }

    public ObjectWriteResponse uploadFile(String bucketName,
                                          String objectName,
                                          InputStream stream,
                                          long size,
                                          String contentType) {


        String finalFilename = "";
        String uuid = UUID.randomUUID().toString();
        finalFilename = getCurrentDateTime() + "/" + uuid + " "+ objectName;
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(finalFilename)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build());
            log.info("final obj name: {}", objectWriteResponse.object());
            return objectWriteResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public InputStream getFile (String bucketName, String fileName) throws IOException {
        try {
            // Create GetObjectArgs with bucket name and object name
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build();

            var res = minioClient.getObject(args) ;
            log.info("");

            return minioClient.getObject(args);
        } catch (MinioException e) {
            e.printStackTrace();
            throw new IOException("Failed to retrieve file from MinIO: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public List<Item> listAllFileInfo(String buc, String prefix) {

        if (prefix.isBlank()) {
            prefix = "";
        } else  if (!prefix.endsWith("/") ) {
            prefix = prefix + "/";
        }

        try {
            log.info("prefix: {}", prefix);

            // Create ListObjectsArgs with bucket name
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(buc)
                    .recursive(true)
                    .prefix(prefix.trim())
                    .build();

            // Extract object names from results
            Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);

            List<Item> allItem = new ArrayList<>();
            for (Result<Item> file : results) {
                Item item = file.get();
                String objName = item.objectName();

                // Exclude pseudo-folders (object names that end with a slash)
                if (!objName.endsWith("/")) {
                    allItem.add(item);
                }
            }
            log.info("All file: {}", allItem.stream().map(Item::objectName).collect(Collectors.toList()));
            log.info("Total: {}", allItem.size());
            return allItem;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    public StatObjectResponse getFileInfo(String objName, String buc) {
        try {
            // Create GetObjectArgs with bucket name and object name
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(buc)
                    .object(objName)
                    .build();

            // Retrieve object information from MinIO
            var obj = minioClient.statObject(args);
            log.info("objStat: {}", obj);
            return obj;

        } catch (MinioException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void removeObject(String bucketName, String objectName) throws Exception {
        try {
            // Check if the bucket exists
            var bkExist = BucketExistsArgs.builder().bucket(bucketName).build();
            boolean bucketExists = minioClient.bucketExists(bkExist);
            log.info("bucketExists: {}", bucketExists);

            if (bucketExists) {

                RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build();
                // Remove the object from the bucket
                minioClient.removeObject(removeObjectArgs);
                System.out.println("Object '" + objectName + "' removed successfully from bucket '" + bucketName + "'.");
            } else {
                throw new Exception("Bucket '" + bucketName + "' does not exist.");
            }
        } catch (MinioException e) {
            throw new Exception("Error occurred while removing object: " + e.getMessage());
        }
    }

    public String getPresignedObjectUrl(String objectName, int expirySeconds) throws Exception {
        try {
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("response-content-type", "application/json");

            var arg = GetPresignedObjectUrlArgs.builder()
                    .object(objectName)
                    .method(Method.GET)
                    .bucket(bucket)
                    .extraQueryParams(reqParams)
                    .expiry(expirySeconds, TimeUnit.SECONDS).build();

            // Generate a presigned URL for downloading the object
            String url = minioClient.getPresignedObjectUrl(arg);
            log.info("url: {}", url);
            return url;
        } catch (MinioException e) {
            throw new Exception("Error occurred while generating presigned URL for object: " + e.getMessage());
        }
    }

    public String getPresignedPutUrl(String objectName, int expirySeconds) throws Exception {

        try {
            Map<String, String> reqParams = new HashMap<String, String>();
            reqParams.put("response-content-type", "application/json");

            // presigned URL string to upload
            var arg = GetPresignedObjectUrlArgs.builder()
                    .object(objectName)
                    .method(Method.PUT)
                    .bucket(bucket)
                    .extraQueryParams(reqParams)
                    .expiry(expirySeconds, TimeUnit.SECONDS).build();

            // Generate a presigned URL for uploading the object
            String url = minioClient.getPresignedObjectUrl(arg);
            return url;
        } catch (MinioException e) {
            throw new Exception("Error occurred while generating presigned URL for uploading object: " + e.getMessage());
        }
    }

    public String getMetaData(String objectName) throws Exception {
        try {
            Map<String, String> reqParams = new HashMap<String, String>();
            reqParams.put("response-content-type", "application/json");

            var arg = GetPresignedObjectUrlArgs.builder()
                    .method(Method.HEAD)
                    .bucket(bucket)
                    .object(objectName)
                    .expiry(1, TimeUnit.DAYS)
                    .extraQueryParams(reqParams).build();

            var url = minioClient.getPresignedObjectUrl(arg);
            log.info("url: {}", url);
            return url;
        } catch (MinioException e) {
            throw new Exception("Error occurred while generating presigned URL for uploading object: " + e.getMessage());
        }
    }
}
