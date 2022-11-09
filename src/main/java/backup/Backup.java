package backup;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.archiver.Archiver;
import com.google.api.services.drive.model.File;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * @author : txb
 * @date: 2022/11/4 14:47
 * @description :
 */
public class Backup {

    private final GoogleDriveApi googleDriveApi;

    public Backup() throws GeneralSecurityException, IOException {
        BufferedInputStream inputStream = FileUtil.getInputStream(Const.CredentialsJsonPath);
        this.googleDriveApi = new GoogleDriveApi(inputStream, Const.Parent);
    }

    public void doBackup() throws Exception {
        Future<String> fileNameFuture = ThreadUtil.execAsync(() -> {
            System.out.println("打包文件...");
            java.io.File file = this.tarPackage();
            System.out.println("打包完成");
            try {
                System.out.println("压缩文件...");
                this.gzipArcher(file);
                System.out.println("压缩完成");
            } catch (FileNotFoundException e) {
                System.out.println("压缩文件失败");
                return null;
            }
            return file.getAbsolutePath() + ".gz";
        });

        String packageId = this.findBkPackageId();
        this.cleanHistory(packageId);

        String fileName = fileNameFuture.get(30, TimeUnit.MINUTES);
        this.uploadTar(packageId,fileName);
    }

    private String findBkPackageId() throws IOException {
        List<File> files = googleDriveApi.listFile("mimeType='application/vnd.google-apps.folder' and name='" + Const.ServerName + "'");
        for (File file : files) {
            if (Const.ServerName.equals(file.getName())) {
                return file.getId();
            }
        }
        return googleDriveApi.creatFolder(Const.ServerName);
    }


    private void cleanHistory(String packageId) {


    }

    private java.io.File tarPackage() {
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        String fileName = Const.ServerName + "-" + timestamp + ".tar";
        String name = Const.TarFilePath +"/" + fileName;
        java.io.File file = new java.io.File(name);

        Archiver archiver = CompressUtil.createArchiver(CharsetUtil.CHARSET_UTF_8, ArchiveStreamFactory.TAR, file);
        archiver.add(new java.io.File(Const.BackupPackage));
        archiver.close();
        return file;
    }

    private void gzipArcher(java.io.File file) throws FileNotFoundException {
        try {
            BufferedInputStream in = FileUtil.getInputStream(file);
            FileOutputStream out = new FileOutputStream(file.getAbsolutePath() + ".gz");
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            byte[] buffer = new byte[8192];
            int n = 0;
            while((n = in.read(buffer, 0, buffer.length)) > 0){
                gzip.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void uploadTar(String parent,String absolutePathPath) throws IOException {
        System.out.println("上传文件......");
        googleDriveApi.uploadFile(parent,absolutePathPath);
        System.out.println("上传完成");
    }
}
