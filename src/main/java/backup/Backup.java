package backup;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
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
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
        this.cleanCloudHistory(packageId);

        String fileName = fileNameFuture.get(30, TimeUnit.MINUTES);
        this.uploadTar(packageId, fileName);

        this.cleanLocalHistory(fileName);
    }

    private void cleanLocalHistory(String fileName) {
        System.out.println("删除本地文件:" + fileName);
        FileUtil.del(fileName);
        FileUtil.del(fileName.substring(0, fileName.length() - 3));
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


    private void cleanCloudHistory(String packageId) throws IOException {
        // 取默认配置，删除历史文件
        // 取所有文件 根据日期取3天前的文件集合。判断3天前的文件集合和当前的文件集合size是否一样，一样则保留一份最新的文件
        long nowTimestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        String saveDay = Const.SaveDay;
        long saveDayInt = Long.parseLong(saveDay);
        Long delTime = nowTimestamp - 86400L * saveDayInt;

        String q = "'" + packageId + "' in parents";
        List<File> files = googleDriveApi.listFile(q);
        List<Map<String, Object>> delFileList = new ArrayList<>();
        List<String> bkFileIds = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            int fileNameLength = name.length();
            // 截断文件名，判断是不是备份文件
            int limitLength = Const.ServerName.length() + 7;
            if (fileNameLength <= limitLength) {
                System.out.println("不是备份文件：" + name);
                continue;
            }

            // 前缀
            String prefix = name.substring(0, Const.ServerName.length());
            System.out.println("前缀：" + prefix);
            if (!Const.ServerName.equals(prefix)) {
                System.out.println("不是备份文件：" + name);
                continue;
            }

            // 后缀
            String suffix = name.substring(name.length() - 7);
            System.out.println("后缀：" + suffix);
            if (!".tar.gz".equals(suffix)) {
                System.out.println("不是备份文件：" + name);
                continue;
            }

            // 时间戳
            String fileTimestamp = name.substring(Const.ServerName.length() + 1, Const.ServerName.length() + 11);
            System.out.println("时间戳:" + fileTimestamp);
            long fileTimestampL;
            try {
                fileTimestampL = Long.parseLong(fileTimestamp);
            } catch (NumberFormatException e) {
                System.out.println("不是备份文件：" + name);
                continue;
            }

            bkFileIds.add(file.getId());

            if (fileTimestampL <= delTime) {
                Map<String, Object> delMap = new HashMap<>();
                delMap.put("timestamp", fileTimestampL);
                delMap.put("fileId", file.getId());
                delMap.put("name", name);
                delFileList.add(delMap);
            }

        }

        if (delFileList.size() == bkFileIds.size()) {
            List<Map<String, Object>> delFileListSorted = delFileList.stream().sorted((o1, o2) -> {
                Long o1Timestamp = (Long) o1.get("timestamp");
                Long o2Timestamp = (Long) o2.get("timestamp");
                return o1Timestamp.compareTo(o2Timestamp);
            }).collect(Collectors.toList());

            if (ObjectUtil.isNotEmpty(delFileListSorted)) {
                delFileListSorted.remove(delFileList.size() - 1);
            }
        }

        for (Map<String, Object> delFileMap : delFileList) {
            String fileId = (String) delFileMap.get("fileId");
            System.out.println("删除过期备份文件：" + delFileMap.get("name"));
            googleDriveApi.deleteFile(fileId);
        }
    }

    private java.io.File tarPackage() {
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        String fileName = Const.ServerName + "-" + timestamp + ".tar";
        String name = Const.TarFilePath + "/" + fileName;
        java.io.File file = new java.io.File(name);

        // 文件如果在打包的同时文件被更新了，会出现这个异常 IOException: Request to write '5510' bytes exceeds size in header of '180360043' bytes for entry 'server-backup/mysql/data/binlog.000010'
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
            while ((n = in.read(buffer, 0, buffer.length)) > 0) {
                gzip.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void uploadTar(String parent, String absolutePathPath) throws IOException {
        System.out.println("上传文件......");
        googleDriveApi.uploadFile(parent, absolutePathPath);
        System.out.println("上传完成");
    }
}
