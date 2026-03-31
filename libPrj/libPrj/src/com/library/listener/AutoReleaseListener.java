package com.library.listener;

import com.library.dao.SeatDao;
import com.library.dao.SeatDaoImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AutoReleaseListener implements ServletContextListener {

    // 声明一个定时任务调度器
    private ScheduledExecutorService scheduler;
    private SeatDao seatDao = new SeatDaoImpl();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Tomcat 启动时触发
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // 设定定时任务：延迟0分钟开始，每隔 1 分钟执行一次
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 悄悄去数据库清理超时座位
                seatDao.autoReleaseTimeoutSeats();
            } catch (Exception e) {
                System.err.println("后台清理座位任务异常：" + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
        
        System.out.println("[系统日志] 图书馆座位定时巡检任务已启动！");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Tomcat 关闭时触发，优雅地销毁线程池防止内存泄漏
        if (scheduler != null) {
            scheduler.shutdownNow();
            System.out.println("[系统日志] 图书馆座位定时巡检任务已关闭。");
        }
    }
}