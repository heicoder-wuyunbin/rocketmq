package com.example.rocketmq.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Day 10：SQL92 属性过滤。
 * 在消息上 putUserProperty，消费端用 MessageSelector.bySql 按属性筛选。
 * 前置：Broker 需开启 enablePropertyFilter=true。
 */
public class PropertyFilterService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQProducer producer;

    public PropertyFilterService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    public void sendWithProperty(String topic, String body, Map<String, String> props) throws Exception {
        Message msg = new Message(topic, "prop", body, body.getBytes(StandardCharsets.UTF_8));
        if (props != null) {
            props.forEach(msg::putUserProperty);
        }
        producer.send(msg);
    }

    /** 消费满足 SQL 表达式的消息，例如 "vip = 'true' AND age > 18" */
    public void consumeBySql(String group, String topic, String sql, Consumer<MessageExt> handler)
            throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(NAME_SERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topic, MessageSelector.bySql(sql));
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, ctx) -> {
            for (MessageExt m : msgs) {
                handler.accept(m);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }

    public void close() {
        producer.shutdown();
    }
}
