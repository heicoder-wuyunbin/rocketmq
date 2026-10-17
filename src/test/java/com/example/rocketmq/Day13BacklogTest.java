package com.example.rocketmq;

import com.example.rocketmq.service.BacklogHandlerService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day13BacklogTest {

    @Test
    void recommendScalingForBacklog() {
        // 100 万积压，单实例 2000 TPS → 至少 500 个实例
        int instances = BacklogHandlerService.recommendConsumerInstances(1_000_000, 2000);
        assertEquals(500, instances);
        long secs = BacklogHandlerService.estimateClearSeconds(1_000_000, 500, 2000);
        assertTrue(secs <= 1, "实例足够时应秒级清空");
        System.out.println("[Day13] backlog=1,000,000, tpsPerInstance=2000"
                + ", recommendedInstances=" + instances
                + ", clearSeconds=" + secs);
    }

    @Test
    void consumeThreadsBoundedByQueue() {
        // 队列 4 个 → 线程建议不超过 4（多了空转）
        int threadsFor4 = BacklogHandlerService.recommendConsumeThreads(4);
        int threadsFor0 = BacklogHandlerService.recommendConsumeThreads(0);
        assertEquals(4, threadsFor4);
        assertEquals(1, threadsFor0);
        System.out.println("[Day13] queues=4 -> recommendThreads=" + threadsFor4
                + "; queues=0 -> recommendThreads=" + threadsFor0);
    }
}
