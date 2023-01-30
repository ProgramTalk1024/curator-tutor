package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateNodeTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 创建节点
     */
    @Test
    public void testCreate1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        curatorFramework.create().forPath("/test");
    }

    /**
     * 默认值
     * @throws Exception
     */
    @Test
    public void testCreateDefaultData() throws Exception {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().defaultData("默认值".getBytes(StandardCharsets.UTF_8));
        CuratorFramework client = builder.connectString(connectString).retryPolicy(retryPolicy).build();
        client.start();
        client.create().forPath("/defaultDataTest");
    }

    /**
     * 设置节点值
     * @throws Exception
     */
    @Test
    public void testCreate2() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        curatorFramework.create().forPath("/test", "用户自己设置的值".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 设置节点类型(临时节点)
     * @throws Exception
     */
    @Test
    public void testCreate3() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath("/EPHEMERAL1");
        // 临时节点，会话结束就会删除，线程睡眠用于延长会话时间
        TimeUnit.SECONDS.sleep(30);
    }

    /**
     * 设置节点类型（容器节点）
     * @throws Exception
     */
    @Test
    public void testCreate4() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        curatorFramework.create().withMode(CreateMode.CONTAINER).forPath("/c1");
        curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/c1/c2-1");
        curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/c1/c2-2");
    }

    /**
     * 测试ttl
     * @throws Exception
     */
    @Test
    public void testCreate5() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        curatorFramework.create().withTtl(10000).withMode(CreateMode.PERSISTENT_WITH_TTL).forPath("/ttl1");
    }

    /**
     * 名称空间
     * @throws Exception
     */
    @Test
    public void testCreate6() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        CuratorFramework c2 = curatorFramework.usingNamespace("namespace1");
        c2.create().forPath("/node1");
        c2.create().forPath("/node2");
    }

    /**
     * 测试acl
     * @throws Exception
     */
    @Test
    public void testCreate7() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        List<ACL> aclList = new ArrayList<>();
        ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);
        aclList.add(acl);
        curatorFramework.create().withACL(aclList).forPath("/acl1");
    }

}
