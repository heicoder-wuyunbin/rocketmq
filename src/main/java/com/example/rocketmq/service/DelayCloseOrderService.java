package com.example.rocketmq.service;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Day 9：延迟消息。
 * RocketMQ 只支持 18 个固定延迟等级（1s/5s/10s/.../2h），不能自定义任意时间。
 * 场景：订单 30 分钟未支付自动关单。
 */
public class DelayCloseOrderService {

    public static final String NAME_SERVER = "127.0.0.1:9876";
    public static final String TOPIC = "topic-order-delay";

    // 延迟等级对照：1=1s 2=5s 3=10s 4=30s 5=1m 6=2m 7=3m 8=4m 9=5m 10=6m
    // 11=7m 12=8m 13=9m 14=10m 15=20m 16=30m 17=1h 18=2h
    private static final int[] MINUTES_LEVEL = {0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 60, 120};

    private final DefaultMQProducer producer;

    public DelayCloseOrderService(String group) throws Exception {
        producer = new DefaultMQProducer(group);
        producer.setNamesrvAddr(NAME_SERVER);
        producer.start();
    }

    /** 调度关单：delayMinutes 会被向上取到最近的延迟等级 */
    public void scheduleClose(String orderId, int delayMinutes) throws Exception {
        Message msg = new Message(TOPIC, "close", orderId,
                ("close:" + orderId).getBytes(StandardCharsets.UTF_8));
        msg.setDelayTimeLevel(levelForMinutes(delayMinutes));
        producer.send(msg);
    }

    /** 把分钟映射到最近的延迟等级（简化实现） */
    public static int levelForMinutes(int minutes) {
        if (minutes <= 1) return 5;   // 1 分钟
        if (minutes <= 2) return 6;
        if (minutes <= 3) return 7;
        if (minutes <= 5) return 9;
        if (minutes <= 10) return 14;
        if (minutes <= 20) return 15;
        if (minutes <= 30) return 16;  // 30 分钟关单 → 等级 16
        if (minutes <= 60) return 17;
        return 18;
    }

    public void close() {
        producer.shutdown();
    }
}
