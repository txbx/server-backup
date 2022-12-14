package backup;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;

/**
 * @author : txb
 * @date: 2022/11/4 10:12
 * @description : 进度监听器
 */
public class CustomProgressListener implements MediaHttpUploaderProgressListener {
    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
                System.out.println("开始初始化!");
                break;
            case INITIATION_COMPLETE:
                System.out.println("初始化完成!");
                break;
            case MEDIA_IN_PROGRESS:
                System.out.println("上传进度："+uploader.getProgress());
                System.out.println("已经上传大小（Bytes）："+uploader.getNumBytesUploaded());
                break;
            case MEDIA_COMPLETE:
                System.out.println("上传完成!");
        }
    }
}
