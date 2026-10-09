package com.example.rocketmq;

import com.example.rocketmq.service.DelayCloseOrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day09DelayTest {

    @Test
    void thirtyMinutesMapsToLevel16() {
        // 订单 30 分钟未支付关单：对应延迟等级 16
        assertEquals(16, DelayCloseOrderService.levelForMinutes(30));
    }

    @Test
    void delayLevelInValidRange() {
        int level = DelayCloseOrderService.levelForMinutes(30);
        assertTrue(level >= 1 && level <= 18, "延迟等级必须在 1~18 之间");
    }
}
