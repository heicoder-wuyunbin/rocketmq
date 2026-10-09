package com.example.rocketmq;

import com.example.rocketmq.service.TradeLinkService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Day15TradeLinkTest {

    @Test
    void tradeLinkComposesIdempotentAndDelay() {
        TradeLinkService svc = new TradeLinkService();
        // 加积分幂等：首次生效，重复忽略
        assertEquals("scored:100", svc.applyScore("ORDER-9001", 100));
        assertEquals("ignored:duplicate", svc.applyScore("ORDER-9001", 100));
        // 关单走延迟消息等级 16
        assertEquals(16, svc.closeOrderDelayLevel());
    }
}
