package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.curator.x.async.modeled.ModelSpec;
import org.apache.curator.x.async.modeled.ModeledFramework;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.apache.zookeeper.CreateMode.PERSISTENT_SEQUENTIAL;

@Slf4j
public class CuratorAsyncTest {
    /**
     * 创建节点，并监听
     */
    @Test
    public void testAsyncCreate1() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.29.240.53:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        // 将CuratorFramework转化为AsyncCuratorFramework
        AsyncCuratorFramework asyncCuratorFramework = AsyncCuratorFramework.wrap(curatorFramework);
        // 以下功能是创建一个持久有序节点，并且监听该节点，监听器中打印节点的值。
        asyncCuratorFramework.create().withMode(PERSISTENT_SEQUENTIAL).forPath("/async").thenAccept(actualPath -> {
            asyncCuratorFramework.watched().getData().forPath(actualPath).thenApply((bytes) -> {
                String x = new String(bytes);
                System.out.println(x); // 10.112.33.229
                return x;
            });
        });
        while (true) {

        }
    }
}
