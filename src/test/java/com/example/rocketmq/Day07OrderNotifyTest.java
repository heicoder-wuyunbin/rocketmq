package com.example.rocketmq;

import com.example.rocketmq.service.OrderNotifyService;
import com.example.rocketmq.service.QuickConsumerService;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day07OrderNotifyTest {

    @Test
    void orderPaidTriggersNotifyAndAudit() throws Exception {
        AtomicInteger notify = new AtomicInteger();
        AtomicInteger audit = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(2);
        // 唯一订单号：消费端按 keys 过滤，只统计本次消息，避免历史消息干扰
        String orderId = "ORDER-1001-" + System.nanoTime();
        String userId = "U-888";

        QuickConsumerService cNotify = new QuickConsumerService(
                "day07-notify-" + System.nanoTime(), OrderNotifyService.TOPIC_NOTIFY, "*",
                (MessageExt m) -> {
                    if (orderId.equals(m.getKeys())) {
                        notify.incrementAndGet();
                        latch.countDown();
                    }
                });
        QuickConsumerService cAudit = new QuickConsumerService(
                "day07-audit-" + System.nanoTime(), OrderNotifyService.TOPIC_AUDIT, "*",
                (MessageExt m) -> {
                    if (orderId.equals(m.getKeys())) {
                        audit.incrementAndGet();
                        latch.countDown();
                    }
                });

        OrderNotifyService svc = new OrderNotifyService("day07-producer");
        try {
            svc.onOrderPaid(orderId, userId);
            boolean done = latch.await(15, TimeUnit.SECONDS);
            assertTrue(done);
            assertTrue(notify.get() >= 1 && audit.get() >= 1, "短信通知与审计日志都应被独立消费");
            System.out.println("[Day07] orderId=" + orderId + ", userId=" + userId
                    + ", notifyCount=" + notify.get()
                    + ", auditCount=" + audit.get()
                    + ", bothConsumed=" + (notify.get() >= 1 && audit.get() >= 1));
        } finally {
            svc.close();
            cNotify.close();
            cAudit.close();
        }
    }
}
