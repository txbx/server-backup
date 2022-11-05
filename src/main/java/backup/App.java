package backup;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author : txb
 * @date: 2022/11/4 17:23
 * @description :
 */
public class App {
    public static void main(String[] args) {
        CronUtil.schedule("0 0 1 * * ?", (Task) () -> {
            System.out.println("开始备份");
            try {
                Backup service = new Backup();
                service.doBackup();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("备份失败："+e.getMessage());
            }
        });

        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
}
