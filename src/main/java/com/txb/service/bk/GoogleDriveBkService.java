package com.txb.service.bk;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.api.services.drive.model.File;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * @author : txb
 * @date: 2022/11/4 14:47
 * @description :
 */
public class GoogleDriveBkService {

    private final GoogleDriveApi googleDriveApi;

    public GoogleDriveBkService() throws GeneralSecurityException, IOException {
        BufferedInputStream inputStream = FileUtil.getInputStream(ServiceConst.CredentialsJsonPath);
        this.googleDriveApi = new GoogleDriveApi(inputStream, ServiceConst.Parent);
    }

    public void doBk() throws IOException {
        String packageId = this.findBkPackageId();
        this.cleanHistory(packageId);
    }

    private String findBkPackageId() throws IOException {
        List<File> files = googleDriveApi.listFile("mimeType='application/vnd.google-apps.folder' and name='" + ServiceConst.BackPackage + "'");
        String id = null;
        for (File file : files) {
            if (ServiceConst.BackPackage.equals(file.getName())) {
                id = file.getId();
                break;
            }
        }

        if(ObjectUtil.isNotEmpty(id)){
            return id;
        }

        return googleDriveApi.creatFolder(ServiceConst.BackPackage);
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
