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
        // 唯一订单号：消费端按 keys 过滤，只收本次消息，避免历史消息干扰顺序断言
        String orderId = "ORDER-2001-" + System.nanoTime();
        List<String> seq = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(3);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("day08-consumer-" + System.nanoTime());
        consumer.setNamesrvAddr(OrderSequenceService.NAME_SERVER);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.subscribe(topic, "*");
        consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
            for (MessageExt m : msgs) {
                if (orderId.equals(m.getKeys())) {
                    seq.add(new String(m.getBody(), StandardCharsets.UTF_8));
                    latch.countDown();
                }
            }
            return ConsumeOrderlyStatus.SUCCESS;
        });
        consumer.start();

        OrderSequenceService svc = new OrderSequenceService("day08-producer");
        try {
            svc.sendOrderEvent(topic, orderId, "CREATED");
            svc.sendOrderEvent(topic, orderId, "PAID");
            svc.sendOrderEvent(topic, orderId, "SHIPPED");

            List<String> expected = List.of(
                    orderId + ":CREATED", orderId + ":PAID", orderId + ":SHIPPED");
            assertTrue(latch.await(15, TimeUnit.SECONDS));
            assertEquals(expected, seq);
            System.out.println("[Day08] orderId=" + orderId
                    + ", sequence=" + seq
                    + ", inOrder=" + seq.equals(expected));
        } finally {
            svc.close();
            consumer.shutdown();
        }
    }
}
