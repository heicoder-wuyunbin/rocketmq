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

        QuickConsumerService cNotify = new QuickConsumerService(
                "day07-notify-" + System.nanoTime(), OrderNotifyService.TOPIC_NOTIFY, "*",
                (MessageExt m) -> { notify.incrementAndGet(); latch.countDown(); });
        QuickConsumerService cAudit = new QuickConsumerService(
                "day07-audit-" + System.nanoTime(), OrderNotifyService.TOPIC_AUDIT, "*",
                (MessageExt m) -> { audit.incrementAndGet(); latch.countDown(); });

        OrderNotifyService svc = new OrderNotifyService("day07-producer");
        try {
            svc.onOrderPaid("ORDER-1001", "U-888");
            boolean done = latch.await(15, TimeUnit.SECONDS);
            assertTrue(done);
            assertTrue(notify.get() >= 1 && audit.get() >= 1, "短信通知与审计日志都应被独立消费");
            System.out.println("[Day07] orderId=ORDER-1001, userId=U-888"
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
