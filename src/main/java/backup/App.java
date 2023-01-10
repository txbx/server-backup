package backup;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author : txb
 * @date: 2022/11/4 17:23
 * @description :
 */
public class App {
    public static void main(String[] args) {
        Map<String, String> getenv = System.getenv();
        System.out.println("环境变量:" + getenv);

        // 启动就执行一次
        doBackUp();

        // 定时任务
        CronUtil.schedule("0 0 1 * * ?", (Task) App::doBackUp);
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    static void doBackUp(){
        System.out.println("开始备份");
        try {
            Backup service = new Backup();
            service.doBackup();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("备份失败：" + e.getMessage());
        } finally {
            System.out.println("gc清理");
            System.gc();
        }
    }
}
