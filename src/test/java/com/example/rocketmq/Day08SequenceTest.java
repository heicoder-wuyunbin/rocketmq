package com.example.rocketmq;

import com.example.rocketmq.service.OrderSequenceService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day08SequenceTest {

    @Test
    void sameOrderStatusKeptInOrder() throws Exception {
        String topic = "TopicDay08";
        List<String> seq = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(3);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("day08-consumer-" + System.nanoTime());
        consumer.setNamesrvAddr(OrderSequenceService.NAME_SERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topic, "*");
        consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
            for (MessageExt m : msgs) {
                seq.add(new String(m.getBody(), StandardCharsets.UTF_8));
                latch.countDown();
            }
            return ConsumeOrderlyStatus.SUCCESS;
        });
        consumer.start();

        OrderSequenceService svc = new OrderSequenceService("day08-producer");
        try {
            String orderId = "ORDER-2001";
            svc.sendOrderEvent(topic, orderId, "CREATED");
            svc.sendOrderEvent(topic, orderId, "PAID");
            svc.sendOrderEvent(topic, orderId, "SHIPPED");

            assertTrue(latch.await(15, TimeUnit.SECONDS));
            assertEquals(List.of("ORDER-2001:CREATED", "ORDER-2001:PAID", "ORDER-2001:SHIPPED"), seq);
        } finally {
            svc.close();
            consumer.shutdown();
        }
    }
}
