package cn.programtalk;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RoundRobinStrategy;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;

@Slf4j
public class ServiceDiscoveryTest {
    /**
     * 实例元数据类
     */
    @Data
    static class InstanceMetadata implements Serializable {
        private String url;
        private String name;

        @Override
        public String toString() {
            return "InstanceMetadata{" +
                    "url='" + url + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    /**
     * 服务注册，模拟三个服务
     *
     * @throws Exception
     */
    @Test
    public void testRegisterService() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.29.240.53:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        // 未成功连接Zk前一直阻塞
        curatorFramework.blockUntilConnected();
        Random random = new Random();
        InstanceMetadata instanceMetadata = new InstanceMetadata();
        instanceMetadata.setUrl(random.nextInt(1000) + "");
        instanceMetadata.setName("产品服务");
        // 定义ServiceDiscovery
        ServiceDiscovery<InstanceMetadata> serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
                // zk路径
                .basePath("/base-path")
                // zk客户端
                .client(curatorFramework)
                //.thisInstance(serviceInstance)
                // 是否监听实例
                .watchInstances(true)
                // 序列化
                .serializer(new JsonInstanceSerializer<>(InstanceMetadata.class))
                .build();
        // 一个服务（产品服务）, 有三个节点。
        // 服务名称
        String serviceName = "PRODUCT";
        for (int i = 0; i < 3; i++) {
            String address = "127.0.0.1";
            String id = "" + i;
            ServiceInstance<InstanceMetadata> serviceInstance = ServiceInstance.<InstanceMetadata>builder()
                    // IP或者域名地址
                    .address(address)
                    // 实例载体，就是path的值
                    .payload(instanceMetadata)
                    // 实例Id
                    .id(id)
                    // 端口，仅仅是模拟，不要管是否是合法端口
                    .port(i)
                    // SSL端口
                    .sslPort(i)
                    // 服务名称
                    .name(serviceName)
                    // 统一资源标识符
                    .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
                    // 构建方法
                    .build();
            serviceDiscovery.registerService(serviceInstance);
        }
        // 启动（必须）
        serviceDiscovery.start();
        while (true) {

        }
    }

    /**
     * 消费者，获取服务
     *
     * @throws Exception
     */
    @Test
    public void testGetService() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.29.240.53:2181", new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        // 未成功连接Zk前一直阻塞
        curatorFramework.blockUntilConnected();

        // 定义ServiceDiscovery
        ServiceDiscovery<InstanceMetadata> serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceMetadata.class)
                // zk路径
                .basePath("/base-path")
                // zk客户端
                .client(curatorFramework)
                // 是否监听实例
                .watchInstances(true)
                // 序列化
                .serializer(new JsonInstanceSerializer<>(InstanceMetadata.class))
                .build();
        // 服务名
        String serviceName = "PRODUCT";
        ServiceProvider<InstanceMetadata> serviceProvider = serviceDiscovery.serviceProviderBuilder().serviceName(serviceName)
                .providerStrategy(new RoundRobinStrategy<>())
                .build();
        serviceProvider.start();
        //根据名称获取服务
        log.info("获取PRODUCT的所有实例：");
        Collection<ServiceInstance<InstanceMetadata>> instances = serviceProvider.getAllInstances();
        for (ServiceInstance<InstanceMetadata> instance : instances) {
            log.info("服务名称={}，服务类型={}， 服务id={}， 载体={}， 端口={}， SSL端口={}, 地址={}， 注册时间={}", instance.getName()
                    , instance.getServiceType(), instance.getId(), instance.getPayload(), instance.getPort()
                    , instance.getSslPort(), instance.getAddress(), instance.getRegistrationTimeUTC());
        }
        log.info("获取PRODUCT的单个实例（根据serviceProvider的策略）：");
        ServiceInstance<InstanceMetadata> instance = serviceProvider.getInstance();
        log.info("服务名称={}，服务类型={}， 服务id={}， 载体={}， 端口={}， SSL端口={}, 地址={}， 注册时间={}", instance.getName()
                , instance.getServiceType(), instance.getId(), instance.getPayload(), instance.getPort()
                , instance.getSslPort(), instance.getAddress(), instance.getRegistrationTimeUTC());

    }
}