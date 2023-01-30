package cn.programtalk;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InterProcessSemaphoreV2Test {
    static String connectString = "172.24.246.68:2181";
    static RetryPolicy retryPolicy = new RetryForever(10000);

    @Test
    public void testInterProcessSemaphoreV21() {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executor.submit(new Thread(new Task()));
        }
        while (true) {
        }
    }

    @Test
    public void testInterProcessSemaphoreV22() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV2", new SharedCount(curatorFramework, "/InterProcessSemaphoreV2_SharedCount", 3));
        Lease lease = null;
        try {
            lease = interProcessSemaphoreV2.acquire();
            log.info("{} 获取到租约", Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //interProcessSemaphoreV2.returnLease(lease);
            //log.info("{} 释放掉租约", Thread.currentThread().getName());
        }
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
            curatorFramework.start();
            InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV2", 3);
            Lease lease = null;
            try {
                lease = interProcessSemaphoreV2.acquire();
                log.info("{} 获取到租约", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                interProcessSemaphoreV2.returnLease(lease);
                log.info("{} 释放掉租约", Thread.currentThread().getName());
            }
        }
    }

    static class Task2 implements Runnable {
        @Override
        public void run() {
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
            curatorFramework.start();
            InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV2", new SharedCount(curatorFramework, "/InterProcessSemaphoreV2-SharedCount", 3));
            Lease lease = null;
            try {
                lease = interProcessSemaphoreV2.acquire();
                log.info("{} 获取到租约", Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                interProcessSemaphoreV2.returnLease(lease);
                log.info("{} 释放掉租约", Thread.currentThread().getName());
            }
        }
    }
}
