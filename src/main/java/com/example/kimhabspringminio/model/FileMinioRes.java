package com.example.kimhabspringminio.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
public class FileMinioRes {
    private String fileName;
    private String size;
    @JsonFormat(pattern = "dd-MM-yy HH:mm:ss")
    private String lastMod;

    private String etag;
    private String bucket;

    public FileMinioRes(StatObjectResponse statObjectResponse) {
        this.fileName = statObjectResponse.object();
        this.size = String.valueOf((double) statObjectResponse.size() / (1024 * 1024)).substring(0, 5) + " MB";
        this.lastMod = String.valueOf(statObjectResponse.lastModified());
        this.etag = statObjectResponse.etag();
        this.bucket = statObjectResponse.bucket();
    }

    public FileMinioRes(Item item) {
        this.fileName = item.objectName();
        this.size = String.valueOf((double) item.size() / (1024 * 1024)) + " MB";
        //    this.lastMod = item.lastModified() == null ? "" : String.valueOf(item.lastModified());
//        if (item.lastModified().equals(null)) {
//            this.lastMod = "";
//        } else {
//            this.lastMod = item.lastModified() == null ? "" : String.valueOf(item.lastModified());
//        }

        this.etag = item.etag();
        this.bucket = "";
    }

}


