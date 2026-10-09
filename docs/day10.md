# Day 10：批量消息 + SQL92 过滤 + 消息查询

> **班级**：中级班（必学）
> **前置**：Day2 发送、Day5 Tag/属性
> **目标**：会用批量发送省 RTT；会用 SQL92 按属性过滤；知道按 keys/msgId 查消息。

---

## 一、批量发送：一次发一堆【必学】

```java
List<Message> list = new ArrayList<>();
for (int i = 0; i < 100; i++) {
    list.add(new Message(topic, "batch", "k"+i, body));
}
producer.send(list);   // 一次网络往返发 100 条
```

省 100 次 RTT，吞吐大涨。见 `BatchSendService.sendBatch`。

### 限制（必记）
- **单批总大小 ≤ 1MB**（超了拆分）
- **同 Topic**（批量不能跨 Topic）
- **不能有延迟 / 事务属性**（批量消息不支持）

---

## 二、SQL92 属性过滤【必学】

Tag 只能做"等值或或"的粗筛。要按数值/多条件筛（如 `age > 18 AND vip = 'true'`），用 SQL92：

```java
consumer.subscribe(topic, MessageSelector.bySql("vip = 'true' AND age > 18"));
// 发消息时挂属性
msg.putUserProperty("vip", "true");
msg.putUserProperty("age", "20");
```

> ⚠️ 前置：Broker 必须开 `enablePropertyFilter=true`，否则 SQL 过滤不生效。Tag 过滤不需要这个。

支持的运算符：`=`, `<>`, `>`, `>=`, `<`, `<=`, `BETWEEN`, `IN`, `IS NULL`, `AND`, `OR`, `NOT` 等。

---

## 三、消息查询：按 keys / msgId【理解】

消息发出去要排查？两条路径：

- **Dashboard**（rocketmq-dashboard）：按 Topic + keys 或 msgId 搜，看轨迹
- **mqadmin**：`mqadmin queryMsgByKey` / `queryMsgById`

`keys` 就是你 Day5 设的业务主键（订单号）。查的时候拿它去搜。

---

## 四、实战：批量同步用户状态 + 按属性推不同下游【必学】

1. 批量发 100 条用户状态变更（同 Topic）
2. VIP 用户（`vip=true`）由"营销"消费组用 SQL92 筛出，发优惠券
3. 普通用户走默认消费

---

## 五、代码模板【必学】

- `BatchSendService.sendBatch`：批量发
- `PropertyFilterService`：`sendWithProperty` + `consumeBySql`

---

## 六、单元测试 Day10

`Day10BatchFilterTest`：批量发 3 条普通 + 1 条 `vip=true`，SQL92 消费者只收到 VIP 那条。

```bash
mvn test -Dtest=Day10BatchFilterTest
```

---

## 七、核对清单

- [ ] ✅ 会用 `producer.send(List<Message>)` 批量发
- [ ] ✅ 知道批量三限制（≤1MB / 同 Topic / 无延迟事务）
- [ ] ✅ 会用 `MessageSelector.bySql` 且知道 Broker 需开 `enablePropertyFilter`
- [ ] ✅ 知道按 keys/msgId 在 Dashboard/mqadmin 查消息
- [ ] ✅ Day10 测试绿灯
