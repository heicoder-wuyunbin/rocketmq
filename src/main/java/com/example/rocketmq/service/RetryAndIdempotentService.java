package com.example.rocketmq.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Day 11：消费幂等（去重）。
 * 为什么必须做：网络重发、消费重试都会让同一条消息被投递多次。
 * 这里用"内存去重表"演示幂等三板斧之一（生产用 DB 唯一键 / Redis SETNX）。
 *
 * @return true=首次处理；false=重复消息，已去重
 */
public class RetryAndIdempotentService {

    private final Set<String> processed = ConcurrentHashMap.newKeySet();

    public boolean handle(String bizKey) {
        return processed.add(bizKey);
    }

    public int processedCount() {
        return processed.size();
    }
}
