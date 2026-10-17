package com.example.rocketmq;

import com.example.rocketmq.service.DeadLetterService;
import com.example.rocketmq.service.RetryAndIdempotentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day11RetryIdempotentTest {

    @Test
    void duplicateMessageHandledOnlyOnce() {
        RetryAndIdempotentService svc = new RetryAndIdempotentService();
        // 模拟同一条扣款消息被重试投递 3 次
        boolean first = svc.handle("deduct:ORDER-3001:100");   // 第 1 次：处理
        boolean second = svc.handle("deduct:ORDER-3001:100");  // 第 2 次：去重
        boolean third = svc.handle("deduct:ORDER-3001:100");   // 第 3 次：去重
        int count = svc.processedCount();
        assertTrue(first);
        assertFalse(second);
        assertFalse(third);
        assertEquals(1, count);
        System.out.println("[Day11] msg=deduct:ORDER-3001:100 (重投 3 次)"
                + ", 1stProcessed=" + first
                + ", 2ndDeduplicated=" + !second
                + ", 3rdDeduplicated=" + !third
                + ", processedCount=" + count);
    }

    @Test
    void deadLetterNaming() {
        String retry = DeadLetterService.retryTopic("g1");
        String dlq = DeadLetterService.deadLetterTopic("g1");
        assertEquals("%RETRY%g1", retry);
        assertEquals("%DLQ%g1", dlq);
        System.out.println("[Day11] consumerGroup=g1"
                + ", retryTopic=" + retry
                + ", deadLetterTopic=" + dlq);
    }
}
