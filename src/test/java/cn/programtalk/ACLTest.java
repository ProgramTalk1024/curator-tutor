package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.ACL;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ACLTest {
    String connectString = "172.20.98.4:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 创建节点
     */
    @Test
    public void testAcl1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        List<ACL> acls = curatorFramework.getACL().forPath("/test");
        acls.forEach(acl -> System.out.println(acl.getId() + " " + acl.getPerms()));
    }

}
