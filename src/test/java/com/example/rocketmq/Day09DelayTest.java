package com.example.rocketmq;

import com.example.rocketmq.service.DelayCloseOrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Day09DelayTest {

    @Test
    void thirtyMinutesMapsToLevel16() {
        // 订单 30 分钟未支付关单：对应延迟等级 16
        int level = DelayCloseOrderService.levelForMinutes(30);
        assertEquals(16, level);
        System.out.println("[Day09] minutes=30 -> delayLevel=" + level
                + " (对应 RocketMQ 延迟等级 16)");
    }

    @Test
    void delayLevelInValidRange() {
        int level = DelayCloseOrderService.levelForMinutes(30);
        assertTrue(level >= 1 && level <= 18, "延迟等级必须在 1~18 之间");
        System.out.println("[Day09] delayLevel=" + level + ", validRange=[1,18], inRange=" + (level >= 1 && level <= 18));
    }
}
