package com.example.rocketmq.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.function.Consumer;

/**
 * Day 3：并发消费者封装。
 * 注册一个监听器，把每条消息交给 handler 处理。
 */
public class QuickConsumerService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQPushConsumer consumer;

    public QuickConsumerService(String group, String topic, String subExpression,
                                Consumer<MessageExt> handler) throws Exception {
        consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(NAME_SERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topic, subExpression);
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, ctx) -> {
            for (MessageExt m : msgs) {
                handler.accept(m);
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }

    public void close() {
        consumer.shutdown();
    }
}
