![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301251143816.png)

# Apache Curator Framework框架学习
Apache Curator 是 Apache ZooKeeper（分布式协调服务）的 Java/JVM 客户端库。它包括一个高级API框架和实用程序，使使用Apache ZooKeeper变得更加容易和可靠。

![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301131005419.png)



![img](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301251038136.png)

# 依赖
curator 有很多的依赖，比如如下是maven依赖官方说明
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111130055.png)
一般情况下只要引入`curator-recipes`基本就够用。他包含了`client`和`framework`的依赖，会自动下载下来。
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111131555.png)
# 创建项目并引入依赖
pom文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.itlab1024</groupId>
    <artifactId>curator-framework-tutorial</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>5.4.0</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.24</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.6</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```
# 创建连接
curator主要通过工厂类`CuratorFrameworkFactory`的`newClient`方法创建连接
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111300623.png)
有三种多态方法。
```text
public static CuratorFramework newClient(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy, ZKClientConfig zkClientConfig)
public static CuratorFramework newClient(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy)
public static CuratorFramework newClient(String connectString, RetryPolicy retryPolicy)
```
参数说明：
* connectString：连接字符串，服务器访问地址例如localhost:2181(注意是IP（域名）+ 端口),如果是集群地址，则用逗号(,)隔开即可。
* sessionTimeoutMs：会话超时时间，单位毫秒，如果不设置则先去属性中找`curator-default-session-timeout`的值，如果没设置，则默认是60 * 1000毫秒。
* connectionTimeoutMs：连接超时时间，单位毫秒，如果不设置则先去属性中找`curator-default-connection-timeout`的值，如果没设置，则默认是15 * 1000毫秒。
* RetryPolicy：重试策略，后面具体讲解。

使用Java代码创建连接并创建一个节点
```java
package cn.programtalk.connection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

public class ConnectionTest {
    /**
     * 创建连接
     */
    @Test
    public void TestConnection1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("172.30.140.89:2181",  new ExponentialBackoffRetry(1000,3));
        curatorFramework.start();
        curatorFramework.create().forPath("/test");
    }
}
```
运行完毕后查看结果：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111344287.png)



也可以使用其`builder()`建造者模式构建client。

```java
@Test
public void TestConnection2() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.builder().connectString("172.20.98.4:2181")
        .retryPolicy(new ExponentialBackoffRetry(1000,3))
        .sessionTimeoutMs(1000)
        .connectionTimeoutMs(10000)
        .build();
    curatorFramework.start();
    curatorFramework.create().forPath("/test");
}
```



# 重试策略

重试策略有几种实现，可以通过如下类图直观地展示出来。

![image-20230121185013815](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301211850924.png)

## RetryForever

该策略是永远尝试。

`new RetryForever(2000)`参数是毫秒，代表间隔多久进行重试！

```java
package cn.programtalk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

public class RetryTest {
    /**
     * RetryForever
     */
    @Test
    public void testRetryForever() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181",  new RetryForever(2000));
        curatorFramework.start();
    }
}
```

## SessionFailedRetryPolicy

session超时重试策略，其构造方法是`SessionFailedRetryPolicy(RetryPolicy delegatePolicy)`，参数就是也是一个重试策略，其含义就是说会话超时的时候使用哪种具体的重试策略。

```java
public void testSessionFailedRetryPolicy() throws Exception {
        RetryPolicy sessionFailedRetryPolicy = new SessionFailedRetryPolicy(new RetryForever(1000));

        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(sessionFailedRetryPolicy)
                .build();
        curatorFramework.start();
        TimeUnit.DAYS.sleep(1);
    }
```
session超时后，会尝试重新连接。
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301241657836.png)



## RetryNTimes

重试N次策略：`public RetryNTimes(int n, int sleepMsBetweenRetries)`，第一个是重试次数，第二个参数是每次重试间隔多少毫秒。

```java
@Test
public void testRetryNTimes() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", new RetryNTimes(5, 1000));
    curatorFramework.start();
    TimeUnit.DAYS.sleep(1);
}
```

上面的代码就是重试5次，重试间隔1000毫秒。

## RetryOneTime

重试一次，他是RetryNTime的特例，N=1的情况。

```java
@Test
public void testRetryOneTime() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", new RetryOneTime(1000));
    curatorFramework.start();
    TimeUnit.DAYS.sleep(1);
}
```



## RetryUntilElapsed

`public RetryUntilElapsed(int maxElapsedTimeMs, int sleepMsBetweenRetries)`

一直重试直到达到规定的时间，`maxElapsedTimeMs`：最大重试时间，`sleepMsBetweenRetries`每次重试间隔时间。

```java
@Test
public void testRetryUntilElapsed() throws Exception {
    RetryUntilElapsed retryPolicy = new RetryUntilElapsed(3000, 1000);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", retryPolicy);
    curatorFramework.start();
    TimeUnit.DAYS.sleep(1);
}
```



## ExponentialBackoffRetry

按照设定的次数重试，每次重试之间的睡眠时间都会增加。

构造方法如下：

```java
public ExponentialBackoffRetry(int baseSleepTimeMs, int maxRetries)
{
    this(baseSleepTimeMs, maxRetries, DEFAULT_MAX_SLEEP_MS);
}
```

`baseSleepTimeMs`：重试间隔毫秒数

`maxRetries`：最大重试次数

```java
@Test
public void testExponentialBackoffRetry() throws Exception {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 1000);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", retryPolicy);
    curatorFramework.start();
    TimeUnit.DAYS.sleep(1);
}
```

## BoundedExponentialBackoffRetry

重试策略，该策略重试设定的次数，重试之间的休眠时间增加（最多达到最大限制）

`BoundedExponentialBackoffRetry`继承`ExponentialBackoffRetry`，相比与`ExponentialBackoffRetry`，它增加了最大休眠时间的设置。

构造方法如下：

```java
public BoundedExponentialBackoffRetry(int baseSleepTimeMs, int maxSleepTimeMs, int maxRetries)
{
    super(baseSleepTimeMs, maxRetries);
    this.maxSleepTimeMs = maxSleepTimeMs;
}
```

示例如下：

```java
@Test
public void testBoundedExponentialBackoffRetry() throws Exception {
    RetryPolicy retryPolicy = new BoundedExponentialBackoffRetry(3000, 6000, 1000);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("unknownHost:2181", retryPolicy);
    curatorFramework.start();
    TimeUnit.DAYS.sleep(1);
}
```



# **名称空间（Namespace）**

curator中名称空间的含义，就是设置一个公共的父级path，之后的操作全部都是基于该path。

```java
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
```

查看运行结果：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121316314.png)



# CRUD基础

## 创建节点

创建节点使用`create`方法，该方法返回一个`CreateBuilder`他是一个建造者模式的类。用于创建节点。
```java
package cn.programtalk.connection;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

public class CreateNodeTest {
    String connectString = "172.30.140.89:2181";
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
}
```
创建完毕后，通过命令行查看节点：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111419073.png)
看到值是`10.112.33.229`,可实际上我并未给节点设置值，这个值是框架默认设置的，客户端的IP。
这个默认值可以修改，此时不能使用`newClient`方法，需要使用工厂的builder自己构建设置。示例代码如下：
```java
@Test
public void testCreateDefaultData() throws Exception {
    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().defaultData("默认值".getBytes(StandardCharsets.UTF_8));
    CuratorFramework client = builder.connectString(connectString).retryPolicy(retryPolicy).build();
    client.start();
    client.create().forPath("/defaultDataTest");
}
```
运行结果：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111424095.png)
可以看到，默认值已经被修改为`默认值`。

创建节点时如果节点存在，则会抛出`NodeExistsException`异常
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111410229.png)

### **使用forPath设置节点的值**

forPath还接收第二个参数（节点的值，字节数组类型）
```java
@Test
public void testCreate2() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    curatorFramework.start();
    curatorFramework.create().forPath("/test2", "用户自己设置的值".getBytes(StandardCharsets.UTF_8));
}
```
运行结果：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111427929.png)
可见正确设置了值。

### **节点模式设置**

可以通过`withMode`方法设置节点的类型，为显示指定的节点都是持久性节点。

```java
/**
 * 设置节点类型
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
```
查看结果：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301111512167.png)
可以看到临时节点，红色框内只有临时节点该属性才是非零。

### **TTL时长设置**

使用`withTtl`设置时长，单位毫秒。当模式为 CreateMode.PERSISTENT_WITH_TTL 或CreateMode.PERSISTENT_SEQUENTIAL_WITH_TTL时指定 TTL。必须大于 0 且小于或等于 EphemeralType.MAX_TTL。
```java
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
```
可能出现如下错误：
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121306568.png)
这是因为TTL默认是关闭的，需要打开（zoo.cfg中设置`extendedTypesEnabled=true`）。
再次运行：
```shell
[zk: localhost:2181(CONNECTED) 8] ls /
[defaultDataTest, hiveserver2_zk, test, test2, ttl1, zookeeper]
#等待10秒后再次查看，ttl1节点自动被删除。
[zk: localhost:2181(CONNECTED) 9] ls /
[defaultDataTest, hiveserver2_zk, test, test2, zookeeper]
```
### ACL权限

创建节点时设置ACL，主要通过`withACL`方法设置，接收一个`List<ACL>`类型的参数。

`ACL`实例对象，通过该类的构造方法创建，类似`ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE);`

```java
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
```

运行结果：

![image-20230112153655747](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121536793.png)

运行完毕后，通过命令行查看权限，可以看到已经设置成功。

如果不设置ACL，默认则是`new ACL(Perms.ALL, ANYONE_ID_UNSAFE)`。

## 查询值

查询数据使用`getData`方法。

```java
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
```

结果：

![命令行查询](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121347952.png)



![api查询结果](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121347616.png)

## 设置值

使用`setData`，配合`forpath`方法。

```java
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
```

运行结果是：

![image-20230112150323445](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121503512.png)

## 获取孩子节点

```java
/**
 * 获取孩子节点
 * @throws Exception
 */
@Test
public void testGetState() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    curatorFramework.start();
    List<String> children = curatorFramework.getChildren().forPath("/namespace1");
    children.forEach(System.out::println);
}
```

运行结果：

![image-20230112152418448](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121524560.png)

![image-20230112152442481](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121524557.png)

## 获取ACL

```java
package cn.programtalk.connection;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ACLTest {
    String connectString = "172.30.140.89:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 获取Acl列表
     */
    @Test
    public void testAcl1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        List<ACL> acls = curatorFramework.getACL().forPath("/test");
        acls.forEach(acl -> System.out.println(acl.getId() + " " + acl.getPerms()));
    }

}
```

运行结果：

![image-20230112153012559](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121530612.png)

## 删除节点

使用`delete`，搭配`forPath`方法，删除指定的节点。

```java
package cn.programtalk.connection;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DeleteNodeTest {
    String connectString = "172.30.140.89:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 删除节点
     * @throws Exception
     */
    @Test
    public void testDelete1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        curatorFramework.delete().forPath("/test");
    }
}
```

程序执行完毕后，通过命令行查询`/test`可知已经被删除。

![image-20230112154133228](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121541279.png)

如果被删除的节点有孩子节点，则无法删除，抛出`NotEmptyException`。



**那么如何删除包含子节点的节点呢？需要使用`deletingChildrenIfNeeded`方法**

```java
/**
 * 删除节点（包含子节点）
 * @throws Exception
 */
@Test
public void testDelete2() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    curatorFramework.start();
    curatorFramework.delete().deletingChildrenIfNeeded().forPath("/namespace1");
}
```

运行后，查看该节点



![image-20230112182019037](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121820090.png)



节点已经被删除。并且级联删除了子节点。



## 检查节点是否存在

使用`checkExists()`搭配`forPath`来实现，返回一个`Stat`对象信息。

```
package cn.programtalk.connection;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;

public class CheckExistsTest {
    String connectString = "172.30.140.89:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 检查是否存在
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
```

运行结果：

![image-20230112173212601](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121732695.png)



`stat`的具体信息如下：



![image-20230112173310084](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121733222.png)

## 查看会话状态

使用`getState()`。

```java
package cn.programtalk.connection;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetStateTest {
    String connectString = "172.30.140.89:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

    /**
     * 查询客户端状态
     * @throws Exception
     */
    @Test
    public void testGetState() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        CuratorFrameworkState state = curatorFramework.getState();
        System.out.println("状态是" + state); // 状态是LATENT
        curatorFramework.start();
        state = curatorFramework.getState();
        System.out.println("状态是" + state); // 状态是STARTED
        curatorFramework.close();
        state = curatorFramework.getState();
        System.out.println("状态是" + state); // 状态是STOPPED
    }
}
```

# 事务



```java
package cn.programtalk.connection;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetConfigBuilder;
import org.apache.curator.framework.api.transaction.CuratorMultiTransaction;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public class TransactionTest{
    String connectString = "172.30.140.89:2181";
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
```

运行程序前先看下zk节点情况

![image-20230112175631300](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121756369.png)

可以看到没有`transaction1`和`transaction2`和`transaction3`。

运行程序会出现如下异常。

![image-20230112175722403](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121757512.png)



出现异常则事务应该回滚，也就是说`transaction1`节点不应该创建成功。

![image-20230112175815921](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121758968.png)

通过上图可知确实没有创建成功。

接下来我通过命令长创建`/transaction2`和`/transaction3`这两个节点。

![image-20230112180734954](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121807011.png)

创建完毕，并且可以看到`/transaction2`节点的值是`null`。

重新运行程序后，不会发生异常。



![image-20230112180839996](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121808093.png)



通过命令行看下事务是否完全执行成功。

![image-20230112180946262](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121809319.png)

可以看到`/transaction1`节点创建成功，`/transaction2`节点的值修改成功。`/transaction3`节点被删除。说明事务是有效的！



---

为了演示清晰，我先清理掉所有节点。

![image-20230112195536876](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301121955942.png)



# 监听节点

本版本中`PathChildrenCache`、`NodeCache`、`TreeCache`都已经是过期的了，官方推荐使用`CuratorCache`。

并且api风格也更改了，改为了流式风格。

`CuratorCacheListener`提供了多种监听器，比如`forInitialized`，`forCreates`等。

```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CacheTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);


    /**
     *
     * @throws Exception
     */
    @Test
    public void testCache1() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        CuratorCache curatorCache = CuratorCache.builder(curatorFramework, "/ns1").build();
        CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
                .forInitialized(() -> {
                    log.info("forInitialized回调");
                    log.info("--------");
                })

                .forCreates(childData -> {
                    log.info("forCreates回调执行, path=[{}], data=[{}], stat=[{}]", childData.getPath(), Objects.isNull(childData.getData()) ? null : new String(childData.getData(), StandardCharsets.UTF_8), childData.getStat());
                    log.info("--------");
                })

                .forNodeCache(() -> {
                    log.info("forNodeCache回调");
                    log.info("--------");
                })

                .forChanges((oldNode, node) -> {
                    log.info("forChanges回调, oldNode.path=[{}], oldNode.data=[{}], oldNode.stat=[{}], node.path=[{}], node.data=[{}], node.stat=[{}]", oldNode.getPath(), Objects.isNull(oldNode.getData()) ? null : new String(oldNode.getData(), StandardCharsets.UTF_8), oldNode.getStat(), node.getPath(), Objects.isNull(node.getData()) ? null : new String(node.getData(), StandardCharsets.UTF_8), node.getStat());
                    log.info("--------");
                })

                .forDeletes(childData -> {
                    log.info("forDeletes回调执行, path=[{}], data=[{}], stat=[{}]", childData.getPath(), Objects.isNull(childData.getData()) ? null : new String(childData.getData(), StandardCharsets.UTF_8), childData.getStat());
                    log.info("--------");
                })

                .forAll((type, oldNode, node) -> {
                    log.info("forAll回调");
                    log.info("type=[{}]", type);
                    if (Objects.nonNull(oldNode)) {
                        log.info("oldNode.path=[{}], oldNode.data=[{}], oldNode.stat=[{}]", oldNode.getPath(), Objects.isNull(oldNode.getData()) ? null : new String(oldNode.getData(), StandardCharsets.UTF_8), oldNode.getStat());
                    }
                    if (Objects.nonNull(node)) {
                        log.info("node.path=[{}], node.data=[{}], node.stat=[{}]", node.getPath(), Objects.isNull(node.getData()) ? null : new String(node.getData(), StandardCharsets.UTF_8), node.getStat());
                    }
                    log.info("--------");
                })

                .forCreatesAndChanges((oldNode, node) -> {
                    log.info("forCreatesAndChanges回调");
                    if (Objects.nonNull(oldNode)) {
                        log.info("oldNode.path=[{}], oldNode.data=[{}], oldNode.stat=[{}]", oldNode.getPath(), Objects.isNull(oldNode.getData()) ? null : new String(oldNode.getData(), StandardCharsets.UTF_8), oldNode.getStat());
                    }
                    if (Objects.nonNull(node)) {
                        log.info("node.path=[{}], node.data=[{}], node.stat=[{}]", node.getPath(), Objects.isNull(node.getData()) ? null : new String(node.getData(), StandardCharsets.UTF_8), node.getStat());
                    }
                    log.info("--------");
                })
                .build();
        // 获取监听器列表容器
        Listenable<CuratorCacheListener> listenable = curatorCache.listenable();
        // 将监听器放入容器中
        listenable.addListener(curatorCacheListener);
        // curatorCache必须启动
        curatorCache.start();
        // 延时，以保证连接不关闭
        TimeUnit.DAYS.sleep(10);
        curatorCache.close();
    }
}
```

上面的代码就是创建监听节点的核心代码。

> 以前的监听类型是不同的类（过期的类）实现的。现在是通过不同的forXXX方法指定的（例如：`forInitialized`）。

**在测试前我将zk中的数据清理掉**

```shell
[zk: localhost:2181(CONNECTED) 5] ls /
[zookeeper]
```

可以看到完全清理掉了。

## API说明

在测试之前要简单地说明下API的基本使用方式。curator监听主要有如下几个主要的类。

* `CuratorFrameworkFactory`这是简单的静态工厂类，用于创建连接zk的客户端（client），里面提供了`newClient`的多态方法，也可以使用`builder`建造者模式类创建客户端。

  ```java
  String connectString = "localhost:2181";
  RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
  // 使用newClient方法
  CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
  // 也可以使用静态builder()方法
  CuratorFramework curatorFramework2 = CuratorFrameworkFactory.builder().connectString(connectString).retryPolicy(retryPolicy).build();
  ```

* `CuratorCache`类，该类也有提供builder方法

  ```java
  CuratorCache curatorCache = CuratorCache.builder(curatorFramework, "/ns1").build();
  ```

  也提供了build方法，可以像下面这样使用。

  ```java
  CuratorCache curatorCache = CuratorCache.builder(curatorFramework, "/ns1").build();
          curatorCache= CuratorCache.build(curatorFramework, "/ns1", CuratorCache.Options.SINGLE_NODE_CACHE, CuratorCache.Options.COMPRESSED_DATA, CuratorCache.Options.DO_NOT_CLEAR_ON_CLOSE);
  ```

  

* `CuratorCacheListener`监听器类，里面可以定义各种监听器。

## 测试

### 启动

运行上面的示例，会打印如下内容：

![image-20230120132327826](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201323978.png)

可见初始化回调被调用。

### 创建节点

创建CuratorCache监听的节点`/ns1`，需要注意的是此时节点并不存在。

命令行操作如下：

![image-20230120133059820](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201330851.png)

程序输出如下：

![image-20230120203053122](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202030171.png)

我们看到当创建节点的时候有四个回调函数被执行。

**结论：** 当创建节点的时候`forCreates`、`forAll`、`forCreatesAndChanges`被回调。

那么如果再创建子节点情况会是什么样的呢？比如我创建`/ns1/sub1`。

命令行：

![image-20230120134553410](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201345465.png)



控制台：

![image-20230120203145032](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202031086.png)



节点创建监听器，监听类型是`CuratorCacheListener.Type.NODE_CREATED`，创建节点的时候会触发，当创建子节点的时候也会触发	。

**结论：** 创建子节点依然会回调上述所说的四个监听器。

### 修改数据

修改监听的根节点`/ns1`的值

命令行修改值：

![image-20230120185406734](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201854761.png)

控制台输出：

![image-20230120203257819](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202032866.png)

当修改监听根节点`/ns1`的值的时候，`forChanges`、`forAll`、`forCreatesAndChanges`四个监听器被触发。

接下来再修改其子节点的值

![image-20230120185605378](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201856407.png)

控制台输出如下：

![image-20230120203909407](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202039450.png)

依然回调`forChanges`、`forAll`、`forCreatesAndChanges`四个监听器函数。

**结论：** 修改监听节点以及其子节点都会触发`forChanges`、`forAll`、`forCreatesAndChanges`监听器。

### ~~ACL设置~~

命令行：

![image-20230120190131911](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201901941.png)

控制台没有打印回调：

![image-20230120190152818](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201901883.png)

**结论：** 设置ACL不会触发监听器。

### 删除节点

首先我先删除监听节点`/ns1`下的子节点

命令行：

![image-20230120193659179](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201936209.png)

控制台：

![image-20230120204018629](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202040684.png)

删除子节点的时候会触发`forDeletes`、`forNodeCache`、`forAll`执行。

接下来再删除监听根节点`/ns1`。

命令行：

![image-20230120193904291](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301201939345.png)

控制台输出：

![image-20230120204106249](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202041293.png)

跟上面子节点的删除触发的监听器回调一样！

**总结：** 删除监听根节点以及其子节点会触发`forDeletes`、`forAll`监听器。

那么如果我删除的是一个父级节点呢？会出现什么情况？

**因为我之前的实验，删除了`/ns1/sub1`所以重建，重建后使用`deleteall /ns1`**

命令行：

![image-20230120200205459](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202002490.png)



控制台：

![image-20230120204257817](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301202042862.png)



可以看到，级联删除，会多次触发`forDeletes`，根节点和其子节点的删除都会触发。同理`forAll`也会多次触发。

**总结：** 对于节点的删除，无论是单个删除还是级联删除，每个节点的删除都会触发`forDeletes`、`forAll`监听器。

> 那么上面这些总结对吗？起码默认情况是对的！因为缓存我使用这样的方式创建的`CuratorCache curatorCache = CuratorCache.builder(curatorFramework, "/ns1").build();`

## CuratorCache配置

上面的代码中我使用的`CuratorCache curatorCache = CuratorCache.builder(curatorFramework, "/ns1").build();`会导致子节点的操作也会触发监听器，这是因为默认就是如此，当然如果想值监听一个节点，可以使用如下方法（源码如下）：

```java
static CuratorCache build(CuratorFramework client, String path, Options... options)
{
    return builder(client, path).withOptions(options).build();
}
```

第三个参数Options就可以配置。

比如我配置就监听一个节点，就可以按照如下方式创建`CuratorCache `:

```java
CuratorCache curatorCache = CuratorCache.build(curatorFramework, "/ns1", CuratorCache.Options.SINGLE_NODE_CACHE);
```

这里我传递了第三个参数`CuratorCache.Options.SINGLE_NODE_CACHE`。也就实现了只监听`/ns1`节点的功能。

**CuratorCache.Options.SINGLE_NODE_CACHE**：单节点缓存

**CuratorCache.Options.COMPRESSED_DATA**：通过以下方式解压缩数据 org.apache.curator.framework.api.GetDataBuilder.decompressed()

**CuratorCache.Options.DO_NOT_CLEAR_ON_CLOSE**：通常，当缓存通过 关闭 close()时，存储将通过 清除 CuratorCacheStorage.clear()。此选项可防止清除存储。

使用`CuratorCache.builder(curatorFramework, "/ns1").build()`构建的时候，`CuratorCache.Options.SINGLE_NODE_CACHE=FALSE`、`CuratorCache.Options.COMPRESSED_DATA=FALAW`、`CuratorCache.Options.DO_NOT_CLEAR_ON_CLOSE=true`。

通过Debug可以看到对应的配置如下：

![image-20230124211120905](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301242111149.png)

## 说明

上面我使用了命令行搭配代码的方式大致测试了下监听器的类型。接下来详细说明下各种监听器的作用。

### forInitialized

初始化完毕触发，也就是说`CuratorFramework`的`start`方法执行完毕后就会被触发。

### forCreates

触发条件： `CuratorCacheListener.Type.NODE_CREATED`，也就是说被监听节点或者子节点创建就会被触发。

### forChanges

触发条件： ` CuratorCacheListener.Type.NODE_CHANGED`，也就是说被监听节点或者子节点值修改就会被触发。

### forCreatesAndChanges

触发条件： ` CuratorCacheListener.Type.NODE_CREATED 和 CuratorCacheListener.Type.NODE_CHANGED`，也就是说被监听节点或者子节点创建或者值修改就会被触发。

### forDeletes

触发条件： ` CuratorCacheListener.Type.NODE_DELETED`，也就是说被监听节点或者子节点删除就会被触发。

### forAll

触发条件：上面的`forCreates`、`forChanges`、`forCreatesAndChanges`、`forDeletes`触发的时候都会同时触发`forAll`。

### forNodeCache、forTreeCache、forPathChildrenCache

这三个是一种桥接监听器，它允许将旧式监听器`PathChildrenCache`、`NodeCache`、`TreeCache`与`CuratorCache`重用，不过我觉得上面的那些监听器已经能够满足需求，无需使用这三个了。



>  <font color="red">**如果读者有不一样的间接，欢迎留言！！！**</font>



```java
@Test
public void testCache2() throws Exception {
    curatorFramework.start();
    CuratorCache curatorCache = CuratorCache.bridgeBuilder(curatorFramework, "/ns1").build();
    CuratorCacheListener curatorCacheListener = CuratorCacheListener.builder()
        .forNodeCache(() -> {
            log.info("forNodeCache回调");
            log.info("----------------------------------------");
        })
        .forTreeCache(curatorFramework, (client, event) -> {
            log.info("forTreeCache回调");
            log.info("type=[{}], data=[{}], oldData=[{}]", event.getType(), event.getData(), event.getOldData());
            log.info("----------------------------------------");
        })
        .forPathChildrenCache("/test", curatorFramework, (client, event) -> {
            log.info("forPathChildrenCache回调");
            log.info("type=[{}], data=[{}], InitialData=[{}]", event.getType(), event.getData(), event.getInitialData());
            log.info("----------------------------------------");
        })
        .build();
    // 获取监听器列表容器
    Listenable<CuratorCacheListener> listenable = curatorCache.listenable();
    // 将监听器放入容器中
    listenable.addListener(curatorCacheListener);
    // curatorCache必须启动
    curatorCache.start();

    TimeUnit.MILLISECONDS.sleep(500);
    byte[] oldData = "A".getBytes(StandardCharsets.UTF_8);
    byte[] newData = "B".getBytes(StandardCharsets.UTF_8);
    // 创建根节点
    curatorFramework.create().forPath("/ns1", oldData);
    log.info("创建/ns1节点");
    curatorFramework.create().forPath("/ns1/sub1", oldData);
    log.info("创建/ns1/sub1节点");

    // 修改根节点的值
    curatorFramework.setData().forPath("/ns1", newData);
    log.info("修改/ns1节点的值");
    // 修改子节点的值
    curatorFramework.setData().forPath("/ns1/sub1", newData);
    log.info("修改/ns1/sub1节点的值");

    // 删除子节点
    curatorFramework.delete().forPath("/ns1/sub1");
    log.info("删除/ns1/sub1节点");

    // 删除根节点
    curatorFramework.delete().forPath("/ns1");
    log.info("删除/ns1节点");

    curatorCache.close();
}
```

运行日志如下：

```text
INFO forTreeCache回调
INFO type=[INITIALIZED], data=[null], oldData=[null]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO type=[INITIALIZED], data=[null], InitialData=[null]
INFO ----------------------------------------
INFO 创建/ns1节点
INFO 创建/ns1/sub1节点
INFO forNodeCache回调
INFO ----------------------------------------
INFO forTreeCache回调
DEBUG Reading reply session id: 0x10001d2b6b70006, packet:: clientPath:/ns1/sub1 serverPath:/ns1/sub1 finished:false header:: 10,4  replyHeader:: 10,226,0  request:: '/ns1/sub1,F  response:: #41,s{226,226,1674566507592,1674566507592,0,0,0,0,1,0,226} 
INFO type=[NODE_ADDED], data=[ChildData{path='/ns1', stat=225,225,1674566507586,1674566507586,0,1,0,0,1,1,226
, data=[65]}], oldData=[null]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO type=[CHILD_ADDED], data=[ChildData{path='/ns1', stat=225,225,1674566507586,1674566507586,0,1,0,0,1,1,226
, data=[65]}], InitialData=[null]
INFO ----------------------------------------
INFO forNodeCache回调
INFO ----------------------------------------
INFO forTreeCache回调
INFO type=[NODE_ADDED], data=[ChildData{path='/ns1/sub1', stat=226,226,1674566507592,1674566507592,0,0,0,0,1,0,226
, data=[65]}], oldData=[null]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO type=[CHILD_ADDED], data=[ChildData{path='/ns1/sub1', stat=226,226,1674566507592,1674566507592,0,0,0,0,1,0,226
, data=[65]}], InitialData=[null]
INFO ----------------------------------------
INFO 修改/ns1节点的值
INFO forNodeCache回调
INFO ----------------------------------------
INFO forTreeCache回调
INFO type=[NODE_UPDATED], data=[ChildData{path='/ns1', stat=225,227,1674566507586,1674566507601,1,1,0,0,1,1,226
, data=[66]}], oldData=[ChildData{path='/ns1', stat=225,225,1674566507586,1674566507586,0,1,0,0,1,1,226
, data=[65]}]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO type=[CHILD_UPDATED], data=[ChildData{path='/ns1', stat=225,227,1674566507586,1674566507601,1,1,0,0,1,1,226
, data=[66]}], InitialData=[null]
INFO ----------------------------------------
INFO 修改/ns1/sub1节点的值
INFO forNodeCache回调
INFO ----------------------------------------
INFO forTreeCache回调
INFO type=[NODE_UPDATED], data=[ChildData{path='/ns1/sub1', stat=226,228,1674566507592,1674566507605,1,0,0,0,1,0,226
, data=[66]}], oldData=[ChildData{path='/ns1/sub1', stat=226,226,1674566507592,1674566507592,0,0,0,0,1,0,226
, data=[65]}]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO type=[CHILD_UPDATED], data=[ChildData{path='/ns1/sub1', stat=226,228,1674566507592,1674566507605,1,0,0,0,1,0,226
, data=[66]}], InitialData=[null]
INFO ----------------------------------------
INFO 删除/ns1/sub1节点
INFO forNodeCache回调
INFO ----------------------------------------
INFO forTreeCache回调
INFO type=[NODE_REMOVED], data=[ChildData{path='/ns1/sub1', stat=226,228,1674566507592,1674566507605,1,0,0,0,1,0,226
, data=[66]}], oldData=[null]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO type=[CHILD_REMOVED], data=[ChildData{path='/ns1/sub1', stat=226,228,1674566507592,1674566507605,1,0,0,0,1,0,226
, data=[66]}], InitialData=[null]
INFO ----------------------------------------
INFO forNodeCache回调
INFO ----------------------------------------
INFO forTreeCache回调
INFO type=[NODE_REMOVED], data=[ChildData{path='/ns1', stat=225,227,1674566507586,1674566507601,1,1,0,0,1,1,226
, data=[66]}], oldData=[null]
INFO ----------------------------------------
INFO forPathChildrenCache回调
INFO 删除/ns1节点
INFO type=[CHILD_REMOVED], data=[ChildData{path='/ns1', stat=225,227,1674566507586,1674566507601,1,1,0,0,1,1,226
, data=[66]}], InitialData=[null]
INFO ----------------------------------------
```

 

> <font color="red">**这里有个问题，`CuratorCache.bridgeBuilder(curatorFramework, "/ns1").build()`设置监听的是`/ns1`,后面又通过`.forPathChildrenCache("/test", curatorFramework, (client, event) -> {`设置了监听`/test`，那么到底监听哪个？从日志上看是监听了`/ns`，那为什么要设置`/test`，是API设计问题？还是我用错了？欢迎交流！！！**</font>





# 计数器

## Shared Counter

`ShareCount`是`curator`的一个共享计数器，所有监视同一路径的客户端都将具有共享整数的最新值（考虑到 ZK 的一致性保证）。

![image-20230130134757766](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301347896.png)

主要涉及三个类`ShareCount`、`SharedCountReader`， `SharedCountListener`。以下是基本使用方法

```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ShareCountTest {
    // 连接地址
    public static final String CONNECT_STRING = "172.24.246.68:2181";

    public static final RetryPolicy RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @Test
    public void testShareCount() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(CONNECT_STRING, RETRY_POLICY);
        curatorFramework.start();
        SharedCount sharedCount = new SharedCount(curatorFramework, "/ShareCount", 0);
        sharedCount.start();
        sharedCount.addListener(new SharedCountListener() {
            @Override
            public void countHasChanged(SharedCountReader sharedCountReader, int newCount) throws Exception {
                log.info("countHasChanged callback");
                log.info("newCount={}", newCount);
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {

            }
        }, EXECUTOR_SERVICE);
        sharedCount.setCount(1);
        TimeUnit.DAYS.sleep(1);
        sharedCount.close();
    }
}
```

运行结果：

![image-20230130144955532](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301449669.png)



成功获取到了监听的值。

## Distributed Atomic Long

尝试原子增量的计数器。它首先尝试使用乐观锁定。如果失败，则采用可选的 InterProcessMutex。对于乐观和互斥锁，使用重试策略重试增量。

![image-20230130145355466](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301453597.png)

有两个构造方法：

`public DistributedAtomicLong(CuratorFramework client, String counterPath, RetryPolicy retryPolicy)`采用乐观模式。

```java
package cn.programtalk;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

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
}
```

运行结果：

```text
INFO 1. preValue=0, postValue=0, succeeded=true
INFO initialize succeed? true
INFO 2. preValue=0, postValue=0, succeeded=true
INFO 3. preValue=10, postValue=10, succeeded=true
INFO 4. preValue=9, postValue=9, succeeded=true
INFO 5. preValue=10, postValue=10, succeeded=true
INFO 6. preValue=9, postValue=9, succeeded=true
```



另外一个构造方法，提供类锁的模式，在互斥升级模式下创建。将首先使用给定的重试策略尝试乐观锁定。如果增量不成功， InterProcessMutex 将使用自己的重试策略尝试。

```java
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
```

运行结果：

```text
INFO 1. preValue=9, postValue=9, succeeded=true
INFO initialize succeed? false
INFO 2. preValue=9, postValue=9, succeeded=true
INFO 3. preValue=19, postValue=19, succeeded=true
INFO 4. preValue=18, postValue=18, succeeded=true
INFO 5. preValue=19, postValue=19, succeeded=true
INFO 6. preValue=18, postValue=18, succeeded=true
```



# 锁

使用ZK可以实现分布式锁功能。

## Shared Reentrant Lock（InterProcessMutex）

### 基本说明

全局同步的完全分布式锁，这意味着没有两个客户端可以同时持有相同的锁。

其提供了如下构造方法

```java
public InterProcessMutex(CuratorFramework client, String path)
{
    this(client, path, new StandardLockInternalsDriver());
}
```

这里有两个参数`client`：CuratorFramework客户端，`path`：zookeeper的node，抢锁路径，同一个锁path需一致。

```java
public void testLock1() throws Exception {
    curatorFramework.start();
    // 定义锁
    InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/program-talk-lock");
    // 获取锁
    lock.acquire();
    log.info("此处是业务代码");
    // 模拟业务执行30秒
    TimeUnit.SECONDS.sleep(30);
    // 释放锁
    lock.release();
}
```

某个时刻，查看Zk的节点，可以看到如下所示内容。

![image-20230125194401502](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301251944770.png)

当执行完毕的时候，如果正常释放锁，则会清理到对应的信息。

JavaDoc文档中其实有说跨JVM的锁，那么同一个JVM中多线程使用这个锁可以吗，可以!

```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

@Slf4j
public class InterProcessMutexThreadTest implements Runnable {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);


    @Override
    public void run() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        // 定义锁
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/InterProcessMutex");
        try {
            lock.acquire();
            String threadName = Thread.currentThread().getName();
            log.info("{} ，执行业务代码开始", threadName);
            TimeUnit.SECONDS.sleep(10);
            log.info("{} ，执行业务代码完毕", threadName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        InterProcessMutexThreadTest task = new InterProcessMutexThreadTest();
        Thread t1 = new Thread(task, "任务1");
        Thread t2 = new Thread(task, "任务2");
        t1.start();
        t2.start();
    }
}
```

运行结果如下：

```text
INFO 任务1 ，执行业务代码开始
DEBUG Reading reply session id: 0x100000022e20032, packet:: clientPath:null serverPath:null finished:false header:: 8,4  replyHeader:: 8,458,0  request:: '/InterProcessMutex/_c_4929b7d6-6c6b-4a9a-ae48-5315dc67523e-lock-0000000000,T  response:: #3139322e3136382e31302e31,s{457,457,1674654986411,1674654986411,0,0,0,72057594623164465,12,0,457} 
INFO 任务1 ，执行业务代码完毕
DEBUG Got notification session id: 0x100000022e20032
DEBUG Reading reply session id: 0x100000022e20031, packet:: clientPath:null serverPath:null finished:false header:: 8,2  replyHeader:: 8,459,0  request:: '/InterProcessMutex/_c_4929b7d6-6c6b-4a9a-ae48-5315dc67523e-lock-0000000000,-1  response:: null
DEBUG Got ping response for session id: 0x100000022e20031 after 5ms.
DEBUG Got WatchedEvent state:SyncConnected type:NodeDeleted path:/InterProcessMutex/_c_4929b7d6-6c6b-4a9a-ae48-5315dc67523e-lock-0000000000 for session id 0x100000022e20032
DEBUG Got ping response for session id: 0x100000022e20032 after 2ms.
DEBUG Reading reply session id: 0x100000022e20032, packet:: clientPath:null serverPath:null finished:false header:: 9,12  replyHeader:: 9,459,0  request:: '/InterProcessMutex,F  response:: v{'_c_284a2de1-37d1-42c4-b818-3d2206a50c34-lock-0000000001},s{455,455,1674654986408,1674654986408,0,3,0,0,0,1,459} 
INFO 任务2 ，执行业务代码开始
INFO 任务2 ，执行业务代码完毕
DEBUG Reading reply session id: 0x100000022e20032, packet:: clientPath:null serverPath:null finished:false header:: 10,2  replyHeader:: 10,460,0  request:: '/InterProcessMutex/_c_284a2de1-37d1-42c4-b818-3d2206a50c34-lock-0000000001,-1  response:: null
DEBUG Got ping response for session id: 0x100000022e20032 after 7ms.
Disconnected from the target VM, address: '127.0.0.1:58751', transport: 'socket'

Process finished with exit code 0
```

可以看到两个任务是顺序执行的，不过单个JVM基本不使用分布式锁，JDK内置的锁即可!



### 定义锁

正如上面所说的那样，通过构造方法去定义一个可重入排它锁，`InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/program-talk-lock");`。

### 获取锁

获取锁有两种方法，一种是使用上面所使用的`lock.acquire();`，这是个无参函数，他会一直尝试获取锁，如果获取不到则会一直阻塞。

另外一种是使用`public boolean acquire(long time, TimeUnit unit) throws Exception`，不同于上面那个，这个不会一直阻塞，`time`是时间参数，`unit`是时间的单位。超过这个时间则会放弃获取锁。

示例代码如下：

```java
 lock.acquire(10, TimeUnit.SECONDS);
```

此代码的意思就是如果在10秒内能获取到锁则返回`true`，超过10秒获取不到则返回`false`。不会一直阻塞。

### 释放锁

当程序执行完毕后必须释放锁，释放锁使用`release()`方法。

### 可重入性

```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 *  InterProcessMutex 锁的可重入性测试
 */
@Slf4j
public class InterProcessMutexReentrantTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    InterProcessMutex lock = new InterProcessMutex(curatorFramework, "/InterProcessMutexReentrantTest");
    void a() throws Exception {
        lock.acquire();
        log.info("a方法执行");
        b();
        lock.release();
    }
    void b() throws Exception {
        lock.acquire();
        log.info("b方法执行");
        lock.release();
    }

    @Test
    public void test() throws Exception {
        curatorFramework.start();
        a();
    }
}
```

上面的代码中，a函数调用b函数，并且a和b都是用了同一个锁。执行结果如下:

![image-20230126110754084](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301261107184.png)

程序正常执行，说明锁具备可重入性。



## Shared Lock（InterProcessSemaphoreMutex）

### 基本说明

`InterProcessSemaphoreMutex`也是一个排它锁，不同于`InterProcessMutex`的是，`InterProcessSemaphoreMutex`不是一个可重入锁。

使用方法（定义锁、获取锁、释放锁）跟`InterProcessMutex`没有太大区别。

代码示例：

```java
@Test
public void testLock3() throws Exception {
    curatorFramework.start();
    // 定义锁
    InterProcessLock lock = new InterProcessSemaphoreMutex(curatorFramework, "/InterProcessSemaphoreMutex");
    // 获取锁
    try {
        boolean got = lock.acquire(30, TimeUnit.SECONDS);
        if (got) {
            log.info("此处是业务代码");
            // 模拟业务执行30秒
            TimeUnit.SECONDS.sleep(30);
        } else {
            log.warn("未获取到锁");
        }
    }catch (Exception e) {
        e.printStackTrace();
    }
    finally {
        // 释放锁
        lock.release();
    }
}
```

某个时刻，查看Zk的节点，可以看到如下所示内容。



![image-20230125204141295](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301252041333.png)



### 可重入性

```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;

/**
 *  InterProcessSemaphoreMutex 锁的可重入性测试
 */
@Slf4j
public class InterProcessSemaphoreMutexReentrantTest {
    String connectString = "localhost:2181";
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(curatorFramework, "/InterProcessSemaphoreMutex");
    void a() throws Exception {
        lock.acquire();
        log.info("a方法执行");
        b();
        lock.release();
    }
    void b() throws Exception {
        lock.acquire();
        log.info("b方法执行");
        lock.release();
    }

    @Test
    public void test() throws Exception {
        curatorFramework.start();
        a();
    }
}
```

运行效果如下图：

![image-20230126111311989](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301261113118.png)





不会正常执行完毕，会一直锁住，说明此锁不具备可重入性。

## Shared Reentrant Read Write Lock（InterProcessReadWriteLock）

### 基本说明

`InterProcessReadWriteLock`是类似JDK的`ReentrantReadWriteLock`. 一个读写锁管理一对相关的锁。 一个负责读操作，另外一个负责写操作。 读操作在写锁没被使用时可同时由多个进程使用，而写锁使用时不允许读 (阻塞)。 此锁是可重入的。一个拥有写锁的线程可重入读锁，但是读锁却不能进入写锁。 这也意味着写锁可以降级成读锁， 比如请求写锁 —>读锁 —->释放写锁。 从读锁升级成写锁是不成的。

读锁和写锁有如下关系：

* 读写互斥

* 写写互斥

* 读读不互斥

**重入性**

读写锁是可以重入的，意味着你获取了一次读锁/写锁，那么你可以再次获取。但是要记得最后释放锁，获取了几次就得释放几次。

### 定义锁

```java
// 定义读锁
InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
```

### 获取锁

```java
InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
// 获取读锁
InterProcessReadWriteLock.ReadLock readLock = lock.readLock();
// 获取写锁
InterProcessReadWriteLock.WriteLock writeLock = lock.writeLock();
```

### 释放锁

同样使用`release()`释放锁

```java
writeLock.release();
readLock.release();
```

### 测试

**读写互斥**

读写互斥就是说，当写的时候（写锁没有释放的时候，无法读取）。

```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

@Slf4j
public class InterProcessReadWriteLockTest {
    String connectString = "172.24.246.68:2181";
    RetryPolicy retryPolicy = new RetryForever(1000);

    @Test
    public void testRead() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
        InterProcessReadWriteLock.ReadLock readLock = lock.readLock();
        readLock.acquire();
        log.info("读成功");
        readLock.release();
    }

    @Test
    public void testWrite() throws Exception {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        curatorFramework.start();
        InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
        InterProcessReadWriteLock.WriteLock writeLock = lock.writeLock();
        writeLock.acquire();
        TimeUnit.SECONDS.sleep(30);
        log.info("写成功");
        writeLock.release();
    }
}
```

`testRead`方法是读，`testWrite`方法是写，`testWrite`我休眠了30秒，主要是为了锁释放慢一点，来测试是否可读。

首先运行`testWrite`，然后运行`testRead`（不到超过30后再运行，为了保证此时写锁并没有释放）。

在读锁没有释放之前，运行效果图如下：

![image-20230130102133560](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301021689.png)



![image-20230130102146330](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301021435.png)



可以看到读也阻塞着，等待一段时间后，写锁释放，读也就不会继续阻塞，运行完毕。



![image-20230130102336466](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301023576.png)



![image-20230130102305928](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301023032.png)



**写写互斥**

运行两次`testWrite`方法，要保证多实例运行。idea需要设置。按照下图设置。

![image-20230130102535158](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301025289.png)



接下来运行`testWrite`方法。

![image-20230130102724844](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301027929.png)



![image-20230130102744697](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301027801.png)



第一个没运行完，第二个也会阻塞。



**读读不互斥**

我就不具体测试了，道理一样。

**可重入性**

```java
public void testWrite() throws Exception {
    CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
    curatorFramework.start();
    InterProcessReadWriteLock lock  = new InterProcessReadWriteLock(curatorFramework, "/InterProcessReadWriteLock");
    InterProcessReadWriteLock.WriteLock writeLock = lock.writeLock();
    writeLock.acquire();
    writeLock.acquire();
    log.info("写成功");
    writeLock.release();
    writeLock.release();
}
```

程序能够正常执行完毕，说明具备可重入性。



### 使用场景

分布式读写锁适用于读多写少的情况。



## Multi Shared Lock（InterProcessMultiLock）

### 基本说明

`InterProcessMultiLock`是多锁的意思，相当于一个容器，包含了多个锁。统一管理，一起获取锁，一起释放锁。

![image-20230130103843996](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301038109.png)

### 定义锁

他有两个构造方法。

`InterProcessMultiLock(CuratorFramework, List<String>)`创造的是一个`InterProcessMutex`的锁。

```
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
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
}
```

运行后，从命令行看：

![image-20230130104415814](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301044896.png)

创建了两个节点。



两外一个构造方法是`InterProcessMultiLock(List<InterProcessLock> locks)`，它则允许任何实现`InterProcessLock`的锁。

```java
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
```

运行结果：命令行查看。

![image-20230130105407474](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301054553.png)



## Shared Semaphore（InterProcessSemaphoreV2）

`InterProcessSemaphoreV2`是一个信号量，跨JVM工作，多个客户端使用通过一个path则会统一受到进程间租约限制。这个信号量是公平的，会按照顺序获得租约。

直白点说：`InterProcessSemaphoreV2`就类似JDK中的`Semaphore`，`Semaphore`用于控制进入临界区的线程数，而`InterProcessSemaphoreV2`是跨JVM的而已。

![image-20230130153513493](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301535640.png)

有两个构造方法：

```java
// 最大租约是由给定路径的用户维护的约定。
public InterProcessSemaphoreV2(CuratorFramework client, String path, int maxLeases)
// SharedCountReader 用作给定路径的信号量的方法，以确定最大租约。
public InterProcessSemaphoreV2(CuratorFramework client, String path, SharedCountReader count)
```



```java
package cn.programtalk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreV2;
import org.apache.curator.framework.recipes.locks.Lease;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.retry.RetryForever;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InterProcessSemaphoreV2Test {
    static String connectString = "172.24.246.68:2181";
    static RetryPolicy retryPolicy = new RetryForever(10000);

    @Test
    public void testInterProcessSemaphoreV21() {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executor.submit(new Thread(new Task()));
        }
        while (true) {
        }
    }

    @Test
    public void testInterProcessSemaphoreV22() {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            executor.submit(new Thread(new Task2()));
        }
        while (true) {
        }
    }

    static class Task implements Runnable {
        @Override
        public void run() {
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
            curatorFramework.start();
            InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV2", 3);
            Lease lease = null;
            try {
                lease = interProcessSemaphoreV2.acquire();
                log.info("{} 获取到租约", Thread.currentThread().getName());
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                interProcessSemaphoreV2.returnLease(lease);
                log.info("{} 释放掉租约", Thread.currentThread().getName());
            }
        }
    }

    static class Task2 implements Runnable {
        @Override
        public void run() {
            CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
            curatorFramework.start();
            InterProcessSemaphoreV2 interProcessSemaphoreV2 = new InterProcessSemaphoreV2(curatorFramework, "/InterProcessSemaphoreV2", new SharedCount(curatorFramework, "/InterProcessSemaphoreV2-SharedCount", 3));
            Lease lease = null;
            try {
                lease = interProcessSemaphoreV2.acquire();
                log.info("{} 获取到租约", Thread.currentThread().getName());
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                interProcessSemaphoreV2.returnLease(lease);
                log.info("{} 释放掉租约", Thread.currentThread().getName());
            }
        }
    }
}
```



`testInterProcessSemaphoreV21`搭配Task任务，实现的是`public InterProcessSemaphoreV2(CuratorFramework client, String path, int maxLeases)`构造方法定义的实例。

运行截图：

![image-20230130175241912](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202301301752058.png)



从图上可知，线程9、3、2获取到了租约（凭证），之后9释放了租约（凭证），此时空闲一个租约（凭证），然后线程3就又获得了，以此类推，总之，同时获得租约（凭证）的线程**只有三个**！！！

---

`testInterProcessSemaphoreV22`搭配Task2任务，实现的是`public InterProcessSemaphoreV2(CuratorFramework client, String path, SharedCountReader count)`构造方法定义的实例。

运行结果跟上面类似。














> Github：<font color="green">https://github.com/ProgramTalk1024/curator-tutor</font>
