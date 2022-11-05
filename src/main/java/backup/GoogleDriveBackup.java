package backup;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.api.services.drive.model.File;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author : txb
 * @date: 2022/11/4 14:47
 * @description :
 */
public class GoogleDriveBackup {

    private final GoogleDriveApi googleDriveApi;

    public GoogleDriveBackup() throws GeneralSecurityException, IOException {
        BufferedInputStream inputStream = FileUtil.getInputStream(Const.CredentialsJsonPath);
        this.googleDriveApi = new GoogleDriveApi(inputStream, Const.Parent);
    }

    public void doBackup() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ThreadUtil.execute(()->{
            System.out.println("打包文件...");
            latch.countDown();
        });


        String packageId = this.findBkPackageId();
        this.cleanHistory(packageId);

        boolean await = latch.await(30, TimeUnit.MINUTES);

    }

    private String findBkPackageId() throws IOException {
        List<File> files = googleDriveApi.listFile("mimeType='application/vnd.google-apps.folder' and name='" + Const.BackPackage + "'");
        String id = null;
        for (File file : files) {
            if (Const.BackPackage.equals(file.getName())) {
                id = file.getId();
                break;
            }
        }

        if(ObjectUtil.isNotEmpty(id)){
            return id;
        }

        return googleDriveApi.creatFolder(Const.BackPackage);
    }


    private void cleanHistory(String packageId) {


    }

    private File tarPackage() {


        return null;
    }


    private String uploadTar() {


        return null;
    }
}
