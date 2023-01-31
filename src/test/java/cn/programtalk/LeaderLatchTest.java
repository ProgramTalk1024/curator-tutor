package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LeaderLatchTest {
    @Test
    public void testLeaderLatch() throws Exception {
        List<LeaderLatch> leaderLatches = new ArrayList<>();
        List<CuratorFramework> curatorFrameworks = new ArrayList<>();
        try {
            // 包装10个CuratorFramework客户端和LeaderLatch
            for (int i = 0; i < 10; i++) {
                CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new ExponentialBackoffRetry(1000, 3));
                curatorFramework.start();
                curatorFrameworks.add(curatorFramework);
                LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, "/LeaderLatch", "node-" + i, LeaderLatch.CloseMode.NOTIFY_LEADER);
                leaderLatch.addListener(new LeaderLatchListener() {
                    @Override
                    public void isLeader() {
                        log.info("isLeader callback : {} is Leader ", leaderLatch.getId());
                    }

                    @Override
                    public void notLeader() {
                        log.info("notLeader callback : {} is not Leader ", leaderLatch.getId());
                    }
                });
                leaderLatches.add(leaderLatch);
            }
            // LeaderLatch启动
            for (LeaderLatch latch : leaderLatches) {
                new Thread(() -> {
                    try {
                        latch.start();
                        latch.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    log.info("{} 选举完成!", latch.getId());
                }).start();
            }

            // 睡眠一段时间等待选举完成。
            TimeUnit.SECONDS.sleep(30);
            // 查看状态
            //for (LeaderLatch latch : leaderLatches) {
            //    log.info("id={}, isLeader={}", latch.getId(), latch.hasLeadership());
            //    if (latch.hasLeadership()) {
            //        log.info("{} 关闭", latch.getId());
            //        latch.close();
            //    }
            //}

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭LeaderLatch
            for (LeaderLatch leaderLatch : leaderLatches) {
                CloseableUtils.closeQuietly(leaderLatch);
            }
            // 关闭CuratorFramework
            for (CuratorFramework curatorFramework : curatorFrameworks) {
                CloseableUtils.closeQuietly(curatorFramework);
            }
        }
        while (true) {

        }
    }
}
