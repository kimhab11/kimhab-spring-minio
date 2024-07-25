package com.example.kimhabspringminio.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LocalFileRes {
    private String fileName;
    private String path;
    @JsonFormat(pattern = "dd-MM-yy HH:mm:ss")
    private String createdAt;
    private String extension;
    private String url;
    private long size;

    @JsonProperty("size")
    public String getSizeWithUnit() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        }
    }
}
