package com.example.rocketmq;

import com.example.rocketmq.service.QuickConsumerService;
import com.example.rocketmq.service.SyncProducerService;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day03ConsumeTest {

    @Test
    void clusterConsumerReceivesMessage() throws Exception {
        String topic = "TopicDay03";
        CountDownLatch latch = new CountDownLatch(1);
        String[] received = new String[1];

        QuickConsumerService consumer = new QuickConsumerService(
                "day03-consumer-" + System.nanoTime(), topic, "*", (MessageExt m) -> {
            received[0] = new String(m.getBody(), StandardCharsets.UTF_8);
            latch.countDown();
        });

        SyncProducerService producer = new SyncProducerService("day03-producer");
        try {
            producer.send(topic, "order", "k1", "hello-consumer");
            boolean got = latch.await(15, TimeUnit.SECONDS);
            assertTrue(got, "消费者应在 15s 内收到消息（确认 Broker 已启动）");
            assertEquals("hello-consumer", received[0]);
        } finally {
            producer.close();
            consumer.close();
        }
    }
}
