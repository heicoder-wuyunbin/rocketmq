package com.example.rocketmq.service;

/**
 * Day 15：电商交易链路综合实战（编排层）。
 * 串联前面学到的能力：
 *   - 事务消息（下单 + 扣库存最终一致）
 *   - 顺序消息（订单状态流转）
 *   - 延迟消息（30 分钟未支付关单）
 *   - 消费幂等（加积分/通知防重）
 * 这里用真实服务组合出可运行链路；本类演示"幂等加积分"与"关单延迟等级"两个无 Broker 依赖的环节。
 */
public class TradeLinkService {

    private final RetryAndIdempotentService idempotent = new RetryAndIdempotentService();

    /** 加积分：同一订单只能加一次（幂等） */
    public String applyScore(String orderId, int score) {
        if (idempotent.handle("score:" + orderId)) {
            return "scored:" + score;
        }
        return "ignored:duplicate";
    }

    /** 关单延迟等级：30 分钟 → 等级 16 */
    public int closeOrderDelayLevel() {
        return DelayCloseOrderService.levelForMinutes(30);
    }
}
