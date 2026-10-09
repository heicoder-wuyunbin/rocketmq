package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Day 8：顺序消息（分区有序）。
 * 用 MessageQueueSelector 把同一业务 key（订单号）路由到固定队列，
 * 该队列内的消息严格按发送顺序被消费。
 */
public class OrderSequenceService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQProducer producer;

    public OrderSequenceService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    /** 发送订单状态事件，按 orderId 路由到固定队列 */
    public void sendOrderEvent(String topic, String orderId, String status) throws Exception {
        Message msg = new Message(topic, status, orderId,
                (orderId + ":" + status).getBytes(StandardCharsets.UTF_8));
        producer.send(msg, new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message message, Object arg) {
                int h = arg.hashCode();
                return mqs.get(Math.floorMod(h, mqs.size()));
            }
        }, orderId);
    }

    public void close() {
        producer.shutdown();
    }
}
