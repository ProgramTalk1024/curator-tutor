package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

public class GetStateTest {
    String connectString = "172.20.98.4:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 获取状态
     * @throws Exception
     */
    @Test
    public void testGetState() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        CuratorFrameworkState state = curatorFramework.getState();
        System.out.println("状态是" + state); // 状态是LATENT
        curatorFramework.start();
        state = curatorFramework.getState();
        System.out.println("状态是" + state); // 状态是STARTED
        curatorFramework.close();
        state = curatorFramework.getState();
        System.out.println("状态是" + state); // 状态是STOPPED
    }
}
