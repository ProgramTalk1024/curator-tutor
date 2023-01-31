package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DistributedBarrierTest {
    @Test
    public void testDistributedBarrier() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new RetryForever(1000));
        curatorFramework.start();
        // 创建DistributedBarrier
        DistributedBarrier distributedBarrier = new DistributedBarrier(curatorFramework, "/DistributedBarrier");
        // setBarrier的功能是创建path
        distributedBarrier.setBarrier();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    String threadName = Thread.currentThread().getName();
                    log.info("{}线程设置障碍", threadName);
                    distributedBarrier.waitOnBarrier();
                    log.info("障碍被移除，{}线程继续执行", threadName);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        TimeUnit.SECONDS.sleep(5);
        log.info(">>移除障碍<<");
        distributedBarrier.removeBarrier();
    }
}
