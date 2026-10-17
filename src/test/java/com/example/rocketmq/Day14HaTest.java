package com.example.rocketmq;

import com.example.rocketmq.service.HaDeployService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Day14HaTest {

    @Test
    void dledgerClusterTopology() {
        HaDeployService deploy = HaDeployService.dledgerCluster(3);
        assertEquals(3, deploy.getNamesrvCount());
        assertSame(HaDeployService.ReplicaMode.DLEDGER, deploy.getReplicaMode());
        // 3 组，每组 1 主 + 2 从 = 9 个 Broker
        assertEquals(9, deploy.totalBrokers());
        System.out.println("[Day14] Dledger集群: namesrvCount=" + deploy.getNamesrvCount()
                + ", replicaMode=" + deploy.getReplicaMode()
                + ", totalBrokers=" + deploy.totalBrokers()
                + " (3组 × 1主2从)");
    }

    @Test
    void masterSlaveTopology() {
        HaDeployService deploy = HaDeployService.masterSlave(2);
        assertSame(HaDeployService.ReplicaMode.MASTER_SLAVE, deploy.getReplicaMode());
        assertSame(HaDeployService.FlushPolicy.ASYNC, deploy.getFlushPolicy());
        assertEquals(4, deploy.totalBrokers()); // 2 组 × (1主+1从)
        System.out.println("[Day14] 主从集群: replicaMode=" + deploy.getReplicaMode()
                + ", flushPolicy=" + deploy.getFlushPolicy()
                + ", totalBrokers=" + deploy.totalBrokers()
                + " (2组 × 1主1从)");
    }
}
