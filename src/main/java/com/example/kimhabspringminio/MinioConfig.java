package com.example.kimhabspringminio;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.accessKey}")
  //  private String accessKey = "bdgPkQoX1WpW5V5twYZD";
   private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;
  //  private String secretKey = "UMpij8r4nimTbRjiSYtiPGAneQbPwVUwchYdernt";

    @Bean
    public MinioClient minioClient() {
        try {
            return MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }

    }
}
