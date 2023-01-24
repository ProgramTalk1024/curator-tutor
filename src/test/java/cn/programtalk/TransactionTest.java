package cn.programtalk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TransactionTest{
    String connectString = "172.20.98.4:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 查询客户端状态
     * @throws Exception
     */
    @Test
    public void testTransaction() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        CuratorOp createOp = curatorFramework.transactionOp().create().forPath("/transaction1");
        CuratorOp setDataOp = curatorFramework.transactionOp().setData().forPath("/transaction2", "transaction2".getBytes(StandardCharsets.UTF_8));
        CuratorOp deleteOp = curatorFramework.transactionOp().delete().forPath("/transaction3");

        List<CuratorTransactionResult> result = curatorFramework.transaction().forOperations(createOp, setDataOp, deleteOp);
        result.forEach(rt -> System.out.println(rt.getForPath() + "---" + rt.getType()));
    }
}
