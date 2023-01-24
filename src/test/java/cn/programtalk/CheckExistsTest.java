package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;

public class CheckExistsTest {
    String connectString = "172.20.98.4:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 查询客户端状态
     * @throws Exception
     */
    @Test
    public void testGetState() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        Stat stat = curatorFramework.checkExists().forPath("/namespace1");
        System.out.println(stat);
    }
}
