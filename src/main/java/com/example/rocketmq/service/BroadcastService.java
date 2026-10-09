package com.example.rocketmq.service;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

import java.util.List;
import java.util.function.Consumer;

/**
 * Day 6：广播模式消费者。
 * 同组每个实例都收到全量消息（集群模式是分摊）。
 * 典型场景：多节点本地缓存刷新。
 */
public class BroadcastService {

    public static final String NAME_SERVER = "127.0.0.1:9876";

    private final DefaultMQPushConsumer consumer;

    public BroadcastService(String group, String topic, Consumer<MessageExt> handler) throws Exception {
        consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(NAME_SERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setMessageModel(MessageModel.BROADCASTING);
        consumer.subscribe(topic, "*");
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
