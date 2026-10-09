package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * Day 2：同步发送封装。
 * 讲清 DefaultMQProducer 生命周期 + SendResult 各字段含义。
 */
public class SyncProducerService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQProducer producer;

    public SyncProducerService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.setRetryTimesWhenSendFailed(3);
        producer.start();
    }

    /** 同步发送：阻塞等到 Broker 返回结果 */
    public SendResult send(String topic, String tags, String keys, String body) throws Exception {
        Message msg = new Message(topic, tags, keys, body.getBytes(StandardCharsets.UTF_8));
        return producer.send(msg);
    }

    public void close() {
        producer.shutdown();
    }
}
