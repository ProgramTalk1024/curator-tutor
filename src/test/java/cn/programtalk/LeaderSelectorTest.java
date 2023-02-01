package cn.programtalk;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class LeaderSelectorTest {
    @SneakyThrows
    @Test
    public void testLeaderSelector() {
        CuratorFramework curatorFramework = null;
        MyLeaderSelectorListener listener = null;
        try {
            curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new ExponentialBackoffRetry(1000, 3));
            curatorFramework.start();

            String name = "client-" + System.currentTimeMillis();
            log.info("节点名称={}", name);
            listener = new MyLeaderSelectorListener(curatorFramework, "/LeaderSelector", name);
            listener.start();

            while (true) {

            }
        } finally {
            CloseableUtils.closeQuietly(listener);
            CloseableUtils.closeQuietly(curatorFramework);
        }
    }

    /**
     * IMPORTANT: The recommended action for receiving SUSPENDED or LOST is to throw CancelLeadershipException. This will cause the LeaderSelector instance to attempt to interrupt and cancel the thread that is executing the takeLeadership method. Because this is so important, you should consider extending LeaderSelectorListenerAdapter. LeaderSelectorListenerAdapter has the recommended handling already written for you.
     */
    @Slf4j
    static class MyLeaderSelectorListener extends LeaderSelectorListenerAdapter implements Closeable {
        private final String name;
        private final LeaderSelector leaderSelector;

        // 用于控制takeLeadership方法不返回（一直阻塞）
        private static final CountDownLatch LATCH = new CountDownLatch(1);

        public MyLeaderSelectorListener(CuratorFramework curatorFramework, String path, String name) {
            this.name = name;
            // 所有节点选举必须是同一个path
            leaderSelector = new LeaderSelector(curatorFramework, path, this);
            // 设置leaderSelector的存储Id
            leaderSelector.setId(name);
            // 放弃领导权时重新排队
            leaderSelector.autoRequeue();
        }

        public void start() throws Exception {
            // 异步的
            leaderSelector.start();
        }

        @Override
        public void close() {
            leaderSelector.close();
        }

        @Override
        public void takeLeadership(CuratorFramework client) throws Exception {
            log.info("{} 成为Leader", name);
            // 控制该方法不返回，如果返回则释放了Leader, 不管你用什么代码实现，只要方法不返回，该leader就不会释放。 特别重要一定要注意！！！
            LATCH.await();
        }
    }
}
