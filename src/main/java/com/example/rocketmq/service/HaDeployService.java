package com.example.rocketmq.service;

/**
 * Day 14：高可用架构（配置描述，纯建模）。
 * - NameServer：无状态、互不通信，各自维护路由表
 * - Broker 主从：SYNC_FLUSH/ASYNC_FLUSH（刷盘）、同步双写/异步复制（一致性 vs 性能）
 * - DLedger：基于 Raft 的主从自动切换，解决"主挂人工切"
 */
public class HaDeployService {

    public enum FlushPolicy { SYNC, ASYNC }

    public enum ReplicaMode { MASTER_SLAVE, DLEDGER }

    private final int namesrvCount;
    private final int brokerGroups;
    private final int replicasPerGroup;
    private final FlushPolicy flushPolicy;
    private final ReplicaMode replicaMode;

    public HaDeployService(int namesrvCount, int brokerGroups, int replicasPerGroup,
                           FlushPolicy flushPolicy, ReplicaMode replicaMode) {
        this.namesrvCount = namesrvCount;
        this.brokerGroups = brokerGroups;
        this.replicasPerGroup = replicasPerGroup;
        this.flushPolicy = flushPolicy;
        this.replicaMode = replicaMode;
    }

    public static HaDeployService dledgerCluster(int brokerGroups) {
        return new HaDeployService(3, brokerGroups, 2, FlushPolicy.SYNC, ReplicaMode.DLEDGER);
    }

    public static HaDeployService masterSlave(int brokerGroups) {
        return new HaDeployService(2, brokerGroups, 1, FlushPolicy.ASYNC, ReplicaMode.MASTER_SLAVE);
    }

    public int totalBrokers() {
        return brokerGroups * (replicasPerGroup + 1); // 每组 1 主 + N 从
    }

    public int getNamesrvCount() { return namesrvCount; }
    public FlushPolicy getFlushPolicy() { return flushPolicy; }
    public ReplicaMode getReplicaMode() { return replicaMode; }
}
