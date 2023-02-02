package cn.programtalk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

public class ServiceDiscoveryServerTest {
    @Test
    public void testServer() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.29.240.53:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();

    }
}
