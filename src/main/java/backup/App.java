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
    public static void main(String[] args) throws UnsupportedEncodingException {
        Map<String, String> getenv = System.getenv();
        System.out.println("环境变量:" + getenv);

        System.out.println("属性："+System.getProperties());
        //获取系统默认编码
        System.out.println("系统默认编码：" + System.getProperty("file.encoding")); //查询结果GBK
        //系统默认字符编码
        System.out.println("系统默认字符编码：" + Charset.defaultCharset()); //查询结果GBK
        //操作系统用户使用的语言
        System.out.println("系统默认语言：" + System.getProperty("user.language")); //查询结果zh

        // 使用String的有参构造方法
        String str = new String("hhhh ty编码更合适是asdasd");
        // 1.以GBK编码方式获取str的字节数组，再用String有参构造函数构造字符串
        System.out.println(new String(str.getBytes("GBK")));
        // 2.以UTF-8编码方式获取str的字节数组，再以默认编码构造字符串
        System.out.println(new String(str.getBytes(StandardCharsets.UTF_8)));


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
        }
    }
}
