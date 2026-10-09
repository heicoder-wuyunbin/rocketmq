package com.example.rocketmq.service;

/**
 * Day 13：消息积压治理（纯计算，决策辅助）。
 * 积压成因：消费慢 / 消费挂 / 突发流量。
 * 应急手段：扩容消费实例（同组加机器，队列被分摊）、提升消费线程数、
 * 必要时跳过非关键消息；再配合消费端限流保护下游。
 */
public class BacklogHandlerService {

    /** 推荐消费实例数 = 向上取整(积压量 / 单实例吞吐) */
    public static int recommendConsumerInstances(long backlog, long tpsPerConsumer) {
        if (tpsPerConsumer <= 0) return 1;
        return (int) Math.ceil((double) backlog / tpsPerConsumer);
    }

    /** 预计消费完所需秒数（按实例数 × 单实例吞吐） */
    public static long estimateClearSeconds(long backlog, int consumers, long tpsPerConsumer) {
        long total = (long) consumers * tpsPerConsumer;
        if (total <= 0) return Long.MAX_VALUE;
        return (long) Math.ceil((double) backlog / total);
    }

    /** 单实例消费线程数建议（不超过队列数，否则空转） */
    public static int recommendConsumeThreads(int queueCount) {
        return Math.max(1, Math.min(queueCount, 20));
    }
}
