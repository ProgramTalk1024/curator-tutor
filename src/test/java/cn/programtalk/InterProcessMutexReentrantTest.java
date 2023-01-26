package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 *  InterProcessMutex 锁的可重入性测试
 */
@Slf4j
public class InterProcessMutexReentrantTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/InterProcessMutexReentrantTest");
    void a() throws Exception {
        lock.acquire();
        log.info("a方法执行");
        b();
        lock.release();
    }
    void b() throws Exception {
        lock.acquire();
        log.info("b方法执行");
        lock.release();
    }

    @Test
    public void test() throws Exception {
        curatorFramework.start();
        a();
    }
}
