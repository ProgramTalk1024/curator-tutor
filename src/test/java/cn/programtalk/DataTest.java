package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class DataTest {
    String connectString = "172.20.98.4:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 查询节点的值
     * @throws Exception
     */
    @Test
    public void testGetData() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        byte[] bytes = curatorFramework.getData().forPath("/test");
        System.out.println("/test节点的值是:" + new String(bytes, StandardCharsets.UTF_8));
    }

    /**
     * 设置节点的值
     * @throws Exception
     */
    @Test
    public void testGetData2() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        byte[] bytes = curatorFramework.getData().forPath("/test");
        System.out.println("/test节点的原始值是:" + new String(bytes, StandardCharsets.UTF_8));
        curatorFramework.setData().forPath("/test", "updated".getBytes(StandardCharsets.UTF_8));
        bytes = curatorFramework.getData().forPath("/test");
        System.out.println("/test节点的新值是:" + new String(bytes, StandardCharsets.UTF_8));
    }
}
