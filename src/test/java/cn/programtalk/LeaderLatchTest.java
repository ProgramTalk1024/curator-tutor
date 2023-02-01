package cn.programtalk;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

@Slf4j
public class LeaderLatchTest {
    @Test
    public void testLeaderLatch() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, "/LeaderLatch", "node-" + System.currentTimeMillis(), LeaderLatch.CloseMode.NOTIFY_LEADER);
        leaderLatch.addListener(new LeaderLatchListener() {
            /**
             * 当 LeaderLatch 的状态从 hasLeadership = false 变为 hasLeadership = true 时，将调用此值。
             * 请注意，当此方法调用发生时，hasLeadership 可能已回退到 false。如果发生这种情况，notLeader() 也会被调用。
             */
            @SneakyThrows
            @Override
            public void isLeader() {
                log.info("isLeader callback : {} is Leader ", leaderLatch.getId());
            }

            /**
             * 当 LeaderLatch 的状态从 hasLeadership = true 变为 hasLeadership = false 时，将调用此值。
             * 请注意，当此方法调用发生时，hasLeadership 可能已经变为真。如果发生这种情况，isLeader() 也会被调用。
             */
            @SneakyThrows
            @Override
            public void notLeader() {
                log.info("notLeader callback : {} is not Leader ", leaderLatch.getId());
            }
        });
        leaderLatch.start();
        leaderLatch.await();
        log.info("{} 只有leader才会执行 ", leaderLatch.getId());

        while (true) {

        }
    }

}
