# Day 2：第一个生产者 + 同步发送

> **班级**：初级班（必学）
> **前置**：Day1 环境已通（Docker 起好 NameServer + Broker）
> **目标**：会用 `DefaultMQProducer` 同步发送；读懂 `SendResult` 的 `msgId` / `sendStatus` / `queueOffset` / `messageQueue`；理解发送重试参数。

---

## 一、Producer 生命周期【必学】

```mermaid
flowchart LR
    A[new DefaultMQProducer group] --> B[setNamesrvAddr]
    B --> C[start]
    C --> D[send 阻塞等结果]
    D --> E[shutdown 释放]
```

四步缺一不可：`start()` 前发送会报错；不 `shutdown()` 会泄漏连接。

---

## 二、send 到底返回了什么：SendResult【必学】

```java
SendResult r = producer.send(msg);
r.getMsgId();          // 全局唯一消息 ID（运维查消息用）
r.getSendStatus();     // 发送结果状态
r.getMessageQueue();   // 这条消息落到了哪个 MessageQueue
r.getQueueOffset();    // 在该队列里的位点（单调递增）
```

`queueOffset` 很关键：它和 `messageQueue` 一起唯一确定"消息在 Broker 上的位置"，也是消费进度的依据（Day3 会用到）。

---

## 三、四种发送状态【理解】

| 状态 | 含义 | 业务要不要管 |
|:---|:---|:---|
| `SEND_OK` | 正常，主从都妥 | 不用 |
| `FLUSH_DISK_TIMEOUT` | 刷盘超时（Broker 配了 SYNC_FLUSH 但没及时落盘） | 要告警/补偿 |
| `FLUSH_SLAVE_TIMEOUT` | 同步从节点超时 | 要告警 |
| `SLAVE_NOT_AVAILABLE` | 从节点不可用 | 一般只记日志 |

> 默认 Broker 是 ASYNC_FLUSH，所以正常几乎都是 `SEND_OK`。知道后三种存在，是 Day14 高可用里"丢消息边界"的伏笔。

---

## 四、发送重试参数【理解】

```java
producer.setRetryTimesWhenSendFailed(3);  // 同步发送失败重试次数，默认 2
producer.setRetryTimesWhenSendAsyncFailed(3);
producer.setSendLatencyFaultEnable(true);  // 避开响应慢的 Broker
```

重试只针对"发送失败"（网络抖、Broker 不可用），**不保证业务去重**——重试可能产生重复消息，消费端照样要做幂等（Day11）。

---

## 五、代码模板（SyncProducerService）【必学】

```java
public class SyncProducerService {
    public SendResult send(String topic, String tags, String keys, String body) throws Exception {
        Message msg = new Message(topic, tags, keys, body.getBytes(StandardCharsets.UTF_8));
        return producer.send(msg);   // 同步：阻塞直到 Broker 返回
    }
}
```

`Message(topic, tags, keys, body)` 四个参数对应：主题 / 标签 / 业务主键（查消息用）/ 消息体（字节数组）。

---

## 六、单元测试 Day02

`Day02SyncSendTest.syncSendReturnsResult`：发一条到 `TopicDay02`，断言 `msgId` / `sendStatus` 非空。另一测试校验四种 `SendStatus` 常量存在。

```bash
mvn test -Dtest=Day02SyncSendTest
```

> 跑测试前确认 `docker compose up -d` 已起 Broker，否则连接失败。

---

## 七、核对清单

- [ ] ✅ 能默画 Producer 生命周期（new→setNamesrv→start→send→shutdown）
- [ ] ✅ 能说清 `msgId` / `queueOffset` / `messageQueue` 三者关系
- [ ] ✅ 知道 `SEND_OK` 之外还有三种状态及其含义
- [ ] ✅ 知道"发送重试 ≠ 业务去重"，消费端仍要幂等
- [ ] ✅ Day02 测试绿灯
