package com.example.rocketmq.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * RocketMQ 通用配置：自封装一个 DefaultMQProducer Bean，统一注入 name-server 与 producer group。
 * 等价于 redis-demo 的 RedisConfig。
 *
 * 后续 Day 会在此基础上封装 RocketMQTemplate（发送便利方法），
 * Day1 的任务则是用"裸" DefaultMQProducer 理解底层生命周期。
 */
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name-server:127.0.0.1:9876}")
    private String nameServer;

    @Value("${rocketmq.producer.group:rocketmq-course-producer}")
    private String producerGroup;

    private DefaultMQProducer producer;

    @Bean
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        DefaultMQProducer p = new DefaultMQProducer(producerGroup);
        p.setNamesrvAddr(nameServer);
        // 发送失败重试次数（默认 2，可在此调大）
        p.setRetryTimesWhenSendFailed(2);
        p.start();
        this.producer = p;
        return p;
    }

    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
