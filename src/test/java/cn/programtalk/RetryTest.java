package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.SessionFailedRetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class RetryTest {
    /**
     * RetryForever
     */
    @Test
    public void testRetryForever() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", new RetryForever(2000));
        curatorFramework.start();
    }

    /**
     * SessionFailedRetryPolicy
     *
     * @throws Exception
     */
    @Test
    public void testSessionFailedRetryPolicy() throws Exception {
        RetryPolicy sessionFailedRetryPolicy = new SessionFailedRetryPolicy(new RetryForever(1000));

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(sessionFailedRetryPolicy)
                .build();
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void testRetryNTimes() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", new RetryNTimes(5, 1000));
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void testRetryOneTime() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", new RetryOneTime(1000));
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void testRetryUntilElapsed() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(3000, 1000);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", retryPolicy);
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void testExponentialBackoffRetry() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 1000);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", retryPolicy);
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }

    @Test
    public void testBoundedExponentialBackoffRetry() throws Exception {
        RetryPolicy retryPolicy = new BoundedExponentialBackoffRetry(3000, 6000, 1000);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", retryPolicy);
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }
}
