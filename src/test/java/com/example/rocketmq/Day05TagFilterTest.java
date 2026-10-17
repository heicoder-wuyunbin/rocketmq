package com.example.rocketmq;

import com.example.rocketmq.service.QuickConsumerService;
import com.example.rocketmq.service.TagFilterService;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day05TagFilterTest {

    @Test
    void onlySubscribedTagsAreReceived() throws Exception {
        String topic = "TopicDay05";
        List<String> got = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(2); // 只收 tagA / tagB
        // 唯一 key 前缀：消费端按 keys 过滤，只统计本次消息，避免历史消息干扰
        String runId = System.nanoTime() + "-";

        QuickConsumerService consumer = new QuickConsumerService(
                "day05-consumer-" + System.nanoTime(), topic, "tagA || tagB",
                (MessageExt m) -> {
                    String k = m.getKeys();
                    if (k != null && k.startsWith(runId)) {
                        got.add(m.getTags());
                        latch.countDown();
                    }
                });

        TagFilterService producer = new TagFilterService("day05-producer");
        try {
            producer.sendWithTag(topic, "tagA", runId + "k1", "A", null);
            producer.sendWithTag(topic, "tagB", runId + "k2", "B", null);
            producer.sendWithTag(topic, "tagC", runId + "k3", "C", null); // 不应被收到

            boolean done = latch.await(15, TimeUnit.SECONDS);
            assertTrue(done, "应只收到 tagA/tagB 两条");
            assertTrue(got.contains("tagA") && got.contains("tagB"));
            System.out.println("[Day05] subscribed=tagA || tagB, receivedTags=" + got
                    + ", tagCFilteredOut=" + !got.contains("tagC"));
        } finally {
            producer.close();
            consumer.close();
        }
    }
}
