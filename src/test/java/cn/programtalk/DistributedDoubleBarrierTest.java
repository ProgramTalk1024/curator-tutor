package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DistributedDoubleBarrierTest {
    @Test
    public void testDistributedDoubleBarrier() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 1; i++) {
            executorService.submit(() -> {
                try {
                    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new RetryForever(1000));
                    curatorFramework.start();
                    // 创建distributedDoubleBarrier
                    DistributedDoubleBarrier distributedDoubleBarrier = new DistributedDoubleBarrier(curatorFramework, "/DistributedDoubleBarrier", 2);
                    distributedDoubleBarrier.enter();
                    String threadName = Thread.currentThread().getName();
                    log.info("{}进入障碍", threadName);
                    log.info("{}执行具体业务逻辑", threadName);
                    TimeUnit.SECONDS.sleep(1);
                    distributedDoubleBarrier.leave();
                    log.info("{}离开障碍", threadName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        while (true){

        }
    }
}
