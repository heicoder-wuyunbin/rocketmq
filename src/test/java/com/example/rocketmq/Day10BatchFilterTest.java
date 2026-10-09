package com.example.rocketmq;

import com.example.rocketmq.service.BatchSendService;
import com.example.rocketmq.service.PropertyFilterService;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day10BatchFilterTest {

    @Test
    void batchSendAndSqlFilter() throws Exception {
        String topic = "TopicDay10";
        AtomicInteger vipCount = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);

        PropertyFilterService consumerSvc = new PropertyFilterService("day10-consumer");
        consumerSvc.consumeBySql("day10-consumer-" + System.nanoTime(), topic,
                "vip = 'true'", (MessageExt m) -> {
                    vipCount.incrementAndGet();
                    latch.countDown();
                });

        // 批量发 3 条普通 + 1 条 VIP
        BatchSendService batch = new BatchSendService("day10-producer");
        List<String> bodies = new ArrayList<>();
        for (int i = 0; i < 3; i++) bodies.add("normal-" + i);
        batch.sendBatch(topic, bodies);

        PropertyFilterService prod = new PropertyFilterService("day10-producer-2");
        prod.sendWithProperty(topic, "vip-user", Map.of("vip", "true", "age", "20"));

        boolean done = latch.await(15, TimeUnit.SECONDS);
        assertTrue(done, "VIP 消息应被 SQL 过滤消费到");

        batch.close();
        prod.close();
        consumerSvc.close();
    }
}
