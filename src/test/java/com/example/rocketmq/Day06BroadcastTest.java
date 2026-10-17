package com.example.rocketmq;

import com.example.rocketmq.service.BroadcastService;
import com.example.rocketmq.service.SyncProducerService;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day06BroadcastTest {

    @Test
    void everyInstanceInGroupReceivesAll() throws Exception {
        String topic = "TopicDay06";
        // 两个广播实例，各自应收到 1 条
        AtomicInteger inst1 = new AtomicInteger();
        AtomicInteger inst2 = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(2);

        BroadcastService c1 = new BroadcastService("day06-broadcast-" + System.nanoTime(), topic,
                (MessageExt m) -> { inst1.incrementAndGet(); latch.countDown(); });
        BroadcastService c2 = new BroadcastService("day06-broadcast-" + System.nanoTime(), topic,
                (MessageExt m) -> { inst2.incrementAndGet(); latch.countDown(); });

        SyncProducerService producer = new SyncProducerService("day06-producer");
        try {
            producer.send(topic, "evt", "k1", "refresh-cache");
            boolean done = latch.await(15, TimeUnit.SECONDS);
            assertTrue(done);
            assertTrue(inst1.get() >= 1 && inst2.get() >= 1, "广播模式下两个实例都应收到消息");
            System.out.println("[Day06] topic=" + topic
                    + ", inst1Count=" + inst1.get()
                    + ", inst2Count=" + inst2.get()
                    + ", bothReceived=" + (inst1.get() >= 1 && inst2.get() >= 1));
        } finally {
            producer.close();
            c1.close();
            c2.close();
        }
    }
}
