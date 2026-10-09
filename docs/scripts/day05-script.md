# Day 5 口播稿 · Tag 过滤 + Keys + 属性透传

> **对应讲义**：`docs/day05.md`
> **班级标签**：初级班（核心）
> **预估时长**：13-16 分钟

---

## 章节时间戳

```
00:00 开场：Tag 不是分类
01:00 Tag 是二级过滤
04:00 Keys 业务身份证
06:30 putUserProperty 透传
08:30 Tag 误用陷阱
11:00 今日收获 + 下集预告
```

---

## 00:00 开场（0:00 - 1:00）

【画面】标题卡

> 今天加 Tag Keys 属性。/ 先说最常见错用 / 把 Tag 当分类。

## 01:00 Tag 二级过滤（1:00 - 4:00）

【画面】subscribe "pay || refund" 图

> Tag 是 Topic 内细分。/ subscribe 表达式筛 / 服务端做 / 不关心的 Tag 不推。/ 省带宽。

【留存锚点】"过滤在哪做 / 服务端 Broker / 评论区扣 5。"

## 04:00 Keys（4:00 - 6:30）

【画面】Message(topic,tag,keys,body) 高亮 keys

> keys 业务主键 订单号。/ 运维按 keys 查消息轨迹。/ 每条都设 / 成本零出事救命。

## 06:30 属性透传（6:30 - 8:30）

【画面】putUserProperty traceId → getUserProperty

> putUserProperty 挂 traceId userId。/ 消费端直接拿 / 不用解析 body。/ 透传排查字段。

## 08:30 误用陷阱（8:30 - 11:00）

【画面】左：一个 Topic 塞所有业务(红) 右：Topic 分边界(绿)

> 错误：订单物流积分全塞一个 Topic 用 Tag 分。/ 队列权限扩容搅一起。/ 正确：Topic 是业务边界 / Tag 是细类。

## 11:00 收尾

【画面】总结卡

> 今天：Tag 过滤、Keys 溯源、属性透传、Topic 划边界。/
> 明天 Day6 / 集群 vs 广播。/ 点赞订阅。

【画面】结束卡：Subscribe + 「Day6：集群与广播」+ 水印

---

## 拍摄备忘

- 误用陷阱用左右对比图，左红右绿
- keys 那张标"每条都设"
