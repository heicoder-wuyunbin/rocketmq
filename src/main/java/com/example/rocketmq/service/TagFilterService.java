package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * Day 5：Tag 过滤 + Keys + 消息属性透传。
 * Tag 是 Topic 下的二级分类，用 subscribe 表达式筛选；
 * keys 用于运维查询；putUserProperty 透传业务字段。
 */
public class TagFilterService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQProducer producer;

    public TagFilterService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    public void sendWithTag(String topic, String tag, String keys, String body,
                            java.util.Map<String, String> properties) throws Exception {
        Message msg = new Message(topic, tag, keys, body.getBytes(StandardCharsets.UTF_8));
        if (properties != null) {
            properties.forEach(msg::putUserProperty);
        }
        producer.send(msg);
    }

    public void close() {
        producer.shutdown();
    }
}
