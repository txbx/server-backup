package com.txb.service.bk;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * @author : txb
 * @date: 2022/11/4 14:49
 * @description :
 */
public class GoogleDriveApi {

    private static final String APPLICATION_NAME = "server-bk";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE);

    private final Drive service;

    public GoogleDriveApi(InputStream configJsonFile) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, fromServiceAccount(configJsonFile))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public String creatFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id") //指定响应内容
                    .execute();
            System.out.println("文件id ID: " + file.getId());
            return file.getId();
        } catch (GoogleJsonResponseException e) {
            // TODO:send error email
            System.err.println("创建文件夹失败: " + e.getDetails());
            throw e;
        }
    }

    public List<File> listFile(String packageName) {


        return null;
    }

    public Boolean deleteFile(String fileId) {


        return null;
    }

    public File uploadFile() {


        return null;
    }

    private static HttpRequestInitializer fromServiceAccount(InputStream configJsonFile) throws IOException {
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(configJsonFile).createScoped(SCOPES);
        return new HttpCredentialsAdapter(credentials);
    }

}
