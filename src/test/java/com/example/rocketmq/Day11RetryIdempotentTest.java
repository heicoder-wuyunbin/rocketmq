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
        assertTrue(svc.handle("deduct:ORDER-3001:100"));   // 第 1 次：处理
        assertFalse(svc.handle("deduct:ORDER-3001:100"));  // 第 2 次：去重
        assertFalse(svc.handle("deduct:ORDER-3001:100"));  // 第 3 次：去重
        assertEquals(1, svc.processedCount());
    }

    @Test
    void deadLetterNaming() {
        assertEquals("%RETRY%g1", DeadLetterService.retryTopic("g1"));
        assertEquals("%DLQ%g1", DeadLetterService.deadLetterTopic("g1"));
    }
}
