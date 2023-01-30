package cn.programtalk;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.atomic.PromotedToLock;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryOneTime;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class DistributedAtomicLongTest {
    @SneakyThrows
    @Test
    public void testDistributedAtomicLong1() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new RetryForever(1000));
        curatorFramework.start();
        DistributedAtomicLong distributedAtomicLong = new DistributedAtomicLong(curatorFramework, "/DistributedAtomicLong", new RetryForever(1000));
        AtomicValue<Long> longAtomicValue = distributedAtomicLong.get();
        log.info("1. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // 设置初始值，如果节点已经存在，则会返回false.
        boolean succeed = distributedAtomicLong.initialize(0L);
        log.info("initialize succeed? {}", succeed);
        longAtomicValue = distributedAtomicLong.get();
        log.info("2. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // add 将增量添加到当前值并返回新值信息。请记住始终检查 AtomicValue.succeeded().
        distributedAtomicLong.add(10L);
        longAtomicValue = distributedAtomicLong.get();
        log.info("3. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // subtract 从当前值中减去增量并返回新值信息。请记住始终检查 AtomicValue.succeeded().
        distributedAtomicLong.subtract(1L);
        longAtomicValue = distributedAtomicLong.get();
        log.info("4. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // increment 加一
        distributedAtomicLong.increment();
        longAtomicValue = distributedAtomicLong.get();
        log.info("5. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // decrement 减一
        distributedAtomicLong.decrement();
        longAtomicValue = distributedAtomicLong.get();
        log.info("6. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
    }

    @SneakyThrows
    @Test
    public void testDistributedAtomicLong2() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.24.246.68:2181", new RetryForever(1000));
        curatorFramework.start();
        DistributedAtomicLong distributedAtomicLong;
        distributedAtomicLong = new DistributedAtomicLong(curatorFramework, "/DistributedAtomicLong"
                , new RetryForever(1000)
                , PromotedToLock.builder().lockPath("/DistributedAtomicLongPromotedToLock").timeout(3000, TimeUnit.MILLISECONDS).retryPolicy(new RetryOneTime(1000)).build());
        AtomicValue<Long> longAtomicValue = distributedAtomicLong.get();
        log.info("1. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // 设置初始值，如果节点已经存在，则会返回false.
        boolean succeed = distributedAtomicLong.initialize(0L);
        log.info("initialize succeed? {}", succeed);
        longAtomicValue = distributedAtomicLong.get();
        log.info("2. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // add 将增量添加到当前值并返回新值信息。请记住始终检查 AtomicValue.succeeded().
        distributedAtomicLong.add(10L);
        longAtomicValue = distributedAtomicLong.get();
        log.info("3. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // subtract 从当前值中减去增量并返回新值信息。请记住始终检查 AtomicValue.succeeded().
        distributedAtomicLong.subtract(1L);
        longAtomicValue = distributedAtomicLong.get();
        log.info("4. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // increment 加一
        distributedAtomicLong.increment();
        longAtomicValue = distributedAtomicLong.get();
        log.info("5. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
        // decrement 减一
        distributedAtomicLong.decrement();
        longAtomicValue = distributedAtomicLong.get();
        log.info("6. preValue={}, postValue={}, succeeded={}", longAtomicValue.preValue(), longAtomicValue.postValue(), longAtomicValue.succeeded());
    }
}
