package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.SessionFailedRetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class RetryTest {
    /**
     * RetryForever
     */
    @Test
    public void testRetryForever() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181",  new RetryForever(2000));
        curatorFramework.start();
    }

    /**
     * SessionFailedRetryPolicy
     * @throws Exception
     */
    @Test
    public void testSessionFailedRetryPolicy() throws Exception {
        RetryPolicy sessionFailedRetryPolicy = new SessionFailedRetryPolicy(new RetryForever(1000));

        CuratorFramework curatorFramework = null;
        try{
            //fluent 样式的API风格
            curatorFramework = CuratorFrameworkFactory.builder()
                    .connectString("localhost:2181")
                    .sessionTimeoutMs(1000)
                    .retryPolicy(sessionFailedRetryPolicy)
                    .connectionTimeoutMs(1000)
                    .build();
            curatorFramework.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
