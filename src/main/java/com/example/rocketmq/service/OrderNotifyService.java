package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * Day 7：普通消息实战——下单异步解耦。
 * 下单成功后，异步发"短信通知" + "审计日志"两条消息，
 * 主流程不阻塞，下游各自消费。
 */
public class OrderNotifyService {

    public static final String NAME_SERVER = "127.0.0.1:9876";
    public static final String TOPIC_NOTIFY = "topic-order-notify";
    public static final String TOPIC_AUDIT = "topic-order-audit";

    private final DefaultMQProducer producer;

    public OrderNotifyService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    /** 下单支付成功后的解耦动作 */
    public void onOrderPaid(String orderId, String userId) throws Exception {
        // 短信通知：单向即可（丢了最多用户没收到短信，不影响主流程）
        Message notify = new Message(TOPIC_NOTIFY, "sms",
                orderId, ("orderPaid:" + orderId).getBytes(StandardCharsets.UTF_8));
        producer.sendOneway(notify);

        // 审计日志：同步确认落库（审计不能丢）
        Message audit = new Message(TOPIC_AUDIT, "log",
                orderId, ("audit:userId=" + userId).getBytes(StandardCharsets.UTF_8));
        producer.send(audit);
    }

    public void close() {
        producer.shutdown();
    }
}
