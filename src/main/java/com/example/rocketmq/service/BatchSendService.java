package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Day 10：批量消息。
 * 单次 send(List<Message>) 合并发送，省网络 RTT。
 * 限制：单批总大小 ≤ 1MB；同 Topic；不能有延迟/事务属性。
 */
public class BatchSendService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQProducer producer;

    public BatchSendService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    public void sendBatch(String topic, List<String> bodies) throws Exception {
        java.util.List<Message> list = new java.util.ArrayList<>();
        for (int i = 0; i < bodies.size(); i++) {
            list.add(new Message(topic, "batch", "k" + i,
                    bodies.get(i).getBytes(StandardCharsets.UTF_8)));
        }
        producer.send(list);
    }

    public void close() {
        producer.shutdown();
    }
}
