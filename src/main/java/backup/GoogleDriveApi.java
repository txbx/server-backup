package backup;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
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

    private final String parent;

    public GoogleDriveApi(InputStream configJsonFile,String parent) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, fromServiceAccount(configJsonFile))
                .setApplicationName(APPLICATION_NAME)
                .build();
        this.parent = parent;
    }

    public String creatFolder(String folderName) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(CollectionUtil.newArrayList(parent));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id") //指定响应内容
                    .execute();
            System.out.println("文件id ID: " + file.getId());
            return file.getId();
        } catch (GoogleJsonResponseException e) {
            System.err.println("创建文件夹失败: " + e.getDetails());
            throw e;
        }
    }

    public List<File> listFile(String q) throws IOException {
        Drive.Files.List list = service.files().list();
        if(ObjectUtil.isNotEmpty(q)){
            list.setQ(q);
        }
        FileList result = list.execute();
        return  result.getFiles();
    }

    public Void deleteFile(String fileId) throws IOException {
        return service.files().delete(fileId).execute();
    }

    public File uploadFile(String parent,String fileName,String fileUrl) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(fileName);

        fileMetadata.setParents(CollectionUtil.newArrayList(parent));

        java.io.File filePath = new java.io.File(fileUrl);
        FileContent mediaContent = new FileContent(null, filePath);

        try {
            Drive.Files.Create create = service.files().create(fileMetadata, mediaContent);
            MediaHttpUploader mediaHttpUploader = create.getMediaHttpUploader();
            mediaHttpUploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
            mediaHttpUploader.setProgressListener(new CustomProgressListener());
            File file = create.execute();
            System.out.println("文件 ID: " + file.getId());
            return file;
        } catch (GoogleJsonResponseException e) {
            System.err.println("上传文件失败: " + e.getDetails());
            throw e;
        }
    }

    private static HttpRequestInitializer fromServiceAccount(InputStream configJsonFile) throws IOException {
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(configJsonFile).createScoped(SCOPES);
        return new HttpCredentialsAdapter(credentials);
    }

}
