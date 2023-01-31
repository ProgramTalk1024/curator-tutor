package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

@Slf4j
public class InterProcessSemaphoreV2Test {
    static String connectString = "172.24.246.68:2181";
    static RetryPolicy retryPolicy = new RetryForever(10000);

    @Test
    public void testInterProcessSemaphoreV21() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV21", 3);
        Lease lease = null;
        try {
            lease = interProcessSemaphoreV2.acquire();
            log.info("{} 获取到租约", Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 为了测试租约等待情况，我不释放租约
            //interProcessSemaphoreV2.returnLease(lease);
            //log.info("{} 释放掉租约", Thread.currentThread().getName());
        }
        while (true) {

        }
    }

    @Test
    public void testInterProcessSemaphoreV22() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        SharedCount sharedCount = new SharedCount(curatorFramework, "/InterProcessSemaphoreV22_SharedCount", 3);
        sharedCount.start();
        InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV22", sharedCount);
        Lease lease = null;
        try {
            lease = interProcessSemaphoreV2.acquire();
            log.info("{} 获取到租约", Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 为了测试租约等待情况，我不释放租约
            //interProcessSemaphoreV2.returnLease(lease);
            //log.info("{} 释放掉租约", Thread.currentThread().getName());
        }
        while (true) {

        }
    }
}
