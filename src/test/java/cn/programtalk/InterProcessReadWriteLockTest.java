package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class InterProcessReadWriteLockTest {
    String connectString = "172.24.246.68:2181";
    RetryPolicy retryPolicy = new RetryForever(1000);

    @Test
    public void testRead() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
        InterProcessReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.acquire();
        log.info("读成功");
        readLock.release();
    }

    @Test
    public void testWrite() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
        InterProcessReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.acquire();
        writeLock.acquire();
        log.info("写成功");
        writeLock.release();
        writeLock.release();
    }
}
