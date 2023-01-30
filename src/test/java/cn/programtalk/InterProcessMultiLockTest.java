package cn.programtalk;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
public class InterProcessMultiLockTest {
    String connectString = "172.24.246.68:2181";
    RetryPolicy retryPolicy = new RetryForever(1000);

    @Test
    public void testInterProcessMultiLock1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessMultiLock lock  = new InterProcessMultiLock(curatorFramework, List.of("/InterProcessMultiLock1", "/InterProcessMultiLock2"));
        lock.acquire();
        log.info("读成功");
        lock.release();
    }

    @Test
    public void testInterProcessMultiLock2() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        List<InterProcessLock> mutexes = Lists.newArrayList();
        InterProcessMutex interProcessMutex = new InterProcessMutex(curatorFramework, "/InterProcessMultiLock3");
        mutexes.add(interProcessMutex);
        InterProcessSemaphoreMutex interProcessSemaphoreMutex = new InterProcessSemaphoreMutex(curatorFramework, "/InterProcessMultiLock4");
        mutexes.add(interProcessSemaphoreMutex);

        InterProcessMultiLock lock  = new InterProcessMultiLock(mutexes);
        lock.acquire();
        log.info("读成功");
        lock.release();
    }
}
