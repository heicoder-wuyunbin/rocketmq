package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * Day 4：三种发送方式封装。
 * - sync：等结果，重要业务
 * - async：高吞吐，SendCallback 收结果
 * - oneway：不关心结果，日志型
 */
public class SendModeService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQProducer producer;

    public SendModeService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    public SendResult syncSend(String topic, String tags, String body) throws Exception {
        return producer.send(new Message(topic, tags, body.getBytes(StandardCharsets.UTF_8)));
    }

    public void asyncSend(String topic, String tags, String body, SendCallback callback) throws Exception {
        producer.send(new Message(topic, tags, body.getBytes(StandardCharsets.UTF_8)), callback);
    }

    public void onewaySend(String topic, String tags, String body) throws Exception {
        producer.sendOneway(new Message(topic, tags, body.getBytes(StandardCharsets.UTF_8)));
    }

    public void close() {
        producer.shutdown();
    }
}
