package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

public class InterProcessReadWriteLockTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);

    @Test
    public void testInterProcessReadWriteLock() throws Exception {
        curatorFramework.start();
        InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
        InterProcessReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.acquire();
    }
}
