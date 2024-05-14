package com.example.kimhabspringminio.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.minio.messages.Bucket;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class BucketRes {
    private String name;
    @JsonFormat(pattern = "yyyy-dd-mm HH:mm:ss", timezone = "Asia/Bangkok")
    private ZonedDateTime create;

    public BucketRes(Bucket bucket){
        this.name = bucket.name();
        this.create = bucket.creationDate();
    }
}
