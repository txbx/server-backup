package com.txb.service.bk;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;

/**
 * @author : txb
 * @date: 2022/11/4 17:23
 * @description :
 */
public class App {
    public static void main(String[] args) {
        CronUtil.schedule("0 0 1 * * ?", (Task) () -> {
            System.out.println("开始备份");
            GoogleDriveBkService service = new GoogleDriveBkService();
            service.doBk();
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
}
