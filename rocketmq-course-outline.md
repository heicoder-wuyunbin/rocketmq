# RocketMQ 实战训练营

> **面向对象**：在校大学生 / 初级~高级 Java 后端开发者
> **前置条件**：本机已装 Docker（或 brew 装的 RocketMQ）；具备 Maven / Java 基础
> **技术栈**：Spring Boot 4 + Java 25 (GraalVM) + Maven 3.9.9 + RocketMQ 5.3.x 原生 Client
> **配置格式**：`application.properties`
> **操作系统**：macOS / Linux（aarch64 / x86_64）

---

## 目录

1. [开班说明与分层策略](#一开班说明与分层策略)
2. [通用工程模板](#二通用工程模板)
3. [初级班（Day 1~7）](#三初级班day-17)
4. [中级班（Day 8~11）](#四中级班day-811)
5. [高级班（Day 12~15）](#五高级班day-1215)
6. [番外专题（选修）](#六番外专题选修)
7. [考核与结业标准](#七考核与结业标准)
8. [附录](#八附录)

---

## 一、开班说明与分层策略

### 1.1 为什么要分层

消息队列的知识跨度极大：从"发一条消息收一条消息"，到"用事务消息保跨库一致性、用 DLedger 保高可用"，中间隔着好几个数量级的认知复杂度。不分层，初级生会被事务消息回查、消息积压、消费幂等等概念直接压垮。

分层后，每个班级有明确的**能力边界**和**产出标准**，学生知道该学到哪、学到什么程度能毕业。

### 1.2 三层班级概览

| 班级 | 周期 | 覆盖天数 | 核心产出 | 适合人群 |
|:---|:---|:---|:---|:---|
| **初级班** | 7 天 | Day 1 ~ Day 7 | 能跑通生产/消费最小闭环，理解核心概念，能覆盖 80% 日常异步解耦场景 | 在校生、实习生、刚入职的 Java 后端 |
| **中级班** | 4 天 | Day 8 ~ Day 11 | 掌握顺序/延迟/批量/过滤四类消息 + 消费重试与幂等，能独立设计可靠消息链路 | 1~3 年经验，需要独立负责模块 |
| **高级班** | 4 天 | Day 12 ~ Day 15 | 掌握事务消息、积压治理、高可用架构，能设计电商交易级综合系统，能应对面试 | 3 年以上，需要扛生产流量或准备面试 |
| **番外专题** | 4 篇 | 选修 | 存储源码、Kafka 对比、5.x Proxy、监控运维 | 高级班学员选修，或工作中遇到具体问题定向学习 |

### 1.3 分班规则

- **入学测试**：5 道选择题（消息队列选型 + 1 道简单代码题）
  - 得分 0~2：进初级班
  - 得分 3~4：进中级班
  - 得分 5：进高级班（或自主选择从初级班补基础）
- **升级机制**：初级班结业后，通过 checkpoint 1 可进入中级班；中级班结业后，通过 checkpoint 2 可进入高级班
- **降级保护**：高级班学员如果跟不上，允许回退到中级班重修

### 1.4 每日讲义标记规范

```
【必学】核心知识点，必须掌握
【理解】需要听懂，但暂时不用手写
【选修】拓展内容，有余力再看
【高级标记】仅高级班讲解，初中级了解即可
```

---

## 二、通用工程模板

所有班级共用一套工程骨架，从 Day 1 开始逐步填充。

### 2.1 项目结构

```
rocketmq-demo/
├── pom.xml
├── docker-compose.yml          # 一键起 NameServer + Broker
├── application.properties
├── src/
│   ├── main/
│   │   ├── java/com/example/rocketmq/
│   │   │   ├── RocketmqCourseApplication.java
│   │   │   ├── config/RocketMQConfig.java
│   │   │   └── service/...        # 每日新增的业务 Service
│   │   └── resources/
│   └── test/java/com/example/rocketmq/
│       └── DayXX*Test.java        # 每日测试
└── student-repos/dayXX/          # 初始化脚本批量生成
```

### 2.2 依赖与配置要点

- 父工程：`spring-boot-starter-parent:4.0.6`
- 消息客户端：`org.apache.rocketmq:rocketmq-client:5.3.x`（经典 `DefaultMQProducer` / `DefaultMQPushConsumer` 那套，直连 5.x Broker，无需 Proxy）
- `RocketMQConfig` 自封装一个 `DefaultMQProducer` Bean，统一注入 `name-server` 与 `producer group`，等价于 redis-demo 的 `RedisConfig`
- `application.properties` 关键项：
  ```properties
  rocketmq.name-server=127.0.0.1:9876
  rocketmq.producer.group=rocketmq-course-producer
  ```

### 2.3 环境搭建（二选一）

**方式 A（首选，跨平台可复现）：Docker Compose**
```bash
cd rocketmq-demo
docker compose up -d        # 起 NameServer(9876) + Broker(10911)
```
仓库已内置 `docker-compose.yml`，`autoCreateTopicEnable=true` 自动建 Topic，零配置开箱即用。

**方式 B（本地无 Docker）：brew 原生安装**
```bash
brew install apache-rocketmq   # 若公式可用；否则下载官方 binary tarball
mqnamesrv &                    # 起 NameServer
mqbroker -n 127.0.0.1:9876 autoCreateTopicEnable=true &
```

---

## 三、初级班（Day 1~7）

> **目标**：跑通"发消息—收消息"最小闭环，理解 RocketMQ 核心概念，覆盖 80% 日常异步解耦场景。

### Day 1：架构全景 + 环境搭建 + Hello World 最小收发闭环【必学】
- RocketMQ 四大角色：Producer / Consumer / NameServer / Broker
- 两个核心概念：Topic / MessageQueue（队列是并行度单位）
- Docker 一键起环境（或 brew）
- **最小闭环**：用 `DefaultMQProducer` 发 1 条消息，用 `DefaultMQPushConsumer` 收 1 条，测试断言收到 → 证明环境通了
- 引出"为什么需要自封装 RocketMQTemplate"

### Day 2：第一个生产者 + 同步发送【必学】
- `DefaultMQProducer` 生命周期（构造→setNamesrvAddr→start→send→shutdown）
- 同步发送 `SendResult`：`msgId` / `sendStatus` / `queueOffset` / `messageQueue`
- 三种发送状态（SEND_OK / FLUSH_DISK_TIMEOUT / FLUSH_SLAVE_TIMEOUT）含义
- 发送超时与重试参数（`retryTimesWhenSendFailed`）

### Day 3：第一个消费者 + 消费组 + 集群模式【必学】
- `DefaultMQPushConsumer` + `MessageListenerConcurrently`
- 消费组 `consumer group`：同组集群分摊、不同组广播复制
- `ConsumeConcurrentlyStatus.RECONSUME_LATER` vs `CONSUME_SUCCESS`
- 消息体 `Message` 结构：`topic` / `tags` / `keys` / `body`

### Day 4：发送三兄弟——同步 / 异步 / 单向【必学】
- 同步：等结果，重要业务
- 异步：`sendAsync` + `SendCallback`，高吞吐
- 单向：`sendOneway`，日志型不关心结果
- 异步回调里的线程（`callbackExecutor`）与异常处理

### Day 5：Tag 过滤 + Keys + 消息属性透传【必学】
- `topic:tag` 二级过滤，`subscribe("topic", "tagA || tagB")`
- `keys` 用于消息溯源与运维查询
- `putUserProperty` 透传业务字段（traceId / userId）
- Tag 误用陷阱：Tag 不是分类维度，Topic 才是

### Day 6：集群模式 vs 广播模式【理解】
- 集群模式（默认）：同组消费者分摊消息
- 广播模式：`MessageModel.BROADCASTING`，同组每人一份，常用于本地缓存刷新
- 两种模式对消费进度（offset）存储位置的区别（集群存 Broker，广播存本地）

### Day 7：普通消息实战——下单异步解耦【必学】
- 场景：用户下单成功后，异步发"短信通知" + "记审计日志"，主流程不阻塞
- 用 Day4 的单向/异步发送 + Day3 的并发监听器实现
- 引出可靠性问题：消息丢了怎么办？（为中级班埋伏笔）

---

## 四、中级班（Day 8~11）

> **目标**：掌握四类进阶消息 + 消费可靠性，能独立设计可靠消息链路。

### Day 8：顺序消息（分区有序）【必学】
- 全局顺序 vs 分区顺序（RocketMQ 只保证分区顺序）
- `MessageQueueSelector`：按业务 key（如订单号）路由到固定队列
- 顺序消费 `MessageListenerOrderly` + 队列锁
- 实战：订单状态流转（创建→支付→发货）严格有序

### Day 9：延迟消息【必学】
- RocketMQ 18 个延迟等级（1s/5s/10s/.../2h）
- `setDelayTimeLevel` 用法与"不能自定义任意时间"的坑
- 替代方案：定时任务 / 时间轮（任意精度延迟）
- 实战：订单 30 分钟未支付自动关单

### Day 10：批量消息 + SQL92 过滤 + 消息查询【必学】
- 批量发送 `send(List<Message>)`，单批 ≤ 1MB、同 Topic 同队列
- SQL92 过滤：`subscribe("topic", "age > 18 AND type = 'VIP'")`，需开启 `enablePropertyFilter`
- 按 `keys` / `msgId` 查询消息（`mqadmin` 或 Dashboard）
- 实战：批量同步用户状态 + 按属性筛选推送给不同下游

### Day 11：消费重试 + 幂等 + 死信队列【必学】
- 并发消费失败重试 16 次（退避），进入 `%RETRY%group` 主题
- 重试耗尽进入 `%DLQ%group` 死信队列，需人工处理
- **消费幂等**：为什么必须做（重试/网络重发都会重复）
- 幂等三板斧：唯一键去重表 / Redis SETNX / 业务状态机
- 实战：扣款消息重复消费不重复扣钱

---

## 五、高级班（Day 12~15）

> **目标**：事务一致性、运维治理、高可用架构、综合实战。

### Day 12：事务消息（Half + 本地事务 + 回查）【必学】
- 分布式事务难题：本地库扣成功，消息没发，怎么办？
- Half Message（半消息）：对消费者不可见，先落 Broker
- `TransactionListener`：`executeLocalTransaction` + `checkLocalTransaction`
- 回查机制：Broker 定时回调确认本地事务最终状态
- 实战：下单（本地事务）+ 扣库存（下游）最终一致

### Day 13：消息积压治理【必学】
- 积压成因：消费慢 / 消费挂 / 突发流量
- 应急：临时扩容消费实例（同组加机器分摊队列）、提升消费线程数、必要时跳过非关键消息
- 限流：消费端线程池 + 信号量保护下游
- 监控指标：堆积量 `Diff`、消费 TPS

### Day 14：高可用架构【高级标记】
- NameServer：无状态、互不通信、各自维护路由表
- Broker 主从：同步双写 vs 异步复制（一致性 vs 性能）
- 刷盘策略：SYNC_FLUSH vs ASYNC_FLUSH（丢消息风险边界）
- DLedger：基于 Raft 的主从自动切换，解决"主挂人工切"
- 部署拓扑图 + 选型建议

### Day 15：生产最佳实践 + 电商交易链路综合实战【必学】
- 串联：顺序（订单流转）+ 延迟（关单）+ 事务（下单扣库存）+ 幂等（防重）
- 生产避坑清单：Topic 规划、消息大小、消费幂等、监控告警
- 面试高频题串讲

---

## 六、番外专题（选修）

- **源码篇**：CommitLog / ConsumeQueue / IndexFile 三段式存储，为什么快
- **对比篇**：RocketMQ vs Kafka vs RabbitMQ 选型
- **云原生篇**：RocketMQ 5.x Proxy + gRPC Client，与 4.x 客户端差异
- **运维篇**：Dashboard（`rocketmq-dashboard`）+ `mqadmin` 命令 + Exporter + Grafana 监控

---

## 七、考核与结业标准

| 班级 | Checkpoint | 达标要求 |
|:---|:---|:---|
| 初级班 | CP1 | Day7 实战跑通：下单消息能被独立消费者收到，测试断言成功 |
| 中级班 | CP2 | Day11 幂等测试：同一条消息重复投递，业务只生效一次 |
| 高级班 | 结业 | Day15 综合实战：事务消息最终一致 + 积压后能恢复消费 |

通用要求：每 Day `mvn test-compile` 通过；高级班需能口头讲清高可用选型与事务回查。

---

## 八、附录

### A.1 每日产物清单（镜像 redis-demo 标准）
- `docs/dayXX.md`：每日讲义（知识点 + 代码模板 + 作业 + mermaid 图）
- `docs/articles/dayXX-N-标题.md`：叙事文章（事故 / 对比 / 原理），供公众号 / 视频
- `docs/scripts/dayXX-script.md`：口播稿（时间戳 + 画面 + 留存锚点）
- `student-repos/dayXX/`：累积式 Maven 工程（含 day1~dayN 的 service / test）
- `init-student-repos.sh`：批量生成 15 个学生仓库 + git init + `mvn test-compile` 校验

### A.2 标记规范速查
【必学】【理解】【选修】【高级标记】四级（见 1.4）
