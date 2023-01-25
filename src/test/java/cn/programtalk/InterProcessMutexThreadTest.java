package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

@Slf4j
public class InterProcessMutexThreadTest implements Runnable {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);


    @Override
    public void run() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        // 定义锁
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/InterProcessMutex");
        try {
            lock.acquire();
            String threadName = Thread.currentThread().getName();
            log.info("{} ，执行业务代码开始", threadName);
            TimeUnit.SECONDS.sleep(10);
            log.info("{} ，执行业务代码完毕", threadName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        InterProcessMutexThreadTest task = new InterProcessMutexThreadTest();
        Thread t1 = new Thread(task, "任务1");
        Thread t2 = new Thread(task, "任务2");
        t1.start();
        t2.start();
    }
}
