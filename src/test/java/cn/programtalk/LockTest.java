package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class LockTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);

    @Test
    public void testLock1() throws Exception {
        curatorFramework.start();
        // 定义锁
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/InterProcessMutex");
        // 获取锁
        lock.acquire();
        log.info("此处是业务代码");
        // 模拟业务执行30秒
        TimeUnit.SECONDS.sleep(30);
        // 释放锁
        lock.release();
    }

    @Test
    public void testLock2() throws Exception {
        curatorFramework.start();
        // 定义锁
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/InterProcessMutex");
        // 获取锁
        lock.acquire(10, TimeUnit.SECONDS);
        log.info("此处是业务代码");
        // 模拟业务执行30秒
        TimeUnit.SECONDS.sleep(30);
        // 释放锁
        lock.release();
    }

    @Test
    public void testLock3() throws Exception {
        curatorFramework.start();
        // 定义锁
        InterProcessLock lock = new InterProcessSemaphoreMutex(curatorFramework, "/InterProcessSemaphoreMutex");
        // 获取锁
        try {
            boolean got = lock.acquire(30, TimeUnit.SECONDS);
            if (got) {
                log.info("此处是业务代码");
                // 模拟业务执行30秒
                TimeUnit.SECONDS.sleep(30);
            } else {
                log.warn("未获取到锁");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            lock.release();
        }
    }

    @Test
    public void testLock4() {
        curatorFramework.start();
    }
}
