package com.example.rocketmq.service;

/**
 * Day 11：死信队列（DLQ）命名规则。
 * 并发消费失败重试 16 次（退避）后仍失败 → 进入 %DLQ%消费组 主题，需人工处理。
 */
public class DeadLetterService {

    /** 重试主题：%RETRY%group */
    public static String retryTopic(String group) {
        return "%RETRY%" + group;
    }

    /** 死信主题：%DLQ%group */
    public static String deadLetterTopic(String group) {
        return "%DLQ%" + group;
    }
}
