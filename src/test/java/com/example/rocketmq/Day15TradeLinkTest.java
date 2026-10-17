package com.example.rocketmq;

import com.example.rocketmq.service.TradeLinkService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day15TradeLinkTest {

    @Test
    void tradeLinkComposesIdempotentAndDelay() {
        TradeLinkService svc = new TradeLinkService();
        // 加积分幂等：首次生效，重复忽略
        String first = svc.applyScore("ORDER-9001", 100);
        String duplicate = svc.applyScore("ORDER-9001", 100);
        // 关单走延迟消息等级 16
        int level = svc.closeOrderDelayLevel();
        assertEquals("scored:100", first);
        assertEquals("ignored:duplicate", duplicate);
        assertEquals(16, level);
        System.out.println("[Day15] orderId=ORDER-9001"
                + ", firstApplyScore=" + first
                + ", duplicateApplyScore=" + duplicate
                + ", closeOrderDelayLevel=" + level);
    }
}
