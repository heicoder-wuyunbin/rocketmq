package com.example.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Day 1 最小收发闭环（Hello World）。
 *
 * 目的：用"裸" DefaultMQProducer / DefaultMQPushConsumer 跑通 发→收，
 * 证明本机 RocketMQ 环境（NameServer + Broker）已通。
 *
 * 前置：docker compose up -d 已起 NameServer(9876) + Broker(10911)。
 */
public class Day01QuickStartTest {

    private static final String TOPIC = "TopicTestDay01";
    private static final String NAME_SERVER = "127.0.0.1:9876";

    @Test
    void helloWorldSendAndReceive() throws Exception {
        // 每个测试用独立消费组，确保能从第一条开始消费，避免历史 offset 干扰
        String group = "day01-consumer-" + System.nanoTime();
        CountDownLatch latch = new CountDownLatch(1);
        String[] received = new String[1];

        // 1) 消费者：订阅 Topic，注册并发监听器
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(NAME_SERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(TOPIC, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (MessageExt msg : msgs) {
                received[0] = new String(msg.getBody(), StandardCharsets.UTF_8);
                latch.countDown();
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();

        // 2) 生产者：发送一条消息
        DefaultMQProducer producer = new DefaultMQProducer("day01-producer");
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();

        Message message = new Message(TOPIC, "hello", "key-1",
                "Hello RocketMQ".getBytes(StandardCharsets.UTF_8));
        SendResult result = producer.send(message);

        // 3) 等待消费者收到（最多 15s）
        boolean got = latch.await(15, TimeUnit.SECONDS);

        producer.shutdown();
        consumer.shutdown();

        assertTrue(got, "消费者应在 15s 内收到消息（检查 NameServer/Broker 是否已启动）");
        assertEquals("Hello RocketMQ", received[0]);
        assertNotNull(result.getMsgId());
        System.out.println("[Day01] sendStatus=" + result.getSendStatus()
                + ", msgId=" + result.getMsgId() + ", received=" + received[0]);
    }
}
