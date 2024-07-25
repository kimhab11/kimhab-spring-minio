package com.example.kimhabspringminio.service;

import com.example.kimhabspringminio.model.LocalFileRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class LocalDriveService {

    @Value("${server.port}")
    private String port;

    private final String FILE_DIRECTORY = "src/main/resources/static/uploads/";

    public void createFile(byte[] content, String fileName) throws IOException {

        // Create the upload directory if it doesn't exist
        File uploadDir = new File(FILE_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // generate filename
        String finalName = generateFilename() + fileName;
        log.info("finalName file: {}", finalName);

        // Save the file to the specified directory
        Path filePath = Paths.get(FILE_DIRECTORY + finalName);
        Files.write(filePath, content);
        log.info("File written to drive: {}", filePath);

    }

    public File download(String fileName) {
        String filePath = FILE_DIRECTORY + File.separator + fileName;

        // read file
        File file = new File(filePath);

        // check file exist
        if (file.exists()) {
            log.info("file: {}", file);
            return file;
        } else throw new RuntimeException("file not found");
    }

    public void deleteFile(String fileName) {
        // read file
        File file = new File(FILE_DIRECTORY + File.separator + fileName);
        // check file
        if (file.exists()) {
            file.delete();
            log.info("deleted file, {} ", fileName);
        } else throw new RuntimeException("file not found");
    }

    public List<LocalFileRes> listAllFiles() {

        File directory = new File(FILE_DIRECTORY);
        File[] files = directory.listFiles();

        List<LocalFileRes> fileResList = new ArrayList<>();
        if (files.length >= 1) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //  DecimalFormat decimalFormat = new DecimalFormat("#.##");

            String url = "http://"+getUrlHost()+":"+port+"/drive/file/preview?fileName=";

            for (File file : files) {
                LocalFileRes localFileRes = new LocalFileRes();
                localFileRes.setFileName(file.getName());
                localFileRes.setPath(file.getPath());
                localFileRes.setExtension(getFileExtension(file.getPath()));
                //    localFileRes.setSize(Long.parseLong(decimalFormat.format(file.length() / 1024.0))); // Convert bytes to kilobytes
                localFileRes.setSize(file.length());
                localFileRes.setCreatedAt(dateFormat.format(file.lastModified()));
                localFileRes.setUrl(url+localFileRes.getFileName());
                fileResList.add(localFileRes);

            }

            log.info("files total: {}", Arrays.stream(files).count());

        }
        log.info("file res list: {}", fileResList);
        return fileResList;
    }

    protected String generateFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = sdf.format(new Date());
        log.info("formattedDate: {}", formattedDate);

        String uuid = String.valueOf(UUID.randomUUID());
        log.info("uuid generate: {}", uuid);

        return formattedDate + "-" + uuid + " ";
    }

    protected static String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == filePath.length() - 1) {
            return ""; // No extension found
        }
        return filePath.substring(lastIndex + 1);
    }

    protected static String getUrlHost(){
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            log.info("InetAddress: {}", inetAddress);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
