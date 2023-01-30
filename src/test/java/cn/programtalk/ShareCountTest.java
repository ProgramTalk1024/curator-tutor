package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShareCountTest {
    // 连接地址
    public static final String CONNECT_STRING = "172.24.246.68:2181";

    public static final RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @Test
    public void testShareCount() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(CONNECT_STRING, RETRY_POLICY);
        curatorFramework.start();
        SharedCount sharedCount = new SharedCount(curatorFramework, "/ShareCount", 0);
        sharedCount.start();
        sharedCount.addListener(new SharedCountListener() {
            @Override
            public void countHasChanged(SharedCountReader sharedCountReader, int newCount) throws Exception {
                log.info("countHasChanged callback");
                log.info("newCount={}", newCount);
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {

            }
        }, EXECUTOR_SERVICE);
        sharedCount.setCount(1);
        TimeUnit.DAYS.sleep(1);
        sharedCount.close();
    }
}