# 你发出的消息，Broker 到底回了一句什么

你以为消息发出去就万事大吉了？

我见过一个真实的故障：某电商下单后发了一条"订单创建"消息，代码里 `producer.send(msg)` 一行带过，没看返回值。大促那天，运营发现部分订单"没处理"。查了一整晚日志，最后在 Broker 上看到一堆 `FLUSH_DISK_TIMEOUT`——消息根本没落盘，但发送方还以为成功了。

问题出在哪？出在绝大多数人把 `send` 当成"快递已寄出"，而它本质是一张"带坐标的回执"。今天，我们把这张回执拆开。

## 本质：SendResult 不是"已送达"，而是"已落盘/已同步"的状态确认

第一句话请记住：**`producer.send(msg)` 是同步阻塞的**。它不会"发出去就返回"，而是阻塞到 Broker 确认收到、并给出结果，才把 `SendResult` 交还给你。

所以 SendResult 回答的不是"消息在路上了没"，而是"消息现在到了什么状态"。这是底层逻辑的第一个分叉：很多系统的"发送"是尽力而为（fire and forget），而 RocketMQ 的同步发送是"确认即责任"——你拿到了结果，就背上了判断的责任。

这张回执里，有四个字段最关键。

## 第一：Producer 有生命周期，"随用随扔"会出事

先看发送方自己。很多初学者随手就写：

```java
DefaultMQProducer producer = new DefaultMQProducer(group);
producer.setNamesrvAddr("127.0.0.1:9876");
producer.start();              // 不 start，send 直接报错
SendResult r = producer.send(msg);
producer.shutdown();           // 不 shutdown，连接泄漏
```

它不是一个"new 出来就能用"的工具类，而是一台需要启动、也需要关机的设备。生命周期四步：`new → setNamesrvAddr → start → send → shutdown`。`start()` 之前发送会报"service not started"；用完了不 `shutdown()`，连接和线程就一直挂着，时间久了进程资源被吃光。

**方法**：把 Producer 做成"单例 + 应用生命周期绑定"，启动即 start，关闭钩子里 shutdown。不要每条消息 new 一个 Producer——这是教科书级别的浪费，也会把 NameServer 的连接数打爆。

## 第二：SendResult 四字段，真正能定位消息的是"坐标"而非"ID"

回执拆开，绝大多数人只盯着 `msgId`。四个字段是：

- `msgId`：全局唯一消息 ID，运维查消息全靠它。
- `sendStatus`：Broker 告诉你这次发得怎么样。
- `messageQueue`：这条消息落到了哪个队列（一个 Topic 被拆成多个队列）。
- `queueOffset`：在该队列里的位点（单调递增）。

重点在第三和第四个。**`messageQueue + queueOffset`，唯一锁定了消息在 Broker 磁盘上的坐标。** 打个比方：`msgId` 是快递单号，而 `queueOffset` 是货架上第几排第几格。消费进度（offset）本质上就是"这个队列我读到第几个了"——理解了这点，你才看得懂 Day3 的消费者。

所以别再只记 `msgId`，把"队列 + 位点"当成消息的 GPS。

## 第三：状态不止 SEND_OK，另三种是"丢消息的边界"

默认 Broker 是异步刷盘，你看到的几乎全是 `SEND_OK`。但请记住还有三种：

- `FLUSH_DISK_TIMEOUT`：配了"同步刷盘"，但消息没及时写进磁盘。
- `FLUSH_SLAVE_TIMEOUT`：配了"同步复制"，从节点没及时跟上。
- `SLAVE_NOT_AVAILABLE`：从节点挂了。

这三者的共同含义是：**消息可能还只在内存里，没真正落盘。** 此时 Broker 一宕机，消息就没了。

**边界**：`SEND_OK` 才是"妥了"；其余三种，是你在高可用边界上的红色信号。生产环境看到它们，必须告警或补偿，不能装没看见。这是 Day14 讲"丢消息边界"时的事实来源，今天先埋下伏笔。

## 第四：重试解决"别丢"，不解决"别重复"

`producer.setRetryTimesWhenSendFailed(3)` 很贴心：发送失败自动重试 3 次。

但必须现在说破一个坑：**重试可能产生重复消息。** 比如消息其实已经写进 Broker 了，只是返回那一刻网络断了，客户端以为失败又发了一次。于是 Broker 上躺着两条一样的内容。

所以"发送重试"的边界极清晰——它解决"网络抖动别丢"，不解决"业务别重复处理"。后者是消费端的事（Day11 幂等）。这两件事今天一次性钉死，后面少踩十年坑。

## 怎么办：落地清单

1. **永远检查 SendResult**：不要 `producer.send(msg)` 一行了事，至少把 `sendStatus` 非 SEND_OK 的情况打告警日志。
2. **Producer 单例化 + 绑定生命周期**：start 在应用启动，shutdown 在关闭钩子，杜绝连接泄漏。
3. **把 queueOffset 当 GPS 记**：运维排查时，`messageQueue + queueOffset` 比 msgId 更能定位"消息到底在哪"。
4. **为三种异常状态配告警/补偿**：`FLUSH_DISK_TIMEOUT` 等不是可以忽略的噪音。
5. **消费端必须做幂等**：既然发送重试会造重复，消费方就自己兜底，别指望发送方保证唯一。

---

下一篇：Day3，消息发出去了，谁来看？消费者登场——消费组、集群模式，以及那个决定"成不成功"的返回。


后面还有 13 篇，从生产者、消费者一路讲到事务消息、高可用和综合实战——订阅本合集就能连着看，不用一篇篇翻。

要是这系列真帮你少踩了坑，文末赞赏随缘就好，就当请我喝杯茶。原创手写，与君共勉。
